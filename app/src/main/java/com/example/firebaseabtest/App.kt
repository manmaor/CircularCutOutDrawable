package com.example.firebaseabtest

import android.app.Application

import android.content.Context

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        context = this

        try {
            MyService.startService(MyService.Companion.ServiceStateType.BACKGROUND)

        } catch (e: android.app.BackgroundServiceStartNotAllowedException) {
            e.printStackTrace()
        }
    }

    companion object {
        lateinit var context: Context
    }
}