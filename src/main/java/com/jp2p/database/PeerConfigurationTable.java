package com.jp2p.database;

import com.jp2p.configuration.JsonReader;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class allows creating and interacting with the PEER_CONFIGURATION table.
 * The table is seeded using the seed.json file.
 */
public class PeerConfigurationTable {
    /**
     * This method is used to crate the PEER_CONFIGURATION table if it does not exist.
     *
     * @throws SQLException if the table could not be created.
     */
    public static void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS PEER_CONFIGURATION (" +
                     "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "NAME TEXT," +
                     "VALUE TEXT);";
        Connection connection = DatabaseConnection.getConnection();
        Statement statement = connection.createStatement();
        statement.execute(sql);
    }

    /**
     * This method is used to seed the PEER_CONFIGURATION table with the seed.json file.
     *
     * @throws SQLException if the table could not be seeded.
     */
    public static void seedConfiguration() throws SQLException {
        if (!isTableEmpty()) {
            System.out.println("Configuration table is already seeded, skipping seed...");
            return;
        }

        String sql = String.format("INSERT INTO PEER_CONFIGURATION (NAME, VALUE) " +
                                   "VALUES ('%s', '%s'), ('%s', '%s'), ('%s', '%s');",
                "default_port", JsonReader.GetNode("default_peer", "port"),
                "default_name", JsonReader.GetNode("default_peer", "name"),
                "max_peers", JsonReader.GetNode("default_peer", "max_peers"));
        Connection connection = DatabaseConnection.getConnection();
        Statement statement = connection.createStatement();
        statement.execute(sql);
        System.out.println("Configuration seeded to the database...");
    }

    /**
     * This method is used to get the value of a configuration parameter from the database given its name.
     *
     * @param name the name of the configuration parameter.
     * @return the value of the configuration parameter.
     * @throws SQLException if the value could not be retrieved.
     */
    public static String getConfiguration(String name) throws SQLException {
        String sql = String.format("SELECT VALUE FROM PEER_CONFIGURATION WHERE NAME = '%s';", name);
        Connection connection = DatabaseConnection.getConnection();
        Statement statement = connection.createStatement();
        return statement.executeQuery(sql).getString(1);
    }

    /**
     * This method is used to check if the PEER_CONFIGURATION table is empty.
     *
     * @return true if the table is empty, false otherwise.
     * @throws SQLException if the table could not be checked.
     */
    private static boolean isTableEmpty() throws SQLException {
        String sql = "SELECT COUNT(*) FROM PEER_CONFIGURATION;";
        Connection connection = DatabaseConnection.getConnection();
        Statement statement = connection.createStatement();
        int count = statement.executeQuery(sql).getInt(1);
        return count <= 0;
    }
}
