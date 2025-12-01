package cgodin.qc.ca.petitgazouillis.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import cgodin.qc.ca.petitgazouillis.R
import cgodin.qc.ca.petitgazouillis.data.api.RetrofitClient
import cgodin.qc.ca.petitgazouillis.data.repository.PublicationRepository
import cgodin.qc.ca.petitgazouillis.data.session.SessionManager
import cgodin.qc.ca.petitgazouillis.data.utils.Resource
import cgodin.qc.ca.petitgazouillis.viewmodels.CreatePublicationViewModel
import cgodin.qc.ca.petitgazouillis.viewmodels.CreatePublicationViewModelFactory

class AddPostFragment : Fragment() {

    private lateinit var viewModel: CreatePublicationViewModel
    private lateinit var sessionManager: SessionManager

    private lateinit var etContenu: EditText
    private lateinit var btnPublier: Button
    private lateinit var fabBack: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etContenu = view.findViewById(R.id.etContenu)
        btnPublier = view.findViewById(R.id.btnPublish)
        fabBack = view.findViewById(R.id.fabBack)

        fabBack.setOnClickListener {
            findNavController().navigate(R.id.action_addPostFragment_to_homeFragment)
        }

        val api = RetrofitClient.create { sessionManager.getToken() }
        val repo = PublicationRepository(api)
        viewModel = ViewModelProvider(
            this,
            CreatePublicationViewModelFactory(repo)
        )[CreatePublicationViewModel::class.java]

        btnPublier.setOnClickListener {
            val contenu = etContenu.text.toString().trim()
            if (contenu.isEmpty()) {
                Toast.makeText(requireContext(), "Le contenu est vide", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.ajouterPost(contenu)
        }

        viewModel.postState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    btnPublier.isEnabled = false
                }

                is Resource.Success -> {
                    btnPublier.isEnabled = true

                    Toast.makeText(requireContext(), "Publication envoyée!", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(R.id.action_addPostFragment_to_homeFragment)
                }

                is Resource.Error -> {
                    btnPublier.isEnabled = true

                    Toast.makeText(
                        requireContext(),
                        "Erreur: ${state.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
