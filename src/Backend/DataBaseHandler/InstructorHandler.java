package Backend.DataBaseHandler;

import Backend.domain.Instructor;
import Backend.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InstructorHandler {

    public static Instructor FindInstructorByInstructorID(int InstructorID) throws SQLException {
        String SQL = "SELECT UserID, Name, Email, Qualification, JoiningDate, Department FROM instructors WHERE InstructorID = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, InstructorID);

            try (ResultSet Result = Statement.executeQuery()) {
                if (Result.next()) {
                    Instructor CurrentInstructor = new Instructor();
                    CurrentInstructor.SetDepartment(Result.getString("Department"));
                    CurrentInstructor.SetName(Result.getString("Name"));
                    CurrentInstructor.SetEmail(Result.getString("Email"));
                    CurrentInstructor.SetUserID(Result.getInt("UserID"));
                    CurrentInstructor.SetInstructorID(InstructorID);
                    CurrentInstructor.SetJoiningDate(Result.getDate("JoiningDate").toLocalDate());
                    CurrentInstructor.SetQualification(Result.getString("Qualification"));
                    return CurrentInstructor;
                }
            }
        }
        return null;
    }

    public static Instructor FindInstructorByUserID(int UserID) throws SQLException {
        String SQL = "SELECT InstructorID, Name, Email, Qualification, JoiningDate, Department FROM instructors WHERE UserID = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, UserID);

            try (ResultSet Result = Statement.executeQuery()) {
                if (Result.next()) {
                    Instructor CurrentInstructor = new Instructor();
                    CurrentInstructor.SetDepartment(Result.getString("Department"));
                    CurrentInstructor.SetName(Result.getString("Name"));
                    CurrentInstructor.SetEmail(Result.getString("Email"));
                    CurrentInstructor.SetUserID(UserID);
                    CurrentInstructor.SetInstructorID(Result.getInt("InstructorID"));
                    CurrentInstructor.SetJoiningDate(Result.getDate("JoiningDate").toLocalDate());
                    CurrentInstructor.SetQualification(Result.getString("Qualification"));
                    return CurrentInstructor;
                }
            }
        }
        return null;
    }

    public static boolean DeleteInstructor(int UserID) throws SQLException {
        String SQL = "DELETE FROM instructors WHERE UserID  = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, UserID);
            return Statement.executeUpdate() > 0;
        }
    }

    public static boolean UpdateInstructor(Instructor CurrentInstructor) throws SQLException {
        String SQL = "UPDATE instructors SET Name = ?, Email = ?, Qualification = ?, JoiningDate = ?, Department = ? WHERE InstructorID = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setString(1, CurrentInstructor.GetName());
            Statement.setString(2, CurrentInstructor.GetEmail());
            Statement.setString(3, CurrentInstructor.GetQualification());
            Statement.setDate(4, java.sql.Date.valueOf(CurrentInstructor.GetJoiningDate()));
            Statement.setString(5, CurrentInstructor.GetDepartment());
            Statement.setInt(6, CurrentInstructor.GetInstructorID());
            return Statement.executeUpdate() > 0;
        }
    }

    public static List<Instructor> GetAllInstructors() throws SQLException {
        String SQL = "SELECT UserID, InstructorID, Name, Email, Qualification, JoiningDate, Department FROM instructors";
        List<Instructor> InstructorList = new ArrayList<>();
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL);
             ResultSet Result = Statement.executeQuery()) {

            while (Result.next()) {
                Instructor CurrentInstructor = new Instructor();
                CurrentInstructor.SetDepartment(Result.getString("Department"));
                CurrentInstructor.SetName(Result.getString("Name"));
                CurrentInstructor.SetEmail(Result.getString("Email"));
                CurrentInstructor.SetJoiningDate(Result.getDate("JoiningDate").toLocalDate());
                CurrentInstructor.SetQualification(Result.getString("Qualification"));
                CurrentInstructor.SetUserID(Result.getInt("UserID"));
                CurrentInstructor.SetInstructorID(Result.getInt("InstructorID"));
                InstructorList.add(CurrentInstructor);
            }
        }
        InstructorList.sort(Comparator.comparing(Instructor::GetJoiningDate));
        return InstructorList;
    }

    public static boolean AddInstructor(Instructor NewInstructor) throws SQLException {
        String SQL = "INSERT INTO instructors (Name, Email, Qualification, JoiningDate, Department, UserID, InstructorID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setString(1, NewInstructor.GetName());
            Statement.setString(2, NewInstructor.GetEmail());
            Statement.setString(3, NewInstructor.GetQualification());
            Statement.setDate(4, java.sql.Date.valueOf(NewInstructor.GetJoiningDate()));
            Statement.setString(5, NewInstructor.GetDepartment());
            Statement.setInt(6, NewInstructor.GetUserID());
            Statement.setInt(7, NewInstructor.GetInstructorID());
            return Statement.executeUpdate() > 0;
        }
    }
}
