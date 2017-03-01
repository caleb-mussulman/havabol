package havabol;

import java.util.*;

public class STFunction extends STEntry
{
    
    public int returnType;          // function's return type
    public int subClassif;          // function subclassification (builtin vs. user defined)
    public int numArgs;             // number of arguments the function takes
    public ArrayList parmList;      // parameter list that will hold the function's parameters
    public SymbolTable symbolTable; // each user defined function will need it's own symbol table
    
    // Constants for return type of function are located in Token's OPERAND subclassifications
    /*
     * public static final int INTEGER = 2; // integer constant
     * public static final int FLOAT   = 3; // float constant
     * public static final int BOOLEAN = 4; // boolean constant
     * public static final int STRING  = 5; // string constant
     * public static final int DATE    = 6; // date constant
     * public static final int VOID    = 7; // void
     */
    
    // Constants for function subclassification are located in Token's FUNCTION subclassifications
    /*
     * public static final int BUILTIN = 13;// builtin function (e.g., print)
     * public static final int USER    = 14;// user defined
     */
    
    // Constant for a function with a variable number of arguments
    public static final int VAR_ARGS = -1;
    
    /**
     * Creates an STFunction object for a function that will be put
     * into the symbol table
     * <p>
     * The function will have a unique name (i.e. print, MAXELEM) and
     * will have an explicit return type. It will also have a subclassification
     * whether it is a builtin or user defined function, and it can either
     * have a variable number or an explicitly defined number of arguments passed
     * to it.
     * @param symbol      the token string (i.e. name) of the function
     * @param primClassif used for calling STEntry constructor, should always be Token.FUNCTION
     * @param returnType  the return type of the function
     * @param subClassif  builtin or user defined function
     * @param numArgs     the number of arguments for the function
     */
    public STFunction(String symbol, int primClassif, int returnType, int subClassif, int numArgs)
    {
        super(symbol, primClassif);
        
        // set return type
        this.returnType = returnType;
        
        // set subClassif
        this.subClassif = subClassif;
        
        // set number of arguments
        this.numArgs = numArgs;
    }
}
