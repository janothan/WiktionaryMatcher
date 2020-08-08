package de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.complexString;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComplexIndexerTest {

    @Test
    void normalize() {
        String[] element = new String[1];
        element[0] = "sue";
        BagOfWords bow = new BagOfWords(element);
        assertTrue(bow.equals(ComplexIndexer.normalize("Sue`s")));
    }


}