package cgodin.qc.ca.petitgazouillis.data.models

data class UserProfile(
    val id: Int,
    val nom_utilisateur: String,
    val photo_url: String?,
    val followers_count: Int? = null,
    val following_count: Int? = null,
    val created_at: String? = null,
    val is_following: Boolean? = null
)

data class UpdateProfileRequest(
    val nom_utilisateur: String
)

data class UpdatePasswordRequest(
    val ancien_mot_de_passe: String,
    val nouveau_mot_de_passe: String
)

data class PhotoUploadResponse(
    val photo_url: String
)

data class MessageResponse(
    val message: String?
)
