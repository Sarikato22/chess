package services;

import dataaccess.DataAccess;
import chess.model.result.ClearResult;

public class ClearService {

    private final DataAccess dao;

    public ClearService(DataAccess dao) {
        this.dao = dao;
    }

    public ClearResult clear() {
        try {
            dao.clear();
            return new ClearResult(true, null);
        } catch (Exception e) {
            return new ClearResult(false, "Error: " + e.getMessage());
        }
    }
}
