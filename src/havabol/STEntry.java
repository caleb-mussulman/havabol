package havabol;

class STEntry
{
    String symbol;
    int primClassif;
	
    /**
     * STEntry Constructor called by It's sub classes
     * to initalize the values of the working tokens tokenStr
     * and it's primClassif
     * <p>
     * @param aSymbol working tokens tokenSTr
     * @param aPrimClassif the primClassif of the token
     */
    public STEntry(String aSymbol, int aPrimClassif)
    {
        this.symbol = aSymbol;
        this.primClassif = aPrimClassif;
    }
    
}
