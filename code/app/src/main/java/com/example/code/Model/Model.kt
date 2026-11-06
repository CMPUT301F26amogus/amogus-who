import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import android.content.Context
import android.provider.Settings

interface ModelUpdater<V>{
    fun addView(v:V)
    fun removeView(v:V)
}

open class Model: ModelUpdater<View>{
    val db = Firebase.firestore
    var deviceId: String = ""

    /**
     * function that sets the device id of the user, this is needed by all models,
     * reference the deviceId attribute.
     *
     * @param deviceId this is the deviceId passed in by the view
     */
    fun setDeviceInformation(deviceId: String){
        this.deviceId = deviceId
    }



    /*
    private val userRef = db.collection("User Data")
    private val CampsiteRef = db.collection("Campsite Data")
    private val adminRef = db.collection("Admin Data")

    boiler plate code, for each collection in the database, idk yet
    */



    override fun addView(v: View){

    }
    override fun removeView(v: View){

    }
}