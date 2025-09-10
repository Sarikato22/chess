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
    private void addDirectionalMoves(ChessBoard board, ChessPosition from,
                                     Collection<ChessMove> moves, int[][] directions) {
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
                    break; // stop in this direction
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
                int[][] directions = { {1,0}, {-1,0}, {0,1}, {0,-1} };
                addDirectionalMoves(board, myPosition, moves, directions);
                break;
            }
            case BISHOP: {
                int[][] directions = { {1,1}, {1,-1}, {-1,1}, {-1,-1} };
                addDirectionalMoves(board, myPosition, moves, directions);
                break;
            }
            case QUEEN:
                // generate queen moves
                break;
            case KNIGHT:
                // generate knight moves
                break;
            case KING:
                // generate king moves
                break;
            case PAWN:
                // generate pawn moves (trickier: forward, capture, promotion)
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
}
