package gt.uvg.laboratorio9

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import gt.uvg.laboratorio9.ui.StoreApp
import gt.uvg.laboratorio9.ui.theme.Laboratorio9Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Laboratorio9Theme {
                StoreApp()
            }
        }
    }
}