package com.amijul.location.data

import android.content.Context
import android.location.Address
import android.location.Location
import com.amijul.location.domain.LocationData
import java.util.Locale

class LocationRepository(private val ctx: Context): LocationData {
    override suspend fun getLocation(accuracy: Boolean): Location {
        TODO("Not yet implemented")
    }

    override suspend fun getAddress(
        latitude: Double,
        longitude: Double,
        locale: Locale
    ): Address? {
        TODO("Not yet implemented")
    }
}