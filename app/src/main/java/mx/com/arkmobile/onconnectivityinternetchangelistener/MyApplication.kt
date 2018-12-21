package mx.com.arkmobile.onconnectivityinternetchangelistener

import android.app.Application
import mx.com.arkmobile.library.onconnectivityinternetchangelistener.OnConnectivityInternetChangeListener

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        OnConnectivityInternetChangeListener.initialize(applicationContext)
    }

}