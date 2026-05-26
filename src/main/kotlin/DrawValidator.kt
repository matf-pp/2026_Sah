class DrawValidator(private val game: Game)
{
    var lastBoardState: Long? = null
    val boardStateHistory = mutableMapOf<Long,Int>()
    var fiftyMoveCounter = 0

    fun isDraw(): Boolean
    {
        if(isInsufficientMaterial())
        {
            return true
        }
        if(threeFoldRepetition())
        {
            return true
        }
        if(fiftyMoveRule())
        {
            return true
        }

        return false
    }
    fun isInsufficientMaterial(): Boolean
    {
        val pieces = mutableListOf< Pair<ChessPiece, Pair<Int,Int>> >()

        for (row in 0..7)
        {
            for (col in 0..7)
            {
                val piece = game.board.grid[row][col]
                if (piece != null && piece.type != Piece.KING)
                {
                    pieces.add(piece to (row to col))
                }
            }
        }

        if (pieces.isEmpty()) return true

        if (pieces.size == 1 && (pieces[0].first.type == Piece.KNIGHT || pieces[0].first.type == Piece.BISHOP))
        {
            return true
        }

        if (pieces.size == 2 &&
            pieces[0].first.type == Piece.BISHOP && pieces[1].first.type == Piece.BISHOP &&
            pieces[0].first.player != pieces[1].first.player &&
            isSquareWhite(pieces[0].second.first,pieces[0].second.second) ==
            isSquareWhite(pieces[1].second.first,pieces[1].second.second)
        )
        {
            return true
        }

        return false
    }
    fun threeFoldRepetition(): Boolean
    {
        return boardStateHistory[lastBoardState] == 3
    }
    fun fiftyMoveRule(): Boolean
    {
        return fiftyMoveCounter==50
    }

    fun calculateBoardStateHash(): Long
    {
        var hash: Long = 1

        for (i in 0..7)
        {
            for (j in 0..7)
            {
                val piece = game.board.grid[i][j]
                val pieceValue = if (piece == null)
                    0
                else
                    (if (piece.player == Player.WHITE) 1 else 7) + piece.type.ordinal
                hash = 31 * hash + pieceValue
            }
        }

        hash = 31 * hash + if (game.board.castlingRights.whiteKingSide) 1 else 0
        hash = 31 * hash + if (game.board.castlingRights.whiteQueenSide) 1 else 0
        hash = 31 * hash + if (game.board.castlingRights.blackKingSide) 1 else 0
        hash = 31 * hash + if (game.board.castlingRights.blackQueenSide) 1 else 0

        if (game.board.enPassantTarget != null)
        {
            hash = 31 * hash + game.board.enPassantTarget!!.first
            hash = 31 * hash + game.board.enPassantTarget!!.second
        }

        hash = 31 * hash + (if (game.playerOnTurn == Player.WHITE) 1 else 0)

        return hash
    }

    fun reset()
    {
        lastBoardState = null
        boardStateHistory.clear()
        fiftyMoveCounter = 0
    }
}