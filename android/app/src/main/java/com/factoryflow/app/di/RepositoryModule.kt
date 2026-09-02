package com.factoryflow.app.di

import com.factoryflow.app.core.data.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun auth(impl: DefaultAuthRepository): AuthRepository
    @Binds @Singleton abstract fun dashboard(impl: DefaultDashboardRepository): DashboardRepository
    @Binds @Singleton abstract fun ocr(impl: DefaultOcrRepository): OcrRepository
    @Binds @Singleton abstract fun reports(impl: DefaultReportsRepository): ReportsRepository
    @Binds @Singleton abstract fun generated(impl: DefaultGeneratedReportsRepository): GeneratedReportsRepository
    @Binds @Singleton abstract fun statistics(impl: DefaultStatisticsRepository): StatisticsRepository
    @Binds @Singleton abstract fun schedules(impl: DefaultSchedulesRepository): SchedulesRepository
    @Binds @Singleton abstract fun notifications(impl: DefaultNotificationsRepository): NotificationsRepository
    @Binds @Singleton abstract fun intelligence(impl: DefaultMaintenanceIntelligenceRepository): MaintenanceIntelligenceRepository
}
