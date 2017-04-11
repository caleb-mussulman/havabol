package havabol;

import java.util.HashMap;

public class SymbolTable     
{
    public HashMap<String, STEntry> ht;
    public StorageManager storageManager;
    
    /**
     * SymbolTable constructor that will initialize a hash map to
     * act as the symbol table to store the symbols
     * <p>
     * The hash map will first be initialized with language defined
     * symbols first
     */
    public SymbolTable()
    {
        //Creating our HashMap
        ht = new HashMap<String, STEntry>();
        storageManager = new StorageManager();
        //Initializing Definition Values in the HashMap
        initGlobal();
    }
    
    /**
     * Takes in the working Token tokenStr as a symbol and uses it
     * as a key to do a hash lookup in HashMap ht. If the symbol is found:
     * Return A STEntry object ref or STEntry subClasses object ref.
     * Otherwise Return null
     * <p>
     * Important to call this function with caution by making sure
     * the subClassif == IDENTIFIER 
     * @param symbol    - Effectively our working tokenStr
     * @return STEntry  - Object reference or STEntry subClasses object reference 
     *                    (STControl, STFunction, or STIdentifier))
     */
    STEntry getSymbol(String symbol)
    {    	
    	// The tokenStr (symbol) is in the HashMap ht
    	if (ht.containsKey(symbol))
        {
            //Return STEntry or STEntry Subclass value thats in ht
            return ht.get(symbol);
        }
        //Return an actual null upon miss in ht
        return null;
    }
    
    /**
     * Used to insert a (key, value) pair into a new SymbolTable.
     * <p>
     * in future programs
     * @param symbol  - Effectively our working tokenStr
     * @param entry   - An STEntry object, 
     *                  the superclass of STControl, STFunction, STIdentifier
     */
    void putSymbol(String symbol, STEntry entry) 
    {
        //TODO: Cover this with Caleb.
        //IMORTANT NOTE: This function will ONLY be called upon the declaration of a new variable...
        //               If that new variable has the same 'key' then we must remove the reference
        //We already have that 'key' in the SymbolTable.
        if(ht.containsKey(symbol))
        {
            //Check if there is a value for the corresponding StorageManager hashmap.
            if(storageManager.sm.get(symbol) != null){
                //There is a resultValue in the storageManager from a previous declaration. Remove that reference.

                //Note: HashMap.put over-writes the <key, value> pair.
                //Manually put the symbol back in with null as it's value.
                storageManager.sm.put(symbol, null);
            }
            //Effectively changing the reference to the object
        }
        //If the symbol wasn't in the SymbolTable.ht already, declare it.
    	ht.put(symbol, entry);
    }
    
    /**
     * Receives a symbol so it can call storageManager's function 
     * getVariableValue and return a ResultValue to where ever 
     * the value associated with that symbol is needed.
     * <p>
     * @param errParse   - The Parser so we can use it's error() method
     * @param symbol     - The variable name
     * @return resVal    - ResultValue variable to where ever that value is needed.
     * @throws Exception
     */
    ResultValue retrieveVariableValue(Parser errParse, String symbol) throws Exception
    {
        // First check that the variable has been declared
        if(! ht.containsKey(symbol))
        {
            errParse.error("Variable '%s' has not been declared", symbol);
        }
        // Variable has been declared, so get it from the storage manager
        ResultValue resVal = storageManager.getVariableValue(errParse, symbol);
        return resVal;
    }
    
    /**
     * TODO: How can we use this method to store both ResultValues and ResultArrays in the storageManager?
     * Stores value into the storageManager HashMap.
     * <p>
     * @param errParse - The Parser so we can use it's error() method
     * @param symbol   - The variable name
     * @param value    - The value from 
     */
    void storeVariableValue(Parser errParse, String symbol, ResultValue value) throws Exception
    {
        //Check if the symbol is already declared.
        if(ht.containsKey(symbol))
        {
            // Get the type of the variable
            int symbolType = ((STIdentifier) ht.get(symbol)).dclType;
            
            // Check that the type of the value equals the type of the variable
            // (A string variable may be assigned a numeric value)
            if((value.type == symbolType) || (symbolType == Token.STRING && ((value.type == Token.INTEGER) || (value.type == Token.FLOAT))))
            {
                //TODO: We need a reference to putResultArray from StorageManager
                storageManager.putVariableValue(errParse, symbol, value);
            }
            // The types do not match
            else
            {
                errParse.error("Variable '%s' of type '%s' can not be assigned value '%s' of type '%s'"
                              ,symbol, Token.getType(errParse, symbolType) ,value.value, Token.getType(errParse, value.type)); 
            }
        }
        // The variable has not been declared
        else
        {
            errParse.error("Variable '%s' has not been declared", symbol);
        }
    }

    /**
     * Initializes all the keys in the HashMap ht to their corresponding values
     * <p>
     * This method is only called once in the construction of a new symbolTable.
     * initGlobal() is a private function, only allowing it 
     * to be accessed from within SymbolTable.java
     */
    private void initGlobal()
    {
        //==========================CONTROL==========================
        ht.put("def", new STControl("def",Token.CONTROL, Token.FLOW));
        ht.put("if", new STControl("if",Token.CONTROL,Token.FLOW));
        ht.put("for", new STControl("for",Token.CONTROL,Token.FLOW));
        ht.put("while", new STControl("while",Token.CONTROL,Token.FLOW));
        
        ht.put("to", new STControl("to", Token.CONTROL, Token.END));
        ht.put("by", new STControl("by", Token.CONTROL, Token.END));
        ht.put("in", new STControl("in", Token.CONTROL, Token.END));
        ht.put("from", new STControl("from", Token.CONTROL, Token.END));
        
        ht.put("enddef",new STControl("enddef",Token.CONTROL, Token.END));
        ht.put("endif", new STControl("endif",Token.CONTROL,Token.END));
        ht.put("else", new STControl("else",Token.CONTROL,Token.END));
        ht.put("endfor", new STControl("endfor", Token.CONTROL, Token.END));
        ht.put("endwhile", new STControl("endwhile",Token.CONTROL,Token.END));
        
        ht.put("Int", new STControl("Int",Token.CONTROL,Token.DECLARE));
        ht.put("Float", new STControl("Float",Token.CONTROL,Token.DECLARE));
        ht.put("String", new STControl("String",Token.CONTROL,Token.DECLARE));
        ht.put("Bool", new STControl("Bool",Token.CONTROL,Token.DECLARE));
        ht.put("Date", new STControl("Date",Token.CONTROL,Token.DECLARE));
        
        //===========================FUNCTIONS=======================
        ht.put("print", new STFunction("print",Token.FUNCTION,Token.VOID
                      , Token.BUILTIN, STFunction.VAR_ARGS));
        ht.put("LENGTH", new STFunction("LENGTH",Token.FUNCTION,Token.INTEGER
                       , Token.BUILTIN, 1));        
        ht.put("SPACES", new STFunction("SPACES",Token.FUNCTION,Token.BOOLEAN
                       , Token.BUILTIN, 1));
        ht.put("ELEM", new STFunction("ELEM",Token.FUNCTION,Token.INTEGER
                     , Token.BUILTIN, 1));
        ht.put("MAXELEM", new STFunction("MAXELEM",Token.FUNCTION,Token.INTEGER
                        , Token.BUILTIN, 1));
        ht.put("debug", new STFunction("debug", Token.FUNCTION, Token.VOID
                      , Token.BUILTIN, 2));
        
        //==========================OPERATORS========================
        ht.put("and", new STOperator("and",Token.OPERATOR, Token.BINARY));
        ht.put("or", new STOperator("or",Token.OPERATOR, Token.BINARY));
        ht.put("not", new STOperator("not",Token.OPERATOR, Token.UNARY));
        ht.put("in", new STOperator("in",Token.OPERATOR, Token.BINARY));
        ht.put("notin", new STOperator("notin",Token.OPERATOR, Token.BINARY));
    }
}
