package com.taskflow.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.WorkManager
import com.taskflow.app.data.local.database.ALL_MIGRATIONS
import com.taskflow.app.data.local.database.AppDatabase
import com.taskflow.app.data.local.database.seed.CategorySeeder
import com.taskflow.app.data.local.preferences.DashboardPreferencesManager
import com.taskflow.app.data.local.preferences.DashboardPreferencesManagerImpl
import com.taskflow.app.data.local.preferences.ThemeManager
import com.taskflow.app.data.local.preferences.WeatherPreferencesManager
import com.taskflow.app.data.local.preferences.WeatherPreferencesManagerImpl
import com.taskflow.app.data.local.repository.CategoryRepositoryImpl
import com.taskflow.app.data.local.repository.TaskRepositoryImpl
import com.taskflow.app.data.remote.WeatherRepositoryImpl
import com.taskflow.app.domain.repository.CategoryRepository
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.repository.WeatherRepository
import com.taskflow.app.domain.scheduler.TaskNotificationScheduler
import com.taskflow.app.domain.usecase.AddCategoryUseCase
import com.taskflow.app.domain.usecase.AddTaskUseCase
import com.taskflow.app.domain.usecase.CheckWeatherAlertsUseCase
import com.taskflow.app.domain.usecase.CompleteTaskUseCase
import com.taskflow.app.domain.usecase.DeleteTaskUseCase
import com.taskflow.app.domain.usecase.GetCategoriesUseCase
import com.taskflow.app.domain.usecase.GetCurrentWeatherUseCase
import com.taskflow.app.domain.usecase.GetTasksUseCase
import com.taskflow.app.domain.usecase.GetWeatherLocationUseCase
import com.taskflow.app.domain.usecase.ReorderCategoriesUseCase
import com.taskflow.app.domain.usecase.ScheduleNotificationUseCase
import com.taskflow.app.domain.usecase.SetWeatherLocationUseCase
import com.taskflow.app.domain.usecase.UndoCompleteTaskUseCase
import com.taskflow.app.domain.usecase.UpdateTaskUseCase
import com.taskflow.app.domain.util.RecurrenceCalculator
import com.taskflow.app.notification.AlarmScheduler
import com.taskflow.app.notification.NotificationScheduler
import com.taskflow.app.notification.WeatherAlertScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.dsl.module

val appModule = module {
    single {
        val context = get<Context>()
        lateinit var instance: AppDatabase
        instance = Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(*ALL_MIGRATIONS)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        CategorySeeder(instance.categoryDao()).seedIfNeeded()
                    }
                }
            })
            .build()
        instance
    }
    single { get<AppDatabase>().taskDao() }
    single { get<AppDatabase>().taskExecutionDao() }
    single { get<AppDatabase>().categoryDao() }

    single { WorkManager.getInstance(get()) }
    single { AlarmScheduler(get()) }
    single<TaskNotificationScheduler> { NotificationScheduler(get(), get()) }
    single { WeatherAlertScheduler(get()) }
    single { RecurrenceCalculator() }

    single<TaskRepository> { TaskRepositoryImpl(get(), get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single { ThemeManager(get()) }
    single<DashboardPreferencesManager> { DashboardPreferencesManagerImpl(get()) }
    single<WeatherPreferencesManager> { WeatherPreferencesManagerImpl(get()) }
    single<WeatherRepository> { WeatherRepositoryImpl(get(), get(), get()) }

    factory { GetTasksUseCase(get()) }
    factory { ScheduleNotificationUseCase(get()) }
    factory { AddTaskUseCase(get(), get(), get()) }
    factory { UpdateTaskUseCase(get(), get()) }
    factory { CompleteTaskUseCase(get(), get(), get()) }
    factory { UndoCompleteTaskUseCase(get(), get()) }
    factory { DeleteTaskUseCase(get(), get()) }
    factory { GetCategoriesUseCase(get()) }
    factory { AddCategoryUseCase(get()) }
    factory { ReorderCategoriesUseCase(get()) }
    factory { GetCurrentWeatherUseCase(get()) }
    factory { CheckWeatherAlertsUseCase(get()) }
    factory { GetWeatherLocationUseCase(get()) }
    factory { SetWeatherLocationUseCase(get()) }
}
