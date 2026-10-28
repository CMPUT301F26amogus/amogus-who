import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

interface ViewObserver<M> {
    fun update( model: M )
}

abstract open class View: ViewObserver<Model>{
    override fun update(model: Model) {

    }
    //Common colors go here
    protected val entrantHeaderBackgroundColor = Color(0xff28b019)
    protected val entrantHeaderTextColor = Color(0xffffffff)
    protected val entrantFooterBackgroundColor = Color(0xff93cd8b)
    protected val entrantFooterButtonColor = Color(0xff9bfa8e)
    protected val entrantFooterTextColor = Color(0xff4a4459)

    protected val ownerHeaderBackgroundColor = Color(0xff054f33)
    protected val ownerHeaderTextColor = Color(0xffffffff)
    protected val ownerFooterBackgroundColor = Color(0xff57bc78)
    protected val ownerFooterButtonColor = Color(0xff9bfa8e)
    protected val ownerFooterTextColor = Color(0xff4a4459)

    protected val adminHeaderBackgroundColor = Color(0xff033131)
    protected val adminHeaderTextColor = Color(0xffffffff)
    protected val adminFooterBackgroundColor = Color(0xff5aae8c)
    protected val adminFooterButtonColor = Color(0xff9bfa8e)
    protected val adminFooterTextColor = Color(0xff4a4459)

    //Generic Component Composables go here
    @Composable
    protected fun header(text: String, backgroundColor: Color, textColor: Color) {
        Column(Modifier.fillMaxWidth().height(100.dp).background(backgroundColor)) {
            Spacer(Modifier.size(25.dp))
            Row(modifier = Modifier.fillMaxWidth().height(80.dp).background(backgroundColor)) {
                Spacer(Modifier.size(20.dp))
                Text(text = text, fontSize = 60.sp, color = textColor)
            }
        }
    }
    @Composable
    protected fun footer(backgroundColor:Color,textColor:Color,buttonColor: Color){
        Column(Modifier.fillMaxWidth().height(100.dp).background(backgroundColor)) {
            Spacer(Modifier.size(25.dp))
            Row(modifier = Modifier.fillMaxWidth().height(80.dp).background(backgroundColor)) {
                Spacer(Modifier.size(40.dp))
                ElevatedButton(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                ) {
                    Text(text = "Back", color = textColor)
                }
                Spacer(Modifier.size(40.dp))
                ElevatedButton(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                ) {
                    Text(text = "Home", color = textColor)
                }
                Spacer(Modifier.size(40.dp))
                ElevatedButton(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                ) {
                    Text(text = "Profile", color = textColor)
                }
            }
        }
    }
}