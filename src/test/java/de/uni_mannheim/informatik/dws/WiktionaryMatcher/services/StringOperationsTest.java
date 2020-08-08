package de.uni_mannheim.informatik.dws.WiktionaryMatcher.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringOperationsTest {

    @Test
    void isSameString(){
        assertFalse(StringOperations.isSameStringIgnoringStopwordsAndNumbers("Pyramid","pallidum"));
        assertTrue(StringOperations.isSameStringIgnoringStopwordsAndNumbers("Pyramid","pyramid"));
        assertTrue(StringOperations.isSameStringIgnoringStopwordsAndNumbers("PyramidEgypt","pyramid_Egypt"));
        assertTrue(StringOperations.isSameStringIgnoringStopwordsAndNumbers("hair shaft","Shaft_of_the_Hair"));
        assertTrue(StringOperations.isSameStringIgnoringStopwordsAndNumbers("trunk skin","Skin_of_the_Trunk"));
    }

    @Test
    void isMeaningfulFragment(){
        assertTrue(StringOperations.isMeaningfulFragment("humanAnatomy_123"));
        assertFalse(StringOperations.isMeaningfulFragment("NCI123"));
        assertFalse(StringOperations.isMeaningfulFragment("MA_12345"));
    }

}