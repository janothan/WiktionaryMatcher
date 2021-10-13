package de.uni_mannheim.informatik.dws.WiktionaryMatcher;

import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import org.junit.jupiter.api.Test;


class OrchestratingMatcherTest {


    @Test
    void runMatcher() {
        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(
                Executor.run(
                        TrackRepository.Multifarm.getSpecificMultifarmTrack("de-en"),
                        new OrchestratingMatcher()));
        evaluatorCSV.writeToDirectory();
    }



}