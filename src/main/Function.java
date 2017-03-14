package main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

public class Function implements Serializable {
    // Declare fields
    private static final long serialVersionUID = -4936036854404504779L; // Necessary for serialisable objects in Java
    private static String InfixExpression;
    private static String PostfixExpression;

    // Method converts a given string to RPN
    private static void convertInfixToRPN() {
        Stack<String> OperatorStack = new Stack<>();
        StringBuilder OutputBuilder = new StringBuilder();
        Function.InfixExpression = removeAllSpaces(getInfixExpression());
        String[] UncheckedTokens = tokeniseString(getInfixExpression());
        // Check tokens
        String[] CheckedTokens = checkTokens(UncheckedTokens);
        for (String currentToken : CheckedTokens) {
            if (checkIfOperand(currentToken)) {
                OutputBuilder.append(currentToken).append(",");
            } else if (isLeftParenthesis(currentToken)) {
                OperatorStack.push(currentToken);
            } else if (isRightParenthesis(currentToken)) {
                while (!isLeftParenthesis(OperatorStack.peek())) {
                    OutputBuilder.append(OperatorStack.pop()).append(",");
                }
                OperatorStack.pop();
            } else if (isOperator(currentToken)) {
                while (true) {
                    if (!OperatorStack.isEmpty()) {
                        if (isHigherPrecedence(currentToken, OperatorStack.peek())) {
                            OutputBuilder.append(OperatorStack.pop()).append(",");
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                OperatorStack.push(currentToken);
            }
        }
        while (!OperatorStack.isEmpty()) {
            OutputBuilder.append(OperatorStack.pop()).append(",");
        }
        String finalResult = OutputBuilder.toString();
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

    // Return int precedence values for each Token
    private static int getPrecedenceForToken(String Token) {
        switch (Token) {
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
        return Function.PostfixExpression;
    }

    public static void setPostfixExpression(String postfixExpression) {
        Function.PostfixExpression = postfixExpression;
    }

    public static double evaluate(Coordinate coordinateToUse) throws EvaluationException {
        /*
            Work through each token in the array, pushing
            operands to the stack and popping operands if
            an operator is encountered
         */
        Stack<String> OperandStack = new Stack<>();
        String[] Tokens = getPostfixExpression().split(",");
        double LeftOperandDouble, RightOperandDouble;
        for (String TokenStepper : Tokens) {
            // if the current token is an operator pop items off the stack
            if (isOperator(TokenStepper)) {
                String LeftOperand, RightOperand;
                // pop the first operand off the stack
                RightOperand = OperandStack.pop();
                try {
                    // attempt to pop a second item off of the stack
                    LeftOperand = OperandStack.pop();
                } catch (EmptyStackException emptyStackException) {
                    LeftOperand = "0";
                }
                // Attempt numeric conversion
                try {
                    LeftOperandDouble = Double.parseDouble(LeftOperand);
                } catch (NumberFormatException numberFormatException) {
                    // If the operand isn't a number, it must be a letter representation
                    switch (LeftOperand) {
                        case "x":
                            LeftOperandDouble = coordinateToUse.getXValue();
                            break;
                        case "y":
                            LeftOperandDouble = coordinateToUse.getYValue();
                            break;
                        case "e":
                            LeftOperandDouble = Math.E;
                            break;
                        case "p":
                            LeftOperandDouble = Math.PI;
                            break;
                        default:
                            // a letter has been used that has no known value
                            throw new EvaluationException("Unrecognised operand: " + LeftOperand);
                    }
                }
                try {
                    // Attempt numeric conversion
                    RightOperandDouble = Double.parseDouble(RightOperand);
                } catch (NumberFormatException e) {
                    // If the operand isn't a number, it must be a letter representation
                    switch (RightOperand) {
                        case "x":
                            RightOperandDouble = coordinateToUse.getXValue();
                            break;
                        case "y":
                            RightOperandDouble = coordinateToUse.getYValue();
                            break;
                        case "e":
                            RightOperandDouble = Math.E;
                            break;
                        case "p":
                            RightOperandDouble = Math.PI;
                            break;
                        default:
                            // a letter has been used that has no known value
                            throw new EvaluationException("Unrecognised operand: " + LeftOperand);
                    }
                }
                // Perform the operator on the two popped stack items
                switch (TokenStepper) {
                    case "+":
                        OperandStack.push(String.valueOf(LeftOperandDouble + RightOperandDouble));
                        break;
                    case "-":
                        OperandStack.push(String.valueOf(LeftOperandDouble - RightOperandDouble));
                        break;
                    case "*":
                        OperandStack.push(String.valueOf(LeftOperandDouble * RightOperandDouble));
                        break;
                    case "/":
                        OperandStack.push(String.valueOf(LeftOperandDouble / RightOperandDouble));
                        break;
                    case "^":
                        OperandStack.push(String.valueOf(Math.pow(LeftOperandDouble, RightOperandDouble)));
                        break;
                    case "s":
                        OperandStack.push(String.valueOf(Math.pow(Math.sin(LeftOperandDouble), RightOperandDouble)));
                        break;
                    case "c":
                        OperandStack.push(String.valueOf(Math.pow(Math.cos(LeftOperandDouble), RightOperandDouble)));
                        break;
                    case "t":
                        OperandStack.push(String.valueOf(Math.pow(Math.tan(LeftOperandDouble), RightOperandDouble)));
                        break;
                }
            } else {
                // current item is a operand, so push it to the stack
                OperandStack.push(TokenStepper);
            }
        }
        // return the evaluation
        return Double.valueOf(OperandStack.pop());
    }

    // Accessor method for the infix expression
    public static String getInfixExpression() {
        return Function.InfixExpression;
    }

    // Mutator method for the infix expression
    public static void setInfixExpression(String InfixExpression) {
        Function.InfixExpression = InfixExpression;
        convertInfixToRPN();
    }

    // This private method checks a String array and moves minus signs if they are unary
    private static String[] checkTokens(String[] UncheckedTokenArray) {
        ArrayList<String> CheckedTokensList = new ArrayList<>();
        // Since unary minus could be counted as an operator, we need to check for it
        for (int Stepper = 0; Stepper < UncheckedTokenArray.length; Stepper++) {
            if (Stepper > 0) {
                // Two consecutive operators test
                if ("-".equals(UncheckedTokenArray[Stepper]) && isOperator(UncheckedTokenArray[Stepper - 1])) {
                    UncheckedTokenArray[Stepper] = "";
                    UncheckedTokenArray[Stepper + 1] = "-" + UncheckedTokenArray[Stepper + 1];
                    // Current token is obsolete
                    continue;
                }
            }
            if (Stepper == 0) {
                // '-' at startSearch test
                if ("-".equals(UncheckedTokenArray[Stepper])) {
                    UncheckedTokenArray[Stepper + 1] = "-" + UncheckedTokenArray[Stepper + 1];
                    // Current token is obsolete
                    CheckedTokensList.add(UncheckedTokenArray[Stepper]);
                    continue;
                }
            }
            CheckedTokensList.add(UncheckedTokenArray[Stepper]);
        }
        return CheckedTokensList.toArray(new String[0]);
    }

    // Removes all spaces in a String
    private static String removeAllSpaces(String Input) {
        for (int i = 0; i < Input.length(); i++) {
            if (Input.charAt(i) == ' ') {
                Input = new StringBuilder(Input).deleteCharAt(i).toString();
            }
        }
        return Input;
    }
}