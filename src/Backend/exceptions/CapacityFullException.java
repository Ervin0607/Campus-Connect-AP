package Backend.exceptions;

public class CapacityFullException extends RuntimeException {
    public CapacityFullException(String message) {
        super(message);
    }
}
