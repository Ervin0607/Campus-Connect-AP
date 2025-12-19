package Backend.domain;

public class User {
    private int UserID;
    private String UserName;
    private String Role;
    private String Password;
    private String Status;

    public int GetUserID(){
        return UserID;
    }
    public String GetUserName(){
        return UserName;
    }
    public String GetRole(){
        return Role;
    }
    public String GetPassword(){
        return Password;
    }
    public String GetStatus(){
        return Status;
    }
    public void SetUserID(int UserID){
        this.UserID = UserID;
    }
    public void SetUserName(String UserName){
        this.UserName = UserName;
    }
    public void SetRole(String Role){
        this.Role = Role;
    }
    public void SetPassword(String Password){
        this.Password = Password;
    }
    public void SetStatus(String Status){
        this.Status = Status;
    }
}
