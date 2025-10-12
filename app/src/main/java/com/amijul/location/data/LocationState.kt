package com.amijul.location.data

import android.location.Address

data class LocationState(
    val isLoading: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val location: Address? = null,
    val address: String = "" ,
    val error: String? = null
)
