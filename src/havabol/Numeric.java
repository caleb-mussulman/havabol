package havabol;

/**
 * @desc Converts a ResultValue into a Numeric so that arithmetic operations
 * can be performed on the result.
 *
 * @authors Taylor Brauer
 */
public class Numeric {
	
	/*
	 * CLASS VARIABLES
	 */
    int integerValue;
	double doubleValue;
	String strValue; // display value
	int type; // INTEGER, FLOAT
	
	// CONSTANTS FOR NUMERIC
	/*
	 * Type: (same as in token classifications.
	 * INTEGER = 2
	 * FLOAT   = 3
	 */
	
	// constructor vars
	Parser parser;
	ResultValue resval;
	String operator;
	String opndDesc;
    
	/*
	 * CONSTRUCTOR
	 */
	public Numeric(Parser aParser, ResultValue aResultValue, String aOperator, String aOperandDesc) throws Exception
	{
		this.parser = aParser;
		this.resval = aResultValue;
		this.operator = aOperator;
		this.opndDesc = aOperandDesc;
		
		if (! isValidNumeric(resval.type)) {
			parser.errorWithCurrent("%s is not a valid type.", resval.type);
		}
	}
	
	/*
	 *  METHODS
	 */
	public static Boolean isValidNumeric(int type)
	{
		if (type == Token.INTEGER || type == Token.FLOAT)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
