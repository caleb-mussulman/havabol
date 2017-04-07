package havabol;

import java.util.*;

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
    /*
    * LOGICAL OPERATIONS
    */
    // Binary
    public static final int EQUAL              = 31;
    public static final int NOT_EQUAL          = 32;
    public static final int LESS_THAN          = 33;
    public static final int GREATER_THAN       = 34;
    public static final int LESS_THAN_EQUAL    = 35;
    public static final int GREATER_THAN_EQUAL = 36;
    public static final int AND                = 37;
    public static final int OR                 = 38;
    
    private static Set<String> dates = new HashSet<String>();
    static {
        for (int year = 0000; year < 9999; year++) {
            for (int month = 1; month <= 12; month++) {
                for (int day = 1; day <= daysInMonth(year, month); day++) {
                    StringBuilder date = new StringBuilder();
                    date.append(String.format("%04d", year));
                    date.append("-");
                    date.append(String.format("%02d", month));
                    date.append("-");
                    date.append(String.format("%02d", day));
                    dates.add(date.toString());
                }
            }
        }
    }
    
    @SuppressWarnings("serial")
    public final static Map<Integer, String> logicalOperator = Collections.unmodifiableMap(new HashMap<Integer, String>(){{
                                                               put(31, "=="); put(32, "!="); put(33, "<"); put(34, ">");
                                                               put(35, "<="); put(36, ">="); put(37, "and"); put(38, "or"); }});
    
    /*
    * CLASS METHODS -- needs to deal with coercion.
    */
    // Assignment Operators (+=, -=, *=, /=)
    public static ResultValue subtract(Parser parser, ResultValue resParam1, ResultValue resParam2, String operationCalledFrom) throws Exception
    {
        // Must get a copy of the passed in result values so that the
        // original result value objects are not manipulated
        ResultValue resOp1 = Utility.getResultValueCopy(resParam1);
        ResultValue resOp2 = Utility.getResultValueCopy(resParam2);
        
        ResultValue res = new ResultValue();
        Numeric nOp1 = new Numeric(parser, resOp1, operationCalledFrom, "1st operand");
        Numeric nOp2;
        
        // Compare the two values' type and coerce if needed
        if(resOp1.type != resOp2.type)
        {
            coerce(parser, resOp1.type, resOp2, operationCalledFrom);
        }
        
        // Get the second result value as a numeric
        nOp2 = new Numeric(parser, resOp2, operationCalledFrom, "2nd operand");
        
        // Is a floating point value.
        if (nOp1.type == Token.FLOAT)
        {
            Double tempValue = nOp1.doubleValue - nOp2.doubleValue;
            
            // Store the new value of operation
            res.value = tempValue.toString();
            res.type  = nOp1.type;
            res.structure = STIdentifier.PRIMITVE;
        }
        // It is an integer value.
        else if (nOp1.type == Token.INTEGER)
        {
            Integer tempValue = nOp1.integerValue - nOp2.integerValue;
            
            // Store the new value of operation
            res.value = tempValue.toString();
            res.type  = nOp1.type;
            res.structure = STIdentifier.PRIMITVE;
        }
        // Operation not defined for given type
        else
        {
            parser.errorWithCurrent("The operation '%s' is not defined for the type '%s'"
                                    , operationCalledFrom, Token.getType(parser, resOp1.type));
        }
        return res;
    }
    
    public static ResultValue add(Parser parser, ResultValue resParam1, ResultValue resParam2, String operationCalledFrom) throws Exception
    {
        // Must get a copy of the passed in result values so that the
        // original result value objects are not manipulated
        ResultValue resOp1 = Utility.getResultValueCopy(resParam1);
        ResultValue resOp2 = Utility.getResultValueCopy(resParam2);
        
        ResultValue res = new ResultValue();
        Numeric nOp1 = new Numeric(parser, resOp1, operationCalledFrom, "1st operand");
        Numeric nOp2;
        
        // Compare the two values' type and coerce if needed
        if(resOp1.type != resOp2.type)
        {
            coerce(parser, resOp1.type, resOp2, operationCalledFrom);
        }
        
        // Get the second result value as a numeric
        nOp2 = new Numeric(parser, resOp2, operationCalledFrom, "2nd operand");
        
        // Is a floating point value.
        if (nOp1.type == Token.FLOAT)
        {
            Double tempValue = nOp1.doubleValue + nOp2.doubleValue;
            
            // Store the new value of the operation
            res.value = tempValue.toString();
            res.type  = nOp1.type;
            res.structure = STIdentifier.PRIMITVE;
        }
        // It is an integer value.
        else if (nOp1.type == Token.INTEGER)
        {
            Integer tempValue = nOp1.integerValue + nOp2.integerValue;
            
            // Store the new value of the operation
            res.value = tempValue.toString();
            res.type  = nOp1.type;
            res.structure = STIdentifier.PRIMITVE;
        }
        // Operation not defined for given type
        else
        {
            parser.errorWithCurrent("The operation '%s' is not defined for the type '%s'"
                                    , operationCalledFrom, Token.getType(parser, resOp1.type));
        }
        return res;
    }
    
    public static ResultValue multiply(Parser parser, ResultValue resParam1, ResultValue resParam2) throws Exception
    {
        // Must get a copy of the passed in result values so that the
        // original result value objects are not manipulated
        ResultValue resOp1 = Utility.getResultValueCopy(resParam1);
        ResultValue resOp2 = Utility.getResultValueCopy(resParam2);
        
        ResultValue res = new ResultValue();
        Numeric nOp1 = new Numeric(parser, resOp1, "*", "1st operand");
        Numeric nOp2;
        
        // Compare the two values' type and coerce if needed
        if(resOp1.type != resOp2.type)
        {
            coerce(parser, resOp1.type, resOp2, "*");
        }
        
        // Get the second result value as a numeric
        nOp2 = new Numeric(parser, resOp2, "*", "2nd operand");
        
        // Is a floating point value.
        if (nOp1.type == Token.FLOAT)
        {
            Double tempValue = nOp1.doubleValue * nOp2.doubleValue;
            
            // Store the new value of the operation
            res.value = tempValue.toString();
            res.type  = nOp1.type;
            res.structure = STIdentifier.PRIMITVE;
        }
        // It is an integer value.
        else if (nOp1.type == Token.INTEGER)
        {
            Integer tempValue = nOp1.integerValue * nOp2.integerValue;
            
            // Store the new value of the operation
            res.value = tempValue.toString();
            res.type  = nOp1.type;
            res.structure = STIdentifier.PRIMITVE;
        }
        // Operation not defined for given type
        else
        {
            parser.errorWithCurrent("The operation '%s' is not defined for the type '%s'"
                                    , "*", Token.getType(parser, resOp1.type));
        }
        return res;
    }
    
    public static ResultValue divide(Parser parser, ResultValue resParam1, ResultValue resParam2) throws Exception
    {
        // Must get a copy of the passed in result values so that the
        // original result value objects are not manipulated
        ResultValue resOp1 = Utility.getResultValueCopy(resParam1);
        ResultValue resOp2 = Utility.getResultValueCopy(resParam2);
        
        ResultValue res = new ResultValue();
        Numeric nOp1 = new Numeric(parser, resOp1, "/", "1st operand");
        Numeric nOp2;
        
        // Compare the two values' type and coerce if needed
        if(resOp1.type != resOp2.type)
        {
            coerce(parser, resOp1.type, resOp2, "/");
        }
        
        // Get the second result value as a numeric
        nOp2 = new Numeric(parser, resOp2, "/", "2nd operand");
        
        // Is a floating point value.
        if (nOp1.type == Token.FLOAT)
        {
            // Can not divide by 0.0
            if(nOp2.doubleValue == 0.0)
            {
                parser.errorWithCurrent("Attempted to divide by zero");
            }
            
            Double tempValue = nOp1.doubleValue / nOp2.doubleValue;
            
            // Store the new value of the operation
            res.value = tempValue.toString();
            res.type  = nOp1.type;
            res.structure = STIdentifier.PRIMITVE;
        }
        // It is an integer value.
        else if (nOp1.type == Token.INTEGER)
        {
            // Can not divide by 0
            if(nOp2.integerValue == 0)
            {
                parser.errorWithCurrent("Attempted to divide by zero");
            }
            
            Integer tempValue = nOp1.integerValue / nOp2.integerValue;
            
            // Store the new value of the operation
            res.value = tempValue.toString();
            res.type  = nOp1.type;
            res.structure = STIdentifier.PRIMITVE;
        }
        // Operation not defined for given type
        else
        {
            parser.errorWithCurrent("The operation '%s' is not defined for the type '%s'"
                                    , "*", Token.getType(parser, resOp1.type));
        }
        return res;
    }
    
    /**
     * Compares 2 result values' together based on a given operation.
     * <p>
     * @param parser - used for logging error messages.
     * @param operation - Binary operator (==, !=, <, >, <=, >=)
     * @param resParam1 - Object containing result value 1
     * @param resParam2 - Object containing result value 2
     * @return A true or false Havabol value (e.g. "T" or "F")
     * @throws Exception when a RS value fails to parse correctly.
     */
    public static ResultValue compare(Parser parser, int operation, ResultValue resParam1, ResultValue resParam2) throws Exception
    {
        // Must get a copy of the passed in result values so that the
        // original result value objects are not manipulated
        ResultValue resval1 = Utility.getResultValueCopy(resParam1);
        ResultValue resval2 = Utility.getResultValueCopy(resParam2);
        
        // If the result values are numbers, they must be converted to numerics
        // Must initialize with dummy values in order to suppress Java error messages
        // that these variables may not have been initialized
        ResultValue tempRes = new ResultValue();
        tempRes.type = Token.INTEGER;
        tempRes.value = "0";
        Numeric nOp1 = new Numeric(parser, tempRes, logicalOperator.get(operation), "temp initialization");
        Numeric nOp2 = new Numeric(parser, tempRes, logicalOperator.get(operation), "temp initialization");
        
        // Coerce the second type to the first if they are not equal
        if (resval1.type != resval2.type)
        {
            coerce(parser, resval1.type, resval2, logicalOperator.get(operation));
        }
        
        // Types must be the same now, so if they are numerics, convert to Numeric object
        if((resval1.type == Token.INTEGER) || (resval1.type == Token.FLOAT))
        {
            nOp1 = new Numeric(parser, resval1, logicalOperator.get(operation), "1st operand");
            nOp2 = new Numeric(parser, resval2, logicalOperator.get(operation), "2nd operand");
        }
        
        String result = "";
        
        // Determine the passed in comparison operation
        switch(operation) {
            case EQUAL:
                if ((resval2.type == Token.STRING) || (resval2.type == Token.BOOLEAN))
                {
                    result = resval1.value.equals(resval2.value) ? "T" : "F";
                }
                else if (resval2.type == Token.INTEGER)
                {
                    result = (nOp1.integerValue == nOp2.integerValue) ? "T" : "F";
                }
                else if (resval2.type == Token.FLOAT)
                {
                    result = (nOp1.doubleValue == nOp2.doubleValue) ? "T" : "F";
                }
                // The operation is not defined for the given type
                else
                {
                    parser.errorWithCurrent("The operation '%s' is not defined for the type '%s'"
                                            , logicalOperator.get(operation), Token.getType(parser, resval1.type));
                }
                break;
            case NOT_EQUAL:
                if ((resval2.type == Token.STRING) || (resval2.type == Token.BOOLEAN))
                {
                    result = resval1.value.equals(resval2.value) ? "F" : "T";
                }
                else if (resval2.type == Token.INTEGER)
                {
                    result = (nOp1.integerValue == nOp2.integerValue) ? "F" : "T";
                }
                else if (resval2.type == Token.FLOAT)
                {
                    result = (nOp1.doubleValue == nOp2.doubleValue) ? "F" : "T";
                }
                // The operation is not defined for the given type
                else
                {
                    parser.errorWithCurrent("The operation '%s' is not defined for the type '%s'"
                                            , logicalOperator.get(operation), Token.getType(parser, resval1.type));
                }
                break;
            case LESS_THAN:
                if (resval2.type == Token.STRING)
                {
                    int resCompare = resval1.value.compareTo(resval2.value);
                    result = (resCompare < 0) ? "T" : "F";
                }
                else if (resval2.type == Token.INTEGER)
                {
                    result = (nOp1.integerValue < nOp2.integerValue) ? "T" : "F";
                }
                else if (resval2.type == Token.FLOAT)
                {
                    result = (nOp1.doubleValue < nOp2.doubleValue) ? "T" : "F";
                }
                // The operation is not defined for the given type
                else
                {
                    parser.errorWithCurrent("The operation '%s' is not defined for the type '%s'"
                                            , logicalOperator.get(operation), Token.getType(parser, resval1.type));
                }
                break;
            case GREATER_THAN:
                if (resval2.type == Token.STRING)
                {
                    int resCompare = resval1.value.compareTo(resval2.value);
                    result = (resCompare > 0) ? "T" : "F";
                }
                else if (resval2.type == Token.INTEGER)
                {
                    result = (nOp1.integerValue > nOp2.integerValue) ? "T" : "F";
                }
                else if (resval2.type == Token.FLOAT)
                {
                    result = (nOp1.doubleValue > nOp2.doubleValue) ? "T" : "F";
                }
                // The operation is not defined for the given type
                else
                {
                    parser.errorWithCurrent("The operation '%s' is not defined for the type '%s'"
                                            , logicalOperator.get(operation), Token.getType(parser, resval1.type));
                }
                break;
            case LESS_THAN_EQUAL:
                if (resval2.type == Token.STRING)
                {
                    int resCompare = resval1.value.compareTo(resval2.value);
                    result = (resCompare <= 0) ? "T" : "F";
                }
                else if (resval2.type == Token.INTEGER)
                {
                    result = (nOp1.integerValue <= nOp2.integerValue) ? "T" : "F";
                }
                else if (resval2.type == Token.FLOAT)
                {
                    result = (nOp1.doubleValue <= nOp2.doubleValue) ? "T" : "F";
                }
                // The operation is not defined for the given type
                else
                {
                    parser.errorWithCurrent("The operation '%s' is not defined for the type '%s'"
                                            , logicalOperator.get(operation), Token.getType(parser, resval1.type));
                }
                break;
            case GREATER_THAN_EQUAL:
                if (resval2.type == Token.STRING)
                {
                    int resCompare = resval1.value.compareTo(resval2.value);
                    result = (resCompare >= 0) ? "T" : "F";
                }
                else if (resval2.type == Token.INTEGER)
                {
                    result = (nOp1.integerValue >= nOp2.integerValue) ? "T" : "F";
                }
                else if (resval2.type == Token.FLOAT)
                {
                    result = (nOp1.doubleValue >= nOp2.doubleValue) ? "T" : "F";
                }
                // The operation is not defined for the given type
                else
                {
                    parser.errorWithCurrent("The operation '%s' is not defined for the type '%s'"
                                            , logicalOperator.get(operation), Token.getType(parser, resval1.type));
                }
                break;
            case AND:
                if (resval2.type == Token.STRING)
                {
                    parser.errorWithCurrent("Cannot && %s and %s together", resval1.type, resval2.type);
                }
                else if (resval2.type == Token.INTEGER)
                {
                    parser.errorWithCurrent("Cannot && %s and %s together", resval1.type, resval2.type);
                }
                else if (resval2.type == Token.FLOAT)
                {
                    parser.errorWithCurrent("Cannot && %s and %s together", resval1.type, resval2.type);
                }
                else if (resval2.type == Token.BOOLEAN)
                {
                    if (resval1.value.equals("T") && resval2.value.equals("T"))
                    {
                        result = "T";
                    }
                    else
                    {
                        result = "F";
                    }
                }
                // The operation is not defined for the given type
                else
                {
                    parser.errorWithCurrent("The operation '%s' is not defined for the type '%s'"
                                            , logicalOperator.get(operation), Token.getType(parser, resval1.type));
                }
                break;
            case OR:
                if (resval2.type == Token.STRING)
                {
                    parser.errorWithCurrent("Cannot || %s and %s together", resval1.type, resval2.type);
                }
                else if (resval2.type == Token.INTEGER)
                {
                    parser.errorWithCurrent("Cannot || %s and %s together", resval1.type, resval2.type);
                }
                else if (resval2.type == Token.FLOAT)
                {
                    parser.errorWithCurrent("Cannot || %s and %s together", resval1.type, resval2.type);
                }
                else if (resval2.type == Token.BOOLEAN)
                {
                    if (resval1.value.equals("F") && resval2.value.equals("F"))
                    {
                        result = "F";
                    }
                    else
                    {
                        result = "T";
                    }
                }
                // The operation is not defined for the giveen type
                else
                {
                    parser.errorWithCurrent("The operation '%s' is not defined for the type '%s'"
                                            , logicalOperator.get(operation), Token.getType(parser, resval1.type));
                }
                break;
            // The given logical operator constant is not valid
            default:
                parser.error("Called method 'compare' with invalid operator constant '%d'", operation);
        }
        
        // Check to ensure the boolean result was initialized, in case we missed some logic
        if(result.equals(""))
        {
            parser.errorWithCurrent("The boolean result for operation '%s' was not evaluated properly", logicalOperator.get(operation));
        }
        
        // Create a ResultValue object to hold the boolean value to return
        ResultValue resReturn = new ResultValue();
        resReturn.value = result;
        resReturn.type = Token.BOOLEAN;
        resReturn.structure = STIdentifier.PRIMITVE;
        
        return resReturn;
    }
    
    public static ResultValue not(Parser parser, ResultValue resParam) throws Exception
    {
        // Must get a copy of the passed in result value so that the
        // original result value object is not manipulated
        ResultValue resval = Utility.getResultValueCopy(resParam);
        
        // TODO may be able to coerce a string to a boolean first (need to talk to Clark)
        // Can only perform the operation on a boolean value
        if(resval.type != Token.BOOLEAN)
        {
            parser.errorWithCurrent("The operation 'not' is not defined for the type '%s'", Token.getType(parser, resval.type));
        }
        
        // Reverse the value of the boolean
        if (resval.value.equals("T"))
        {
            resval.value = "F";
        }
        else
        {
            resval.value = "T";
        }
        
        return resval;
    }
    
    public static ResultValue concat(Parser parser, ResultValue resParam1, ResultValue resParam2) throws Exception
    {
        // Must get a copy of the passed in result values so that the
        // original result value objects are not manipulated
        ResultValue resval1 = Utility.getResultValueCopy(resParam1);
        ResultValue resval2 = Utility.getResultValueCopy(resParam2);
        ResultValue resReturn = new ResultValue();
        
        Utility.coerce(parser, Token.STRING, resval1, "#");   
        Utility.coerce(parser, Token.STRING, resval2, "#");
        
        resReturn.value = resval1.value + resval2.value;
        resReturn.type = Token.STRING;
        resReturn.structure = STIdentifier.PRIMITVE;
        
        return resReturn;
    }
    
    public static ResultValue uminus(Parser parser, ResultValue resParam) throws Exception
    {
        // Must get a copy of the passed in result value so that the
        // original result value object is not manipulated
        ResultValue resval = Utility.getResultValueCopy(resParam);
        
        Numeric nOp = new Numeric(parser, resval, "u-", "operand");
        
        if (nOp.type == Token.INTEGER)
        {
            resval.value = String.valueOf(nOp.integerValue *= -1);
            resval.type = Token.INTEGER;
        }
        else if (nOp.type == Token.FLOAT)
        {
            resval.value = String.valueOf(nOp.doubleValue *= -1);
            resval.type = Token.FLOAT;
        }
        return resval;
    }
    
    public static ResultValue exponent(Parser parser, ResultValue resParam1, ResultValue resParam2) throws Exception
    {
        // Must get a copy of the passed in result values so that the
        // original result value objects are not manipulated
        ResultValue resval1 = Utility.getResultValueCopy(resParam1);
        ResultValue resval2 = Utility.getResultValueCopy(resParam2);
        
        String result = "";
        
        if (resval1.type == Token.STRING)
        {
            parser.errorWithCurrent("Cannot perform exponentiation on %s", resval1.type);
        }
        else if (resval1.type == Token.INTEGER)
        {
            Numeric nOp1 = new Numeric(parser, resval1, "^", "1st operand");
            Numeric nOp2 = new Numeric(parser, resval2, "^", "2nd operand");
            
            Double exponVal = Math.pow(nOp1.integerValue, nOp2.integerValue);
            result = String.valueOf(exponVal.intValue());
        }
        else if (resval1.type == Token.FLOAT)
        {
            Numeric nOp1 = new Numeric(parser, resval1, "^", "1st operand");
            Numeric nOp2 = new Numeric(parser, resval2, "^", "2nd operand");
            
            Double exponVal = Math.pow(nOp1.doubleValue, nOp2.doubleValue);
            result = String.valueOf(exponVal);
        }
        else if (resval1.type == Token.BOOLEAN)
        {
            parser.errorWithCurrent("Cannot perform exponentiation on %s", resval1.type);
        }
        else
        {
            parser.errorWithCurrent("Taylor didn't add another case for uminus.");
        }
        
        resval1.value = result;
        return resval1;
    }
    
    /**
     * Parses an input date to see if it is of form yyyy-mm-dd
     * mm and dd must have a 0 in front of them if it is a single integer date.
     * @param date
     * @return
     */
    public static boolean isValidDate(String date)
    {
        if (date.length() != 10)
        {
            return false;
        }
        
        if (date.matches("\\d{4}-\\d{2}-\\d{2}"))
        {
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(5,7));
            if (month > 12 || month == 0)
            {
                return false;
            }
            int daysMonth = daysInMonth(year, month);
            int day = Integer.parseInt(date.substring(8, 10));
            if (day == 0 || day > daysMonth)
            {
                return false;
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     ******************************** START OF FUNCTIONS ***************************************************
     */
    
    /**
     * This takes in a string, creates a new Result Value, gets the size of the value in the passed in
     * ResultValue and assigns that value to the newly created Result Value as an Integer.
     * @param parser - Used for sending error messages to the programmer.
     * @param string - String that the programmer wants to get a count on.
     * @return A result value that contains information about the length of the string that was passed in.
     * @throws Exception
     */
    public static ResultValue LEN(Parser parser, ResultValue string) throws Exception
    {
        ResultValue stringresval = new ResultValue();
        
        if (stringresval.type != Token.STRING)
        {
            parser.errorWithCurrent("Cannot get length of type %s. Must be type string.", stringresval.type);
        }
        
        int len = string.value.length();
        stringresval.value = String.valueOf(len);
        stringresval.type = Token.INTEGER;
        stringresval.structure = STIdentifier.PRIMITVE;
        
        return stringresval;
    }
    
    /**
     * This takes in a string, creates a new Result Value, checks to see if the string is empty or matches
     * to some white space, and returns true is during its check it does not encounter a non white space
     * character.
     * @param parser - Used for sending error messages to the programmer.
     * @param string - String that the programmer wants to check for spaces or empty.
     * @return Result value that contains T or F indicating if the string has spaces or is empty.
     * @throws Exception
     */
    public static ResultValue SPACES(Parser parser, ResultValue string) throws Exception
    {
        ResultValue stringresval = new ResultValue();
        
        if (stringresval.type != Token.STRING)
        {
            parser.errorWithCurrent("Cannot make a check for spaces on type %s", stringresval.type);
        }
        
        if (stringresval.value.isEmpty() || stringresval.value.matches("\\s"))
        {
            stringresval.value = "T";
            stringresval.type = Token.BOOLEAN;
            stringresval.structure = STIdentifier.PRIMITVE;
        }
        else
        {
            stringresval.value = "F";
            stringresval.type = Token.BOOLEAN;
            stringresval.structure = STIdentifier.PRIMITVE;
        }
        
        return stringresval;
    }
    
    /**
     * TODO: Cover this with Caleb
     * Havabol built-in function:
     * ELEM - short for ELEMENTS
     * FIXED or UNBOUNDED
     * <p>
     * Returning a ResultValue that holds the number of elements in the Havabol Array
     * @param resultArray
     * @return - The number of elements in the array
     */
    public static ResultValue ELEM(Parser errParse, ResultArray resultArray)
    {
        int tmp;

        //Create a new resultValue to return and initialize its attributes
        ResultValue resultValue = new ResultValue();
        resultValue.type = Token.INTEGER;   //This will always be an integer.
        resultValue.structure = STIdentifier.PRIMITVE;
        resultValue.terminatingStr = "";

        //The highest populated subscript + 1, in ArrayList's is simply the what the .size() function returns.
        tmp = resultArray.valueList.size();     //Returns the number of Elements in the array.

        resultValue.value = String.valueOf(tmp); //Converts integer value to a string.

        return resultValue;
    }

    /**
     * TODO: Cover this with Caleb
     * @param errParse
     * @param resultArray
     * @return
     */
    public static ResultValue MAXELEM(Parser errParse, ResultArray resultArray) throws ParserException {
        int tmp;

        //TODO: No access to the resultArray's "symbol" -- Store in value of ResultArray's?
        if(resultArray.structure == STIdentifier.UNBOUNDED_ARRAY){
            errParse.error("Array is of structure Unbounded, can not resolve MAXELEM");
        }

        //Create a new resultValue to return and initalize its attributes.
        ResultValue resultValue = new ResultValue();
        resultValue.type = Token.INTEGER;
        resultValue.structure = STIdentifier.PRIMITVE;
        resultValue.terminatingStr = "";

        //Parser has already initalized maxElem within the resultArray
        tmp = resultArray.maxElem;
        resultValue.value = String.valueOf(tmp);

        return resultValue;
    }
    /**
     ******************************** HELPER FUNCTIONS ***********************************************
     */
    
    /**
     * Coerce a result value to the given type
     * <p>
     * Takes in a ResultValue object and a type constant which is
     * defined in Token. Attempts to change the result value to the
     * type, and if it is unsuccessful, will error out, giving the
     * operation for which the coercion failed
     * @param parser      to use its different error methods and stop execution
     * @param coerceType  the type constant to coerce the result value to
     * @param resval      the result value to be coerced
     * @param operation   the operation which contains the operand being coerced
     * @throws Exception  if the result value could not be coerced into the type
     *                    if the type passed in is not a defined type
     */
    public static void coerce(Parser parser, int coerceType, ResultValue resval, String operation) throws Exception
    {
        // Same type so there is nothing to coerce.
        if (coerceType == resval.type)
        {
            return;
        }
        
        // Check the type to coerce to
        if (coerceType == Token.INTEGER)
        {
            // Coerce to an INTEGER
            switch(resval.type)
            {
                case Token.FLOAT:
                    // The result value is a FLOAT to be coerced into an INTEGER
                    try
                    {
                        double tempDouble = Double.parseDouble(resval.value);
                        resval.value = Integer.toString((int)tempDouble);
                        resval.type = Token.INTEGER;
                    }
                    catch(NumberFormatException e)
                    {
                        // This will really only happen if we scan/store a FLOAT incorrectly
                        parser.errorWithCurrent("Unable to coerce value '%s' of type 'FLOAT' into type 'INTEGER' for operation '%s'"
                                                , resval.value, operation);
                    }
                    break;
                case Token.STRING:
                    // The result value is a STRING to be coerced into an INTEGER
                    try
                    {
                        // Attempt to parse the string as an int
                        Integer.parseInt(resval.value);
                        // It parsed properly so change the type to INTEGER
                        resval.type = Token.INTEGER;
                    }
                    catch(NumberFormatException e)
                    {
                        // STRING could not be parsed into INTEGER
                        parser.errorWithCurrent("Unable to coerce value '%s' of type 'STRING' into type 'INTEGER' for operation '%s'"
                                                , resval.value, operation);
                    }
                    break;
                default:
                    parser.errorWithCurrent("Illegal operation: attempted to coerce type '%s' into type 'INTEGER'", Token.getType(parser, resval.type));                    
            }
        }
        else if (coerceType == Token.FLOAT)
        {
            // Coerce to a FLOAT
            switch(resval.type)
            {
                case Token.INTEGER:
                    // The result value is an INTEGER to be coerced into a FLOAT
                    try
                    {
                        int tempInt = Integer.parseInt(resval.value);
                        resval.value = Double.toString((double)tempInt);
                        resval.type = Token.FLOAT;
                    }
                    catch(NumberFormatException e)
                    {
                        // This will really only happen if we scan/store an INTEGER incorrectly
                        parser.errorWithCurrent("Unable to coerce value '%s' of type 'INTEGER' into type 'FLOAT' for operation '%s'"
                                                , resval.value, operation);
                    }
                    break;
                case Token.STRING:
                    // The result value is an STRING to be coerced into a FLOAT
                    try
                    {
                        // Attempt to parse the string as an double
                        Double.parseDouble(resval.value);
                        // It parsed properly so change the type to FLOAT
                        resval.type = Token.FLOAT;
                    }
                    catch(NumberFormatException e)
                    {
                        // STRING could not be parsed into FLOAT
                        parser.errorWithCurrent("Unable to coerce value '%s' of type 'STRING' into type 'FLOAT' for operation '%s'"
                                                , resval.value, operation);
                    }
                    break;
                default:
                    parser.errorWithCurrent("Illegal operation: attempted to coerce type '%s' into type 'FLOAT'",Token.getType(parser, resval.type));
            }
        }
        else if(coerceType == Token.BOOLEAN)
        {
            // Coerce to a BOOLEAN
            switch(resval.type)
            {
                case Token.STRING:
                    // The result value is a STRING to be coerced into a BOOLEAN
                    
                    // See if the string contains boolean values 'T' or 'F'
                    if(Arrays.asList("T", "F").contains(resval.value))
                    {
                        resval.type = Token.BOOLEAN;
                    }
                    // String is not a valid boolean value
                    else
                    {
                        parser.errorWithCurrent("Unable to coerce value '%s' of type 'STRING' into type 'BOOLEAN' for operation '%s'"
                                                , resval.value, operation);
                    }
                    break;
                default:
                    parser.errorWithCurrent("Illegal operation: attempted to coerce type '%s' into type 'BOOLEAN'", Token.getType(parser, resval.type));
            }
        }
        else if(coerceType == Token.DATE)
        {
            // TODO when we add date types
            // Coerce to a DATE
            switch(resval.type)
            {
                case Token.STRING:
                    // The result value is a STRING to be coerced into a DATE
                    
                    // See if the string is a valid date.
                    if(isValidDate(resval.value))
                    {
                        resval.type = Token.DATE;
                    }
                    // String is not a valid boolean value
                    else
                    {
                        parser.errorWithCurrent("Unable to coerce value '%s' of type 'STRING' into type 'DATE' for operation '%s'"
                                                , resval.value, operation);
                    }
                    break;
                default:
                    parser.errorWithCurrent("Illegal operation: attempted to coerce type '%s' into type 'DATE'", Token.getType(parser, resval.type));
            }
        }
        else if(coerceType == Token.STRING)
        {
            //If it is a float, reduce precision to two decimal spaces
            if(resval.type == Token.FLOAT)
            {
                try
                {
                    resval.value = String.format("%.2f", Double.parseDouble(resval.value));
                }
                catch(NumberFormatException e)
                {
                    // STRING could not be parsed into FLOAT
                    parser.errorWithCurrent("Unable to coerce value '%s' of type 'FLOAT' into type 'STRING' for operation '%s'"
                                            , resval.value, operation);
                }
            }
            
            // All data types are stored as STRINGS, so just change the
            // result value's type
            resval.type = Token.STRING;
        }
        else
        {
            parser.errorWithCurrent("Illegal operation: attempted to coerce value '%s' into unknown type represented by '%d'", resval.value, resval.type);
        }
    }

    /**
     * TODO: DOCUMENTATION FOR THIS FUNCTION
     * @param resParam
     * @return
     */
    public static ResultValue getResultValueCopy(ResultValue resParam)
    {
        ResultValue resReturn = new ResultValue();
        resReturn.type = resParam.type;
        resReturn.value = resParam.value;
        resReturn.structure = resParam.structure;
        return resReturn;
    }
    
    
    /**
     * Outputs the appropriate number of days in a month based on the month and the year.
     * It needs the year to determine leap years that happen in February(2)
     * @param year - The calendar year as an integer.
     * @param month - The calendar month as an integer.
     * @return integer representing the number of days in a particular month.
     */
    private static int daysInMonth(int year, int month) {
        int daysInMonth;
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                daysInMonth = 31;
                break;
            case 2:
                if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
                    daysInMonth = 29;
                } else {
                    daysInMonth = 28;
                }
                break;
            default:
                // returns 30 even for nonexistant months 
                daysInMonth = 30;
        }
        return daysInMonth;
    }
}