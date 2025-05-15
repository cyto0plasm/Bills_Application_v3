package Data_Access_Object;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DbConnection {
    private static final String DB_NAME = "database3.db"; // Using your existing database file
    private static final String DB_PATH = getDatabasePath();
    private static Connection connection;

    // Get the SQLite connection
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Register SQLite JDBC driver
                Class.forName("org.sqlite.JDBC");
                
                // Use SQLite JDBC URL
                String url = "jdbc:sqlite:" + DB_PATH;
                //String url = "jdbc:sqlite:D:/NetBeansProjects/Bills/Database.db";
//System.out.println(new java.io.File("your_database_file_path_here :   "+getDatabasePath()).getAbsolutePath());

                connection = DriverManager.getConnection(url);
                
                // Enable foreign key support
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
                
                // Create tables if they don't exist
                createTablesIfNeeded();
                
            } catch (ClassNotFoundException e) {
                System.err.println("SQLite JDBC driver not found: " + e.getMessage());
                throw new SQLException("Database driver not found", e);
            } catch (SQLException e) {
                System.err.println("Error establishing SQLite connection: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    private static String getDatabasePath() {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        return currentPath.resolve(DB_NAME).toString();
    }

    private static void createTablesIfNeeded() {
        String createUserTable = "CREATE TABLE IF NOT EXISTS user ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "phone INTEGER, "
                + "password TEXT NOT NULL, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        String createClientTable = "CREATE TABLE IF NOT EXISTS client ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "phone INTEGER NOT NULL UNIQUE, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        String createInvoiceTable = "CREATE TABLE IF NOT EXISTS invoice ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "product TEXT NOT NULL, "
                + "quantity INTEGER NOT NULL, "
                + "price REAL NOT NULL, "
                + "total REAL NOT NULL, "
                + "income REAL NOT NULL, "
                + "remaining REAL NOT NULL, "
                + "client_id INTEGER NOT NULL, "
                + "payment_method TEXT NOT NULL DEFAULT 'cash' CHECK (payment_method IN ('cash', 'credit card', 'wallet')), "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE CASCADE);";

        String createInvoiceHeaderTable = "CREATE TABLE IF NOT EXISTS invoice_header ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "client_id INTEGER NOT NULL, "
                + "total_amount REAL NOT NULL DEFAULT 0.00, "
                + "income REAL NOT NULL, "
                + "remaining REAL NOT NULL, "
                + "payment_method TEXT NOT NULL DEFAULT 'cash' CHECK (payment_method IN ('cash', 'credit card', 'wallet')), "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE CASCADE);";
                
        String createInvoiceDetailsTable = "CREATE TABLE IF NOT EXISTS invoice_details ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "invoice_id INTEGER NOT NULL, "
                + "product TEXT NOT NULL, "
                + "quantity DECIMAL(10, 2) NOT NULL, "
                + "price DECIMAL(10, 2) NOT NULL, "
                + "total DECIMAL(10, 2), "
                + "payment_method TEXT NOT NULL DEFAULT 'cash' CHECK (payment_method IN ('cash', 'credit card', 'wallet')), "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (invoice_id) REFERENCES invoice_header(id) ON DELETE CASCADE);";

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(createUserTable);
            stmt.execute(createClientTable);
            stmt.execute(createInvoiceTable);
            stmt.execute(createInvoiceHeaderTable);
            stmt.execute(createInvoiceDetailsTable);
            
            try {
                stmt.execute("PRAGMA foreign_keys=off;");
                
                // Check if table exists before trying to modify it
                try {
                    stmt.executeQuery("SELECT COUNT(*) FROM invoice_details");
                    
                    // Check and add payment_method if needed
                    try {
                        stmt.executeQuery("SELECT payment_method FROM invoice_details LIMIT 1");
                    } catch (SQLException e) {
                        stmt.execute("ALTER TABLE invoice_details ADD COLUMN payment_method TEXT NOT NULL DEFAULT 'cash' "
                                + "CHECK (payment_method IN ('cash', 'credit card', 'wallet'))");
                    }
                } catch (SQLException e) {
                    // Table doesn't exist yet, will be created by the create statement above
                    System.out.println("Table invoice_details does not exist yet, will be created.");
                }
                
                stmt.execute("PRAGMA foreign_keys=on;");
                //Delete All The App Data 
                //deleteAllData();

            } catch (SQLException e) {
                System.err.println("Error modifying tables: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Close connection method
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    public static void deleteAllData() {
    String[] deleteStatements = {
        "DELETE FROM invoice_details;",
        "DELETE FROM invoice_header;",
        "DELETE FROM invoice;",
        "DELETE FROM client;",
        "DELETE FROM user;"
    };

    try (Statement stmt = getConnection().createStatement()) {
        stmt.execute("PRAGMA foreign_keys=OFF;");
        stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name IN ('user', 'client', 'invoice', 'invoice_header', 'invoice_details');");

        for (String sql : deleteStatements) {
            stmt.executeUpdate(sql);
        }
        stmt.execute("PRAGMA foreign_keys=ON;");
        System.out.println("All data deleted successfully.");  // So you know it ran
    } catch (SQLException e) {
        System.err.println("Error deleting all data: " + e.getMessage());
        e.printStackTrace();
    }
    
}
}