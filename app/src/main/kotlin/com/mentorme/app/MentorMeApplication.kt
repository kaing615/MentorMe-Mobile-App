package com.mentorme.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MentorMeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
