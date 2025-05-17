package org.projeti.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private final String USER = "root";
    private final String PWD = "";
    private final String URL = "jdbc:mysql://localhost:3306/databaseweb";

    private static Database instance;
    private Connection cnx;

    private Database() {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Establish the connection
            cnx = DriverManager.getConnection(URL, USER, PWD);
            System.out.println("✅ Connexion établie à la base de données.");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé : " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Échec de connexion à la base de données : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Database getInstance() {
        if (instance == null) {
            synchronized (Database.class) {
                if (instance == null) {
                    instance = new Database();
                }
            }
        }
        return instance;
    }

    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                // Try to reconnect if the connection is null or closed
                cnx = DriverManager.getConnection(URL, USER, PWD);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de la connexion : " + e.getMessage());
            e.printStackTrace();
        }
        return cnx;
    }
}

