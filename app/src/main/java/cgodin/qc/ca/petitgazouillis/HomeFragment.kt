package cgodin.qc.ca.petitgazouillis

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import cgodin.qc.ca.petitgazouillis.databinding.FragmentHomeBinding
import cgodin.qc.ca.petitgazouillis.ui.PostAdapter


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PostAdapter

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

        binding.btnFilterAll.setOnClickListener {
            // TODO: ViewModel.loadPosts("all")
        }

        binding.btnFilterFollowed.setOnClickListener {
            // TODO: ViewModel.loadPosts("followed")
        }

        binding.btnFilterMine.setOnClickListener {
            // TODO: ViewModel.loadPosts("me")
        }

        binding.btnNext.setOnClickListener {
            // TODO: ViewModel.nextPage()
        }

        binding.btnPrev.setOnClickListener {
            // TODO: ViewModel.prevPage()
        }

        binding.fabCreatePost.setOnClickListener {
            // TODO: navigate to CreatePostFragment
        }
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