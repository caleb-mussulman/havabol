/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
    public static final int FLOW = 1;
    public static final int END = 2;
    public static final int DECLARE = 3;
    
    public static final int CONTROL = 4;
    
    // CONSTRUCTOR
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
