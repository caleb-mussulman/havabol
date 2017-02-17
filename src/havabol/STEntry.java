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
class STEntry
{

	String symbol;
	int primClassif;
	
	public static final int OR = 1;
    public static final int AND = 2;
    public static final int NOT = 3;
    public static final int IN = 4;
    public static final int NOTIN = 5;
	
    public STEntry(String aSymbol, int aPrimClassif)
    {
        this.symbol = aSymbol;
        this.primClassif = aPrimClassif;
    }
    
}
