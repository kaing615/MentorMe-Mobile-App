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

    @Binds @Singleton
    abstract fun bindMentorRepository(impl: MentorRepositoryImpl): MentorRepository

    @Binds @Singleton
    abstract fun bindBookingRepository(impl: BookingRepositoryImpl): BookingRepository

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
