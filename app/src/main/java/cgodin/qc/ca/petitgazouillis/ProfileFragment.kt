package cgodin.qc.ca.petitgazouillis

import android.Manifest
import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import cgodin.qc.ca.petitgazouillis.data.api.RetrofitClient
import cgodin.qc.ca.petitgazouillis.data.repository.ProfileRepository
import cgodin.qc.ca.petitgazouillis.data.session.SessionManager
import cgodin.qc.ca.petitgazouillis.data.utils.Resource
import cgodin.qc.ca.petitgazouillis.databinding.FragmentProfileBinding
import cgodin.qc.ca.petitgazouillis.viewmodels.ProfileViewModel
import cgodin.qc.ca.petitgazouillis.viewmodels.ProfileViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import androidx.navigation.navOptions
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var sessionManager: SessionManager
    private var initialProfileLoaded = false
    private val baseUrl = "http://10.0.2.2:8000"
    private var currentPhotoFile: File? = null

    private var currentPhotoUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handleSelectedImage(it) }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), getString(R.string.toast_camera_denied), Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            handleSelectedImage(currentPhotoUri!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        val api = RetrofitClient.create { sessionManager.getToken() }
        val repo = ProfileRepository(api)
        val factory = ProfileViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        setupUi()
        observeViewModel()

        viewModel.loadProfile()
    }

    private fun setupUi() {
        binding.btnBackProfile.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnPickGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.btnTakePhoto.setOnClickListener {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.btnSaveProfile.setOnClickListener {
            val newName = binding.inputDisplayName.text?.toString()?.trim().orEmpty()
            val oldPwd = binding.inputOldPassword.text?.toString()?.trim().orEmpty()
            val newPwd = binding.inputNewPassword.text?.toString()?.trim().orEmpty()

            var didSomething = false

            if (newName.isNotEmpty()) {
                viewModel.updateName(newName)
                didSomething = true
            }

            if (oldPwd.isNotEmpty() && newPwd.isNotEmpty()) {
                viewModel.updatePassword(oldPwd, newPwd)
                didSomething = true
            }

            if (!didSomething) {
                Toast.makeText(requireContext(), getString(R.string.toast_no_changes), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.clear()
            findNavController().navigate(
                R.id.loginFragment,
                null,
                navOptions {
                    popUpTo(R.id.nav_graph) { inclusive = true }
                }
            )
        }
    }

    private fun observeViewModel() {
        viewModel.profileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> binding.btnSaveProfile.isEnabled = false
                is Resource.Success -> {
                    binding.btnSaveProfile.isEnabled = true
                    state.data?.let { profile ->
                        if (binding.inputDisplayName.text.isNullOrEmpty()) {
                            binding.inputDisplayName.setText(profile.nom_utilisateur)
                        }
                        profile.photo_url?.let { loadPhoto(it) }
                        if (initialProfileLoaded) {
                            Toast.makeText(requireContext(), getString(R.string.toast_profile_updated), Toast.LENGTH_SHORT).show()
                        }
                        initialProfileLoaded = true
                    }
                }
                is Resource.Error -> {
                    binding.btnSaveProfile.isEnabled = true
                    Toast.makeText(requireContext(), state.message ?: getString(R.string.error_profile_default), Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        viewModel.passwordState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), getString(R.string.toast_password_updated), Toast.LENGTH_SHORT).show()
                    binding.inputOldPassword.text?.clear()
                    binding.inputNewPassword.text?.clear()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), state.message ?: getString(R.string.error_password_default), Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        viewModel.photoState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    state.data?.photo_url?.let { loadPhoto(it) }
                    Toast.makeText(requireContext(), getString(R.string.toast_photo_updated), Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), state.message ?: getString(R.string.error_photo_default), Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        binding.imgProfile.setImageURI(uri)
        val directFile = if (uri == currentPhotoUri) currentPhotoFile else null
        prepareFilePart("photo", uri, directFile)?.let { part ->
            viewModel.uploadPhoto(part)
        } ?: Toast.makeText(requireContext(), getString(R.string.toast_read_image_failed), Toast.LENGTH_SHORT).show()
    }

    private fun loadPhoto(url: String) {
        val cleaned = if (url.startsWith("http")) url else "$baseUrl${url.trim()}"
        Glide.with(this)
            .load(cleaned)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .dontAnimate()
            .into(binding.imgProfile)
    }

    private fun prepareFilePart(partName: String, uri: Uri, directFile: File? = null): MultipartBody.Part? {
        val contentResolver = requireContext().contentResolver

        val fileToSend = if (directFile?.exists() == true && directFile.length() > 0) {
            directFile
        } else {
            val fileName = queryFileName(contentResolver, uri)
            val tempFile = File(requireContext().cacheDir, fileName)
            try {
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                } ?: return null

                if (tempFile.length() == 0L) return null
            } catch (e: Exception) {
                return null
            }
            tempFile
        }

        val requestFile = fileToSend.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, fileToSend.name, requestFile)
    }

    private fun queryFileName(resolver: ContentResolver, uri: Uri): String {
        var name = "photo_${System.currentTimeMillis()}.jpg"
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            resolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    name = cursor.getString(nameIndex)
                }
            }
        } else if (uri.scheme == ContentResolver.SCHEME_FILE) {
            name = File(uri.path!!).name
        }
        return name
    }

    private fun openCamera() {
        val photoFile = File.createTempFile(
            "camera_${System.currentTimeMillis()}_",
            ".jpg",
            requireContext().cacheDir
        )

        currentPhotoFile = photoFile

        currentPhotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(currentPhotoUri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
