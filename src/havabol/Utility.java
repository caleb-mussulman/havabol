package havabol;

import sun.util.resources.en.CalendarData_en;

import java.text.ParseException;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * @desc Performs an operation on two numerics, stores the result as a ResultValue,
 * and returns it to whatever called it.
 * This is used for behind the scenes operations that are not apparent to the
 * programmer.
 * <p>
 * programmer makes a statement
 * if x < 5:
 *    ...
 * endif:
 * The < operation is what this method handles.
 *
 * @authors Taylor Brauer, Caleb Mussulman, Steven Cenci
 */
public class Utility
{
    //LOGICAL OPERATIONS
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

    /**
     * Binary operation 'subtracting' two values (ResultValues) in Havabol.
     * Follows numeric coerce rules.
     * <p>
     * @param parser              - Used for error handling.
     * @param resParam1           - First value (object) for binary operation.
     * @param resParam2           - Second value (object) for binary operation.
     * @param operationCalledFrom - A description of the operation.
     * @return                    - Returns a value (ResultValue Object Reference)
     * @throws Exception          - ...
     */
    public static ResultValue subtract(Parser parser, ResultValue resParam1, ResultValue resParam2, String operationCalledFrom) throws Exception
    {
        if(resParam1 instanceof ResultArray || resParam2 instanceof ResultArray)
        {
            parser.error("Operation '-' expected Primitive parameters, Found '%s' and '%s' may be Array(s)"
                         , resParam1.value, resParam2.value);
        }
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

    /**
     * Binary operation 'addition' of two values (ResultValues) in Havabol.
     * Follows numeric coerce rules.
     * <p>
     * @param parser              - Used for error handling.
     * @param resParam1           - First value (object) for binary operation.
     * @param resParam2           - Second value (object) for binary operation.
     * @param operationCalledFrom - A description of the operation.
     * @return                    - Returns a value (ResultValue Object Reference)
     * @throws Exception          - ...
     */
    public static ResultValue add(Parser parser, ResultValue resParam1, ResultValue resParam2, String operationCalledFrom) throws Exception
    {
        //Binary operands may be of subclass ResultArray. This is not valid for this function.
        if(resParam1 instanceof ResultArray || resParam2 instanceof ResultArray)
        {
            parser.error("Operation '+' expected Primitive parameters, Found '%s' and '%s' may be Array(s)"
                    , resParam1.value, resParam2.value);
        }

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

    /**
     * Binary operation 'multiplying' two values (ResultValues) in Havabol.
     * Follows numeric coerce rules.
     * <p>
     * @param parser     - Used for error handling.
     * @param resParam1  - First value (object) for binary operation.
     * @param resParam2  - Second value (object) for binary operation.
     * @return           - Returns a value (ResultValue Object Reference)
     * @throws Exception - ...
     */
    public static ResultValue multiply(Parser parser, ResultValue resParam1, ResultValue resParam2) throws Exception
    {

        //Binary operands may be of subclass ResultArray. This is not valid for this function.
        if(resParam1 instanceof ResultArray || resParam2 instanceof ResultArray)
        {
            parser.error("Operation '*' expected Primitive parameters, Found '%s' and '%s' may be Array(s)"
                    , resParam1.value, resParam2.value);
        }

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

    /**
     * Binary operation 'dividing' two values (ResultValues) in Havabol.
     * Follows numeric coerce rules.
     * <p>
     * @param parser     - Used for error handling.
     * @param resParam1  - First value (object) for binary operation.
     * @param resParam2  - Second value (object) for binary operation.
     * @return           - Returns a value (ResultValue Object Reference)
     * @throws Exception - ...
     */
    public static ResultValue divide(Parser parser, ResultValue resParam1, ResultValue resParam2) throws Exception
    {
        //Binary operands may be of subclass ResultArray. This is not valid for this function.
        if(resParam1 instanceof ResultArray || resParam2 instanceof ResultArray)
        {
            parser.error("Operation '/' expected Primitive parameters, Found '%s' and '%s' may be Array(s)"
                    , resParam1.value, resParam2.value);
        }
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
     * @param parser    - Used for error handling.
     * @param operation - Binary operator (==, !=, <, >, <=, >=)
     * @param resParam1 - Object containing result value 1
     * @param resParam2 - Object containing result value 2
     * @return          - A true or false Havabol value (e.g. "T" or "F")
     * @throws Exception when a RS value fails to parse correctly.
     */
    public static ResultValue compare(Parser parser, int operation, ResultValue resParam1, ResultValue resParam2) throws Exception
    {
        //Binary operands may be of subclass ResultArray. This is not valid for this function.
        if(resParam1 instanceof ResultArray || resParam2 instanceof ResultArray)
        {
            parser.error("Comparision Operation: '%s' expected Primitive parameters, Found '%s' and '%s' may be Array(s)"
                    ,logicalOperator.get(operation), resParam1.value, resParam2.value);
        }
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
                if ((resval2.type == Token.STRING) || (resval2.type == Token.BOOLEAN) || (resval2.type == Token.DATE))
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
                if ((resval2.type == Token.STRING) || (resval2.type == Token.BOOLEAN) || (resval2.type == Token.DATE))
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
                if ((resval2.type == Token.STRING) || (resval2.type == Token.BOOLEAN))
                {
                    Utility.coerce(parser, Token.BOOLEAN, resval1, logicalOperator.get(operation));
                    Utility.coerce(parser, Token.BOOLEAN, resval2, logicalOperator.get(operation));
                    
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
                if ((resval2.type == Token.STRING) || (resval2.type == Token.BOOLEAN))
                {
                    Utility.coerce(parser, Token.BOOLEAN, resval1, logicalOperator.get(operation));
                    Utility.coerce(parser, Token.BOOLEAN, resval2, logicalOperator.get(operation));

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

    /**
     * Does a negation operation on a given ResultValue. (Inverse of ResultValue.value)
     * Follows numeric coerce rules.
     * <p>
     * @param parser      - Used for error handling.
     * @param resParam    - Value (object) for NOT operation.
     * @return            - Returns a value (ResultValue Object Reference)
     * @throws Exception  - ...
     */
    public static ResultValue not(Parser parser, ResultValue resParam) throws Exception
    {
        //Binary operands may be of subclass ResultArray. This is not valid for this function.
        if(resParam instanceof ResultArray)
        {
            parser.error("Operation 'not' expected Primitive parameters, Found Array '%s'"
                    , resParam.value);
        }
        // Must get a copy of the passed in result value so that the
        // original result value object is not manipulated, and coerce
        ResultValue resval = Utility.getResultValueCopy(resParam);
        Utility.coerce(parser, Token.BOOLEAN, resval, "not");
        
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

    /**
     * Does a String concatenation on 2 ResultValues (Strings in Havabol)
     * Follows numeric coerce rules.
     * <p>
     *     Example:
     *     String x = "Hello";
     *     String y = "World";
     *     String z = x # y; // Hello World
     * @param parser          - Used for error handling.
     * @param resParam1       - First value (object) for binary operation.
     * @param resParam2       - Second value (object) for binary operation.
     * @return                - Returns a value (ResultValue Object Reference)
     * @throws Exception      - ...
     */
    public static ResultValue concat(Parser parser, ResultValue resParam1, ResultValue resParam2) throws Exception
    {
        //Binary operands may be of subclass ResultArray. This is not valid for this function.
        if(resParam1 instanceof ResultArray || resParam2 instanceof ResultArray)
        {
            parser.error("Operation '#' expected Primitive parameters, Found '%s' and '%s' may be Array(s)"
                        , resParam1.value, resParam2.value);
        }

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

    /**
     * Does a mathematical negation operation on Numeric values. (Negative numbers)
     * Simply does (ResultValue.value * -1) for the negation.
     * Ensures numeric value.
     * <p>
     *     Example:
     *     Int x = -6; //Negative Numbers
     * @param parser      - Used for error handling.
     * @param resParam    - Value (object) for UNARY operation.
     * @return            - Returns a value (ResultValue Object Reference)
     * @throws Exception  - ...
     */
    public static ResultValue uminus(Parser parser, ResultValue resParam) throws Exception
    {
        //Binary operands may be of subclass ResultArray. This is not valid for this function.
        if(resParam instanceof ResultArray)
        {
            parser.error("Operation (unary) '-' expected Primitive parameters, Found Array '%s'"
                        , resParam.value);
        }
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

    /**
     * Does an exponentiation operation with 2 ResultValues.
     * Ensures numeric values.
     * <p>
     *     Example:
     *     Int x = 2^5; // 32
     * @param parser     - Used for error handling.
     * @param resParam1  - First value (object) for binary operation.
     * @param resParam2  - Second value (object) for binary operation.
     * @return           - Returns a value (ResultValue Object Reference)
     * @throws Exception - ...
     */
    public static ResultValue exponent(Parser parser, ResultValue resParam1, ResultValue resParam2) throws Exception
    {
        //Binary operands may be of subclass ResultArray. This is not valid for this function.
        if(resParam1 instanceof ResultArray || resParam2 instanceof ResultArray)
        {
            parser.errorWithCurrent("Operation '^' expected Primitive parameters, Found '%s' and '%s' may be Array(s)"
                        , resParam1.value, resParam2.value);
        }
        // Must get a copy of the passed in result values so that the
        // original result value objects are not manipulated
        ResultValue resval1 = Utility.getResultValueCopy(resParam1);
        ResultValue resval2 = Utility.getResultValueCopy(resParam2);
        
        String result = "";
        
        Numeric nOp1 = new Numeric(parser, resval1, "^", "1st operand");
        Numeric nOp2;
        
        coerce(parser, resval1.type, resval2, "^");
        nOp2 = new Numeric(parser, resval2, "^", "2nd operand");
        
        if (nOp1.type == Token.INTEGER)
        {
            Double exponVal = Math.pow(nOp1.integerValue, nOp2.integerValue);
            result = String.valueOf(exponVal.intValue());
        }
        else
        {
            Double exponVal = Math.pow(nOp1.doubleValue, nOp2.doubleValue);
            result = String.valueOf(exponVal);
        }
        
        resval1.value = result;
        return resval1;
    }
    
    /**
     * Parses an input date to see if it is of form yyyy-mm-dd
     * mm and dd must have a 0 in front of them if it is a single integer date.
     * <p>
     * @param  - date
     * @return - ...
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
            if (year < 0001)
            {
                return false;
            }
            int month = Integer.parseInt(date.substring(5,7));
            if (month > 12 || month == 0)
            {
                return false;
            }
            int daysPerMonth = daysInMonth(year, month);
            int day = Integer.parseInt(date.substring(8, 10));
            if (day == 0 || day > daysPerMonth)
            {
                return false;
            }
            
            return true;
        }
        else
            return false;
    }

    /**
     * Compares 2 dates to calculate a difference in the number of days between the 2.
     * @param parser - Used for error messages
     * @param resParam1  - First date, in date format.
     * @param resParam2  - Second date, in date format.
     * @return - A result value object containing the difference in the 2 dates. This type is now an
     *           integer.
     * @throws ParserException
     */
    public static ResultValue dateDiff(Parser parser, ResultValue resParam1, ResultValue resParam2) throws ParserException
    {
        int julian1;
        int julian2;
        int result;
        ResultValue dateDifference = new ResultValue();

        // Binary operands may be of subclass ResultArray. This is not valid for this function.
        if(resParam1 instanceof ResultArray || resParam2 instanceof ResultArray)
        {
            parser.errorWithCurrent("Method 'dateDiff' expected Primitive parameters, Found '%s' and '%s' may be Array(s)"
                    , resParam1.value, resParam2.value);
        }

        // Validate both inputs
        if (!isValidDate(resParam1.value))
        {
            parser.errorWithCurrent("The first argument to dateDiff is not a valid date. | Value : %s |", resParam1.value);
        }

        if (!isValidDate(resParam2.value))
        {
            parser.errorWithCurrent("The second argument to dateDiff is not a valid date. | Value : %s |", resParam2.value);
        }

        // Compare to March
        julian1 = dateToJulian(resParam1.value);
        julian2 = dateToJulian(resParam2.value);

        // # of days between the two dates
        result = julian1 - julian2;

        dateDifference.structure = STIdentifier.PRIMITVE;
        dateDifference.type = Token.INTEGER;
        dateDifference.value = String.valueOf(result);

        return dateDifference;
    }

    /**
     * This method takes a date and manipulates it based on a positive or negative integer. It moves the date forward
     * or backward in time based on the number input into the method.
     * @param parser - Userd for error messages.
     * @param resParam1   - This is a date format.
     * @param resParam2   - This is the number of days to adjust the date by. This is an integer that can be
     *                 positive or negative which compares the date to the future or past respectively.
     * @return       - A result array that is type DATE and contains a valid date string as its value.
     * @throws ParserException
     */
    public static ResultValue dateAdj(Parser parser, ResultValue resParam1, ResultValue resParam2) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        ResultValue dateAdj = new ResultValue();

        // Binary operands may be of subclass ResultArray. This is not valid for this function.
        if(resParam1 instanceof ResultArray || resParam2 instanceof ResultArray)
        {
            parser.errorWithCurrent("Method 'dateAdj' expected Primitive parameters, Found '%s' and '%s' may be Array(s)"
                    , resParam1.value, resParam2.value);
        }

        // Validate the input for date.
        if (!isValidDate(resParam1.value))
        {
            parser.errorWithCurrent("The first argument to dateAdj is not a valid date. | Value : %s |", resParam1.value);
        }

        // Coerce days to integer
        ResultValue days = Utility.getResultValueCopy(resParam2);
        Utility.coerce(parser, Token.INTEGER, days, "dateAdj");

        // Break up the date for the gregorian calendar
        int year = Integer.parseInt(resParam1.value.substring(0, 4));
        int month = Integer.parseInt(resParam1.value.substring(5,7));
        int day = Integer.parseInt(resParam1.value.substring(8, 10));

        // Make a new java date
        Calendar cDate = new GregorianCalendar(year, month-1, day);


        // Validate the java converted date.
        if (!isValidDate(sdf.format(cDate.getTime())))
        {
            parser.errorWithCurrent("%s is not a valid date.", sdf.format(cDate.getTime()));
        }

        // Days to adjust the date by.
        // NOTE: This exception will only be raised if there is an error with coerce.
        try
        {
            int daysAdjustment = Integer.parseInt(days.value);
            cDate.add(Calendar.DAY_OF_MONTH, daysAdjustment);
            //System.err.println(cDate.get(Calendar.ERA) + " " + sdf.format(cDate.getTime()));
        }
        catch (Exception e)
        {
            parser.errorWithCurrent("dateAdj parameter 3 is of type %s but failed to parse as a valid integer.", Token.getType(parser, Token.INTEGER));
        }

        // Validate after the conversion as well.
        if (!isValidDate(sdf.format(cDate.getTime())))
        {
            parser.errorWithCurrent("%s is not a valid date.", sdf.format(cDate.getTime()));
        }

        if (cDate.get(Calendar.ERA) < 1)
        {
            parser.errorWithCurrent("Year must be greater than 0000.");
        }


        // Store the new calendar date in the result value object.
        dateAdj.value = sdf.format(cDate.getTime());
        dateAdj.type  = Token.DATE;
        dateAdj.structure = STIdentifier.PRIMITVE;

        return dateAdj;
    }

    /**
     * This takes two dates and returns only the number of years between the two.
     * @param parser - Userd for error messages.
     * @param resParam1  - First date, in date format.
     * @param resParam2  - Second date, in date format.
     * @return       - The number of years between the two dates stored in a result value that is of type integer.
     * @throws ParserException
     */
    public static ResultValue dateAge(Parser parser, ResultValue resParam1, ResultValue resParam2) throws ParserException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        ResultValue dateAge = new ResultValue();

        // Binary operands may be of subclass ResultArray. This is not valid for this function.
        if(resParam1 instanceof ResultArray || resParam2 instanceof ResultArray)
        {
            parser.errorWithCurrent("Method 'dateAge' expected Primitive parameters, Found '%s' and '%s' may be Array(s)"
                    , resParam1.value, resParam2.value);
        }

        // Validate the input dates.
        if (!isValidDate(resParam1.value))
        {
            parser.errorWithCurrent("The first argument to dateAge is not a valid date. | Value : %s |", resParam1.value);
        }

        if (!isValidDate(resParam2.value))
        {
            parser.errorWithCurrent("The second argument to dateAge is not a valid date. | Value : %s |", resParam2.value);
        }

        // Compare to March
        // Break up the first date for the gregorian calendar
        int year1 = Integer.parseInt(resParam1.value.substring(0, 4));
        int month1 = Integer.parseInt(resParam1.value.substring(5,7));
        int day1 = Integer.parseInt(resParam1.value.substring(8, 10));

        // Break up the second date for the gregorian calendar
        int year2 = Integer.parseInt(resParam2.value.substring(0, 4));
        int month2 = Integer.parseInt(resParam2.value.substring(5,7));
        int day2 = Integer.parseInt(resParam2.value.substring(8, 10));

        // Set dates
        Calendar cDate1 = new GregorianCalendar(year1, month1-1, day1);
        Calendar cDate2 = new GregorianCalendar(year2, month2-1, day2);

        // Validate after java date conversion.
        if (!isValidDate(sdf.format(cDate1.getTime())))
        {
            parser.errorWithCurrent("%s is not a valid date.", sdf.format(cDate1.getTime()));
        }

        if (!isValidDate(sdf.format(cDate2.getTime())))
        {
            parser.errorWithCurrent("%s is not a valid date.", sdf.format(cDate2.getTime()));
        }

        // Calculate years difference (YEAR1 - YEAR2)
        int numYearsApart = cDate1.get(Calendar.YEAR) - cDate2.get(Calendar.YEAR);

        // Need to check the difference in months/days to determine the correct difference in years.
        // This depends on the ordering of the parameters, e.g., Given the dates 15 Nov 1995
        // and 15 Feb 1997, 1997 - 1995 = 2, but the difference in years is 1 year
        if(cDate2.after(cDate1))
        {
            // Reduce the year if the following criteria is met.
            if (cDate1.get(Calendar.MONTH) > cDate2.get(Calendar.MONTH) ||
                    (cDate1.get(Calendar.MONTH) == cDate2.get(Calendar.MONTH) && cDate1.get(Calendar.DATE) > cDate2.get(Calendar.DATE)))
            {
                numYearsApart++;
            }
        }
        else
        {
            if (cDate2.get(Calendar.MONTH) > cDate1.get(Calendar.MONTH) ||
                    ((cDate2.get(Calendar.MONTH) == cDate1.get(Calendar.MONTH) && cDate2.get(Calendar.DATE) > cDate1.get(Calendar.DATE))))
            {
                numYearsApart--;
            }
        }

        // Store the difference in years in the result value object.
        dateAge.value = String.valueOf(numYearsApart);
        dateAge.type  = Token.INTEGER;
        dateAge.structure = STIdentifier.PRIMITVE;

        return dateAge;
    }

    /******************** dateToJulian ***********************************
     public static int dateToJulian(String date)
     Purpose:
     Converts a date to a Julian Days value.  This will start numbering
     at 1 for 0000-03-01. Making dates relaive to March 1st helps eliminate
     some leap day issues.
     Parameters:
     String date        Date as a string in the form "yyyy-mm-dd"
     Notes:
     1 We replace the month with the number of months since March.
     March is 0, Apr is 1, May is 2, ..., Jan is 10, Feb is 11.
     2 Since Jan and Feb are before Mar, we subtract 1 from the year
     for those months.
     3 Jan 1 is 306 days from Mar 1.
     4 The days per month is in a pattern that begins with March
     and repeats every 5 months:
     Mar 31 Aug 31 Jan 31
     Apr 30 Sep 30
     May 31 Oct 31
     Jun 30 Nov 30
     Jul 31 Dec 31
     Therefore:
     Mon  AdjMon  NumberDaysFromMarch (AdjMon*306 + 5)/10
     Jan    10      306
     Feb    11      337
     Mar     0        0
     Apr     1       31
     May     2       61
     Jun     3       92
     Jul     4      122
     Aug     5      153
     Sep     6      184
     Oct     7      214
     Nov     8      245
     Dec     9      275
     5 Leap years are
     years that are divisible by 4 and
     either years that are not divisible by 100 or
     years that are divisible by 400
     Return Value:
     the number of days since 0000-03-01 beginning with 1 for
     0000-03-01.
     **********************************************************************/
    public static int dateToJulian(String date)
    {
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5,7));
        int day = Integer.parseInt(date.substring(8, 10));

        // Calculate number of days in 0000-03-01
        int countDays;

        if (month > 2)
            month -= 3;
        else
        {
            month += 9;
            year--;
        }

        countDays = 365*year                    // 365 days in a year
                + year/4 - year/100 + year/400  // add a day for each leap year
                + (month * 306 + 5) / 10        // Days per month plus 5 months for repeats.
                + day;                          // add the days
        return countDays;
    }
    
    /**
     ******************************** START OF FUNCTIONS ***************************************************
     */
    
    /**
     * This takes in a string, creates a new Result Value, gets the size of the value in the passed in
     * ResultValue and assigns that value to the newly created Result Value as an Integer.
     * <p>
     * @param parser     - Used for sending error messages to the programmer.
     * @return           - A result value that contains information about the length of the string that was passed in.
     * @throws Exception - ...
     */
    public static ResultValue LENGTH(Parser parser, ResultValue resOp) throws Exception
    {
        //resOp may be subclass ResultArray.
        if(resOp instanceof ResultArray)
        {
            //function takes in a ResultValue. Not a ResultArray.
            parser.error("Function 'LENGTH' expected Primitive parameter, Found Array '%s'"
                        ,resOp.value);
        }

        // Get a copy of the operand and attempt to coerce to a string
        ResultValue resString = Utility.getResultValueCopy(resOp);
        Utility.coerce(parser, Token.STRING, resString, "LENGTH");
        
        // Get the length of the string
        int len = resString.value.length();
        
        // Construct the return value using the length
        ResultValue resLength = new ResultValue();
        resLength.value = String.valueOf(len);
        resLength.type = Token.INTEGER;
        resLength.structure = STIdentifier.PRIMITVE;
        
        return resLength;
    }
    
    /**
     * This takes in a string, creates a new Result Value, checks to see if the string is empty or matches
     * to some white space, and returns true is during its check it does not encounter a non white space
     * character.
     * <p>
     * @param parser     - Used for sending error messages to the programmer.
     * @return           - Result value that contains T or F indicating if the string has spaces or is empty.
     * @throws Exception - ...
     */
    public static ResultValue SPACES(Parser parser, ResultValue resOp) throws Exception
    {
        //resOp may be subclass ResultArray
        if(resOp instanceof ResultArray)
        {
            //function takes in a ResultValue. Not a ResultArray.
            parser.error("Function 'SPACES' expected Primitive parameter, Found Array '%s'"
                    ,resOp.value);
        }


        // Get a copy of the operand and attempt to coerce to a string
        ResultValue resString = Utility.getResultValueCopy(resOp);
        Utility.coerce(parser, Token.STRING, resString, "LENGTH");
        
        ResultValue resSpaces = new ResultValue();
        resSpaces.type = Token.BOOLEAN;
        resSpaces.structure = STIdentifier.PRIMITVE;
        
        // Determine if the string is empty
        if(resString.value.isEmpty())
        {
            resSpaces.value = "T";
        }
        // The string is not empty, so check if it only contains spaces
        else
        {
            // Assume the string initially only contains spaces
            resSpaces.value = "T";
            
            // Check each character to find one that isn't a space
            for(int i = 0; i < resString.value.length(); i++)
            {
                // If the character isn't a space, return false
                if(! (resString.value.charAt(i) == ' '))
                {
                    resSpaces.value = "F";
                    break;
                }
            }
        }
        
        return resSpaces;
    }
    
    /**
     * Havabol built-in function:
     * ELEM - short for ELEMENTS
     * FIXED or UNBOUNDED
     * <p>
     * Returning a ResultValue that holds the number of elements in the Havabol Array
     * @param parser      - Responsible for handling error messages.
     * @param resultArray - A ResultArray object reference -- A Havabol Array.
     * @return            - The number of elements in the array
     */
    public static ResultValue ELEM(Parser parser, ResultArray resultArray)
    {
        // Create a new resultValue to return and initialize its attributes
        ResultValue resultValue = new ResultValue();
        resultValue.type = Token.INTEGER;   //This will always be an integer.
        resultValue.structure = STIdentifier.PRIMITVE;

        // The highest populated subscript + 1, in ArrayList's is simply the what the .size() function returns.
        int tmp = resultArray.valueList.size();     // Returns the number of Elements in the array.

        resultValue.value = String.valueOf(tmp); // Converts integer value to a string.

        return resultValue;
    }

    /**
     * Gets the Maximum declared size of a Fixed Array and returns
     * <p>
     * This value is saved inside the ResultArray Object.
     * @param parser      - Responsible for handling error messages.
     * @param resultArray - The array that is being referenced by Parser.
     * @return            - A Result Value of the max elem value in result array.
     */
    public static ResultValue MAXELEM(Parser parser, ResultArray resultArray) throws ParserException
    {
        // Unbounded arrays don't have a maximum element
        if(resultArray.structure == STIdentifier.UNBOUNDED_ARRAY)
        {
            parser.errorWithCurrent("Function 'MAXELEM' is undefined for unbounded arrays");
        }

        // Create a new resultValue to return and initialize its attributes.
        ResultValue resultValue = new ResultValue();
        resultValue.type = Token.INTEGER;
        resultValue.structure = STIdentifier.PRIMITVE;

        // Parser has already initialized maxElem within the resultArray
        int tmp = resultArray.maxElem;
        resultValue.value = String.valueOf(tmp);

        return resultValue;
    }
    
    /**
     * Assumes that 'resval' is a primitive
     * TODO : Needs to support a value list (e.g. gradePt IN {4, 3, 2, 1, 0} )
     * Searches an array resultArray for a value resval contained in it.
     * Upon finding it a truthy or falsey value are returned to the caller.
     * <p>
     * @param parser      - Responsible for handling error messages.
     * @param resval      - The value being searched for.
     * @param resultArray - The array that a value is being located in.
     * @return Havabol T or F.
     * @throws ParserException
     */
    public static ResultValue IN(Parser parser, ResultValue resval, ResultArray resultArray) throws Exception
    {
        ResultValue resReturn = new ResultValue();
        resReturn.type = Token.BOOLEAN;
        resReturn.structure = STIdentifier.PRIMITVE;
        resReturn.value = "F";
        
        for(ResultValue resArrElem : resultArray.valueList)
        {
            if(resArrElem != null)
            {
                ResultValue resArrElemCopy = Utility.getResultValueCopy(resArrElem);
                Utility.coerce(parser, resval.type, resArrElemCopy, "IN");
                
                if(resArrElemCopy.value.equals(resval.value))
                {
                    resReturn.value = "T";
                    break;
                }
            }
        }

        return resReturn;
    }
    
    /**
     * TODO : Needs to support a value list (e.g. fruit NOTIN {"apple", "orange", "clark"} )
     * Searches an array resultArray to determine if a value is not contained in the array.
     * Upon finding it a truthy or falsey value are returned to the caller.
     * <p>
     * @param parser           - Responsible for handling error messages.
     * @param resval           - The value being searched for.
     * @param resultArray      - The array that a value is being located in.
     * @return                 - ResultValue Havabol boolean T or F.
     * @throws ParserException - ...
     */
    public static ResultValue NOTIN(Parser parser, ResultValue resval, ResultArray resultArray) throws ParserException
    {
        //Binary operands may be of subclass ResultArray. This is not valid for this function.
        if(resval instanceof ResultArray)
        {
            parser.error("Operation 'NOTIN' expected Primitive parameters, Found Array '%s'"
                        , resval.value);
        }

        if (resultArray.structure != STIdentifier.FIXED_ARRAY ||
                resultArray.structure != STIdentifier.UNBOUNDED_ARRAY)
        {
            parser.errorWithCurrent("Cannot start search for type %s in type %s.", resval.value, Token.getType(parser, resultArray.type));
        }
            
        if (resval.structure != STIdentifier.PRIMITVE)
        {
            parser.errorWithCurrent("Cannot start search for type %s in %s.", Token.getType(parser, resval.type),
                        Token.getType(parser, resultArray.type));
        }
            
        ResultValue resReturn = new ResultValue();
            
        if (!resultArray.valueList.contains(resval.value))
        {
            resReturn.value = "T";
            resReturn.type = Token.BOOLEAN;
            resReturn.structure = STIdentifier.PRIMITVE;
        }
        else
        {
            resReturn.value = "F";
            resReturn.type = Token.BOOLEAN;
            resReturn.structure = STIdentifier.PRIMITVE;
        }
            
            return resReturn;
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
                    parser.errorWithCurrent("Unable to coerce value '%s' of type '%s' into type 'INTEGER' for operation '%s'"
                                            , resval.value, Token.getType(parser, resval.type), operation);                    
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
                    parser.errorWithCurrent("Unable to coerce value '%s' of type '%s' into type 'FLOAT' for operation '%s'"
                            , resval.value, Token.getType(parser, resval.type), operation);
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
                    parser.errorWithCurrent("Unable to coerce value '%s' of type '%s' into type 'BOOLEAN' for operation '%s'"
                                            , resval.value, Token.getType(parser, resval.type), operation);
            }
        }
        else if(coerceType == Token.DATE)
        {
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
                    parser.errorWithCurrent("Unable to coerce value '%s' of type '%s' into type 'DATE' for operation '%s'"
                                            , resval.value, Token.getType(parser, resval.type), operation);
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
            parser.errorWithCurrent("Unable to coerce value '%s' into unknown type represented by '%d'", resval.value, resval.type);
        }
    }

    
    /**
     * Returns a fresh result value that is soley used for the purpose of storing a passed in result so that the original result
     * value is not manipulated resulting in data being overwritten.
     * <p>
     * @param resParam - The result value that we want to make a copy of.
     * @return         - A new result value with the contents of resParam.
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
     * @param year  - The calendar year as an integer.
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
                // This should never really be reached.
                daysInMonth = 30;
        }
        return daysInMonth;
    }
}