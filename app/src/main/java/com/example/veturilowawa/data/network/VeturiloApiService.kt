package com.example.veturilowawa.data.network

import com.example.veturilowawa.data.model.StationInfoResponse
import com.example.veturilowawa.data.model.StationStatusResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface VeturiloApiService {

    @GET("station_information.json")
    suspend fun getStationInformation(): StationInfoResponse

    @GET("station_status.json")
    suspend fun getStationStatus(): StationStatusResponse

    companion object {
        // Oficjalny endpoint GBFS systemu Veturilo (Nextbike Warszawa)
        private const val BASE_URL = "https://gbfs.nextbike.net/maps/gbfs/v2/nextbike_vw/pl/"

        fun create(): VeturiloApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VeturiloApiService::class.java)
        }
    }
}
