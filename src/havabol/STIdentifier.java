package havabol;

public class STIdentifier extends STEntry
{
   public int dclType;   // identifier declaration type (Int, Float, String, Bool, Date)
   public int parm;      // by reference, by value, or not a parameter
   public int nonLocal;  // base address reference (0 - local, 1 to k - surrounding, 99 - global)
   public int structure; // data structure (primitive, fixed array, unbounded array)
   
   // Constants for declare type are located in Token's OPERAND subclassifications
   /*
    * public static final int INTEGER       = 2; // integer constant
    * public static final int FLOAT         = 3; // float constant
    * public static final int BOOLEAN       = 4; // boolean constant
    * public static final int STRING        = 5; // string constant
    * public static final int DATE          = 6; // date constant
    */
   
   // Constants for parameter passing type
   public static final int BY_REFERENCE     = 21; // by reference parameter
   public static final int BY_VALUE         = 22; // by value parameter
   public static final int NOT_A_PARAMETER  = 23; // not a parameter
   
   // Constants for scope if local or global
   public static final int LOCAL            = 0;  // local scope
   public static final int GLOBAL           = 99; // global scope
   
   // Constants for data structure
   public static final int PRIMITVE         = 18; // primitive value structure
   public static final int FIXED_ARRAY      = 19; // fixed size array structure
   public static final int UNBOUNDED_ARRAY  = 20; // unbounded array structure
  
   /**
    * STIdentifier constructor that will initialize the follow values of
    * each token:
    * Declaration type
    * Parameter Passing type
    * Structure
    * Scope
    * <p>
    * See comments within method for examples of the values for each of
    * the following parameters:
    * @param symbol      the token string of the identifier
    * @param primClassif used for calling STEntry constructor, should always be Token.OPERAND (identifier)
    * @param dclType     constant specifying the declared type of the identifier
    * @param parm        constant specifying by reference, by value, or not a parameter
    * @param structure   constant specifying primitive, fixed array, or unbounded array
    * @param nonLocal    constant specifying the base address reference (0 - local, 1 to k - surrounding, 99 - global)
    */
   public STIdentifier(String symbol, int primClassif, int dclType, int parm, int structure, int nonLocal)
   {
       //calling STEntry's constructor
        super(symbol, primClassif);
        //Declaration Type -- INT, FLOAT, STRING, BOOL, DATE
        this.dclType = dclType;
        //Parameter type -- Not a Param, By Ref, By Value
        this.parm = parm;
        //Structure of the Identifier -- primitive, fixed array, unbounded array 
        this.structure = structure;
        //Address of the non-local reference of a variable.
        this.nonLocal = nonLocal;
   }
    
}
