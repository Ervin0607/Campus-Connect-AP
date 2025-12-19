package Backend.DataBaseHandler;

import Backend.domain.StudentGradeRecord;
import Backend.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StudentGradeRecordHandler {

    public static java.util.List<StudentGradeRecord> FindByRollNumber(int RollNumber) throws SQLException {
        String SQL = "SELECT * FROM studentgraderecord WHERE RollNumber = ?";
        java.util.List<StudentGradeRecord> GradeRecordList = new java.util.ArrayList<>();

        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, RollNumber);

            try (ResultSet Result = Statement.executeQuery()) {
                while (Result.next()) {
                    StudentGradeRecord CurrentGrade = new StudentGradeRecord();
                    CurrentGrade.setOfferingID(Result.getInt("OfferingID"));
                    CurrentGrade.setRollNumber(Result.getInt("RollNumber"));

                    ObjectMapper ObjectMapperInstance = new ObjectMapper();
                    HashMap<String, Integer> GradeMap =
                            ObjectMapperInstance.readValue(Result.getString("Grade"), new TypeReference<>() {});
                    CurrentGrade.setGrade(GradeMap);

                    GradeRecordList.add(CurrentGrade);
                }
            }
            catch (JsonMappingException ExceptionObject) {
                throw new RuntimeException(ExceptionObject);
            }
            catch (JsonProcessingException ExceptionObject) {
                throw new RuntimeException(ExceptionObject);
            }
        }
        return GradeRecordList;
    }

    public static java.util.List<StudentGradeRecord> FindByOfferingID(int OfferingID) throws SQLException {
        String SQL = "SELECT * FROM studentgraderecord WHERE OfferingID = ?";
        java.util.List<StudentGradeRecord> GradeRecordList = new java.util.ArrayList<>();

        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, OfferingID);

            try (ResultSet Result = Statement.executeQuery()) {
                while (Result.next()) {
                    StudentGradeRecord CurrentGrade = new StudentGradeRecord();
                    CurrentGrade.setOfferingID(Result.getInt("OfferingID"));
                    CurrentGrade.setRollNumber(Result.getInt("RollNumber"));

                    ObjectMapper ObjectMapperInstance = new ObjectMapper();
                    HashMap<String, Integer> GradeMap =
                            ObjectMapperInstance.readValue(Result.getString("Grade"), new TypeReference<>() {});
                    CurrentGrade.setGrade(GradeMap);

                    GradeRecordList.add(CurrentGrade);
                }
            }
            catch (JsonMappingException ExceptionObject) {
                throw new RuntimeException(ExceptionObject);
            }
            catch (JsonProcessingException ExceptionObject) {
                throw new RuntimeException(ExceptionObject);
            }
        }
        return GradeRecordList;
    }

    public static StudentGradeRecord GetStudentGradeRecord(int OfferingID, int RollNumber) throws SQLException {
        String SQL = "SELECT * FROM studentgraderecord WHERE OfferingID = ? AND RollNumber = ?";

        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, OfferingID);
            Statement.setInt(2, RollNumber);

            try (ResultSet Result = Statement.executeQuery()) {
                if (Result.next()) {
                    StudentGradeRecord CurrentGrade = new StudentGradeRecord();
                    CurrentGrade.setOfferingID(OfferingID);
                    CurrentGrade.setRollNumber(RollNumber);

                    ObjectMapper ObjectMapperInstance = new ObjectMapper();
                    HashMap<String, Integer> GradeMap =
                            ObjectMapperInstance.readValue(Result.getString("Grade"), new TypeReference<>() {});
                    CurrentGrade.setGrade(GradeMap);

                    return CurrentGrade;
                }
            }
            catch (JsonMappingException ExceptionObject) {
                throw new RuntimeException(ExceptionObject);
            }
            catch (JsonProcessingException ExceptionObject) {
                throw new RuntimeException(ExceptionObject);
            }
        }
        return null;
    }

    public static boolean DeleteGradeRecord(int OfferingID, int RollNumber) throws SQLException {
        String SQL = "DELETE FROM studentgraderecord WHERE OfferingID = ? AND RollNumber = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, OfferingID);
            Statement.setInt(2, RollNumber);
            return Statement.executeUpdate() > 0;
        }
    }

    public static boolean UpdateGradeRecord(int OfferingID, int RollNumber, HashMap<String, Integer> NewGrades) throws SQLException {
        String SQL = "UPDATE studentgraderecord SET Grade = ? WHERE OfferingID = ? AND RollNumber = ?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            ObjectMapper ObjectMapperInstance = new ObjectMapper();
            String JsonData = ObjectMapperInstance.writeValueAsString(NewGrades);

            Statement.setString(1, JsonData);
            Statement.setInt(2, OfferingID);
            Statement.setInt(3, RollNumber);

            return Statement.executeUpdate() > 0;
        }
        catch (JsonProcessingException ExceptionObject) {
            throw new RuntimeException(ExceptionObject);
        }
    }

    public static boolean AddGradeRecord(StudentGradeRecord NewGradeRecord) throws SQLException {
        String SQL = "INSERT INTO studentgraderecord (OfferingID, RollNumber, Grade) VALUES (?, ?, ?)";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, NewGradeRecord.getOfferingID());
            Statement.setInt(2, NewGradeRecord.getRollNumber());

            ObjectMapper ObjectMapperInstance = new ObjectMapper();
            String JsonData = ObjectMapperInstance.writeValueAsString(NewGradeRecord.getGrade());
            Statement.setString(3, JsonData);

            return Statement.executeUpdate() > 0;
        }
        catch (JsonProcessingException ExceptionObject) {
            throw new RuntimeException(ExceptionObject);
        }
    }
}
