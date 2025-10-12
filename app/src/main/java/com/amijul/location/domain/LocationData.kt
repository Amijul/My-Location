package com.amijul.location.domain

import android.location.Address
import android.location.Location
import java.util.Locale

interface LocationData {

    fun isOnline(): Boolean
    suspend fun getLocation(accuracy: Boolean): Location?
    suspend fun getAddress(latitude: Double, longitude: Double, locale: Locale = Locale.getDefault()): Address?

}