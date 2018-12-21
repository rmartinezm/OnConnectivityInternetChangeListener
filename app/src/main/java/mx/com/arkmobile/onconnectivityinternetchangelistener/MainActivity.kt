package mx.com.arkmobile.onconnectivityinternetchangelistener

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import mx.com.arkmobile.library.onconnectivityinternetchangelistener.ConnectionType
import mx.com.arkmobile.library.onconnectivityinternetchangelistener.OnConnectivityInternetChangeListener

class MainActivity : AppCompatActivity() {

    private lateinit var onConnectivityInternetChangeListener: OnConnectivityInternetChangeListener
    private var hashCodeSubscription: Int? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        onConnectivityInternetChangeListener = OnConnectivityInternetChangeListener.getInstance()

        tvConnectionCellular.text = "Celular: ${onConnectivityInternetChangeListener.isCurrentConnected(ConnectionType.CELLULAR)}"
        tvConnectionWifi.text = "Wifi: ${onConnectivityInternetChangeListener.isCurrentConnected(ConnectionType.WIFI)}"
        tvConnectionAny.text = "Cualquiera: ${onConnectivityInternetChangeListener.isCurrentConnected(ConnectionType.ANY)}"

        hashCodeSubscription = onConnectivityInternetChangeListener.subscribeTo(ConnectionType.CELLULAR) {
            runOnUiThread { tvConnectionCellular.text = "Celular: $it" }
        }
        hashCodeSubscription = onConnectivityInternetChangeListener.subscribeTo(ConnectionType.WIFI) {
            runOnUiThread { tvConnectionWifi.text = "Wifi: $it" }
        }
        hashCodeSubscription = onConnectivityInternetChangeListener.subscribeTo(ConnectionType.ANY) {
            runOnUiThread { tvConnectionAny.text = "Cualquiera: $it" }
        }
    }

}
