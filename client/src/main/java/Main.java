import chess.ChessGame;
import chess.ChessPiece;
import ui.ChessClient;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        System.out.println("Starting Chess Client...");
        var client = new ChessClient("http://localhost:8080");
        client.run(); // This will launch your REPL loop
    }
}