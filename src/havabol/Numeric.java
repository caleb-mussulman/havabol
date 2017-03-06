/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package havabol;

/**
 *
 * @author root
 */
public class Numeric {
	
	/*
	 * CLASS VARIABLES
	 */
	int integervalue;
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
	// Convert a result object into a Numeric object.
	
}
