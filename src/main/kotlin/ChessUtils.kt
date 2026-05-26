fun isSquareWhite(row: Int, col: Int): Boolean
{
    return (row + col) % 2 == 0
}
fun getPromotionPieces(): List<Piece>
{
    return arrayListOf(Piece.ROOK, Piece.KNIGHT, Piece.BISHOP, Piece.QUEEN)
}
fun winnerMessage(player: Player?): String
{
    return when (player)
    {
        null -> ""
        Player.WHITE -> "WHITE WON!"
        Player.BLACK -> "BLACK WON!"
    }
}

fun colToString(col: Int): String
{
    return ('a' + col).toString()
}

fun findKing(board: Board, player: Player) : Pair<Int, Int>?
{
    var piece:ChessPiece?

    for (row in 0..7)
    {
        for (col in 0..7)
        {
            piece = board.grid[row][col]

            if (piece!=null && piece.type == Piece.KING && piece.player == player)
            {
                return row to col
            }
        }
    }

    return null
}

fun isKingVulnerable(board:Board,opponent: Player, modifyBoard: (Board) -> Unit): Boolean
{
    val tempBoard = board.clone()

    modifyBoard(tempBoard)

    val validator = CheckValidator(tempBoard)
    return validator.isPlayerGivingCheck(opponent)
}

fun formatTime(seconds: Int): String
{
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return if (hours > 0)
    {
        "%02d:%02d:%02d".format(hours, minutes, secs)
    }
    else
    {
        "%02d:%02d".format(minutes, secs)
    }
}