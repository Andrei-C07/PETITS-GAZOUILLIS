package cgodin.qc.ca.petitgazouillis.data.api

import cgodin.qc.ca.petitgazouillis.data.models.CreatePublicationRequest
import cgodin.qc.ca.petitgazouillis.data.models.CreatePublicationResponse
import cgodin.qc.ca.petitgazouillis.data.models.LoginRequest
import cgodin.qc.ca.petitgazouillis.data.models.LoginResponse
import cgodin.qc.ca.petitgazouillis.data.models.LogoutResponse
import cgodin.qc.ca.petitgazouillis.data.models.MessageResponse
import cgodin.qc.ca.petitgazouillis.data.models.PhotoUploadResponse
import cgodin.qc.ca.petitgazouillis.data.models.PublicationResponse
import cgodin.qc.ca.petitgazouillis.data.models.UpdatePasswordRequest
import cgodin.qc.ca.petitgazouillis.data.models.UpdateProfileRequest
import cgodin.qc.ca.petitgazouillis.data.models.UserProfile
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService{

    @POST("/api/jeton/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/deconnexion")
    suspend fun logout(): Response<LogoutResponse>

    @GET("/api/publication/")
    suspend fun getPublications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 5
    ): Response<PublicationResponse>

    @GET("/api/publication/par_user/{user_id}")
    suspend fun getPublicationsByUser(
        @Path("user_id") userId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 5
    ): Response<PublicationResponse>

    @GET("/api/publication/suivis")
    suspend fun getFollowedPublications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 5
    ): Response<PublicationResponse>

    @POST("/api/publication/")
    suspend fun createPublication(
        @Body body: CreatePublicationRequest
    ): Response<CreatePublicationResponse>

    @GET("/api/utilisateur/profil")
    suspend fun getProfile(): Response<UserProfile>

    @PUT("/api/utilisateur/profil")
    suspend fun updateProfile(
        @Body body: UpdateProfileRequest
    ): Response<UserProfile>

    @PUT("/api/utilisateur/profil/mot_de_passe")
    suspend fun updatePassword(
        @Body body: UpdatePasswordRequest
    ): Response<MessageResponse>

    @Multipart
    @POST("/api/utilisateur/profil/photo")
    suspend fun uploadPhoto(
        @Part photo: MultipartBody.Part
    ): Response<PhotoUploadResponse>

    @GET("/api/utilisateur/{id}")
    suspend fun getUserById(
        @Path("id") id: Int
    ): Response<UserProfile>
}
