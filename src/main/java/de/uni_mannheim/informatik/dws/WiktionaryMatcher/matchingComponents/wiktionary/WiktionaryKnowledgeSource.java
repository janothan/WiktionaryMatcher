package de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.wiktionary;

import de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.util.Language;
import de.uni_mannheim.informatik.dws.WiktionaryMatcher.services.PersistenceService;
import de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.wiktionary.linking.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.wiktionary.linking.WiktionaryLinker;
import org.apache.jena.query.*;
import org.apache.jena.tdb.TDBFactory;
import org.mapdb.BTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Class utilizing dbnary.
 * Dbnary endpoint for tests:
 */
public class WiktionaryKnowledgeSource extends KnowledgeSource {


    /**
     * Just for tests.
     *
     * @param args
     */
    public static void main(String[] args) {
        //WiktionaryKnowledgeSource wiktionary = new WiktionaryKnowledgeSource();
//
        //System.out.println("\nSynonyms:");
        //System.out.println(wiktionary.isInDictionary("cat"));
        //for (String s : wiktionary.getSynonyms("cat")) {
        //    System.out.println(s);
        //}
//
        //System.out.println("\nHypernyms:");
        //for(String s : wiktionary.getHypernyms("cat")){
        //    System.out.println(s);
        //}
//
        //wiktionary.close();

        WiktionaryKnowledgeSource wks = new WiktionaryKnowledgeSource();
        for (String s : wks.getTranslation("conference", Language.ENGLISH, Language.DUTCH)) {
            System.out.println(s);
        }
        wks.close();
    }

    /**
     * Logger for this class.
     */
    private static Logger LOGGER = LoggerFactory.getLogger(WiktionaryKnowledgeSource.class);

    /**
     * directory where the TDB database with the wiktionaryMatcher files lies
     */
    public String tdbDirectoryPath;

    /**
     * Default TDB path to be used.
     * TODO: change to relative once SEALS deployment is running.
     */
    //public static final String DEFAULT_TDB_DIRECTORY_PATH = "C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\dbnary\\2019-08-02\\tdb";
    //public static final String DEFAULT_TDB_DIRECTORY_PATH = "C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\dbnary\\2019-08-07\\compressed\\tdb_data_set";
    public static final String DEFAULT_TDB_DIRECTORY_PATH = "./oaei-resources/wiktionary-tdb";

    /**
     * The default online SPARQL endpoint for dbnary.
     */
    public static final String ONLINE_SPARQL_ENDPOINT = "http://kaiko.getalp.org/sparql";

    /**
     * In-Memory Buffer for Synonyms.
     */
    private HashMap<String, HashSet<String>> synonymyBuffer;

    /**
     * In-Memory Buffer for Hypernymy.
     */
    private HashMap<String, HashSet<String>> hypernymyBuffer;

    /**
     * The TDB dataset into which the dbnary data set was loaded.
     */
    private Dataset tdbDataset;

    /**
     * default true
     */
    private boolean useOfflineDatabase = true;

    /**
     * The linker that links input strings to terms.
     */
    private WiktionaryLinker linker;

    /**
     * Constructor
     * @param tdbDirectoryPath Path to the TDB directory.
     */
    public WiktionaryKnowledgeSource(String tdbDirectoryPath) {
        initOffline(tdbDirectoryPath);
    }

    /**
     * Use only for constructor call. Developer's remark: Modularized, so that this piece of code can be called from
     * multiple constructors.
     * @param tdbDirectoryPath
     */
    private void initOffline(String tdbDirectoryPath){
        this.tdbDirectoryPath = tdbDirectoryPath;
        initBuffers();

        // convenience checks for stable code
        File tdbDirectoryFile = new File(tdbDirectoryPath);
        if (!tdbDirectoryFile.exists()) {
            LOGGER.error("tdbDirectoryPath does not exist. - Switching to online endpoint.");
            return;
        }
        if (!tdbDirectoryFile.isDirectory()) {
            LOGGER.error("tdbDirectoryPath is not a directory. - Switching to online endpoint.");
            return;
        }

        synonymyBuffer = new HashMap<>();
        hypernymyBuffer = new HashMap<>();

        // dataset and model creation
        tdbDataset = TDBFactory.createDataset(tdbDirectoryPath);
        tdbDataset.begin(ReadWrite.READ);

        linker = new WiktionaryLinker(this);
    }

    /**
     * Use only for constructor call. Developer's remark: Modularized, so that this piece of code can be called from
     * multiple constructors.
     */
    private void initOnline(){
        initBuffers();
        linker = new WiktionaryLinker(this);
        synonymyBuffer = new HashMap<>();
        hypernymyBuffer = new HashMap<>();
        this.useOfflineDatabase = false;
    }

    /**
     * Constructor (only for local purposes)
     */
    public WiktionaryKnowledgeSource() {
        this(DEFAULT_TDB_DIRECTORY_PATH);
    }

    /**
     * Constructor
     * @param useOnlineEndpoint false if tdb shall be used.
     */
    public WiktionaryKnowledgeSource(boolean useOnlineEndpoint) {
        if(useOnlineEndpoint){
            initOnline();
        } else {
            initOffline(DEFAULT_TDB_DIRECTORY_PATH);
        }
    }

    private BTreeMap<String, HashSet<String>> getTranslationBuffer;
    private BTreeMap<String, HashSet<String>> getTranslation_OF_Buffer;

    /**
     * Initialize on-disk buffers.
     */
    private void initBuffers(){
        PersistenceService service = PersistenceService.getService();
        getTranslationBuffer = service.getTranslationBuffer();
        getTranslation_OF_Buffer = service.getTranslation_OF_Buffer();
    }


    /**
     * De-constructor; call before ending the program.
     */
    public void close() {
        tdbDataset.close();
        LOGGER.info("Dataset closed.");
    }


    @Override
    public boolean isInDictionary(String word) {
        return this.isInDictionary(word, Language.ENGLISH);
    }


    /**
     * Language dependent query for existence in the dbnary dictionary.
     * Note that case-sensitivity applies ( (Katze, deu) can be found whereas (katze, deu) will not return any results ).
     *
     * @param word     The word to be looked for.
     * @param language The language of the word.
     * @return boolean indicating whether the word exists in the dictionary in the corresponding language.
     */
    public boolean isInDictionary(String word, Language language) {
        word = encodeWord(word);
        String queryString =
                "PREFIX lexvo: <http://lexvo.org/id/iso639-3/>\r\n" +
                        "PREFIX dbnary: <http://kaiko.getalp.org/dbnary#>\r\n" +
                        "ASK {  <http://kaiko.getalp.org/dbnary/" + language.toWiktionaryChar3() + "/" + word + "> ?p ?o . }";
        try {
            Query query = QueryFactory.create(queryString);
            QueryExecution queryExecution;
            if(useOfflineDatabase) {
                queryExecution = QueryExecutionFactory.create(query, tdbDataset);
            } else {
                queryExecution = QueryExecutionFactory.sparqlService(ONLINE_SPARQL_ENDPOINT, queryString);
            }
            return queryExecution.execAsk();
        } catch(Exception e){
            LOGGER.error("Could not execute query isInDictionary.", e);
            LOGGER.error("Current query:\n" + queryString);
            LOGGER.error("Word: " + word);
        }
        return false;
    }


    @Override
    public HashSet<String> getSynonyms(String linkedConcept) {
        return getSynonyms(linkedConcept, Language.ENGLISH);
    }


    /**
     * Retrieves the synonyms of a particular word in a particular language.
     *
     * @param word     Word for which the synonyms shall be retrieved.
     * @param language Language of the word.
     * @return Set of synonyms.
     */
    public HashSet<String> getSynonyms(String word, Language language) {
        if(word == null) return null;
        word = encodeWord(word);
        if (synonymyBuffer.containsKey(word + "_" + language.toWiktionaryLanguageTag())) {
            return synonymyBuffer.get(word + "_" + language.toWiktionaryLanguageTag());
        }
        HashSet<String> result = new HashSet<>();
        String queryString =
                "PREFIX dbnary: <http://kaiko.getalp.org/dbnary#>\r\n" +
                        "PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>\r\n" +
                        "select distinct ?synonym WHERE {\r\n" +
                        "\r\n" +
                        "{" +
                        // synonyms of described concepts
                        "select distinct ?synonym where {\r\n" +
                        "<http://kaiko.getalp.org/dbnary/" + language.toWiktionaryChar3() + "/" + word + "> <http://kaiko.getalp.org/dbnary#describes> ?descriptionConcepts .\r\n" +
                        "?descriptionConcepts dbnary:synonym ?synonym .\r\n" +
                        "}" +
                        "}\r\n" +
                        "UNION\r\n" +
                        // and now synonyms of senses
                        "{\r\n" +
                        "select distinct ?synonym where {\r\n" +
                        "<http://kaiko.getalp.org/dbnary/" + language.toWiktionaryChar3() + "/" + word + "> <http://kaiko.getalp.org/dbnary#describes> ?descriptionConcepts .\r\n" +
                        "?descriptionConcepts ontolex:sense ?sense .\r\n" +
                        "?sense dbnary:synonym ?synonym .\r\n" +
                        "}\r\n" +
                        "}\r\n" +
                        "}";
        //System.out.println(queryString);
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution;
        if(useOfflineDatabase) {
            queryExecution = QueryExecutionFactory.create(query, tdbDataset);
        } else {
            queryExecution = QueryExecutionFactory.sparqlService(ONLINE_SPARQL_ENDPOINT, queryString);
        }
        ResultSet queryResult = queryExecution.execSelect();
        while (queryResult.hasNext()) {
            result.add(getLemmaFromURI(queryResult.next().getResource("synonym").toString()));
        }
        synonymyBuffer.put(word + "_" + language.toWiktionaryLanguageTag(), result);
        if(result == null || result.size() == 0){
            return null;
        }
        return result;
    }


    /**
     * Given a resource URI, this method will transform it to a lemma.
     *
     * @param uri Resource URI to be transformed.
     * @return Lemma.
     */
    private static String getLemmaFromURI(String uri) {
        return uri.substring(35, uri.length()).replace("_", " ");
    }

    /**
     * Encodes words so that they can be looked up in the wiktionaryMatcher dictionary.
     *
     * @param word Word to be encoded.
     * @return encoded word
     */
    private static String encodeWord(String word) {
        word =  word.replace(" ", "_");
        word = word.replace("\n", "_");
        return word;
    }

    /**
     * Obtain hypernyms for the given concept. The assumed language is English.
     *
     * @param linkedConcept The linked concept for which hypernyms shall be retrieved.
     * @return A set of hypernyms.
     */
    @Override
    public HashSet<String> getHypernyms(String linkedConcept) {
        return this.getHypernyms(linkedConcept, Language.ENGLISH);
    }

    /**
     * Obtain hypernyms for the given concept.
     *
     * @param linkedConcept The linked concept for which hypernyms shall be retrieved.
     * @param language      The desired language of the hypernyms.
     * @return A set of hypernyms (not links).
     */
    public HashSet<String> getHypernyms(String linkedConcept, Language language) {
        linkedConcept = encodeWord(linkedConcept);
        if (hypernymyBuffer.containsKey(linkedConcept + "_" + linkedConcept.toString())) {
            return hypernymyBuffer.get(linkedConcept + "_" + linkedConcept.toString());
        }
        HashSet<String> result = new HashSet<>();
        String queryString = "PREFIX dbnary: <http://kaiko.getalp.org/dbnary#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX dbnarylan: <http://kaiko.getalp.org/dbnary/eng/>\n" +
                "SELECT distinct ?hypernym {\n" +
                "{select ?hypernym where {\n" +
                "dbnarylan:" + linkedConcept + " dbnary:hypernym ?hypernym.\n" +
                "?hypernym rdf:type dbnary:Page .}}\n" +
                "UNION\n" +
                "{select ?hypernym where {\n" +
                "?hs dbnary:hyponym dbnarylan:" + linkedConcept + " .\n" +
                "?hypernym dbnary:describes ?hs .\n" +
                "?hypernym rdf:type dbnary:Page .}}\n" +
                "UNION\n" +
                "{select ?hypernym where {\n" +
                "dbnarylan:" + linkedConcept + " dbnary:describes ?dc .\n" +
                "?dc dbnary:hypernym ?hypernym.\n" +
                "?hypernym rdf:type dbnary:Page .\n" +
                "}}}";
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution;
        if(useOfflineDatabase) {
            queryExecution = QueryExecutionFactory.create(query, tdbDataset);
        } else {
            queryExecution = QueryExecutionFactory.sparqlService(ONLINE_SPARQL_ENDPOINT, queryString);
        }
        ResultSet queryResult = queryExecution.execSelect();
        while (queryResult.hasNext()) {
            result.add(getLemmaFromURI(queryResult.next().getResource("hypernym").toString()));
        }
        hypernymyBuffer.put(linkedConcept + "_" + language.toWiktionaryLanguageTag(), result);
        return result;
    }



    /**
     *
     * @param linkedConcept The concept that was linked.
     * @param sourceLanguage Language of the linked concept.
     * @param targetLanguage Language to which the concept shall be translated.
     * @return The result is not a linked concept but instead a word.
     */
    public HashSet<String> getTranslation(String linkedConcept, Language sourceLanguage, Language targetLanguage) {
        String key = linkedConcept + "_" + sourceLanguage + "_" + targetLanguage;
        if(getTranslationBuffer.containsKey(key)){
            return getTranslationBuffer.get(key);
        }

        HashSet<String> result = new HashSet<>();
        String queryString = "PREFIX dbnary: <http://kaiko.getalp.org/dbnary#>\n" +
                "select distinct ?tp where\n" +
                "{\n" +
                "<http://kaiko.getalp.org/dbnary/" + sourceLanguage.toWiktionaryChar3() + "/" + linkedConcept + "> dbnary:describes ?le .\n" +
                "?t dbnary:isTranslationOf ?le .\n" +
                "?t dbnary:targetLanguage <http://lexvo.org/id/iso639-3/" + targetLanguage.toWiktionaryChar3() + "> .\n" +
                "?t dbnary:writtenForm ?tp .\n" +
                "}";
        try {
            //System.out.println(queryString);
            Query query = QueryFactory.create(queryString);
            QueryExecution queryExecution;
            if(useOfflineDatabase) {
                queryExecution = QueryExecutionFactory.create(query, tdbDataset);
            } else {
                queryExecution = QueryExecutionFactory.sparqlService(ONLINE_SPARQL_ENDPOINT, queryString);
            }
            ResultSet queryResult = queryExecution.execSelect();
            while (queryResult.hasNext()) {
                result.add(queryResult.next().getLiteral("tp").getString());
            }
            getTranslationBuffer.put(key, result);
            return result;
        } catch (Exception e){
            LOGGER.error("Could not execute getTranslation query for concept " + linkedConcept + " (" + sourceLanguage + " to " + targetLanguage + ")");
            LOGGER.error("Problematic Query:\n" + queryString);
            getTranslationBuffer.put(key, new HashSet<>());
            return null;
        }
    }


    /**
     * @param linkedConcept
     * @param sourceLanguage
     * @param targetLanguage
     * @return The result is not a linked concept but instead a word that was normalized.
     */
    public HashSet<String> getNormalizedTranslations(String linkedConcept, Language sourceLanguage, Language targetLanguage){
        HashSet<String> result = new HashSet<>();
        HashSet<String> nonNormalized = getTranslation(linkedConcept, sourceLanguage, targetLanguage);
        if(nonNormalized == null) return null;
        for(String s : nonNormalized){
            result.add(normalizeForTranslations(s));
        }
        return result;
    }


    /**
     * This mehthod also considers translations of synonyms.
     * @param linkedConcept
     * @param sourceLanguage
     * @param targetLanguage
     * @return The result is not a linked concept but instead a word that was normalized.
     */
    public HashSet<String> getNormalizedTranslationsSynonymyExtension(String linkedConcept, Language sourceLanguage, Language targetLanguage){
        if (linkedConcept == null || sourceLanguage == null || targetLanguage == null) return null;
        HashSet<String> result = new HashSet<>();
        HashSet<String> nonNormalized = getTranslation(linkedConcept, sourceLanguage, targetLanguage);

        HashSet<String> synonyms = getSynonyms(linkedConcept, sourceLanguage);
        if(synonyms != null) {
            for (String synonymousLink : synonyms) {
                HashSet<String> furtherTranslations = getTranslation(synonymousLink, sourceLanguage, targetLanguage);
                if (furtherTranslations != null) nonNormalized.addAll(furtherTranslations);
            }
        }

        if(nonNormalized == null) return null;
        for(String s : nonNormalized){
            result.add(normalizeForTranslations(s));
        }
        return result;
    }


    /**
     * Given a translation, find concepts which state that the given translation is their translation.
     * @param translation The translation.
     * @return A set of concepts of which {@code translation} is the given translation.
     */
    public HashSet<String> getTranslationOf(String translation, Language languageOfTranslation){

        // buffer lookup
        String key = translation + "_" + languageOfTranslation;
        if(getTranslation_OF_Buffer.containsKey(key)){
            return getTranslation_OF_Buffer.get(key);
        }

        HashSet<String> result = new HashSet<>();
        String queryString = "";
        if(languageOfTranslation != Language.CHINESE) {
            queryString = "PREFIX dbnary: <http://kaiko.getalp.org/dbnary#>\n" +
                    "select distinct ?c where\n" +
                    "{\n" +
                    "?c dbnary:describes ?le .\n" +
                    "?t dbnary:isTranslationOf ?le .\n" +
                    "?t dbnary:targetLanguage <http://lexvo.org/id/iso639-3/" + languageOfTranslation.toWiktionaryChar3() + "> .\n" +
                    "?t dbnary:writtenForm \"" + translation + "\"@" + languageOfTranslation.toWiktionaryLanguageTag() + " .\n" +
                    "}";
        } else {
            // special case: Chinese
            queryString = "PREFIX dbnary: <http://kaiko.getalp.org/dbnary#>\n" +
                    "select distinct ?c where\n" +
                    "{\n" +
                    "{\n" +
                    "select distinct ?c where\n" +
                    "{\n" +
                    "?c dbnary:describes ?le .\n" +
                    "?t dbnary:isTranslationOf ?le .\n" +
                    "?t dbnary:targetLanguage <http://lexvo.org/id/iso639-3/yue> .\n" +
                    "?t dbnary:writtenForm \"" + translation + "\"@yue .\n" +
                    "}\n" +
                    "}\n" +
                    "UNION\n"+
                    "{\n" +
                    "select ?c where\n" +
                    "{\n" +
                    "?c dbnary:describes ?le .\n" +
                    "?t dbnary:isTranslationOf ?le .\n" +
                    "?t dbnary:targetLanguage <http://lexvo.org/id/iso639-3/cmn> .\n" +
                    "?t dbnary:writtenForm \"" + translation + "\"@cmn .\n" +
                    "}\n" +
                    "}\n" +
                    "}\n";
        }
        try {
            //System.out.println(queryString);
            Query query = QueryFactory.create(queryString);
            QueryExecution queryExecution;
            if(useOfflineDatabase) {
                queryExecution = QueryExecutionFactory.create(query, tdbDataset);
            } else {
                queryExecution = QueryExecutionFactory.sparqlService(ONLINE_SPARQL_ENDPOINT, queryString);
            }
            ResultSet queryResult = queryExecution.execSelect();
            while (queryResult.hasNext()) {
                result.add(queryResult.next().getResource("c").getURI());
            }
            getTranslation_OF_Buffer.put(key, result);
            return result;
        } catch (Exception e){
            LOGGER.error("Could not execute getTranslationOf query for concept " + translation + " (" + languageOfTranslation + ")", e);
            LOGGER.error("Problematic Query:\n" + queryString);
            getTranslation_OF_Buffer.put(key, new HashSet<>());
            return null;
        }
    }


    /**
     * Checks whether the two words are translation of the same word (this mechanism uses another language as common
     * denominator).
     * @param word_1 Word 1 (does not have to be linked).
     * @param language_1 Language 1.
     * @param word_2 Word 2 (does not have to be linked).
     * @param language_2 Language 2.
     * @return True, if a translation can be derived; else false.
     */
    public boolean isTranslationDerived(String word_1, Language language_1, String word_2, Language language_2){
        if(word_1 == null || word_2 == null || language_1 == null || language_2 == null) return false;
        Set<String> concepts_1 = getTranslationOf(word_1, language_1);
        Set<String> concepts_2 = getTranslationOf(word_2, language_2);
        if(concepts_1 == null || concepts_2 == null) return false;
        if(concepts_1.size() == 0 || concepts_2.size() == 0) return false;
        int expected = concepts_1.size() + concepts_2.size();
        Set<String> merge = new HashSet<>();
        merge.addAll(concepts_1);
        merge.addAll(concepts_2);
        return !(expected == merge.size());
    }


    /**
     * Checks whether linkedConceptToBeTranslated can be translated to linkedConcept_2.
     * Note that BOTH concepts have to be linked.
     *
     * @param linkedConceptToBeTranslated
     * @param language_1                  Language of linkedConceptToBeTranslated.
     * @param linkedConcept_2
     * @param language_2                  Language of linkedConcept_2.
     * @return True if translation from linkedConceptToBeTranslated to linkedConcept_2 possible, else false.
     */
    public boolean isTranslationLinked(String linkedConceptToBeTranslated, Language language_1, String linkedConcept_2, Language language_2) {

        // developer note: the only way that works offline is ENG -> ANY_LANGUAGE - else more needs to be downloaded
        if (!language_1.toWiktionaryChar3().equals("eng") && !language_2.toWiktionaryChar3().equals("eng")) {
            LOGGER.error("Currently only English translations are supported.");
            return false;
        }

        HashSet<String> translations_1 = getTranslation(linkedConceptToBeTranslated, language_1, language_2);
        for (String translated : translations_1) {
            translated = normalizeForTranslations(translated);
            linkedConcept_2 = normalizeForTranslations(linkedConcept_2);
            if (translated.equals(linkedConcept_2)) return true;
        }

        // try reverse lookup
        HashSet<String> translations_2 = getTranslation(linkedConcept_2, language_2, language_1);
        for (String translated : translations_2) {
            translated = normalizeForTranslations(translated);
            linkedConceptToBeTranslated = normalizeForTranslations(linkedConceptToBeTranslated);
            if (translated.equals(linkedConceptToBeTranslated)) return true;
        }
        return false;
    }

    /**
     * Checks whether linkedConceptToBeTranslated can be translated to linkedConcept_2.
     * Note that the first concept has to be linked.
     *
     * @param linkedConceptToBeTranslated
     * @param language_1                  Language of linkedConceptToBeTranslated.
     * @param nonlinkedConcept_2
     * @param language_2                  Language of linkedConcept_2.
     * @return True if translation from linkedConceptToBeTranslated to linkedConcept_2 possible, else false.
     */
    public boolean isTranslationNonLinked(String linkedConceptToBeTranslated, Language language_1, String nonlinkedConcept_2, Language language_2) {

        // developer note: the only way that works offline is ENG -> ANY_LANGUAGE - else more needs to be downloaded
        if (!language_1.toWiktionaryChar3().equals("eng") && !language_2.toWiktionaryChar3().equals("eng")) {
            LOGGER.error("Currently only English translations are supported.");
            return false;
        }

        HashSet<String> translations_1 = getTranslation(linkedConceptToBeTranslated, language_1, language_2);
        for (String translated : translations_1) {
            translated = normalizeForTranslations(translated);
            nonlinkedConcept_2 = normalizeForTranslations(nonlinkedConcept_2);
            if (translated.equals(nonlinkedConcept_2)) return true;
        }

        // try reverse lookup
        String linkedConcept_2 = linker.linkToSingleConcept(nonlinkedConcept_2, language_2);
        if(linkedConcept_2 != null) {
            HashSet<String> translations_2 = getTranslation(linkedConcept_2, language_2, language_1);
            for (String translated : translations_2) {
                translated = normalizeForTranslations(translated);
                linkedConceptToBeTranslated = normalizeForTranslations(linkedConceptToBeTranslated);
                if (translated.equals(linkedConceptToBeTranslated)) return true;
            }
        }
        return false;
    }

    /**
     * This method removes characters that are not valid in a sparql query.
     * @param stringToMask
     * @return
     */
    public static String removeIllegalCharactersForSparqlQuery(String stringToMask){
        return stringToMask.replaceAll("\"", "");
    }

    /**
     * Normalization Function for translations.
     *
     * @param setToBeNormalized Set whose strings shall be normalized.
     * @return HashSet with Normalized Strings.
     */
    public static HashSet<String> normalizeForTranslations(HashSet<String> setToBeNormalized) {
        HashSet<String> result = new HashSet<>();
        for (String s : setToBeNormalized) {
            result.add(normalizeForTranslations(s));
        }
        return result;
    }

    public static String normalizeForTranslations(String stringToBeNormalized) {
        String result = stringToBeNormalized.toLowerCase();
        result = result.trim();
        result = result.replace(" ", "_");
        result = result.replace("-", "_");
        return result;
    }

    @Override
    public LabelToConceptLinker getLinker() {
        return this.linker;
    }

    @Override
    public String getName() {
        return "Wiktionary";
    }

    public boolean isUseOfflineDatabase() {
        return useOfflineDatabase;
    }

    public void setUseOfflineDatabase(boolean useOfflineDatabase) {
        this.useOfflineDatabase = useOfflineDatabase;
        if(useOfflineDatabase){
            initOffline(this.tdbDirectoryPath);
        } else {
            initOnline();
        }
    }
}
