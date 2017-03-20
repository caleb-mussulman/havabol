package havabol;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    
    @SuppressWarnings("serial")
    public final static Map<Integer, String> logicalOperator = Collections.unmodifiableMap(new HashMap<Integer, String>(){{
                                                               put(31, "=="); put(32, "!="); put(33, "<"); put(34, ">");
                                                               put(35, "<="); put(36, ">="); put(37, "and"); put(38, "or"); }});
    
    /*
    * CLASS METHODS -- needs to deal with coercion.
    */
    // Assignment Operators (+=, -=, *=, /=)
    public static ResultValue subtract(Parser parser, ResultValue resOp1, ResultValue resOp2, String operationCalledFrom) throws Exception
    {
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
    
    public static ResultValue add(Parser parser, ResultValue resOp1, ResultValue resOp2, String operationCalledFrom) throws Exception
    {
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
    
    public static ResultValue multiply(Parser parser, ResultValue resOp1, ResultValue resOp2) throws Exception
    {
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
    
    public static ResultValue divide(Parser parser, ResultValue resOp1, ResultValue resOp2) throws Exception
    {
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
     * @param resval1 - Object containing result value 1
     * @param resval2 - Object containing result value 2
     * @return A true or false Havabol value (e.g. "T" or "F")
     * @throws Exception when a RS value fails to parse correctly.
     */
    public static ResultValue compare(Parser parser, int operation, ResultValue resval1, ResultValue resval2) throws Exception
    {
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
    
    public static ResultValue not(Parser parser, ResultValue resval) throws Exception
    {
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
    
    public static ResultValue concat(Parser parser, ResultValue resval1, ResultValue resval2) throws Exception
    {
        ResultValue resReturn = new ResultValue();
        String result = "";
        
        if (resval1.type == Token.STRING)
        {
            result = resval1.value + resval2.value;
        }
        else if (resval1.type == Token.INTEGER)
        {
            Integer iRes1 = Integer.parseInt(resval1.value);
            Integer iRes2 = Integer.parseInt(resval2.value);
            
            resval1.value = String.valueOf(iRes1);
            resval2.value = String.valueOf(iRes2);
            
            result = resval1.value + resval2.value;
        }
        else if (resval1.type == Token.FLOAT)
        {
            Double dRes1 = Double.parseDouble(resval1.value);
            Double dRes2 = Double.parseDouble(resval2.value);
            
            resval1.value = String.valueOf(dRes1);
            resval2.value = String.valueOf(dRes2);
            
            result = resval1.value + resval2.value;
        }
        else if (resval1.type == Token.BOOLEAN)
        {
            Boolean bRes1 = Boolean.parseBoolean(resval1.value);
            Boolean bRes2 = Boolean.parseBoolean(resval2.value);
            
            resval1.value = String.valueOf(bRes1);
            resval2.value = String.valueOf(bRes2);
            
            result = resval1.value + resval2.value;
        }
        else
        {
            parser.errorWithCurrent("Taylor didn't add another case for concat.");
        }
        
        resReturn.value = result;
        resReturn.type = Token.STRING;
        resReturn.structure = STIdentifier.PRIMITVE;
        
        return resReturn;
    }
    
    public static ResultValue uminus(Parser parser, ResultValue resval) throws Exception
    {
        if (resval.type == Token.STRING)
        {
            parser.errorWithCurrent("Cannot perform unairy minus on %s", resval.type);
        }
        else if (resval.type == Token.INTEGER)
        {
            Numeric nOp = new Numeric(parser, resval, "u-", "1st operand");
            resval.value = String.valueOf(nOp.integerValue *= -1);
        }
        else if (resval.type == Token.FLOAT)
        {
            Numeric nOp = new Numeric(parser, resval, "u-", "1st operand");
            resval.value = String.valueOf(nOp.doubleValue *= -1);
        }
        else if (resval.type == Token.BOOLEAN)
        {
            parser.errorWithCurrent("Cannot perform unairy minus on %s", resval.type);
        }
        else
        {
            parser.errorWithCurrent("Taylor didn't add another case for uminus.");
        }
        return resval;
    }
    
    public static ResultValue exponent(Parser parser, ResultValue resval1, ResultValue resval2) throws Exception
    {
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
        }
        else if(coerceType == Token.STRING)
        {
            // All data types are stored as STRINGS, so just change the result value's type
            resval.type = Token.STRING;
        }
        else
        {
            parser.errorWithCurrent("Illegal operation: attempted to coerce value '%s' into unknown type represented by '%d'", resval.value, resval.type);
        }
    }
}
