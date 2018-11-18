package su.katso.synonym.common.inject

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import okhttp3.logging.HttpLoggingInterceptor.Level.NONE
import org.koin.dsl.module.Module
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import su.katso.synonym.BuildConfig
import su.katso.synonym.common.network.ApiService
import su.katso.synonym.common.network.ErrorInterceptor

val appModule: Module = module {
    single { get<Context>().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE); }
    single { GsonBuilder().serializeNulls().create() }

    scope(SESSION_SCOPE) {
        OkHttpClient.Builder()
            .addInterceptor(ErrorInterceptor())
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) BASIC else NONE
            })
            .build()
    }

    scope(SESSION_SCOPE) {
        HttpUrl.Builder()
            .scheme(get<SharedPreferences>().getString(PREF_SCHEME, "") ?: "")
            .host(get<SharedPreferences>().getString(PREF_BASE_URL, "") ?: "")
            .addPathSegments("webapi/")
            .port(5000)
            .build()
    }

    scope(SESSION_SCOPE) {
        Retrofit.Builder()
            .baseUrl(get<HttpUrl>())
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

const val SESSION_SCOPE = "session_scope"
const val PREFERENCES = "preferences"
const val PREF_BASE_URL = "pref_base_url"
const val PREF_SCHEME = "pref_scheme"