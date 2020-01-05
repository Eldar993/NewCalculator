package company;

import java.util.*;

import static company.Token.Type.OPERATOR;

class Token {
    enum Type {
        UNKNOWN,
        NUMBER,
        OPERATOR,
        LEFT_PARENTHESIS,
        RIGHT_PARENTHESIS,
        VARIABLE
    }

    private Type type;
    private double value;
    private char operator;
    private int precedence;

    public Token() {
        type = Type.UNKNOWN;
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
                type = Type.LEFT_PARENTHESIS;
                break;
            case ")":
                type = Type.RIGHT_PARENTHESIS;
                break;
            default:
                type = Type.NUMBER;
                try {
                    value = Double.parseDouble(contents);
                } catch (Exception ex) {
                    type = Type.UNKNOWN;
                }
        }
    }

    public Token(double x) {
        type = Type.NUMBER;
        value = x;
    }

    Type getType() {
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
            if (Token.Type.NUMBER.equals(nextToken.getType())) {
                valueStack.push(nextToken);
            } else if (Token.Type.OPERATOR.equals(nextToken.getType())) {
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
            } else if (Token.Type.LEFT_PARENTHESIS.equals(nextToken.getType())) {
                operatorStack.push(nextToken);
            } else if (Token.Type.RIGHT_PARENTHESIS.equals(nextToken.getType())) {
                while (!operatorStack.isEmpty() && Token.Type.OPERATOR.equals(operatorStack.top().getType())) {
                    Token toProcess = operatorStack.top();
                    operatorStack.pop();
                    processOperator(toProcess);
                }
                if (!operatorStack.isEmpty() && Token.Type.LEFT_PARENTHESIS.equals(operatorStack.top().getType())) {
                    operatorStack.pop();
                } else {
                    errorMessage = "Missing parenthesis";
//                    error = true;
                }
            }

        }
        // Empty out the operator stack at the end of the input
        while (!operatorStack.isEmpty() && Token.Type.OPERATOR.equals(operatorStack.top().getType())) {
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


