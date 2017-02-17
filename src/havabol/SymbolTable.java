package havabol;

import java.util.HashMap;

public class SymbolTable     
{
    public String symbol;
    public HashMap<String, STEntry> ht;
	public SymbolTable() 
    {
	    ht = new HashMap<String, STEntry>();
	    initGlobal();
	}
	
	STEntry getSymbol (String symbol) 
    {
	    return null;	
	}
        
	void putSymbol (String symbol, STEntry entry) 
	{
		
	}
	
	private void initGlobal()
	{
            
	    ht.put("if", new STControl("if",Token.CONTROL,Token.FLOW));
        ht.put("endif", new STControl("endif",Token.CONTROL,Token.END));        
        ht.put("for", new STControl("for",Token.CONTROL,Token.FLOW));

        ht.put("Int", new STControl("Int",Token.CONTROL,Token.DECLARE));
        ht.put("Float", new STControl("Float",Token.CONTROL,Token.DECLARE));

        ht.put("print", new STFunction("print",Token.FUNCTION,Token.VOID
                 , Token.BUILTIN, STFunction.VAR_ARGS));

        ht.put("and", new STEntry("and", Token.OPERATOR));
        ht.put("or", new STEntry("or", Token.OPERATOR));
		
	}
}
