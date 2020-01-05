package company;

import java.util.*;
import java.util.regex.Pattern;

class Token {
    enum Type {
        UNKNOWN,
        NUMBER,
        OPERATOR,
        LEFT_PARENTHESIS,
        RIGHT_PARENTHESIS,
        VARIABLE
    }

    private static Pattern LETTERS = Pattern.compile("[a-zA-Z]+");

    private Type type;
    private double value;
    private String name;
    private char operator;
    private int precedence;

    public Token() {
        type = Type.UNKNOWN;
    }

    public Token(String contents) {
        if (isVariable(contents)) {
            type = Type.VARIABLE;
            name = contents;
            return;
        }

        switch (contents) {
            case "+":
                type = Type.OPERATOR;
                operator = contents.charAt(0);
                precedence = 1;
                break;
            case "-":
                type = Type.OPERATOR;
                operator = contents.charAt(0);
                precedence = 1;
                break;
            case "*":
                type = Type.OPERATOR;
                operator = contents.charAt(0);
                precedence = 2;
                break;
            case "/":
                type = Type.OPERATOR;
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

    String getName() {
        return name;
    }

    static boolean isVariable(String content) {
        return LETTERS.matcher(content).matches();
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
    private final double value;

    CalculationResult(String result, String message) {
        this(result, message, 0);
    }

    CalculationResult(String result, String message, double value) {
        this.result = result;
        this.message = message;
        this.value = value;
    }

    static CalculationResult success(double value) {
        return new CalculationResult("OK", formatValue(value), value);
    }

    static CalculationResult error() {
        return new CalculationResult("ERROR", "");
    }

    private static String formatValue(double value) {
        return String.format("%.5f", value).replaceAll(",", ".");
    }


    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public double getValue() {
        return value;
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
                ", value='" + value + '\'' +
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
        variables.put(LAST, 0.0);
    }

    private void init() {
        operatorStack = new TokenStack();
        valueStack = new TokenStack();
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

    //x =  1 + 1
    public CalculationResult processInput(String input) {
        init();
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(input);
        }
        int pos = input.indexOf("=");
        if (pos == -1) {
            CalculationResult result = calcExpression(input);
            variables.put(LAST, result.getValue());
            return result;
        } else {
            String[] parts = input.split("=");
            parts[0] = parts[0].trim();
            if (parts.length != 2) {
                return new CalculationResult("ERROR", "unexpected '='");
            } else if (!Token.isVariable(parts[0])) {
                return new CalculationResult("ERROR", "Left side is not variable");
            }
            CalculationResult result = calcExpression(parts[1].trim());
            variables.put(parts[0], result.getValue());
            return result;
        }
    }


    private CalculationResult calcExpression(String input) {
        // The tokens that make up the input
        String[] parts = input.split(" ");
        List<Token> tokens = new ArrayList<>();
        for (int n = 0; n < parts.length; n++) {
            if (parts[n].isBlank()) {
                continue;
            }
            tokens.add(new Token(parts[n]));
        }

        // Main loop - process all input tokens
        for (int n = 0; n < tokens.size(); n++) {
            Token nextToken = tokens.get(n);
            if (Token.Type.NUMBER.equals(nextToken.getType())) {
                valueStack.push(nextToken);
            } else if (Token.Type.VARIABLE.equals(nextToken.getType())) {
                double value = getVariableValue(nextToken.getName());
                valueStack.push(new Token(value));
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
                return CalculationResult.success(result.getValue());
            }
        } else {
            return new CalculationResult("ERROR", errorMessage);
        }
    }

    private double getVariableValue(String name) {
        if (!variables.containsKey(name)) {
            variables.put(name, 0.0);
        }

        return variables.get(name);
    }
}

public class Main {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        FullCalculator calc = new FullCalculator();
        while (scanner.hasNextLine()) {
            try {
                doMethod(calc);
            } catch (Exception e) {
                System.out.println("ERROR");
            }
        }
    }

    private static void doMethod(FullCalculator calc) {
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return;
        }

        CalculationResult result = calc.processInput(input);
        if ("OK".equals(result.getResult())) {
            System.out.println(result.getMessage());
        } else {
            System.out.println(result.getResult());
//            System.out.println(result.getMessage());
        }
    }
}
