package com.taskflow.app

import android.app.Application
import com.taskflow.app.di.appModule
import com.taskflow.app.di.networkModule
import com.taskflow.app.di.presentationModule
import com.taskflow.app.notification.WeatherAlertScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin

class TaskFlowApplication : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TaskFlowApplication)
            workManagerFactory()
            modules(appModule, presentationModule, networkModule)
        }
        get<WeatherAlertScheduler>().scheduleDaily()
    }
}
