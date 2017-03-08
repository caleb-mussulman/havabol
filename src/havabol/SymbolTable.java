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
    ResultValue retrieveVariableValue(Parser errParse, String symbol) throws Exception{
        //All Error checking is done in storageManager.getVariableValue
        ResultValue resVal = storageManager.getVariableValue(errParse, symbol);
        return resVal;
    }
    
    /**
     * Stores value into the storageManager HashMap.
     * <p>
     * @param errParse - The Parser so we can use it's error() method
     * @param symbol   - The variable name
     * @param value    - The value from 
     */
    void storeVariableValue(Parser errParse, String symbol, ResultValue value) throws Exception{
        //Check if the symbol is already declared.
        if(ht.containsKey(symbol)){
            //Check if the ResultValue's type matches the type
            int symbolType = ((STIdentifier) ht.get(symbol)).dclType;
            if(value.type ==  symbolType){
                storageManager.putVariableValue(errParse, symbol, value);
            }else {
                errParse.error("Variable '%s' of type '%s' can not be assigned value '%s' of type '%s'"
                              ,symbol, symbolType ,value.value, value.type); 
            }
        }else {
            errParse.error("Variable: '%s' has not been declared", symbol);
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
        
        ht.put("enddef",new STControl("enddef",Token.CONTROL, Token.END));
        ht.put("endif", new STControl("if",Token.CONTROL,Token.END));
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
        ht.put("MAXLENGTH", new STFunction("MAXLENGTH",Token.FUNCTION,Token.INTEGER
                          , Token.BUILTIN, 1));
        
        ht.put("SPACES", new STFunction("SPACE",Token.FUNCTION,Token.BOOLEAN
                       , Token.BUILTIN, 1));
        
        ht.put("ELEM", new STFunction("ELEM",Token.FUNCTION,Token.INTEGER
                     , Token.BUILTIN, 1));
        ht.put("MAXELEM", new STFunction("MAXELEM",Token.FUNCTION,Token.INTEGER
                        , Token.BUILTIN, 1));
        
        //==========================OPERATORS========================
        ht.put("and", new STEntry("and",Token.OPERATOR));
        ht.put("or", new STEntry("or",Token.OPERATOR));
        ht.put("not", new STEntry("not",Token.OPERATOR));
        ht.put("in", new STEntry("in",Token.OPERATOR));
        ht.put("notin", new STEntry("notin",Token.OPERATOR));
    }
}
