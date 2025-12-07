package cgodin.qc.ca.petitgazouillis.ui

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import cgodin.qc.ca.petitgazouillis.R
import cgodin.qc.ca.petitgazouillis.data.api.RetrofitClient
import cgodin.qc.ca.petitgazouillis.data.repository.PublicationRepository
import cgodin.qc.ca.petitgazouillis.data.session.SessionManager
import cgodin.qc.ca.petitgazouillis.data.utils.Resource
import cgodin.qc.ca.petitgazouillis.databinding.FragmentAddPostBinding
import cgodin.qc.ca.petitgazouillis.databinding.FragmentProfileBinding
import cgodin.qc.ca.petitgazouillis.viewmodels.CreatePublicationViewModel
import cgodin.qc.ca.petitgazouillis.viewmodels.CreatePublicationViewModelFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class AddPostFragment : Fragment() {

    private lateinit var viewModel: CreatePublicationViewModel
    private lateinit var sessionManager: SessionManager

    private var _binding: FragmentAddPostBinding? = null
    private val binding get() = _binding!!

    private lateinit var etContenu: EditText
    private lateinit var btnPublier: Button
    private lateinit var fabBack: View

    private var currentPhotoFile: File? = null
    private var currentPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etContenu = binding.etContenu
        btnPublier = binding.btnPublish
        fabBack = binding.fabBack

        fabBack.setOnClickListener {
            findNavController().navigate(R.id.action_addPostFragment_to_homeFragment)
        }

        val api = RetrofitClient.create(requireContext().applicationContext) { sessionManager.getToken() }
        val repo = PublicationRepository(api)
        viewModel = ViewModelProvider(
            this,
            CreatePublicationViewModelFactory(repo)
        )[CreatePublicationViewModel::class.java]

        // Gallery button
        binding.btnGalerie.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        // Camera button
        binding.btnCamera.setOnClickListener {
            val photoFile = createImageFile()
            currentPhotoFile = photoFile

            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            currentPhotoUri = uri
            cameraLauncher.launch(uri)
        }

        btnPublier.setOnClickListener {
            val contenu = etContenu.text.toString().trim()
            if (contenu.isEmpty()) {
                Toast.makeText(requireContext(), "Le contenu est vide", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Use the POST photo URL only
            viewModel.publierPost(contenu)
        }

        // Observe post creation state
        viewModel.postState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> btnPublier.isEnabled = false
                is Resource.Success -> {
                    btnPublier.isEnabled = true
                    Toast.makeText(requireContext(), "Publication envoyée!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_addPostFragment_to_homeFragment)
                }
                is Resource.Error -> {
                    btnPublier.isEnabled = true
                    Toast.makeText(requireContext(), mapPostError(state.message), Toast.LENGTH_LONG).show()
                }
            }
        }

        // Observe post photo upload
        viewModel.postPhotoState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> { /* maybe show a progress */ }
                is Resource.Success -> Toast.makeText(requireContext(), "Photo prête!", Toast.LENGTH_SHORT).show()
                is Resource.Error -> Toast.makeText(requireContext(), mapPostError(state.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handleSelectedPostImage(it) }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            handleSelectedPostImage(currentPhotoUri!!)
        }
    }

    private fun createImageFile(): File {
        val fileName = "photo_${System.currentTimeMillis()}.jpg"
        val storageDir = requireContext().cacheDir
        return File(storageDir, fileName)
    }

    // --- handle only post images now ---
    private fun handleSelectedPostImage(uri: Uri) {
        binding.imgPost.setImageURI(uri)
        val directFile = if (uri == currentPhotoUri) currentPhotoFile else null
        prepareFilePart("photo", uri, directFile)?.let { part ->
            viewModel.uploadPostPhoto(part)
        } ?: Toast.makeText(requireContext(), getString(R.string.toast_read_image_failed), Toast.LENGTH_SHORT).show()
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

    private fun mapPostError(message: String?): String {
        val lower = message?.lowercase() ?: ""
        return when {
            lower.contains("no_internet") || lower.contains("failed to connect") || lower.contains("unable to resolve host") || lower.contains("timeout") || lower.contains("refused") ->
                getString(R.string.error_service_unavailable)
            lower.contains("format") || lower.contains("invalid") ->
                getString(R.string.error_invalid_format)
            else -> message ?: getString(R.string.error_generic)
        }
    }
}
