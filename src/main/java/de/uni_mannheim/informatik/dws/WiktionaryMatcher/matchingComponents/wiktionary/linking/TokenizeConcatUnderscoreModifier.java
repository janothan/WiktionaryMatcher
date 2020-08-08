package de.uni_mannheim.informatik.dws.WiktionaryMatcher.matchingComponents.wiktionary.linking;

public class TokenizeConcatUnderscoreModifier implements StringModifier {

    /**
     * Tokenizes using best guess and AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION.
     * Concatenate using lower scores ("_")
     * @param stringToBeModified
     * @return
     */
    @Override
    public String modifyString(String stringToBeModified) {
        stringToBeModified = stringToBeModified.replaceAll("(?<!^)(?<!\\s)(?=[A-Z][a-z])", "_"); // convert camelCase to under_score_case
        stringToBeModified = stringToBeModified.replace(" ", "_");
        stringToBeModified = stringToBeModified.replaceAll("(_){1,}", "_"); // make sure there are no double-spaces
        return stringToBeModified;
    }

    @Override
    public String getName() {
        return "TokenizeConcatUnderscore";
    }

}
