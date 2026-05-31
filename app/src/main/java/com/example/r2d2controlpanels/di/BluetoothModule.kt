package com.example.r2d2controlpanels.di
import com.example.r2d2controlpanels.BluetoothController
import com.example.r2d2controlpanels.data.SerialService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BluetoothModule {

    @Provides
    @Singleton
    fun provideSerialService(): SerialService {

        return SerialService()
    }

    @Provides
    @Singleton
    fun provideBluetoothController(
        serialService: SerialService
    ): BluetoothController {

        return BluetoothController(
            serialService
        )
    }
}