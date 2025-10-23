package chess;

import java.util.Collection;
import java.util.Collections;
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

    /**
     * Constructs a new ChessGame with a fresh board and sets turn to white.
     */
    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.teamTurn = TeamColor.WHITE; // white ALWAYS starts first
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.teamTurn;
    }

    /**
     * Set's which team's turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        if (team == null) {
            throw new IllegalArgumentException("Team cannot be null.");
        }
        this.teamTurn = team;
    }

    /**
     * Switches the turn to the opposing team.
     */
    public void switchTurns() {
        if (this.teamTurn == TeamColor.WHITE) {
            this.teamTurn = TeamColor.BLACK;
        } else {
            this.teamTurn = TeamColor.WHITE;
        }
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }


    /**
     * Simulates whether the specified team would be in check after a move on a given board state.
     *
     * @param board      the board state to check
     * @param teamColor  the team to check for check
     * @return true if the team would be in check
     */
    private boolean wouldBeInCheck(ChessBoard board, ChessGame.TeamColor teamColor) {
        ChessPosition kingPos = findKing(board, teamColor);
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece otherPiece = board.getPiece(pos);
                if (otherPiece != null && otherPiece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves = otherPiece.pieceMoves(board, pos);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPos)) {
                            return true; // king is under attack
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Creates a deep copy of the provided chess board.
     *
     * @param original the board to copy
     * @return a new ChessBoard instance with the same piece layout
     */
    private ChessBoard copyBoard(ChessBoard original) {
        ChessBoard copy = new ChessBoard();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = original.getPiece(pos);
                if (piece != null) {
                    copy.addPiece(pos, new ChessPiece(piece.getTeamColor(), piece.getPieceType()));
                }
            }
        }
        return copy;
    }

    /**
     * Gets all valid moves for a piece at the given location. A valid move does not leave the king in check.
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or an empty set if no piece at startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> legalMoves = new HashSet<>();
        ChessPiece piece = getBoard().getPiece(startPosition);
        if (piece == null) return legalMoves;

        Collection<ChessMove> possibleMoves = piece.pieceMoves(getBoard(), startPosition);

        for (ChessMove move : possibleMoves) {
            ChessBoard boardCopy = copyBoard(getBoard());

            // Simulate the move
            if (move.getPromotionPiece() != null) {
                boardCopy.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
                boardCopy.addPiece(startPosition, null);
            } else {
                boardCopy.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), piece.getPieceType()));
                boardCopy.addPiece(startPosition, null);
            }

            // Check if the move would leave the king in check
            if (!wouldBeInCheck(boardCopy, piece.getTeamColor())) {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        if (board.getPiece(startPosition) == null) throw new InvalidMoveException("No piece found");
        ChessPiece piece = board.getPiece(startPosition);

        if (piece.getTeamColor() != getTeamTurn()) throw new InvalidMoveException("Not your turn");

        Collection<ChessMove> legalMoves = validMoves(startPosition);
        if (legalMoves.contains(move)) {
            if (move.getPromotionPiece() != null) {
                board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
                board.addPiece(startPosition, null);
            } else {
                board.addPiece(move.getEndPosition(), piece);
                board.addPiece(startPosition, null);
            }
            switchTurns();
        } else {
            throw new InvalidMoveException("Move is invalid.");
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = findKing(this.board, teamColor);
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece otherPiece = this.board.getPiece(pos);
                if (otherPiece != null && otherPiece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves = otherPiece.pieceMoves(this.board, pos);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPos)) {
                            return true; // king is under attack
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Finds the position of the king for the specified team.
     *
     * @param board     The chess board to search.
     * @param teamColor The color of the king to find.
     * @return The position of the king, or null if not found (should never happen in a valid game).
     */
    public ChessPosition findKing(ChessBoard board, TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition current = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(current);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    return current;
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in checkmate.
     * A team is in checkmate if its king is under attack and it has no legal moves to escape.
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = this.board.getPiece(pos);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> legalMoves = validMoves(pos);
                    if (!legalMoves.isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not being in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = this.board.getPiece(pos);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> legalMoves = validMoves(pos);
                    if (!legalMoves.isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessGame chessGame)) {
            return false;
        }
        return teamTurn == chessGame.teamTurn &&
                Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "teamTurn=" + teamTurn +
                '}';
    }
}
