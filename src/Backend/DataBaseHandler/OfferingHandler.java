package Backend.DataBaseHandler;

import Backend.domain.Offerings;
import Backend.util.DB;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;

import java.sql.*;
import java.util.*;

public class OfferingHandler {

    public static int GetMaxOfferingID() throws SQLException {
        String SQL = "SELECT COALESCE(MAX(OfferingID), 0) AS maxid FROM offerings";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL);
             ResultSet Result = Statement.executeQuery()) {
            if (Result.next()) return Result.getInt("maxid");
            return 0;
        }
    }

    public static Offerings FindOffering(int OfferingID) throws SQLException {

        String SQL = "SELECT * FROM offerings WHERE OfferingID = ?";

        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, OfferingID);

            try (ResultSet Result = Statement.executeQuery()) {
                if (Result.next()) {
                    Offerings CurrentOffering = new Offerings();
                    CurrentOffering.SetCapacity(Result.getInt("Capacity"));
                    CurrentOffering.SetOfferingID(Result.getInt("OfferingID"));
                    CurrentOffering.SetCourseID(Result.getInt("CourseID"));
                    CurrentOffering.SetYear(Result.getInt("Year"));
                    CurrentOffering.SetSemester(Result.getString("Semester"));
                    CurrentOffering.SetInstructorID(Result.getInt("InstructorID"));
                    CurrentOffering.SetCurrentEnrollment(Result.getInt("CurrentEnrollment"));
                    CurrentOffering.SetAnnouncements(Result.getString("Announcements"));

                    ObjectMapper ObjectMapperInstance = new ObjectMapper();

                    String gradeComponentsJson = Result.getString("GradeComponents");
                    if (gradeComponentsJson == null || gradeComponentsJson.isBlank()) {
                        gradeComponentsJson = "{}";
                    }
                    HashMap<String, Integer> GradingComponentsMap =
                            ObjectMapperInstance.readValue(gradeComponentsJson, new TypeReference<HashMap<String, Integer>>() {});
                    if (GradingComponentsMap == null) {
                        GradingComponentsMap = new HashMap<>();
                    }
                    CurrentOffering.SetGradingComponents(GradingComponentsMap);

                    String gradeSlabsJson = Result.getString("GradeSlabs");
                    if (gradeSlabsJson == null || gradeSlabsJson.isBlank()) {
                        gradeSlabsJson = "{}";
                    }
                    HashMap<String, Integer> GradeSlabsMap =
                            ObjectMapperInstance.readValue(gradeSlabsJson, new TypeReference<HashMap<String, Integer>>() {});
                    if (GradeSlabsMap == null) {
                        GradeSlabsMap = new HashMap<>();
                    }
                    else {
                        for (Map.Entry<String, Integer> e : GradeSlabsMap.entrySet()) {
                            if (e.getValue() == null) {
                                e.setValue(0);
                            }
                        }
                    }
                    CurrentOffering.SetGradingSlabs(GradeSlabsMap);

                    CurrentOffering.SetLectureSchedule(Result.getString("LectureSchedule"));
                    CurrentOffering.SetLabSchedule(Result.getString("LabSchedule"));

                    return CurrentOffering;
                }
            }
            catch (JsonProcessingException Exception) {
                throw new RuntimeException(Exception);
            }
        }
        return null;
    }


    public static boolean DeleteOffering(int OfferingID) throws SQLException {
        String SQL = "DELETE FROM offerings WHERE OfferingID = ?";

        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, OfferingID);
            return Statement.executeUpdate() > 0;
        }
    }

    public static boolean UpdateOffering(Offerings CurrentOffering) throws SQLException {
        String SQL = "UPDATE offerings SET " +
                "Capacity = ?, Semester = ?, OfferingID = ?, Year = ?, InstructorID = ?, " +
                "CurrentEnrollment = ?, LectureSchedule = ?, LabSchedule = ?, Announcements = ? " +
                "WHERE OfferingID = ?";

        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, CurrentOffering.GetCapacity());
            Statement.setString(2, CurrentOffering.GetSemester());
            Statement.setInt(3, CurrentOffering.GetOfferingID());
            Statement.setInt(4, CurrentOffering.GetYear());
            Statement.setInt(5, CurrentOffering.GetInstructorID());
            Statement.setInt(6, CurrentOffering.GetCurrentEnrollment());

            Statement.setString(7, CurrentOffering.GetLectureSchedule());
            Statement.setString(8, CurrentOffering.GetLabSchedule());
            String Announcements = CurrentOffering.GetAnnouncements();
            if (Announcements == null || Announcements.isEmpty()) Announcements = "[]";
            Statement.setString(9, Announcements);

            Statement.setInt(10, CurrentOffering.GetOfferingID());

            return Statement.executeUpdate() > 0;
        }
    }

    public static List<Offerings> GetAllOfferings() throws SQLException {
        String SQL = "SELECT * FROM offerings";
        List<Offerings> AllOfferings = new ArrayList<>();
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL);
             ResultSet Result = Statement.executeQuery()) {

            ObjectMapper ObjectMapperInstance = new ObjectMapper();

            while (Result.next()) {
                Offerings CurrentOffering = new Offerings();
                CurrentOffering.SetCapacity(Result.getInt("Capacity"));
                CurrentOffering.SetOfferingID(Result.getInt("OfferingID"));
                CurrentOffering.SetCourseID(Result.getInt("CourseID"));
                CurrentOffering.SetYear(Result.getInt("Year"));
                CurrentOffering.SetSemester(Result.getString("Semester"));
                CurrentOffering.SetInstructorID(Result.getInt("InstructorID"));
                CurrentOffering.SetCurrentEnrollment(Result.getInt("CurrentEnrollment"));
                CurrentOffering.SetAnnouncements(Result.getString("Announcements"));

                String gradeComponentsJson = Result.getString("GradeComponents");
                if (gradeComponentsJson == null || gradeComponentsJson.isBlank()) {
                    gradeComponentsJson = "{}";
                }
                HashMap<String, Integer> GradingComponentsMap =
                        ObjectMapperInstance.readValue(gradeComponentsJson, new TypeReference<HashMap<String, Integer>>() {});
                if (GradingComponentsMap == null) {
                    GradingComponentsMap = new HashMap<>();
                }
                CurrentOffering.SetGradingComponents(GradingComponentsMap);

                String gradeSlabsJson = Result.getString("GradeSlabs");
                if (gradeSlabsJson == null || gradeSlabsJson.isBlank()) {
                    gradeSlabsJson = "{}";
                }
                HashMap<String, Integer> GradeSlabsMap =
                        ObjectMapperInstance.readValue(gradeSlabsJson, new TypeReference<HashMap<String, Integer>>() {});
                if (GradeSlabsMap == null) {
                    GradeSlabsMap = new HashMap<>();
                }
                else {
                    for (Map.Entry<String, Integer> e : GradeSlabsMap.entrySet()) {
                        if (e.getValue() == null) {
                            e.setValue(0);
                        }
                    }
                }
                CurrentOffering.SetGradingSlabs(GradeSlabsMap);

                CurrentOffering.SetLectureSchedule(Result.getString("LectureSchedule"));
                CurrentOffering.SetLabSchedule(Result.getString("LabSchedule"));

                AllOfferings.add(CurrentOffering);
            }
        }
        catch (JsonProcessingException Exception) {
            throw new RuntimeException(Exception);
        }
        AllOfferings.sort(Comparator.comparing(Offerings::GetOfferingID));
        return AllOfferings;
    }


    public static boolean AddOfferings(Offerings CurrentOffering) throws SQLException {
        String SQL = "INSERT INTO offerings (" +
                "Capacity, Semester, OfferingID, Year, InstructorID, CourseID, " +
                "CurrentEnrollment, GradeComponents, GradeSlabs, LectureSchedule, LabSchedule, Announcements" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, CurrentOffering.GetCapacity());
            Statement.setString(2, CurrentOffering.GetSemester());
            Statement.setInt(3, CurrentOffering.GetOfferingID());
            Statement.setInt(4, CurrentOffering.GetYear());
            Statement.setInt(5, CurrentOffering.GetInstructorID());
            Statement.setInt(6, CurrentOffering.GetCourseID());
            Statement.setInt(7, CurrentOffering.GetCurrentEnrollment());

            Statement.setString(8, "{}");
            Statement.setString(9, "{\"A+\": 90, \"A-\": 80, \"B+\": 70, \"B-\": 60, \"C+\": 50, \"C-\": 40, \"D+\": 30, \"D-\": 20}");

            Statement.setString(10, CurrentOffering.GetLectureSchedule());
            Statement.setString(11, CurrentOffering.GetLabSchedule());
            String Announcements = CurrentOffering.GetAnnouncements();
            if (Announcements == null || Announcements.isEmpty()) Announcements = "[]";
            Statement.setString(12, Announcements);

            return Statement.executeUpdate() > 0;
        }
    }


    public static Offerings FindOfferingByID(int OfferingID) throws SQLException {
        String SQL = "SELECT OfferingID, CourseID, InstructorID, Semester, Year, Capacity, CurrentEnrollment, GradeComponents, GradeSlabs, LectureSchedule, LabSchedule FROM offerings WHERE OfferingID=?";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {
            Statement.setInt(1, OfferingID);
            try (ResultSet Result = Statement.executeQuery()) {
                if (Result.next()) {
                    Offerings CurrentOffering = new Offerings();
                    CurrentOffering.SetOfferingID(Result.getInt("OfferingID"));
                    CurrentOffering.SetCourseID(Result.getInt("CourseID"));
                    CurrentOffering.SetInstructorID(Result.getInt("InstructorID"));
                    CurrentOffering.SetSemester(Result.getString("Semester"));
                    CurrentOffering.SetYear(Result.getInt("Year"));
                    CurrentOffering.SetCapacity(Result.getInt("Capacity"));
                    CurrentOffering.SetCurrentEnrollment(Result.getInt("CurrentEnrollment"));

                    CurrentOffering.SetLectureSchedule(Result.getString("LectureSchedule"));
                    CurrentOffering.SetLabSchedule(Result.getString("LabSchedule"));
                    CurrentOffering.SetAnnouncements(Result.getString("Announcements"));

                    ObjectMapper ObjectMapperInstance = new ObjectMapper();
                    HashMap<String, Integer> GradingComponentsMap =
                            ObjectMapperInstance.readValue(Result.getString("GradeComponents"), new TypeReference<>() {});
                    CurrentOffering.SetGradingComponents(GradingComponentsMap);

                    HashMap<String, Integer> GradeSlabsMap =
                            ObjectMapperInstance.readValue(Result.getString("GradeSlabs"), new TypeReference<>() {});
                    CurrentOffering.SetGradingSlabs(GradeSlabsMap);

                    CurrentOffering.SetLectureSchedule(Result.getString("LectureSchedule"));
                    CurrentOffering.SetLabSchedule(Result.getString("LabSchedule"));
                    return CurrentOffering;
                }
            }
            catch (JsonMappingException Exception) {
                throw new RuntimeException(Exception);
            }
            catch (JsonProcessingException Exception) {
                throw new RuntimeException(Exception);
            }
        }
        return null;
    }

    public static List<Offerings> FindOfferingsByIDs(List<Integer> OfferingIDList) throws SQLException {
        List<Offerings> OfferingList = new ArrayList<>();
        if (OfferingIDList == null || OfferingIDList.isEmpty()) return OfferingList;
        String QueryString = String.join(",", java.util.Collections.nCopies(OfferingIDList.size(), "?"));
        String SQL = "SELECT OfferingID, CourseID, InstructorID, Semester, Year, Capacity, CurrentEnrollment, LectureSchedule, LabSchedule FROM offerings WHERE OfferingID IN (" + QueryString + ")";
        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {
            int Index = 1;
            for (Integer CurrentOfferingID : OfferingIDList) {
                Statement.setInt(Index++, CurrentOfferingID);
            }
            try (ResultSet Result = Statement.executeQuery()) {
                while (Result.next()) {
                    Offerings CurrentOffering = new Offerings();
                    CurrentOffering.SetOfferingID(Result.getInt("OfferingID"));
                    CurrentOffering.SetCourseID(Result.getInt("CourseID"));
                    CurrentOffering.SetInstructorID(Result.getInt("InstructorID"));
                    CurrentOffering.SetSemester(Result.getString("Semester"));
                    CurrentOffering.SetYear(Result.getInt("Year"));
                    CurrentOffering.SetCapacity(Result.getInt("Capacity"));
                    CurrentOffering.SetCurrentEnrollment(Result.getInt("CurrentEnrollment"));

                    CurrentOffering.SetLectureSchedule(Result.getString("LectureSchedule"));
                    CurrentOffering.SetLabSchedule(Result.getString("LabSchedule"));

                    OfferingList.add(CurrentOffering);
                }
            }
        }
        return OfferingList;
    }

    public static List<Map<String, String>> GetAnnouncementsForOffering(int OfferingID) throws SQLException {
        String SQL = "SELECT Announcements FROM offerings WHERE OfferingID = ?";

        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            Statement.setInt(1, OfferingID);

            try (ResultSet Result = Statement.executeQuery()) {
                if (Result.next()) {
                    String JsonData = Result.getString("Announcements");
                    if (JsonData == null || JsonData.isEmpty()) {
                        JsonData = "[]";
                    }

                    ObjectMapper ObjectMapperInstance = new ObjectMapper();
                    try {
                        return ObjectMapperInstance.readValue(
                                JsonData,
                                new TypeReference<List<Map<String, String>>>() {}
                        );
                    }
                    catch (JsonProcessingException Exception) {
                        throw new RuntimeException(Exception);
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    public static boolean SaveAnnouncementsForOffering(int OfferingID, List<Map<String, String>> AnnouncementList) throws SQLException {
        String SQL = "UPDATE offerings SET Announcements = ? WHERE OfferingID = ?";

        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            if (AnnouncementList == null) {
                AnnouncementList = new ArrayList<>();
            }

            ObjectMapper ObjectMapperInstance = new ObjectMapper();
            String JsonData;
            try {
                JsonData = ObjectMapperInstance.writeValueAsString(AnnouncementList);
            }
            catch (JsonProcessingException Exception) {
                throw new RuntimeException(Exception);
            }

            Statement.setString(1, JsonData);
            Statement.setInt(2, OfferingID);

            return Statement.executeUpdate() > 0;
        }
    }

    public static boolean AddAnnouncementToOffering(int OfferingID, String DateDDMMYYYY, String Message) throws SQLException {
        List<Map<String, String>> AnnouncementList = GetAnnouncementsForOffering(OfferingID);
        if (AnnouncementList == null) AnnouncementList = new ArrayList<>();

        Map<String, String> NewAnnouncement = new HashMap<>();
        NewAnnouncement.put("Date", DateDDMMYYYY);
        NewAnnouncement.put("Data", Message);

        AnnouncementList.add(NewAnnouncement);

        return SaveAnnouncementsForOffering(OfferingID, AnnouncementList);
    }

    public static boolean UpdateGradingComponent(int OfferingID, HashMap<String, Integer> Components) throws SQLException {
        String SQL = "UPDATE offerings SET GradeComponents = ? WHERE OfferingID = ?";

        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            ObjectMapper ObjectMapperInstance = new ObjectMapper();
            String JsonData = ObjectMapperInstance.writeValueAsString(Components);
            Statement.setString(1, JsonData);
            Statement.setInt(2, OfferingID);

            return Statement.executeUpdate() > 0;
        }
        catch (JsonProcessingException Exception) {
            throw new RuntimeException(Exception);
        }
    }

    public static boolean UpdateGradeSlabs(int OfferingID, HashMap<String, Integer> Slabs) throws SQLException {
        String SQL = "UPDATE offerings SET GradeSlabs = ? WHERE OfferingID = ?";

        try (Connection DBConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement Statement = DBConnection.prepareStatement(SQL)) {

            ObjectMapper ObjectMapperInstance = new ObjectMapper();
            String JsonData = ObjectMapperInstance.writeValueAsString(Slabs);

            Statement.setString(1, JsonData);
            Statement.setInt(2, OfferingID);

            return Statement.executeUpdate() > 0;
        }
        catch (JsonProcessingException Exception) {
            throw new RuntimeException(Exception);
        }
    }
}