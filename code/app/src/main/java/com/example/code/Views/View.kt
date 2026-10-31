import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.Views.Option

interface ViewObserver<M> {
    fun update( model: M )
}

abstract open class View: ViewObserver<Model>{
    override fun update(model: Model) {

    }
    //Common colors go here
    protected val light = Color(0xffffffff)
    protected val dark = Color(0xff000000)
    protected val gray = Color(0xff7d7e7d)
    protected val entrantHeaderBackgroundColor = Color(0xff28b019)
    protected val entrantBackgroundColor = Color(0xffcae99d)
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
    protected fun header(headerText: String = "",
                         subheaderText:String = "",
                         backgroundColor: Color,
                         textColor: Color,
                         headerTextSize: Int = 40,
                         subheaderTextSize: Int = 30) {
        Column(Modifier.fillMaxWidth().height(120.dp).background(backgroundColor)) {
            if (headerText!="") {
                Spacer(Modifier.size(25.dp))
                Row(modifier = Modifier.fillMaxWidth().height(50.dp).background(backgroundColor)) {
                    Spacer(Modifier.size(20.dp))
                    Text(text = headerText, fontSize = headerTextSize.sp, color = textColor)
                }
            }
            if (subheaderText!="") {
                Spacer(Modifier.size(5.dp))
                Row(modifier = Modifier.fillMaxWidth().height(50.dp).background(backgroundColor)) {
                    Spacer(Modifier.size(60.dp))
                    Text(text = subheaderText, fontSize = subheaderTextSize.sp, color = textColor)
                }
            }
        }
    }
    @Composable
    protected fun footer(backgroundColor:Color,
                         textColor:Color,
                         buttonColor: Color,
                         excludeButtons: Boolean = false){
        Column(Modifier.fillMaxWidth().height(100.dp).background(backgroundColor)) {
            Spacer(Modifier.size(25.dp))
            Row(modifier = Modifier.fillMaxWidth().height(80.dp).background(backgroundColor)) {
                if (!excludeButtons) {
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
    @Composable
    protected fun TabManager(tabs:List<Tab>){
        Column(modifier = Modifier.height(50.dp)) {
            LazyRow(modifier = Modifier.height(50.dp)) {
                itemsIndexed(tabs) { index,tab: Tab ->
                    TabCol(tab = tab)
                }
            }
        }
    }
    @Composable
    private fun TabCol(tab: Tab) {
        Column(
            modifier = Modifier
                .height(60.dp)
                .clickable(enabled = true, onClick = { tab.option })
        ) {
            Row(modifier = Modifier.background(tab.col).width(120.dp).fillMaxHeight()) {
                Spacer(modifier = Modifier.width(10.dp))
                Column(Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = tab.name,
                        fontSize = 25.sp,
                        color = ownerHeaderBackgroundColor,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                }
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}
class Tab(name: String, color: Color, option: (Unit)->Unit){
    var name = name
    var col = color
    var option = option
}