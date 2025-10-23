package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a chess piece with type and color.
 * Can calculate valid moves for itself on a given board.
 */
public class ChessPiece {
    private final TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    public TeamColor getTeamColor() {
        return this.pieceColor;
    }

    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Adds diagonal capture moves for pawns
     */
    private void pawnCaptures(ChessBoard board, ChessPosition from, Collection<ChessMove> moves) {
        int row = from.getRow();
        int col = from.getCol();
        int direction = (pieceColor == TeamColor.WHITE) ? 1 : -1;
        int promotionRow = (pieceColor == TeamColor.WHITE) ? 8 : 1;

        // diagonal capture
        int[] cols = {col - 1, col + 1};
        int diagonalRow = row + direction;

        for (int i : cols) {
            if (!board.inBounds(diagonalRow, i)) {
                continue;
            }
            ChessPosition to = new ChessPosition(diagonalRow, i);
            ChessPiece occupant = board.getPiece(to);

            // checking if they are the enemy
            if (occupant != null && occupant.getTeamColor() != this.getTeamColor()) {
                if (diagonalRow == promotionRow) {
                    addPromotions(moves, from, to);
                } else {
                    moves.add(new ChessMove(from, to, null));
                }
            }
        }
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();

        switch (this.type) {
            case ROOK: {
                int[][] directions = {
                        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
                };
                addSlidingMoves(board, myPosition, moves, directions);
                break;
            }
            case BISHOP: {
                int[][] directions = {
                        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
                };
                addSlidingMoves(board, myPosition, moves, directions);
                break;
            }
            case QUEEN: {
                int[][] directions = {
                        {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
                };
                addSlidingMoves(board, myPosition, moves, directions);
                break;
            }
            case KNIGHT: {
                int[][] directions = {
                        {-2, -1}, {-2, 1},
                        {-1, -2}, {-1, 2},
                        {1, -2}, {1, 2},
                        {2, -1}, {2, 1}
                };
                addStepMoves(board, myPosition, moves, directions);
                break;
            }
            case KING: {
                int[][] directions = {
                        {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
                };
                addStepMoves(board, myPosition, moves, directions);
                break;
            }
            case PAWN:
                pawnMoves(board, myPosition, moves);
                pawnCaptures(board, myPosition, moves);
                break;
        }

        return moves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * The two teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }
}
