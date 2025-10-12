package com.amijul.location.di

import android.app.Application
import org.koin.core.context.startKoin
import org.koin.android.ext.koin.androidContext


class Amijul: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@Amijul)
            modules(myModule)
        }
    }
}