package cgodin.qc.ca.petitgazouillis.data.models

data class User(
    val nom_utilisateur : String,
    val mot_de_passe : String,
)

data class Post(
    val id: Int,
    val texte: String,
    val utilisateur: User,
    val timestamp: String
)