package havabol;

/**
 * @desc Performs an operation on two numerics, stores the result as a ResultValue,
 * and returns it to whatever called it.
 *
 * @authors Taylor Brauer
 */
public class Utility 
{
	/*
	 * CLASS VARIABLES
	 */
	
	// CONSTANTS FOR UTILITY
	/*
	 * Type: (same as in token classifications.
	 * INTEGER = 2
	 * FLOAT   = 3
	 */
	
	/*
	 *  CONSTRUCTOR
	 */
    public Utility()
    {
    	
    }
    
    /*
     * CLASS METHODS
     */
    // Assignment Operators (+=, -=, *=, /=)
    public static ResultValue subtract(Parser parser, Numeric nop1, Numeric nop2) 
    {
    	ResultValue res = new ResultValue();
    	
    	// Is a floating point value.
    	if (nop1.resval.structure == 15 && nop1.resval.type == 3) 
    	{
    		nop1.type = 3;
    		nop2.type = 3;
    		
    		nop1.doubleValue = Double.parseDouble(nop1.resval.value);
    		nop2.doubleValue = Double.parseDouble(nop2.resval.value);
    		
    		Double tempValue = nop1.doubleValue - nop2.doubleValue;
    		
    		// The updated value gets stored in nop1 because it is of form -=
    		nop1.strValue = tempValue.toString();
    		
    		res.value = String.format("%0.2s", nop1.strValue);
    	}
    	// It is an integer value.
    	else if (nop1.resval.structure == 15 && nop1.resval.type == 2) 
    	{
    		nop1.type = 2;
    		nop2.type = 2;
    		
    		nop1.integerValue = Integer.parseInt(nop1.resval.value);
    		nop2.integerValue = Integer.parseInt(nop2.resval.value);
    		
    		int subValue = nop1.integerValue - nop2.integerValue;
    		
    		// The updated value gets stored in nop1 because it is of form -=
    		nop1.strValue = String.valueOf(subValue);
    		
    		res.value = String.format("%0.2s", nop1.strValue);
    	}
    	
		return res;
    }
    public static ResultValue add(Parser parser, Numeric nop1, Numeric nop2) 
    {
    	ResultValue res = new ResultValue();
    	
    	// Is a floating point value.
    	if (nop1.resval.structure == 15 && nop1.resval.type == 3) 
    	{
    		nop1.type = 3;
    		nop2.type = 3;
    		
    		nop1.doubleValue = Double.parseDouble(nop1.resval.value);
    		nop2.doubleValue = Double.parseDouble(nop2.resval.value);
    		
    		Double tempValue = nop1.doubleValue + nop2.doubleValue;
    		
    		// The updated value gets stored in nop1 because it is of form +=
    		nop1.strValue = tempValue.toString();
    		
    		res.value = String.format("%0.2s", nop1.strValue);
    	}
    	// It is an integer value.
    	else if (nop1.resval.structure == 15 && nop1.resval.type == 2) 
    	{
    		nop1.type = 2;
    		nop2.type = 2;
    		
    		nop1.integerValue = Integer.parseInt(nop1.resval.value);
    		nop2.integerValue = Integer.parseInt(nop2.resval.value);
    		
    		int subValue = nop1.integerValue + nop2.integerValue;
    		
    		// The updated value gets stored in nop1 because it is of form +=
    		nop1.strValue = String.valueOf(subValue);
    		
    		res.value = String.format("%0.2s", nop1.strValue);
    	}
    	
		return res;
    }
    public static ResultValue mulitply(Parser parser, Numeric nop1, Numeric nop2) 
    {
    	ResultValue res = new ResultValue();
    	
    	// Is a floating point value.
    	if (nop1.resval.structure == 15 && nop1.resval.type == 3) 
    	{
    		nop1.type = 3;
    		nop2.type = 3;
    		
    		nop1.doubleValue = Double.parseDouble(nop1.resval.value);
    		nop2.doubleValue = Double.parseDouble(nop2.resval.value);
    		
    		Double tempValue = nop1.doubleValue * nop2.doubleValue;
    		
    		// The updated value gets stored in nop1 because it is of form *=
    		nop1.strValue = tempValue.toString();
    		
    		res.value = String.format("%0.2s", nop1.strValue);
    	}
    	// It is an integer value.
    	else if (nop1.resval.structure == 15 && nop1.resval.type == 2) 
    	{
    		nop1.type = 2;
    		nop2.type = 2;
    		
    		nop1.integerValue = Integer.parseInt(nop1.resval.value);
    		nop2.integerValue = Integer.parseInt(nop2.resval.value);
    		
    		int subValue = nop1.integerValue * nop2.integerValue;
    		
    		// The updated value gets stored in nop1 because it is of form *=
    		nop1.strValue = String.valueOf(subValue);
    		
    		res.value = String.format("%0.2s", nop1.strValue);
    	}
    	
		return res;
    }
    public static ResultValue divide(Parser parser, Numeric nop1, Numeric nop2) 
    {
    	ResultValue res = new ResultValue();
    	
    	// Is a floating point value.
    	if (nop1.resval.structure == 15 && nop1.resval.type == 3) 
    	{
    		nop1.type = 3;
    		nop2.type = 3;
    		
    		nop1.doubleValue = Double.parseDouble(nop1.resval.value);
    		nop2.doubleValue = Double.parseDouble(nop2.resval.value);
    		
    		Double tempValue = nop1.doubleValue / nop2.doubleValue;
    		
    		// The updated value gets stored in nop1 because it is of form /=
    		nop1.strValue = tempValue.toString();
    		
    		res.value = String.format("%0.2s", nop1.strValue);
    	}
    	// It is an integer value.
    	else if (nop1.resval.structure == 15 && nop1.resval.type == 2) 
    	{
    		nop1.type = 2;
    		nop2.type = 2;
    		
    		nop1.integerValue = Integer.parseInt(nop1.resval.value);
    		nop2.integerValue = Integer.parseInt(nop2.resval.value);
    		
    		int subValue = nop1.integerValue / nop2.integerValue;
    		
    		// The updated value gets stored in nop1 because it is of form /=
    		nop1.strValue = String.valueOf(subValue);
    		
    		res.value = String.format("%0.2s", nop1.strValue);
    	}
    	
		return res;
    }
    
    // Binary Operators (<=, >=)
    public static ResultValue geCompare(Parser parser, Numeric nop1, Numeric nop2) 
    {
    	ResultValue res = new ResultValue();
    	
    	// Determine what type and structure nop1 and nop2 are.
    	// PRIM, FLOAT
    	if (nop1.resval.structure == 15 && nop1.resval.type == 3)
    	{
    		// Set type
    		nop1.type = 3;
    		nop2.type = 3;
    		
    		// Do compare on the two
    		
    	
    		// Store the value of the result.
    	}
    	// PRIM, INT
    	else if (nop1.resval.structure == 15 && nop1.resval.type == 2) 
    	{
    		// Set type
    		nop1.type = 2;
    		nop2.type = 2;
    		
    		// Do compare on the two
        	
    		// Store the value of the result.
    	}
    	
    	
    	// Give the result to the caller.
    	return res;
    }
    
    // Unary Operators
    
}
