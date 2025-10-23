package passoff.chess;

import chess.ChessMove;
import chess.ChessMoveBuilder;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Collection;
import java.util.List;


public class ChessMoveTests extends EqualsTestingUtility<ChessMove> {
    public ChessMoveTests() {
        super("ChessMove", "moves");
    }

    @Override
    protected ChessMove buildOriginal() {
        return new ChessMoveBuilder().setStartPosition(new ChessPosition(2, 6)).setEndPosition(new ChessPosition(1,
                5)).setPromotionPiece(null).createChessMove();
    }

    @Override
    protected Collection<ChessMove> buildAllDifferent() {
        return List.of(
                new ChessMoveBuilder().setStartPosition(new ChessPosition(1, 5)).setEndPosition(new ChessPosition(2,
                        6)).setPromotionPiece(null).createChessMove(),
                new ChessMoveBuilder().setStartPosition(new ChessPosition(2, 4)).setEndPosition(new ChessPosition(1,
                        5)).setPromotionPiece(null).createChessMove(),
                new ChessMoveBuilder().setStartPosition(new ChessPosition(2, 6)).setEndPosition(new ChessPosition(5,
                        3)).setPromotionPiece(null).createChessMove(),
                new ChessMoveBuilder().setStartPosition(new ChessPosition(2, 6)).setEndPosition(new ChessPosition(1,
                        5)).setPromotionPiece(ChessPiece.PieceType.QUEEN).createChessMove(),
                new ChessMoveBuilder().setStartPosition(new ChessPosition(2, 6)).setEndPosition(new ChessPosition(1,
                        5)).setPromotionPiece(ChessPiece.PieceType.ROOK).createChessMove(),
                new ChessMoveBuilder().setStartPosition(new ChessPosition(2, 6)).setEndPosition(new ChessPosition(1,
                        5)).setPromotionPiece(ChessPiece.PieceType.BISHOP).createChessMove(),
                new ChessMoveBuilder().setStartPosition(new ChessPosition(2, 6)).setEndPosition(new ChessPosition(1,
                        5)).setPromotionPiece(ChessPiece.PieceType.KNIGHT).createChessMove()
        );
    }

}
