package chess.server;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an exception that can be serialized into a JSON response.
 * Useful for returning structured error responses from handlers or services.
 */
public class ResponseException extends Exception {

    public enum Code {
        ServerError,
        ClientError, Unauthorized, Other,
    }

    final private Code code;

    public ResponseException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public String toJson() {
        return new Gson().toJson(Map.of("message", getMessage(), "status", code));
    }
    
    public static ResponseException fromJson(String json) {
        try {
            Gson gson = new Gson();
            Map<?, ?> map = gson.fromJson(json, Map.class);

            if (map == null) {
                return new ResponseException(Code.Other, "Error: empty or invalid response body");
            }

            Object msgObj = map.get("message");
            String message = msgObj != null ? msgObj.toString() : "Unknown error";

            // Try to infer the code based on the message or default to ClientError
            Code code = Code.ClientError;
            if (message.toLowerCase().contains("unauthorized")) {
                code = Code.Unauthorized;
            } else if (message.toLowerCase().contains("internal")) {
                code = Code.ServerError;
            }

            return new ResponseException(code, message);

        } catch (Exception e) {
            System.err.println("Error parsing ResponseException JSON: " + e.getMessage());
            return new ResponseException(Code.Other, "Failed to parse error JSON: " + e.getMessage());
        }
    }


    public Code code() {
        return code;
    }

    public static Code fromHttpStatusCode(int httpStatusCode) {
        return switch (httpStatusCode) {
            case 500 -> Code.ServerError;
            case 400 -> Code.ClientError;
            default -> throw new IllegalArgumentException("Unknown HTTP status code: " + httpStatusCode);
        };
    }

    public int toHttpStatusCode() {
        return switch (code) {
            case ServerError -> 500;
            case ClientError -> 400;
            case Unauthorized -> 0;
            case Other -> 0;
        };
    }
}