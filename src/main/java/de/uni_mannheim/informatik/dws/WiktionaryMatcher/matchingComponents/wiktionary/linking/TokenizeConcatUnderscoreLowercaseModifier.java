package de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.wiktionary.linking;

public class TokenizeConcatUnderscoreLowercaseModifier implements StringModifier {

    /**
     * Constructor
     */
    public TokenizeConcatUnderscoreLowercaseModifier(){
        tokenizeModifier = new TokenizeConcatUnderscoreModifier();
    }

    private TokenizeConcatUnderscoreModifier tokenizeModifier;

    @Override
    public String modifyString(String stringToBeModified) {
        return tokenizeModifier.modifyString(tokenizeModifier.modifyString(stringToBeModified)).toLowerCase();
    }

    @Override
    public String getName() {
        return "TokenizeConcatUnderscoreCapitalizeModifier";
    }

}
