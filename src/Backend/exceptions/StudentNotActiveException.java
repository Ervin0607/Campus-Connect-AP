package Backend.exceptions;

public class StudentNotActiveException extends RuntimeException {
    public StudentNotActiveException(String message) { super(message); }
}
