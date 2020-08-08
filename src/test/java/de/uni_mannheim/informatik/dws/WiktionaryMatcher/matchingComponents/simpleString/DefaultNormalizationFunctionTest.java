package de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.simpleString;

import static org.junit.jupiter.api.Assertions.*;

class DefaultNormalizationFunctionTest {

    @org.junit.jupiter.api.Test
    void simplyNormalize() {
        DefaultNormalizationFunction dnf = new DefaultNormalizationFunction();
        assertEquals("a_pour_thématique", dnf.apply("a pour thématique@fr"));
        assertEquals("mary_stuart", dnf.apply("Mary Stuart (1542 - 1567)"));
        assertEquals("mary_stuart", dnf.apply("  Mary Stuart  (1542 - 1567) "));
        assertEquals("altiero_spinelli", dnf.apply(" Altiero Spinelli [1907 – 1986]"));
        assertEquals("8th_century", dnf.apply("8th_century"));
    }
}