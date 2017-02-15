package main;

/*
    Custom error class to allow moving up the call stack when an evaluation fails.
 */
public class EvaluationException extends Exception {
    private static final long serialVersionUID = -2461747964902835012L;

    public EvaluationException(String s) {
        super(s);
    }
}