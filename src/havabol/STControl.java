package havabol;

public class STControl extends STEntry
{
    int subClassif;     //To store the subClassif of the CONTROL token
    
    /**
     * STControl constructor that will simply initialize the subClassif
     * of the working token. 
     * <p> 
     * @param aSymbol working tokens tokenStr
     * @param aPrimClassif primClassif defined by superclass STEntry
     * @param aSubClassif the subClassif to be initalized
     */
    public STControl(String aSymbol, int aPrimClassif, int aSubClassif)
    {
        //Calling STEntry's constructor
        super(aSymbol, aPrimClassif);
        // set subClassif
        this.subClassif = aSubClassif;
    } 
}
