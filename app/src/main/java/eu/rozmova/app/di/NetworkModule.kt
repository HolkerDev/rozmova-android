package eu.rozmova.app.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eu.rozmova.app.BuildConfig
import eu.rozmova.app.clients.backend.BugReportClient
import eu.rozmova.app.clients.backend.ChatClient
import eu.rozmova.app.clients.backend.MegaScenariosClient
import eu.rozmova.app.clients.backend.MessageClient
import eu.rozmova.app.clients.backend.ScenarioClient
import eu.rozmova.app.clients.backend.TranslationClient
import eu.rozmova.app.clients.backend.VerificationClient
import eu.rozmova.app.clients.backend.network.AuthInterceptor
import eu.rozmova.app.utils.instantDeserializer
import eu.rozmova.app.utils.instantSerializer
import io.github.jan.supabase.SupabaseClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
        }

    @Provides
    @Singleton
    fun provideAuthInterceptor(supabaseClient: SupabaseClient): AuthInterceptor = AuthInterceptor(supabaseClient)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("backend")
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    @Named("mega")
    fun provideMegaApiRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.API_MEGA_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideGson(): Gson =
        GsonBuilder()
            .registerTypeAdapter(
                Instant::class.java,
                instantSerializer,
            ).registerTypeAdapter(Instant::class.java, instantDeserializer)
            .create()

    @Provides
    @Singleton
    fun provideServiceClient(
        @Named("backend") retrofit: Retrofit,
    ): ScenarioClient = retrofit.create(ScenarioClient::class.java)

    @Provides
    @Singleton
    fun provideChatClient(
        @Named("backend") retrofit: Retrofit,
    ): ChatClient = retrofit.create(ChatClient::class.java)

    @Provides
    @Singleton
    fun provideMessageClient(
        @Named("backend")
        retrofit: Retrofit,
    ): MessageClient = retrofit.create(MessageClient::class.java)

    @Provides
    @Singleton
    fun provideBugReportClient(
        @Named("backend")
        retrofit: Retrofit,
    ): BugReportClient = retrofit.create(BugReportClient::class.java)

    @Provides
    @Singleton
    fun provideTranslationClient(
        @Named("mega")
        retrofit: Retrofit,
    ): TranslationClient = retrofit.create(TranslationClient::class.java)

    @Provides
    @Singleton
    fun provideVerificationClient(
        @Named("mega")
        retrofit: Retrofit,
    ): VerificationClient = retrofit.create(VerificationClient::class.java)

    @Provides
    @Singleton
    fun provideMegaScenariosClient(
        @Named("mega")
        retrofit: Retrofit,
    ): MegaScenariosClient = retrofit.create(MegaScenariosClient::class.java)
}
