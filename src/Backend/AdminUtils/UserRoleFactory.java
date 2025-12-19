package Backend.AdminUtils;

import Backend.DataBaseHandler.InstructorHandler;
import Backend.DataBaseHandler.StudentHandler;
import Backend.DataBaseHandler.UserAuthenticationHandler;
import Backend.domain.Instructor;
import Backend.domain.Student;
import Backend.domain.User;

import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static Backend.util.PasswordUtil.Hashing;

public class UserRoleFactory {

    public static final String DummyPassword = "DUMMY@123";
    public static final String DefaultStatus = "ACTIVE";

    private static int GlobalUserID = 0;
    private static final Map<Integer, Integer> StudentSeqByYear = new HashMap<>();
    private static final Map<Integer, Integer> InstructorSeqByYear = new HashMap<>();

    public boolean Create(String Name, String RoleRaw) {
        int CurrentYear = Year.now().getValue();
        return Create(Name, RoleRaw, null, CurrentYear);
    }

    public static synchronized void InitializeFromDatabase(int CurrentMaxUserID) {
        if (CurrentMaxUserID > GlobalUserID) {
            GlobalUserID = CurrentMaxUserID;
        }
    }

    public boolean Create(String Name, String RoleRaw, String UserNameOrNull, int Year) {
        try {
            if (Name == null || Name.isBlank()) throw new IllegalArgumentException("name is required");
            if (RoleRaw == null || RoleRaw.isBlank()) throw new IllegalArgumentException("role is required");

            String Role = RoleRaw.trim().toUpperCase(Locale.ROOT);
            int UserID = NextUserID();
            String Username = (UserNameOrNull == null || UserNameOrNull.isBlank())
                    ? SlugFromName(Name) + Pad5(UserID)
                    : UserNameOrNull.trim();

            User User = new User();
            User.SetUserID(UserID);
            User.SetUserName(Username);
            User.SetRole(Role);
            User.SetPassword(Hashing(DummyPassword));
            User.SetStatus(DefaultStatus);

            UserAuthenticationHandler.CreateUser(User);

            switch (Role) {
                case "STUDENT":
                    int Roll = NextStudentRoll(Year);
                    Student CurrentStudent = new Student();
                    CurrentStudent.SetUserID(UserID);
                    CurrentStudent.SetRollNumber(Roll);
                    CurrentStudent.SetName(Name);
                    CurrentStudent.SetProgram("UNASSIGNED");
                    CurrentStudent.SetYear(1);
                    StudentHandler.AddStudent(CurrentStudent);
                    break;

                case "INSTRUCTOR":
                    int InstructorID = NextInstructorId(Year);
                    Instructor CurrentInstructor = new Instructor();
                    CurrentInstructor.SetUserID(UserID);
                    CurrentInstructor.SetInstructorID(InstructorID);
                    CurrentInstructor.SetName(Name);
                    CurrentInstructor.SetEmail("unknown@example.com");
                    CurrentInstructor.SetQualification("UNSPECIFIED");
                    CurrentInstructor.SetJoiningDate(LocalDate.of(Year, 7, 1));
                    CurrentInstructor.SetDepartment("GENERAL");
                    InstructorHandler.AddInstructor(CurrentInstructor);
                    break;

                case "ADMIN":
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported role: " + Role + " (use STUDENT / INSTRUCTOR / ADMIN)");
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static synchronized int NextUserID() {
        GlobalUserID += 1;
        return GlobalUserID;
    }

    public static synchronized int NextStudentRoll(int Year) {
        int Last = StudentSeqByYear.getOrDefault(Year, 0) + 1;
        StudentSeqByYear.put(Year, Last);
        return Year * 1000 + Last;
    }

    public static synchronized int NextInstructorId(int Year) {
        int Last = InstructorSeqByYear.getOrDefault(Year, 0) + 1;
        InstructorSeqByYear.put(Year, Last);
        return Year * 1000 + Last;
    }

    public static synchronized int CurrentStudentCount(int Year) {
        return StudentSeqByYear.getOrDefault(Year, 0);
    }

    public static synchronized int CurrentInstructorCount(int Year) {
        return InstructorSeqByYear.getOrDefault(Year, 0);
    }

    private static String Pad5(int N) { return String.format("%05d", N); }

    private static String SlugFromName(String Name) {
        String CurrentName = Name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", ".");
        CurrentName = CurrentName.replaceAll("^\\.+|\\.+$", "");
        return CurrentName.isBlank() ? "user" : CurrentName;
    }
}