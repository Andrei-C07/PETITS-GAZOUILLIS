package cgodin.qc.ca.petitgazouillis.data.repository

import cgodin.qc.ca.petitgazouillis.data.api.ApiService
import cgodin.qc.ca.petitgazouillis.data.models.MessageResponse
import cgodin.qc.ca.petitgazouillis.data.models.PhotoUploadResponse
import cgodin.qc.ca.petitgazouillis.data.models.UpdatePasswordRequest
import cgodin.qc.ca.petitgazouillis.data.models.UpdateProfileRequest
import cgodin.qc.ca.petitgazouillis.data.models.UserProfile
import cgodin.qc.ca.petitgazouillis.data.utils.Resource
import okhttp3.MultipartBody

class ProfileRepository(private val api: ApiService) {

    suspend fun fetchProfile(): Resource<UserProfile> {
        return try {
            val response = api.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Erreur profil")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur réseau")
        }
    }

    suspend fun updateName(newName: String): Resource<UserProfile> {
        return try {
            val response = api.updateProfile(UpdateProfileRequest(newName))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Erreur lors de la mise à jour")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur réseau")
        }
    }

    suspend fun updatePassword(oldPassword: String, newPassword: String): Resource<MessageResponse> {
        return try {
            val response = api.updatePassword(
                UpdatePasswordRequest(
                    ancien_mot_de_passe = oldPassword,
                    nouveau_mot_de_passe = newPassword
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Erreur mot de passe")
            }
        } catch (e: Exception) {
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
