import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
interface ModelUpdater<V>{
    fun addView(v:V)
    fun removeView(v:V)
}

open class Model: ModelUpdater<View>{
    val db = Firebase.firestore
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