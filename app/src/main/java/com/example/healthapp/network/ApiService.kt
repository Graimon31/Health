package com.example.healthapp.network

import com.example.healthapp.network.model.HistoryResponse
import com.example.healthapp.network.model.MeasurementRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * REST API used for synchronisation with the backend service.
 */
interface ApiService {

    @POST("api/v1/measurements")
    suspend fun postMeasurement(@Body request: MeasurementRequest): Response<Unit>

    @GET("api/v1/history/{userId}")
    suspend fun getHistory(
        @Path("userId") userId: String,
        @Query("days") days: Int
    ): HistoryResponse
}
