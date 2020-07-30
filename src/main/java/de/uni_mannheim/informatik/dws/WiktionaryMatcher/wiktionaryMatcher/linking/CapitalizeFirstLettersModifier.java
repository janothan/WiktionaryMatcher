package de.uni_mannheim.informatik.dws.WiktionaryMatcher.wiktionaryMatcher.linking;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CapitalizeFirstLettersModifier implements StringModifier {


    /**
     * Constructor
     * @param delimiter The delimiter that is to be used. Every character after the delimiter (+ the very first character)
     *                  will be upper-cased.
     */
    public CapitalizeFirstLettersModifier(String delimiter){
        this.delimiter = delimiter;
    }


    private String delimiter;

    @Override
    public String modifyString(String stringToBeModified) {

        Pattern pattern = Pattern.compile("(?<=" + delimiter + ")[a-z]");
        List<Integer> positions = new ArrayList<>();

        Matcher m = pattern.matcher(stringToBeModified);

        while(m.find()){
            positions.add(m.start());
        }


        char[] charArray = stringToBeModified.toCharArray();

        // upper-case position one
        charArray[0] = Character.toUpperCase(charArray[0]);

        for(int position : positions){
            charArray[position] = Character.toUpperCase(charArray[position]);
        }

        return new String(charArray);
    }

    @Override
    public String getName() {
        return "CapitalizeFirstLettersModifier";
    }
}
