package havabol;

import java.util.HashMap;
/**
 * 
 * 
 */
public class StorageManager
{
    /* This class should NOT need fields for
    *  symbol and ResultValue because those variables
    *  are stored in the hashtable, not in an instance of StorageManager
    */
    public HashMap<String,ResultValue> sm;
    
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
    void storeVariableValue(Parser errParse, String symbol, ResultValue value)
    {
        //TODO : Error Checking on both symbol and ResultValue
        
        //Need to be able to test this... not sure if works as intended...
        sm.put(symbol,value);
        
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
    ResultValue getVariableValue(Parser errParse, String symbol)
    {
        
        ResultValue resValue;
        
        resValue = sm.get(symbol);
        //We need to check if our symbol is in the StorageManager
        if (resValue == null){
            //There is no value associated with that (valid) key
            //Call parser.error to call the ParserException
            
            
            
        } 
        return resValue;
    }
    
}
