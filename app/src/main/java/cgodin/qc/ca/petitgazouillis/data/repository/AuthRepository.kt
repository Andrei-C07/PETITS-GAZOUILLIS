package cgodin.qc.ca.petitgazouillis.data.repository

import cgodin.qc.ca.petitgazouillis.data.api.ApiService
import cgodin.qc.ca.petitgazouillis.data.models.LoginRequest
import cgodin.qc.ca.petitgazouillis.data.models.LoginResponse
import cgodin.qc.ca.petitgazouillis.data.utils.Resource

class AuthRepository(private val api: ApiService) {

    suspend fun login(request: LoginRequest): Resource<LoginResponse> {
        return try {
            val response = api.login(request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Erreur inconnue")
            }

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Problème réseau")
        }
    }

    suspend fun logout(): Resource<Unit> {
        return try {
            val response = api.logout()

            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Erreur lors de la déconnexion")
            }

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Problème réseau")
        }
    }
}
