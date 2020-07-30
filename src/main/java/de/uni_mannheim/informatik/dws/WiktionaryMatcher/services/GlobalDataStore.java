package de.uni_mannheim.informatik.dws.WiktionaryMatcher.services;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Resource;

import java.util.HashMap;

/**
 * Store accessible to all matchers where variables and results can be persisted in.
 */
public class GlobalDataStore {

    /**
     * Singleton instance
     */
    private static GlobalDataStore instance;

    /**
     * Constructor (Singleton Pattern)
     */
    private GlobalDataStore(){
    }

    /**
     * Get global data store instance.
     * @return Instance of the global data store.
     */
    public static GlobalDataStore getInstance(){
        if(instance == null){
            instance = new GlobalDataStore();
            return instance;
        } else {
            return instance;
        }
    }

    /**
     * Central Store Object
     */
    private HashMap<String, Object> centralStore = new HashMap<>();

    /**
     * Put an object to the data store.
     * @param key Key
     * @param value Value
     */
    public void put(String key, Object value){
        centralStore.put(key, value);
    }

    /**
     * Get an object from the data store using a key.
     * @param key Key used to retrieve object.
     * @return Value stored for key.
     */
    public Object get(String key){
        return centralStore.get(key);
    }

    /**
     * Check if key exists in store.
     * @param key Key that shall be looked up.
     * @return true if key contained, else false.
     */
    public boolean containsKey(String key){
        return centralStore.containsKey(key);
    }

    /**
     * Delete store.
     */
    public void clear(){
        centralStore = new HashMap<>();
    }

    /**
     * Get the ontology ID in order to store ontology-related info.
     * @param model Model for which ID shall be retrieved.
     * @return ID as string.
     */
    public static String getOntId(OntModel model){
        try {
            QueryExecution queryExecution = QueryExecutionFactory.create(QueryFactory.create(("SELECT ?ont WHERE { ?ont <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Ontology> . }")), model);
            ResultSet result = queryExecution.execSelect();
            if (result.hasNext()) {
                QuerySolution solution = result.next();
                Resource resource = solution.getResource("ont");
                return resource.getURI();
            } else return null;
        } catch (Exception e){
            return null;
        }
    }

}
