package com.uva.metaquotestest

import android.app.Application

class App : Application() {

    companion object {
        lateinit var instance: App
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
    }
}
