package Backend.domain;

import java.time.LocalDate;

public class Instructor {
    private int InstructorID;
    private int UserID;
    private String Name;
    private String Email;
    private String Qualification;
    private LocalDate JoiningDate;
    private String Department;



    public int GetUserID(){
        return UserID;
    }
    public String GetName(){
        return Name;
    }
    public String GetEmail(){
        return Email;
    }
    public String GetQualification(){
        return Qualification;
    }
    public LocalDate GetJoiningDate(){
        return JoiningDate;
    }
    public String GetDepartment(){
        return Department;
    }
    public int GetInstructorID(){
        return InstructorID;
    }
    public void SetUserID(int UserID){
        this.UserID = UserID;
    }
    public void SetName(String Name){
        this.Name = Name;
    }
    public void SetEmail(String Email){
        this.Email = Email;
    }
    public void SetQualification(String Qualification){
        this.Qualification = Qualification;
    }
    public void SetJoiningDate(LocalDate JoiningDate){
        this.JoiningDate = JoiningDate;
    }
    public void SetDepartment(String Department){
        this.Department = Department;
    }
    public void SetInstructorID(int InstructorID){
        this.InstructorID = InstructorID;
    }


}
