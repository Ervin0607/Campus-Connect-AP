package Backend.domain;

public class Registration {
    private int RegistrationNumber;
    private int StudentRollNumber;
    private int OfferingID;
    private String Status;

    public int GetRegistrationNumber()
    {
        return RegistrationNumber;
    }
    public void SetRegistrationNumber(int RegistrationNumber)
    {
        this.RegistrationNumber = RegistrationNumber;
    }
    public int GetStudentRollNumber()
    {
        return StudentRollNumber;
    }
    public void SetStudentRollNumber(int StudentRollNumber)
    {
        this.StudentRollNumber = StudentRollNumber;
    }
    public int GetOfferingID()
    {
        return OfferingID;
    }
    public void SetOfferingID(int OfferingID)
    {
        this.OfferingID = OfferingID;
    }
    public String GetStatus()
    {
        return Status;
    }
    public void SetStatus(String Status)
    {
        this.Status = Status;
    }

}
