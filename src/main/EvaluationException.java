package main;

/*
    Custom error class used when in the unlikely event that
    an input fails the validation checks made. It moves an
    error message up the call stack to the UI thread where the
    user can be alerted.
 */
public class EvaluationException extends Exception {

    private static final long serialVersionUID = -2461747964902835012L;

    // Returns a new EvaluationException with the error description passed
    public EvaluationException(String ErrorDescription) {
        super(ErrorDescription);
    }
}