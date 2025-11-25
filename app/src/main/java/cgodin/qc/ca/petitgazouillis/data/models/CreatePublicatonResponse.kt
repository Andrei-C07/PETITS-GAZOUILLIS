package cgodin.qc.ca.petitgazouillis.data.models

data class CreatePublicationRequest(
    val content: String
)

data class CreatePublicationResponse(
    val message: String,
    val id: Int
)
