package de.uni_mannheim.informatik.dws.WiktionaryMatcher.wiktionaryMatcher.linking;

import de.uni_mannheim.informatik.dws.WiktionaryMatcher.services.StringOperations;
import de.uni_mannheim.informatik.dws.WiktionaryMatcher.wiktionaryMatcher.Language;
import de.uni_mannheim.informatik.dws.WiktionaryMatcher.wiktionaryMatcher.WiktionaryKnowledgeSource;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * This linker can link strings to dictionary entries. 
 * @author D060249
 *
 */
public class WiktionaryLinker implements LabelToConceptLinker {

	/**
	 * Default name of the linker.
	 */
	private String nameOfLinker = "DbnaryLinker";


	/**
	 * The dictionary that is used to perform lookups.
	 */
	private WiktionaryKnowledgeSource dictionary;

	/**
	 * The list of operations that is performed to find a concept in the dictionary.
	 */
	LinkedList<StringModifier> stringModificationSequence;

	/**
	 * Constructor
	 * @param dictionary The dictionary that shall be used.
	 */
	public WiktionaryLinker(WiktionaryKnowledgeSource dictionary) {
		this.dictionary = dictionary;
		stringModificationSequence = new LinkedList<>();
		stringModificationSequence.add(new TokenizeConcatUnderscoreModifier());
		stringModificationSequence.add(new TokenizeConcatUnderscoreCapitalizeModifier());
		stringModificationSequence.add(new TokenizeConcatUnderscoreLowercaseModifier());
	}

	/**
	 * The assumed language is English.
	 * @param labelToBeLinked The label which shall be linked to a single concept.
	 * @return
	 */
	@Override
	public String linkToSingleConcept(String labelToBeLinked) {
		String modifiedConcept;
		for(StringModifier modifier : stringModificationSequence) {
			modifiedConcept = modifier.modifyString(labelToBeLinked);
			if(dictionary.isInDictionary(modifiedConcept)) {
				return modifiedConcept;
			}
		}
		return null;
	}

	/**
	 * The language can be specified.
	 * @param labelToBeLinked Label that shall be linked.
	 * @param language The language of the label.
	 * @return Linked concept or null.
	 */
	public String linkToSingleConcept(String labelToBeLinked, Language language){
		String modifiedConcept;
		for(StringModifier modifier : stringModificationSequence) {
			modifiedConcept = modifier.modifyString(labelToBeLinked);
			if(dictionary.isInDictionary(modifiedConcept, language)) {
				return modifiedConcept;
			}
		}
		return null;
	}


	@Override
	public HashSet<String> linkToPotentiallyMultipleConcepts(String labelToBeLinked) {
		HashSet<String> result = linkLabelToTokensLeftToRight(labelToBeLinked);
		int possibleConceptParts = StringOperations.clearArrayFromStopwords(StringOperations.tokenizeBestGuess(labelToBeLinked)).length;

		int actualConceptParts = 0;
		for(String s : result) {
			actualConceptParts = actualConceptParts + StringOperations.clearArrayFromStopwords(StringOperations.tokenizeBestGuess(s)).length;
		}
		
		// TODO: for now: only 100% results
		if(possibleConceptParts <= actualConceptParts) {
			return result;
		}
		return null;
	}


	/**
	 * Splits the labelToBeLinked in ngrams up to infinite size and tries to link components.
	 * This corresponds to a MAXGRAM_LEFT_TO_RIGHT_TOKENIZER or NGRAM_LEFT_TO_RIGHT_TOKENIZER OneToManyLinkingStrategy.
	 * @return A set of concept URIs that were found.
	 */
	private HashSet<String> linkLabelToTokensLeftToRight(String labelToBeLinked){
		StringOperations.removeNonAlphanumericCharacters(StringOperations.removeEnglishGenitiveS(labelToBeLinked));
		LeftToRightTokenizer tokenizer;
		String[] tokens = StringOperations.tokenizeBestGuess(labelToBeLinked);

		//tokenizer = new NgramLeftToRightTokenizer(tokens, "_", 10);
		tokenizer = new MaxGramLeftToRightTokenizer(tokens, "_");

		HashSet<String> result = new HashSet<>();
		String resultingConcept = "";
		String token = tokenizer.getInitialToken();
		while(token != null){
			resultingConcept = linkToSingleConcept(token);
			if(resultingConcept == null || resultingConcept.length() == 0){
				token = tokenizer.getNextTokenNotSuccessful();
			} else {
				result.add(resultingConcept);
                token = tokenizer.getNextTokenSuccessful();
			}
		}
		return result;
	}

	@Override
	public String getNameOfLinker() {
		return this.nameOfLinker;
	}

	@Override
	public void setNameOfLinker(String nameOfLinker) {
		this.nameOfLinker = nameOfLinker;
	}


	/**
	 * Normalize for WordNet lookup, i.e., lowercasing and camel-case resolution.
	 * Dev-Remark: The WordNet library utilizes space-separation.
	 * @param lookupString The string that shall be normalized for lookup.
	 * @return Space-separated lookup word.
	 */
	public static String normalizeForWiktionaryLookup(String lookupString){
		lookupString = lookupString.replaceAll("(?<!^)(?<!\\s)(?=[A-Z][a-z])", "_"); // convert camelCase to under_score_case
		lookupString = lookupString.replaceAll("(_){1,}", "_"); // make sure there are no double-spaces
		return lookupString;
	}
}
