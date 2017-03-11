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
    
	// CONSTRUCTOR
	public Numeric(Parser aParser, ResultValue aResultValue, String aOperator, String aOperandDesc) {
		this.parser = aParser;
		this.resval = aResultValue;
		this.operator = aOperator;
		this.opndDesc = aOperandDesc;
	}
	
	/*
	 *  METHODS
	 */
	
}
