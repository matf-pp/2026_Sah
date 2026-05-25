import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class Game {

    var checkState by mutableStateOf(CheckState(false, null))
    var message by mutableStateOf("")
    var board by mutableStateOf(Board())
    var playerOnTurn by mutableStateOf(Player.WHITE)
    var gameState by mutableStateOf(GameState.PLAYING)
    var capturedPieces by mutableStateOf(listOf<ChessPiece>())

    var selectedStartSquare by mutableStateOf<Pair<Int, Int>?>(null)
    var isEndSquareSelected by mutableStateOf(false)
    var moveOptions by mutableStateOf(MoveOptions(emptyList(), emptyList()))

    val moveExecutor = MoveExecutor(this)
    val historyManager = HistoryManager(this)
    var timerManager = TimerManager(this)

    var lastBoardStateHash: Long? = null
    val boardStateHashHistory = mutableMapOf<Long,Int>()
    var fiftyMoveCounter:Int = 0

    var promotionSquare by mutableStateOf<Pair<Int, Int>?>(null)
    var pendingPromotionPlayer by mutableStateOf<Player?>(null)

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

        checkState = CheckState(false, null)
        capturedPieces = listOf()

        selectedStartSquare = null
        isEndSquareSelected = false
        moveOptions = MoveOptions(emptyList(), emptyList())

        message = ""
        playerOnTurn = Player.WHITE
        gameState = GameState.PLAYING

        promotionSquare =null
        pendingPromotionPlayer = null

        boardStateHashHistory.clear()
        lastBoardStateHash = null
        fiftyMoveCounter = 0

        historyManager.reset()
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
            whoWon(Player.BLACK)
        else
            whoWon(Player.WHITE)
    }

    fun onSquareClick(row: Int, col: Int)
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

                board = tempBoard
                historyManager.addBoardToSnapshots(board.clone())

                if (checkPromotionConditions(movingPiece,toRow))
                {
                    promotionSquare = toRow to toCol
                    pendingPromotionPlayer = movingPiece.player
                    return
                }

                lastBoardStateHash = calculateBoardStateHash()
                boardStateHashHistory[lastBoardStateHash!!] = (boardStateHashHistory[lastBoardStateHash] ?: 0) + 1

                evaluateCheck(playerOnTurn)
                evaluateEndConditions(playerOnTurn)

                switchPlayerOnTurn()
            }

            selectedStartSquare = null
            isEndSquareSelected = false
            moveOptions=MoveOptions(emptyList(),emptyList())
        }
    }

    fun checkPromotionConditions(movingPiece: ChessPiece,row:Int):Boolean
    {
        return movingPiece.type == Piece.PAWN &&
                ((movingPiece.player == Player.WHITE && row == 0) || (movingPiece.player == Player.BLACK && row == 7))
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
        val drawValidator = DrawValidator(board,lastBoardStateHash!!,boardStateHashHistory,fiftyMoveCounter)

        if (checkValidator.isOpponentCheckmatedByPlayer(player))
        {
            message = "CHECKMATE!" + "  " + whoWon(player)
            gameState = GameState.CHECKMATE
        }
        else if (checkValidator.isStalemateCausedByPlayer(player))
        {
            message = "STALEMATE!" + "  " + whoWon(null)
            gameState = GameState.STALEMATE
        }
        else if (drawValidator.isDraw())
        {
            message = "DRAW!" + "  " + whoWon(null)
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
    fun pawnPromotion(pieceType: Piece)
    {
        val (row, col) = promotionSquare!!

        val tempBoard = board.clone()
        tempBoard.grid[row][col]= ChessPiece(pieceType, pendingPromotionPlayer!!)
        board = tempBoard

        historyManager.removeLastBoard()
        historyManager.addBoardToSnapshots(board.clone())

        val last = historyManager.popLastMove()
        val updated = last.copy(promotion = pieceType)
        historyManager.addMoveToHistory(updated)

        lastBoardStateHash = calculateBoardStateHash()
        boardStateHashHistory[lastBoardStateHash!!] = (boardStateHashHistory[lastBoardStateHash] ?: 0) + 1

        evaluateCheck(pendingPromotionPlayer!!)
        evaluateEndConditions(pendingPromotionPlayer!!)

        switchPlayerOnTurn()

        promotionSquare = null
        pendingPromotionPlayer = null

        selectedStartSquare = null
        isEndSquareSelected = false
        moveOptions=MoveOptions(emptyList(),emptyList())
    }
    fun setTime(time: Int) {
        timerManager.setTime(time)
    }

    fun calculateBoardStateHash(): Long
    {
        var hash: Long = 1

        for (i in 0..7)
        {
            for (j in 0..7)
            {
                val piece = board.grid[i][j]
                val pieceValue = if (piece == null)
                    0
                else
                    (if (piece.player == Player.WHITE) 1 else 7) + piece.type.ordinal
                hash = 31 * hash + pieceValue
            }
        }

        hash = 31 * hash + if (board.castlingRights.whiteKingSide) 1 else 0
        hash = 31 * hash + if (board.castlingRights.whiteQueenSide) 1 else 0
        hash = 31 * hash + if (board.castlingRights.blackKingSide) 1 else 0
        hash = 31 * hash + if (board.castlingRights.blackQueenSide) 1 else 0

        if (board.enPassantTarget != null)
        {
            hash = 31 * hash + board.enPassantTarget!!.first
            hash = 31 * hash + board.enPassantTarget!!.second
        }

        hash = 31 * hash + (if (playerOnTurn == Player.WHITE) 1 else 0)

        return hash
    }



}