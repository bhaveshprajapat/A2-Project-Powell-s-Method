package main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;

class Function implements Serializable {
    private static final long serialVersionUID = -4936036854404504779L;
    private static String infixExpression;
    private static String postfixExpression;

    public void convertInfixToPostfix() {
        Function.postfixExpression = convert(Function.infixExpression);
    }

    public double outputFOfXY(Coordinate coordinateToUse) throws EvaluationException {
        return parse(Function.postfixExpression, coordinateToUse.getXValue(), coordinateToUse.getYValue());
    }

    // private method converts a given string to RPN
    private String convert(String infix) {
        Stack<String> operatorStack = new Stack<>();
        StringBuilder outputBuilder = new StringBuilder();
        infix = removeAllSpaces(infix);
        String[] uncheckedTokens = tokeniseString(infix);
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
        return finalResult.substring(0, finalResult.length() - 1);
    }

    private boolean isRightParenthesis(String currentToken) {
        return currentToken.equals(")");
    }

    private boolean isOperator(String currentToken) {
        return currentToken.matches("[*/+\\-\\^(]|s|c|t");
    }

    private boolean isHigherPrecedence(String currentToken, String peek) {
        HashMap<String, Integer> operationPrecedenceMap = new HashMap<>();
        operationPrecedenceMap.put("+", 1);
        operationPrecedenceMap.put("-", 1);
        operationPrecedenceMap.put("*", 2);
        operationPrecedenceMap.put("/", 2);
        operationPrecedenceMap.put("^", 3);
        operationPrecedenceMap.put("s", 4);
        operationPrecedenceMap.put("c", 4);
        operationPrecedenceMap.put("t", 4);
        operationPrecedenceMap.put("(", -1);
        int currentTokenPrecedence = operationPrecedenceMap.get(currentToken);
        int peekPrecedence = operationPrecedenceMap.get(peek);
        return peekPrecedence >= currentTokenPrecedence;
    }

    private boolean isLeftParenthesis(String currentToken) {
        return currentToken.equals("(");
    }

    private boolean checkIfOperand(String currentToken) {
        return currentToken.matches("(?:\\d*\\.?\\d+)|[xype]|pi");
    }

    private String[] tokeniseString(String stringToTokenise) {
        return stringToTokenise.split("(?<=[-+^*/()])|(?=[-+*/()^])|(?=[a-z])|(?<=.[a-z])|(?<=[a-z])");
    }

    private double parse(String RPNToParse, double x, double y) throws EvaluationException {
        // Test if the string is equal to an empty string
        if (RPNToParse.equals("")) {
            throw new EvaluationException("RPN String cannot be empty");
        }
        /*
            Work through each token in the array, pushing
            operands to the stack and popping operands if
            an operator is encountered
         */
        Stack<String> stack = new Stack<>();
        String[] tokens = RPNToParse.split(",");
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
                                numberLeft = x;
                                break;
                            case "y":
                                numberLeft = y;
                                break;
                            case "e":
                                numberLeft = Math.E;
                                break;
                            case "pi":
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
                                numberRight = x;
                                break;
                            case "y":
                                numberRight = y;
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

    String getInfixExpression() {
        return Function.infixExpression;
    }

    void setInfixExpression(String algorithmString) {
        Function.infixExpression = algorithmString;
    }

    String[] checkTokens(String[] uncheckedTokenArray) {
        ArrayList<String> checkedTokensList = new ArrayList<>();
        // Since unary minus could be counted as an operator, we need to check
        // for it
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

    String removeAllSpaces(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == ' ') {
                input = new StringBuilder(input).deleteCharAt(i).toString();
            }
        }
        return input;
    }
}