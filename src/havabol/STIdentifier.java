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
public class STIdentifier
{
   
   public String parm;
   public int nonLocal;
   public int structure;
   //==========Structures==========
   public static final int PRIMITIVE = 1;
   public static final int FIXED_ARRAY = 2;
   public static final int UNBOUNDED_ARRAY = 3;
   public static final int USER = 4;
   //=========Parameters===========
   public static final int VALUE = 5;
   public static final int REF = 6;
   public static final int NOT_PARM = 7;
   //==========Declare Type========
   public String dclType;
   //==========Non-Locals==========
   public static final int LOCAL = 0;
   public static final int GLOBAL = 99;
   /**
    * 
    * @param aDclType
    * @param aParm
    * @param aStructure
    * @param aNonLocal 
    */
   public STIdentifier(String aDclType, String aParm, int aStructure, int aNonLocal)
   {
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
