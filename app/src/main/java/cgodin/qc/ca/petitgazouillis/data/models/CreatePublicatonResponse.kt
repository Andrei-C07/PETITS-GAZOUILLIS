package cgodin.qc.ca.petitgazouillis.data.models

data class CreatePublicationRequest(
    val photo_url: String?,
    val content: String
)

data class CreatePublicationResponse(
    val photo_url: String?,
    val message: String,
    val id: Int
)
