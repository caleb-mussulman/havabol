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
    		Float n1 = Float.parseFloat(nop1.resval.value);
    		Float n2 = Float.parseFloat(nop2.resval.value);
    		
    		Float subValue = n1 - n2;
    		
    		String valString = subValue.toString();
    		res.value = valString.format("%0.2s", valString);
    	// It is an integer value.
    	} else if (nop1.resval.value.matches("\\d+")) {
    		int n1 = Integer.parseInt(nop1.resval.value);
    		int n2 = Integer.parseInt(nop2.resval.value);
    		
    		int subValue = n1 - n2;
    		
    		String valString = String.valueOf(subValue);
    		res.value = valString.format("%0.2s", valString);
    	}
    	
		return res;
    }
    public static ResultValue add(Parser parser, Numeric nop1, Numeric nop2) {
    	ResultValue res = null;
    	
    	// Is a floating point value.
    	if (nop1.resval.value.matches("[+-]?([0-9]*[.])?[0-9]+")) {
    		Float n1 = Float.parseFloat(nop1.resval.value);
    		Float n2 = Float.parseFloat(nop2.resval.value);
    		
    		Float subValue = n1 + n2;
    		
    		String valString = subValue.toString();
    		res.value = valString.format("%0.2s", valString);
    	// It is an integer value.
    	} else if (nop1.resval.value.matches("\\d+")) {
    		int n1 = Integer.parseInt(nop1.resval.value);
    		int n2 = Integer.parseInt(nop2.resval.value);
    		
    		int subValue = n1 + n2;
    		
    		String valString = String.valueOf(subValue);
    		res.value = valString.format("%0.2s", valString);
    	}
    	
		return res;
    }
    public static ResultValue mulitply(Parser parser, Numeric nop1, Numeric nop2) {
    	ResultValue res = null;
    	
    	// Is a floating point value.
    	if (nop1.resval.value.matches("[+-]?([0-9]*[.])?[0-9]+")) {
    		Float n1 = Float.parseFloat(nop1.resval.value);
    		Float n2 = Float.parseFloat(nop2.resval.value);
    		
    		Float subValue = n1 * n2;
    		
    		String valString = subValue.toString();
    		res.value = valString.format("%0.2s", valString);
    	// It is an integer value.
    	} else if (nop1.resval.value.matches("\\d+")) {
    		int n1 = Integer.parseInt(nop1.resval.value);
    		int n2 = Integer.parseInt(nop2.resval.value);
    		
    		int subValue = n1 * n2;
    		
    		String valString = String.valueOf(subValue);
    		res.value = valString.format("%0.2s", valString);
    	}
    	
		return res;
    }
    public static ResultValue divide(Parser parser, Numeric nop1, Numeric nop2) {
    	ResultValue res = null;
    	
    	// Is a floating point value.
    	if (nop1.resval.value.matches("[+-]?([0-9]*[.])?[0-9]+")) {
    		Float n1 = Float.parseFloat(nop1.resval.value);
    		Float n2 = Float.parseFloat(nop2.resval.value);
    		
    		Float subValue = n1 / n2;
    		
    		String valString = subValue.toString();
    		res.value = valString.format("%0.2s", valString);
    	// It is an integer value.
    	} else if (nop1.resval.value.matches("\\d+")) {
    		int n1 = Integer.parseInt(nop1.resval.value);
    		int n2 = Integer.parseInt(nop2.resval.value);
    		
    		int subValue = n1 / n2;
    		
    		String valString = String.valueOf(subValue);
    		res.value = valString.format("%0.2s", valString);
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
