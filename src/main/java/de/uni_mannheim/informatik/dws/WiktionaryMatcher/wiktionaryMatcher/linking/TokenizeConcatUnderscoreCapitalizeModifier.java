package de.uni_mannheim.informatik.dws.WiktionaryMatcher.wiktionaryMatcher.linking;

public class TokenizeConcatUnderscoreCapitalizeModifier implements StringModifier {

    /**
     * Constructor
     */
    public TokenizeConcatUnderscoreCapitalizeModifier(){
        tokenizeModifier = new TokenizeConcatUnderscoreModifier();
        capitalizeModifier = new CapitalizeFirstLettersModifier("_");
    }

    private TokenizeConcatUnderscoreModifier tokenizeModifier;
    private CapitalizeFirstLettersModifier capitalizeModifier;


    @Override
    public String modifyString(String stringToBeModified) {
        return capitalizeModifier.modifyString(tokenizeModifier.modifyString(stringToBeModified));
    }

    @Override
    public String getName() {
        return "TokenizeConcatUnderscoreCapitalizeModifier";
    }
}
