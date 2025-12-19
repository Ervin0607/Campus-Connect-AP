package Backend.domain;

public class Student {
    private String Name;
    private int RollNumber;
    private String Program;
    private int Year;
    private int UserID;


    public String GetName(){
        return Name;
    }
    public int GetRollNumber(){
        return RollNumber;
    }
    public String GetProgram(){
        return Program;
    }
    public int GetYear(){
        return Year;
    }
    public int GetUserID(){
        return UserID;
    }
    public void SetName(String Name){
        this.Name = Name;
    }
    public void SetRollNumber(int RollNumber){
        this.RollNumber = RollNumber;
    }
    public void SetProgram(String Program){
        this.Program = Program;
    }
    public void SetYear(int Year){
        this.Year = Year;
    }
    public void SetUserID(int UserID){
        this.UserID = UserID;
    }

}
