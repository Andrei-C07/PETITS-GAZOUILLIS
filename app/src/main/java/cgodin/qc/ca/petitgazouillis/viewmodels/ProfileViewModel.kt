package cgodin.qc.ca.petitgazouillis.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cgodin.qc.ca.petitgazouillis.data.models.MessageResponse
import cgodin.qc.ca.petitgazouillis.data.models.PhotoUploadResponse
import cgodin.qc.ca.petitgazouillis.data.models.UserProfile
import cgodin.qc.ca.petitgazouillis.data.repository.ProfileRepository
import cgodin.qc.ca.petitgazouillis.data.utils.Resource
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class ProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _profileState = MutableLiveData<Resource<UserProfile>>()
    val profileState: LiveData<Resource<UserProfile>> = _profileState

    private val _passwordState = MutableLiveData<Resource<MessageResponse>>()
    val passwordState: LiveData<Resource<MessageResponse>> = _passwordState

    private val _photoState = MutableLiveData<Resource<PhotoUploadResponse>>()
    val photoState: LiveData<Resource<PhotoUploadResponse>> = _photoState

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = Resource.Loading()
            _profileState.value = repository.fetchProfile()
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            _profileState.value = Resource.Loading()
            _profileState.value = repository.updateName(name)
        }
    }

    fun updatePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _passwordState.value = Resource.Loading()
            _passwordState.value = repository.updatePassword(oldPassword, newPassword)
        }
    }

    fun uploadPhoto(part: MultipartBody.Part) {
        viewModelScope.launch {
            _photoState.value = Resource.Loading()
            val res = repository.uploadPhoto(part)
            _photoState.value = res

            if (res is Resource.Success) {
                val current = (_profileState.value as? Resource.Success)?.data
                if (current != null && res.data?.photo_url != null) {
                    _profileState.value = Resource.Success(
                        current.copy(photo_url = res.data!!.photo_url)
                    )
                }
                loadProfile()
            }
        }
    }
}

class ProfileViewModelFactory(
    private val repository: ProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
