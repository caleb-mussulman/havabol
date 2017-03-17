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
    
	/**
         * Initalizes Numeric fields
         * <p>
         * @param aParser
         * @param aResultValue
         * @param aOperator
         * @param aOperandDesc
         * @throws Exception 
         */
	public Numeric(Parser Parser, ResultValue ResultValue, String Operator, String OperandDesc) throws Exception
	{
		this.parser = Parser;
		this.resval = ResultValue;
		this.operator = Operator;
		this.opndDesc = OperandDesc;
		
                //ensure our result value types are valid primitives
		if (resval.type != Token.INTEGER && resval.type != Token.FLOAT) {
			parser.errorWithCurrent("%s is not a valid type.", Token.strSubClassifM[resval.type]);
		}
	}
}
