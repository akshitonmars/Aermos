package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val region: String = "",
    val country: String = "",
    val latitude: Double,
    val longitude: Double,
    val isCurrentLocation: Boolean = false,
    val isFavorite: Boolean = true,
    val orderIndex: Int = 0,
    val cachedTempC: Double? = null,
    val cachedConditionCode: Int? = null,
    val cachedWeatherSummary: String? = null,
    val cachedTimestamp: Long = System.currentTimeMillis()
)
