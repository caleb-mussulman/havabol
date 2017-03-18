package havabol;

/**
 * @desc Converts a ResultValue into a Numeric so that arithmetic operations
 * can be performed on the result.
 *
 * @authors Taylor Brauer
 */
public class Numeric {
	
	
	//CLASS VARIABLES
        int integerValue;
	double doubleValue;
	String strValue; // display value
	int type;        // INTEGER, FLOAT
	
	// constructor vars
	Parser parser;
	ResultValue resval;
	String operator;
	String opndDesc;
    
	/**
         * Initializes Numeric constructs
         * <p>
         * @param Parser       - Parser to handle errors
         * @param resultValue  - ResultValue for numbers value/type/...
         * @param operator     - String to hold 
         * @param operandDesc  - String to hold
         * @throws Exception   - Exception for Parser
         */
	public Numeric(Parser Parser, ResultValue resultValue, String operator, String operandDesc) throws Exception
	{
		this.parser = Parser;
		this.resval = resultValue;
		this.operator = operator;
		this.opndDesc = operandDesc;
                
                //ensure our result value types are valid primitives
		if (resval.type != Token.INTEGER && resval.type != Token.FLOAT) {
			parser.errorWithCurrent("%s is not a valid type.", Token.strSubClassifM[resval.type]);
		}
	}
}
