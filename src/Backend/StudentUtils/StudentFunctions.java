package Backend.StudentUtils;

import Backend.DataBaseHandler.RegistrationHandler;
import Backend.DataBaseHandler.UserAuthenticationHandler;
import Backend.domain.Registration;
import Backend.domain.Student;
import Backend.domain.User;
import Backend.exceptions.StudentNotActiveException;

import java.time.Year;



public class StudentFunctions {

    private static int GlobalRegistrationSeq = 0;

    private static synchronized int NextRegistrationNumber() {
        int year = Year.now().getValue();
        GlobalRegistrationSeq += 1;
        if (GlobalRegistrationSeq > 9999) GlobalRegistrationSeq = 1;
        return year * 10000 + GlobalRegistrationSeq;
    }

    public boolean AddRegistration(Student currentStudent, int offeringID) {
        try {
            if (currentStudent == null) return false;

            int UserID = currentStudent.GetUserID();
            User NewUser = UserAuthenticationHandler.FindUserID(UserID);
            String Status = NewUser.GetStatus();
            if (Status == null || !"ACTIVE".equalsIgnoreCase(Status)) {
                throw new StudentNotActiveException("Cannot register for the course: student is not ACTIVE");
            }

            Registration NewRegistration = new Registration();
            NewRegistration.SetRegistrationNumber(NextRegistrationNumber());
            NewRegistration.SetOfferingID(offeringID);
            NewRegistration.SetStatus("ONGOING");
            NewRegistration.SetStudentRollNumber(currentStudent.GetRollNumber());

            return new RegistrationHandler().AddRegistration(NewRegistration);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
