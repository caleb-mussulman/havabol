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
	
	/*
	 *  CONSTRUCTOR
	 */
    public Utility()
    {
    	
    }
    
    /*
     * CLASS METHODS -- needs to deal with coercion.
     */
    // Assignment Operators (+=, -=, *=, /=)
    public static ResultValue subtract(Parser parser, Numeric nop1, Numeric nop2) 
    {
    	ResultValue res = new ResultValue();
    	
    	// Compare the two values' type and coerce if needed
    	if (! compareType(nop1.resval.type, nop2.resval.type) ) 
    	{
    		coerceType(nop1, nop2);
    	}
    	
    	// Is a floating point value.
    	if (nop1.resval.type == Token.FLOAT) 
    	{
    		Double tempValue = nop1.doubleValue - nop2.doubleValue;
    		
    		// The updated value gets stored in nop1 because it is of form -=
    		nop1.strValue = tempValue.toString();
    		
    		res.value = String.format("%0.2s", nop1.strValue);
    		res.type  = nop1.resval.type;
    		res.structure = nop1.resval.structure;
    	}
    	// It is an integer value.
    	else if (nop1.resval.type == Token.INTEGER) 
    	{
    		int tempValue = nop1.integerValue - nop2.integerValue;
    		
    		// The updated value gets stored in nop1 because it is of form -=
    		nop1.strValue = String.valueOf(tempValue);
    		
    		res.value = String.format("%s", nop1.strValue);
    		res.type  = nop1.resval.type;
    		res.structure = nop1.resval.structure;
    	}
    	
		return res;
    }
    
    public static ResultValue add(Parser parser, Numeric nop1, Numeric nop2) 
    {
    	ResultValue res = new ResultValue();
    	
    	// Compare the two values' type and coerce if needed
    	if (! compareType(nop1.resval.type, nop2.resval.type) ) 
    	{
    		coerceType(nop1, nop2);
    	}
    	
    	// Is a floating point value.
    	if (nop1.resval.type == Token.FLOAT) 
    	{
    		Double tempValue = nop1.doubleValue + nop2.doubleValue;
    		
    		nop1.strValue = tempValue.toString();
    		
    		res.value = String.format("%0.2s", nop1.strValue);
    		res.type  = nop1.resval.type;
    		res.structure = nop1.resval.structure;
    	}
    	// It is an integer value.
    	else if (nop1.resval.type == Token.INTEGER) 
    	{
    		int tempValue = nop1.integerValue + nop2.integerValue;
    		
    		nop1.strValue = String.valueOf(tempValue);
    		
    		res.value = String.format("%s", nop1.strValue);
    		res.type  = nop1.resval.type;
    		res.structure = nop1.resval.structure;
    	}
    	
		return res;
    }
    public static ResultValue mulitply(Parser parser, Numeric nop1, Numeric nop2) 
    {
    	ResultValue res = new ResultValue();
    	
    	// Compare the two values' type and coerce if needed
    	if (! compareType(nop1.resval.type, nop2.resval.type) ) 
    	{
    		coerceType(nop1, nop2);
    	}
    	
    	// Is a floating point value.
    	if (nop1.resval.type == Token.FLOAT) 
    	{
    		Double tempValue = nop1.doubleValue * nop2.doubleValue;
    		
    		nop1.strValue = tempValue.toString();
    		
    		res.value = String.format("%0.2s", nop1.strValue);
    		res.type  = nop1.resval.type;
    		res.structure = nop1.resval.structure;
    	}
    	// It is an integer value.
    	else if (nop1.resval.type == Token.INTEGER) 
    	{
    		int tempValue = nop1.integerValue * nop2.integerValue;
    		
    		nop1.strValue = String.valueOf(tempValue);
    		
    		res.value = String.format("%s", nop1.strValue);
    		res.type  = nop1.resval.type;
    		res.structure = nop1.resval.structure;
    	}
    	
		return res;
    }
    public static ResultValue divide(Parser parser, Numeric nop1, Numeric nop2) 
    {
    	ResultValue res = new ResultValue();
    	
    	// Compare the two values' type and coerce if needed
    	if (! compareType(nop1.resval.type, nop2.resval.type) ) 
    	{
    		coerceType(nop1, nop2);
    	}
    	
    	// Is a floating point value.
    	if (nop1.resval.type == Token.FLOAT) 
    	{
    		Double tempValue = nop1.doubleValue / nop2.doubleValue;
    		
    		nop1.strValue = tempValue.toString();
    		
    		res.value = String.format("%0.2s", nop1.strValue);
    		res.type  = nop1.resval.type;
    		res.structure = nop1.resval.structure;
    	}
    	// It is an integer value.
    	else if (nop1.resval.type == Token.INTEGER) 
    	{
    		int tempValue = nop1.integerValue / nop2.integerValue;
    		
    		nop1.strValue = String.valueOf(tempValue);
    		
    		res.value = String.format("%s", nop1.strValue);
    		res.type  = nop1.resval.type;
    		res.structure = nop1.resval.structure;
    	}
    	
		return res;
    }
    
    // Binary Operators (<=, >=, <, >) => Value is a primitive
//    public static ResultValue compare(Parser parser, Numeric nop1, Numeric nop2) 
//    {
//    	ResultValue res = new ResultValue();
//    	
//    	// Determine what type and structure nop1 and nop2 are.
//    	// PRIM, FLOAT
//    	if (nop1.resval.structure == 15 && nop1.resval.type == 3)
//    	{
//    		// Set type
//    		nop1.type = 3;
//    		nop2.type = 3;
//    		
//    		// Convert
//    		nop1.doubleValue = Double.parseDouble(nop1.resval.value);
//    		nop2.doubleValue = Double.parseDouble(nop2.resval.value);
//    		
//    		// Do compare on the two
//    		if (nop1.doubleValue >= nop1.doubleValue) // >=
//    		{
//    			nop1.strValue = String.valueOf(nop1.doubleValue);
//    			res.value = String.format("%0.2s", nop1.strValue);
//    		}
//    		else if (nop1.doubleValue <= nop1.doubleValue) // <=
//    		{
//    			nop2.strValue = String.valueOf(nop2.doubleValue);
//    			res.value = String.format("%0.2s", nop2.strValue);
//    		}
//    		else if (nop1.doubleValue > nop1.doubleValue) // >
//    		{
//    			nop1.strValue = String.valueOf(nop1.doubleValue);
//    			res.value = String.format("%0.2s", nop1.strValue);
//    		}
//    		else if (nop1.doubleValue < nop1.doubleValue) // <
//    		{
//    			nop2.strValue = String.valueOf(nop2.doubleValue);
//    			res.value = String.format("%0.2s", nop2.strValue);
//    		}
//    		
//    		// ** ERROR ** IF ANOTHER CASE HERE.
//    	}
//    	// PRIM, INT
//    	else if (nop1.resval.structure == 15 && nop1.resval.type == 2) 
//    	{
//    		// Set type
//    		nop1.type = 2;
//    		nop2.type = 2;
//    		
//    		// Convert
//    		nop1.integerValue = Integer.parseInt(nop1.resval.value);
//    		nop2.integerValue = Integer.parseInt(nop2.resval.value);
//    		
//    		// Do compare on the two
//    		if (nop1.integerValue >= nop1.integerValue) // >=
//    		{
//    			nop1.strValue = String.valueOf(nop1.integerValue);
//    			res.value = String.format("%s", nop1.strValue);
//    		}
//    		else if (nop1.integerValue <= nop1.integerValue) // <=
//    		{
//    			nop2.strValue = String.valueOf(nop2.integerValue);
//    			res.value = String.format("%s", nop2.strValue);
//    		}
//    		else if (nop1.integerValue > nop1.integerValue) // >
//    		{
//    			nop1.strValue = String.valueOf(nop1.integerValue);
//    			res.value = String.format("%s", nop1.strValue);
//    		}
//    		else if (nop1.integerValue < nop1.integerValue) // <
//    		{
//    			nop2.strValue = String.valueOf(nop2.integerValue);
//    			res.value = String.format("%s", nop2.strValue);
//    		}
//    		
//    		// ** ERROR ** IF ANOTHER CASE HERE.
//    	}
//    	
//    	
//    	// Give the result to the caller.
//    	return res;
//    }
//    
//    // Binary Operators (==, !=) => Value is a Boolean
//    public static ResultValue equals(Parser parser, Numeric nop1, Numeric nop2)
//    {
//    	ResultValue res = new ResultValue();
//    	
//    	// Determine what type and structure nop1 and nop2 are.
//    	// PRIM, FLOAT
//    	if (nop1.resval.structure == 15 && nop1.resval.type == 3)
//    	{
//    		// Set type
//    		nop1.type = 3;
//    		nop2.type = 3;
//    		
//    		// Convert
//    		nop1.doubleValue = Double.parseDouble(nop1.resval.value);
//    		nop2.doubleValue = Double.parseDouble(nop2.resval.value);
//    		
//    		// Do compare on the two
//    		if (nop1.doubleValue == nop1.doubleValue) // ==
//    		{
//    			nop1.strValue = "T";
//    			res.value = String.format("%s", nop1.strValue);
//    		}
//    		else
//    		{
//    			nop2.strValue = "F"; // !=
//    			res.value = String.format("%s", nop2.strValue);
//    		}
//    		
//    		// ** ERROR ** IF ANOTHER CASE HERE.
//    	}
//    	// PRIM, INT
//    	else if (nop1.resval.structure == 15 && nop1.resval.type == 2) 
//    	{
//    		// Set type
//    		nop1.type = 2;
//    		nop2.type = 2;
//    		
//    		// Convert
//    		nop1.integerValue = Integer.parseInt(nop1.resval.value);
//    		nop2.integerValue = Integer.parseInt(nop2.resval.value);
//    		
//    		// Do compare on the two
//    		if (nop1.integerValue == nop1.integerValue) // ==
//    		{
//    			nop1.strValue = "T";
//    			res.value = String.format("%s", nop1.strValue);
//    		}
//    		else
//    		{
//    			nop2.strValue = "F"; // !=
//    			res.value = String.format("%s", nop2.strValue);
//    		}
//    		
//    		// ** ERROR ** IF ANOTHER CASE HERE.
//    	}
//    	
//    	return res;
//    }
    
    // Unary Operators
    
    // Compare and Converter methods
    public static Boolean compareType(int type, int type2)
    {
    	if (type == type2) 
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
    
    public static void coerceType(Numeric nop1, Numeric nop2) {
    	if (nop1.resval.type == Token.FLOAT)
		{
    		nop1.resval.structure = STIdentifier.PRIMITVE;
    		nop2.resval.structure = STIdentifier.PRIMITVE;
    		
			nop1.doubleValue = Double.parseDouble(nop1.resval.value);
			nop2.doubleValue = Double.parseDouble(nop2.resval.value);
			
			nop2.resval.type = Token.FLOAT;
		}
		else
		{
			nop1.integerValue = Integer.parseInt(nop1.resval.value);
			nop2.integerValue = Integer.parseInt(nop2.resval.value);
			
			nop2.resval.type = Token.INTEGER;
		}
    }
}
