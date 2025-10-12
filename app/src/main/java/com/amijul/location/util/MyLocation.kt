package com.amijul.location.util

import android.location.Address

fun Address.myLocation(): String {
    // 1) Prefer the formatted address lines if present
    if (maxAddressLineIndex >= 0) {
        val lines = (0..maxAddressLineIndex)
            .mapNotNull { getAddressLine(it)?.trim() }
            .filter { it.isNotBlank() }
        if (lines.isNotEmpty()) {
            return lines.joinToString(", ")
        }
    }

    // 2) Fallback: build from components (skip null/blank, keep order, de-dup adjacent)
    val parts = listOfNotNull(
        subThoroughfare?.takeIf { it.isNotBlank() },
        thoroughfare?.takeIf { it.isNotBlank() },
        premises?.takeIf { it.isNotBlank() },
        subLocality?.takeIf { it.isNotBlank() },
        locality?.takeIf { it.isNotBlank() },
        subAdminArea?.takeIf { it.isNotBlank() },
        adminArea?.takeIf { it.isNotBlank() },
        postalCode?.takeIf { it.isNotBlank() },
        countryName?.takeIf { it.isNotBlank() }
    )

    if (parts.isNotEmpty()) {
        val dedup = mutableListOf<String>()
        parts.forEach { if (dedup.lastOrNull() != it) dedup.add(it) }
        return dedup.joinToString(", ")
    }

    // 3) Ultimate fallback
    return ""
}
