package a.slelin.work.word.master.service.logic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnswerCheckerTest {

    @ParameterizedTest(name = "[{index}] expected ''{0}'', given ''{1}'' -> {2}")
    @CsvSource(delimiter = '|', value = {
            "яблоко|яблоко|EXACT",
            "яблоко|  Яблоко! |EXACT",
            "ёлка|елка|EXACT",
            "идти, ехать|ехать|EXACT",
            "идти, ехать|идти|EXACT",
            "идти, ехать|идти ехать|EXACT",
            "go – went – gone|go went gone|EXACT",
            "go – went – gone|go-went-gone|EXACT",
            "get along (with)|get along|EXACT",
            "get along (with)|get along with|EXACT",
            "to implement|implement|EXACT",
            "How much is it?|how much is it|EXACT",
            "сражаться|сражатся|TYPO",
            "authentication|autentication|TYPO",
            "adiós|adios|TYPO",
            "Straße|strase|TYPO",
            "кот|кит|WRONG",
            "яблоко|груша|WRONG",
            "яблоко|''|WRONG"
    })
    void check(String expected, String given, AnswerChecker.Result result) {
        assertEquals(result, AnswerChecker.check(expected, given));
    }
}
