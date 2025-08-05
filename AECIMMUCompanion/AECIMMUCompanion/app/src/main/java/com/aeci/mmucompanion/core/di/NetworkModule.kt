package com.aeci.mmucompanion.core.di

import android.content.Context
import com.aeci.mmucompanion.data.remote.api.AECIApiService
import com.aeci.mmucompanion.data.remote.api.MobileServerApiService
import com.aeci.mmucompanion.data.remote.api.ApiService
import com.aeci.mmucompanion.data.remote.ApiConfig
import com.aeci.mmucompanion.core.util.NetworkManager
import com.aeci.mmucompanion.core.util.MobileServerConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    // Legacy AECI server URL (keep for backward compatibility)
    private const val AECI_SERVER_URL = "https://your-aeci-server.com/api/"
    
    // Phone server configuration - dynamically configured
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    @Named("phone_server_client")
    fun providePhoneServerOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
            
            // Add JWT token if available
            // TODO: Get token from secure storage
            
            chain.proceed(requestBuilder.build())
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    @Named("phone_server_retrofit")
    fun providePhoneServerRetrofit(
        @Named("phone_server_client") okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.baseUrl + "/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    @Named("aeci_retrofit")
    fun provideAECIRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AECI_SERVER_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideApiService(@Named("phone_server_retrofit") retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideAECIApiService(@Named("aeci_retrofit") retrofit: Retrofit): AECIApiService {
        return retrofit.create(AECIApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideMobileServerApiService(@Named("phone_server_retrofit") retrofit: Retrofit): MobileServerApiService {
        return retrofit.create(MobileServerApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideNetworkManager(@ApplicationContext context: Context): NetworkManager {
        return NetworkManager(context)
    }
}