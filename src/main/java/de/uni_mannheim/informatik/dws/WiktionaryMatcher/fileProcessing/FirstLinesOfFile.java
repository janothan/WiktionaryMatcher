package de.uni_mannheim.informatik.dws.WiktionaryMatcher.fileProcessing;

import java.io.*;

public class FirstLinesOfFile {

    public static void main(String[] args) {
        printFirstLinesOfFile("C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\dbnary\\2019-08-07\\fr_dbnary_ontolex.ttl", 50);
        //printFirstLinesOfFile("C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\dbnary\\2019-08-07\\compressed\\en_dbnary_ontolex.ttl", 400);
    }

    /**
     * Outputs the first view lines of a file. This can be  useful when a very large file cannot be opened in
     * a text editor.
     * @param filePath
     * @param numberOfLines
     */
    public static void printFirstLinesOfFile(String filePath,  int numberOfLines) {
        System.out.println("START\n\n");
        File f = new File(filePath);
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String readLine;
            int linesRead = 0;
            while((readLine = br.readLine()) != null){
                //if(readLine.endsWith(".")) {
                //    System.out.println(readLine);
                //}
                System.out.println(readLine);
                linesRead++;
                if(linesRead == numberOfLines){
                    break;
                }
            }
            br.close();
        } catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
        System.out.println("DONE\n\n");
    }

}
