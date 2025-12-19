package Backend.DataBaseHandler;

import Backend.util.DB;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SemestersHandler {

    private static final ObjectMapper ObjectMapperInstance = new ObjectMapper();

    public static class SemesterProfile {
        public int Year;
        public String Semester;
        public String StartDate;
        public String EndDate;
        public String RegistrationStartDate;
        public String RegistrationEndDate;

        public SemesterProfile() {
        }

        public SemesterProfile(int Year, String Semester, String Start, String End, String RegistrationStart, String RegistrationEnd) {
            this.Year = Year;
            this.Semester = Semester;
            this.StartDate = Start;
            this.EndDate = End;
            this.RegistrationStartDate = RegistrationStart;
            this.RegistrationEndDate = RegistrationEnd;
        }
    }

    public static void InitTable() throws SQLException {
        String CountSQL = "SELECT COUNT(*) FROM semesters";
        String InsertSQL = "INSERT INTO semesters (SemData, Maintainence, CurrentSem) VALUES (?, ?, ?)";

        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(CountSQL);
             ResultSet Result = Statement.executeQuery()) {

            if (Result.next() && Result.getInt(1) == 0) {
                try (PreparedStatement InsertStatement = DBConnection.prepareStatement(InsertSQL)) {
                    InsertStatement.setString(1, "[]");
                    InsertStatement.setInt(2, 0);
                    InsertStatement.setString(3, "{}");
                    InsertStatement.executeUpdate();
                }
            }
        }
    }

    public static SemesterProfile GetCurrentSemester() throws SQLException {
        String SQL = "SELECT CurrentSem FROM semesters LIMIT 1";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL);
             ResultSet Result = Statement.executeQuery()) {

            if (Result.next()) {
                String JsonData = Result.getString("CurrentSem");
                if (JsonData == null || JsonData.equals("{}") || JsonData.isEmpty()) return null;
                try {
                    return ObjectMapperInstance.readValue(JsonData, SemesterProfile.class);
                }
                catch (Exception ExceptionObject) {
                    ExceptionObject.printStackTrace();
                }
            }
        }
        return null;
    }

    public static boolean UpdateCurrentSemester(SemesterProfile NewSemester) throws SQLException {
        String SQL = "UPDATE semesters SET CurrentSem = CAST(? AS JSON)";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            String JsonString = ObjectMapperInstance.writeValueAsString(NewSemester);
            Statement.setString(1, JsonString);
            return Statement.executeUpdate() > 0;
        }
        catch (Exception ExceptionObject) {
            throw new SQLException("JSON Serialization failed", ExceptionObject);
        }
    }

    public static List<SemesterProfile> GetAllSemesters() throws SQLException {
        String SQL = "SELECT SemData FROM semesters LIMIT 1";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL);
             ResultSet Result = Statement.executeQuery()) {

            if (Result.next()) {
                String JsonData = Result.getString("SemData");
                if (JsonData == null || JsonData.equals("[]")) return new ArrayList<>();
                try {
                    return ObjectMapperInstance.readValue(JsonData, new TypeReference<List<SemesterProfile>>() {});
                } catch (Exception ExceptionObject) {
                    ExceptionObject.printStackTrace();
                }
            }
        }
        return new ArrayList<>();
    }

    public static boolean AddSemesterToHistory(SemesterProfile OldSemester) throws SQLException {
        String SQL = "UPDATE semesters SET SemData = JSON_ARRAY_APPEND(SemData, '$', CAST(? AS JSON))";

        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            String JsonString = ObjectMapperInstance.writeValueAsString(OldSemester);
            Statement.setString(1, JsonString);
            return Statement.executeUpdate() > 0;
        }
        catch (Exception ExceptionObject) {
            throw new SQLException("JSON Serialization failed", ExceptionObject);
        }
    }

    public static boolean IsMaintenanceMode() throws SQLException {
        String SQL = "SELECT Maintainence FROM semesters LIMIT 1";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL);
             ResultSet Result = Statement.executeQuery()) {

            if (Result.next()) {
                return Result.getInt("Maintainence") == 1;
            }
        }
        return false;
    }

    public static boolean SetMaintenanceMode(boolean Enable) throws SQLException {
        String SQL = "UPDATE semesters SET Maintainence = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, Enable ? 1 : 0);
            return Statement.executeUpdate() > 0;
        }
    }
}
