package data.remote

import data.model.LoginRequest
import data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


//Rotas no backend
interface AuthApiService {

    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<LoginResponse>
}
