package remote

import com.google.gson.Gson
import datastore.TokenManager
import model.CreateProgressRequest
import model.ErrorResponse
import model.UpdateUserRequest
import model.UserDto
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: UserApiService,
    private val tokenManager: TokenManager
) {

    //User
    suspend fun getUser(id: Int): Result<UserDto> {
        return try {
            val response = api.getUserById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response.errorBody()?.string(), "Error loading user")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(
        currentPassword: String,
        newPassword: String?,
        id: Int,
        nome: String?,
        altura: Float?,
        genero: String?,
        nivel_atividade: Int,
        dataNascenca: String?,
    ): Result<Unit> {
        return try {
            val body = UpdateUserRequest(
                currentPassword = currentPassword,        // vem do UI
                nome = nome,
                altura = altura?.toInt(),
                genero = genero,
                data_nascenca = dataNascenca,
                nivel_atividade = nivel_atividade,
                password = newPassword                   // nova password (já em texto normal)
            )
            val response = api.updateUser(id, body)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseError(response.errorBody()?.string(), "Error updating user")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(id: Int): Result<Unit> {
        return try {
            val response = api.deleteUser(id)
            if (response.isSuccessful) {
                // aqui podes também limpar o token
                tokenManager.clearToken()
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseError(response.errorBody()?.string(), "Error deleting user")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //Progress

    suspend fun createProgress(
        idUser: Int,
        pesoAtual: Float,
        pesoMeta: Float?,
        caloriasDiarias: Int?,
    ): Result<Unit> {
        return try {
            val body = CreateProgressRequest(
                id_user = idUser,
                peso_atual = pesoAtual,
                peso_meta = pesoMeta,
                calorias_diarias = caloriasDiarias,
            )

            val response = api.createProgress(idUser, body)

            // LOGS PARA DEBUG
            android.util.Log.d("UserRepository", "createProgress code=${response.code()}")
            android.util.Log.d("UserRepository", "createProgress body=${response.body()?.string()}")
            android.util.Log.d("UserRepository", "createProgress error=${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        parseError(
                            response.errorBody()?.string(),
                            "Erro ao criar progresso remoto (HTTP ${response.code()})"
                        )
                    )
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "createProgress exception", e)
            Result.failure(e)
        }
    }

    suspend fun deleteAllProgress(idUser: Int): Result<Unit> {
        return try {
            val response = api.deleteAllProgress(idUser)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseError(response.errorBody()?.string(), "Error deleting progress")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseError(raw: String?, defaultMessage: String): String {
        if (raw.isNullOrBlank()) return defaultMessage
        return try {
            val error = Gson().fromJson(raw, ErrorResponse::class.java)
            error.error
        } catch (e: Exception) {
            defaultMessage
        }
    }
}
