package havabol;

import java.util.HashMap;
/**
 * 
 * @author root
 */
public class SymbolTable     
{
    public String symbol;
    public HashMap<String, STEntry> htGlobal;
    
    public SymbolTable() 
    {
        //Creating our HashMap
	htGlobal = new HashMap<String, STEntry>();
        //Initializing Key Values in the HashMap
        initGlobal();
    }
    /**
     * Takes in the working Token tokenStr as a symbol and uses it
     * as a key to do a hash lookup in HashMap ht. If the symbol is found:
     * Return A STEntry obj ref or STEntry subClasses obj ref.
     * Otherwise Return null
     * <p>
     * @param symbol effectively our working tokenStr
     * @return STEntry object reference or STEntry subClasses object reference 
     *        (STControl, STFunction, or STIdentifier))
     */
    STEntry getSymbol(String symbol)
    {    	
    	// The tokenStr (symbol) is in the HashMap ht
    	if (htGlobal.containsKey(symbol))
        {
            //Return the STEntry or STEntry Subclass value that is in the HashMap
            return htGlobal.get(symbol);
        }
        //Return an actual null when we miss in the ht
        return null;
    }
        
    void putSymbol(String symbol, STEntry entry) 
    {
        //Insert a new 
    	htGlobal.put(symbol, entry);
    }
    /**
     * Initializes all the keys in the HashMap ht to their corresponding values
     * <p>
     * This is only called once in the construction of a new symbolTable.
     * Since STControl, STFunction and STIdentifier are subClasses of STEntry
     * we can 
     */
    private void initGlobal()
    {
        //==========================CONTROL==========================
        htGlobal.put("def", new STControl("def",Token.CONTROL, Token.FLOW));
        htGlobal.put("if", new STControl("if",Token.CONTROL,Token.FLOW));
        htGlobal.put("for", new STControl("for",Token.CONTROL,Token.FLOW));
        htGlobal.put("while", new STControl("while",Token.CONTROL,Token.FLOW));
        
        htGlobal.put("enddef",new STControl("enddef",Token.CONTROL, Token.END));
        htGlobal.put("else", new STControl("else",Token.CONTROL,Token.END));
        htGlobal.put("endfor", new STControl("endfor", Token.CONTROL, Token.END));
        htGlobal.put("endwhile", new STControl("endwhile",Token.CONTROL,Token.END));
        
        htGlobal.put("Int", new STControl("Int",Token.CONTROL,Token.DECLARE));
        htGlobal.put("Float", new STControl("Float",Token.CONTROL,Token.DECLARE));
        htGlobal.put("String", new STControl("String",Token.CONTROL,Token.DECLARE));
        htGlobal.put("Bool", new STControl("Bool",Token.CONTROL,Token.DECLARE));
        htGlobal.put("Date", new STControl("Date",Token.CONTROL,Token.DECLARE));
        
        //===========================FUNCTIONS=======================
        htGlobal.put("print", new STFunction("print",Token.FUNCTION,Token.VOID
                      , Token.BUILTIN, STFunction.VAR_ARGS));
        
        htGlobal.put("LENGTH", new STFunction("LENGTH",Token.FUNCTION,Token.INTEGER
                       , Token.BUILTIN, 1));
        htGlobal.put("MAXLENGTH", new STFunction("MAXLENGTH",Token.FUNCTION,Token.INTEGER
                          , Token.BUILTIN, 1));
        
        // RETURN TYPE IS BOOL, BUT TYPE IS DEFINIED AS INTEGER IN NOTES?
        htGlobal.put("SPACES", new STFunction("SPACE",Token.FUNCTION,Token.BOOLEAN
                       , Token.BUILTIN, 1));
        
        htGlobal.put("ELEM", new STFunction("ELEM",Token.FUNCTION,Token.INTEGER
                     , Token.BUILTIN, 1));
        htGlobal.put("MAXELEM", new STFunction("MAXELEM",Token.FUNCTION,Token.INTEGER
                        , Token.BUILTIN, 1));
        
        //==========================OPERATORS========================
        htGlobal.put("and", new STEntry("and",Token.OPERATOR));
        htGlobal.put("or", new STEntry("or",Token.OPERATOR));
        htGlobal.put("not", new STEntry("not",Token.OPERATOR));
        htGlobal.put("in", new STEntry("in",Token.OPERATOR));
        htGlobal.put("notin", new STEntry("notin",Token.OPERATOR));
    }
}
