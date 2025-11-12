package ui;

import java.util.*;

import chess.model.request.GameRequest;
import chess.model.request.RegisterRequest;
import chess.model.request.SessionRequest;
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
                System.out.print(eval(input));
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

//Prelogin
    private String login(String... params) throws Exception {
        if (params.length < 2) return "Usage: login <username> <password>\n";

        SessionRequest req = new SessionRequest(params[0], params[1]);
        SessionResult result = server.login(req);

        this.username = result.getUsername();
        this.authToken = result.getAuthToken();
        this.state = State.SIGNEDIN;

        return String.format("Logged in as %s.\n", username);
    }

    private String register(String... params) throws Exception {
        if (params.length < 3) return "Usage: register <username> <password> <email>\n";

        RegisterRequest req = new RegisterRequest(params[0], params[1], params[2]);
        RegisterResult result = server.register(req);

        this.username = result.getUsername();
        this.authToken = result.getAuthToken();
        this.state = State.SIGNEDIN;

        return String.format("Registered and logged in as %s.\n", username);
    }
    //postlogin
    private String createGame(String... params) throws Exception {
        if (state != State.SIGNEDIN) {
            return "You must be signed in to create a game.\n";
        }

        if (params.length < 1) return "Usage: createGame <gameName>\n";

        String gameName = params[0];
        GameRequest req = new GameRequest(gameName);

        // Debug: print what is being sent
        System.out.println("DEBUG: Creating game with name: " + gameName);
        System.out.println("DEBUG: Using authToken: " + authToken);

        GameResult result = server.createGame(req, authToken);

        // Debug: print what we received
        System.out.println("DEBUG: Received game ID: " + result.getGameID());

        return String.format("Game '%s' created with ID %d.\n", gameName, result.getGameID());
    }

    private String logout() throws Exception {
        server.logout(authToken);
        username = null;
        authToken = null;
        state = State.SIGNEDOUT;
        lastListedGames.clear();
        return "Logged out.\n";
    }

    //eval

    private String eval(String input) throws Exception {
        String[] tokens = input.split(" ");
        String cmd = tokens.length > 0 ? tokens[0].toLowerCase() : "";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

        if (state == State.SIGNEDOUT) {
            return switch (cmd) {
                case "help" -> help();
                case "login" -> login(params);
                case "register" -> register(params);
                case "quit" -> "quit";
                default -> "Unknown command. Type 'help'.\n";
            };
        } else {
            return switch (cmd) {
                case "help" -> help();
                case "logout" -> logout();
                case "creategame" -> createGame(params);
//                case "listgames" -> listGames();
//                case "playgame" -> playGame(params);
//                case "observegame" -> observeGame(params);
                case "quit" -> "quit";
                default -> "Unknown command. Type 'help'.\n";
            };
        }
    }

}//end of class

