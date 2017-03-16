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
		
		if (resval.type != Token.INTEGER && resval.type != Token.FLOAT) {
			parser.errorWithCurrent("%s is not a valid type.", Token.strSubClassifM[resval.type]);
		}
	}
}
