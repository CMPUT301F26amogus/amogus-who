import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
                    modifier = Modifier.background(buttonColor),
                ) {
                    Text(text = "Back", color = textColor)
                }
                Spacer(Modifier.size(40.dp))
                ElevatedButton(
                    onClick = {},
                    modifier = Modifier.background(buttonColor)
                ) {
                    Text(text = "Home", color = textColor)
                }
                Spacer(Modifier.size(40.dp))
                ElevatedButton(
                    onClick = {},
                    modifier = Modifier.background(buttonColor),
                ) {
                    Text(text = "Profile", color = textColor)
                }
            }
        }
    }
}