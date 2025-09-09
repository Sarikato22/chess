package chess;

public class ChessMoveBuilder {
    private ChessPosition startPosition;
    private ChessPosition endPosition;
    private ChessPiece.PieceType promotionPiece;

    public ChessMoveBuilder setStartPosition(ChessPosition startPosition) {
        this.startPosition = startPosition;
        return this;
    }

    public ChessMoveBuilder setEndPosition(ChessPosition endPosition) {
        this.endPosition = endPosition;
        return this;
    }

    public ChessMoveBuilder setPromotionPiece(ChessPiece.PieceType promotionPiece) {
        this.promotionPiece = promotionPiece;
        return this;
    }

    public ChessMove createChessMove() {
        return new ChessMove(startPosition, endPosition, promotionPiece);
    }
}