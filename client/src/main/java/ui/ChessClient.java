package ui;

import java.util.*;

import chess.ChessBoard;
import chess.ChessGame;
import chess.model.data.GameData;
import chess.model.request.GameRequest;
import chess.model.request.JoinGameRequest;
import chess.model.request.RegisterRequest;
import chess.model.request.SessionRequest;
import chess.server.ResponseException;
import com.google.gson.Gson;
import chess.model.result.*;
import chess.server.ServerFacade;

public class ChessClient implements ServerMessageObserver {

    private String username = null;
    private String authToken = null;
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;

    private Map<Integer, GameData> lastListedGames = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);
    private final Gson gson = new Gson();

    private ChessBoard board = new ChessBoard();
    private ChessBoardRenderer renderer;
    private WebSocketCommunicator ws;
    private Integer currentGameId = null;
    private final String serverUrl;
    private boolean inGame = false;


    private ChessGame currentGame = null;
    private ChessGame.TeamColor currentColor = null;


    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl;
        server = new ServerFacade(serverUrl);
        board.resetBoard();
        renderer = new ChessBoardRenderer();
    }

    public void run() {
        System.out.println("♕ Welcome to Chess Client");
        System.out.print(help());

        String input;
        while (true) {
            System.out.print("\n>>> ");
            input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("quit")) {
                break;
            }
            try {
                System.out.print(eval(input));
            } catch (Exception e) {
                System.out.println(e.getMessage());
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
        if (params.length < 2 || params.length > 2) {
            return "Usage: login <username> <password>\n";
        }
        SessionRequest req = new SessionRequest(params[0], params[1]);
        SessionResult result = server.login(req);

        this.username = result.getUsername();
        this.authToken = result.getAuthToken();
        this.state = State.SIGNEDIN;

        return String.format("Logged in as %s.\n", username);
    }

    private String register(String... params) throws Exception {
        if (params.length < 3 || params.length > 3 ) {
            return "Usage: register <username> <password> <email>\n";
        }
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

        if (params.length < 1 || params.length > 1) {
            return "Usage: createGame <gameName>\n";
        }
        String gameName = params[0];
        GameRequest req = new GameRequest(gameName);
        GameResult result = server.createGame(req, authToken);
        return String.format("Game '%s' created with ID %d.\n", gameName, result.getGameID());
    }
    //listGames
    private String listGames() throws Exception {
        GameListResult result = server.listGames(authToken);
        if (!result.isSuccess()) {
            return "Failed to list games: " + result.getMessage() + "\n";
        }

        List<GameData> games = result.getGames();

        lastListedGames.clear();
        int i = 1;
        for (GameData game : games) {
            System.out.printf("%d. %s (White: %s, Black: %s)%n",
                    i, game.getGameName(),
                    game.getWhiteUsername(),
                    game.getBlackUsername());
            lastListedGames.put(i, game);
            i++;
        }

        return String.format("Listed %d games successfully.\n", games.size());
    }
    private void refreshGameListSilently() {
        try {
            GameListResult result = server.listGames(authToken); // your existing listGames
            lastListedGames.clear();
            int index = 1;
            for (GameData game : result.getGames()) {
                lastListedGames.put(index++, game);
            }
        } catch (ResponseException e) {

        }
    }

    //playGame
    private String playGame(String... params) throws Exception {
        refreshGameListSilently();
        if (params.length != 2) {
            return "Usage: playGame <number> <WHITE|BLACK>\n";
        }

        int num;
        try {
            num = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return "Invalid game number.\n";
        }

        if (!lastListedGames.containsKey(num)) {
            return "Game number not found. Please check available games.\n";
        }

        ChessGame.TeamColor color;
        try {
            color = ChessGame.TeamColor.valueOf(params[1].toUpperCase());
            inGame = true;
            currentColor = color;
        } catch (IllegalArgumentException e) {
            return "Invalid color. Use WHITE or BLACK.\n";
        }


        GameData gameData = lastListedGames.get(num);
        JoinGameRequest req = new JoinGameRequest(color, gameData.getGameId());
        JoinGameResult joinResult = server.joinGame(authToken, req);

        if (!joinResult.isSuccess()) {
            return "Failed to join game: " + joinResult.getMessage() + "\n";
        }

        currentGameId = gameData.getGameId();

        ws = new WebSocketCommunicator(this, serverUrl);
        ws.sendConnect(authToken, currentGameId);

        return String.format("Joined game %s as %s. Waiting for board...\n",
                gameData.getGameName(), color);
    }

    //Observe game
    private String observeGame(String... params) throws Exception {
        refreshGameListSilently();
        if (params.length != 1) {
            return "Usage: observeGame <number>\n";
        }
        int num;
        try {
            num = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return "Invalid game number.\n";
        }

        if (!lastListedGames.containsKey(num)) {
            return "Game number not found. List games first.\n";
        }

        GameData gameData = lastListedGames.get(num);

        currentGameId = gameData.getGameId();
        inGame = true;
        currentColor = null;

        ws = new WebSocketCommunicator(this, serverUrl);
        ws.sendConnect(authToken, currentGameId);

        return String.format("Observing game %s. Waiting for board...\n", gameData.getGameName());
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

        if (inGame) {
            return switch (cmd) {
                case "help"     -> inGameHelp();
                case "move"     -> inGameMove(params);
                case "leave"    -> inGameLeave();
                case "resign"   -> inGameResign();
                case "redraw"   -> inGameRedraw();
                case "highlight"-> inGameHighlight(params);
                case "quit"     -> "quit";
                default         -> "Unknown command. Type 'help'.\n";
            };
        } else if (state == State.SIGNEDOUT) {
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
                case "listgames" -> listGames();
                case "playgame" -> playGame(params);
                case "observegame" -> observeGame(params);
                case "quit" -> "quit";
                default -> "Unknown command. Type 'help'.\n";
            };
        }
    }


    @Override
    public void notify(websocket.messages.ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                var load = (websocket.messages.LoadGameMessage) message;
                this.currentGame = load.getGame();

                ChessGame.TeamColor perspective =
                        (currentColor != null ? currentColor : ChessGame.TeamColor.WHITE);

                if (currentGame != null) {
                    renderer.drawBoard(currentGame.getBoard(), perspective);
                }
            }
            case NOTIFICATION -> {
                var note = (websocket.messages.NotificationMessage) message;
                System.out.println("\n[Notification] " + note.getMessage());
            }
            case ERROR -> {
                var err = (websocket.messages.ErrorMessage) message;
                System.out.println("\n[Error] " + err.getErrorMessage());
            }
        }
    }
    private String inGameHelp() {
        return """
            Gameplay commands:
            - help
            - move <from> <to>      (e.g., move e2 e4)
            - highlight <square>    (e.g., highlight e2)
            - redraw
            - leave
            - resign
            """;
    }

    private String inGameMove(String... params) throws Exception {
        // TODO: parse params into a ChessMove and call ws.sendMakeMove(...)
        return "";
    }

    private String inGameLeave() throws Exception {
        // TODO: ws.sendLeave(...), close ws, set inGame=false
        return "";
    }

    private String inGameResign() throws Exception {
        // TODO: ws.sendResign(...)
        return "";
    }

    private String inGameRedraw() {
        // TODO: if currentGame != null, renderer.drawBoard(...)
        return "";
    }

    private String inGameHighlight(String... params) {
        // TODO later: compute validMoves on currentGame
        return "";
    }


}//end of class

