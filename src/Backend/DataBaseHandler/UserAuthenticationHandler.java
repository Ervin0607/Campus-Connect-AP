package Backend.DataBaseHandler;

import Backend.domain.User;
import Backend.util.DB;
import Backend.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserAuthenticationHandler {
    public static User FindUserName(String UserName) throws SQLException {
        User UserAuth = null;
        String SQL = "SELECT UserID, UserName, Role, Password, Status FROM users WHERE UserName = ?";

        Connection DBConnection = null;
        PreparedStatement Statement = null;
        ResultSet Result = null;

        try {
            DBConnection = DB.GetAuthenticanDataSource().getConnection();
            Statement = DBConnection.prepareStatement(SQL);
            Statement.setString(1, UserName);
            Result = Statement.executeQuery();

            if (Result.next()) {
                UserAuth = new User();
                UserAuth.SetUserID(Result.getInt("UserID"));
                UserAuth.SetUserName(Result.getString("UserName"));
                UserAuth.SetRole(Result.getString("Role"));
                UserAuth.SetPassword(Result.getString("Password"));
                UserAuth.SetStatus(Result.getString("Status"));
            }
        }
        finally {
            if (Result != null) {
                Result.close();
            }
            if (Statement != null) {
                Statement.close();
            }
            if (DBConnection != null) {
                DBConnection.close();
            }
        }

        return UserAuth;
    }


    public static User FindUserID(int UserID) throws SQLException {
        User UserAuth = null;
        String SQL = "SELECT UserID, UserName, Role, Password, Status FROM users WHERE UserID = ?";

        Connection DBConnection = null;
        PreparedStatement Statement = null;
        ResultSet Result = null;

        try {
            DBConnection = DB.GetAuthenticanDataSource().getConnection();
            Statement = DBConnection.prepareStatement(SQL);
            Statement.setInt(1, UserID);
            Result = Statement.executeQuery();

            if (Result.next()) {
                UserAuth = new User();
                UserAuth.SetUserID(Result.getInt("UserID"));
                UserAuth.SetUserName(Result.getString("UserName"));
                UserAuth.SetRole(Result.getString("Role"));
                UserAuth.SetPassword(Result.getString("Password"));
                UserAuth.SetStatus(Result.getString("Status"));
            }
        }
        finally {
            if (Result != null) {
                Result.close();
            }
            if (Statement != null) {
                Statement.close();
            }
            if (DBConnection != null) {
                DBConnection.close();
            }
        }

        return UserAuth;
    }


    public static boolean CreateUser(User UserAuth) throws SQLException {
        String SQL = "INSERT INTO users (UserID, UserName, Role, Password, Status) VALUES (?, ?, ?, ?, ?)";
        try (Connection DBConnection = DB.GetAuthenticanDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, UserAuth.GetUserID());
            Statement.setString(2, UserAuth.GetUserName());
            Statement.setString(3, UserAuth.GetRole());
            Statement.setString(4, UserAuth.GetPassword());
            Statement.setString(5, UserAuth.GetStatus());
            Statement.executeUpdate();
            return true;
        }
        catch(SQLException ExceptionObject){
            ExceptionObject.printStackTrace();
            return false;
        }
    }


    public static boolean UpdateUser(User UserAuth, String NewUserName) throws SQLException {
        String SQL = "UPDATE users SET UserName = ? WHERE UserID = ?";
        try (Connection DBConnection = DB.GetAuthenticanDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setString(1, NewUserName);
            Statement.setInt(2, UserAuth.GetUserID());

            int RowsAffected = Statement.executeUpdate();
            return RowsAffected > 0;
        }
    }

    public static boolean DeleteUser(User UserAuth) throws SQLException {
        String SQL = "DELETE FROM users WHERE UserID = ?";
        try (Connection DBConnection = DB.GetAuthenticanDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {
            Statement.setInt(1, UserAuth.GetUserID());
            int RowsAffected = Statement.executeUpdate();
            return RowsAffected > 0;
        }
    }

    public static List<User> GetAllUsers() throws SQLException {
        List<User> UserList = new ArrayList<>();
        String SQL = "SELECT UserID, UserName, Role, Password, Status FROM users ORDER BY UserID ASC";
        try (Connection DBConnection = DB.GetAuthenticanDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL);
             ResultSet Result = Statement.executeQuery()) {
            while (Result.next()) {
                User CurrentUser = new User();
                CurrentUser.SetUserID(Result.getInt("UserID"));
                CurrentUser.SetUserName(Result.getString("UserName"));
                CurrentUser.SetRole(Result.getString("Role"));
                CurrentUser.SetPassword(Result.getString("Password"));
                CurrentUser.SetStatus(Result.getString("Status"));
                UserList.add(CurrentUser);
            }
        }
        return UserList;
    }


    public static int GetMaxUserID() throws SQLException {
        String SQL = "SELECT COALESCE(MAX(UserID), 0) AS MaxID FROM users";
        try (Connection DBConnection = DB.GetAuthenticanDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL);
             ResultSet Result = Statement.executeQuery()) {
            return Result.next() ? Result.getInt("MaxID") : 0;
        }
    }

    public static boolean UpdateUserPassword(User UserAuth, String NewPassword) throws SQLException {
        String SQL = "UPDATE users SET Password = ? WHERE UserID = ?";

        try (Connection DBConnection = DB.GetAuthenticanDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            String HashedPassword = PasswordUtil.Hashing(NewPassword);

            Statement.setString(1, HashedPassword);
            Statement.setInt(2, UserAuth.GetUserID());

            int RowsAffected = Statement.executeUpdate();

            if (RowsAffected > 0) {
                UserAuth.SetPassword(HashedPassword);
                return true;
            }
            return false;
        }
    }
}
