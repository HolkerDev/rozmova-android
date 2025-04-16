package eu.rozmova.app.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eu.rozmova.app.BuildConfig
import eu.rozmova.app.clients.ChatClient
import eu.rozmova.app.clients.MessageClient
import eu.rozmova.app.clients.ScenarioClient
import eu.rozmova.app.clients.network.AuthInterceptor
import eu.rozmova.app.utils.instantDeserializer
import eu.rozmova.app.utils.instantSerializer
import io.github.jan.supabase.SupabaseClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.util.concurrent.TimeUnit
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
    fun provideGson(): Gson =
        GsonBuilder()
            .registerTypeAdapter(
                Instant::class.java,
                instantSerializer,
            ).registerTypeAdapter(Instant::class.java, instantDeserializer)
            .create()

    @Provides
    @Singleton
    fun provideServiceClient(retrofit: Retrofit): ScenarioClient = retrofit.create(ScenarioClient::class.java)

    @Provides
    @Singleton
    fun provideChatClient(retrofit: Retrofit): ChatClient = retrofit.create(ChatClient::class.java)

    @Provides
    @Singleton
    fun provideMessageClient(retrofit: Retrofit): MessageClient = retrofit.create(MessageClient::class.java)
}
