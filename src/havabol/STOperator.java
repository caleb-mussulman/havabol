package havabol;

public class STOperator extends STEntry
{
    int subClassif; // operator's number of operands (unary, binary)
    
    // Constants for operator sub-classification are located in Token's OPERATOR sub-classifications
    /*
     * public static final int UNARY      = 15;
     * public static final int BINARY     = 16;
     */

    /**
     * STOperator's constructor that will simply initialize the subClassif
     * of the working token. 
     * <p>
     * @param symbol      the token string of the operator
     * @param primClassif used for calling STEntry constructor, should always be Token.OPERATOR
     * @param subClassif  the operator's number of operands
     */
    public STOperator(String symbol, int primClassif, int subClassif)
    {
        // Calling STEntry's constructor
        super(symbol, primClassif);
        // Set subClassif
        this.subClassif = subClassif;
    }

}
