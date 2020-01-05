package company;

import java.util.*;

class Token {
    public static final int UNKNOWN = -1;
    public static final int NUMBER = 0;
    public static final int OPERATOR = 1;
    public static final int LEFT_PARENTHESIS = 2;
    public static final int RIGHT_PARENTHESIS = 3;
    public static final int VARIABLE = 4;

    private int type;
    private double value;
    private char operator;
    private int precedence;

    public Token() {
        type = UNKNOWN;
    }

    public Token(String contents) {
        switch (contents) {
            case "+":
                type = OPERATOR;
                operator = contents.charAt(0);
                precedence = 1;
                break;
            case "-":
                type = OPERATOR;
                operator = contents.charAt(0);
                precedence = 1;
                break;
            case "*":
                type = OPERATOR;
                operator = contents.charAt(0);
                precedence = 2;
                break;
            case "/":
                type = OPERATOR;
                operator = contents.charAt(0);
                precedence = 2;
                break;
            case "(":
                type = LEFT_PARENTHESIS;
                break;
            case ")":
                type = RIGHT_PARENTHESIS;
                break;
            default:
                type = NUMBER;
                try {
                    value = Double.parseDouble(contents);
                } catch (Exception ex) {
                    type = UNKNOWN;
                }
        }
    }

    public Token(double x) {
        type = NUMBER;
        value = x;
    }

    int getType() {
        return type;
    }

    double getValue() {
        return value;
    }

    int getPrecedence() {
        return precedence;
    }

    Token operate(double a, double b) {
        double result = 0;
        switch (operator) {
            case '+':
                result = a + b;
                break;
            case '-':
                result = a - b;
                break;
            case '*':
                result = a * b;
                break;
            case '/':
                result = a / b;
                break;
        }
        return new Token(result);
    }
}

class TokenStack {
    /**
     * Member variables
     **/
    private ArrayList<Token> tokens = new ArrayList<Token>();

    /**
     * Accessor methods
     **/
    public boolean isEmpty() {
        return tokens.size() == 0;
    }

    public Token top() {
        return tokens.get(tokens.size() - 1);
    }

    /**
     * Mutator methods
     **/
    public void push(Token t) {
        tokens.add(t);
    }

    public void pop() {
        tokens.remove(tokens.size() - 1);
    }
}

class CalculationResult {
    private final String result;
    private final String message;

    CalculationResult(String result, String message) {
        this.result = result;
        this.message = message;
    }

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalculationResult that = (CalculationResult) o;
        return Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result);
    }

    @Override
    public String toString() {
        return "CalculationResult{" +
                "result='" + result + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

class FullCalculator {
    private static final String LAST = "last";
    private TokenStack operatorStack;
    private TokenStack valueStack;
    private String errorMessage;
    private Map<String, Double> variables = new HashMap<>();

    public FullCalculator() {
        operatorStack = new TokenStack();
        valueStack = new TokenStack();
        variables.put(LAST, 0.0);
    }

    private void processOperator(Token t) {
        Token A = null, B = null;
        if (valueStack.isEmpty()) {
//            System.out.println("ERROR");
//            error = true;
            errorMessage = "value stack is empty (read B)";
            return;
        } else {
            B = valueStack.top();
            valueStack.pop();
        }
        if (valueStack.isEmpty()) {
//            System.out.println("ERROR");
//            error = true;
            errorMessage = "value stack is empty (read A)";
            return;
        } else {
            A = valueStack.top();
            valueStack.pop();
        }
        Token R = t.operate(A.getValue(), B.getValue());
        valueStack.push(R);
    }

    public CalculationResult processInput(String input) {
        // The tokens that make up the input
        String[] parts = input.split(" ");
        Token[] tokens = new Token[parts.length];
        for (int n = 0; n < parts.length; n++) {
            tokens[n] = new Token(parts[n]);
        }

        // Main loop - process all input tokens
        for (int n = 0; n < tokens.length; n++) {
            Token nextToken = tokens[n];
            if (nextToken.getType() == Token.NUMBER) {
                valueStack.push(nextToken);
            } else if (nextToken.getType() == Token.OPERATOR) {
                if (operatorStack.isEmpty() || nextToken.getPrecedence() > operatorStack.top().getPrecedence()) {
                    operatorStack.push(nextToken);
                } else {
                    while (!operatorStack.isEmpty() && nextToken.getPrecedence() <= operatorStack.top().getPrecedence()) {
                        Token toProcess = operatorStack.top();
                        operatorStack.pop();
                        processOperator(toProcess);
                    }
                    operatorStack.push(nextToken);
                }
            } else if (nextToken.getType() == Token.LEFT_PARENTHESIS) {
                operatorStack.push(nextToken);
            } else if (nextToken.getType() == Token.RIGHT_PARENTHESIS) {
                while (!operatorStack.isEmpty() && operatorStack.top().getType() == Token.OPERATOR) {
                    Token toProcess = operatorStack.top();
                    operatorStack.pop();
                    processOperator(toProcess);
                }
                if (!operatorStack.isEmpty() && operatorStack.top().getType() == Token.LEFT_PARENTHESIS) {
                    operatorStack.pop();
                } else {
                    errorMessage = "Missing parenthesis";
//                    error = true;
                }
            }

        }
        // Empty out the operator stack at the end of the input
        while (!operatorStack.isEmpty() && operatorStack.top().getType() == Token.OPERATOR) {
            Token toProcess = operatorStack.top();
            operatorStack.pop();
            processOperator(toProcess);
        }
        // Print the result if no error has been seen.
        if (errorMessage == null) {
            Token result = valueStack.top();
            valueStack.pop();
            if (!operatorStack.isEmpty() || !valueStack.isEmpty()) {
                return new CalculationResult("ERROR", "operatorStack or valueStack is not empty");
            } else {
                return new CalculationResult("OK", String.format("%.5f", result.getValue()).replaceAll(",", "."));
            }
        } else {
            return new CalculationResult("ERROR", errorMessage);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        // The original input
        System.out.print("Enter an expression to compute: ");
        String userInput = input.nextLine();

        FullCalculator calc = new FullCalculator();
        CalculationResult result = calc.processInput(userInput);
        if ("OK".equals(result.getResult())) {
            System.out.println(result.getMessage());
        } else {
            System.out.println(result.getResult());
            System.out.println(result.getMessage());
        }
    }
}


