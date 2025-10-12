package com.amijul.location.ui

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amijul.location.presentation.LocationViewModel
import com.amijul.location.ui.component.LocationCard
import com.amijul.location.ui.component.LocationInfo
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLocationApp(
    modifier: Modifier = Modifier,
    vm: LocationViewModel = koinViewModel()
) {
    val ctx = LocalContext.current
    val state by vm.state.collectAsStateWithLifecycle()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        if (res.values.all { it }) {
            vm.getLocation(accuracy = true)
        } else {
            // Open App Settings on denial so user can grant later
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                "package:${ctx.packageName}".toUri()
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
        }
    }

    fun requestPermissions() {
        launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Location", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2AB3A6), Color(0xFF1C8CDE))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("My Location", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                    state.address.ifBlank { if(state.error != null) {state.error}  else "Turn on internet & fetch address" }
                        ?.let {
                            Text(
                                it,
                                color = if(state.error != null) MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                lineHeight = 18.sp
                            )
                        }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { requestPermissions() }) {
                            Text("Get Location")
                        }
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(22.dp)
                                    .align(Alignment.CenterVertically),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Lat / Lng cards
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val lat = state.latitude
                val lng = state.longitude

                LocationCard(
                    title = "Latitude",
                    value = lat?.let { "%.6f".format(it) } ?: "--",
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp)
                )
                LocationCard(
                    title = "Longitude",
                    value = lng?.let { "%.6f".format(it) } ?: "--",
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Address categories
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Address Details",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Build rows from Address safely
                val a = state.location
                val ngh = a?.getAddressLine(0)?.split(",")?.getOrNull(1)?.trim().orEmpty()
                LocationInfo("House No", listOfNotNull(a?.featureName, ngh).joinToString(", "))
                LocationInfo("Street", listOfNotNull(a?.subThoroughfare, a?.thoroughfare).joinToString(" ").trim())
                LocationInfo("Area", a?.subLocality.orEmpty())
                LocationInfo("City", a?.locality.orEmpty())
                LocationInfo("State", a?.adminArea.orEmpty())
                LocationInfo("Postal Code", a?.postalCode.orEmpty())
                LocationInfo("Country", a?.countryName.orEmpty())


            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
