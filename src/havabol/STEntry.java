package havabol;

class STEntry
{
    String symbol;   // the token string for the symbol table entry
    int primClassif; // the primary classification for the symbol
    
    // Constants for the primary classification are defined in Token
    /*
     * public static final int OPERAND   = 1; // constants, identifier
     * public static final int OPERATOR  = 2; // + - * / < > = ! 
     * public static final int SEPARATOR = 3; // ( ) , : ; [ ]
     * public static final int FUNCTION  = 4; // TBD
     * public static final int CONTROL   = 5; // TBD
     * public static final int EOF       = 6; // EOF encountered
     * public static final int RT_PAREN  = 7; // TBD
     */
	
    /**
     * Creates an STEntry and initializes the values of the
     * working token's tokenStr and it's primClassif
     * <p>
     * This constructor is also called from the subclasses 
     * STControl, STFunction, and STIdentifier
     * @param symbol      the token string for the symbol table entry
     * @param primClassif the primClassif of the token
     */
    public STEntry(String symbol, int primClassif)
    {
        this.symbol = symbol;
        this.primClassif = primClassif;
    }
    
}
