package de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.wiktionary.linking;

import de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.util.LabelLanguageTuple;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
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
        assertEquals(ll_1, ll_1);
        assertEquals(ll_1, ll_3);
        assertNotEquals(ll_1, ll_2);
        assertNotEquals(ll_1, ll_4);
        assertNotEquals(ll_2, ll_4);
        assertNotEquals(ll_2, ll_5);
        assertNotEquals(ll_2, 23.8);
        assertNotEquals("Europe", ll_2);
    }
}