import androidx.compose.runtime.*
import androidx.compose.ui.window.*

fun main() = application()
{
    Window(onCloseRequest = ::exitApplication, title = "Chess")
    {
        App()
    }
}

@Composable
fun App()
{}
