package com.example.pointoassignment

import android.app.Application
import timber.log.Timber

class MyApplication: Application() {

    companion object{
        lateinit var instance: MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Timber.plant(Timber.DebugTree())
    }
}