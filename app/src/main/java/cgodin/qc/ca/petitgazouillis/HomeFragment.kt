package cgodin.qc.ca.petitgazouillis

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cgodin.qc.ca.petitgazouillis.data.api.ApiService
import cgodin.qc.ca.petitgazouillis.data.api.RetrofitClient
import cgodin.qc.ca.petitgazouillis.data.repository.PublicationRepository
import cgodin.qc.ca.petitgazouillis.data.session.SessionManager
import cgodin.qc.ca.petitgazouillis.data.utils.Resource
import cgodin.qc.ca.petitgazouillis.databinding.FragmentHomeBinding
import cgodin.qc.ca.petitgazouillis.ui.PostAdapter
import cgodin.qc.ca.petitgazouillis.viewmodels.PostViewModel
import cgodin.qc.ca.petitgazouillis.viewmodels.PostViewModelFactory


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PostAdapter

    private lateinit var sessionManager: SessionManager

    private lateinit var postViewModel: PostViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        sessionManager = SessionManager(requireContext())
        Log.d("TOKEN", sessionManager.getToken().toString())
        val api = RetrofitClient.create { sessionManager.getToken() }
        val repo = PublicationRepository(api)
        val factory = PostViewModelFactory(repo)
        postViewModel = ViewModelProvider(this, factory)[PostViewModel::class.java]

        binding.btnFilterAll.setOnClickListener {
            postViewModel.loadPublications("all")
        }

        binding.btnFilterFollowed.setOnClickListener {
            postViewModel.loadPublications("followed")
        }

        binding.btnFilterMine.setOnClickListener {
            postViewModel.loadPublications("me", userId = sessionManager.getUserId())
        }

        binding.btnNext.setOnClickListener {
            postViewModel.nextPage()
        }

        binding.btnPrev.setOnClickListener {
            postViewModel.prevPage()
        }


        binding.fabCreatePost.setOnClickListener {
            // TODO: navigate to CreatePostFragment
        }
        postViewModel.totalPages.observe(viewLifecycleOwner) { total ->
            val current = postViewModel.getCurrentPage()
            binding.txtPageCounter.text = "Page $current / $total"

            binding.btnPrev.isEnabled = current > 1
            binding.btnNext.isEnabled = current < total
        }

        postViewModel.publications.observe(viewLifecycleOwner) { res ->
            when (res) {
                is Resource.Loading -> {}
                is Resource.Success -> adapter.submitList(res.data ?: emptyList())
                is Resource.Error -> {}
            }
        }


        postViewModel.loadPublications("all")
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter()
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPosts.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}