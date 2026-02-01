package data.model

data class RegisterRequest(
    val nome: String,
    val email: String,
    val password: String,
    val altura: Int,
    val data_nascenca: String,      // "YYYY-MM-DD"
    val genero: String,             // "M" ou "F"
    val peso_atual: Float,
    val peso_inicial: Float,
    val peso_meta: Float,
    val calorias_diarias: Int,
    val dificuldades_anteriores: Int?,  // 0..3 ou null
    val objetivo: Int?,                  // 0..2 ou null
    val nivel_atividade: Int?
)