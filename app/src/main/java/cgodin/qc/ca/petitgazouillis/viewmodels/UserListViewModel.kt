package cgodin.qc.ca.petitgazouillis.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cgodin.qc.ca.petitgazouillis.data.models.MessageResponse
import cgodin.qc.ca.petitgazouillis.data.models.UserProfile
import cgodin.qc.ca.petitgazouillis.data.repository.ProfileRepository
import cgodin.qc.ca.petitgazouillis.data.utils.Resource
import kotlinx.coroutines.launch

class UserListViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _users = MutableLiveData<Resource<List<UserProfile>>>()
    val users: LiveData<Resource<List<UserProfile>>> = _users

    private val _followResult = MutableLiveData<Resource<MessageResponse>>()
    val followResult: LiveData<Resource<MessageResponse>> = _followResult

    fun loadUsers() {
        viewModelScope.launch {
            _users.value = Resource.Loading()
            _users.value = repository.fetchUsers()
        }
    }

    fun follow(userId: Int) {
        viewModelScope.launch {
            _followResult.value = Resource.Loading()
            val res = repository.follow(userId)
            _followResult.value = res
            if (res is Resource.Success) {
                loadUsers()
            }
        }
    }

    fun unfollow(userId: Int) {
        viewModelScope.launch {
            _followResult.value = Resource.Loading()
            val res = repository.unfollow(userId)
            _followResult.value = res
            if (res is Resource.Success) {
                loadUsers()
            }
        }
    }
}

class UserListViewModelFactory(
    private val repository: ProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
