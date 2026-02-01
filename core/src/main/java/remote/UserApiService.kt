package remote

import model.CreateProgressRequest
import model.UpdateUserRequest
import model.UserDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApiService {

    //Progresso
    @POST("user/{id_user}/progress")
    suspend fun createProgress(
        @Path("id_user") idUser: Int,
        @Body body: CreateProgressRequest
    ): Response<ResponseBody>

    //User
    @GET("user/{id}")
    suspend fun getUserById(
        @Path("id") id: Int
    ): Response<UserDto>

    @PUT("user/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body body: UpdateUserRequest
    ): Response<ResponseBody>

    // apagar conta
    @DELETE("user/{id}")
    suspend fun deleteUser(
        @Path("id") id: Int
    ): Response<ResponseBody>

    // apagar TODOS os pesos desse utilizador
    @DELETE("user/{id_user}/progress")
    suspend fun deleteAllProgress(
        @Path("id_user") idUser: Int
    ): Response<ResponseBody>

    // apagar um registo específico (já tinhas no backend)
    @DELETE("user/progress/{id}")
    suspend fun deleteProgressById(
        @Path("id") progressId: Int
    ): Response<ResponseBody>
}
