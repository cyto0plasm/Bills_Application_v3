package com.team.bills;

import Data_Access_Object.Client.Client;
import Data_Access_Object.Client.ClientDao;
import Data_Access_Object.Client.ClientDaoImp;
import Data_Access_Object.DbConnection;
import Data_Access_Object.invoice_Details.Invoice_Details;
import Data_Access_Object.invoice_Details.Invoice_DetailsDaoImp;
import Data_Access_Object.user.User;
import Data_Access_Object.user.UserDao;
import Data_Access_Object.user.UserDaoImp;
import frames.Login;

import javax.swing.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.sql.Connection;
import java.sql.SQLException;
import Data_Access_Object.invoice_Details.Invoice_DetailsDao;

public class Bills {

    private static final String LOCK_FILE = "app.lock";
    private static FileChannel lockChannel;
    private static FileLock lock;

    public static void main(String[] args) throws SQLException {
        if (!acquireLock()) {
            JOptionPane.showMessageDialog(null, "التطبيق يعمل بالفعل!", "تنبيه", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        // Database Connection Test
        Connection con = DbConnection.getConnection();
        System.out.println(con == null ? "Failed to connect" : "Connected");
        // Initialize DAOs
        UserDao userDao = new UserDaoImp();
        ClientDao clientDao = new ClientDaoImp();
        Invoice_DetailsDao invoiceDao = new Invoice_DetailsDaoImp();
   
        Client client;
        // Launch Login Screen
        SwingUtilities.invokeLater(() -> {
          //  Login login = new Login();
            Login login=new Login();
            login.setVisible(true);
        });
    }

    private static boolean acquireLock() {
        try {
            File file = new File(LOCK_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            lockChannel = new RandomAccessFile(file, "rw").getChannel();
            lock = lockChannel.tryLock();
            return lock != null;
        } catch (IOException e) {
            return false;
        }
    }
}
