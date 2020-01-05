package company;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TokenTest {

    @ParameterizedTest
    @MethodSource("source")
    void isVariable(String content, boolean expected) {
        boolean result = Token.isVariable(content);

        Assertions.assertEquals(expected, result);
    }

    static Stream<Arguments> source() {
        return Stream.of(
                Arguments.of("qwe", true),
                Arguments.of("qFd", true),
                Arguments.of("r", true),
                Arguments.of("qwe34dfg", false),
                Arguments.of("+", false),
                Arguments.of("43", false),
                Arguments.of("qwe+lkfg", false)
        );
    }
}