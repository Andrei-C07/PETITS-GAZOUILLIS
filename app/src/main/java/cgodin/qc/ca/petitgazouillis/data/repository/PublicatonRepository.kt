package cgodin.qc.ca.petitgazouillis.data.repository

import cgodin.qc.ca.petitgazouillis.data.api.ApiService
import cgodin.qc.ca.petitgazouillis.data.models.CreatePublicationRequest
import cgodin.qc.ca.petitgazouillis.data.models.CreatePublicationResponse
import cgodin.qc.ca.petitgazouillis.data.models.PhotoUploadResponse
import cgodin.qc.ca.petitgazouillis.data.models.PublicationResponse
import cgodin.qc.ca.petitgazouillis.data.utils.Resource
import okhttp3.MultipartBody

class PublicationRepository(private val api: ApiService) {

    suspend fun getAll(page: Int): Resource<PublicationResponse> {
        return try {
            val res = api.getPublications(page)
            if (res.isSuccessful && res.body() != null) {
                Resource.Success(res.body()!!)
            } else {
                Resource.Error(res.errorBody()?.string() ?: "Erreur inconnue")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur réseau")
        }
    }

    suspend fun getFollowed(page: Int): Resource<PublicationResponse> {
        return try {
            val res = api.getFollowedPublications(page)
            if (res.isSuccessful && res.body() != null) {
                Resource.Success(res.body()!!)
            } else {
                Resource.Error(res.errorBody()?.string() ?: "Erreur inconnue")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur réseau")
        }
    }

    suspend fun getByUser(userId: Int, page: Int): Resource<PublicationResponse> {
        return try {
            val res = api.getPublicationsByUser(userId, page)
            if (res.isSuccessful && res.body() != null) {
                Resource.Success(res.body()!!)
            } else {
                Resource.Error(res.errorBody()?.string() ?: "Erreur inconnue")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur réseau")
        }
    }

    suspend fun createPost(photo_url: String?, contenu: String): Resource<CreatePublicationResponse>{
        return try{
            val response = api.createPublication(CreatePublicationRequest(photo_url, contenu))

            if(response.isSuccessful && response.body() != null){
                Resource.Success(response.body()!!)
            }else{
                Resource.Error(response.errorBody()?.string() ?: "Erreur inconnue")
            }
        }catch (e: Exception){
            Resource.Error(e.message ?: "Erreur réseau")
        }
    }

    suspend fun uploadPhoto(photoPart: MultipartBody.Part): Resource<PhotoUploadResponse> {
        return try {
            val response = api.uploadPhoto(photoPart)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Erreur upload photo")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur réseau")
        }
    }
}
