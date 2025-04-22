package com.boardmanager;

import java.sql.SQLException;

import com.boardmanager.persistence.migration.MigrationStrategy;
import com.boardmanager.ui.MainMenu;

import static com.boardmanager.persistence.config.ConnectionConfig.getConnection;

public class Main {

    public static void main(String[] args) throws SQLException {
        try(var connection = getConnection()){
            new MigrationStrategy(connection).executeMigration();
        }
        new MainMenu().execute();
    }

}