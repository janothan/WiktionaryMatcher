package de.uni_mannheim.informatik.dws.WiktionaryMatcher.wiktionaryMatcher;

import de.uni_mannheim.informatik.dws.WiktionaryMatcher.wiktionaryMatcher.linking.LabelToConceptLinker;

import java.util.HashSet;

/**
 * Interface for dictionary access.
 * @author D060249
 *
 */
public abstract class KnowledgeSource {

	/**
	 * Checks whether the given word is available in the dictionary.
	 * The assumed language is English.
	 * @param word The word to be looked for.
	 * @return Returns true if the word is in the dictionary, else false.
	 */
	public abstract boolean isInDictionary(String word);
	
	/**
	 * Retrieves a list of synonyms independently of the word sense.
	 * The assumed language is English.
	 * @param linkedConcept The linked concept for which synonyms shall be retrieved.
	 * @return A set of linked concepts.
	 */
	public abstract HashSet<String> getSynonyms(String linkedConcept);

	/**
	 * Retrieves a set of hypernyms independently of the word sense.
	 * The assumed language is English.
	 * @param linkedConcept The linked concept for which hypernyms shall be retrieved.
	 * @return A set of linked concepts.
	 */
	public abstract HashSet<String> getHypernyms(String linkedConcept);


	/**
	 * Closing open resources
	 */
	public abstract void close();
	
	/**
	 * Checks for synonymous words in a loose-form fashion: There has to be an overlap in the two sets of synonyms
	 * or word_1 and word_2.
	 * The assumed language is English.
	 * @param word1 linked word 1
	 * @param word2 linked word 2
	 * @return True if the given words are synonymous, else false.
	 */
	@Deprecated
	public boolean isSynonymous(String word1, String word2) {
		if(word1 == null || word2 == null) {
			return false;
		}
		
		HashSet<String> synonyms1 = getSynonyms(word1);
		HashSet<String> synonyms2 = getSynonyms(word2);

		if(synonyms1 == null && synonyms2 == null){
			// only if both are null b/c one concept might not have synonyms but still be a synonym of the other concept
			return false;
		}
		if(synonyms1 == null) {
			synonyms1 = new HashSet<>();
		}
		if(synonyms2 == null) {
			synonyms2 = new HashSet<>();
		}
		
		// add the words themselves
		synonyms1.add(word1);
		synonyms2.add(word2);

		// remove empty strings to avoid false positives
		synonyms1.remove("");
		synonyms2.remove("");

		for(String s : synonyms1) {
			if(synonyms2.contains(s)) {
				return true;
			}
		}
		return false;
	}


    /**
     * Checks for hypernymous words in a loose-form fashion: One concept needs to be a hypernym of the other concept
     * where the order of concepts is irrelevant, i.e., the method returns (hypernymous(w1, w2) || hypernymous(w2, w1).
     *
     * The assumed language is English.
     *
     * @param linkedConcept_1 linked word 1
     * @param linkedConcept_2 linked word 2
     * @return True if the given words are hypernymous, else false.
     */
	public boolean isHypernymous(String linkedConcept_1, String linkedConcept_2){
        if(linkedConcept_1 == null || linkedConcept_2 == null) {
            return false;
        }

        HashSet<String> hypernyms_1 = getHypernyms(linkedConcept_1);
        HashSet<String> hypernyms_2 = getHypernyms(linkedConcept_2);

        for(String hypernym : hypernyms_1){
            if(linkedConcept_2.equals(hypernym)) return true;
        }
        for(String hypernym : hypernyms_2){
            if(linkedConcept_1.equals(hypernym)) return true;
        }
        return false;
    }


    /**
     * Checks whether the two concepts are snonymous or whether there is a hypernymy/homonymy relation between them.
     * @param linkedConcept_1 linked concept 1.
     * @param linkedConcept_2 linked concept 2.
     * @return True or false.
     */
    public boolean isSynonymousOrHypernymous(String linkedConcept_1, String linkedConcept_2){
	    return isStrongFormSynonymous(linkedConcept_1, linkedConcept_2) || isHypernymous(linkedConcept_1, linkedConcept_2);
    }


	/**
	 * Checks for synonymy by determining whether word1 is contained in the set of synonymous words of word2 and
	 * vice versa.
	 * @param word1 Word 1
	 * @param word2 Word 2
	 * @return True if the given words are synonymous, else false.
	 */
	public boolean isStrongFormSynonymous(String word1, String word2){
		if(word1 == null || word2 == null) {
			return false;
		}

		HashSet<String> synonyms1 = getSynonyms(word1);
		HashSet<String> synonyms2 = getSynonyms(word2);

		if(synonyms1 == null && synonyms2 == null){
			// only if both are null b/c one concept might not have synonyms but still be a synonym of the other concept
			return false;
		}
		if(synonyms1 == null) {
			synonyms1 = new HashSet<>();
		}
		if(synonyms2 == null) {
			synonyms2 = new HashSet<>();
		}

		// add the words themselves
		synonyms1.add(word1);
		synonyms2.add(word2);

		// remove empty strings to avoid false positives
		synonyms1.remove("");
		synonyms2.remove("");

		if(synonyms1.contains(word2)) return true;
		if(synonyms2.contains(word1)) return true;

		return false;
	}


	public abstract LabelToConceptLinker getLinker();

	public abstract String getName();
}
