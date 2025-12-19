package Backend.DataBaseHandler;

import Backend.domain.Notification;
import Backend.util.DB;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationHandler {

    private static final ObjectMapper JsonMapper = new ObjectMapper();

    public static List<Notification> GetNotifications(int RollNumber) throws SQLException {
        String QueryString = "SELECT NotificationData FROM StudentNotifications WHERE StudentRollNumber = ?";

        try (Connection DbConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement PreparedStatementObject = DbConnection.prepareStatement(QueryString)) {

            PreparedStatementObject.setInt(1, RollNumber);
            ResultSet ResultSetObject = PreparedStatementObject.executeQuery();

            if (ResultSetObject.next()) {
                String JsonData = ResultSetObject.getString("NotificationData");

                if (JsonData == null || JsonData.isEmpty() || JsonData.equals("null")) {
                    return new ArrayList<>();
                }

                try {
                    List<Notification> ListOfNotifications = JsonMapper.readValue(JsonData, new TypeReference<>() {});
                    return ListOfNotifications;

                } catch (JsonProcessingException ExceptionObject) {
                    ExceptionObject.printStackTrace();
                    return new ArrayList<>();
                }
            }
        }
        return new ArrayList<>();
    }

    public static synchronized void AddNotification(int RollNumber, Notification NewNotification) throws SQLException {
        List<Notification> CurrentList = GetNotifications(RollNumber);
        CurrentList.add(0, NewNotification);

        String UpdatedJson;
        try {
            UpdatedJson = JsonMapper.writeValueAsString(CurrentList);
        } catch (JsonProcessingException ExceptionObject) {
            throw new RuntimeException("Failed to serialize notification data", ExceptionObject);
        }

        String UpsertQueryString = """
            INSERT INTO StudentNotifications (StudentRollNumber, NotificationData) 
            VALUES (?, ?) 
            ON DUPLICATE KEY UPDATE NotificationData = VALUES(NotificationData)
        """;

        try (Connection DbConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement PreparedStatementObject = DbConnection.prepareStatement(UpsertQueryString)) {

            PreparedStatementObject.setInt(1, RollNumber);
            PreparedStatementObject.setString(2, UpdatedJson);
            PreparedStatementObject.executeUpdate();
        }
    }

    public static void ClearAll(int RollNumber) throws SQLException {
        String QueryString = """
            INSERT INTO StudentNotifications (StudentRollNumber, NotificationData)
            VALUES (?, '[]')
            ON DUPLICATE KEY UPDATE NotificationData = '[]'
        """;

        try (Connection DbConnection = DB.GetERPDataSource().getConnection();
             PreparedStatement PreparedStatementObject = DbConnection.prepareStatement(QueryString)) {

            PreparedStatementObject.setInt(1, RollNumber);
            PreparedStatementObject.executeUpdate();
        }
    }
}
