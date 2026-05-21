class MoveExecutor(private val game: Game)
{
    fun executeNormalMove(board: Board, movingPiece: ChessPiece, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int)
    {
        val capturedPiece = board.grid[toRow][toCol]

        if (capturedPiece != null) {
            game.capturedPieces += capturedPiece

        }

        board.grid[toRow][toCol] = movingPiece
        board.grid[fromRow][fromCol] = null
    }
}