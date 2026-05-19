import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class Game {

    var message by mutableStateOf("")
    var board by mutableStateOf(Board())
    var playerOnTurn by mutableStateOf(Player.WHITE)
    var gameState by mutableStateOf(GameState.PLAYING)

    fun init()
    {
        val tempBoard = Board()

        tempBoard.clearBoard()
        tempBoard.setUpInitialBoard()
        tempBoard.setUpCastlingRights()
        tempBoard.setUpEnPassantTarget()

        board = tempBoard
    }

    fun restartGame() {
        init()

        message = ""
        playerOnTurn = Player.WHITE
        gameState = GameState.PLAYING
    }

    fun resignGame() {
        gameState = GameState.RESIGNED

        message = "RESIGNED!" + "   " + if(playerOnTurn == Player.WHITE)
            whoWon(Player.BLACK)
        else
            whoWon(Player.WHITE)
    }
    fun onSquareClick(row: Int, col: Int) {}
}