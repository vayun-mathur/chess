package com.vayunmathur.games.chess

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ChessUiState(
    val board: Board = Board.initialState,
    val selectedPiece: Position? = null,
    val turn: PieceColor = PieceColor.WHITE,
    val gameStatus: String? = null
)

class ChessViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChessUiState())
    val uiState: StateFlow<ChessUiState> = _uiState.asStateFlow()

    fun onSquareClick(position: Position) {
        val currentState = _uiState.value
        if (currentState.gameStatus != null || currentState.board.promotionPosition != null) return

        val piece = currentState.board.pieces[position.row][position.col]
        if (currentState.selectedPiece == null) {
            if (piece != null && piece.color == currentState.turn) {
                _uiState.update { it.copy(selectedPiece = position) }
            }
        } else {
            if (currentState.board.isValidMove(currentState.selectedPiece, position)) {
                val newBoard = currentState.board.movePiece(currentState.selectedPiece, position)
                _uiState.update {
                    it.copy(
                        board = newBoard,
                        turn = if (newBoard.promotionPosition == null) {
                            if (it.turn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
                        } else {
                            it.turn
                        },
                        selectedPiece = null
                    )
                }
                checkGameStatus()
            } else {
                _uiState.update { it.copy(selectedPiece = null) }
            }
        }
    }

    fun onPromote(pieceType: PieceType) {
        val currentState = _uiState.value
        val newBoard = currentState.board.promotePawn(currentState.board.promotionPosition!!, pieceType)
        _uiState.update {
            it.copy(
                board = newBoard,
                turn = if (it.turn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
            )
        }
        checkGameStatus()
    }

    fun resetGame() {
        _uiState.value = ChessUiState()
    }

    private fun checkGameStatus() {
        val currentState = _uiState.value
        if (currentState.board.isCheckmate(currentState.turn)) {
            _uiState.update {
                it.copy(gameStatus = "Checkmate! ${if (currentState.turn == PieceColor.WHITE) "Black" else "White"} wins.")
            }
        }
    }
}
