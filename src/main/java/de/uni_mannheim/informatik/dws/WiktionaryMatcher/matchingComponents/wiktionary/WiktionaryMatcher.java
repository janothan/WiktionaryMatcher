package de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.wiktionary;

import de.uni_mannheim.informatik.dws.WiktionaryMatcher.LabelBasedMatcher;
import de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.util.UriLabelInfo;
import de.uni_mannheim.informatik.dws.WiktionaryMatcher.services.IOoperations;
import de.uni_mannheim.informatik.dws.WiktionaryMatcher.services.StringOperations;
import de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.util.LabelLanguageTuple;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wiktionary.WiktionaryKnowledgeSource;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wiktionary.WiktionaryLinker;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.DefaultExtensions;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Matcher exploiting Wiktionary.
 * This matcher assumes that one of the String matchers has ben run before.
 */
public class WiktionaryMatcher extends LabelBasedMatcher {


    /**
     * Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(WiktionaryMatcher.class);

    /**
     * Linker used to link labels to wiktionary concepts.
     */
    private WiktionaryLinker linker;

    /**
     * Tracks clashed labels.
     */
    private HashMap<String, HashSet<String>> clashedLabels;

    /**
     * the wiktionary to be used
     */
    private WiktionaryKnowledgeSource wiktionary;

    /**
     * Labels longer than the linking limit will be ignored and not mapped.
     */
    private int linkingLimit = 100;

    /**
     * Multilingual mode of matcher. Default false.
     */
    private boolean isInMultiLingualMode = false;

    /**
     * Constructor
     */
    public WiktionaryMatcher() {
        this(false);
    }

    private static final String TDB_DIRECTORY = "./oaei-resources/tdb";

    /**
     * Constructor
     *
     * @param isInMultilingualMode true if the ontologies are in different languages.
     */
    public WiktionaryMatcher(boolean isInMultilingualMode) {
        this.wiktionary = new WiktionaryKnowledgeSource(TDB_DIRECTORY);
        this.linker = (WiktionaryLinker) this.wiktionary.getLinker();
        isInMultiLingualMode = isInMultilingualMode;
    }

    @Override
    public Alignment match(OntModel sourceOntology, OntModel targetOntology, Alignment m, Properties p) throws Exception {
        ontology1 = sourceOntology;
        ontology2 = targetOntology;
        alignment = m;
        loadLabels(sourceOntology, targetOntology);

        if (!isInMultiLingualMode) {
            complexMonolingualMatch(uri2labelMapClasses_1.getUriLabelMap(), uri2labelMapClasses_2.getUriLabelMap(), "classes");
            complexMonolingualMatch(uri2labelMapDatatypeProperties_1.getUriLabelMap(), uri2labelMapDatatypeProperties_2.getUriLabelMap(), "datatypeProperties");
            complexMonolingualMatch(uri2labelMapObjectProperties_1.getUriLabelMap(), uri2labelMapObjectProperties_2.getUriLabelMap(), "objectProperties");
            complexMonolingualMatch(uri2labelMapRemainingProperties_1.getUriLabelMap(), uri2labelMapRemainingProperties_2.getUriLabelMap(), "remainingProperties");
        } else {
            complexMultilingualMatch(uri2labelMapClasses_1, uri2labelMapClasses_2, "classes");
            complexMultilingualMatch(uri2labelMapDatatypeProperties_1, uri2labelMapDatatypeProperties_2, "datatypeProperties");
            complexMultilingualMatch(uri2labelMapObjectProperties_1, uri2labelMapObjectProperties_2, "objectProperties");
            complexMultilingualMatch(uri2labelMapRemainingProperties_1, uri2labelMapRemainingProperties_2, "remainingProperties");
        }

        LOGGER.info("Wiktionary Matcher Completed");
        return this.alignment;
    }

    /**
     * Matching orchestration for monolingual matching (ontologies are in different languages).
     *
     * @param uriLabelMap_1 Map 1 (source).
     * @param uriLabelMap_2 Map 2 (target).
     * @param what          What is matched.
     */
    private void complexMultilingualMatch(UriLabelInfo uriLabelMap_1, UriLabelInfo uriLabelMap_2, String what) {
        // step 1
        LOGGER.info("Beginning full string translation matching for " + what + ".");
        performFullStringTranslationMatch(uriLabelMap_1, uriLabelMap_2);
        LOGGER.info("Full string translation matching for " + what + " performed.");

        // step 2: induced mappings
        if (this.alignment.size() <= 5) {
            LOGGER.info("Running relaxed translation matching.");
            performBridgedFullStringTranslationMatch(uriLabelMap_1, uriLabelMap_2);
            LOGGER.info("Running relaxed translation matching completed.");
        }
    }

    /**
     * Translates the full given string.
     *
     * @param uriLabelMap_1 Map 1 (source).
     * @param uriLabelMap_2 map 2 (target)
     */
    private void performFullStringTranslationMatch(UriLabelInfo uriLabelMap_1, UriLabelInfo uriLabelMap_2) {
        LOGGER.info("Building Map:  Uri -> Link");
        HashMap<String, HashSet<LabelLanguageTuple>> uris2linksSource_1 = convertToUriLinkMapWithLanguage(uriLabelMap_1.getUriLabelMap(), true);
        HashMap<String, HashSet<LabelLanguageTuple>> uris2linksTarget_2 = convertToUriLinkMapWithLanguage(uriLabelMap_2.getUriLabelMap(), false);
        LOGGER.info("BuildingMap finished: Uri -> Link Map");

        // normalized index for translation lookup
        SimpleIndexerMultilangual simpleIndexerMultilangual_1 = new SimpleIndexerMultilangual(uriLabelMap_1);
        SimpleIndexerMultilangual simpleIndexerMultilangual_2 = new SimpleIndexerMultilangual(uriLabelMap_2);

        // translate from link 1, normalize and look up in index of ontology 2
        for (HashMap.Entry<String, HashSet<LabelLanguageTuple>> entry : uris2linksSource_1.entrySet()) {
            for (LabelLanguageTuple tuple : entry.getValue()) {
                Language tupleLanguage = tuple.language;
                if (tuple.language == Language.UNKNOWN) tupleLanguage = mostFrequentLanguage_1;

                // assuming most frequent language for 2 is a simplification, but works for OAEI b/c ontologies are all monolingual
                for (String normalizedTranslation : wiktionary.getNormalizedTranslations(tuple.label, tupleLanguage, mostFrequentLanguage_2)) {
                    if (simpleIndexerMultilangual_2.index.containsKey(normalizedTranslation)) {
                        List<String> matchResults = simpleIndexerMultilangual_2.index.get(normalizedTranslation);
                        for (String uri2 : matchResults) {
                            String explanation = "The label of entity 1 was found in Wiktionary as '" + tuple.label + "' and translated " +
                                    "to '" + normalizedTranslation + "' which equals the normalized label of entity 2.";
                            addCorrespondence(entry.getKey(), uri2, explanation);
                        }
                    }
                }
            }
        }

        // translate from link 2, normalize and look up in index of ontology 1
        for (HashMap.Entry<String, HashSet<LabelLanguageTuple>> entry : uris2linksTarget_2.entrySet()) {
            for (LabelLanguageTuple tuple : entry.getValue()) {
                Language tupleLanguage = tuple.language;
                if (tuple.language == Language.UNKNOWN) tupleLanguage = mostFrequentLanguage_2;

                // assuming most frequent language for 2 is a simplification, but works for OAEI b/c ontologies are all monolingual
                for (String normalizedTranslation : wiktionary.getNormalizedTranslations(tuple.label, tupleLanguage, mostFrequentLanguage_1)) {
                    if (simpleIndexerMultilangual_1.index.containsKey(normalizedTranslation)) {
                        List<String> matchResults = simpleIndexerMultilangual_1.index.get(normalizedTranslation);
                        for (String uri1 : matchResults) {
                            String explanation = "Entity 2 was found in Wiktionary as '" + tuple.label + "' and translated " +
                                    "to '" + normalizedTranslation + "' which equals the normalized label of entity 1.";
                            addCorrespondence(uri1, entry.getKey(), explanation);
                        }
                    }
                }
            }
        }
    }


    /**
     * Translates the full given string using English as bridging language.
     *
     * @param uriLabelMap_1 Map 1 (source).
     * @param uriLabelMap_2 map 2 (target)
     */
    private void performBridgedFullStringTranslationMatch(UriLabelInfo uriLabelMap_1, UriLabelInfo uriLabelMap_2) {
        nextEntry1:
        for (HashMap.Entry<String, HashSet<LabelLanguageTuple>> entry_1 : uriLabelMap_1.getUriLabelMap().entrySet()) {

            // only work with entries that have not been mapped before
            if (alignment.isSourceContained(entry_1.getKey())) continue nextEntry1;

            // only work with entries that have a translation
            if (!isTranslationOfEntryAvailable(entry_1)) continue nextEntry1;

            nextEntry2:
            for (HashMap.Entry<String, HashSet<LabelLanguageTuple>> entry_2 : uriLabelMap_2.getUriLabelMap().entrySet()) {

                // only work with entries that have not been mapped before
                if (alignment.isTargetContained(entry_2.getKey())) continue nextEntry2;

                // only work with entries that have a translation
                if (!isTranslationOfEntryAvailable(entry_2)) continue nextEntry2;

                for (LabelLanguageTuple tuple_1 : entry_1.getValue()) {
                    for (LabelLanguageTuple tuple_2 : entry_2.getValue()) {

                        Language tuple_1_Language = tuple_1.language;
                        if (tuple_1.language == Language.UNKNOWN) tuple_1_Language = mostFrequentLanguage_1;
                        Language tuple_2_language = tuple_2.language;
                        if (tuple_2.language == Language.UNKNOWN) tuple_2_language = mostFrequentLanguage_2;

                        if (wiktionary.isTranslationDerived(tuple_1.label, tuple_1_Language, tuple_2.label, tuple_2_language)) {
                            String explanation = "Entity 1 was found in Wiktionary as translation for the same concept for which Entity 2 is a translation of.";
                            addCorrespondence(entry_1.getKey(), entry_2.getKey(), explanation, 0.5);
                        }

                    } // loop over labels 2
                } // loop over labels 1

            } // loop over uri label map 2
        } // loop over uri label map 1
    }

    /**
     * Check whether there is a bridged translation available at all.
     *
     * @param entry The entry for which the check shall be performed.
     * @return True if available, else false.
     */
    private boolean isTranslationOfEntryAvailable(HashMap.Entry<String, HashSet<LabelLanguageTuple>> entry) {
        // check whether there is a translation at all
        boolean translation1available = false;
        for (LabelLanguageTuple tuple_1 : entry.getValue()) {
            Language tuple_1_Language = tuple_1.language;
            if (tuple_1.language == Language.UNKNOWN) tuple_1_Language = mostFrequentLanguage_1;
            HashSet<String> translations = wiktionary.getTranslationOf(tuple_1.label, tuple_1_Language);
            if (translations != null && translations.size() > 0) translation1available = true;
        }
        return translation1available;
    }

    /**
     * This method transforms the uri2labels into a uri2links HashMap with language annotations.
     * Thereby, the linking function is called only once.
     * Furthermore, concepts that cannot be linked are not included in the resulting HashMap.
     * Mapped entries are not linked.
     *
     * @param uriLabelMap Input HashMap URI -> (labels, language)
     * @return HashMap URI -> (links, language)
     */
    private HashMap<String, HashSet<LabelLanguageTuple>> convertToUriLinkMapWithLanguage(HashMap<String, HashSet<LabelLanguageTuple>> uriLabelMap, boolean isSourceOntology) {
        HashMap<String, HashSet<LabelLanguageTuple>> result = new HashMap<>();
        for (Map.Entry<String, HashSet<LabelLanguageTuple>> uri2label : uriLabelMap.entrySet()) {

            // check whether already mapped (intuition: do not map something that has been mapped before by more accurate algorithm)
            if (isSourceOntology && mappingExistsForSourceURI(uri2label.getKey())) {
                continue;
            } else if (!isSourceOntology && mappingExistsForTargetURI(uri2label.getKey())) {
                continue;
            }

            HashSet<LabelLanguageTuple> links = new HashSet();
            for (LabelLanguageTuple tuple : uri2label.getValue()) {
                if (tuple.label.length() <= linkingLimit) {
                    Language language = tuple.language;
                    if (language == Language.UNKNOWN) {
                        if (isSourceOntology) language = mostFrequentLanguage_1;
                        else language = mostFrequentLanguage_2;
                    }
                    String linkedConcept = linker.linkToSingleConcept(tuple.label, language); // add language
                    if (linkedConcept != null) {
                        links.add(new LabelLanguageTuple(linkedConcept, tuple.language));
                    }
                }
            } // for loop over individual labels

            if (links.size() > 0) {
                result.put(uri2label.getKey(), links);
            }
        } // for loop over whole map
        return result;
    }

    /**
     * Matching orchestration for monolingual matching (both ontologies are in the same language).
     *
     * @param uriLabelMap_1 Map 1 (source).
     * @param uriLabelMap_2 Map 2 (target).
     * @param what          What is matched.
     */
    private void complexMonolingualMatch(HashMap<String, HashSet<LabelLanguageTuple>> uriLabelMap_1, HashMap<String, HashSet<LabelLanguageTuple>> uriLabelMap_2, String what) {

        // step 1: (more complex approach) look up whole label in background source
        LOGGER.info("Beginning full string synonymy matching for " + what + ".");
        performFullStringSynonymyMatching(uriLabelMap_1, uriLabelMap_2);
        LOGGER.info("Full string synonymy matching for " + what + " performed.");

        // step 2: (more complex approach) look up long sub-parts in the label
        // issues: what to do with partial mappings, performance...
        LOGGER.info("Beginning longest string synonymy matching for " + what + ".");
        performLongestStringSynonymyMatching(uriLabelMap_1, uriLabelMap_2);
        LOGGER.info("Longest string synonymy matching for " + what + " performed.");

        // step 3:
        LOGGER.info("Beginning token based synonymy matching for" + what + ".");
        performTokenBasedSynonymyMatching(uriLabelMap_1, uriLabelMap_2);
        LOGGER.info("Token based synonymy matching for " + what + " performed.");
    }

    private final static String explanationFullStringSynonymyMatching = "";
    private String runningExplanation = "";

    /**
     * Adds a correspondence (i.e., a mapping) to the alignment.
     * A default confidence of 1.0 is assumed.
     *
     * @param uri_1       URI 1.
     * @param uri_2       URI 2.
     * @param explanation Explanation why a match was performed.
     */
    private void addCorrespondence(String uri_1, String uri_2, String explanation) {
        addCorrespondence(uri_1, uri_2, explanation, 1.0);
    }

    /**
     * Adds a correspondence (i.e., a mapping) to the alignment.
     *
     * @param uri_1       URI 1.
     * @param uri_2       URI 2.
     * @param explanation Explanation why a match was performed.
     * @param confidence  The confidence
     */
    private void addCorrespondence(String uri_1, String uri_2, String explanation, double confidence) {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put(DefaultExtensions.DublinCore.DESCRIPTION.toString(), explanation);
        Correspondence newCorrespondence = new Correspondence(uri_1, uri_2, confidence, CorrespondenceRelation.EQUIVALENCE, extensions);
        LOGGER.info("New correspondence (" + uri_1 + "," + uri_2 + "): " + explanation);
        alignment.add(newCorrespondence);
    }

    /**
     * Filter out token synonymy utilizing a synonymy strategy.
     * Note that the method accepts a HashMap of Uri -> set(LINKS) rather than Uri -> set(labels).
     *
     * @param uri2labelMap_1 URI2labels map of the source ontology.
     * @param uri2labelMap_2 URI2labels map of the target ontology.
     */
    private void performFullStringSynonymyMatching(HashMap<String, HashSet<LabelLanguageTuple>> uri2labelMap_1,
                                                   HashMap<String, HashSet<LabelLanguageTuple>> uri2labelMap_2) {

        LOGGER.info("Building Map:  Uri -> Link");
        HashMap<String, HashSet<String>> uris2linksSource_1 = convertToUriLinkMap(uri2labelMap_1, true);
        HashMap<String, HashSet<String>> uris2linksTarget_2 = convertToUriLinkMap(uri2labelMap_2, false);
        LOGGER.info("Building Map finished: Uri -> Link Map");

        for (Map.Entry<String, HashSet<String>> uri2linksSource_1 : uris2linksSource_1.entrySet()) {
            for (Map.Entry<String, HashSet<String>> uri2linksTarget_2 : uris2linksTarget_2.entrySet()) {
                if (fullMatchUsingDictionaryWithLinks(uri2linksSource_1.getValue(), uri2linksTarget_2.getValue())) {
                    addCorrespondence(uri2linksSource_1.getKey(), uri2linksTarget_2.getKey(), runningExplanation);
                    LOGGER.info(uri2linksSource_1.getKey() + " " + uri2linksTarget_2.getKey() + " (full word synonymy match)");
                    LOGGER.info(uri2linksSource_1.getKey() + ": (" + IOoperations.convertHashSetLabelLanguageTupleToStringPipeSeparated(uri2labelMap_1.get(uri2linksSource_1.getKey())) + ")");
                    LOGGER.info(uri2linksTarget_2.getKey() + ": (" + IOoperations.convertHashSetLabelLanguageTupleToStringPipeSeparated(uri2labelMap_2.get(uri2linksTarget_2.getKey())) + ")");
                    LOGGER.info(runningExplanation);
                }
            } // end of target ontology loop
        } // end of source ontology loop
    }

    /**
     * Determines whether two sets of links match using the internal wiktionary dictionary.
     * Not that no linking is performed but links are expected in the given sets.
     *
     * @param linkSet1 Set 1 of links of source.
     * @param linkSet2 Set 2 of links of target.
     * @return true if there is a match, else false.
     */
    public boolean fullMatchUsingDictionaryWithLinks(HashSet<String> linkSet1, HashSet<String> linkSet2) {
        outerSet:
        for (String lookupTerm1 : linkSet1) {
            for (String lookupTerm2 : linkSet2) {
                if (lookupTerm2.length() <= linkingLimit) {
                    if (compare(lookupTerm1, lookupTerm2)) {
                        runningExplanation = "The first concept was mapped to dictionary entry [" + lookupTerm1 + "] and the second concept was mapped to dictionary entry [" + lookupTerm2 + "]. " +
                                "According to Wiktionary, those two concepts are synonymous.";
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Match based on token equality and synonymy.
     *
     * @param uri2labelMap_1 source uri2labels map
     * @param uri2labelMap_2 target uri2labels map
     */
    private void performTokenBasedSynonymyMatching(HashMap<String, HashSet<LabelLanguageTuple>> uri2labelMap_1, HashMap<String, HashSet<LabelLanguageTuple>> uri2labelMap_2) {
        LOGGER.info("Beginning to convert to URI -> Tokens map.");
        HashMap<String, List<HashSet<String>>> uri2tokensMap_1 = convertToUriTokenLinkMap(uri2labelMap_1, true);
        HashMap<String, List<HashSet<String>>> uri2tokensMap_2 = convertToUriTokenLinkMap(uri2labelMap_2, false);
        LOGGER.info("Conversion completed to URI -> Tokens map.");

        for (HashMap.Entry<String, List<HashSet<String>>> uri2tokenlists_1 : uri2tokensMap_1.entrySet()) {
            for (HashMap.Entry<String, List<HashSet<String>>> uri2tokenlists_2 : uri2tokensMap_2.entrySet()) {
                if (isTokenSetSynonymous(uri2tokenlists_1.getValue(), uri2tokenlists_2.getValue())) {
                    String uri1 = uri2tokenlists_1.getKey();
                    String uri2 = uri2tokenlists_2.getKey();
                    addCorrespondence(uri1, uri2, runningExplanation);
                    LOGGER.info(uri1 + " " + uri2 + " (token based synonymy match)");
                    LOGGER.info(uri1 + ": (" + IOoperations.convertHashSetLabelLanguageTupleToStringPipeSeparated(uri2labelMap_1.get(uri1)) + ")");
                    LOGGER.info(uri2 + ": (" + IOoperations.convertHashSetLabelLanguageTupleToStringPipeSeparated(uri2labelMap_2.get(uri2)) + ")");
                    LOGGER.info(runningExplanation);
                }
            }
        }
    }

    /**
     * Checks whether the two lists are synonymous, this means that:
     * each component of one list can be found in the other list OR is synonymous to one component in the other list.
     *
     * @param tokenList1 List of words
     * @param tokenList2 List of words
     * @return true if synonymous, else false
     */
    public boolean isTokenSetSynonymous(List<HashSet<String>> tokenList1, List<HashSet<String>> tokenList2) {
        for (HashSet<String> set_1 : tokenList1) {
            for (HashSet<String> set_2 : tokenList2) {
                if (isTokenSynonymous(set_1, set_2)) {
                    runningExplanation = "The source concept was mapped to dictionary entries [" + StringOperations.getCommaSeparatedString(set_1) +
                            "] and the target concept was mapped to dictionary entries [" + StringOperations.getCommaSeparatedString(set_2) + "]. " +
                            "According to Wiktionary, each of the entries of one ontology concept has a synonymous partner in the set of entries in the other ontology.";
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Compare the two maps for synonymous terms.
     *
     * @param set1 Set of tokens 1 (not links!)
     * @param set2 Set of tokens 2 (not links!)
     * @return true if the term of a set has a synonymous or equal counterpart in the other set. T
     * his is tested both ways (set1 -> set2 and set2 -> set1).
     */
    public boolean isTokenSynonymous(HashSet<String> set1, HashSet<String> set2) {

        if (set1.size() != set2.size()) {
            return false;
        }

        // required to avoid modification to the passed sets
        HashSet<String> workingSet1 = new HashSet<>(set1);
        HashSet<String> workingSet2 = new HashSet<>(set2);

        //
        workingSet1.removeAll(set2);
        workingSet2.removeAll(set1);


        int s2mapped = 0;

        // set 1 check
        nextS1:
        for (String s1 : workingSet1) {
            if (s1.endsWith("_not_linkable")) {
                // non-linkable label that was not yet filtered out -> there will be no partner
                return false;
            }

            nextS2:
            for (String s2 : workingSet2) {
                if (s2.endsWith("_not_linkable")) {
                    // non-linkable label that was not yet filtered out -> there will be no partner
                    return false;
                }
                if (compare(s1, s2)) {
                    s2mapped++;
                    continue nextS1;
                }
            }
            // there was no match for the current s1
            return false;
        }

        if (s2mapped < workingSet2.size()) {
            // the second set could not be mapped

            nextS2:
            for (String s2 : workingSet2) {
                if (s2.endsWith("_not_linkable")) {
                    // non-linkable label that was not yet filtered out -> there will be no partner
                    return false;
                }
                for (String s1 : workingSet1) {
                    if (compare(s1, s2)) {
                        s2mapped++;
                        continue nextS2;
                    }
                }
                // there was no match for the current s2
                return false;
            }
        }
        return true;
    }


    /**
     * Match by determining multiple concepts for a label.
     *
     * @param uri2labelMap_1 URI2label map 1.
     * @param uri2labelMap_2 URI2label map 2.
     */
    private void performLongestStringSynonymyMatching(HashMap<String, HashSet<LabelLanguageTuple>> uri2labelMap_1, HashMap<String, HashSet<LabelLanguageTuple>> uri2labelMap_2) {
        LOGGER.info("Building URI 2 n-links map.");
        HashMap<String, List<HashSet<String>>> uri2linksMap_1 = convertToUriLinksMap(uri2labelMap_1, true);
        HashMap<String, List<HashSet<String>>> uri2linksMap_2 = convertToUriLinksMap(uri2labelMap_2, false);
        LOGGER.info("URI 2 n-links map built.");

        for (HashMap.Entry<String, List<HashSet<String>>> uri2links_1 : uri2linksMap_1.entrySet()) {
            for (HashMap.Entry<String, List<HashSet<String>>> uri2links_2 : uri2linksMap_2.entrySet()) {
                if (isLinkListSynonymous(uri2links_1.getValue(), uri2links_2.getValue())) {
                    addCorrespondence(uri2links_1.getKey(), uri2links_2.getKey(), runningExplanation);
                    LOGGER.info(uri2links_1.getKey() + " " + uri2links_2.getKey() + " (longest string synonymy match)");
                    LOGGER.info(uri2links_1.getKey() + ": (" + IOoperations.convertHashSetLabelLanguageTupleToStringPipeSeparated(uri2labelMap_1.get(uri2links_1.getKey())) + ")");
                    LOGGER.info(uri2links_2.getKey() + ": (" + IOoperations.convertHashSetLabelLanguageTupleToStringPipeSeparated(uri2labelMap_2.get(uri2links_2.getKey())) + ")");
                    LOGGER.info(runningExplanation);
                }
            }
        }
    }


    /**
     * Given two lists of links, this method checks whether those are synonymous.
     *
     * @param list_1 List of links 1.
     * @param list_2 List of links 2.
     * @return Returns true, if the links are synonymous.
     */
    private boolean isLinkListSynonymous(List<HashSet<String>> list_1, List<HashSet<String>> list_2) {
        for (HashSet<String> set_1 : list_1) {
            for (HashSet<String> set_2 : list_2) {
                if (isLinkSetSynonymous(set_1, set_2)) {
                    runningExplanation = "The source concept was mapped to dictionary entries [" + StringOperations.getCommaSeparatedString(set_1) +
                            "] and the target concept was mapped to dictionary entries [" + StringOperations.getCommaSeparatedString(set_2) + "]. " +
                            "According to Wiktionary, each of the entries of one ontology concept has a synonymous partner in the set of entries in the other ontology.";
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * All components of set_1 have to be synonymous to components in set_2.
     *
     * @param set_1
     * @param set_2
     * @return
     */
    private boolean isLinkSetSynonymous(HashSet<String> set_1, HashSet<String> set_2) {

        if (set_1.size() != set_2.size()) {
            return false;
        }

        // required to avoid modification to the passed sets
        HashSet<String> workingSet1 = new HashSet<>(set_1);
        HashSet<String> workingSet2 = new HashSet<>(set_2);

        // remove duplicate concepts
        workingSet1.removeAll(set_2);
        workingSet2.removeAll(set_1);

        HashSet<String> set2covered = new HashSet<>();

        // set 1 check
        for (String s1 : workingSet1) {
            nextS2:
            for (String s2 : workingSet2) {
                if (set2covered.contains(s2)) continue nextS2; // already mapped
                if (compare(s1, s2)) {
                    set2covered.add(s2);
                } else {
                    // -> not strong form synonymous, no counterpart, return false
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * This method converts a URIs -> labels HashMap to a URIs -> List<nlinks>.
     * Mapped entries are ignored.
     *
     * @param uris2labels URIs to labels map.
     * @return URI -> tokens
     */
    private HashMap<String, List<HashSet<String>>> convertToUriLinksMap(HashMap<String, HashSet<LabelLanguageTuple>> uris2labels, boolean isSourceOntology) {
        HashMap<String, List<HashSet<String>>> result = new HashMap<>();
        for (HashMap.Entry<String, HashSet<LabelLanguageTuple>> uri2labels : uris2labels.entrySet()) {

            // filter out what has been mapped before
            if (isSourceOntology && mappingExistsForSourceURI(uri2labels.getKey())) {
                continue;
            } else if (!isSourceOntology && mappingExistsForTargetURI(uri2labels.getKey())) {
                continue;
            }

            List<HashSet<String>> list = new LinkedList<>();
            for (LabelLanguageTuple tuple : uri2labels.getValue()) {
                if (tuple.label.length() <= linkingLimit) {
                    HashSet<String> linkedConcepts = linker.linkToPotentiallyMultipleConcepts(tuple.label);
                    if (linkedConcepts != null) {
                        list.add(linkedConcepts);
                    }
                }
            }
            if (list.size() > 0) {
                result.put(uri2labels.getKey(), list);
            }
        }
        return result;
    }

    /**
     * Check whether the specified word is synonymous to a word in the given set.
     *
     * @param word Word to be checked.
     * @param set  Set containing the words.
     * @return true if synonymous.
     */
    private boolean setContainsSynonym(String word, HashSet<String> set) {
        String linkedWord = linker.linkToSingleConcept(word);
        for (String s : set) {
            if (compare(linkedWord, linker.linkToSingleConcept(s))) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method transforms the uri2labels into a uri2links HashMap.
     * Thereby, the linking function is called only once.
     * Furthermore, concepts that cannot be linked are not inlcuded in the resulting HashMap.
     * Mapped entries are not linked.
     *
     * @param uri2labels Input HashMap URI -> labels
     * @return HashMap URI -> links
     */
    private HashMap<String, HashSet<String>> convertToUriLinkMap(HashMap<String, HashSet<LabelLanguageTuple>> uri2labels, boolean isSourceOntology) {
        HashMap<String, HashSet<String>> result = new HashMap<>();
        for (Map.Entry<String, HashSet<LabelLanguageTuple>> uri2label : uri2labels.entrySet()) {

            // check whether already mapped (intuition: do not map something that has been mapped before by more accurate algorithm)
            if (isSourceOntology && mappingExistsForSourceURI(uri2label.getKey())) {
                continue;
            } else if (!isSourceOntology && mappingExistsForTargetURI(uri2label.getKey())) {
                continue;
            }

            HashSet<String> links = new HashSet();
            for (LabelLanguageTuple tuple : uri2label.getValue()) {
                if (tuple.label.length() <= linkingLimit) {
                    String linkedConcept = linker.linkToSingleConcept(tuple.label);
                    if (linkedConcept != null) {
                        links.add(linkedConcept);
                    }
                }
            } // for loop over individual labels

            if (links.size() > 0) {
                result.put(uri2label.getKey(), links);
            }
        } // for loop over whole map
        return result;
    }

    /**
     * This method converts a URIs -> labels HashMap to a URIs -> tokens HashMap.
     * Mapped entries are ignored.
     *
     * @param uris2labels URIs to labels map.
     * @return URI -> tokens
     */
    private HashMap<String, List<HashSet<String>>> convertToUriTokenMap(HashMap<String, HashSet<String>> uris2labels, boolean isSourceOntology) {
        HashMap<String, List<HashSet<String>>> result = new HashMap<>();
        for (Map.Entry<String, HashSet<String>> uri2label : uris2labels.entrySet()) {
            // check whether already mapped (intuition: do not map something that has been mapped before by a more accurate algorithm)
            if (isSourceOntology && mappingExistsForSourceURI(uri2label.getKey())) {
                continue;
            } else if (!isSourceOntology && mappingExistsForTargetURI(uri2label.getKey())) {
                continue;
            }
            List<HashSet<String>> listOfTokenSequences = new LinkedList<>();
            for (String label : uri2label.getValue()) {
                if (label.length() <= linkingLimit) {
                    listOfTokenSequences.add(tokenizeAndFilter(label));
                }
            }
            result.put(uri2label.getKey(), listOfTokenSequences);
        } // end of for loop over whole map
        return result;
    }

    /**
     * This method converts a URIs -> labels HashMap to a URIs -> links HashMap where non-linkable terms are depicted as "token_not_linkable".
     * Mapped entries are ignored.
     *
     * @param uris2labels URIs to labels map.
     * @return URI -> links
     */
    private HashMap<String, List<HashSet<String>>> convertToUriTokenLinkMap(HashMap<String, HashSet<LabelLanguageTuple>> uris2labels, boolean isSourceOntology) {
        HashMap<String, List<HashSet<String>>> result = new HashMap<>();
        for (Map.Entry<String, HashSet<LabelLanguageTuple>> uri2label : uris2labels.entrySet()) {
            // check whether already mapped (intuition: do not map something that has been mapped before by a more accurate algorithm)
            if (isSourceOntology && mappingExistsForSourceURI(uri2label.getKey())) {
                continue;
            } else if (!isSourceOntology && mappingExistsForTargetURI(uri2label.getKey())) {
                continue;
            }
            List<HashSet<String>> listOfTokenSequences = new LinkedList<>();
            for (LabelLanguageTuple tuple : uri2label.getValue()) {
                if (tuple.label.length() <= linkingLimit) {
                    HashSet<String> tokens = tokenizeAndFilter(tuple.label);
                    HashSet<String> resultingLinks = new HashSet<>();
                    for (String token : tokens) {
                        String link = linker.linkToSingleConcept(token);
                        if (link != null) {
                            resultingLinks.add(link);
                        } else {
                            resultingLinks.add(token + "_not_linkable");
                        }
                    }
                }
            }
            result.put(uri2label.getKey(), listOfTokenSequences);
        } // end of for loop over whole map
        return result;
    }

    /**
     * Tokenizes a label and filters out stop words.
     *
     * @param label The label to be tokenized.
     * @return Tokenized label.
     */
    public static HashSet<String> tokenizeAndFilter(String label) {
        // camelcase resolution
        label = label.replaceAll("(?<!^)(?<!\\s)(?=[A-Z][a-z])", "_");

        // delete everything that is not a-z and A-Z
        label = label.replaceAll("[^a-zA-Z_]", "_");

        // after de-camelCasing: lowercase
        label = label.toLowerCase();

        // replace __ and ___ etc. with just one _
        label = label.replaceAll("(_)+", "_");

        // delete leading and trailing underscores
        if (label.startsWith("_")) {
            label = label.substring(1);
        }
        if (label.endsWith("_")) {
            label = label.substring(0, label.length() - 1);
        }

        // array conversion
        String[] tokens = label.split("_");

        // hashset conversion
        HashSet<String> result = new HashSet<>(Arrays.asList(tokens));

        // remove free floating genitive s
        result.remove("s");

        // stopword removal
        result = StringOperations.clearHashSetFromStopwords(result);
        return result;
    }

    /**
     * Checks whether there exists a alignment cell where the URI is used as source.
     *
     * @param uri URI for which the check shall be performed.
     * @return True if at least one alignment cell exists, else false.
     */
    private boolean mappingExistsForSourceURI(String uri) {
        return this.alignment.getCorrespondencesSource(uri).iterator().hasNext();
    }

    /**
     * Checks whether there exists a alignment cell where the URI is used as target.
     *
     * @param uri URI for which the check shall be performed.
     * @return True if at least one alignment cell exists, else false.
     */
    private boolean mappingExistsForTargetURI(String uri) {
        return this.alignment.getCorrespondencesTarget(uri).iterator().hasNext();
    }


    /**
     * The compare method compares two concepts that are available in a background knowledge source.
     *
     * @return True if similarity larger than minimal threshold, else false.
     */
    private boolean compare(String lookupTerm1, String lookupTerm2) {
        return wiktionary.isStrongFormSynonymous(lookupTerm1, lookupTerm2);
    }


    /**
     * Returns the label. If it does not exist: local name.
     *
     * @param resource The resource for which a string shall be retrieved.
     * @return Label or local name. Null if resource is anonymous.
     */
    public static String getLabelOrFragment(OntResource resource) {
        if (resource.isAnon()) {
            return null;
        }
        ExtendedIterator<RDFNode> iterator = resource.listLabels(null);
        while (iterator.hasNext()) {
            RDFNode node = iterator.next();
            return node.asLiteral().toString();
        }
        // no label found: return local name
        return resource.getLocalName();
    }


    /**
     * Get the name of the matcher.
     *
     * @return A textual representation of the matcher.
     */
    public String getMatcherName() {
        return "WiktionaryMatcher";
    }


    /**
     * Close all open resources.
     * The matcher cannot be run after being closed.
     */
    public void close() {
        wiktionary.close();
    }

}
