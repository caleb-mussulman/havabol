package havabol;

import java.util.HashMap;
/**
 * 
 * <p>
 * 
 */
public class StorageManager
{
    
    String symbol;
    ResultValue value;
    
    public HashMap<String,ResultValue> sm;      //Declare the hashmap
    
    /**
     * Simply creates HashMap that will effectively be our
     * StorageManager.
     * <p>
     */
    StorageManager()
    {
        //Create a new HashMap -- Empty
        sm = new HashMap<String,ResultValue>();
    }
    
    /**
     * The function that will be used to store a variable value
     * into the HashMap using it's associated symbol (variable)
     * <p>
     * 
     * @param symbol
     * @param value 
     */
    void storeVariableValue(String symbol, String value){
        
    }
    
    /**
     * The function that will be used to get the value of the passed
     * symbol (variable) from the prase(?) to retrieve the value 
     * of the symbol(variable).
     * <p>
     * Quickly does a hash lookup for the passed String symbol and returns
     * the value as a string...
     * Conversion will be done by numeric class
     * @param symbol
     * @return ResultValue
     */
    String getVariableValue(String symbol){
        String value;
        value = get.sm(symbol);    
        return value;
    }
    
}
