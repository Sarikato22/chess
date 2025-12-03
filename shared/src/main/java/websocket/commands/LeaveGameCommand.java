package websocket.commands;
    public class LeaveGameCommand extends UserGameCommand {
        public LeaveGameCommand(String authToken, int gameID) {
            super(CommandType.LEAVE, authToken, gameID);
        }
    }


