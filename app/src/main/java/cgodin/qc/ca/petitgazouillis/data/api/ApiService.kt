package cgodin.qc.ca.petitgazouillis.data.api

import cgodin.qc.ca.petitgazouillis.data.models.LoginRequest
import cgodin.qc.ca.petitgazouillis.data.models.LoginResponse
import cgodin.qc.ca.petitgazouillis.data.models.LogoutResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService{

    @POST("/api/jeton/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/deconnexion")
    suspend fun logout(): Response<LogoutResponse>
}
