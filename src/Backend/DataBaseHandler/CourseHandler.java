package Backend.DataBaseHandler;

import Backend.domain.Course;
import Backend.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CourseHandler {

    public static Course FindCourseByCode(String Code) throws SQLException {
        String SQL = "SELECT CourseId, Code, Title, Credits FROM courses WHERE Code = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setString(1, Code);
            try (ResultSet Result = Statement.executeQuery()) {
                if (Result.next()) {
                    Course CurrentCourse = new Course();
                    CurrentCourse.SetCourseID(Result.getInt("CourseId"));
                    CurrentCourse.SetCode(Result.getString("Code"));
                    CurrentCourse.SetTitle(Result.getString("Title"));
                    CurrentCourse.SetCredits(Result.getInt("Credits"));
                    return CurrentCourse;
                }
            }
        }
        return null;
    }

    public static Course FindCourseByID(int CourseID) throws SQLException {
        String SQL = "SELECT CourseId, Code, Title, Credits FROM courses WHERE CourseId = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, CourseID);
            try (ResultSet Result = Statement.executeQuery()) {
                if (Result.next()) {
                    Course CurrentCourse = new Course();
                    CurrentCourse.SetCourseID(Result.getInt("CourseId"));
                    CurrentCourse.SetCode(Result.getString("Code"));
                    CurrentCourse.SetTitle(Result.getString("Title"));
                    CurrentCourse.SetCredits(Result.getInt("Credits"));
                    return CurrentCourse;
                }
            }
        }
        return null;
    }

    public static boolean AddCourse(Course NewCourse) throws SQLException {
        String SQL = "INSERT INTO courses (CourseID, Code, Title, Credits) VALUES (?, ?, ?, ?)";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, NewCourse.GetCourseID());
            Statement.setString(2, NewCourse.GetCode());
            Statement.setString(3, NewCourse.GetTitle());
            Statement.setInt(4, NewCourse.GetCredits());
            return Statement.executeUpdate() > 0;
        }
    }

    public static boolean DeleteCourse(String Code) throws SQLException {
        String SQL = "DELETE FROM courses WHERE Code = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setString(1, Code);
            return Statement.executeUpdate() > 0;
        }
    }

    public static boolean UpdateCourse(Course CurrentCourse) throws SQLException {
        String SQL = "UPDATE courses SET Title = ?, Credits = ? WHERE Code = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setString(1, CurrentCourse.GetTitle());
            Statement.setInt(2, CurrentCourse.GetCredits());
            Statement.setString(3, CurrentCourse.GetCode());
            return Statement.executeUpdate() > 0;
        }
    }

    public static List<Course> GetAllCourses() throws SQLException {
        String SQL = "SELECT CourseId, Code, Title, Credits FROM courses";
        List<Course> CourseList = new ArrayList<>();

        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL);
             ResultSet Result = Statement.executeQuery()) {

            while (Result.next()) {
                Course CurrentCourse = new Course();
                CurrentCourse.SetCourseID(Result.getInt("CourseId"));
                CurrentCourse.SetCode(Result.getString("Code"));
                CurrentCourse.SetTitle(Result.getString("Title"));
                CurrentCourse.SetCredits(Result.getInt("Credits"));
                CourseList.add(CurrentCourse);
            }
        }
        CourseList.sort(Comparator.comparingInt(Course::GetCourseID));
        return CourseList;
    }

    public static int GetMaxCourseID() throws SQLException {
        String SQL = "SELECT COALESCE(MAX(CourseId), 0) AS MaxID FROM courses";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL);
             ResultSet Result = Statement.executeQuery()) {

            if (Result.next()) return Result.getInt("MaxID");
        }
        return 0;
    }
}
