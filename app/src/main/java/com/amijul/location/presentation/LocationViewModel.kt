package com.amijul.location.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amijul.location.data.LocationState
import com.amijul.location.domain.LocationData
import com.amijul.location.util.myLocation
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

class LocationViewModel(private val repo: LocationData) : ViewModel() {

    private val _state = kotlinx.coroutines.flow.MutableStateFlow(LocationState())
    val state = _state.asStateFlow()

    fun getLocation(accuracy: Boolean) {
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val locData = repo.getLocation(accuracy) ?: error("LOCATION_UNAVAILABLE")

                // Publish coords immediately
                _state.value = _state.value.copy(
                    isLoading = false,
                    latitude = locData.latitude,
                    longitude = locData.longitude,
                    error = null
                )

                // If offline, don’t even try geocoding; show a clear hint
                if (!repo.isOnline()) {
                    _state.value = _state.value.copy(
                        location = null,               // Address?
                        address  = "",                 // address text
                        error    = "No internet. Showing coordinates only."
                    )
                    return@launch
                }

                // Best-effort reverse geocode with its own deadline
                val addrObj = withTimeoutOrNull(3_000L) {
                    repo.getAddress(
                        latitude = locData.latitude,
                        longitude = locData.longitude,
                        locale = Locale.getDefault()
                    )
                }
                val addrText = addrObj?.myLocation().orEmpty()

                _state.value = _state.value.copy(
                    location = addrObj,   // Address?
                    address  = addrText   // String
                )

            } catch (t: Throwable) {
                val msg = when (t.message) {
                    "NO_PERMISSION"         -> "Location permission not granted."
                    "LOCATION_DISABLED"     -> "Location is turned off."
                    "LOCATION_TIMEOUT"      -> "Timed out while fetching location."
                    "LOCATION_UNAVAILABLE"  -> "Location unavailable."
                    else                    -> t.message ?: "Failed to get location."
                }
                _state.value = _state.value.copy(isLoading = false, error = msg)
            }
        }
    }
    fun copiedAddress(addr: String) {
        viewModelScope.launch {
            repo.copiedAddress(addr = addr)
        }
    }


}
