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
    public int returnType;
    public int definedBy;
    public int numArgs;
    ArrayList parmList;
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
    
    public STFunction(String symbol, int primClassif, int type, int definedBy, int numArgs)
    {
        super(symbol, primClassif);
    }
    
    
}
