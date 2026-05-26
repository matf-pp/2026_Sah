import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch

fun main() = application()
{
    val game = remember { Game().apply { init() } }

    Window(onCloseRequest = ::exitApplication, title = "Chess")
    {
        App(game)
    }
}

@Composable
fun App(game: Game)
{
    val commonTextStyle = MaterialTheme.typography.headlineSmall

    if (game.timerManager.setGameTime)
    {
        setGameTime(game)
    }
    else
    {
        LaunchedEffect(Unit)
        {
            game.timerManager.startTimer()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    )
    {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF4A2F1A))
                .padding(2.dp)
        )
        {
            if (maxWidth < 700.dp)
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                )
                {
                    topBarItems(game)
                }
            else
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF4A2F1A))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    topBarItems(game)
                }
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
                    .weight(0.15f)
                    .background(Color(0xFFB58863))
                    .wrapContentWidth()
                    .fillMaxHeight(1f)
                    .padding(10.dp)
            )
            {
                Text(
                    text = "Match history",
                    style = commonTextStyle,
                    maxLines = 1,
                    softWrap = false
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f, fill = false)
                )
                {
                    game.historyManager.getMovesHistoryFormated().forEachIndexed { index, moveText ->
                        Text(
                            text = moveText,
                            style = commonTextStyle,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.clickable {
                                game.historyManager.goToMove(index)
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.weight(0.85f),
                horizontalArrangement = Arrangement.SpaceEvenly,

            )
            {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(0.2f)
                        .padding(5.dp),
                )
                {
                    Text(
                        "White captures",
                        style = commonTextStyle,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false
                    )

                    game.capturedPieces
                        .filter { it.player == Player.WHITE }
                        .forEach{ piece ->
                            Text(
                                text = piece.type.getSymbol(),
                                fontSize = 40.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                }

                Column(
                    modifier = Modifier.weight(0.6f),
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    gameInfoText(game)

                    val scope = rememberCoroutineScope()
                    ChessBoard(
                        game=game,
                        onSquareClick = { row, col ->
                            scope.launch {
                                game.onSquareClick(row, col)
                            }
                        }
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(0.2f)
                        .padding(5.dp)
                )
                {
                    Text(
                        "Black captures",
                        style = commonTextStyle,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false
                    )

                    game.capturedPieces
                        .filter { it.player == Player.BLACK }
                        .forEach{ piece ->
                            Text(
                                textAlign = TextAlign.Center,
                                text = piece.type.getSymbol(),
                                fontSize = 40.sp,
                                color = Color.Black,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                }
            }
        }
    }

    if(game.promotionSquare != null)
    {
        promotionDialog(game)
    }
}

@Composable
fun ChessBoard(
    game: Game,
    onSquareClick: (row: Int, col: Int) -> Unit
)
{
    val lightSquare = Color(0xFFF0D9B5)
    val darkSquare = Color(0xFFB58863)
    val checkSquare = Color(0xFFE53935)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxHeight()
    )
    {
        val boardSize = minOf(maxWidth, maxHeight)
        val squareSize = boardSize / 10

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Column()
            {
                for (row in 0..7) {
                    Box(
                        modifier = Modifier
                            .width(squareSize * 0.4f)
                            .height(squareSize),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Text("${8 - row}",
                            fontSize = (squareSize.value * 0.2f).sp,
                            maxLines = 1,
                            softWrap = false)
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
                                for (col in 0..7) {

                                    val piece = game.board.grid[row][col]
                                    val isSelected = game.selectedStartSquare == (row to col)

                                    val isInCheck = game.checkState.isCheck && game.checkState.kingPosition == (row to col)
                                    val isMovable = (row to col) in game.moveOptions.moves.map { it.first }.toSet()
                                    val isCapturable = (row to col) in game.moveOptions.captures

                                    Box(
                                        modifier = Modifier
                                            .size(squareSize)
                                            .background(if (isSquareWhite(row, col)) lightSquare else darkSquare)
                                            .then(
                                                if (isSelected)
                                                    Modifier.border(3.dp, Color.Green)
                                                else
                                                    Modifier
                                            ).then(
                                                if (isInCheck)
                                                    Modifier.background(checkSquare.copy(alpha = 0.4f))
                                                else
                                                    Modifier
                                            )
                                            .clickable { onSquareClick(row, col) },
                                        contentAlignment = Alignment.Center
                                    )
                                    {
                                        if (piece != null) {
                                            Text(
                                                text = piece.type.getSymbol(),
                                                fontSize = (squareSize.value * 0.8f).sp,
                                                color = if (piece.player == Player.WHITE)
                                                    Color.White
                                                else
                                                    Color.Black,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                        if (isCapturable) {
                                            Box(
                                                modifier = Modifier.fillMaxSize().padding(squareSize * 0.04f)
                                                    .border(3.dp, Color.Black.copy(alpha = 0.35f), CircleShape)
                                            )
                                        } else if (isMovable) {
                                            Box(
                                                modifier = Modifier.size(squareSize * 0.15f)
                                                    .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Row()
                {
                    for (c in 'a'..'h') {
                        Box(
                            modifier = Modifier
                                .width(squareSize)
                                .height(squareSize * 0.4f),
                            contentAlignment = Alignment.Center
                        )
                        {
                            Text("$c",
                                fontSize = (squareSize.value * 0.2f).sp,
                                maxLines = 1,
                                softWrap = false)
                        }
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
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            ),
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun topBarItems(game: Game)
{
    timerBox(
        label = "White",
        totalSeconds = game.timerManager.timeLeftWhite,
        background = Color.DarkGray,
        textColor = Color.White
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    )
    {
        item {
            styledButton("RESTART GAME") { game.restartGame() }
        }
        item {
            styledButton("RESIGN GAME") { game.resignGame() }
        }
        item {
            styledButton("PAUSE GAME") { game.pauseGame() }
        }
    }

    timerBox(
        label = "Black",
        totalSeconds = game.timerManager.timeLeftBlack,
        background = Color(0xFFB58863),
        textColor = Color.White
    )
}

@Composable
fun timerBox(
    label: String,
    totalSeconds: Int,
    background: Color,
    textColor: Color
)
{
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.75f),
                maxLines = 1,
                softWrap = false
            )

            Text(
                text = formatTime(totalSeconds),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun gameInfoText(game : Game)
{
    val (text, color) = if (game.gameState != GameState.PLAYING)
    {
        game.message to MaterialTheme.colorScheme.error
    }
    else
    {
        if (game.playerOnTurn == Player.WHITE)
            "White to move" to MaterialTheme.colorScheme.onSurface
        else
            "Black to move" to MaterialTheme.colorScheme.onSurface
    }

    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.headlineMedium,
        maxLines = 1,
        softWrap = false
    )
}

@Composable
fun promotionDialog(game: Game)
{
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
    {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize()
        )
        {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            )
            {
                item {
                    Text(
                        "Pawn Promotion",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                items(getPromotionPieces().toList())
                {
                    styledButton(it.getSymbol() + " " + it.getLabel())
                    {
                        game.promotionPiece.complete(it)
                    }
                }
            }
        }
    }
}

@Composable
fun setGameTime(game: Game)
{
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
    {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(16.dp)
                .widthIn(max = 420.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "Game type",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                items( GameType.entries.toList())
                {
                    type -> styledButton("${type.gameTypeName} (${formatTime(type.gameTime)})")
                    {
                        game.timerManager.setTime(type.gameTime)
                        game.timerManager.setGameTime = false
                    }
                }
            }
        }
    }
}