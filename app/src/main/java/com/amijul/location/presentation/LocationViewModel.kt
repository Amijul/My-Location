package com.amijul.location.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amijul.location.data.LocationState
import com.amijul.location.domain.LocationData
import com.amijul.location.util.myLocation
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class LocationViewModel(private val repo: LocationData) : ViewModel() {

    private val _state = kotlinx.coroutines.flow.MutableStateFlow(LocationState())
    val state = _state.asStateFlow()

    fun getLocation(accuracy: Boolean) {
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val locData = repo.getLocation(accuracy) ?: error("LOCATION_UNAVAILABLE")

                val location = repo.getAddress(
                    latitude = locData.latitude,
                    longitude = locData.longitude,
                    locale = Locale.getDefault()
                )
                val address = location?.myLocation().orEmpty()

                _state.value = _state.value.copy(
                    isLoading = false,
                    latitude = locData.latitude,
                    longitude = locData.longitude,
                    location = location,
                    address = address,
                    error = null
                )
            } catch (t: Throwable) {
                val msg = when (t.message) {
                    "NO_PERMISSION"       -> "Location permission not granted."
                    "LOCATION_DISABLED"   -> "Location is turned off."
                    "LOCATION_TIMEOUT"    -> "Timed out while fetching location."
                    "LOCATION_UNAVAILABLE"-> "Location unavailable."
                    else                  -> t.message ?: "Failed to get location."
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = msg
                )
            }
        }
    }

}
