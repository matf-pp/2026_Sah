fun isWhiteSquare(row: Int, col: Int): Boolean
{
    return (row + col) % 2 == 0
}

fun whoWon(player: Player?): String
{
    return when (player)
    {
        null -> "NO WINNER!"
        Player.WHITE -> "WHITE WON!"
        Player.BLACK -> "BLACK WON!"
    }
}

fun colToString(col: Int): String
{
    return ('a' + col).toString()
}