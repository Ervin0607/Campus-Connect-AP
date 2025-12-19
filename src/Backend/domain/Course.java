package Backend.domain;

public class Course {
    private int CourseID;
    private String Code;
    private String Title;
    private int Credits;



    public int GetCourseID() {
        return CourseID;
    }
    public void SetCourseID(int CourseID) {
        this.CourseID = CourseID;
    }
    public String GetCode() {
        return Code;
    }
    public void SetCode(String Code) {
        this.Code = Code;
    }
    public void SetTitle(String Title) {
        this.Title = Title;
    }
    public String GetTitle() {
        return Title;
    }
    public int GetCredits() {
        return Credits;
    }
    public void SetCredits(int Credits) {
        this.Credits = Credits;
    }
}
