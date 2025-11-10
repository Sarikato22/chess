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
        ClientError
    }

    private final Code code;

    // --- Constructors ---
    public ResponseException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public ResponseException(String message) {
        this(Code.ClientError, message);
    }

    public ResponseException(Code code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Code code() {
        return code;
    }

    // --- JSON serialization ---
    public String toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("message", getMessage());
        map.put("status", code.name());
        return new Gson().toJson(map);
    }

    public static ResponseException fromJson(String json) {
        var map = new Gson().fromJson(json, HashMap.class);
        var code = Code.valueOf(map.get("status").toString());
        var message = map.get("message").toString();
        return new ResponseException(code, message);
    }

    // --- HTTP status mapping ---
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
        };
    }

    @Override
    public String toString() {
        return "ResponseException{" +
                "code=" + code +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
