package Backend.DataBaseHandler;

import Backend.domain.Instructor;
import Backend.domain.Student;
import Backend.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentHandler {
    public static Student FindStudentByRollNumber(int RollNumber) throws SQLException {
        Student CurrentStudent = null;
        String SQL = "SELECT * FROM students WHERE RollNumber = ?";
        PreparedStatement Statement = null;
        ResultSet Result = null;
        Connection DBConnection = null;
        try {
            DBConnection = DB.GetERPDataSource().getConnection();
            Statement = DBConnection.prepareStatement(SQL);
            Statement.setInt(1, Integer.parseInt(String.valueOf(RollNumber)));
            Result = Statement.executeQuery();
            if (Result.next()) {
                CurrentStudent = new Student();
                CurrentStudent.SetName(Result.getString("Name"));
                CurrentStudent.SetProgram(Result.getString("Program"));
                CurrentStudent.SetYear(Result.getInt("Year"));
                CurrentStudent.SetRollNumber(Integer.parseInt(Result.getString("RollNumber")));
                CurrentStudent.SetUserID(Integer.parseInt(Result.getString("UserID")));
            }
        }
        finally {
            if (Result != null) {
                Result.close();
            }
            if (Statement != null) {
                Statement.close();
            }
            if (DBConnection != null) {
                DBConnection.close();
            }
        }
        return CurrentStudent;


    }


    public static boolean AddStudent(Student CurrentStudent) throws SQLException {
        String SQL = "INSERT INTO students VALUES (?, ?, ?, ?,?)";
        PreparedStatement Statement = null;
        ResultSet Result = null;
        Connection DBConnection = null;
        try {
            DBConnection = DB.GetERPDataSource().getConnection();
            Statement = DBConnection.prepareStatement(SQL);
            Statement.setString(1, CurrentStudent.GetName());
            Statement.setInt(2, CurrentStudent.GetRollNumber());
            Statement.setString(3, CurrentStudent.GetProgram());
            Statement.setInt(4, CurrentStudent.GetYear());
            Statement.setInt(5, CurrentStudent.GetUserID());

            Statement.executeUpdate();
            return true;

        }
        catch(SQLException ExceptionObject) {
            System.out.println(ExceptionObject.getMessage());
            return false;
        }
    }

    public boolean DeleteStudent(Student CurrentStudent) throws SQLException {
        String SQL = "DELETE FROM students WHERE RollNumber = ?";
        PreparedStatement Statement = null;
        ResultSet Result = null;
        Connection DBConnection = null;
        try {
            DBConnection = DB.GetERPDataSource().getConnection();
            Statement = DBConnection.prepareStatement(SQL);
            Statement.setInt(1, CurrentStudent.GetRollNumber());
            int RowsAffected =  Statement.executeUpdate();
            return RowsAffected > 0;

        }
        catch(SQLException ExceptionObject) {
            System.out.println(ExceptionObject.getMessage());
            return false;
        }
    }
    public boolean UpdateStudentName(String RollNumber, String NewName) throws SQLException {
        String SQL = "UPDATE students SET Name = ? WHERE RollNumber = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setString(1, NewName);
            Statement.setString(2, RollNumber);

            return Statement.executeUpdate() > 0;
        }
    }

    public boolean UpdateStudentProgram(String RollNumber, String NewProgram) throws SQLException {
        String SQL = "UPDATE students SET Program = ? WHERE RollNumber = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setString(1, NewProgram);
            Statement.setString(2, RollNumber);

            return Statement.executeUpdate() > 0;
        }
    }

    public boolean UpdateStudentYear(String RollNumber, int NewYear) throws SQLException {
        String SQL = "UPDATE students SET Year = ? WHERE RollNumber = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, NewYear);
            Statement.setString(2, RollNumber);

            return Statement.executeUpdate() > 0;
        }
    }

    public boolean UpdateStudent(String RollNumber, String NewName, String NewProgram, int NewYear) throws SQLException {
        String SQL = "UPDATE students SET Name = ?, Program = ?, Year = ? WHERE RollNumber = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setString(1, NewName);
            Statement.setString(2, NewProgram);
            Statement.setInt(3, NewYear);
            Statement.setString(4, RollNumber);

            return Statement.executeUpdate() > 0;
        }
    }

    public static Student FindStudentByUserID(int UserID) throws SQLException {
        String SQL = "SELECT Name, RollNumber, Program, Year, UserID FROM students WHERE UserID = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {
            Statement.setInt(1, UserID);
            try (ResultSet Result = Statement.executeQuery()) {
                if (Result.next()) {
                    Student CurrentStudent = new Student();
                    CurrentStudent.SetName(Result.getString("Name"));
                    CurrentStudent.SetRollNumber(Result.getInt("RollNumber"));
                    CurrentStudent.SetProgram(Result.getString("Program"));
                    CurrentStudent.SetYear(Result.getInt("Year"));
                    CurrentStudent.SetUserID(Result.getInt("UserID"));
                    return CurrentStudent;
                }
            }
        }
        return null;
    }




}
