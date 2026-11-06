import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import android.content.Context
import android.provider.Settings
import com.example.code.Model.User

interface ModelUpdater<V>{
    fun addView(v:V)
    fun removeView(v:V)
}

open class Model: ModelUpdater<View>{
    // DATA BASE SET UP STUFF
    val db = Firebase.firestore
    val dataRef = db.collection("EVERYTHING") // put all data in here for now.



    // Use this for the deviceId, this is how we identify the current user.
    var deviceId: String = ""



    // HELPER FUNCTIONS FOR US MODEL PEOPLES, use at your own caution

    /**
     * Always check for null, if this is null then user doesnt exist.
     * Otherwise this returns user as an object
     */
    fun getUserObj(): User?{
        var user: User? = null
        dataRef.document(deviceId).get().addOnSuccessListener {
                snapshot ->
            if (snapshot.exists()){
                user = snapshot.toObject(User::class.java)
            }
        }
        return user
    }

    /**
     * Sets the user's object, useful after getUserObj()
     * WARNING: This will completely remake the ENTIRE document with this User object
     *
     * @param user replacement user
     */
    fun setUserObj(user:User){
        dataRef.document(deviceId).set(user)
    }






    // Everything under this line should be functions that are used by the view
    /**
     * function that sets the device id of the user, this is needed by all models,
     * reference the deviceId attribute.
     *
     * @param deviceId this is the deviceId passed in by the view
     */
    fun setDeviceInformation(deviceId: String){
        this.deviceId = deviceId
    }

    /**
     * delete a profile (any role)
     *
     */
    fun deleteProfile(){
        dataRef.document(deviceId).delete()
    }



    override fun addView(v: View){

    }
    override fun removeView(v: View){

    }
}