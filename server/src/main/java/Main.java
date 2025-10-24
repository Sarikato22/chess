import chess.ChessGame;
import chess.ChessPiece;
import server.Server;

public class Main {
    public static void main(String[] args) {
        var server = new Server();
        server.run(8080);
        System.out.println("Server running on port 8080");
    }
}