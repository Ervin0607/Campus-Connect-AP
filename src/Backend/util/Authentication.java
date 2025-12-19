package Backend.util;

import Backend.DataBaseHandler.UserAuthenticationHandler;
import Backend.domain.User;
import java.sql.SQLException;

public class Authentication {
    public static boolean Login(String Username, String Password) throws SQLException {
        try {
            User CurrentUser = UserAuthenticationHandler.FindUserName(Username);
            if (CurrentUser == null) {
                return false;
            }
            if ("SUSPENDED".equals(CurrentUser.GetStatus())) {
                throw new RuntimeException("Account has been Suspended. Contact the Authorities ");
            }
            if (!PasswordUtil.CheckPassword(Password, CurrentUser.GetPassword())) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return true;
    }
}
