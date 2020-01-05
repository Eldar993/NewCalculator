package company;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class FullCalculatorTest {
    private FullCalculator instance;

    @BeforeEach
    void setUp() {
        instance = new FullCalculator();
    }

    @ParameterizedTest
    @MethodSource("successResult")
    void success(String input, String expectedMessage) {
        //Given
        CalculationResult expected = new CalculationResult("OK", expectedMessage);

        //When
        CalculationResult result = instance.processInput(input);

        //Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected, result);
        Assertions.assertEquals(expected.getMessage(), result.getMessage());
    }

    static Stream<Arguments> successResult() {
        return Stream.of(
                Arguments.of("5 + 10", "15.00000"),
                Arguments.of("5 + 1e2", "105.00000"),
                Arguments.of("7 + 1e-3", "7.00100"),
                Arguments.of("3 + 2", "5.00000"),
                Arguments.of("1e2", "100.00000"),
                Arguments.of("2 * ( 3 + 1 )", "8.00000"),
                Arguments.of("1.2 + 4.3", "5.50000"),
                Arguments.of("5 / 3", "1.66667"),
                Arguments.of("5 + unknown", "5.00000"),
                Arguments.of("5 + unknown / 2", "5.00000"),
                Arguments.of("( 5 + unknown ) / 2", "2.50000")
        );
    }



    @ParameterizedTest
    @MethodSource("withErrorMethodSource")
    void containsError(String input) {
        //Given
        CalculationResult expected = new CalculationResult("ERROR", "mock message");

        //When
        CalculationResult result = instance.processInput(input);

        //Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected.getResult(), result.getResult());
    }

    static Stream<Arguments> withErrorMethodSource() {
        return Stream.of(
                Arguments.of("3 ** 5")
        );
    }
}