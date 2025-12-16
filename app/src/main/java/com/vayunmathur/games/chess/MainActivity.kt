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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
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

                var showNewGameDialog by remember { mutableStateOf(true) }

                ChessGame(
                    uiState = uiState,
                    onSquareClick = viewModel::onSquareClick,
                    onPromote = viewModel::onPromote,
                    onNewGame = { showNewGameDialog = true }
                )

                if (showNewGameDialog) {
                    NewGameDialog(
                        onNewGame = {
                            viewModel.onNewGame(it)
                            showNewGameDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NewGameDialog(onNewGame: (GameMode) -> Unit) {
    var showColorSelection by remember { mutableStateOf<((PieceColor) -> Unit)?>(null) }

    if (showColorSelection != null) {
        AlertDialog(
            onDismissRequest = { showColorSelection = null },
            title = { Text("Select Your Color") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { showColorSelection?.invoke(PieceColor.WHITE) }) {
                        Text("White")
                    }
                    Button(onClick = { showColorSelection?.invoke(PieceColor.BLACK) }) {
                        Text("Black")
                    }
                }
            },
            confirmButton = { }
        )
    } else {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(text = "New Game") },
            text = {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = { onNewGame(GameMode.TwoPlayer) }) {
                        Text("2-Player Local")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        showColorSelection = {
                            onNewGame(GameMode.VsAI(it))
                        }
                    }) {
                        Text("Human vs AI")
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun ChessGame(
    uiState: ChessUiState,
    onSquareClick: (Position) -> Unit,
    onPromote: (PieceType) -> Unit,
    onNewGame: () -> Unit
) {
    if (uiState.board.promotionPosition != null) {
        PawnPromotionDialog(if (uiState.turn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE, onPromote = onPromote)
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
            ChessBoard(
                board = uiState.board,
                selectedPiece = uiState.selectedPiece,
                turn = uiState.turn,
                isFlipped = uiState.isBoardFlipped,
                onSquareClick = onSquareClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            CapturedPiecesRow(uiState.board.capturedByWhite)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNewGame) {
                Text("New Game")
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
fun ChessBoard(
    board: Board,
    selectedPiece: Position?,
    turn: PieceColor,
    isFlipped: Boolean,
    onSquareClick: (Position) -> Unit
) {
    val isKingInCheck = board.isKingInCheck(turn)
    Column(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .graphicsLayer { if (isFlipped) rotationZ = 180f }
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
                            .clickable {
                                onSquareClick(Position(i, j))
                            }
                            .then(
                                if (isSelected)
                                    Modifier.border(2.dp, Color.Yellow)
                                else if (isKingInCheckSquare)
                                    Modifier.border(2.dp, Color.Red)
                                else Modifier
                            )
                    ) {
                        piece?.let {
                            ChessPiece(it, isFlipped = isFlipped)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChessPiece(piece: Piece, size: Dp? = null, isFlipped: Boolean = false) {
    Image(
        painterResource(id = piece.type.resID),
        "${piece.color} ${piece.type}",
        (if (size != null) Modifier.size(size) else Modifier.fillMaxSize())
            .graphicsLayer { if (isFlipped) rotationZ = 180f },
        colorFilter = ColorFilter.tint(if (piece.color == PieceColor.WHITE) Color.White else Color.Black)
    )
}