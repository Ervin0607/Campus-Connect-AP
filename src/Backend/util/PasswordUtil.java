package Backend.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {


    public static String Hashing(String Password){
        return BCrypt.hashpw(Password, BCrypt.gensalt(10));
    }

    public static boolean CheckPassword(String Password, String HashedPassword){
        if(Password == null || HashedPassword == null){
            return false;
        }
        else {
            if(BCrypt.checkpw(Password, HashedPassword)){
                return true;
            }
            else {
                return false;
            }

        }
    }

}
