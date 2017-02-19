package havabol;

/**
 *
 * @author root
 */
public class STControl extends STEntry
{
   
    String symbol;
    int primClassif;
    int subClassif;
    
    public STControl(String aSymbol, int aPrimClassif, int aSubclassif)
    {
        super(aSymbol, aPrimClassif);
        
        // set the symbol
        this.symbol = aSymbol;
        
        // set the primClassif
        this.primClassif = aPrimClassif;
        
        // set subClassif
        this.subClassif = aSubclassif;
    }
}
