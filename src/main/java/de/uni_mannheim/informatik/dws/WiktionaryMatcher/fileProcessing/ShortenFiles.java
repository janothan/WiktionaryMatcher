package de.uni_mannheim.informatik.dws.WiktionaryMatcher.fileProcessing;

import java.io.*;
import java.util.ArrayList;


/**
 * This program reduces the DBnary files to the minimum information required to run the matcher in order to
 * increase performance and to reduce disk requirements.
 */
public class ShortenFiles {


    public static void main(String[] args) throws Exception{
        //writeNewFile("C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\dbnary\\2019-08-07\\en_dbnary_ontolex.ttl",
        //        "C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\dbnary\\2019-08-07\\compressed\\en_dbnary_ontolex.ttl",
        //        LanguageCode.ENGLISH);

        writeNewFile("C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\dbnary\\2019-08-07\\nl_dbnary_ontolex.ttl",
                "C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\dbnary\\2019-08-07\\compressed\\nl_dbnary_ontolex.ttl",
                LanguageCode.DUTCH);
    }


    public static void writeNewFile(String oldFilePath, String newFilePath, LanguageCode language) throws Exception{

        File oldFile = new File(oldFilePath);
        if(!oldFile.exists()){
            System.out.println("File does not exist;");
            return;
        }
        File newFile = new File(newFilePath);

        BufferedReader reader = new BufferedReader(new FileReader(oldFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));

        String languageCode = language.toString();

        ArrayList<String> listToWrite = new ArrayList();

        boolean writeList;

        String line;
        nextLine : while((line= reader.readLine()) != null){

            //if(line.startsWith("        a")){
            //    // check for the different desired types, else do not write the list!
            //    if(line.contains("dbnary:Page")
            //            || line.contains("ontolex:LexicalEntry") // describes type
            //            || line.contains("ontolex:LexicalSense"))
            //    {
            //        writeList = true;
            //    }
            //}

            if(line.startsWith(languageCode)
                    || line.startsWith("        a")
                    || line.startsWith("<")
                    || line.contains("@prefix")){
                listToWrite.add(line);
                //continue nextLine;
            }

            if(language == LanguageCode.ENGLISH) {
                // note: for oaei hypernyms are excluded
                if (line.contains("dbnary:describes")
                        || line.contains("dbnary:synonym")
                        || line.contains("ontolex:sense")
                        || line.contains("dbnary:getTranslationOf")
                        || line.contains("dbnary:writtenForm")
                        || line.contains("dbnary:targetLanguage")
                ) {
                    listToWrite.add(line);
                }
            } else {
                // ignoring synonymy elsewhere
                if (line.contains("dbnary:describes")
                        || line.contains("dbnary:getTranslationOf")
                        || line.contains("dbnary:writtenForm")
                        || line.contains("dbnary:targetLanguage")
                ) {
                    listToWrite.add(line);
                }
            }

            if(line.endsWith(".")){
                // the line closes the current statement
                listToWrite = endList(listToWrite);
                for(String s : listToWrite){
                    writer.write(s + "\n");
                }
                writer.write("\n");
                listToWrite = new ArrayList<>();
            }
        }

        // closing resources
        reader.close();
        writer.flush();
        writer.close();
    }


    /**
     * Replaces a ";" at the end of a statment with a "." because the statement ended now.
     * Have a look at the unit test to understand the inner workings of this method.
     * @param listToEdit The list where the edit shall be performed on.
     * @return Edited List
     */
    public static ArrayList<String> endList(ArrayList<String> listToEdit){
        String lastStatement = listToEdit.get(listToEdit.size() -1);
        lastStatement = lastStatement.substring(0, lastStatement.length() -1);
        lastStatement = lastStatement + ".";
        listToEdit.remove(listToEdit.size() -1);
        listToEdit.add(lastStatement);
        return listToEdit;
    }



    /**
     * Language Codes
     */
    public enum LanguageCode{
        DUTCH, ENGLISH, FRENCH, GERMAN, ITALIAN, PORTUGESE, RUSSIAN, SPANISH;

        @Override
        public String toString(){
            switch (this){
                case DUTCH:
                    return "nld"; // also "dut"
                case ENGLISH:
                    return "eng";
                case FRENCH:
                    return "fra";
                case GERMAN:
                    return "deu";
                case ITALIAN:
                    return "ita";
                case PORTUGESE:
                    return "por";
                case RUSSIAN:
                    return "rus";
                case SPANISH:
                    return "spa";
                default:
                    return "";
            }
        }
    }


}
