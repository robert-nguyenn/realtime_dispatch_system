package com.dispatch.driver.data.model

import com.google.gson.annotations.SerializedName

data class Driver(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("licensePlate") val licensePlate: String?,
    @SerializedName("currentLat") val currentLat: Double?,
    @SerializedName("currentLng") val currentLng: Double?,
    @SerializedName("status") val status: DriverStatus,
    @SerializedName("lastLocationUpdate") val lastLocationUpdate: String?,
    @SerializedName("createdAt") val createdAt: String
)

enum class DriverStatus {
    @SerializedName("OFFLINE") OFFLINE,
    @SerializedName("AVAILABLE") AVAILABLE,
    @SerializedName("BUSY") BUSY,
    @SerializedName("EN_ROUTE") EN_ROUTE
}

data class LocationUpdate(
    @SerializedName("driverId") val driverId: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("heading") val heading: Int? = null,
    @SerializedName("speedKmh") val speedKmh: Double? = null,
    @SerializedName("accuracyMeters") val accuracyMeters: Double? = null
)

data class Ride(
    @SerializedName("id") val id: String,
    @SerializedName("riderId") val riderId: String,
    @SerializedName("driverId") val driverId: String?,
    @SerializedName("pickupLat") val pickupLat: Double,
    @SerializedName("pickupLng") val pickupLng: Double,
    @SerializedName("destinationLat") val destinationLat: Double?,
    @SerializedName("destinationLng") val destinationLng: Double?,
    @SerializedName("status") val status: RideStatus,
    @SerializedName("fareAmount") val fareAmount: Double?,
    @SerializedName("estimatedDurationMinutes") val estimatedDurationMinutes: Int?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("acceptedAt") val acceptedAt: String?,
    @SerializedName("startedAt") val startedAt: String?,
    @SerializedName("completedAt") val completedAt: String?,
    @SerializedName("cancelledAt") val cancelledAt: String?
)

enum class RideStatus {
    @SerializedName("REQUESTED") REQUESTED,
    @SerializedName("ACCEPTED") ACCEPTED,
    @SerializedName("IN_PROGRESS") IN_PROGRESS,
    @SerializedName("COMPLETED") COMPLETED,
    @SerializedName("CANCELLED") CANCELLED
}
