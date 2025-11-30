package cgodin.qc.ca.petitgazouillis.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cgodin.qc.ca.petitgazouillis.data.repository.PublicationRepository

class CreatePublicationViewModelFactory(
    private val publicationRepository: PublicationRepository,
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePublicationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreatePublicationViewModel(publicationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}