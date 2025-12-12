package data.repository

import data.model.LoginRequest
import data.model.LoginResponse
import data.remote.AuthApiService
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: AuthApiService
) {

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Credenciais inválidas ou erro no servidor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
