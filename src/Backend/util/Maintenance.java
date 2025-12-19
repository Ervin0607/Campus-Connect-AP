package Backend.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Maintenance {
    public static boolean CurrentState() throws SQLException {
        String SQL = "SELECT * FROM maintenance";
        Connection DBConnection = DB.GetERPDataSource().getConnection();
        PreparedStatement Statement = null;
        ResultSet Result = null;
        try {
            Statement = DBConnection.prepareStatement(SQL);
            Result = Statement.executeQuery();
            if (Result.getBoolean("value")) {
                return true;
            } else {
                return false;
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void ChangeState() {
        String SQL = "UPDATE settings SET value = ?";
        Connection DBConnection = null;
        PreparedStatement Statement = null;
        try {
            DBConnection = DB.GetERPDataSource().getConnection();
            Statement = DBConnection.prepareStatement(SQL);
            boolean Current = CurrentState();
            Statement.setBoolean(1, !Current);
            Statement.executeUpdate();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
