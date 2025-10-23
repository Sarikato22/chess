package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    ChessGame.TeamColor teamTurn;
    ChessBoard board;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.teamTurn = TeamColor.WHITE; // white ALWAYS starts first
    }

    public TeamColor getTeamTurn() {
        return this.teamTurn;
    }

    public void setTeamTurn(TeamColor team) {
        if (team == null) throw new IllegalArgumentException("Team cannot be null.");
        this.teamTurn = team;
    }

    public void switchTurns() {
        this.teamTurn = (this.teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    private ChessPosition findKing(ChessBoard board, TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition current = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(current);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING
                        && piece.getTeamColor() == teamColor) {
                    return current;
                }
            }
        }
        return null;
    }

    private boolean wouldBeInCheck(ChessBoard board, TeamColor teamColor) {
        ChessPosition kingPos = findKing(board, teamColor);
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    if (anyMoveAttacksPosition(piece, board, pos, kingPos)) return true;
                }
            }
        }
        return false;
    }

    private boolean anyMoveAttacksPosition(ChessPiece piece, ChessBoard board, ChessPosition from, ChessPosition target) {
        for (ChessMove move : piece.pieceMoves(board, from)) {
            if (move.getEndPosition().equals(target)) return true;
        }
        return false;
    }

    private ChessBoard copyBoard(ChessBoard original) {
        ChessBoard copy = new ChessBoard();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = original.getPiece(pos);
                if (piece != null) copy.addPiece(pos, new ChessPiece(piece.getTeamColor(), piece.getPieceType()));
            }
        }
        return copy;
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> legalMoves = new HashSet<>();
        ChessPiece piece = getBoard().getPiece(startPosition);
        if (piece == null) return legalMoves;

        for (ChessMove move : piece.pieceMoves(getBoard(), startPosition)) {
            ChessBoard boardCopy = copyBoard(getBoard());
            applyMoveToBoardCopy(boardCopy, move, piece);
            if (!wouldBeInCheck(boardCopy, piece.getTeamColor())) legalMoves.add(move);
        }
        return legalMoves;
    }

    private void applyMoveToBoardCopy(ChessBoard boardCopy, ChessMove move, ChessPiece piece) {
        if (move.getPromotionPiece() != null) {
            boardCopy.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        } else {
            boardCopy.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), piece.getPieceType()));
        }
        boardCopy.addPiece(move.getStartPosition(), null);
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPiece piece = board.getPiece(start);
        if (piece == null) throw new InvalidMoveException("No piece found");
        if (piece.getTeamColor() != getTeamTurn()) throw new InvalidMoveException("Not your turn");

        if (validMoves(start).contains(move)) {
            applyMoveToBoard(board, move, piece);
            switchTurns();
        } else throw new InvalidMoveException("Move is invalid.");
    }

    private void applyMoveToBoard(ChessBoard board, ChessMove move, ChessPiece piece) {
        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        } else {
            board.addPiece(move.getEndPosition(), piece);
        }
        board.addPiece(move.getStartPosition(), null);
    }

    private boolean hasLegalMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = this.board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor && !validMoves(pos).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInCheck(TeamColor teamColor) {
        return wouldBeInCheck(this.board, teamColor);
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && !hasLegalMoves(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && !hasLegalMoves(teamColor);
    }

    public ChessBoard getBoard() {
        return this.board;
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessGame chessGame)) return false;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }

    @Override
    public String toString() {
        return "ChessGame{" + "teamTurn=" + teamTurn + '}';
    }

    public enum TeamColor {
        WHITE, BLACK
    }
}
