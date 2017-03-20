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
    
	/**
    * Initializes Numeric constructs
    * <p>
    * @param  parser              - Parser to handle errors
    * @param  resultValue         - The ResultValue to be turned into a Numeric
    * @param  operator            - String to hold the operator that called Numeric
    * @param  operandDescription  - String to hold a description of which operand
    * @throws Exception           - If the result value is not of type INTEGER OR FLOAT
    *                             - If the string value could not be parsed as a Java int or double
    */
	public Numeric(Parser parser, ResultValue resultValue, String operator, String operandDescription) throws Exception
	{
	    // Catch any int/double parsing error
		try
		{
		    // Check the type of the result value
		    switch(resultValue.type)
		    {
		        case Token.INTEGER:
		            this.integerValue = Integer.parseInt(resultValue.value);
		            this.strValue = resultValue.value;
		            this.type = resultValue.type;
		            break;
		        case Token.FLOAT:
		            this.doubleValue = Double.parseDouble(resultValue.value);
                    this.strValue = resultValue.value;
                    this.type = resultValue.type;
		            break;
		        default:
		            // Can not create a numeric out of something that is not an INTEGER OR FLOAT
		            parser.errorWithCurrent("%s of operation '%s' has type '%s' and value '%s', must have type 'INTEGER' or 'FLOAT'"
                                            , operandDescription, operator, Token.getType(parser, resultValue.type), resultValue.value);
		    }
		}
		// If there was any error parsing to get the int/double
		// This should only happen if we implemented something incorrectly
		catch(NumberFormatException e)
		{
		    parser.errorWithCurrent("Could not parse '%s' into INTEGER/FLOAT", resultValue.value);
		}
	}
}
