package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor pieceColor;
    private PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {

        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {

        return type;
    }

    /**
     * Helper method for pieceMoves method
     * addSlidingMoves works with the pieces whose movements can go as far as possible like rook, bishop and queen
     **/
    private void addSlidingMoves(ChessBoard board, ChessPosition from, Collection<ChessMove> moves, int[][] directions) {
        for (int[] dir : directions) {
            int row = from.getRow();
            int col = from.getColumn();

            while (true) {
                row += dir[0];
                col += dir[1];

                if (!board.inBounds(row, col)) break;

                ChessPosition to = new ChessPosition(row, col);
                ChessPiece occupant = board.getPiece(to);

                if (occupant == null) {
                    moves.add(new ChessMove(from, to, null));
                } else {
                    if (occupant.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(from, to, null));
                    }
                    break;
                }
            }
        }
    }

    private void addStepMoves(ChessBoard board, ChessPosition from, Collection<ChessMove> moves, int[][] directions) {
        for (int[] dir : directions) {
            int row = from.getRow() + dir[0];
            int col = from.getColumn() + dir[1];

            if (!board.inBounds(row, col)) continue; // skip out-of-bounds

            ChessPosition to = new ChessPosition(row, col);
            ChessPiece occupant = board.getPiece(to);

            if (occupant == null) {
                moves.add(new ChessMove(from, to, null));
            } else if (occupant.getTeamColor() != this.getTeamColor()) {
                moves.add(new ChessMove(from, to, null));
            }
        }

    }

    private void addPromotions(Collection<ChessMove> moves, ChessPosition from, ChessPosition to) {
        moves.add(new ChessMove(from, to, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(from, to, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(from, to, ChessPiece.PieceType.BISHOP));
        moves.add(new ChessMove(from, to, ChessPiece.PieceType.KNIGHT));
    }

    public void pawnMoves(ChessBoard board, ChessPosition from, Collection<ChessMove> moves) {
        int direction;
        int startRow;
        int promotionRow;

        if (this.getTeamColor() == ChessGame.TeamColor.WHITE) {
            direction = 1;
            startRow = 2;
            promotionRow = 8;
        } else {
            direction = -1;
            startRow = 7;
            promotionRow = 1;
        }

        int col = from.getColumn();
        int row = from.getRow();

        //one step
        int oneStep = row + direction;
        if (board.inBounds(oneStep, col) && board.getPiece(new ChessPosition(oneStep, col)) == null) {
            ChessPosition onePos = new ChessPosition(oneStep, col);

            if (oneStep == promotionRow) {
                addPromotions(moves, from, onePos);
            } else {
                moves.add(new ChessMove(from, onePos, null));
                //two steps
                if (row == startRow) {
                    int twoStep = row + 2 * direction;
                    if (board.inBounds(twoStep, col) && board.getPiece(new ChessPosition(twoStep, col)) == null) {
                        moves.add(new ChessMove(from, new ChessPosition(twoStep, col), null));
                    }
                }
            }
        }
    }

    private void pawnCaptures(ChessBoard board, ChessPosition from, Collection<ChessMove> moves) {
        int direction;
        int startRow;
        int promotionRow;

        if (this.getTeamColor() == ChessGame.TeamColor.WHITE) {
            direction = 1;
            startRow = 2;
            promotionRow = 8;
        } else {
            direction = -1;
            startRow = 7;
            promotionRow = 1;
        }

        int col = from.getColumn();
        int row = from.getRow();

        //diagonal capture
        int cols[] = {col - 1, col + 1};
        int diagonalRow = row + direction;

        for (int i : cols) {
            if (!board.inBounds(diagonalRow, i)) continue;
            ChessPosition to = new ChessPosition(diagonalRow, i);
            ChessPiece occupant = board.getPiece(to);

            //checking if they are the enemy
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
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();

        switch (this.type) {
            case ROOK: {
                int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                addSlidingMoves(board, myPosition, moves, directions);
                break;
            }
            case BISHOP: {
                int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
                addSlidingMoves(board, myPosition, moves, directions);
                break;
            }
            case QUEEN: {
                int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
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
                int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
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
}
