package com.amijul.location.data

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.amijul.location.domain.LocationData
import com.amijul.location.util.isOnline
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.IllegalStateException
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationRepository(private val ctx: Context): LocationData {

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(ctx) }
    private val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val clipBoard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun isOnline(): Boolean = ctx.isOnline()

    @SuppressLint("MissingPermission")
    override suspend fun getLocation(accuracy: Boolean): Location? {
        // Fast fail if device location is off
        if (!isLocationEnabled()) {
            throw IllegalStateException("LOCATION_DISABLED")
        }

        val priority = if (accuracy) Priority.PRIORITY_HIGH_ACCURACY
        else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        try {
            // Try current fix with a hard timeout (8s)
            val current: Location? = withTimeoutOrNull(8_000L) {
                suspendCancellableCoroutine { cont ->
                    val cts = CancellationTokenSource()
                    cont.invokeOnCancellation { cts.cancel() }

                    fused.getCurrentLocation(priority, cts.token)
                        .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc) }
                        .addOnFailureListener { e -> if (cont.isActive) cont.resumeWithException(e) }
                        .addOnCanceledListener { if (cont.isActive) cont.resume(null) }
                }
            }
            if (current != null) return current

            // Fallback to lastKnown with a SHORT timeout (1.5s) so we don’t hang
            val last: Location? = withTimeoutOrNull(1_500L) {
                suspendCancellableCoroutine { cont ->
                    fused.lastLocation
                        .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc) }
                        .addOnFailureListener { e -> if (cont.isActive) cont.resumeWithException(e) }
                        .addOnCanceledListener { if (cont.isActive) cont.resume(null) }
                }
            }
            if (last != null) return last

            // Nothing returned within our deadlines
            throw IllegalStateException("LOCATION_TIMEOUT")
        } catch (se: SecurityException) {
            // Called without permission
            throw IllegalStateException("NO_PERMISSION")
        }
    }


    override suspend fun getAddress(
        latitude: Double,
        longitude: Double,
        locale: Locale
    ): Address? {
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return null
        if (!Geocoder.isPresent()) return null

        return try {
            val geocoder = Geocoder(ctx, locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(latitude, longitude, 1) { list ->
                        if (cont.isActive) cont.resume(list.firstOrNull())
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                withContext(Dispatchers.IO) {
                    geocoder.getFromLocation(latitude, longitude, 1)
                }?.firstOrNull()
            }
        } catch (_: java.io.IOException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    override fun copiedAddress(addr: String) {
        if(addr.isEmpty()) return
        clipBoard.setPrimaryClip(ClipData.newPlainText("Address", addr))

    }

}