package de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.wiktionary.linking;

import de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.wiktionary.WiktionaryKnowledgeSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WiktionaryLinkerTest {

    @Test
    void linkToSingleConcept() {
        WiktionaryLinker linker = new WiktionaryLinker(new WiktionaryKnowledgeSource());
        String result = linker.linkToSingleConcept("Fallopian tube");
        assertNotNull(result);
        result = linker.linkToSingleConcept("fallopian tube");
        assertNotNull(result);
    }

    @Test
    void getNameOfLinker() {
        WiktionaryLinker linker = new WiktionaryLinker(new WiktionaryKnowledgeSource());
        assertNotNull(linker.getNameOfLinker());
    }
}