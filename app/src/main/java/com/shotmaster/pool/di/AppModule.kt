package com.shotmaster.pool.di

import android.content.Context
import com.shotmaster.pool.billing.BillingManager
import com.shotmaster.pool.vision.BallDetector
import com.shotmaster.pool.vision.GuidelineDetector
import com.shotmaster.pool.vision.ShotCalculator
import com.shotmaster.pool.vision.TableDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideTableDetector(): TableDetector = TableDetector()

    @Singleton
    @Provides
    fun provideGuidelineDetector(): GuidelineDetector = GuidelineDetector()

    @Singleton
    @Provides
    fun provideBallDetector(): BallDetector = BallDetector()

    @Singleton
    @Provides
    fun provideShotCalculator(): ShotCalculator = ShotCalculator()

    @Singleton
    @Provides
    fun provideBillingManager(@ApplicationContext context: Context): BillingManager =
        BillingManager(context)

    @Singleton
    @Provides
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager =
        PreferencesManager(context)
}
