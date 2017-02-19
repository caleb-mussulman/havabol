package havabol;

import java.util.HashMap;
/**
 * 
 * @author root
 */
public class SymbolTable     
{
    public String symbol;
    public HashMap<String, STEntry> ht;
    
    public SymbolTable() 
    {
        //Creating our HashMap
	ht = new HashMap<String, STEntry>();
        //Initializing Key Values in the HashMap
        initGlobal();
    }
	
    STEntry getSymbol(String symbol)
    {    	
    	// Found it in the HT
    	if (ht.containsKey(symbol))
    		return ht.get(symbol);

        return null;
    }
        
    void putSymbol(String symbol, STEntry entry) 
    {
    	
    	ht.put(symbol, entry);
        // symbol is key
//    	if (ht.containsKey(symbol)) {
//    		entry.primClassif = ht.get(symbol).primClassif;
//    	}
    	
    	// entry is value
    }
	
    private void initGlobal()
    {
        //==========================CONTROL==========================
        ht.put("def", new STControl("def", Token.CONTROL, Token.FLOW));
        ht.put("if", new STControl("if",Token.CONTROL,Token.FLOW));
        ht.put("for", new STControl("for",Token.CONTROL,Token.FLOW));
        ht.put("while", new STControl("while",Token.CONTROL,Token.FLOW));
        
        ht.put("enddef",new STControl("enddef", Token.CONTROL, Token.END));
        ht.put("else", new STControl("else",Token.CONTROL,Token.END));
        ht.put("endfor", new STControl("endfor", Token.CONTROL, Token.END));
        ht.put("endwhile", new STControl("endwhile",Token.CONTROL,Token.END));
        
        ht.put("Int", new STControl("Int",Token.CONTROL,Token.DECLARE));
        ht.put("Float", new STControl("Float",Token.CONTROL,Token.DECLARE));
        ht.put("String", new STControl("String",Token.CONTROL,Token.DECLARE));
        ht.put("Bool", new STControl("Bool",Token.CONTROL,Token.DECLARE));
        ht.put("Date", new STControl("Date",Token.CONTROL,Token.DECLARE));
        
        //===========================FUNCTIONS=======================
        ht.put("print", new STFunction("print", Token.FUNCTION,Token.VOID
                 , Token.BUILTIN, STFunction.VAR_ARGS));
        
        ht.put("LENGTH", new STFunction("LENGTH", Token.FUNCTION,Token.INTEGER
                 , Token.BUILTIN, 1));
        ht.put("MAXLENGTH", new STFunction("MAXLENGTH", Token.FUNCTION,Token.INTEGER
                 , Token.BUILTIN, 1));
        
        // RETURN TYPE IS BOOL, BUT TYPE IS DEFINIED AS INTEGER IN NOTES?
        ht.put("SPACES", new STFunction("SPACE", Token.FUNCTION,Token.BOOLEAN
                 , Token.BUILTIN, 1));
        
        ht.put("ELEM", new STFunction("ELEM", Token.FUNCTION,Token.INTEGER
                 , Token.BUILTIN, 1));
        ht.put("MAXELEM", new STFunction("MAXELEM", Token.FUNCTION,Token.INTEGER
                 , Token.BUILTIN, 1));
        
        //==========================OPERATORS========================
        ht.put("and", new STEntry("and", Token.OPERATOR));
        ht.put("or", new STEntry("or", Token.OPERATOR));
        ht.put("not", new STEntry("not", Token.OPERATOR));
        ht.put("in", new STEntry("in", Token.OPERATOR));
        ht.put("notin", new STEntry("notin", Token.OPERATOR));
    }
}
