import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import androidx.compose.material3.*

fun main() = application()
{
    Window(onCloseRequest = ::exitApplication, title = "Chess")
    {
        App()
    }
}

@Composable
fun App()
{
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()

    val commonTextStyle = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
        color = Color.Black
    )
    Column(
        modifier = Modifier.fillMaxSize()
    )
    {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFB58863))
        )
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF4A2F1A))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row()
                {
                    styledButton("RESTART GAME") {  //TODO game functions should be called here
                         }
                    styledButton("RESIGN GAME") {  }
                }
            }
            //TODO title and some other buttons should be displayed here
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        )
        {
            Column(
                modifier = Modifier
                    .background(Color(0xFFB58863))
                    .wrapContentWidth()
                    .fillMaxHeight(1f)
                    .padding(10.dp)
            )
            {
                Text(
                    text = "Match history",
                    style = commonTextStyle
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f, fill = false)
                )
                {
                    //TODO this column should display game history
                }
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            )
            {
                Column(horizontalAlignment = Alignment.CenterHorizontally)
                {
                    Text(
                        "White captures",
                        style = commonTextStyle
                    )

                    //TODO captured pieces should be displayed here
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally)
                {
                    //TODO game info should be displayed here
                    Box(
                        modifier = Modifier
                            .horizontalScroll(hScroll)
                            .verticalScroll(vScroll)
                    )
                    {
                        ChessBoard()
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally)
                {
                    Text(
                        "Black captures",
                        style = commonTextStyle
                    )
                    //TODO captured pieces should be displayed here
                }
            }
        }
    }
}
fun isWhiteSquare(row: Int, col: Int): Boolean {
    return (row + col) % 2 == 0
}
@Composable
fun ChessBoard() {
    val lightSquare = Color(0xFFF0D9B5)
    val darkSquare = Color(0xFFB58863)

    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Column()
        {
            for (row in 0..7) {
                Box(
                    modifier = Modifier.size(36.dp, 96.dp),
                    contentAlignment = Alignment.Center
                )
                {
                    Text("${8 - row}", fontSize = 24.sp)
                }
            }
        }

        Column()
        {
            Box(
                modifier = Modifier
                    .border(4.dp, Color(0xFF5C4033), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF5C4033))
                    .padding(8.dp)
            )
            {
                Column()
                {
                    for (row in 0..7) {
                        Row()
                        {
                            for (col in 0..7)
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .background(if (isWhiteSquare(row, col)) lightSquare else darkSquare),
                                    contentAlignment = Alignment.Center
                                ){}
                        }
                    }
                }
            }
            Row()
            {
                for (c in 'a'..'h') {
                    Box(
                        modifier = Modifier.size(96.dp, 36.dp),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Text("$c", fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun styledButton(
    label: String,
    onClick: () -> Unit
)
{
    Button(
        modifier = Modifier
            .padding(8.dp)
            .height(48.dp),
        onClick = { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF31302E),
            contentColor = Color.White
        )
    )
    {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        )
    }
}