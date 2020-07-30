package de.uni_mannheim.informatik.dws.WiktionaryMatcher;


import de.uni_mannheim.informatik.dws.WiktionaryMatcher.simpleStringMatcher.SimpleIndexer;
import de.uni_mannheim.informatik.dws.WiktionaryMatcher.wiktionaryMatcher.UriLabelInfo;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ResourceType;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.DefaultExtensions;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Instance Matcher for the knowledge graph track.
 */
public class SimpleInstanceMatcher extends LabelBasedMatcher {


    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleInstanceMatcher.class);

    @Override
    public Alignment match(OntModel sourceOntology, OntModel targetOntology, Alignment alignment, Properties properties) throws Exception {
        ontology1 = sourceOntology;
        ontology2 = targetOntology;
        loadLabels(sourceOntology, targetOntology);

        nextCorrespondence:
        for(Correspondence correspondence : alignment) {
            ResIterator iterator1 = null;
            if(ResourceType.analyze(ontology1, correspondence.getEntityOne()) == ResourceType.CLASS ) {
                iterator1 = ontology1.listSubjectsWithProperty(RDF.type, ontology1.getRDFNode(NodeFactory.createURI(correspondence.getEntityOne())));
            }
            ResIterator iterator2 = null;
            if(ResourceType.analyze(ontology2, correspondence.getEntityTwo()) == ResourceType.CLASS ) {
                iterator2 = ontology2.listSubjectsWithProperty(RDF.type, ontology2.getRDFNode(NodeFactory.createURI(correspondence.getEntityTwo())));
            }

            if(iterator1 == null || iterator2 == null) continue nextCorrespondence;
            UriLabelInfo instanceLabelsHighConfidence_1 = super.getUriLabelMapAndCalculateLanguageDistributionForInstances(iterator1, ontology1, true);
            UriLabelInfo instanceLabelsHighConfidence_2 = super.getUriLabelMapAndCalculateLanguageDistributionForInstances(iterator2, ontology2, true);
            directMatch(instanceLabelsHighConfidence_1, instanceLabelsHighConfidence_2, true);
        }

        ResIterator iterator1 = ontology1.listSubjectsWithProperty(RDF.type);
        ResIterator iterator2 = ontology2.listSubjectsWithProperty(RDF.type);
        UriLabelInfo instanceLabelsLowConfidence_1 = super.getUriLabelMapAndCalculateLanguageDistributionForInstances(iterator1, ontology1, true);
        UriLabelInfo instanceLabelsLowConfidence_2 = super.getUriLabelMapAndCalculateLanguageDistributionForInstances(iterator2, ontology2, true);
        directMatch(instanceLabelsLowConfidence_1, instanceLabelsLowConfidence_2, false);

        cleanAlignment(this.alignment);
        this.alignment.addAll(alignment); // do not add before
        return this.alignment;
    }


    /**
     * A direct matching approach without stop word removal or other advanced approaches.
     *
     * @param uriLabelMap_1 Map 1 (source).
     * @param uriLabelMap_2 Map 2 (target).
     */
    private void directMatch(UriLabelInfo uriLabelMap_1, UriLabelInfo uriLabelMap_2, boolean isHighQuality) {
        SimpleIndexer indexer_1 = new SimpleIndexer(uriLabelMap_1);
        HashMap<String, ArrayList<String>> simpleIndex_1 = indexer_1.index;
        SimpleIndexer indexer_2 = new SimpleIndexer(uriLabelMap_2);
        HashMap<String, ArrayList<String>> simpleIndex_2 = indexer_2.index;

        // adding to the store
        store.put("simpleIndex_instances" + "_1", indexer_1);
        store.put("simpleIndex_instances" + "_2", indexer_2);

        for (String s1 : simpleIndex_1.keySet()) {
            if (simpleIndex_2.containsKey(s1)) {
                for(String sourceUri : simpleIndex_1.get(s1)){
                    for(String targetUri : simpleIndex_2.get(s1)){
                        if(isHighQuality) addMapping(sourceUri, targetUri, s1, 1.0, EXPLANATION_TEXT_HIGH_QUALITY_DIRECT_MATCH);
                        else addMapping(sourceUri, targetUri, s1, 1.0, EXPLANATION_TEXT_LOW_QUALITY_DIRECT_MATCH);
                    }
                }
            }
        }
    }


    private void cleanAlignment(Alignment alignmentToClean){
        LOGGER.info("Beginning to prune instance correspondences.");
        HashSet<Correspondence> correspondencesToRemove = new HashSet<>();
        for(Correspondence c : alignmentToClean){
            correspondencesToRemove.addAll(determineCandidatesForDeletion(alignmentToClean.getCorrespondencesSource(c.getEntityOne()).iterator()));
            correspondencesToRemove.addAll(determineCandidatesForDeletion(alignmentToClean.getCorrespondencesTarget(c.getEntityTwo()).iterator()));
        }
        for(Correspondence c : correspondencesToRemove){
            LOGGER.info("Prune Correspondence: " + c);
            alignmentToClean.remove(c);
        }
        LOGGER.info("Ended pruning instance correspondences.");
        return;
    }


    /**
     * Given a set of correspondences, determine which correspondences to delete in order to improve the F1 score.
     * @param iterator Iterator over correspondences.
     * @return Correspondences that are to be deleted.
     */
    private HashSet<Correspondence> determineCandidatesForDeletion(Iterator<Correspondence> iterator){
        HashSet<Correspondence> result = new HashSet<>();
        if(iterator == null) return result;
        boolean containsLowConfidence = false;
        HashSet<Correspondence> highConfidenceCorrespondences = new HashSet<>();
        HashSet<Correspondence> correspondencesToCheck = new HashSet<>();

        while(iterator.hasNext()){
            Correspondence c = iterator.next();
            if(c.getConfidence() > 1.0){
                highConfidenceCorrespondences.add(c);
            } else containsLowConfidence = true;
            correspondencesToCheck.add(c);
        }

        if(correspondencesToCheck.size() > 1 && highConfidenceCorrespondences.size() == 1 && containsLowConfidence){
            for(Correspondence cCheck : correspondencesToCheck){
                if(cCheck.getConfidence() <= 1.0) result.add(cCheck);
            }
        } else if (correspondencesToCheck.size() > 2){
            // assumption: we are optimizing F1
            // if more than two things are matched to the same entity in a 1-1 arity setting, more than 2 matches
            // worsen the f1 score. Hence, it is better to sacrifice recall for a better F1.
            result.addAll(correspondencesToCheck);
        }

        return result;
    }


    /**
     * Adds a mapping to the alignment.
     * If the mapping is already there, the confidence is increased.
     *
     * @param uri_1 URI 1.
     * @param uri_2 URI 2.
     * @param labelThatLeadsToMatch Label that leads to a match
     */
    private void addMapping(String uri_1, String uri_2, String labelThatLeadsToMatch, double confidence, String explanation) {
        Correspondence existingCorrespondence = alignment.getCorrespondence(uri_1, uri_2, CorrespondenceRelation.EQUIVALENCE);
        if (existingCorrespondence != null) {
            existingCorrespondence.setConfidence(existingCorrespondence.getConfidence() + confidence);
            String extendedExplanation = existingCorrespondence.getExtensions().get(DefaultExtensions.DublinCore.DESCRIPTION.toString()) + ", '" + labelThatLeadsToMatch + "'";
            existingCorrespondence.getExtensions().put(DefaultExtensions.DublinCore.DESCRIPTION.toString(), extendedExplanation);
            LOGGER.info("Updated correspondence: " + existingCorrespondence);
        } else {
            Map<String, Object> extensions = new HashMap<>();
            extensions.put(DefaultExtensions.DublinCore.DESCRIPTION.toString(), explanation + labelThatLeadsToMatch + "'");
            Correspondence newCorrespondence = new Correspondence(uri_1, uri_2, confidence, CorrespondenceRelation.EQUIVALENCE, extensions);
            alignment.add(newCorrespondence);
            LOGGER.info("New correspondence: " + newCorrespondence);
        }
    }


    /**
     * Explanation text.
     */
    private final static String EXPLANATION_TEXT_HIGH_QUALITY_DIRECT_MATCH = "The two resources are instances of classes that were matched before. " +
            "A match was found on the following (normalized) label(s): '";

    private final static String EXPLANATION_TEXT_LOW_QUALITY_DIRECT_MATCH = "A match was found on the following (normalized) label(s): '";

}
