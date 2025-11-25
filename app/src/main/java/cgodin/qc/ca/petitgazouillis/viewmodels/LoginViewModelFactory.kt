package cgodin.qc.ca.petitgazouillis.viewmodels

import androidx.lifecycle.ViewModel
import cgodin.qc.ca.petitgazouillis.data.repository.AuthRepository
import cgodin.qc.ca.petitgazouillis.data.session.SessionManager
import androidx.lifecycle.ViewModelProvider

class LoginViewModelFactory(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}