package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class ChessBoardRenderer {

    // Tile colors
    private static final String BLACK_TILE = "#023047";
    private static final String WHITE_TILE = "#8ECAE6";
    private static final String MARGIN_COLOR = "#219EBC";
    private static final String WHITE_PIECE = "#E98A15";
    private static final String BLACK_PIECE = "#6B2737";

    private static final String[] LETTERS = {"a","b","c","d","e","f","g","h"};

    public void drawBoard(ChessBoard board, ChessGame.TeamColor perspective) {
        int startCol = perspective == ChessGame.TeamColor.WHITE ? 1 : 8;
        int endCol = perspective == ChessGame.TeamColor.WHITE ? 8 : 1;
        int colStep = perspective == ChessGame.TeamColor.WHITE ? 1 : -1;

        int startRow = perspective == ChessGame.TeamColor.WHITE ? 8 : 1;
        int endRow = perspective == ChessGame.TeamColor.WHITE ? 1 : 8;
        int rowStep = perspective == ChessGame.TeamColor.WHITE ? -1 : 1;

        // Top margin with corners
        System.out.print(colorText("  ", "#000000", MARGIN_COLOR));
        for (int c = startCol; perspective == ChessGame.TeamColor.WHITE ? c <= endCol : c >= endCol; c += colStep) {
            System.out.print(colorText(" " + LETTERS[c - 1] + " ", "#000000", MARGIN_COLOR));
        }
        System.out.println(colorText("  ", "#000000", MARGIN_COLOR));

        // Rows
        for (int r = startRow; perspective == ChessGame.TeamColor.WHITE ? r >= endRow : r <= endRow; r += rowStep) {
            // Left margin
            System.out.print(colorText(String.format("%d ", r), "#000000", MARGIN_COLOR));

            for (int c = startCol; perspective == ChessGame.TeamColor.WHITE ? c <= endCol : c >= endCol; c += colStep) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece piece = board.getPiece(pos);

                boolean isBlackTile = (r + c) % 2 == 0;
                String tileColor = isBlackTile ? BLACK_TILE : WHITE_TILE;

                String symbol = piece == null ? " " : getPieceLetter(piece);
                String pieceColor = piece == null ? tileColor : (piece.getTeamColor() == ChessGame.TeamColor.BLACK ? BLACK_PIECE : WHITE_PIECE);

                System.out.print(colorTextBold(" " + symbol + " ", pieceColor, tileColor));
            }

            // Right margin
            System.out.println(colorText(" ", "#000000", MARGIN_COLOR) + colorText(String.format("%d", r), "#000000", MARGIN_COLOR));
        }

        // Bottom margin with corners
        System.out.print(colorText("  ", "#000000", MARGIN_COLOR));
        for (int c = startCol; perspective == ChessGame.TeamColor.WHITE ? c <= endCol : c >= endCol; c += colStep) {
            System.out.print(colorText(" " + LETTERS[c - 1] + " ", "#000000", MARGIN_COLOR));
        }
        System.out.println(colorText("  ", "#000000", MARGIN_COLOR));
    }

    // Piece letters
    private String getPieceLetter(ChessPiece piece) {
        String letter;
        switch (piece.getPieceType()) {
            case PAWN -> letter = "P";
            case ROOK -> letter = "R";
            case KNIGHT -> letter = "N";
            case BISHOP -> letter = "B";
            case QUEEN -> letter = "Q";
            case KING -> letter = "K";
            default -> letter = "?";
        }
        if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            letter = letter.toLowerCase();
        }
        return letter;
    }

    private String colorText(String text, String fgHex, String bgHex) {
        int[] fg = hexToRgbArray(fgHex);
        int[] bg = hexToRgbArray(bgHex);
        return "\u001B[38;2;" + fg[0] + ";" + fg[1] + ";" + fg[2] + "m"
                + "\u001B[48;2;" + bg[0] + ";" + bg[1] + ";" + bg[2] + "m"
                + text + "\u001B[0m";
    }

    private String colorTextBold(String text, String fgHex, String bgHex) {
        return "\u001B[1m" + colorText(text, fgHex, bgHex) + "\u001B[0m";
    }
    private int[] hexToRgbArray(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new int[]{r, g, b};
    }
}

