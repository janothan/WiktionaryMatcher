package de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.wiktionary;


import de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.util.UriLabelInfo;
import de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.util.LabelLanguageTuple;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wiktionary.WiktionaryKnowledgeSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;



/**
 * Creates a simple index of form String -> URI.
 */
public class SimpleIndexerMultilangual {

    /**
     * Data structure for which the index shall be built.
     */
    public UriLabelInfo uriLabelMap;

    /**
     * Constructor.
     * @param uriLabelMap Data structure for which the index shall be built.
     */
    public SimpleIndexerMultilangual(UriLabelInfo uriLabelMap){
        this.uriLabelMap = uriLabelMap;
        if(uriLabelMap != null) {
            runIndexing();
        }
    }

    /**
     * The index.
     * label -> URIs
     * It is intended that the value is a list rather than a set so that one URI might appear in the set multiple times.
     */
    public HashMap<String, ArrayList<String>> index = new HashMap<>();



    /**
     * Perform the indexing function.
     * Note that the index is in the form label -> uris so that no information is lost in the case of a label collision.
     */
    private void runIndexing() {
        for (HashMap.Entry<String, HashSet<LabelLanguageTuple>> entry1 : uriLabelMap.getUriLabelMap().entrySet()) {
            for (LabelLanguageTuple tuple : entry1.getValue()) {
                String normalizedLabel = simplyNormalize(tuple.label);

                // check for label clashes
                ArrayList<String> indexEntry = index.get(normalizedLabel);
                if(indexEntry != null){
                  indexEntry.add(entry1.getKey());
                } else {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(entry1.getKey());
                    index.put(normalizedLabel, list);
                }
            }
        }
    }


    /**
     * A simple label normalization function.
     *
     * @param stringToBeNormalized The label to be normalized.
     * @return Normalized label.
     */
    public static String simplyNormalize(String stringToBeNormalized) {
        if (stringToBeNormalized == null) return null;
        //if(stringToBeNormalized.length() > 3 && stringToBeNormalized.charAt(stringToBeNormalized.length()-3) == '@'){
        //    stringToBeNormalized = stringToBeNormalized.substring(0, stringToBeNormalized.length()-3);
        //}
        stringToBeNormalized = stringToBeNormalized.replaceAll("(?<!^)(?<!\\s)(?=[A-Z][a-z])", "_"); // convert camelCase to under_score_case
        return WiktionaryKnowledgeSource.normalizeForTranslations(stringToBeNormalized);
    }

}
