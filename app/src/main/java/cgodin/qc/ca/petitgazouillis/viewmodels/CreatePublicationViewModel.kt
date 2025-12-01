package cgodin.qc.ca.petitgazouillis.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cgodin.qc.ca.petitgazouillis.data.models.CreatePublicationResponse
import cgodin.qc.ca.petitgazouillis.data.models.PhotoUploadResponse
import cgodin.qc.ca.petitgazouillis.data.repository.PublicationRepository
import cgodin.qc.ca.petitgazouillis.data.utils.Resource
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class CreatePublicationViewModel(
    private val repository: PublicationRepository
) : ViewModel() {

    // ----- LiveData for observing states -----
    private val _postState = MutableLiveData<Resource<CreatePublicationResponse>>()
    val postState: LiveData<Resource<CreatePublicationResponse>> = _postState

    private val _postPhotoState = MutableLiveData<Resource<PhotoUploadResponse>>()
    val postPhotoState: LiveData<Resource<PhotoUploadResponse>> = _postPhotoState

    private val _avatarPhotoState = MutableLiveData<Resource<PhotoUploadResponse>>()
    val avatarPhotoState: LiveData<Resource<PhotoUploadResponse>> = _avatarPhotoState

    var uploadedPostPhotoUrl: String? = null
    var uploadedAvatarPhotoUrl: String? = null

    fun publierPost(contenu: String) {
        viewModelScope.launch {
            _postState.value = Resource.Loading()
            val result = repository.createPost(
                photo_url = uploadedPostPhotoUrl,  // ONLY post photo
                contenu = contenu
            )
            _postState.value = result
        }
    }

    fun uploadPostPhoto(photoPart: MultipartBody.Part) {
        viewModelScope.launch {
            _postPhotoState.value = Resource.Loading()
            val result = repository.uploadPhoto(photoPart)
            _postPhotoState.value = result

            if (result is Resource.Success) {
                uploadedPostPhotoUrl = result.data?.photo_url
            }
        }
    }

    fun uploadAvatarPhoto(photoPart: MultipartBody.Part) {
        viewModelScope.launch {
            _avatarPhotoState.value = Resource.Loading()
            val result = repository.uploadPhoto(photoPart)
            _avatarPhotoState.value = result

            if (result is Resource.Success) {
                uploadedAvatarPhotoUrl = result.data?.photo_url
            }
        }
    }
}
