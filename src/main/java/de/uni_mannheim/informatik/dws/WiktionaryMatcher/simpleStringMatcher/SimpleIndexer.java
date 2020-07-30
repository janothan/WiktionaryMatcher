package de.uni_mannheim.informatik.dws.WiktionaryMatcher.simpleStringMatcher;



import de.uni_mannheim.informatik.dws.WiktionaryMatcher.wiktionaryMatcher.UriLabelInfo;
import de.uni_mannheim.informatik.dws.WiktionaryMatcher.wiktionaryMatcher.linking.LabelLanguageTuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Creates a simple index of form String -> URI.
 */
public class SimpleIndexer {

    /**
     * Data structure for which the index shall be built.
     */
    public UriLabelInfo uriLabelMap;

    /**
     * Very small strings such as 's', 'a', '10' can lead to many false positive matches.
     * Usually, it is not very meaningful to index those.
     * Therefore, this variable sets the minimal length for an index member.
     */
    public int minimalCharacterLengthForIndex = 3;

    /**
     * Constructor.
     * @param uriLabelMap Data structure for which the index shall be built.
     */
    public SimpleIndexer(UriLabelInfo uriLabelMap){
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
     */
    private void runIndexing() {
        for (HashMap.Entry<String, HashSet<LabelLanguageTuple>> entry1 : uriLabelMap.getUriLabelMap().entrySet()) {
            nextTuple:
            for (LabelLanguageTuple tuple : entry1.getValue()) {
                if(tuple.label.length() < minimalCharacterLengthForIndex) continue nextTuple;
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
        String original = stringToBeNormalized;
        if(stringToBeNormalized.length() > 3 && stringToBeNormalized.charAt(stringToBeNormalized.length()-3) == '@'){
            stringToBeNormalized = stringToBeNormalized.substring(0, stringToBeNormalized.length()-3);
        }

        stringToBeNormalized = stringToBeNormalized.replaceAll("\\([^\\)]*\\)", ""); // \([^\)]*\)   → removes everything in round brackets
        stringToBeNormalized = stringToBeNormalized.replaceAll("\\[[^\\]]*\\]", ""); // \[[^\]]*\]   → removes everything in squared brackets
        stringToBeNormalized = stringToBeNormalized.replaceAll("(?<!^)(?<!\\s)(?=[A-Z][a-z])", "_"); // convert camelCase to under_score_case
        stringToBeNormalized = stringToBeNormalized.replace("-", "_");
        stringToBeNormalized = stringToBeNormalized.replace(" ", "_");
        stringToBeNormalized = stringToBeNormalized.replaceAll("_{2,}", "_"); // replace multi-underscores with one underscore
        if(stringToBeNormalized.startsWith("_")){
            stringToBeNormalized = stringToBeNormalized.substring(1);
        }
        if(stringToBeNormalized.endsWith("_")){
            stringToBeNormalized = stringToBeNormalized.substring(0, stringToBeNormalized.length() - 1);
        }
        stringToBeNormalized = stringToBeNormalized.toLowerCase();

        if(stringToBeNormalized.length() < 3){
            // avoid matches to labels such as 'a' or '18' or 's'.
            return original;
        }

        return stringToBeNormalized;
    }

}
