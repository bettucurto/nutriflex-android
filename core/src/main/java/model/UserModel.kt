package model

data class UserDto(
    val id: Int,
    val email: String,
    val nome: String,
    val altura: Int,
    val genero: String,
    val data_nascenca: String    // formato "YYYY-MM-DD"
)

data class UpdateUserRequest(
    val currentPassword: String,          // obrigatório para update
    val nome: String? = null,
    val altura: Int? = null,
    val genero: String? = null,
    val data_nascenca: String? = null,
    val password: String? = null          // nova password (opcional)
)

data class CreateProgressRequest(
    val id_user: Int,
    val peso_atual: Float,
    val peso_meta: Float?,
    val calorias_diarias: Int?,
)