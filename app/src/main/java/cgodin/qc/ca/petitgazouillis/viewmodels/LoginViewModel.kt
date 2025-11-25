package cgodin.qc.ca.petitgazouillis.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cgodin.qc.ca.petitgazouillis.data.models.LoginRequest
import cgodin.qc.ca.petitgazouillis.data.models.LoginResponse
import cgodin.qc.ca.petitgazouillis.data.repository.AuthRepository
import cgodin.qc.ca.petitgazouillis.data.session.SessionManager
import cgodin.qc.ca.petitgazouillis.data.utils.Resource
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableLiveData<Resource<LoginResponse>>()
    val loginState: LiveData<Resource<LoginResponse>> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()

            val result = repository.login(
                LoginRequest(
                    nom_utilisateur = username,
                    mot_de_passe = password
                )
            )

            if (result is Resource.Success) {
                sessionManager.saveToken(result.data!!.token)
                sessionManager.saveUserId(result.data!!.user_id)
            }

            _loginState.value = result
        }
    }
}