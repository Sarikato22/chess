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
    /** Helper method for pieceMoves method
     * addSlidingMoves works with the pieces whose movements can go as far as possible like rook, bishop and queen **/
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
    private void addStepMoves(ChessBoard board, ChessPosition from, Collection<ChessMove> moves, int[][] directions){
        for (int[] dir: directions) {
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

    public void pawnMoves(ChessBoard board, ChessPosition from, Collection<ChessMove> moves, int[][] directions){
        int direction;
        int startRow;
        int promotionRow;

        if (this.getTeamColor() == ChessGame.TeamColor.WHITE){
            direction = 1;
            startRow = 2;
            promotionRow = 8;
        } else{
            direction = -1;
            startRow = 7;
            promotionRow = 1;
        }

        int col = from.getColumn();
        int row = from.getRow();

        //double step
        if (row == startRow){
          int twoStep = row + 2* direction;
          if (board.inBounds(twoStep, col) && board.getPiece(new ChessPosition(twoStep,col)) == null){
              moves.add(new ChessMove(from, new ChessPosition(twoStep,col),null));

          }
        } else if (row == promotionRow) {
            //call promotion method

        } else {
            int oneStep = row + direction;

            if(board.inBounds(oneStep,col) && board.getPiece(new ChessPosition(oneStep, col)) == null) {
                moves.add(new ChessMove(from, new ChessPosition(oneStep, col), null));
            }
        }

    }//end of pawn moves
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
                addSlidingMoves(board, myPosition, moves, directions);
                break;
            }
            case BISHOP: {
                int[][] directions = { {1,1}, {1,-1}, {-1,1}, {-1,-1} };
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
                        {-2, -1}, {-2, 1},   // up 2, left/right 1
                        {-1, -2}, {-1, 2},   // up 1, left/right 2
                        {1, -2},  {1, 2},    // down 1, left/right 2
                        {2, -1},  {2, 1}     // down 2, left/right 1
                };
                addStepMoves(board, myPosition, moves, directions);
                break;
            }
            case KING: {
                int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1,1}, {1,-1}, {-1,1}, {-1,-1}};
                addStepMoves(board, myPosition, moves, directions);
                break;
            }
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

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
    }
}
