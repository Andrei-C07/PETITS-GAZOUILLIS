package cgodin.qc.ca.petitgazouillis.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cgodin.qc.ca.petitgazouillis.R
import cgodin.qc.ca.petitgazouillis.data.api.RetrofitClient
import cgodin.qc.ca.petitgazouillis.data.repository.ProfileRepository
import cgodin.qc.ca.petitgazouillis.data.session.SessionManager
import cgodin.qc.ca.petitgazouillis.data.utils.Resource
import cgodin.qc.ca.petitgazouillis.databinding.FragmentUserListBinding
import cgodin.qc.ca.petitgazouillis.viewmodels.UserListViewModel
import cgodin.qc.ca.petitgazouillis.viewmodels.UserListViewModelFactory

class UserListFragment : Fragment() {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!

    private lateinit var session: SessionManager
    private lateinit var viewModel: UserListViewModel
    private lateinit var adapter: UserListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        val api = RetrofitClient.create(requireContext().applicationContext) { session.getToken() }
        val repo = ProfileRepository(api)
        val factory = UserListViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[UserListViewModel::class.java]

        adapter = UserListAdapter(
            onUserClick = { user ->
                findNavController().navigate(
                    R.id.authorProfileFragment,
                    androidx.core.os.bundleOf(
                        "userId" to user.id,
                        "username" to user.nom_utilisateur
                    )
                )
            },
            onFollowToggle = { user ->
                if (user.is_following == true) {
                    viewModel.unfollow(user.id)
                } else {
                    viewModel.follow(user.id)
                }
            }
        )
        binding.recyclerUsers.adapter = adapter
        binding.recyclerUsers.layoutManager = LinearLayoutManager(requireContext())

        observe()
        viewModel.loadUsers()
    }

    private fun observe() {
        viewModel.users.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    // could add progress indicator
                }
                is Resource.Success -> {
                    val list = state.data ?: emptyList()
                    adapter.submit(list)
                    binding.emptyUsers.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    binding.recyclerUsers.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), mapError(state.message), Toast.LENGTH_SHORT).show()
                    binding.emptyUsers.visibility = View.VISIBLE
                    binding.recyclerUsers.visibility = View.GONE
                }
            }
        }

        viewModel.followResult.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Error -> Toast.makeText(requireContext(), mapError(state.message), Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
    }

    private fun mapError(message: String?): String {
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
