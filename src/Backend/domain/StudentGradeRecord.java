package Backend.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StudentGradeRecord {
    private int OfferingID;
    private int RollNumber;
    private HashMap<String, Integer> Grade;

    public int getOfferingID() {return OfferingID;}
    public void setOfferingID(int OfferingID) {this.OfferingID = OfferingID;}

    public int getRollNumber() {return RollNumber;}
    public void setRollNumber(int RollNumber) {this.RollNumber = RollNumber;}

    public HashMap<String, Integer> getGrade() {
        return Grade;
    }
    public void setGrade(HashMap<String, Integer> Grade) {
        this.Grade = Grade;
    }
}
