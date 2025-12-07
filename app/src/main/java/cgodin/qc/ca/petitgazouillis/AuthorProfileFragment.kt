package cgodin.qc.ca.petitgazouillis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import cgodin.qc.ca.petitgazouillis.data.session.SessionManager
import cgodin.qc.ca.petitgazouillis.data.api.RetrofitClient
import cgodin.qc.ca.petitgazouillis.data.repository.ProfileRepository
import cgodin.qc.ca.petitgazouillis.data.utils.Resource
import cgodin.qc.ca.petitgazouillis.databinding.FragmentAuthorProfileBinding
import cgodin.qc.ca.petitgazouillis.viewmodels.AuthorProfileViewModel
import cgodin.qc.ca.petitgazouillis.viewmodels.AuthorProfileViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class AuthorProfileFragment : Fragment() {

    private var _binding: FragmentAuthorProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AuthorProfileViewModel
    private lateinit var session: SessionManager
    private var viewedUserId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthorProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        val api = RetrofitClient.create(requireContext().applicationContext) { session.getToken() }
        val repo = ProfileRepository(api)
        val factory = AuthorProfileViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[AuthorProfileViewModel::class.java]

        viewedUserId = arguments?.getInt("userId") ?: -1
        val usernameArg = arguments?.getString("username").orEmpty()

        binding.authorTitle.text = getString(R.string.author_title_prefix, usernameArg.ifBlank { getString(R.string.profile_title) })

        observe()

        val currentUserId = session.getUserId()

        if (viewedUserId != -1) {
            if (viewedUserId == currentUserId) {
                view.post {
                    findNavController().navigate(R.id.profileFragment)
                }
            } else {
                viewModel.loadUser(viewedUserId)
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
        }



    }

    private fun observe() {
        viewModel.profileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    val profile = state.data ?: return@observe
                    binding.authorUsername.text = profile.nom_utilisateur
                    binding.authorFollowers.text = getString(R.string.followers_count_format, profile.followers_count ?: 0)
                    binding.authorFollowing.text = getString(R.string.following_count_format, profile.following_count ?: 0)
                    setupFollowButton(profile)
                    profile.photo_url?.let { url ->
                        val full = if (url.startsWith("http")) url else "http://10.0.2.2:8000${url.trim()}"
                        Glide.with(this)
                            .load(full)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .circleCrop()
                            .placeholder(R.drawable.ic_person_placeholder)
                            .error(R.drawable.ic_person_placeholder)
                            .into(binding.authorAvatar)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), mapNetworkError(state.message), Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        viewModel.followState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    // state.data true -> now following, false -> unfollowed
                    val isNowFollowing = state.data ?: false
                    val txt = if (isNowFollowing) getString(R.string.following_action_success) else getString(R.string.unfollow_action_success)
                    Toast.makeText(requireContext(), txt, Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), mapNetworkError(state.message), Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun setupFollowButton(profile: cgodin.qc.ca.petitgazouillis.data.models.UserProfile) {
        val currentUserId = session.getUserId()
        if (profile.id == currentUserId) {
            binding.btnFollow.visibility = View.GONE
            return
        }
        binding.btnFollow.visibility = View.VISIBLE
        val isFollowing = profile.is_following == true
        binding.btnFollow.text = if (isFollowing) getString(R.string.unfollow) else getString(R.string.follow)
        binding.btnFollow.isEnabled = true
        binding.btnFollow.setOnClickListener {
            if (isFollowing) {
                viewModel.unfollow(profile.id)
            } else {
                viewModel.follow(profile.id)
            }
        }
    }

    private fun mapNetworkError(message: String?): String {
        val lower = message?.lowercase() ?: ""
        return when {
            lower.contains("no_internet") || lower.contains("failed to connect") || lower.contains("unable to resolve host") || lower.contains("timeout") || lower.contains("refused") ->
                getString(R.string.error_service_unavailable)
            else -> message ?: getString(R.string.error_generic)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
