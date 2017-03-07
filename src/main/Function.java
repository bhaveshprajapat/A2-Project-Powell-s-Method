package main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

public class Function implements Serializable {
    // Declare fields
    private static final long serialVersionUID = -4936036854404504779L; // Necessary for serialisable objects in Java
    private static String infixExpression;
    private static String postfixExpression;

    // Method converts a given string to RPN
    private static void convertInfixToRPN() {
        Stack<String> operatorStack = new Stack<>();
        StringBuilder outputBuilder = new StringBuilder();
        Function.infixExpression = removeAllSpaces(getInfixExpression());
        String[] uncheckedTokens = tokeniseString(getInfixExpression());
        // Check tokens
        String[] checkedTokens = checkTokens(uncheckedTokens);
        for (String currentToken : checkedTokens) {
            if (checkIfOperand(currentToken)) {
                outputBuilder.append(currentToken).append(",");
            } else if (isLeftParenthesis(currentToken)) {
                operatorStack.push(currentToken);
            } else if (isRightParenthesis(currentToken)) {
                while (!isLeftParenthesis(operatorStack.peek())) {
                    outputBuilder.append(operatorStack.pop()).append(",");
                }
                operatorStack.pop();
            } else if (isOperator(currentToken)) {
                while (true) {
                    if (!operatorStack.isEmpty()) {
                        if (isHigherPrecedence(currentToken, operatorStack.peek())) {
                            outputBuilder.append(operatorStack.pop()).append(",");
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                operatorStack.push(currentToken);
            }
        }
        while (!operatorStack.isEmpty()) {
            outputBuilder.append(operatorStack.pop()).append(",");
        }
        String finalResult = outputBuilder.toString();
        Function.setPostfixExpression(finalResult.substring(0, finalResult.length() - 1));
    }

    // Is the parameterised token a RIGHT PARENTHESIS?
    private static boolean isRightParenthesis(String currentToken) {
        return ")".equals(currentToken);
    }

    // Is the parameterised token an OPERATOR?
    private static boolean isOperator(String currentToken) {
        return currentToken.matches("[*/+\\-\\^(]|s|c|t");
    }

    // Compare the BIDMAS (extended) for each parameterised operator
    private static boolean isHigherPrecedence(String currentToken, String peek) {
        int currentTokenPrecedence = getPrecedenceForToken(currentToken);
        int peekPrecedence = getPrecedenceForToken(peek);
        return peekPrecedence >= currentTokenPrecedence;
    }

    // Return int precedence values for each token
    private static int getPrecedenceForToken(String token) {
        switch (token) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            case "^":
                return 3;
            case "s":
            case "c":
            case "t":
                return 4;
            case "(":
                return -1;
            default:
                return 0;
        }
    }

    // Is the parameterised token a LEFT PARENTHESIS?
    private static boolean isLeftParenthesis(String currentToken) {
        return "(".equals(currentToken);
    }

    // Is the parameterised token an OPERAND?
    private static boolean checkIfOperand(String currentToken) {
        return currentToken.matches("(?:\\d*\\.?\\d+)|[xype]|pi");
    }

    // Split up a string using a Regular Expression to get all of the operands and operators separate.
    private static String[] tokeniseString(String stringToTokenise) {
        return stringToTokenise.split("(?<=[-+^*/()])|(?=[-+*/()^])|(?=[a-z])|(?<=.[a-z])|(?<=[a-z])");
    }

    private static String getPostfixExpression() {
        return Function.postfixExpression;
    }

    public static void setPostfixExpression(String postfixExpression) {
        Function.postfixExpression = postfixExpression;
    }

    public static double evaluate(Coordinate coordinateToUse) throws EvaluationException {
        /*
            Work through each token in the array, pushing
            operands to the stack and popping operands if
            an operator is encountered
         */
        Stack<String> stack = new Stack<>();
        String[] tokens = getPostfixExpression().split(",");
        double numberLeft, numberRight;
        for (String token : tokens) {
            // if the current token is an operator pop items off the stack
            if (isOperator(token)) {
                String stackItemLeft, stackItemRight;
                // pop the first operand off the stack
                stackItemRight = stack.pop();
                try {

                    // attempt to pop a second item off of the stack
                    stackItemLeft = stack.pop();
                } catch (EmptyStackException emptyStackException) {
                    stackItemLeft = "0";
                }
                // Attempt numeric conversion
                try {
                    numberLeft = Double.parseDouble(stackItemLeft);
                } catch (NumberFormatException numberFormatException) {
                    // If the operand isn't a number, it must be a letter representation
                    switch (stackItemLeft) {
                        case "x":
                            numberLeft = coordinateToUse.getXValue();
                            break;
                        case "y":
                            numberLeft = coordinateToUse.getYValue();
                            break;
                        case "e":
                            numberLeft = Math.E;
                            break;
                        case "p":
                            numberLeft = Math.PI;
                            break;
                        default:
                            // a letter has been used that has no known value
                            throw new EvaluationException("Unrecognised operand: " + stackItemLeft);
                    }
                }
                try {
                    // Attempt numeric conversion
                    numberRight = Double.parseDouble(stackItemRight);
                } catch (NumberFormatException e) {
                    // If the operand isn't a number, it must be a letter representation
                    switch (stackItemRight) {
                        case "x":
                            numberRight = coordinateToUse.getXValue();
                            break;
                        case "y":
                            numberRight = coordinateToUse.getYValue();
                            break;
                        case "e":
                            numberRight = Math.E;
                            break;
                        case "p":
                            numberRight = Math.PI;
                            break;
                        default:
                            // a letter has been used that has no known value
                            throw new EvaluationException("Unrecognised operand: " + stackItemLeft);
                    }
                }
                // Perform the operator on the two popped stack items
                switch (token) {
                    case "+":
                        stack.push(String.valueOf(numberLeft + numberRight));
                        break;
                    case "-":
                        stack.push(String.valueOf(numberLeft - numberRight));
                        break;
                    case "*":
                        stack.push(String.valueOf(numberLeft * numberRight));
                        break;
                    case "/":
                        stack.push(String.valueOf(numberLeft / numberRight));
                        break;
                    case "^":
                        stack.push(String.valueOf(Math.pow(numberLeft, numberRight)));
                        break;
                    case "s":
                        stack.push(String.valueOf(Math.pow(Math.sin(numberLeft), numberRight)));
                        break;
                    case "c":
                        stack.push(String.valueOf(Math.pow(Math.cos(numberLeft), numberRight)));
                        break;
                    case "t":
                        stack.push(String.valueOf(Math.pow(Math.tan(numberLeft), numberRight)));
                        break;
                }
            } else {
                // current item is a operand, so push it to the stack
                stack.push(token);
            }
        }
        // return the evaluation
        return Double.valueOf(stack.pop());
    }

    // Accessor method for the infix expression
    public static String getInfixExpression() {
        return Function.infixExpression;
    }

    // Mutator method for the infix expression
    public static void setInfixExpression(String infixExpression) {
        Function.infixExpression = infixExpression;
        convertInfixToRPN();
    }

    // This private method checks a String array and moves minus signs if they are unary
    private static String[] checkTokens(String[] uncheckedTokenArray) {
        ArrayList<String> checkedTokensList = new ArrayList<>();
        // Since unary minus could be counted as an operator, we need to check for it
        for (int index = 0; index < uncheckedTokenArray.length; index++) {
            if (index > 0) {
                // Two consecutive operators test
                if ("-".equals(uncheckedTokenArray[index]) && isOperator(uncheckedTokenArray[index - 1])) {
                    uncheckedTokenArray[index] = "";
                    uncheckedTokenArray[index + 1] = "-" + uncheckedTokenArray[index + 1];
                    // Current token is obsolete
                    continue;
                }
            }
            if (index == 0) {
                // '-' at start test
                if ("-".equals(uncheckedTokenArray[index])) {
                    uncheckedTokenArray[index + 1] = "-" + uncheckedTokenArray[index + 1];
                    // Current token is obsolete
                    checkedTokensList.add(uncheckedTokenArray[index]);
                    continue;
                }
            }
            checkedTokensList.add(uncheckedTokenArray[index]);
        }
        return checkedTokensList.toArray(new String[0]);
    }

    // Removes all spaces in a String
    private static String removeAllSpaces(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == ' ') {
                input = new StringBuilder(input).deleteCharAt(i).toString();
            }
        }
        return input;
    }
}