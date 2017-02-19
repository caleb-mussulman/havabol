package havabol;

import java.util.*;

/**
 *
 * @author root
 */
public class STFunction extends STEntry
{

    //issue with VAR_ARGS???
    static int VAR_ARGS;
    int primClassif;
    int subClassif;
    int returnType;
    ArrayList parmList;
    String symbol;
    SymbolTable symbolTable;
    
    public STFunction(String aSymbol, int aPrimClassif, int aReturnType, int aSubClassif, int aNumArgs)
    {
        super(aSymbol, aPrimClassif);
        
        // set the symbol
        this.symbol = aSymbol;
        
        // set the primClassif
        this.primClassif = aPrimClassif;
        
        // set a return type
        this.returnType = aReturnType;
        
        // set subClassif
        this.subClassif = aSubClassif;
        
        // set number of arguments
        this.VAR_ARGS = aNumArgs;
    }
}
