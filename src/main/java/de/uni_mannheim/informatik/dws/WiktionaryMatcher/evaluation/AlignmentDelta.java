package de.uni_mannheim.informatik.dws.WiktionaryMatcher.evaluation;

import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentParser;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

public class AlignmentDelta {

    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AlignmentDelta.class);

    public static void main(String[] args) throws Exception {

        String pathToBetterAlignment = "/Users/janportisch/IdeaProjects/oaei_2019/WiktionaryMatcher/results/results_2020-08-08_13-57-17/conference_conference-v1/ekaw-iasted/OrchestratingMatcher/systemAlignment.rdf";
        String pathToWorseAlignment = "/Users/janportisch/IdeaProjects/WiktionaryMatcher/results/results_2020-08-08_14-23-33/conference_conference-v1/ekaw-iasted/WiktionaryMatcher/systemAlignment.rdf";

        compareAlignements(pathToBetterAlignment, pathToWorseAlignment, TrackRepository.Conference.V1.getTestCase("ekaw-iasted").getParsedReferenceAlignment());


    }

    public static void compareAlignements(String pathToBetterAlignment, String pathToWorseAlignment, Alignment reference){
        Alignment betterAll = null, worseAll = null;
        try {
            betterAll = AlignmentParser.parse(new File(pathToBetterAlignment));
        } catch (SAXException | IOException se){
            se.printStackTrace();
            LOGGER.error("Better alignment cannot be parsed.");
        }
        try {
        worseAll = AlignmentParser.parse(new File(pathToWorseAlignment));
        } catch (SAXException | IOException se){
            se.printStackTrace();
            LOGGER.error("Worse alignment cannot be parsed.");
        }
        System.out.println("Size better alignment: " + betterAll.size());
        System.out.println("Size worse alignment: " + worseAll.size());

        Alignment betterCorrect = Alignment.intersection(betterAll, reference);
        Alignment worseCorrect = Alignment.intersection(worseAll, reference);

        System.out.println("Correct size better alignment: " + betterCorrect.size());
        System.out.println("Correct size worse alignment: " + worseCorrect.size());

        betterCorrect.removeAll(worseCorrect);
        System.out.println("Exclusive correspondences in better alignment: " + betterCorrect.size());
        for(Correspondence c : betterCorrect){
            System.out.println(c);
        }

        System.out.println("\n\n");
        System.out.println("Detailed Delta Analysis");
        for(Correspondence c : betterCorrect){
            System.out.println("\n");
            System.out.println("Original Correspondence in better alignment: " + c);
            System.out.println("\tSource mappings in worse alignment:");
            for(Correspondence cwm : worseAll.getCorrespondencesSource(c.getEntityOne())){
                System.out.println("\t\t" + cwm);
            }
            System.out.println("\tTarget mappings in worse alignment:");
            for(Correspondence cwm : worseAll.getCorrespondencesTarget(c.getEntityTwo())){
                System.out.println("\t\t" + cwm);
            }
        }
    }

}
