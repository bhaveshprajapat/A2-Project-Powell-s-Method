package main;

/*
    Custom error class is thrown in custom threaded classes
    which allows going up the call stack and purging memory
    used if unintentional behavior were to occur.
 */
public class EvaluationException extends Exception {
    private static final long serialVersionUID = -2461747964902835012L;

    // Returns a new EvaluationException with the ErrorDescription
    public EvaluationException(String ErrorDescription) {
        // message moved up the call stack to the UI thread for displaying error
        super(ErrorDescription);
    }
}