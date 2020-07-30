package de.uni_mannheim.informatik.dws.WiktionaryMatcher.simpleStringMatcher;

import de.uni_mannheim.informatik.dws.WiktionaryMatcher.LabelBasedMatcher;
import de.uni_mannheim.informatik.dws.WiktionaryMatcher.wiktionaryMatcher.UriLabelInfo;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.DefaultExtensions;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Creates a homogeneous n-n alignment.
 */
public class SimpleStringMatcher extends LabelBasedMatcher {

    public static void main(String[] args) {
        SimpleStringMatcher simpleStringMatcher = new SimpleStringMatcher();
        TestCase tc = TrackRepository.Anatomy.Default.getTestCases().get(0);
        //TestCase tc = TrackRepository.Multifarm.getSpecificMultifarmTrack("ar-fr").getTestCases().get(0);
        simpleStringMatcher.match(tc.getSourceOntology(OntModel.class), tc.getTargetOntology(OntModel.class), null, null);
    }

    /**
     * Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(SimpleStringMatcher.class);



    @Override
    public Alignment match(OntModel sourceOntology, OntModel targetOntology, Alignment m, Properties p) {
        ontology1 = sourceOntology;
        ontology2 = targetOntology;
        alignment = m;
        loadLabels(sourceOntology, targetOntology);

        // run mapping strict string matching
        directMatch(uri2labelMapClasses_1, uri2labelMapClasses_2, "classes");
        directMatch(uri2labelMapDatatypeProperties_1, uri2labelMapDatatypeProperties_2, "datatypeProperties");
        directMatch(uri2labelMapObjectProperties_1, uri2labelMapObjectProperties_2, "objectProperties");
        directMatch(uri2labelMapRemainingProperties_1, uri2labelMapRemainingProperties_2, "remainingProperties");

        LOGGER.info("SimpleStringMatcher Completed");
        return alignment;
    }


    /**
     * A direct matching approach without stopword removal or other advanced approaches.
     *
     * @param uriLabelMap_1 Map 1 (source).
     * @param uriLabelMap_2 Map 2 (target).
     * @param what What is matched.
     */
    private void directMatch(UriLabelInfo uriLabelMap_1, UriLabelInfo uriLabelMap_2, String what) {
        SimpleIndexer indexer_1 = new SimpleIndexer(uriLabelMap_1);
        HashMap<String, ArrayList<String>> simpleIndex_1 = indexer_1.index;
        SimpleIndexer indexer_2 = new SimpleIndexer(uriLabelMap_2);
        HashMap<String, ArrayList<String>> simpleIndex_2 = indexer_2.index;

        // adding to the store
        store.put("simpleIndex_" + what + "_1", indexer_1);
        store.put("simpleIndex_" + what + "_2", indexer_2);

        for (String s1 : simpleIndex_1.keySet()) {
            if (simpleIndex_2.containsKey(s1)) {
                for(String sourceUri : simpleIndex_1.get(s1)){
                    for(String targetUri : simpleIndex_2.get(s1)){
                        addMapping(sourceUri, targetUri, s1);
                    }
                }
            }
        }
    }


    /**
     * Adds a mapping to the alignment.
     * If the mapping is already there, the confidence is increased.
     *
     * @param uri_1 URI 1.
     * @param uri_2 URI 2.
     * @param labelThatLeadsToMatch Label that leads to a match
     */
    private void addMapping(String uri_1, String uri_2, String labelThatLeadsToMatch) {

        Correspondence existingCorrespondence = alignment.getCorrespondence(uri_1, uri_2, CorrespondenceRelation.EQUIVALENCE);
        if (existingCorrespondence != null) {
            existingCorrespondence.setConfidence(existingCorrespondence.getConfidence() + 1.0);
            String extendedExplanation = existingCorrespondence.getExtensions().get(DefaultExtensions.DublinCore.DESCRIPTION.toString()) + ", " + labelThatLeadsToMatch;
            existingCorrespondence.getExtensions().put(DefaultExtensions.DublinCore.DESCRIPTION.toString(), extendedExplanation);
            LOGGER.info("Updated correspondence: " + existingCorrespondence);
        } else {
            Map<String, Object> extensions = new HashMap<>();
            extensions.put(DefaultExtensions.DublinCore.DESCRIPTION.toString(), EXPLANATION_TEXT_DIRECT_MATCH + labelThatLeadsToMatch);
            Correspondence newCorrespondence = new Correspondence(uri_1, uri_2, 1.0, CorrespondenceRelation.EQUIVALENCE, extensions);
            alignment.add(newCorrespondence);
            LOGGER.info("New correspondence: " + newCorrespondence);
        }
    }


    /**
     * Explanation text.
     */
    private final static String EXPLANATION_TEXT_DIRECT_MATCH = "The two resources have the same label when normalized. This is the strongest indication of a match for this matcher. " +
            "A match was found on the following (normalized) label(s): ";

}
