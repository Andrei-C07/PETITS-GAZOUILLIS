package cgodin.qc.ca.petitgazouillis.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cgodin.qc.ca.petitgazouillis.data.models.UserProfile
import cgodin.qc.ca.petitgazouillis.data.repository.ProfileRepository
import cgodin.qc.ca.petitgazouillis.data.utils.Resource
import kotlinx.coroutines.launch

class AuthorProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _profileState = MutableLiveData<Resource<UserProfile>>()
    val profileState: LiveData<Resource<UserProfile>> = _profileState

    private val _followState = MutableLiveData<Resource<Boolean>>()
    val followState: LiveData<Resource<Boolean>> = _followState

    fun loadUser(id: Int) {
        viewModelScope.launch {
            _profileState.value = Resource.Loading()
            _profileState.value = repository.fetchUser(id)
        }
    }

    fun follow(userId: Int) {
        viewModelScope.launch {
            _followState.value = Resource.Loading()
            val res = repository.follow(userId)
            if (res is Resource.Success) {
                _followState.value = Resource.Success(true)
                refreshUser(userId)
            } else if (res is Resource.Error) {
                _followState.value = Resource.Error(res.message ?: "Erreur")
            }
        }
    }

    fun unfollow(userId: Int) {
        viewModelScope.launch {
            _followState.value = Resource.Loading()
            val res = repository.unfollow(userId)
            if (res is Resource.Success) {
                _followState.value = Resource.Success(false)
                refreshUser(userId)
            } else if (res is Resource.Error) {
                _followState.value = Resource.Error(res.message ?: "Erreur")
            }
        }
    }

    private fun refreshUser(id: Int) {
        viewModelScope.launch {
            _profileState.value = repository.fetchUser(id)
        }
    }
}

class AuthorProfileViewModelFactory(
    private val repository: ProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthorProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthorProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
