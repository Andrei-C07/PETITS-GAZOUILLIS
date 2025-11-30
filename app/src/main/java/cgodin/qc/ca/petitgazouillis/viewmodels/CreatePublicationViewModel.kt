package cgodin.qc.ca.petitgazouillis.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cgodin.qc.ca.petitgazouillis.data.models.CreatePublicationResponse
import cgodin.qc.ca.petitgazouillis.data.repository.PublicationRepository
import cgodin.qc.ca.petitgazouillis.data.utils.Resource
import kotlinx.coroutines.launch

class CreatePublicationViewModel(
    private val repository: PublicationRepository
) : ViewModel() {
    private val  _postState = MutableLiveData<Resource<CreatePublicationResponse>>()

    val postState: LiveData<Resource<CreatePublicationResponse>> = _postState

    fun ajouterPost(contenu: String){
        viewModelScope.launch {
            _postState.value = Resource.Loading()

            val result = repository.createPost(
                contenu = contenu
            )

            _postState.value = result
        }
    }
}