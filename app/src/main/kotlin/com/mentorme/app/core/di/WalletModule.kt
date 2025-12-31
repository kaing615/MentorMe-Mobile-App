package com.mentorme.app.core.di

import com.mentorme.app.data.repository.wallet.WalletRepositoryInterface
import com.mentorme.app.data.repository.impl.WalletRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WalletModule {

    @Binds
    @Singleton
    abstract fun bindWalletRepositoryInterface(
        impl: WalletRepositoryImpl
    ): WalletRepositoryInterface
}
