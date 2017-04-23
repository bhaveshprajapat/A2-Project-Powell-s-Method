package main;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

public class Function {
    // Declare static fields
    private static String InfixExpression;
    private static String PostfixExpression;

    // Method converts a given string to Postfix
    private static String convertInfixToPostfix(String expression) {
        Stack<String> OperatorStack = new Stack<>();
        StringBuilder OutputBuilder = new StringBuilder();
        expression = removeAllSpaces(expression);
        String[] UncheckedTokens = tokeniseString(expression);
        // Check tokens for common errors and correct them automatically
        String[] CheckedTokens = checkTokens(UncheckedTokens);
        for (String currentToken : CheckedTokens) { // Loop through each token

            if (checkIfOperand(currentToken)) {
                OutputBuilder.append(currentToken).append(','); // Add operand to the output
            } else if (isLeftParenthesis(currentToken)) {
                OperatorStack.push(currentToken); // Push (
            } else if (isRightParenthesis(currentToken)) {
                while (!isLeftParenthesis(OperatorStack.peek())) {
                    OutputBuilder.append(OperatorStack.pop()).append(','); // When a ) is occurred, pop all operators
                }
                OperatorStack.pop(); // Pop the ), without appending to output
            } else if (isOperator(currentToken)) {
                while (!OperatorStack.isEmpty() && isHigherPrecedence(currentToken, OperatorStack.peek())) {
                        OutputBuilder.append(OperatorStack.pop()).append(','); // Pop and output anything with a higher precedence first
                }
                OperatorStack.push(currentToken); // Pop the operator to the output
            } else {
                // (Very unlikely) an unknown token passed validation, it will be ignored - the user will alerted
                MainSceneController.getLog().add("Unrecognised token found in function: " + currentToken + "... ignored.");
            }
        }
        while (!OperatorStack.isEmpty()) {
            OutputBuilder.append(OperatorStack.pop()).append(','); // Pop anything left over
        }
        // Create result and return
        String finalResult = OutputBuilder.toString();
        return finalResult.substring(0, finalResult.length() - 1);
    }

    // Is the parameterised token a RIGHT PARENTHESIS?
    private static boolean isRightParenthesis(String currentToken) {
        return ")".equals(currentToken);
    }

    // Is the parameterised token an OPERATOR?
    private static boolean isOperator(String currentToken) {
        return currentToken.matches("[*/+\\-^(]|s|c|t");
    }

    // Compare the BIDMAS (extended) for each parameterised operator
    private static boolean isHigherPrecedence(String currentToken, String peek) {
        int currentTokenPrecedence = getPrecedenceForToken(currentToken);
        int peekPrecedence = getPrecedenceForToken(peek);
        return peekPrecedence >= currentTokenPrecedence;
    }

    // Return integer precedence values for each Token
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
        return currentToken.matches("(?:\\d*\\.?\\d+)|[xype]");
    }

    // Split up a string using a Regular Expression to get all of the operands and operators separate.
    private static String[] tokeniseString(String stringToTokenise) {
        return stringToTokenise.split("(?<=[-+^*/()])|(?=[-+*/()^])|(?=[a-z])|(?<=.[a-z])|(?<=[a-z])");
    }

    // Public function returns the expression evaluated with a particular coordinate
    public static double evaluate(Coordinate coordinateToUse) throws EvaluationException {
        /*
            Work through each token in the array, pushing
            operands to the stack and popping operands if
            an operator is encountered
         */
        Stack<String> OperandStack = new Stack<>();
        String[] Tokens = PostfixExpression.split(",");
        double LeftOperandDouble;
        double RightOperandDouble;
        String LeftOperand;
        String RightOperand;
        for (String TokenStepper : Tokens) {
            // If the current token is an operator pop items off the stack
            if (isOperator(TokenStepper)) {
                //Pop the first operand off the stack
                RightOperand = OperandStack.pop();
                try {
                    // Attempt to pop a second item off of the stack
                    LeftOperand = OperandStack.pop();
                } catch (EmptyStackException ignored) {
                    throw new EvaluationException("Expression has too many operands");
                }
                // Attempt numeric conversion
                try {
                    LeftOperandDouble = Double.parseDouble(LeftOperand);
                } catch (NumberFormatException numberFormatException) {
                    // If the operand isn't a number, it must be a letter representation - so substitute it
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
                            // A letter has been used that has no known value
                            throw new EvaluationException("Unrecognised operand: " + LeftOperand);
                    }
                }
                try {
                    // Attempt numeric conversion
                    RightOperandDouble = Double.parseDouble(RightOperand);
                } catch (NumberFormatException e) {
                    // If the operand isn't a number, it must be a letter representation - so substitute it
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
                            // A letter has been used that has no known value
                            throw new EvaluationException("Unrecognised operand: " + LeftOperand);
                    }
                }
                // Perform operator on the two popped stack items
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
                // Current item is a operand, so push it to the stack of operands yet to be operated on
                OperandStack.push(TokenStepper);
            }
        }
        // Return final value in the stack
        return Double.valueOf(OperandStack.pop());
    }

    // Accessor method for the infix expression
    public static String getInfixExpression() {
        return InfixExpression;
    }

    // Mutator method for the infix expression
    public static void setInfixExpression(String InfixExpression) {
        Function.InfixExpression = InfixExpression;
        PostfixExpression = convertInfixToPostfix(InfixExpression);
    }

    // This private method checks a String array and moves minus signs if they are unary
    private static String[] checkTokens(String[] UncheckedTokenArray) {
        ArrayList<String> CheckedTokensList = new ArrayList<>();
        // Since unary minus could be counted as an operator, we need to check for it
        for (int Stepper = 0; Stepper < UncheckedTokenArray.length; Stepper++) {
            if (Stepper > 0) {
                // If there is an operator followed by a '-', then the right operator must be negative
                if ("-".equals(UncheckedTokenArray[Stepper]) && isOperator(UncheckedTokenArray[Stepper - 1])) {
                    // No need to keep a value in this space
                    UncheckedTokenArray[Stepper] = "";
                    // Prefix the minus sign to the adjacent array value
                    UncheckedTokenArray[Stepper + 1] = '-' + UncheckedTokenArray[Stepper + 1];
                    continue;
                }
                // Check for any omitted multiplication signs before brackets
                if ("(".equals(UncheckedTokenArray[Stepper]) && !isOperator(UncheckedTokenArray[Stepper - 1])) {
                    CheckedTokensList.add("*");
                }
            }

            if (Stepper == 0) {
                // Tests for a '-' at the beginning of the expression
                if ("-".equals(UncheckedTokenArray[0])) {
                    UncheckedTokenArray[1] = '-' + UncheckedTokenArray[1];
                    // Current token is obsolete
                    CheckedTokensList.add(UncheckedTokenArray[0]);
                    continue;
                }
            }
            CheckedTokensList.add(UncheckedTokenArray[Stepper]);
        }
        return CheckedTokensList.toArray(new String[CheckedTokensList.size()]);
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