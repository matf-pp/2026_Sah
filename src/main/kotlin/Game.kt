import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class Game {

    var message by mutableStateOf("")
    var board by mutableStateOf(Board())
    var playerOnTurn by mutableStateOf(Player.WHITE)
    var gameState by mutableStateOf(GameState.PLAYING)
    var capturedPieces by mutableStateOf(listOf<ChessPiece>())

    var selectedStartSquare by mutableStateOf<Pair<Int, Int>?>(null)
    var isEndSquareSelected by mutableStateOf(false)
    var moveOptions by mutableStateOf(MoveOptions(emptyList(), emptyList()))

    val moveExecutor = MoveExecutor(this)

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

        selectedStartSquare = null
        isEndSquareSelected = false
        moveOptions = MoveOptions(emptyList(), emptyList())

        message = ""
        playerOnTurn = Player.WHITE
        gameState = GameState.PLAYING
    }

    fun resignGame() {

        if(gameState != GameState.PLAYING) return
        gameState = GameState.RESIGNED

        message = "RESIGNED!" + "   " + if(playerOnTurn == Player.WHITE)
            whoWon(Player.BLACK)
        else
            whoWon(Player.WHITE)
    }
    fun onSquareClick(row: Int, col: Int) {

        if (gameState != GameState.PLAYING) return
        squareSelection(row, col)
        if( isEndSquareSelected ) {

            val (fromRow, fromCol) = selectedStartSquare ?: return
            val (toRow, toCol) = row to col
            val legalMoves = moveOptions.moves.map { it.first }.toSet()

            if (toRow to toCol in legalMoves) {

                val movingPiece = board.grid[fromRow][fromCol] ?:return
                val tempBoard = board.clone()
                if (moveExecutor.checkCastlingConditions(movingPiece,fromCol,toCol))
                {
                    moveExecutor.executeCastling(tempBoard,movingPiece,fromRow,fromCol,toRow,toCol)
                }
                else if(moveExecutor.checkEnPassantConditions(movingPiece,toRow,toCol,tempBoard))
                {
                    moveExecutor.executeEnPassant(tempBoard,movingPiece,fromRow,fromCol,toRow,toCol)
                }
                else
                {
                    moveExecutor.executeNormalMove(tempBoard,movingPiece,fromRow,fromCol,toRow,toCol)
                }

                moveExecutor.updateCastlingRights(tempBoard,fromRow,fromCol,toRow,toCol)
                moveExecutor.updateEnPassantTarget(tempBoard,movingPiece,fromRow,toRow,toCol)

                board = tempBoard
                switchPlayerOnTurn()
            }

            selectedStartSquare = null
            isEndSquareSelected = false
            moveOptions=MoveOptions(emptyList(),emptyList())
        }
    }

    fun squareSelection(row: Int, col: Int) {

        val piece = board.grid[row][col]
        if( selectedStartSquare == null )
        {
            if( piece != null && piece.player == playerOnTurn)
            {
                selectedStartSquare = row to col

                val validator= MoveValidator(board)
                moveOptions=validator.getLegalMoves(row, col)
            }
        }
        else if( piece == null || piece.player != playerOnTurn)
        {
            isEndSquareSelected = true
        }
        else
        {
            selectedStartSquare = row to col

            val validator= MoveValidator(board)
            moveOptions=validator.getLegalMoves(row, col)
        }
    }
    fun switchPlayerOnTurn()
    {
        playerOnTurn = if (playerOnTurn == Player.WHITE)
            Player.BLACK
        else
            Player.WHITE
    }
}