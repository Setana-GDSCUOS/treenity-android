package com.setana.treenity.di

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.setana.treenity.BuildConfig
import com.setana.treenity.data.api.*
import com.setana.treenity.data.repository.TreeRepository
import com.setana.treenity.data.repository.TreeRepositoryImpl
import com.setana.treenity.data.repository.UserRepository
import com.setana.treenity.data.repository.UserRepositoryImpl
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
    fun provideInterceptor(): Interceptor = Interceptor {
        // TODO 항상 jwt 토큰이 필요한 것은 아님
        val request = it.request()
        val result = runCatching {
            val user =
                FirebaseAuth.getInstance().currentUser ?: return@Interceptor it.proceed(request)
            val task: Task<GetTokenResult> = user.getIdToken(false)
            // Timeout 10s
            val tokenResult: GetTokenResult =
                Tasks.await(task, 10, java.util.concurrent.TimeUnit.SECONDS)
            tokenResult.token ?: return@Interceptor it.proceed(request)
        }

        return@Interceptor if (result.isSuccess) {
            val token = result.getOrNull()
            Log.d("INTERCEPTOR", token.toString())
            it.proceed(
                request.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            )
        } else {
            // No has auth header
            it.proceed(request)
        }
    }

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
    fun provideOkHttpClient(headerInterceptor: Interceptor): OkHttpClient =
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()
        } else {
            OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .build()
        }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.SETANA_BACKEND_BASE_URL)
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideTreeApiService(retrofit: Retrofit): TreeApiService =
        retrofit.create(TreeApiService::class.java)

    @Provides
    @Singleton
    fun provideTreeApiHelper(treeApiHelper: TreeApiHelperImpl): TreeApiHelper =
        treeApiHelper

    @Provides
    @Singleton
    fun provideTreeRepository(treeRepository: TreeRepositoryImpl): TreeRepository =
        treeRepository

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideUserApiHelper(userApiHelper: UserApiHelperImpl): UserApiHelper =
        userApiHelper

    @Provides
    @Singleton
    fun provideUserRepository(userRepository: UserRepositoryImpl): UserRepository =
        userRepository
}