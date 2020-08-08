package de.uni_mannheim.informatik.dws.WiktionaryMatcher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LevenshteinMatcherTest {
    @Test
    void levenshtein(){
        assertEquals(0, LevenshteinMatcher.levenshteinDistanceRecursive("hallo", "hallo"));
        assertEquals(1, LevenshteinMatcher.levenshteinDistanceRecursive("hallo", "hello"));
    }

    @Test
    void levenshteinDynamic(){
        assertEquals(0, LevenshteinMatcher.levenshteinDynamic("hallo", "hallo"));
        assertEquals(1, LevenshteinMatcher.levenshteinDynamic("hallo", "hello"));
    }

    @Test
    void damerauLevenshteinDistanceWithLimited(){
        assertTrue(LevenshteinMatcher.damerauLevenshteinDistanceWithLimit("hallo".toCharArray(), "hallo".toCharArray(), 1) == 0);
        assertTrue(LevenshteinMatcher.damerauLevenshteinDistanceWithLimit("hallo".toCharArray(), "hello".toCharArray(), 1) == 1);
        assertTrue(LevenshteinMatcher.damerauLevenshteinDistanceWithLimit("hallo".toCharArray(), "hello2".toCharArray(), 2) == 2);
        assertTrue(LevenshteinMatcher.damerauLevenshteinDistanceWithLimit("hallo".toCharArray(), "hello2".toCharArray(), 1) > 100);

    }

    @Test
    void isDamerauLevenshteinEqualOrLess(){
        assertTrue(LevenshteinMatcher.isDamerauLevenshteinEqualOrLess("hallo", "hallo", 1));
        assertTrue(LevenshteinMatcher.isDamerauLevenshteinEqualOrLess("hallo", "hello", 1));
        assertFalse(LevenshteinMatcher.isDamerauLevenshteinEqualOrLess("hallo", "hello2", 1));

        assertTrue(LevenshteinMatcher.isDamerauLevenshteinEqualOrLess("hallo", "hello2", 2));
        assertTrue(LevenshteinMatcher.isDamerauLevenshteinEqualOrLess("hallo3", "hello2", 2));
        assertTrue(LevenshteinMatcher.isDamerauLevenshteinEqualOrLess("h8llo3", "hello2", 2));
        assertFalse(LevenshteinMatcher.isDamerauLevenshteinEqualOrLess("hallo", "hello23", 2));

        assertTrue(LevenshteinMatcher.isDamerauLevenshteinEqualOrLess("hallo", "hello23", 3));
        assertTrue(LevenshteinMatcher.isDamerauLevenshteinEqualOrLess("3hallo", "hello2", 3));
        assertTrue(LevenshteinMatcher.isDamerauLevenshteinEqualOrLess("3hallo3", "hello2", 3));
        assertFalse(LevenshteinMatcher.isDamerauLevenshteinEqualOrLess("3hallo33", "hello2", 3));
    }

}