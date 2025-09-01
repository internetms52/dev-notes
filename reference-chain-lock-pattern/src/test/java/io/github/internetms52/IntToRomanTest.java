package io.github.internetms52;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IntToRomanTest {
    private static final IntToRoman INT_TO_ROMAN = new IntToRoman();

    @Test
    void case1() {
        Assertions.assertEquals(INT_TO_ROMAN.intToRoman(3749),"MMMDCCXLIX");
    }


}
