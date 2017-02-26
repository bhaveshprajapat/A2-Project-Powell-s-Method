package main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

class Function implements Serializable {
    // Declare fields
    private static final long serialVersionUID = -4936036854404504779L; // Necessary for serialisable objects in Java
    private static String infixExpression;
    private static String postfixExpression;

    // Method converts a given string to RPN
    private void convertInfixToRPN() {
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
    private boolean isRightParenthesis(String currentToken) {
        return currentToken.equals(")");
    }

    // Is the parameterised token an OPERATOR?
    private boolean isOperator(String currentToken) {
        return currentToken.matches("[*/+\\-\\^(]|s|c|t");
    }

    // Compare the BIDMAS (extended) for each parameterised operator
    private boolean isHigherPrecedence(String currentToken, String peek) {
        int currentTokenPrecedence = getPrecedenceForToken(currentToken);
        int peekPrecedence = getPrecedenceForToken(peek);
        return peekPrecedence >= currentTokenPrecedence;
    }

    // Return int precedence values for each
    private int getPrecedenceForToken(String token) {
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
    private boolean isLeftParenthesis(String currentToken) {
        return currentToken.equals("(");
    }

    // Is the parameterised token an OPERAND?
    private boolean checkIfOperand(String currentToken) {
        return currentToken.matches("(?:\\d*\\.?\\d+)|[xype]|pi");
    }

    // Split up a string using a Regular Expression to get all of the operands and operators separate.
    private String[] tokeniseString(String stringToTokenise) {
        return stringToTokenise.split("(?<=[-+^*/()])|(?=[-+*/()^])|(?=[a-z])|(?<=.[a-z])|(?<=[a-z])");
    }

    private String getPostfixExpression() {
        return Function.postfixExpression;
    }

    public static void setPostfixExpression(String postfixExpression) {
        Function.postfixExpression = postfixExpression;
    }

    public double evaluate(Coordinate coordinateToUse) throws EvaluationException {
        /*
            Work through each token in the array, pushing
            operands to the stack and popping operands if
            an operator is encountered
         */
        Stack<String> stack = new Stack<>();
        String[] tokens = getPostfixExpression().split(",");
        for (String token : tokens) {
            switch (token) {
                case "+":
                case "-":
                case "/":
                case "*":
                case "^":
                case "s":
                case "c":
                case "t":
                    String stackItemLeft, stackItemRight;
                    stackItemRight = stack.pop();
                    try {
                        stackItemLeft = stack.pop();
                    } catch (EmptyStackException emptyStackException) {
                        stackItemLeft = "0";
                    }
                    double numberLeft, numberRight;
                    try {
                        numberLeft = Double.parseDouble(stackItemLeft);
                    } catch (NumberFormatException numberFormatException) {
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
                                throw new EvaluationException("Unrecognised operand: " + stackItemLeft);
                        }
                    }
                    try {
                        numberRight = Double.parseDouble(stackItemRight);
                    } catch (NumberFormatException e) {
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
                                throw new EvaluationException("Unrecognised operand: " + stackItemLeft);
                        }
                    }
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
                    break;
                default:
                    stack.push(token);
            }
        }
        return Double.valueOf(stack.pop());
    }

    // Accessor method for the infix expression
    public String getInfixExpression() {
        return Function.infixExpression;
    }

    // Mutator method for the infix expression
    public void setInfixExpression(String infixExpression) {
        Function.infixExpression = infixExpression;
        convertInfixToRPN();
    }

    // This private method checks a String array and moves minus signs if they are unary
    private String[] checkTokens(String[] uncheckedTokenArray) {
        ArrayList<String> checkedTokensList = new ArrayList<>();
        // Since unary minus could be counted as an operator, we need to check for it
        for (int index = 0; index < uncheckedTokenArray.length; index++) {
            if (index > 0) {
                // Two consecutive operators test
                if (uncheckedTokenArray[index].equals("-") && isOperator(uncheckedTokenArray[index - 1])) {
                    uncheckedTokenArray[index] = "";
                    uncheckedTokenArray[index + 1] = "-" + uncheckedTokenArray[index + 1];
                    // Current token is obsolete
                    continue;
                }
            }
            if (index == 0) {
                // '-' at start test
                if (uncheckedTokenArray[index].equals("-")) {
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
    private String removeAllSpaces(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == ' ') {
                input = new StringBuilder(input).deleteCharAt(i).toString();
            }
        }
        return input;
    }
}