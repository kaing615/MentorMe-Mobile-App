package com.mentorme.app.core.di

import com.mentorme.app.core.network.ApiKeyInterceptor
import com.mentorme.app.core.network.AuthInterceptor
import com.mentorme.app.core.network.NetworkConstants
import com.mentorme.app.data.remote.MentorMeApi
import com.mentorme.app.data.network.api.auth.AuthApiService
import com.mentorme.app.data.network.api.profile.ProfileApiService
import com.mentorme.app.data.network.api.home.HomeApiService
import com.mentorme.app.data.network.api.review.ReviewApiService
import com.mentorme.app.data.network.api.chat.ChatApiService
import com.mentorme.app.data.network.api.session.SessionApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        apiKeyInterceptor: ApiKeyInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .connectTimeout(NetworkConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(NetworkConstants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val base = NetworkConstants.BASE_URL
        val normalized = if (base.endsWith("/")) base else "$base/"
        val finalBase = if (normalized.endsWith("api/v1/")) normalized else normalized + "api/v1/"
        return Retrofit.Builder()
            /** MentorMe API is mounted at /api/v1, so baseUrl includes /api/v1/ while interface paths stay relative (e.g., 'bookings'). */
            .baseUrl(finalBase)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMentorMeApi(retrofit: Retrofit): MentorMeApi {
        return retrofit.create(MentorMeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideProfileApiService(retrofit: Retrofit): ProfileApiService {
        return retrofit.create(ProfileApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideHomeApiService(retrofit: Retrofit): HomeApiService {
        return retrofit.create(HomeApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideReviewApiService(retrofit: Retrofit): ReviewApiService {
        return retrofit.create(ReviewApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService {
        return retrofit.create(ChatApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSessionApiService(retrofit: Retrofit): SessionApiService {
        return retrofit.create(SessionApiService::class.java)
    }
}
