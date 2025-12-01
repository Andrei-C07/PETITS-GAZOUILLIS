package cgodin.qc.ca.petitgazouillis.data.models

data class PublicationResponse(
    val page: Int,
    val limit: Int,
    val total: Int,
    val total_pages: Int,
    val data: List<Publication>
)

data class Publication(
    val id: Int,
    val content: String,
    val created_at: String,
    val auteur: String,
    val user_id: Int,
    val photo_url: String?,
    val post_photo_url: String?
)
