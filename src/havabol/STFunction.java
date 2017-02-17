/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package havabol;

import java.util.*;

/**
 *
 * @author root
 */
public class STFunction extends STEntry
{
    int primClassif;
    int subClassif;
    int returnType;
    int numArgs;
    ArrayList parmList;
    String symbol;
    
    SymbolTable symbolTable;
    
    public static final int INT = 1;
    public static final int FLOAT = 2;
    public static final int STRING = 3;
    public static final int BOOL = 4;
    public static final int DATE = 5;
    public static final int VOID = 6;
    
    public static final int USER = 7;
    public static final int BUILTIN = 8;
    
    // POSSIBLE TO CHANGE TO ARRAY?!
    public static final int VAR_ARGS = -1;
    
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
        this.numArgs = aNumArgs;
    }
}
