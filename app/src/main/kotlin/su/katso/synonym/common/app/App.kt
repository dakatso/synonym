package su.katso.synonym.common.app

import android.app.Application
import org.koin.android.ext.android.startKoin
import su.katso.synonym.common.inject.appModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(appModule))
    }
}