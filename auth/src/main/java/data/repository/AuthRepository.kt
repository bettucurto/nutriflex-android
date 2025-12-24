package data.repository

import android.util.Log
import com.google.gson.Gson
import data.model.ErrorResponse
import data.model.LoginRequest
import data.model.LoginResponse
import data.model.RegisterRequest
import data.model.RegisterResponse
import data.model.UserWithProgressDto
import data.remote.AuthApiService
import datastore.TokenManager
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: AuthApiService,
    private val tokenManager: TokenManager
) {

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                // guarda o token do login
                tokenManager.saveToken(body.token)
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                val message = if (!errorBody.isNullOrBlank()) {
                    try {
                        val error = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        error.error
                    } catch (e: Exception) {
                        "Erro no login (${response.code()})"
                    }
                } else {
                    "Erro no login (${response.code()})"
                }
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return try {
            Log.d("Register", "request objetivo=${request.objetivo}, dificuldade=${request.dificuldades_anteriores}")
            val response = api.register(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                // guarda o token do registo
                tokenManager.saveToken(body.token)
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                val message = if (!errorBody.isNullOrBlank()) {
                    try {
                        val error = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        error.error
                    } catch (e: Exception) {
                        "Erro no registo (${response.code()})"
                    }
                } else {
                    "Erro no registo (${response.code()})"
                }
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserByEmail(email: String): Result<UserWithProgressDto> {
        return try {
            val response = api.getUserByEmail(email)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val message = if (!errorBody.isNullOrBlank()) {
                    try {
                        val error = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        error.error
                    } catch (e: Exception) {
                        "Erro ao obter utilizador (${response.code()})"
                    }
                } else {
                    "Erro ao obter utilizador (${response.code()})"
                }
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}