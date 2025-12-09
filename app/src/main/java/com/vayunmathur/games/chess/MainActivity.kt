package com.vayunmathur.games.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vayunmathur.games.chess.ui.theme.ChessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessTheme {
                val viewModel: ChessViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                ChessGame(
                    uiState = uiState,
                    onSquareClick = viewModel::onSquareClick,
                    onPromote = viewModel::onPromote,
                    onResetGame = viewModel::resetGame
                )
            }
        }
    }
}

@Composable
fun ChessGame(
    uiState: ChessUiState,
    onSquareClick: (Position) -> Unit,
    onPromote: (PieceType) -> Unit,
    onResetGame: () -> Unit
) {
    if (uiState.board.promotionPosition != null) {
        PawnPromotionDialog(uiState.turn, onPromote = onPromote)
    }

    Scaffold { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
            Arrangement.Center,
            Alignment.CenterHorizontally
        ) {
            CapturedPiecesRow(uiState.board.capturedByBlack)
            Spacer(modifier = Modifier.height(16.dp))
            MovesList(moves = uiState.board.moves, turn = uiState.turn)
            ChessBoard(uiState.board, uiState.selectedPiece, uiState.turn) { position ->
                onSquareClick(position)
            }
            Spacer(modifier = Modifier.height(16.dp))
            CapturedPiecesRow(uiState.board.capturedByWhite)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onResetGame) {
                Text("Reset Game")
            }
            uiState.gameStatus?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MovesList(moves: List<Move>, turn: PieceColor) {
    Box(Modifier.height(100.dp)) {
        LazyColumn(
            Modifier
                .fillMaxHeight()
                .border(2.dp, Color.Gray)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    Text("White", fontWeight = if (turn == PieceColor.WHITE) FontWeight.Bold else FontWeight.Normal)
                    VerticalDivider(color = MaterialTheme.colorScheme.primary)
                    Text("Black", fontWeight = if (turn == PieceColor.BLACK) FontWeight.Bold else FontWeight.Normal)
                }
            }
            items(moves.chunked(2)) { move ->
                Row(Modifier.fillMaxWidth()) {
                    Text(move[0].toString(), Modifier.weight(1f), textAlign = TextAlign.Center)
                    if (move.size == 2) {
                        VerticalDivider(color = MaterialTheme.colorScheme.primary)
                        Text(move[1].toString(), Modifier.weight(1f), textAlign = TextAlign.Center)
                    } else {
                        VerticalDivider()
                        Text("", Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun PawnPromotionDialog(color: PieceColor, onPromote: (PieceType) -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(text = "Promote Pawn") },
        text = {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                for (pieceType in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                    Box(Modifier.clickable { onPromote(pieceType) }) {
                        ChessPiece(Piece(pieceType, color), 64.dp)
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun CapturedPiecesRow(pieces: List<Piece>) {
    Box(
        Modifier
            .height(48.dp)
            .padding(8.dp), Alignment.Center) {
        if (pieces.isNotEmpty()) {
            Card {
                Row(
                    Modifier.padding(4.dp),
                ) {
                    pieces.forEach { ChessPiece(it, 32.dp) }
                }
            }
        }
    }
}

@Composable
fun ChessBoard(board: Board, selectedPiece: Position?, turn: PieceColor, onSquareClick: (Position) -> Unit) {
    val isKingInCheck = board.isKingInCheck(turn)
    Column(Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        for (i in board.pieces.indices) {
            Row(modifier = Modifier.weight(1f)) {
                for (j in board.pieces[i].indices) {
                    val piece = board.pieces[i][j]
                    val isSelected = selectedPiece?.let { it.row == i && it.col == j } ?: false
                    val isKingInCheckSquare =
                        isKingInCheck && piece?.type == PieceType.KING && piece.color == turn
                    val color = if ((i + j) % 2 == 0) Color(0xFFBBBBBB) else Color.Gray

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(color)
                            .clickable { onSquareClick(Position(i, j)) }
                            .then(
                                if (isSelected)
                                    Modifier.border(2.dp, Color.Yellow)
                                else if (isKingInCheckSquare)
                                    Modifier.border(2.dp, Color.Red)
                                else Modifier
                            )
                    ) {
                        piece?.let {
                            ChessPiece(it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChessPiece(piece: Piece, size: Dp? = null) {
    Image(
        painterResource(id = piece.type.resID),
        "${piece.color} ${piece.type}",
        if (size != null) Modifier.size(size) else Modifier.fillMaxSize(),
        colorFilter = ColorFilter.tint(if (piece.color == PieceColor.WHITE) Color.White else Color.Black)
    )
}