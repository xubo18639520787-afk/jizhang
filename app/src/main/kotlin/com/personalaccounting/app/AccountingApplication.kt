package com.personalaccounting.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AccountingApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}