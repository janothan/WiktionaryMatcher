package de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.wiktionary;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WiktionaryMatcherTest {
    static WiktionaryMatcher matcher;

    @BeforeAll
    static void setup(){
        matcher = new WiktionaryMatcher();
    }

    @Test
    void isTokenSynonymous() {
        HashSet<String> set_1 = new HashSet<>();
        set_1.add("dog");
        set_1.add("flex");

        HashSet<String> set_2 = new HashSet<>();
        set_2.add("hound");
        set_2.add("flex");

        assertTrue(matcher.isTokenSynonymous(set_1, set_2));
        assertTrue(matcher.isTokenSynonymous(set_2, set_1));

        set_1.add("cat");
        assertFalse(matcher.isTokenSynonymous(set_1, set_2));
        assertFalse(matcher.isTokenSynonymous(set_2, set_1));


        HashSet<String> set_3 = new HashSet<>();
        set_3.add("abcd_not_linkable");
        set_3.add("dog");

        HashSet<String> set_4 = new HashSet<>();
        set_4.add("abcd_not_linkable");
        set_4.add("hound");

        assertTrue(matcher.isTokenSynonymous(set_3, set_4));
        assertTrue(matcher.isTokenSynonymous(set_4, set_3));

        set_3.add("a_not_linkable");
        assertFalse(matcher.isTokenSynonymous(set_3, set_4));
        assertFalse(matcher.isTokenSynonymous(set_4, set_3));
    }

    @Test
    void isTokenSetSynonymous(){

        // list 1
        List<HashSet<String>> list1 = new LinkedList<>();
        HashSet<String> set1 = new HashSet<>();
        set1.add("humankind");
        set1.add("peace");
        list1.add(set1);

        // list 2
        List<HashSet<String>> list2 = new LinkedList<>();
        HashSet<String> set2 = new HashSet<>();
        set2.add("mankind");
        set2.add("peace");
        HashSet<String> set2a = new HashSet<>();
        set2a.add("random");
        set2a.add("blubb");
        list2.add(set2);
        list2.add(set2a);

        // case: synonymous sets, testing both ways
        assertTrue(matcher.isTokenSetSynonymous(list1, list2));
        assertTrue(matcher.isTokenSetSynonymous(list2, list1));

        // list 3
        List<HashSet<String>> list3 = new LinkedList<>();
        HashSet<String> set3 = new HashSet<>();
        set3.add("random");
        set3.add("blubbb");
        list3.add(set3);

        // case: non-synonymous sets, testing both ways
        assertFalse(matcher.isTokenSetSynonymous(list3, list2));
        assertFalse(matcher.isTokenSetSynonymous(list2, list3));

        // list 4
        List<HashSet<String>> list4 = new LinkedList<>();
        HashSet<String> set4 = new HashSet<>();
        set4.add("random");
        set4.add("blubbb");
        set4.add("addition");
        list4.add(set4);

        // case: non-synonymous set but one contains the other
        assertFalse(matcher.isTokenSetSynonymous(list3, list4));
        assertFalse(matcher.isTokenSetSynonymous(list4, list3));


        // list 5
        List<HashSet<String>> list5 = new LinkedList<>();
        HashSet<String> set5 = new HashSet<>();
        set5.add("xckfg");
        list5.add(set5);

        // list 6
        List<HashSet<String>> list6 = new LinkedList<>();
        HashSet<String> set6 = new HashSet<>();
        set6.add("xckfgabc");
        list6.add(set6);

        // case: non-synonymous, non-linkable
        assertFalse(matcher.isTokenSetSynonymous(list5, list6));
        assertFalse(matcher.isTokenSetSynonymous(list6, list5));


        // test for long lists
        List<HashSet<String>> list7 = new LinkedList<>();
        HashSet<String> set7 = new HashSet<>();
        set7.add("12345");
        set7.add("dog");
        set7.add("warlock");
        set7.add("car");
        set7.add("outsending");
        set7.add("glasses");
        list7.add(set7);

        List<HashSet<String>> list8 = new LinkedList<>();
        HashSet<String> set8 = new HashSet<>();
        set8.add("automobile");
        set8.add("transmission");
        set8.add("hound");
        set8.add("12345");
        set8.add("specs");
        set8.add("warlock");
        list8.add(set8);

        assertTrue(matcher.isTokenSetSynonymous(list7, list8));
        assertTrue(matcher.isTokenSetSynonymous(list8, list7));
    }

    @AfterAll
    static void close(){
        matcher.close();
    }
}