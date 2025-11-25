package cgodin.qc.ca.petitgazouillis.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cgodin.qc.ca.petitgazouillis.data.models.Publication
import cgodin.qc.ca.petitgazouillis.data.repository.PublicationRepository
import cgodin.qc.ca.petitgazouillis.data.utils.Resource
import cgodin.qc.ca.petitgazouillis.ui.PostUI
import kotlinx.coroutines.launch

class PostViewModel(
    private val repository: PublicationRepository
) : ViewModel() {

    private val _publications = MutableLiveData<Resource<List<PostUI>>>()
    val publications: LiveData<Resource<List<PostUI>>> = _publications

    private val _totalPages = MutableLiveData<Int>()
    val totalPages: LiveData<Int> = _totalPages

    private var page = 1
    private var currentFilter = "all"
    private var lastUserId: Int? = null

    fun loadPublications(filter: String = currentFilter, userId: Int? = null) {
        currentFilter = filter

        if (filter == "me" && userId != null) {
            lastUserId = userId
        }

        viewModelScope.launch {
            _publications.value = Resource.Loading()

            val result = when (filter) {
                "all" -> repository.getAll(page)
                "followed" -> repository.getFollowed(page)
                "me" -> repository.getByUser(lastUserId!!, page)
                else -> repository.getAll(page)
            }

            if (result is Resource.Success) {
                val body = result.data

                _totalPages.value = body?.total_pages ?: 1

                val uiList = body?.data?.map { pub ->
                    PostUI(
                        username = pub.auteur,
                        text = pub.content
                    )
                } ?: emptyList()

                _publications.value = Resource.Success(uiList)

            } else if (result is Resource.Error) {
                _publications.value = Resource.Error(result.message!!)
            }
        }
    }

    fun nextPage() {
        page++
        loadPublications()
    }

    fun prevPage() {
        if (page > 1) {
            page--
            loadPublications()
        }
    }

    fun getCurrentPage() = page
}

