package data.remote

import data.model.LoginRequest
import data.model.LoginResponse
import data.model.RegisterRequest
import data.model.RegisterResponse
import data.model.UserWithProgressDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


//Rotas no backend
interface AuthApiService {

    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(
        @Body body: RegisterRequest
    ): Response<RegisterResponse>

    @GET("auth/user/{email}")
    suspend fun getUserByEmail(
        @Path("email") email: String
    ): Response<UserWithProgressDto>
}
