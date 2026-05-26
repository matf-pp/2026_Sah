import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CompletableDeferred

class Game
{
    var board by mutableStateOf(Board())

    var message by mutableStateOf("")
    var playerOnTurn by mutableStateOf(Player.WHITE)

    var moveOptions by mutableStateOf(MoveOptions(emptyList(), emptyList()))
    var capturedPieces by mutableStateOf(listOf<ChessPiece>())

    var selectedStartSquare by mutableStateOf<Pair<Int, Int>?>(null)
    var isEndSquareSelected by mutableStateOf(false)

    var promotionSquare by mutableStateOf<Pair<Int, Int>?>(null)
    var promotionPiece = CompletableDeferred<Piece>()

    var checkState by mutableStateOf(CheckState(false, null))
    var gameState by mutableStateOf(GameState.PLAYING)

    val moveExecutor = MoveExecutor(this)
    val historyManager = HistoryManager(this)
    val drawValidator = DrawValidator(this)
    var timerManager = TimerManager(this)

    fun init()
    {
        val tempBoard = Board()

        tempBoard.clearBoard()
        tempBoard.setUpInitialBoard()
        tempBoard.setUpCastlingRights()
        tempBoard.setUpEnPassantTarget()

        board = tempBoard
    }

    fun restartGame()
    {
        init()

        message = ""
        playerOnTurn = Player.WHITE

        moveOptions = MoveOptions(emptyList(), emptyList())
        capturedPieces = listOf()

        selectedStartSquare = null
        isEndSquareSelected = false

        promotionSquare =null
        promotionPiece = CompletableDeferred()

        checkState = CheckState(false, null)
        gameState = GameState.PLAYING

        historyManager.reset()
        drawValidator.reset()
        timerManager.reset()
    }

    fun resignGame() {

        if(gameState != GameState.PLAYING) return
        gameState = GameState.RESIGNED
        timerManager.stopTimer()
        historyManager.addMoveToHistory(Move(
            -1,
            (-1 to -1),
            (-1 to -1),
            null,
            false,
            null,
            null,
            false,
            gameState,
            playerOnTurn))

            message = "RESIGNED!" + "   " + if(playerOnTurn == Player.WHITE)
            winnerMessage(Player.BLACK)
        else
            winnerMessage(Player.WHITE)
    }

    suspend fun onSquareClick(row: Int, col: Int)
    {
        if (gameState != GameState.PLAYING) return

        squareSelection(row, col)

        if( isEndSquareSelected )
        {
            val (fromRow, fromCol) = selectedStartSquare ?: return
            val (toRow, toCol) = row to col

            val legalMoves = moveOptions.moves.map { it.first }.toSet()

            if (toRow to toCol in legalMoves)
            {
                historyManager.increaseMoveCounter()

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

                if (moveExecutor.checkPromotionConditions(movingPiece,toRow))
                {
                    promotionSquare = toRow to toCol
                    promotionPiece = CompletableDeferred()

                    val selectedPromotionPiece = moveExecutor.awaitPromotionPiece()

                    moveExecutor.pawnPromotion(tempBoard,selectedPromotionPiece,playerOnTurn)

                    promotionSquare = null
                }

                board = tempBoard
                historyManager.addBoardToSnapshots(board.clone())

                drawValidator.lastBoardState = drawValidator.calculateBoardStateHash()
                drawValidator.boardStateHistory[drawValidator.lastBoardState!!] = (drawValidator.boardStateHistory[drawValidator.lastBoardState] ?: 0) + 1

                evaluateCheck(playerOnTurn)
                evaluateEndConditions(playerOnTurn)

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

    fun evaluateCheck(player: Player)
    {
        val validator = CheckValidator(board)

        if (validator.isPlayerGivingCheck(player))
        {
            val enemy = if (player == Player.WHITE) Player.BLACK else Player.WHITE
            checkState = CheckState(true, findKing(board, enemy))

            val last = historyManager.popLastMove()
            val updated = last.copy(check=true)
            historyManager.addMoveToHistory(updated)
        }
        else
        {
            checkState = CheckState(false, null)
        }
    }
    fun evaluateEndConditions(player: Player)
    {
        val checkValidator = CheckValidator(board)

        if (checkValidator.isOpponentCheckmatedByPlayer(player))
        {
            message = "CHECKMATE!" + "  " + winnerMessage(player)
            gameState = GameState.CHECKMATE
        }
        else if (checkValidator.isStalemateCausedByPlayer(player))
        {
            message = "STALEMATE!" + "  " + winnerMessage(null)
            gameState = GameState.STALEMATE
        }
        else if (drawValidator.isDraw())
        {
            message = "DRAW!" + "  " + winnerMessage(null)
            gameState = GameState.DRAW
        }
        else
        {
            message = ""
            gameState = GameState.PLAYING
        }
        if (gameState != GameState.PLAYING) {
            timerManager.stopTimer()
            historyManager.addMoveToHistory(Move(
                -1,
                (-1 to -1),
                (-1 to -1),
                null,
                false,
                null,
                null,
                false,
                gameState,
                playerOnTurn))
        }
    }
}