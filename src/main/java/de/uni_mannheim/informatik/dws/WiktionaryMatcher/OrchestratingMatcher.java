package de.uni_mannheim.informatik.dws.WiktionaryMatcher;

import de.uni_mannheim.informatik.dws.WiktionaryMatcher.complexStringMatcher.ComplexStringMatcher;
import de.uni_mannheim.informatik.dws.WiktionaryMatcher.notYetUsed.Arity;
import de.uni_mannheim.informatik.dws.WiktionaryMatcher.simpleStringMatcher.SimpleStringMatcher;
import de.uni_mannheim.informatik.dws.WiktionaryMatcher.wiktionaryMatcher.WiktionaryMatcher;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Properties;

/**
 * Main Matcher.
 */
public class OrchestratingMatcher extends LabelBasedMatcher {

    /**
     * Indicator whether the alignment shall be homogenous (i.e., only classes are mapped to classes, object properties
     * are mapped to object properties, and datatype properties are mapped to datatype properties).
     * If false, the alignment might be heterogenous (e.g., classes might be mapped to datatype properties).
     */
    private boolean homogenousAlignment = true;

    private Arity arity = Arity.MANY_TO_MANY;

    private static Logger LOGGER = LoggerFactory.getLogger(LabelBasedMatcher.class);


    public OrchestratingMatcher(){
        store.clear();
    }

    @Override
    public Alignment match(OntModel ontology1, OntModel ontology2, Alignment alignment, Properties properties) throws Exception {
        alignment = new Alignment();
        //System.out.println(GlobalDataStore.getOntId(ontology1));
        //System.out.println(GlobalDataStore.getOntId(ontology2));
        loadLabels(ontology1, ontology2);
        LOGGER.info("Detected Language for Source Ontology: " + mostFrequentLanguage_1);
        LOGGER.info("Detected Language for Target Ontology: " + mostFrequentLanguage_2);
        if(mostFrequentLanguage_1 == mostFrequentLanguage_2) {
            LOGGER.info("Both ontologies are in the same language - run matcher in monolingual mode.");

            SimpleStringMatcher smatcher = new SimpleStringMatcher();
            Alignment sAlignment = smatcher.match(ontology1, ontology2, alignment, properties);

            ComplexStringMatcher cmatcher = new ComplexStringMatcher();
            Alignment cAlignment = cmatcher.match(ontology1, ontology2, alignment, properties);

            Alignment merged = mergeAlignments(sAlignment, cAlignment);

            LevenshteinMatcher lmatcher = new LevenshteinMatcher();
            Alignment lAlignment = lmatcher.match(ontology1, ontology2, alignment, properties);

            merged = mergeAlignments(merged, lAlignment);

            WiktionaryMatcher wikiMatcher = new WiktionaryMatcher();
            merged = wikiMatcher.match(ontology1, ontology2, merged, properties);

            // scale confidence values
            scaleConfidenceValues(merged);

            LOGGER.info("Ontologies aligned.");

            if(this.ontology1.listIndividuals().toSet().size() > 10 && this.ontology2.listIndividuals().toSet().size() > 10){
                LOGGER.info("Instances detected. Running Instance Matcher.");
                SimpleInstanceMatcher instanceMatcher = new SimpleInstanceMatcher();
                Alignment finalAlignment = instanceMatcher.match(ontology1, ontology2, merged, properties);
                return finalAlignment;
            }

            return merged;
        } else {
            LOGGER.info("Different Languages detected - run matcher in multilingual mode.");
            WiktionaryMatcher wikiMatcher = new WiktionaryMatcher(true);
            Alignment result = wikiMatcher.match(ontology1, ontology2, new Alignment(), properties);
            return scaleConfidenceValues(result);
        }
    }


    /**
     * Scales the alignment confidence values using a min-max approach.
     * @param alignmentToScale The alignments that shall be scaled.
     * @return Returns the object on which the action is performed for convenience.
     */
    private Alignment scaleConfidenceValues(Alignment alignmentToScale){
        double maxConfidence = 0.0;
        for(Correspondence correspondence : alignmentToScale){
            if(correspondence.getConfidence() > maxConfidence) maxConfidence = correspondence.getConfidence();
        }
        if(maxConfidence > 0) {
            for (Correspondence correspondence : alignmentToScale) {
                correspondence.setConfidence(correspondence.getConfidence() / maxConfidence);
            }
        }
        return alignmentToScale;
    }



    /**
     * Merges the subdominant Alignment in the dominant alignment.
     * (Only the explanations from the dominant alignment will be kept.)
     * @param dominantAlignment Dominant (more important) alignment.
     * @param subdominantAlignment Subdomainant (less important) alignment.
     * @return Merged alignment.
     */
    private Alignment mergeAlignments(Alignment dominantAlignment, Alignment subdominantAlignment){
        Alignment result = new Alignment();
        result.addAll(dominantAlignment);

        for(Correspondence c : subdominantAlignment){
            if(result.contains(c)){
                continue;
            } else {
                result.add(c);
            }
        }
        return result;
    }


    /**
     * Get the size of an iterator.
     * @param iterator The iterator whose size shall be obtained.
     * @return The size of the iterator.
     */
    private static int getIteratorSize(Iterator iterator){
        int size = 0;
        while(iterator.hasNext()){
            iterator.next();
            size++;
        }
        return size;
    }


}
