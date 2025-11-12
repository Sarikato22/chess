package ui;

import java.util.*;
import com.google.gson.Gson;
import chess.model.result.*;
import chess.server.ServerFacade;

public class ChessClient {

    private String username = null;
    private String authToken = null;
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;

    private Map<Integer, GameResult> lastListedGames = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);
    private final Gson gson = new Gson();

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("♕ Welcome to Chess Client");
        System.out.print(help());

        String input;
        while (true) {
            System.out.print("\n>>> ");
            input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("quit")) break;

            try {
//                System.out.print(eval(input));
                System.out.println("To be made");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");


    }

    private String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    Available commands:
                    - help
                    - quit
                    - login <username> <password>
                    - register <username> <password> <email>
                    """;
        } else {
            return """
                    Available commands:
                    - help
                    - logout
                    - createGame <gameName>
                    - listGames
                    - playGame <number> <WHITE|BLACK>
                    - observeGame <number>
                    - quit
                    """;
        }
    }


}//end of class

