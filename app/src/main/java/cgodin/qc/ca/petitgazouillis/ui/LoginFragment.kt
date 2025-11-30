package cgodin.qc.ca.petitgazouillis.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import cgodin.qc.ca.petitgazouillis.R
import cgodin.qc.ca.petitgazouillis.data.api.RetrofitClient
import cgodin.qc.ca.petitgazouillis.data.repository.AuthRepository
import cgodin.qc.ca.petitgazouillis.data.session.SessionManager
import cgodin.qc.ca.petitgazouillis.data.utils.Resource
import cgodin.qc.ca.petitgazouillis.viewmodels.LoginViewModel
import cgodin.qc.ca.petitgazouillis.viewmodels.LoginViewModelFactory

class LoginFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Create API + Repo
        val api = RetrofitClient.create { sessionManager.getToken() }
        val repo = AuthRepository(api)

        // Create ViewModel
        loginViewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(repo, sessionManager)
        )[LoginViewModel::class.java]

        val usernameField = view.findViewById<EditText>(R.id.usernameInput)
        val passwordField = view.findViewById<EditText>(R.id.PasswordInput)
        val loginButton = view.findViewById<Button>(R.id.btnConnect)

        loginButton.setOnClickListener {
            val username = usernameField.text.toString()
            val password = passwordField.text.toString()

            loginViewModel.login(username, password)
        }

        observeLoginState()
    }

    private fun observeLoginState() {
        loginViewModel.loginState.observe(viewLifecycleOwner) { state ->

            when (state) {
                is Resource.Loading -> {
                    // show progress
                }

                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Connexion réussie", Toast.LENGTH_SHORT).show()

                    // NAVIGATE TO HOME/FEED
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }

                is Resource.Error -> {
                    Toast.makeText(requireContext(), state.message ?: "Erreur", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }
}