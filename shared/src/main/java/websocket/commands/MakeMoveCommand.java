package websocket.commands;

    public class MakeMoveCommand extends UserGameCommand {
        private final chess.ChessMove move;

        public MakeMoveCommand(String authToken, int gameID, chess.ChessMove move) {
            super(CommandType.MAKE_MOVE, authToken, gameID);
            this.move = move;
        }

        public chess.ChessMove getMove() {
            return move;
        }
    }
