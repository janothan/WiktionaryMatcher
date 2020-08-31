package de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.wiktionary.linking;

import de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.wiktionary.WiktionaryKnowledgeSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WiktionaryLinkerTest {

    @Test
    void linkToSingleConcept() {
        WiktionaryKnowledgeSource wks = new WiktionaryKnowledgeSource();
        WiktionaryLinker linker = new WiktionaryLinker(wks);
        String result = linker.linkToSingleConcept("Fallopian tube");
        assertNotNull(result);
        result = linker.linkToSingleConcept("fallopian tube");
        assertNotNull(result);
        wks.close();
    }

    @Test
    void getNameOfLinker() {
        WiktionaryKnowledgeSource wks = new WiktionaryKnowledgeSource();
        WiktionaryLinker linker = new WiktionaryLinker(wks);
        assertNotNull(linker.getNameOfLinker());
        wks.close();
    }
}