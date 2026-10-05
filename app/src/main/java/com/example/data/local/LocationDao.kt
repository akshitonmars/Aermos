package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM saved_locations ORDER BY isCurrentLocation DESC, orderIndex ASC, id ASC")
    fun getAllLocations(): Flow<List<SavedLocationEntity>>

    @Query("SELECT * FROM saved_locations WHERE id = :id LIMIT 1")
    suspend fun getLocationById(id: Long): SavedLocationEntity?

    @Query("SELECT * FROM saved_locations WHERE latitude BETWEEN :lat - 0.05 AND :lat + 0.05 AND longitude BETWEEN :lon - 0.05 AND :lon + 0.05 LIMIT 1")
    suspend fun findNear(lat: Double, lon: Double): SavedLocationEntity?

    @Query("SELECT * FROM saved_locations WHERE isCurrentLocation = 1 LIMIT 1")
    suspend fun getCurrentLocation(): SavedLocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: SavedLocationEntity): Long

    @Update
    suspend fun updateLocation(location: SavedLocationEntity)

    @Query("DELETE FROM saved_locations WHERE id = :id")
    suspend fun deleteLocationById(id: Long)

    @Query("UPDATE saved_locations SET cachedTempC = :temp, cachedConditionCode = :code, cachedWeatherSummary = :summary, cachedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateWeatherCache(id: Long, temp: Double, code: Int, summary: String, timestamp: Long)

    @Query("UPDATE saved_locations SET isCurrentLocation = 0")
    suspend fun clearCurrentLocationFlags()
}
