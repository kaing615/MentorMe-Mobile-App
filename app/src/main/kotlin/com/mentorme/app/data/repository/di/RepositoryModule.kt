package com.mentorme.app.data.repository.di

import com.mentorme.app.data.repository.AuthRepository
import com.mentorme.app.data.repository.MentorRepository
import com.mentorme.app.data.repository.BookingRepository
import com.mentorme.app.data.repository.ProfileRepository
import com.mentorme.app.data.repository.impl.AuthRepositoryImpl
import com.mentorme.app.data.repository.impl.MentorRepositoryImpl
import com.mentorme.app.data.repository.impl.BookingRepositoryImpl
import com.mentorme.app.data.repository.impl.ProfileRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.mentorme.app.data.repository.availability.AvailabilityRepository
import com.mentorme.app.data.repository.availability.AvailabilityRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindMentorRepo(
        impl: MentorRepositoryImpl
    ): MentorRepository

    @Binds
    abstract fun bindBookingRepo(
        impl: BookingRepositoryImpl
    ): BookingRepository

    // Availability
    @Binds
    @Singleton
    abstract fun bindAvailabilityRepository(
        impl: AvailabilityRepositoryImpl
    ): AvailabilityRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository
}

// --- Use case providers ---
@Module
@InstallIn(SingletonComponent::class)
object UseCaseProvidersModule {
    @dagger.Provides
    @Singleton
    fun provideUpdateAvailabilitySlotUseCase(
        api: com.mentorme.app.data.remote.MentorMeApi
    ): com.mentorme.app.domain.usecase.availability.UpdateAvailabilitySlotUseCase {
        return com.mentorme.app.domain.usecase.availability.UpdateAvailabilitySlotUseCase(api)
    }

    @dagger.Provides
    @Singleton
    fun provideDeleteAvailabilitySlotUseCase(
        api: com.mentorme.app.data.remote.MentorMeApi
    ): com.mentorme.app.domain.usecase.availability.DeleteAvailabilitySlotUseCase {
        return com.mentorme.app.domain.usecase.availability.DeleteAvailabilitySlotUseCase(api)
    }
}
