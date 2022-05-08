package com.jp2p.database;

import com.jp2p.configuration.JsonReader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class is used to create a connection to the database.
 */
public class DatabaseConnection {
    /**
     * The connection string to the database. Loaded from the seed file.
     */
    private static final String DATABASE_URL = JsonReader.GetNode("database", "connection_string");
    private static Connection connection = null;

    /**
     * This method is used to create a connection to the database.
     * @return The connection to the database.
     * @throws SQLException If there is an error connecting to the database.
     */
    public static Connection connect() throws SQLException {
        connection = DriverManager.getConnection(DATABASE_URL);
        System.out.println("Connected to the database...");
        return connection;
    }

    /**
     * This method is used to close the connection to the database.
     * @throws SQLException If there is an error closing the connection.
     */
    public static void close() throws SQLException {
        connection.close();
        System.out.println("Disconnected from database...");
    }

    /**
     * This method is used to get the connection to the database, tries to connect if not already connected.
     * @return The connection to the database.
     * @throws SQLException If there is an error connecting to the database.
     */
    public static Connection getConnection() throws SQLException {
        return connection == null ? connect() : connection;
    }
}
