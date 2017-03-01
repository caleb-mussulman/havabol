package havabol;

public class STControl extends STEntry
{
    int subClassif;  // control subclassification (flow, end, declare)
    
    // Constants for control subclassification are located in Token's CONTROL subclassifications
    /*
     * public static final int FLOW       = 10; // flow statement (e.g., if)
     * public static final int END        = 11; // end statement (e.g., endif)
     * public static final int DECLARE    = 12; // declare statement (e.g., Int)
     */
    
    /**
     * STControl constructor that will simply initialize the subClassif
     * of the working token. 
     * <p>
     * @param symbol      the token string of the control
     * @param primClassif used for calling STEntry constructor, should always be Token.CONTROL
     * @param subClassif  the type of control statement - flow, end, or declare
     */
    public STControl(String symbol, int primClassif, int subClassif)
    {
        //Calling STEntry's constructor
        super(symbol, primClassif);
        // set subClassif
        this.subClassif = subClassif;
    } 
}
