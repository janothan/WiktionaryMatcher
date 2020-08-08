package de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.wiktionary.linking;

import de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.wiktionary.Language;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LabelLanguageTupleTest {
    @Test
    void testEquals() {
        LabelLanguageTuple ll_1 = new LabelLanguageTuple("Europa", Language.GERMAN);
        LabelLanguageTuple ll_2 = new LabelLanguageTuple("Europe", Language.ENGLISH);
        LabelLanguageTuple ll_3 = new LabelLanguageTuple("Europa", Language.GERMAN);
        LabelLanguageTuple ll_4 = new LabelLanguageTuple("Europa", Language.SPANISH);
        LabelLanguageTuple ll_5 = new LabelLanguageTuple("European Union", Language.ENGLISH);
        assertTrue(ll_1.equals(ll_1));
        assertTrue(ll_1.equals(ll_3));
        assertFalse(ll_1.equals(ll_2));
        assertFalse(ll_1.equals(ll_4));
        assertFalse(ll_2.equals(ll_4));
        assertFalse(ll_2.equals(ll_5));
        assertFalse(ll_2.equals(23.8));
        assertFalse(ll_2.equals("Europe"));
    }
}