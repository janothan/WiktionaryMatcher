package de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.wiktionary;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class WiktionaryKnowledgeSourceTest {
    public static WiktionaryKnowledgeSource wiktionary;

    @BeforeAll
    public static void prepare() {

        //wiktionary = new WiktionaryKnowledgeSource(true);
        wiktionary = new WiktionaryKnowledgeSource();
    }

    @Test
    public void testIsInDictionaryString() {
        // true positive check
        assertTrue(wiktionary.isInDictionary("dog"));

        // true positive check; check for correct encoding of spaces
        assertTrue(wiktionary.isInDictionary("seminal fluid"));

        // false positive check
        assertFalse(wiktionary.isInDictionary("asdfasdfasdf"));
    }

    @Test
    public void testIsInDictionaryStringDBNaryLanguage() {
        // true positive check
        assertTrue(wiktionary.isInDictionary("cat", Language.ENGLISH));

        // true positive check; check for correct encoding of spaces
        assertTrue(wiktionary.isInDictionary("seminal fluid", Language.ENGLISH));

        // false positive check
        assertFalse(wiktionary.isInDictionary("asdfasdfasdf", Language.ENGLISH));
    }

    @Test
    public void testGetSynonymsString() {
        if(wiktionary.isUseOfflineDatabase()) { // find out why this does not work with online access
            // just checking that there are synonyms
            assertTrue(wiktionary.getSynonyms("cat").size() > 0);

            // second test for buffer
            assertTrue(wiktionary.getSynonyms("cat").size() > 0);

            // checking for one specific synonym
            assertTrue(wiktionary.getSynonyms("temporal muscle").contains("temporalis"));
            assertTrue(wiktionary.getSynonyms("head man").contains("boss"));

            // checking for non-existing synonym
            assertNull(wiktionary.getSynonyms("asdfasdfasdf"));
        }
    }

    @Test
    public void testGetSynonymsStringDBNaryLanguage() {
        // buffer check
        int numberOfSynonyms1 = wiktionary.getSynonyms("cat").size();
        int numberOfSynonyms2 = wiktionary.getSynonyms("cat").size();
        assertTrue(numberOfSynonyms1 == numberOfSynonyms2);
    }

    @Test
    public void testIsSynonymous() {
        assertTrue(wiktionary.isSynonymous("dog", "hound"));
        assertTrue(wiktionary.isSynonymous("dog", "dog"));
        assertFalse(wiktionary.isSynonymous("dog", "cat"));
    }

    @Test
    public void testIsStrongFromSynonymous(){
        assertTrue(wiktionary.isStrongFormSynonymous("dog", "hound"));
        assertTrue(wiktionary.isStrongFormSynonymous("dog", "dog"));
        assertTrue(wiktionary.isStrongFormSynonymous("student", "scholar"));
        assertTrue(wiktionary.isStrongFormSynonymous("Fallopian tube", "oviduct"));

        // test with hyphens
        assertTrue(wiktionary.isStrongFormSynonymous("e-mail", "e-dress"));

        // negative test
        assertFalse(wiktionary.isStrongFormSynonymous("dog", "cat"));
    }

    @Test
    public void testHypernymy(){
        assertTrue(wiktionary.getHypernyms("cat").contains("feline"));
        assertFalse(wiktionary.getHypernyms("cat").contains("dog"));

        // assert linking process compatibility
        assertTrue(wiktionary.getHypernyms(wiktionary.getLinker().linkToSingleConcept("cat")).contains("feline"));
        assertFalse(wiktionary.getHypernyms(wiktionary.getLinker().linkToSingleConcept("cat")).contains("dog"));
    }

    @Test
    void isSynonymousOrHypernymyous(){
        if(wiktionary.isUseOfflineDatabase()) { // if time permitsfind out why this does not work with the online access point
            assertTrue(wiktionary.isSynonymousOrHypernymous("cat", "feline"));
            assertTrue(wiktionary.isSynonymousOrHypernymous("dog", "hound"));
            assertFalse(wiktionary.isSynonymousOrHypernymous("dog", "cat"));

            // linking process compatibility
            assertTrue(wiktionary.isSynonymousOrHypernymous(wiktionary.getLinker().linkToSingleConcept("cat"), wiktionary.getLinker().linkToSingleConcept("feline")));
            assertTrue(wiktionary.isSynonymousOrHypernymous(wiktionary.getLinker().linkToSingleConcept("dog"), wiktionary.getLinker().linkToSingleConcept("hound")));
            assertFalse(wiktionary.isSynonymousOrHypernymous(wiktionary.getLinker().linkToSingleConcept("dog"), wiktionary.getLinker().linkToSingleConcept("cat")));
        }
    }


    @Test
    void isTranslation(){

        // English/German

        assertTrue(wiktionary.isTranslationLinked("bed", Language.ENGLISH, "Bett", Language.GERMAN));
        assertTrue(wiktionary.isTranslationLinked("conference", Language.ENGLISH, "Tagung", Language.GERMAN));
        assertTrue(wiktionary.isTranslationLinked("conference", Language.ENGLISH, "Konferenz", Language.GERMAN));
        assertFalse(wiktionary.isTranslationLinked("conference", Language.ENGLISH, "Bett", Language.GERMAN));

        assertTrue(wiktionary.isTranslationLinked("Bett", Language.GERMAN, "bed", Language.ENGLISH));
        assertTrue(wiktionary.isTranslationLinked("Tagung", Language.GERMAN, "conference", Language.ENGLISH));
        assertTrue(wiktionary.isTranslationLinked("Konferenz", Language.GERMAN, "conference", Language.ENGLISH));
        assertFalse(wiktionary.isTranslationLinked("Bett", Language.GERMAN, "conference", Language.ENGLISH));

        // Russian/French
        //assertTrue(wiktionary.isTranslationLinked("рецензия", Language.RUSSIAN, "critique", Language.FRENCH));

    }

    /**
     * Note that for this test, all dbnary core dumps have to be added to the TDB data set.
     */
    @Test
    void getTranslation(){

        //---------------------
        // From Russian
        //---------------------

        // to French
        HashSet<String> russianFrenchTranslation = wiktionary.getTranslation("рецензия", Language.RUSSIAN, Language.FRENCH);
        assertTrue(russianFrenchTranslation.contains("critique"));

        // to French (again to test buffer)
        russianFrenchTranslation = wiktionary.getTranslation("рецензия", Language.RUSSIAN, Language.FRENCH);
        assertTrue(russianFrenchTranslation.contains("critique"));

        //---------------------
        // From Dutch
        //---------------------

        // to English
        HashSet<String> dutchSpanishTranslation = wiktionary.getTranslation("topconferentie", Language.DUTCH, Language.ENGLISH);
        assertTrue(dutchSpanishTranslation.contains("summit conference"));


        //---------------------
        // From German
        //---------------------

        // to Italian
        HashSet<String> germanItalianTranslation = wiktionary.getTranslation("Konferenz", Language.GERMAN, Language.ITALIAN);
        assertTrue(germanItalianTranslation.contains("conferenza"));


        //---------------------
        // From French
        //---------------------

        // to Italian
        HashSet<String> frenchItalianTranslation = wiktionary.getTranslation("conférence", Language.FRENCH, Language.ITALIAN);
        assertTrue(frenchItalianTranslation.contains("conferenza"));


        //---------------------
        // From Portugese
        //---------------------

        // to Italian
        HashSet<String> portugeseItalianTranslation = wiktionary.getTranslation("banco", Language.PORTUGESE, Language.ITALIAN);
        assertTrue(portugeseItalianTranslation.contains("banca"));


        //---------------------
        // From Spanish
        //---------------------

        // to German
        HashSet<String> spanishGermanTranslation = wiktionary.getTranslation("banco", Language.SPANISH, Language.GERMAN);
        assertTrue(spanishGermanTranslation.contains("Bank"));


        //---------------------
        // From Italian
        //---------------------

        // to German
        HashSet<String> italianGermanTranslation = wiktionary.getTranslation("banca", Language.ITALIAN, Language.FRENCH);
        assertTrue(italianGermanTranslation.contains("banque"));


        //---------------------
        // From English
        //---------------------

        // to German
        HashSet<String> germanTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.GERMAN);
        assertTrue(germanTranslation.contains("Konferenz"));

        // to Dutch
        HashSet<String> dutchTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.DUTCH);
        assertTrue(dutchTranslation.contains("conferentie"));

        // to Arabic
        HashSet<String> arabicTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.ARABIC);
        assertTrue(arabicTranslation.contains("مُؤْتَمَر"));

        // to Chinese
        HashSet<String> chineseTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.CHINESE);
        assertTrue(chineseTranslation.contains("會議"));

        // to French
        HashSet<String> frenchTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.FRENCH);
        assertTrue(frenchTranslation.contains("conférence"));

        // to Italian
        HashSet<String> italianTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.ITALIAN);
        assertTrue(italianTranslation.contains("conferenza"));

        // to Portugese
        HashSet<String> portugeseTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.PORTUGESE);
        assertTrue(portugeseTranslation.contains("conferência"));

        // to Russian
        HashSet<String> russianTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.RUSSIAN);
        assertTrue(russianTranslation.contains("конфере́нция"));

        // to Spanish
        HashSet<String> spanishTranslation = wiktionary.getTranslation("conference", Language.ENGLISH, Language.SPANISH);
        assertTrue(spanishTranslation.contains("conferencia"));
    }

    @Test
    void isTranslationOf(){

        // German Word
        HashSet<String> translationPartnerForGermanBank = wiktionary.getTranslationOf("Bank", Language.GERMAN);
        boolean engTranslationForGermanBankAppears = false;
        assertNotNull(translationPartnerForGermanBank);
        for(String uris : translationPartnerForGermanBank){
            if(uris.equals("http://kaiko.getalp.org/dbnary/eng/bank")) engTranslationForGermanBankAppears = true;
        }
        assertTrue(engTranslationForGermanBankAppears);


        // Czech Word
        HashSet<String> translationPartnerForCzechBank = wiktionary.getTranslationOf("banka", Language.CZECH);
        boolean engTranslationForCzechankAppears = false;
        assertNotNull(translationPartnerForCzechBank);
        for(String uris : translationPartnerForCzechBank){
            if(uris.equals("http://kaiko.getalp.org/dbnary/eng/bank")) engTranslationForCzechankAppears = true;
        }
        assertTrue(engTranslationForCzechankAppears);

        /*
        // Chinese Word
        HashSet<String> translationPartnerForChineseFather = wiktionary.getTranslationOf("爸爸", Language.CHINESE);
        boolean engTranslationForChineseFatherAppears = false;
        assertNotNull(translationPartnerForChineseFather);
        for(String uris : translationPartnerForChineseFather){
            if(uris.equals("http://kaiko.getalp.org/dbnary/eng/father")) engTranslationForChineseFatherAppears = true;
        }
        assertTrue(engTranslationForChineseFatherAppears);
*/
    }


    @Test
    void isTranslationDerived(){
        assertTrue(wiktionary.isTranslationDerived("爸爸", Language.CHINESE, "Vater", Language.GERMAN));
        assertTrue(wiktionary.isTranslationDerived("Vertrag", Language.GERMAN, "smlouva", Language.CZECH));
        assertFalse(wiktionary.isTranslationDerived("Europa", Language.GERMAN, "smlouva", Language.CZECH));
        assertTrue(wiktionary.isTranslationDerived("عَقْد", Language.ARABIC, "Vertrag", Language.GERMAN));
        assertFalse(wiktionary.isTranslationDerived("عَقْد", Language.ARABIC, "Europa", Language.GERMAN));
        assertTrue(wiktionary.isTranslationDerived("عَقْد", Language.ARABIC, "smlouva", Language.CZECH));
    }

    @AfterAll
    public static void destruct() {
        wiktionary.close();
    }
}