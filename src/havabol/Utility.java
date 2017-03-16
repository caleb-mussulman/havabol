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
	public static final int EQUAL              = 31;
	public static final int NOT_EQUAL          = 32;
	public static final int LESS_THAN          = 33;
	public static final int GREATER_THAN       = 34;
	public static final int LESS_THAN_EQUAL    = 35;
	public static final int GREATER_THAN_EQUAL = 36;
	
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
    	coerceTypeNum(nop1, nop2);
    	
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
    	coerceTypeNum(nop1, nop2);
    	
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
    
    // Other operators.
    public static String compare(Parser parser, int operation, ResultValue resval1, ResultValue resval2) throws Exception
    {    	
    	if (resval1.type != resval2.type)
    	{
    		// This is the new resval2
    		ResultValue res = coerceTypeRes(resval1.type, resval2);
    		
    		switch(operation) {
    		case NOT_EQUAL:
    			String result = "";
    			
    			if (res.type == Token.STRING)
    			{
    				result = resval1.value.equals(res.value) ? "F" : "T";
    			}
    			else if (res.type == Token.INTEGER)
    			{
    				Integer iRes1 = Integer.parseInt(resval1.value);
    				Integer iRes2 = Integer.parseInt(res.value);
    				
    				result = (iRes1 == iRes2) ? "F" : "T";
    			}
    			else if (res.type == Token.FLOAT)
    			{
    				Double dRes1 = Double.parseDouble(resval1.value);
    				Double dRes2 = Double.parseDouble(res.value);
    				
    				result = (dRes1 == dRes2) ? "F" : "T";
    			}
    			return result;
    		}
    	}
    	else
    	{
    		parser.errorWithCurrent("Cannot convert %s to type %s", resval2.value, resval1.type);
    		return "";
    	}
    	return "";
    }
    
    // Convert for Numeric and ResultValue params.
    public static void coerceTypeNum(Numeric nop1, Numeric nop2) {
    	if (nop1.resval.type != nop2.resval.type) {
    		if (nop1.resval.type == Token.FLOAT)
    		{    
    			nop2.resval.type = Token.FLOAT;
    			nop1.doubleValue = Double.parseDouble(nop1.resval.value);
    			nop2.doubleValue = Double.parseDouble(nop2.resval.value);
    		}
    		else
    		{
    			nop2.resval.type  = Token.INTEGER;
    			nop1.integerValue = Integer.parseInt(nop1.resval.value);
    			nop2.integerValue = Integer.parseInt(nop2.resval.value);
    		}
    	}
    }
    
    public static ResultValue coerceTypeRes(int type, ResultValue resval2) {
    	ResultValue res = new ResultValue();
    	
    	if (type == Token.INTEGER)
    	{
    		int temp = Integer.parseInt(resval2.value); // need to capture this error message.
    		res.type = type;
    		res.value = String.valueOf(temp);
    		res.structure = resval2.structure;
    	}
    	else if (type == Token.FLOAT)
    	{
    		double temp = Double.parseDouble(resval2.value); // need to capture this error message.
    		res.type = type;
    		res.value = String.valueOf(temp);
    		res.structure = resval2.structure;
    	}
    	
    	return res;
    }
}
