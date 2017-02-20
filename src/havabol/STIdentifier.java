package havabol;

/**
 *
 * @author root
 */
public class STIdentifier extends STEntry
{
   public String parm;
   public int nonLocal;
   public int structure;
   public String dclType;

   /**
    * Initalizes parameters to the fields of the object
    * <p>
    * @param aDclType
    * @param aParm
    * @param aStructure
    * @param aNonLocal 
    */
   public STIdentifier(String aSymbol, int aPrimClassif, String aDclType, String aParm, int aStructure, int aNonLocal)
   {
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
