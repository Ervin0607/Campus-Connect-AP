package Backend.DataBaseHandler;

import Backend.domain.Registration;
import Backend.exceptions.CapacityFullException;
import Backend.util.DB;

import java.sql.*;
import java.util.*;

public class RegistrationHandler {

    public static final String STATUS_PRE_ENROLLED = "PRE-ENROLLED";
    public static final String STATUS_ENROLLED = "ENROLLED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_DROPPED = "DROPPED";

    public List<Registration> GetRegistrationList(int StudentRollNumber) throws SQLException {
        return FindByStudentRollNumber(StudentRollNumber);
    }

    public static List<Registration> FindByStudentRollNumber(int StudentRollNumber) throws SQLException {
        String SQL = "SELECT RegistrationNumber, Status, OfferingID, StudentRollNumber FROM registrations WHERE StudentRollNumber = ? AND Status != ?";
        List<Registration> RegistrationList = new ArrayList<>();
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {
            Statement.setInt(1, StudentRollNumber);
            Statement.setString(2, STATUS_DROPPED);
            try (ResultSet Result = Statement.executeQuery()) {
                while (Result.next()) {
                    Registration CurrentRegistration = new Registration();
                    CurrentRegistration.SetRegistrationNumber(Result.getInt("RegistrationNumber"));
                    CurrentRegistration.SetStatus(Result.getString("Status"));
                    CurrentRegistration.SetOfferingID(Result.getInt("OfferingID"));
                    CurrentRegistration.SetStudentRollNumber(Result.getInt("StudentRollNumber"));
                    RegistrationList.add(CurrentRegistration);
                }
            }
        }
        return RegistrationList;
    }

    public static List<Registration> ListByStudentRollNumber(int StudentRollNumber) throws SQLException {
        return FindByStudentRollNumber(StudentRollNumber);
    }

    public static List<Integer> getEnrolledOfferingIDs(int StudentRollNumber) throws SQLException {
        List<Integer> EnrolledOfferingIDs = new ArrayList<>();
        String SQL = "SELECT OfferingID FROM registrations WHERE StudentRollNumber = ? AND Status IN (?, ?, ?)";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {
            Statement.setInt(1, StudentRollNumber);
            Statement.setString(2, STATUS_PRE_ENROLLED);
            Statement.setString(3, STATUS_ENROLLED);
            Statement.setString(4, STATUS_COMPLETED);
            try (ResultSet Result = Statement.executeQuery()) {
                while (Result.next()) {
                    EnrolledOfferingIDs.add(Result.getInt("OfferingID"));
                }
            }
        }
        return EnrolledOfferingIDs;
    }

    public static List<Integer> GetOfferingIDsByStudentRollNumber(int StudentRollNumber) throws SQLException {
        return getEnrolledOfferingIDs(StudentRollNumber);
    }

    public static Set<Integer> GetRegisteredOfferingIds(int StudentRollNumber, String Semester, int Year) throws SQLException {
        String SQL = "SELECT r.OfferingID " +
                "FROM registrations r " +
                "JOIN offerings o ON o.OfferingID = r.OfferingID " +
                "WHERE r.StudentRollNumber=? AND o.Semester=? AND o.Year=? AND r.Status IN (?, ?, ?)";
        Set<Integer> OfferingIDSet = new HashSet<>();
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {
            Statement.setInt(1, StudentRollNumber);
            Statement.setString(2, Semester);
            Statement.setInt(3, Year);
            Statement.setString(4, STATUS_PRE_ENROLLED);
            Statement.setString(5, STATUS_ENROLLED);
            Statement.setString(6, STATUS_COMPLETED);
            try (ResultSet Result = Statement.executeQuery()) {
                while (Result.next()) {
                    OfferingIDSet.add(Result.getInt(1));
                }
            }
        }
        return OfferingIDSet;
    }

    public static boolean EnrollIfPossible(int StudentRollNumber, int OfferingID) throws SQLException, CapacityFullException {
        try (Connection DBConnection = DB.GetERPDataSource().getConnection()) {
            DBConnection.setAutoCommit(false);

            try (PreparedStatement DuplicateCheckStatement = DBConnection.prepareStatement(
                    "SELECT 1 FROM registrations WHERE StudentRollNumber=? AND OfferingID=? AND Status IN (?, ?, ?)")) {
                DuplicateCheckStatement.setInt(1, StudentRollNumber);
                DuplicateCheckStatement.setInt(2, OfferingID);
                DuplicateCheckStatement.setString(3, STATUS_PRE_ENROLLED);
                DuplicateCheckStatement.setString(4, STATUS_ENROLLED);
                DuplicateCheckStatement.setString(5, STATUS_COMPLETED);
                try (ResultSet Result = DuplicateCheckStatement.executeQuery()) {
                    if (Result.next()) {
                        DBConnection.rollback();
                        return false;
                    }
                }
            }

            int Capacity;
            int CurrentEnrollment;
            try (PreparedStatement OfferingCheckStatement = DBConnection.prepareStatement(
                    "SELECT Capacity, CurrentEnrollment FROM offerings WHERE OfferingID=? FOR UPDATE")) {
                OfferingCheckStatement.setInt(1, OfferingID);
                try (ResultSet Result = OfferingCheckStatement.executeQuery()) {
                    if (!Result.next()) {
                        DBConnection.rollback();
                        return false;
                    }
                    Capacity = Result.getInt("Capacity");
                    CurrentEnrollment = Result.getInt("CurrentEnrollment");
                }
            }
            if (CurrentEnrollment >= Capacity) {
                DBConnection.rollback();
                throw new CapacityFullException("Capacity full.");
            }

            int NextRegistrationNumber = 1;
            try (PreparedStatement MaxRegistrationStatement = DBConnection.prepareStatement("SELECT COALESCE(MAX(RegistrationNumber),0)+1 AS N FROM registrations");
                 ResultSet Result = MaxRegistrationStatement.executeQuery()) {
                if (Result.next()) NextRegistrationNumber = Result.getInt("N");
            }

            try (PreparedStatement InsertStatement = DBConnection.prepareStatement(
                    "INSERT INTO registrations (RegistrationNumber, Status, OfferingID, StudentRollNumber) VALUES (?,?,?,?)")) {
                InsertStatement.setInt(1, NextRegistrationNumber);
                InsertStatement.setString(2, STATUS_PRE_ENROLLED);
                InsertStatement.setInt(3, OfferingID);
                InsertStatement.setInt(4, StudentRollNumber);
                if (InsertStatement.executeUpdate() <= 0) {
                    DBConnection.rollback();
                    return false;
                }
            }

            try (PreparedStatement UpdateOfferingStatement = DBConnection.prepareStatement(
                    "UPDATE offerings SET CurrentEnrollment = CurrentEnrollment + 1 WHERE OfferingID=?")) {
                UpdateOfferingStatement.setInt(1, OfferingID);
                if (UpdateOfferingStatement.executeUpdate() <= 0) {
                    DBConnection.rollback();
                    return false;
                }
            }
            DBConnection.commit();
            return true;
        }
    }

    public static boolean DropByStudentAndOffering(int StudentRollNumber, int OfferingID) throws SQLException {
        try (Connection DBConnection = DB.GetERPDataSource().getConnection()) {
            DBConnection.setAutoCommit(false);

            int RowsAffected;
            try (PreparedStatement DeleteStatement = DBConnection.prepareStatement(
                    "DELETE FROM registrations WHERE StudentRollNumber=? AND OfferingID=? AND Status = ?")) {
                DeleteStatement.setInt(1, StudentRollNumber);
                DeleteStatement.setInt(2, OfferingID);
                DeleteStatement.setString(3, STATUS_PRE_ENROLLED);
                RowsAffected = DeleteStatement.executeUpdate();
            }

            if (RowsAffected > 0) {
                try (PreparedStatement UpdateOfferingStatement = DBConnection.prepareStatement(
                        "UPDATE offerings SET CurrentEnrollment = GREATEST(CurrentEnrollment-1,0) WHERE OfferingID=?")) {
                    UpdateOfferingStatement.setInt(1, OfferingID);
                    UpdateOfferingStatement.executeUpdate();
                }
                DBConnection.commit();
                return true;
            } else {
                DBConnection.rollback();
                return false;
            }
        }
    }

    public static boolean LockRegistrations(int StudentRollNumber, String Semester, int Year) throws SQLException {
        String SQL = "UPDATE registrations r " +
                "JOIN offerings o ON o.OfferingID = r.OfferingID " +
                "SET r.Status = ? " +
                "WHERE r.StudentRollNumber=? AND o.Semester=? AND o.Year=? AND r.Status = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {
            Statement.setString(1, STATUS_ENROLLED);
            Statement.setInt(2, StudentRollNumber);
            Statement.setString(3, Semester);
            Statement.setInt(4, Year);
            Statement.setString(5, STATUS_PRE_ENROLLED);
            return Statement.executeUpdate() > 0;
        }
    }

    public static boolean PublishGrades(int OfferingID) throws SQLException {
        String SQL = "UPDATE registrations SET Status = ? WHERE OfferingID = ? AND Status = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {
            Statement.setString(1, STATUS_COMPLETED);
            Statement.setInt(2, OfferingID);
            Statement.setString(3, STATUS_ENROLLED);
            System.out.println(Statement);
            return Statement.executeUpdate() > 0;
        }
    }

    public static boolean AddRegistration(Registration CurrentRegistration) throws SQLException, CapacityFullException {
        return EnrollIfPossible(CurrentRegistration.GetStudentRollNumber(), CurrentRegistration.GetOfferingID());
    }

    public boolean DeleteRegistration(Registration CurrentRegistration) throws SQLException {
        return DropByStudentAndOffering(CurrentRegistration.GetStudentRollNumber(), CurrentRegistration.GetOfferingID());
    }

    public static boolean IsTermLocked(int StudentRollNumber, String Semester, int Year) throws SQLException {
        String SQL = "SELECT 1 FROM registrations r " +
                "JOIN offerings o ON o.OfferingID = r.OfferingID " +
                "WHERE r.StudentRollNumber=? AND o.Semester=? AND o.Year=? AND r.Status IN (?, ?) LIMIT 1";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {
            Statement.setInt(1, StudentRollNumber);
            Statement.setString(2, Semester);
            Statement.setInt(3, Year);
            Statement.setString(4, STATUS_ENROLLED);
            Statement.setString(5, STATUS_COMPLETED);
            try (ResultSet Result = Statement.executeQuery()) {
                return Result.next();
            }
        }
    }

    public static int SumCreditsForTerm(int StudentRollNumber, String Semester, int Year) throws SQLException {
        String SQL = "SELECT COALESCE(SUM(c.Credits),0) FROM registrations r " +
                "JOIN offerings o ON o.OfferingID = r.OfferingID " +
                "JOIN courses c ON c.CourseID = o.CourseID " +
                "WHERE r.StudentRollNumber=? AND o.Semester=? AND o.Year=? AND r.Status IN (?, ?, ?)";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {
            Statement.setInt(1, StudentRollNumber);
            Statement.setString(2, Semester);
            Statement.setInt(3, Year);
            Statement.setString(4, STATUS_PRE_ENROLLED);
            Statement.setString(5, STATUS_ENROLLED);
            Statement.setString(6, STATUS_COMPLETED);
            try (ResultSet Result = Statement.executeQuery()) {
                return Result.next() ? Result.getInt(1) : 0;
            }
        }
    }

    public static boolean isOfferingPublished(int OfferingID) throws SQLException {
        String SQL = "SELECT 1 FROM registrations WHERE OfferingID = ? AND Status = ? LIMIT 1";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {
            Statement.setInt(1, OfferingID);
            Statement.setString(2, STATUS_COMPLETED);
            try (ResultSet Result = Statement.executeQuery()) {
                return Result.next();
            }
        }
    }

    public static TermHint FindLatestTermWithRegistrations(int StudentRollNumber) throws SQLException {
        String SQL = "SELECT o.Semester, o.Year FROM registrations r " +
                "JOIN offerings o ON o.OfferingID = r.OfferingID " +
                "WHERE r.StudentRollNumber=? AND r.Status != ? " +
                "GROUP BY o.Year, o.Semester " +
                "ORDER BY o.Year DESC, FIELD(o.Semester,'WINTER','FALL','SUMMER','SPRING') DESC LIMIT 1";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {
            Statement.setInt(1, StudentRollNumber);
            Statement.setString(2, STATUS_DROPPED);
            try (ResultSet Result = Statement.executeQuery()) {
                if (Result.next()) {
                    TermHint Term = new TermHint();
                    Term.Semester = Result.getString(1);
                    Term.Year = Result.getInt(2);
                    return Term;
                }
            }
        }
        return null;
    }

    public static class RegRow {
        public int OfferingID;
        public String Code;
        public String Title;
        public String Professor;
        public int Credits;
        public String Status;
        public String Semester;
        public int Year;
    }

    public static class TermHint {
        public String Semester;
        public int Year;
    }

    public static List<RegRow> ListRegistrationsDetailedByStudent(int StudentRollNumber) throws SQLException {
        String SQL = "SELECT r.OfferingID, c.Code, c.Title, c.Credits, COALESCE(i.Name,'TBA') AS Professor, r.Status, o.Semester, o.Year " +
                "FROM registrations r " +
                "JOIN offerings o ON o.OfferingID = r.OfferingID " +
                "JOIN courses c ON c.CourseID = o.CourseID " +
                "LEFT JOIN instructors i ON i.InstructorID = o.InstructorID " +
                "WHERE r.StudentRollNumber=? AND r.Status != ? " +
                "ORDER BY o.Year DESC, c.Code";
        List<RegRow> RegistrationRows = new ArrayList<>();
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {
            Statement.setInt(1, StudentRollNumber);
            Statement.setString(2, STATUS_DROPPED);
            try (ResultSet Result = Statement.executeQuery()) {
                while (Result.next()) {
                    RegRow RegistrationRow = new RegRow();
                    RegistrationRow.OfferingID = Result.getInt(1);
                    RegistrationRow.Code = Result.getString(2);
                    RegistrationRow.Title = Result.getString(3);
                    RegistrationRow.Credits = Result.getInt(4);
                    RegistrationRow.Professor = Result.getString(5);
                    RegistrationRow.Status = Result.getString(6);
                    RegistrationRow.Semester = Result.getString(7);
                    RegistrationRow.Year = Result.getInt(8);
                    RegistrationRows.add(RegistrationRow);
                }
            }
        }
        return RegistrationRows;
    }

    public static List<RegRow> ListRegistrationsDetailedAnyTerm(int StudentRollNumber, String Semester, Integer Year) throws SQLException {
        return ListRegistrationsDetailed(StudentRollNumber, Semester, Year != null ? Year : 0);
    }

    public static List<RegRow> ListRegistrationsDetailed(int StudentRollNumber, String Semester, int Year) throws SQLException {
        String SQL = "SELECT r.OfferingID, c.Code, c.Title, c.Credits, i.Name AS Professor, r.Status " +
                "FROM registrations r " +
                "JOIN offerings o ON o.OfferingID = r.OfferingID " +
                "JOIN courses c ON c.CourseID = o.CourseID " +
                "LEFT JOIN instructors i ON i.InstructorID = o.InstructorID " +
                "WHERE r.StudentRollNumber=? AND o.Semester=? AND o.Year=? AND r.Status != ? " +
                "ORDER BY c.Code";
        List<RegRow> RegistrationList = new ArrayList<>();
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {
            Statement.setInt(1, StudentRollNumber);
            Statement.setString(2, Semester);
            Statement.setInt(3, Year);
            Statement.setString(4, STATUS_DROPPED);
            try (ResultSet Result = Statement.executeQuery()) {
                while (Result.next()) {
                    RegRow RegistrationRow = new RegRow();
                    RegistrationRow.OfferingID = Result.getInt("OfferingID");
                    RegistrationRow.Code = Result.getString("Code");
                    RegistrationRow.Title = Result.getString("Title");
                    RegistrationRow.Credits = Result.getInt("Credits");
                    RegistrationRow.Professor = Result.getString("Professor") == null ? "TBA" : Result.getString("Professor");
                    RegistrationRow.Status = Result.getString("Status");
                    RegistrationList.add(RegistrationRow);
                }
            }
        }
        return RegistrationList;
    }
}
