package havabol;

public class STIdentifier extends STEntry
{
   public String dclType;
   public String parm;
   public int nonLocal;
   public int structure;
  
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
    * @param aDclType 
    * @param aParm
    * @param aStructure
    * @param aNonLocal 
    */
   public STIdentifier(String aSymbol, int aPrimClassif, String aDclType, String aParm, int aStructure, int aNonLocal)
   {
       //calling STEntry\s constructor
       super(aSymbol, aPrimClassif);
        //Declaration Type -- INT, FLOAT, STRING, BOOL, DATE
        this.dclType = aDclType;
        //Parameter type -- Not a Param, By Ref, By Value
        this.parm = aParm;
        //Structure of the Identifier -- primitive, fixed array, unbounded array 
        this.structure = aStructure;
        //Address of the non-local reference of a variable.
        this.nonLocal = aNonLocal;
   }
    
}
