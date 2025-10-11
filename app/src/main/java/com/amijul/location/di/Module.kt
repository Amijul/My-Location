package com.amijul.location.di

import com.amijul.location.data.LocationRepository
import com.amijul.location.domain.LocationData
import com.amijul.location.presentation.LocationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val myModule = module {

    single<LocationData> { LocationRepository(get()) }
    viewModel { LocationViewModel(get()) }
}