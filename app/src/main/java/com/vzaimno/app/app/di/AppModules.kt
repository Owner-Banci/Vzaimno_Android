package com.vzaimno.app.app.di

import android.content.Context
import coil.ImageLoader
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.vzaimno.app.BuildConfig
import com.vzaimno.app.core.common.IoDispatcher
import com.vzaimno.app.core.common.ensureTrailingSlash
import com.vzaimno.app.core.common.toWebSocketBaseUrl
import com.vzaimno.app.core.config.AppConfig
import com.vzaimno.app.core.config.AppEnvironment
import com.vzaimno.app.core.network.ApiErrorMapper
import com.vzaimno.app.core.network.AuthTokenProvider
import com.vzaimno.app.core.network.BearerTokenInterceptor
import com.vzaimno.app.data.remote.AnnouncementApi
import com.vzaimno.app.data.remote.AuthApi
import com.vzaimno.app.data.remote.ChatApi
import com.vzaimno.app.data.remote.DeviceApi
import com.vzaimno.app.data.remote.ProfileApi
import com.vzaimno.app.data.remote.RouteApi
import com.vzaimno.app.data.repository.AnnouncementRepository
import com.vzaimno.app.data.repository.AuthRepository
import com.vzaimno.app.data.repository.ChatRepository
import com.vzaimno.app.data.repository.DefaultAnnouncementRepository
import com.vzaimno.app.data.repository.DefaultAuthRepository
import com.vzaimno.app.data.repository.DefaultChatRepository
import com.vzaimno.app.data.repository.DefaultDeviceRepository
import com.vzaimno.app.data.repository.DefaultProfileRepository
import com.vzaimno.app.data.repository.DefaultRouteRepository
import com.vzaimno.app.data.repository.DeviceRepository
import com.vzaimno.app.data.repository.ProfileRepository
import com.vzaimno.app.data.repository.RouteRepository
import com.vzaimno.app.data.repository.SessionManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppConfig(): AppConfig = AppConfig(
        environment = AppEnvironment.from(BuildConfig.APP_ENVIRONMENT),
        apiBaseUrl = BuildConfig.API_BASE_URL.ensureTrailingSlash(),
        webSocketBaseUrl = BuildConfig.WEBSOCKET_BASE_URL
            .ifBlank { BuildConfig.API_BASE_URL.toWebSocketBaseUrl() }
            .ensureTrailingSlash(),
        authEnabled = BuildConfig.AUTH_ENABLED,
    )

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideApiErrorMapper(json: Json): ApiErrorMapper = ApiErrorMapper(json)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        bearerTokenInterceptor: BearerTokenInterceptor,
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(180, TimeUnit.SECONDS)
            .addInterceptor(bearerTokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        appConfig: AppConfig,
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(appConfig.normalizedApiBaseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi = retrofit.create(ProfileApi::class.java)

    @Provides
    @Singleton
    fun provideDeviceApi(retrofit: Retrofit): DeviceApi = retrofit.create(DeviceApi::class.java)

    @Provides
    @Singleton
    fun provideAnnouncementApi(retrofit: Retrofit): AnnouncementApi =
        retrofit.create(AnnouncementApi::class.java)

    @Provides
    @Singleton
    fun provideChatApi(retrofit: Retrofit): ChatApi = retrofit.create(ChatApi::class.java)

    @Provides
    @Singleton
    fun provideRouteApi(retrofit: Retrofit): RouteApi = retrofit.create(RouteApi::class.java)

    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader = ImageLoader.Builder(context)
        .crossfade(true)
        .build()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthTokenProvider(sessionManager: SessionManager): AuthTokenProvider

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: DefaultAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: DefaultProfileRepository): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(impl: DefaultDeviceRepository): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindAnnouncementRepository(impl: DefaultAnnouncementRepository): AnnouncementRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: DefaultChatRepository): ChatRepository

    @Binds
    @Singleton
    abstract fun bindRouteRepository(impl: DefaultRouteRepository): RouteRepository
}
