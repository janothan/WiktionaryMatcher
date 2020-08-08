package de.uni_mannheim.informatik.dws.WiktionaryMatcher.fileProcessing;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ShortenFilesTest {
    @Test
    void endList() {
        ArrayList<String> myStatement_1 = new ArrayList<>();
        myStatement_1.add("deu:__tr_slv_1_Taiwan__Substantiv__1");
        myStatement_1.add("        a                       dbnary:Translation ;");
        myStatement_1.add("        dbnary:gloss            deu:__de_gloss_BkTrig--_Taiwan__Substantiv__1 ;");
        ArrayList<String> list = ShortenFiles.endList(myStatement_1);
        assertTrue(list.get(0).equals("deu:__tr_slv_1_Taiwan__Substantiv__1"));
        assertTrue(list.get(1).equals("        a                       dbnary:Translation ;"));
        assertTrue(list.get(2).equals("        dbnary:gloss            deu:__de_gloss_BkTrig--_Taiwan__Substantiv__1 ."));

        ArrayList<String> myStatement_2 = new ArrayList<>();
        myStatement_2.add("deu:__tr_slv_1_Taiwan__Substantiv__1");
        myStatement_2.add("        a                       dbnary:Translation ;");
        myStatement_2.add("        dbnary:gloss            deu:__de_gloss_BkTrig--_Taiwan__Substantiv__1 .");
        ArrayList<String> list2 = ShortenFiles.endList(myStatement_2);
        assertTrue(list2.get(0).equals("deu:__tr_slv_1_Taiwan__Substantiv__1"));
        assertTrue(list2.get(1).equals("        a                       dbnary:Translation ;"));
        assertTrue(list2.get(2).equals("        dbnary:gloss            deu:__de_gloss_BkTrig--_Taiwan__Substantiv__1 ."));
    }
}