package com.dispatch.driver.data.api

import com.dispatch.driver.data.model.Driver
import com.dispatch.driver.data.model.DriverStatus
import com.dispatch.driver.data.model.LocationUpdate
import com.dispatch.driver.data.model.Ride
import retrofit2.Response
import retrofit2.http.*

interface DispatchApiService {
    
    @GET("drivers/{driverId}")
    suspend fun getDriver(@Path("driverId") driverId: String): Response<Driver>
    
    @POST("drivers/{driverId}/location")
    suspend fun updateDriverLocation(
        @Path("driverId") driverId: String,
        @Body locationUpdate: LocationUpdate
    ): Response<Driver>
    
    @POST("drivers/{driverId}/status")
    suspend fun updateDriverStatus(
        @Path("driverId") driverId: String,
        @Query("status") status: DriverStatus
    ): Response<Driver>
    
    @POST("drivers/{driverId}/online")
    suspend fun goOnline(
        @Path("driverId") driverId: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): Response<Driver>
    
    @POST("drivers/{driverId}/offline")
    suspend fun goOffline(@Path("driverId") driverId: String): Response<Driver>
    
    @GET("drivers/{driverId}")
    suspend fun getDriverRides(@Path("driverId") driverId: String): Response<List<Ride>>
    
    @POST("rides/{rideId}/start")
    suspend fun startRide(
        @Path("rideId") rideId: String,
        @Query("driverId") driverId: String
    ): Response<Ride>
    
    @POST("rides/{rideId}/complete")
    suspend fun completeRide(
        @Path("rideId") rideId: String,
        @Query("driverId") driverId: String,
        @Query("fareAmount") fareAmount: Double
    ): Response<Ride>
    
    @POST("rides/{rideId}/cancel")
    suspend fun cancelRide(
        @Path("rideId") rideId: String,
        @Query("initiatedBy") initiatedBy: String
    ): Response<Ride>
}
