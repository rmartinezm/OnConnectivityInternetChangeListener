package mx.com.arkmobile.library.onconnectivityinternetchangelistener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.net.NetworkCapabilities
import android.net.Network
import kotlin.properties.Delegates
import android.os.AsyncTask
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


/**
 *
 * @author Roberto Martinez Medina
 * @version 0.0.1
 */
class OnConnectivityInternetChangeListener private constructor() {

    /* */
    private lateinit var connectivityManager: ConnectivityManager
    private var cellularConnection by Delegates.observable<Boolean?>(null){ _, oldValue, newValue ->
        val value = oldValue ?: !newValue!!
        if(value != newValue)
            notifyChanges(ConnectionType.CELLULAR, newValue!!)
    }
    private var wifiConnection by Delegates.observable<Boolean?>(false){ _, oldValue, newValue ->
        val value = oldValue ?: !newValue!!
        if(value != newValue)
            notifyChanges(ConnectionType.WIFI, newValue!!)
    }
    private var anyConnection by Delegates.observable<Boolean?>(false){ _, oldValue, newValue ->
        val value = oldValue ?: !newValue!!
        if(value != newValue)
            notifyChanges(ConnectionType.ANY, newValue!!)
    }

    /* */
    private val subscribed: HashMap<Int, Pair<ConnectionType,(value: Boolean) -> Unit>> = HashMap()

    /**
     *
     */
    companion object {

        private const val CONNECTIVITY_CHANGE_INTENT_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"
        private val instance = OnConnectivityInternetChangeListener()

        /**
         *
         */
        @Throws(NotInitializedException::class)
        fun getInstance() : OnConnectivityInternetChangeListener =
            if(instance::connectivityManager.isInitialized)
                instance
            else
                throw NotInitializedException()
        /**
         *
         * @param applicationContext
         */
        fun initialize(applicationContext: Context){
            instance.connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            applicationContext.registerReceiver(NetworkChangeReceiver(), IntentFilter(CONNECTIVITY_CHANGE_INTENT_ACTION))
            instance.initialize()
        }
    }

    /**
     *
     */
    private fun initialize(){
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network?) { validateConnection() }
            override fun onCapabilitiesChanged(network: Network?, networkCapabilities: NetworkCapabilities?) { validateConnection() }
            override fun onLost(network: Network?) { validateConnection() }
        }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    /**
     *
     */
    private fun validateConnection(){
        val activeNetwork = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        CheckInternetTask {
            cellularConnection = it && activeNetwork?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
            wifiConnection = it && activeNetwork?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
            anyConnection = it && (cellularConnection ?: false) || (wifiConnection ?: false)
        }.execute()

    }

    /**
     *
     * @param connectionType
     * @return [Boolean]
     */
    fun isCurrentConnected(connectionType: ConnectionType = ConnectionType.ANY) : Boolean = when(connectionType){
        ConnectionType.WIFI -> (wifiConnection ?: false)
        ConnectionType.CELLULAR -> (cellularConnection ?: false)
        ConnectionType.ANY -> (wifiConnection ?: false) || (cellularConnection ?: false)
    }

    /**
     *
     * @param connectionType
     * @param onChange
     * @return [Int]
     */
    fun subscribeTo(connectionType: ConnectionType = ConnectionType.ANY, onChange: (value: Boolean) -> Unit) : Int {
        val pair = Pair(connectionType, onChange)
        val pairHashCode = pair.hashCode()
        subscribed[pairHashCode] = pair
        return pairHashCode
    }

    /**
     *
     * @param hashCode
     */
    fun unsubscribeTo(hashCode: Int) {
        subscribed.remove(hashCode)
    }

    /**
     *
     * @param connectionType
     * @param value
     */
    private fun notifyChanges(connectionType: ConnectionType = ConnectionType.ANY, value: Boolean) {
        subscribed.forEach { if(it.value.first == connectionType) it.value.second(value) }
    }

    /**
     *
     * @param callback
     */
    private class CheckInternetTask(
        val callback: (value: Boolean) -> Unit
    ) : AsyncTask<Void, Void, Boolean>() {

        /**
         *
         * @param params
         */
        override fun doInBackground(vararg params: Void): Boolean? {
            try {
                val url: URL
                try {
                    url = URL("https://clients3.google.com/generate_204")
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                    return false
                }
                val urlConnection: HttpURLConnection
                try {
                    urlConnection = url.openConnection() as HttpURLConnection
                } catch (e: IOException) {
                    e.printStackTrace()
                    return false
                }
                urlConnection.setRequestProperty("User-Agent", "Android")
                urlConnection.setRequestProperty("Connection", "close")
                urlConnection.connectTimeout = 1500
                urlConnection.connect()
                return urlConnection.responseCode == 204 && urlConnection.contentLength == 0
            } catch (e: IOException) {
                return false
            }

        }

        /**
         *
         * @param isInternetAvailable
         */
        override fun onPostExecute(isInternetAvailable: Boolean?) {
            callback(isInternetAvailable ?: false)
        }

    }


    /**
     *
     */
    private class NetworkChangeReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            OnConnectivityInternetChangeListener.getInstance().validateConnection()
        }

    }

}