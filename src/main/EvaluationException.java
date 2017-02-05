package main;

/*
    Custom error class to allow moving up the call stack when an evaluation fails.
 */
public class EvaluationException extends Exception {
    public EvaluationException(String s) {
        super(s);
    }
}