package havabol;

/**
 * @desc Performs an operation on two numerics, stores the result as a ResultValue,
 * and returns it to whatever called it.
 *
 * @authors Taylor Brauer
 */
public class Utility {
	/*
	 * CLASS VARIABLES
	 */
	
	/*
	 *  CONSTRUCTOR
	 */
    public Utility() {
    	
    }
    
    /*
     * CLASS METHODS
     */
    // Takes Numeric params
    public static ResultValue subtract(Parser parser, Numeric nop1, Numeric nop2) {
    	ResultValue res = null;
    	
    	// Is a floating point value.
    	if (nop1.resval.value.matches("[+-]?([0-9]*[.])?[0-9]+")) {
    		nop1.type = 1;
    		nop2.type = 1;
    		
    		nop1.doubleValue = Double.parseDouble(nop1.resval.value);
    		nop2.doubleValue = Double.parseDouble(nop2.resval.value);
    		
    		Double tempValue = nop1.doubleValue - nop2.doubleValue;
    		
    		// The updated value gets stored in nop1 because it is of form -=
    		nop1.strValue = tempValue.toString();
    		
    		res.value = nop1.strValue.format("%0.2s", nop1.strValue);
    	// It is an integer value.
    	} else if (nop1.resval.value.matches("\\d+")) {
    		nop1.type = 0;
    		nop2.type = 0;
    		
    		nop1.integerValue = Integer.parseInt(nop1.resval.value);
    		nop2.integerValue = Integer.parseInt(nop2.resval.value);
    		
    		int subValue = nop1.integerValue - nop2.integerValue;
    		
    		// The updated value gets stored in nop1 because it is of form -=
    		nop1.strValue = String.valueOf(subValue);
    		
    		res.value = nop1.strValue.format("%0.2s", nop1.strValue);
    	}
    	
		return res;
    }
    public static ResultValue add(Parser parser, Numeric nop1, Numeric nop2) {
    	ResultValue res = null;
    	
    	// Is a floating point value.
    	if (nop1.resval.value.matches("[+-]?([0-9]*[.])?[0-9]+")) {
    		nop1.type = 1;
    		nop2.type = 1;
    		
    		nop1.doubleValue = Double.parseDouble(nop1.resval.value);
    		nop2.doubleValue = Double.parseDouble(nop2.resval.value);
    		
    		Double tempValue = nop1.doubleValue + nop2.doubleValue;
    		
    		// The updated value gets stored in nop1 because it is of form +=
    		nop1.strValue = tempValue.toString();
    		
    		res.value = nop1.strValue.format("%0.2s", nop1.strValue);
    	// It is an integer value.
    	} else if (nop1.resval.value.matches("\\d+")) {
    		nop1.type = 0;
    		nop2.type = 0;
    		
    		nop1.integerValue = Integer.parseInt(nop1.resval.value);
    		nop2.integerValue = Integer.parseInt(nop2.resval.value);
    		
    		int subValue = nop1.integerValue + nop2.integerValue;
    		
    		// The updated value gets stored in nop1 because it is of form +=
    		nop1.strValue = String.valueOf(subValue);
    		
    		res.value = nop1.strValue.format("%0.2s", nop1.strValue);
    	}
    	
		return res;
    }
    public static ResultValue mulitply(Parser parser, Numeric nop1, Numeric nop2) {
    	ResultValue res = null;
    	
    	// Is a floating point value.
    	if (nop1.resval.value.matches("[+-]?([0-9]*[.])?[0-9]+")) {
    		nop1.type = 1;
    		nop2.type = 1;
    		
    		nop1.doubleValue = Double.parseDouble(nop1.resval.value);
    		nop2.doubleValue = Double.parseDouble(nop2.resval.value);
    		
    		Double tempValue = nop1.doubleValue * nop2.doubleValue;
    		
    		// The updated value gets stored in nop1 because it is of form *=
    		nop1.strValue = tempValue.toString();
    		
    		res.value = nop1.strValue.format("%0.2s", nop1.strValue);
    	// It is an integer value.
    	} else if (nop1.resval.value.matches("\\d+")) {
    		nop1.type = 0;
    		nop2.type = 0;
    		
    		nop1.integerValue = Integer.parseInt(nop1.resval.value);
    		nop2.integerValue = Integer.parseInt(nop2.resval.value);
    		
    		int subValue = nop1.integerValue * nop2.integerValue;
    		
    		// The updated value gets stored in nop1 because it is of form *=
    		nop1.strValue = String.valueOf(subValue);
    		
    		res.value = nop1.strValue.format("%0.2s", nop1.strValue);
    	}
    	
		return res;
    }
    public static ResultValue divide(Parser parser, Numeric nop1, Numeric nop2) {
    	ResultValue res = null;
    	
    	// Is a floating point value.
    	if (nop1.resval.value.matches("[+-]?([0-9]*[.])?[0-9]+")) {
    		nop1.type = 1;
    		nop2.type = 1;
    		
    		nop1.doubleValue = Double.parseDouble(nop1.resval.value);
    		nop2.doubleValue = Double.parseDouble(nop2.resval.value);
    		
    		Double tempValue = nop1.doubleValue / nop2.doubleValue;
    		
    		// The updated value gets stored in nop1 because it is of form /=
    		nop1.strValue = tempValue.toString();
    		
    		res.value = nop1.strValue.format("%0.2s", nop1.strValue);
    	// It is an integer value.
    	} else if (nop1.resval.value.matches("\\d+")) {
    		nop1.type = 0;
    		nop2.type = 0;
    		
    		nop1.integerValue = Integer.parseInt(nop1.resval.value);
    		nop2.integerValue = Integer.parseInt(nop2.resval.value);
    		
    		int subValue = nop1.integerValue / nop2.integerValue;
    		
    		// The updated value gets stored in nop1 because it is of form /=
    		nop1.strValue = String.valueOf(subValue);
    		
    		res.value = nop1.strValue.format("%0.2s", nop1.strValue);
    	}
    	
		return res;
    }
    
    /**
     * These may not be used.
     */
    // Takes ResultValue params
//    public ResultValue subtract(Parser parser, ResultValue resval1, ResultValue resval2) {
//    	ResultValue res = null;
//    	
//		return res;
//    }
//    public ResultValue add(Parser parser, ResultValue resval1, ResultValue resval2) {
//    	ResultValue res = null;
//    	
//		return res;
//    }
//    public ResultValue mulitply(Parser parser, ResultValue resval1, ResultValue resval2) {
//    	ResultValue res = null;
//    	
//		return res;
//    }
//    public ResultValue divide(Parser parser, ResultValue resval1, ResultValue resval2) {
//    	ResultValue res = null;
//    	
//		return res;
//    }
}
