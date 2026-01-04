package data.model

data class UserWithProgressDto(
    val id: Int,
    val nome: String,
    val email: String,
    val altura: Int,
    val genero: String,
    val data_nascenca: String,
    val progress: UserProgressDto?
)

data class UserProgressDto(
    val data: String,
    val peso_atual: Float,
    val peso_meta: Float?,
    val calorias_diarias: Int?,
    val dificuldades_anteriores: Int?,
    val objetivo: String?
)
