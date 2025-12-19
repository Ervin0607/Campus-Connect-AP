package Backend.domain;

import java.util.HashMap;

public class Offerings {
    private int OfferingID;
    private int CourseID;
    private int InstructorID;
    private String Semester;
    private int Year;
    private int Capacity;
    private int CurrentEnrollment;
    private HashMap<String, Integer> GradingComponents;
    private HashMap<String, Integer> GradingSlabs;
    private String LectureSchedule;
    private String LabSchedule;
    private String Announcements;

    public int GetOfferingID() {
        return OfferingID;
    }
    public void SetOfferingID(int OfferingID) {
        this.OfferingID = OfferingID;
    }
    public int GetCourseID() {
        return CourseID;
    }
    public void SetCourseID(int CourseID) {
        this.CourseID = CourseID;
    }
    public int GetInstructorID() {
        return InstructorID;
    }
    public void SetInstructorID(int InstructorID) {
        this.InstructorID = InstructorID;
    }
    public String GetSemester() {
        return Semester;
    }
    public void SetSemester(String Semester) {
        this.Semester = Semester;
    }
    public int GetYear() {
        return Year;
    }
    public void SetYear(int Year) {
        this.Year = Year;
    }
    public int GetCapacity() {
        return Capacity;
    }
    public void SetCapacity(int Capacity) {
        this.Capacity = Capacity;
    }
    public int GetCurrentEnrollment() {return CurrentEnrollment;}
    public void SetCurrentEnrollment(int CurrentEnrollment) {this.CurrentEnrollment = CurrentEnrollment;}
    public HashMap<String, Integer> GetGradingComponents() {
        return GradingComponents;
    }
    public  void SetGradingComponents(HashMap<String, Integer> GradingComponents) {
        this.GradingComponents = GradingComponents;
    }
    public HashMap<String, Integer> GetGradingSlabs() {
        return GradingSlabs;
    }
    public void SetGradingSlabs(HashMap<String, Integer> gradingSlabs) {
        GradingSlabs = gradingSlabs;
    }
    public String GetLectureSchedule() {return LectureSchedule;}
    public void SetLectureSchedule(String LectureSchedule) {
        this.LectureSchedule = LectureSchedule;
    }
    public String GetLabSchedule() {return LabSchedule;}
    public void SetLabSchedule(String LabSchedule) {
        this.LabSchedule = LabSchedule;
    }
    public String GetAnnouncements() {return Announcements;}
    public void SetAnnouncements(String Announcements) {
        this.Announcements = Announcements;
    }

}
