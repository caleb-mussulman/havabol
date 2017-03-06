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
     * The hashmap will first be initialized with language defined
     * symbols first
     */
    public SymbolTable()
    {
        //Creating our HashMap
        ht = new HashMap<String, STEntry>();
        storageManager = new StorageManager();
        //Initializing Key Values in the HashMap
        initGlobal();
    }
    
    /**
     * Takes in the working Token tokenStr as a symbol and uses it
     * as a key to do a hash lookup in HashMap ht. If the symbol is found:
     * Return A STEntry object ref or STEntry subClasses object ref.
     * Otherwise Return null
     * <p>
     * @param symbol effectively our working tokenStr
     * @return STEntry object reference or STEntry subClasses object reference 
     *        (STControl, STFunction, or STIdentifier))
     */
    STEntry getSymbol(String symbol)
    {    	
    	// The tokenStr (symbol) is in the HashMap ht
    	if (ht.containsKey(symbol))
        {
            //Return the STEntry or STEntry Subclass value that is in the HashMap
            return ht.get(symbol);
        }
        //Return an actual null when we miss in the ht
        return null;
    }
    
    /**
     * Used to insert a (key, value) hash pair into a new SymbolTable.
     * <p>
     * This function is not used in Program #2 -- it will be used for it's functions
     * in future programs
     * @param symbol effectively our working tokenStr
     * @param entry An STEntry object, the superclass of STControl, STFunction, STIdentifier
     */
    void putSymbol(String symbol, STEntry entry) 
    {
    	ht.put(symbol, entry);
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
