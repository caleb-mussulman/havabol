package havabol;

/**
 * @desc Performs an operation on two numerics, stores the result as a ResultValue,
 * and returns it to whatever called it.
 * <p>
 * This is used for behind the scenes operations that are not apparent to the
 * programmer.
 * <p>
 * programmer makes a statement
 * if x < 5:
 *    ...
 * endif:
 * The < operation is what this method handles.
 *
 * @authors Taylor Brauer
 */
public class Utility 
{
	// Binary
	public static final int EQUAL              = 31;
	public static final int NOT_EQUAL          = 32;
	public static final int LESS_THAN          = 33;
	public static final int GREATER_THAN       = 34;
	public static final int LESS_THAN_EQUAL    = 35;
	public static final int GREATER_THAN_EQUAL = 36;
	
	// Unary
	public static final int NOT                = 41;
	public static final int AND                = 42;
	public static final int OR                 = 43;
	public static final int CONCAT             = 44;
	
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
    
    /**
     * Compares 2 result values' together based on a given operation.
     * @param parser - used for logging error messages.
     * @param operation - Binary operator (==, !=, <, >, <=, >=)
     * @param resval1 - Object containing result value 1
     * @param resval2 - Object containing result value 2
     * @return A true or false Havabol value (e.g. "T" or "F")
     * @throws Exception when a RS value fails to parse correctly.
     */
    public static String compare(Parser parser, int operation, ResultValue resval1, ResultValue resval2) throws Exception
    {    	
    	if (resval1.type != resval2.type)
    	{
    		// This is the new resval2
    		ResultValue res = coerceTypeRes(resval1.type, resval2);
    		String result = "";
    		
    		switch(operation) {
    		case EQUAL:    			
    			if (res.type == Token.STRING)
    			{
    				result = resval1.value.equals(res.value) ? "T" : "F";
    			}
    			else if (res.type == Token.INTEGER)
    			{
    				Integer iRes1 = Integer.parseInt(resval1.value);
    				Integer iRes2 = Integer.parseInt(res.value);
    				
    				result = (iRes1 == iRes2) ? "T" : "F";
    			}
    			else if (res.type == Token.FLOAT)
    			{
    				Double dRes1 = Double.parseDouble(resval1.value);
    				Double dRes2 = Double.parseDouble(res.value);
    				
    				result = (dRes1 == dRes2) ? "T" : "F";
    			}
    			break;
    		case NOT_EQUAL:
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
    			break;
    		case LESS_THAN:
    			if (res.type == Token.STRING)
    			{
    				int resCompare = resval1.value.compareTo(res.value);
    				result = (resCompare < 0) ? "T" : "F";
    			}
    			else if (res.type == Token.INTEGER)
    			{
    				Integer iRes1 = Integer.parseInt(resval1.value);
    				Integer iRes2 = Integer.parseInt(res.value);
    				
    				result = (iRes1 < iRes2) ? "T" : "F";
    			}
    			else if (res.type == Token.FLOAT)
    			{
    				Double dRes1 = Double.parseDouble(resval1.value);
    				Double dRes2 = Double.parseDouble(res.value);
    				
    				result = (dRes1 < dRes2) ? "T" : "F";
    			}
    			break;
    		case GREATER_THAN:
    			if (res.type == Token.STRING)
    			{
    				int resCompare = resval1.value.compareTo(res.value);
    				result = (resCompare > 0) ? "T" : "F";
    			}
    			else if (res.type == Token.INTEGER)
    			{
    				Integer iRes1 = Integer.parseInt(resval1.value);
    				Integer iRes2 = Integer.parseInt(res.value);
    				
    				result = (iRes1 > iRes2) ? "T" : "F";
    			}
    			else if (res.type == Token.FLOAT)
    			{
    				Double dRes1 = Double.parseDouble(resval1.value);
    				Double dRes2 = Double.parseDouble(res.value);
    				
    				result = (dRes1 > dRes2) ? "T" : "F";
    			}
    			break;
    		case LESS_THAN_EQUAL:
    			if (res.type == Token.STRING)
    			{
    				int resCompare = resval1.value.compareTo(res.value);
    				result = (resCompare <= 0) ? "T" : "F";
    			}
    			else if (res.type == Token.INTEGER)
    			{
    				Integer iRes1 = Integer.parseInt(resval1.value);
    				Integer iRes2 = Integer.parseInt(res.value);
    				
    				result = (iRes1 <= iRes2) ? "T" : "F";
    			}
    			else if (res.type == Token.FLOAT)
    			{
    				Double dRes1 = Double.parseDouble(resval1.value);
    				Double dRes2 = Double.parseDouble(res.value);
    				
    				result = (dRes1 <= dRes2) ? "T" : "F";
    			}
    			break;
    		case GREATER_THAN_EQUAL:
    			if (res.type == Token.STRING)
    			{
    				int resCompare = resval1.value.compareTo(res.value);
    				result = (resCompare >= 0) ? "T" : "F";
    			}
    			else if (res.type == Token.INTEGER)
    			{
    				Integer iRes1 = Integer.parseInt(resval1.value);
    				Integer iRes2 = Integer.parseInt(res.value);
    				
    				result = (iRes1 >= iRes2) ? "T" : "F";
    			}
    			else if (res.type == Token.FLOAT)
    			{
    				Double dRes1 = Double.parseDouble(resval1.value);
    				Double dRes2 = Double.parseDouble(res.value);
    				
    				result = (dRes1 >= dRes2) ? "T" : "F";
    			}
    			break;
    		case AND:
    			if (res.type == Token.STRING)
    			{
    				parser.errorWithCurrent("Cannot && %s and %s together", resval1.type, res.type);
    			}
    			else if (res.type == Token.INTEGER)
    			{
    				parser.errorWithCurrent("Cannot && %s and %s together", resval1.type, res.type);
    			}
    			else if (res.type == Token.FLOAT)
    			{
    				parser.errorWithCurrent("Cannot && %s and %s together", resval1.type, res.type);
    			}
    			else if (res.type == Token.BOOLEAN)
    			{
    				if (resval1.value.equals("T") && res.value.equals("T"))
    				{
    					return "T";
    				}
    				else
    				{
    					return "F";
    				}
    			}
    			break;
    		case OR:
    			if (res.type == Token.STRING)
    			{
    				parser.errorWithCurrent("Cannot || %s and %s together", resval1.type, res.type);
    			}
    			else if (res.type == Token.INTEGER)
    			{
    				parser.errorWithCurrent("Cannot || %s and %s together", resval1.type, res.type);
    			}
    			else if (res.type == Token.FLOAT)
    			{
    				parser.errorWithCurrent("Cannot || %s and %s together", resval1.type, res.type);
    			}
    			else if (res.type == Token.BOOLEAN)
    			{
    				if (resval1.value.equals("T") && res.value.equals("T"))
    				{
    					return "T";
    				}
    				else if (resval1.value.equals("F") && res.value.equals("T"))
    				{
    					return "T";
    				}
    				else if (resval1.value.equals("T") && res.value.equals("F"))
    				{
    					return "T";
    				}
    				else
    				{
    					return "F";
    				}
    			}
    			break;
    		case NOT:
    			if (res.type == Token.STRING)
    			{
    				parser.errorWithCurrent("Cannot negate %s", resval1.type);
    			}
    			else if (res.type == Token.INTEGER)
    			{
    				parser.errorWithCurrent("Cannot negate %s", resval1.type);
    			}
    			else if (res.type == Token.FLOAT)
    			{
    				parser.errorWithCurrent("Cannot negate %s", resval1.type);
    			}
    			else if (res.type == Token.BOOLEAN)
    			{
    				if (res.value.equals("T"))
    				{
    					return "F";
    				}
    				else if (res.value.equals("F"))
    				{
    					return "T";
    				}
    			}
    			break;
    		case CONCAT:
    			
    		}
    		return result;
    	}
    	else
    	{
    		parser.errorWithCurrent("Cannot convert %s to type %s", resval2.value, resval1.type);
    		return "";
    	}
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
