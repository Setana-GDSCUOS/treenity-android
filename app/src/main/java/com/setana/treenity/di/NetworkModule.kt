package com.setana.treenity.di

import com.setana.treenity.BuildConfig
import com.setana.treenity.data.api.ImageApiService
import com.setana.treenity.data.api.TreeApiHelper
import com.setana.treenity.data.api.TreeApiHelperImpl
import com.setana.treenity.data.api.TreeApiService
import com.setana.treenity.data.repository.TreeRepository
import com.setana.treenity.data.repository.TreeRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofitInstance(): ImageApiService =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL_TEST)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImageApiService::class.java)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val headerInterceptor = Interceptor {
                val request = it.request()
                    .newBuilder()
                    .build()
                return@Interceptor it.proceed(request)
            }

            OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()
        } else OkHttpClient
            .Builder()
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://gsc.oasisfores.com/")
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideTreeApiService(retrofit: Retrofit): TreeApiService =
        retrofit.create(TreeApiService::class.java)

    @Provides
    @Singleton
    fun provideApiHelper(treeApiHelper: TreeApiHelperImpl): TreeApiHelper =
        treeApiHelper

    @Provides
    @Singleton
    fun provideTreeRepository(treeRepository: TreeRepositoryImpl): TreeRepository =
        treeRepository

}