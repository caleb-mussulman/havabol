package havabol;
import javax.xml.transform.Result;
import java.util.HashMap;

public class StorageManager
{
    /* This class should NOT need fields for
    *  symbol and ResultValue because those variables
    *  are stored in the hashtable, not in an instance of StorageManager
    */
    public HashMap<String,ResultValue> sm;

    /**
     * Simply creates HashMap that will effectively be our
     * StorageManager.
     * <p>
     */
    StorageManager()
    {
        //Create a new HashMap -- Empty
        sm = new HashMap<String,ResultValue>();
    }
    
    /**
     * The function that will be used to store a variable value
     * into the HashMap using it's associated symbol (variable)
     * <p>
     * 
     * @param symbol
     * @param value 
     */
    void putVariableValue(Parser errParse, String symbol, ResultValue value)
    {
        //Stores the object reference to a ResultValue.
        sm.put(symbol, value);
    }
    
    /**
     * The function that will be used to get the value of the passed
     * symbol (variable) from the parser(?) to retrieve the value
     * of the symbol(variable).
     * <p>
     * Quickly does a hash lookup for the passed String symbol and returns
     * the value as a string...
     * Conversion will be done by numeric class
     * @param symbol - Key for the ResultValue (Variable name in Havabol)
     * @return       - ResultValue Object Reference from HashTable sm.
     */
    ResultValue getVariableValue(Parser errParse, String symbol) throws Exception
    {
        ResultValue resValue;
        resValue = sm.get(symbol);
        //We need to check if our symbol is in the StorageManager
        if (resValue == null)
        {
            //There is no value associated with that (valid) key
            //Call parser.error to call the ParserException
            errParse.error("Uninitialized value for variable '%s'", symbol);
        }
        //The value exists
        return resValue;
    }

    /**
     * Stores an array into the StorageManager HashMap sm.
     * <p>
     * @param errParse    - Used for error handling
     * @param symbol      - Key for the ResultArray (Array name in Havabol)
     * @param resultArray - Storing a ResultArray reference as the value.
     * @throws Exception  - ...
     */
    void putResultArray(Parser errParse, String symbol, ResultArray resultArray) throws Exception
    {
        sm.put(symbol, resultArray);
    }

    /**
     * Gets a reference to the ResultArray Object within the HashTable sm.
     * <p>
     * @param errParse - Used for error handling
     * @param symbol - Key for the ResultArray (Array Variable name in Havabol)
     * @return       - ResultArray Object Reference from HashTable sm.
     */
    ResultArray getResultArray(Parser errParse, String symbol) throws Exception
    {
        ResultArray resultArray;
        resultArray = (ResultArray) sm.get(symbol);
        if(resultArray == null)
        {
            //Could not find reference to array in storageManager
            errParse.error("Could not find reference to array '%s'", symbol);
        }
        return resultArray;
    }

    /**
     * Subscript assignment for FIXED and UNBOUNDED Arrays
     * Pads uninitialized subscripts with nulls to reach desired index assignment.
     * UNBOUNDED Arrays can be padded with nulls to very large indices.
     *<p>
     *Example:
     *  Int x[5];
     *  x[2] = 200;
     *  Symbol = x
     *  resultValue = 200
     *  index = 2
     * @param errParse    - Used for error handling
     * @param symbol      - Key for ResultArray (Array Variable name in Havabol)
     * @param resultValue - Source value to be set within the Array.
     * @param index       - Index reference to the position in the Array.
     * @throws Exception  - ...
     */
    void arrayAssignElem(Parser errParse, String symbol, ResultValue resultValue, ResultValue index) throws Exception {

        ResultArray resultArray;
        //getResultArray check if the resultArray exists already, no need to check in this function.
        resultArray = getResultArray(errParse, symbol);

        //Coerce the index to an integer ALWAYS.
        Utility.coerce(errParse, Token.INTEGER, index, "ArrayElementReference");

        //Must get the iIndex (subscript) value from the ResultValue index
        int iIndex = Integer.parseInt(index.value);

        //Stores the corresponding non-negative index of a negative subscript, if the index is non-negative, it remains 0.
        int iTmp_Index = 0;

        //If it's a negative subscript
        if(iIndex < 0)
        {
            //The corresponding non-negative index that the "negative" index references.
            iTmp_Index = resultArray.valueList.size() + iIndex;
        }

        // Attempting to assign to an index out of bounds.
        if((resultArray.structure == STIdentifier.FIXED_ARRAY) && ((iIndex > resultArray.maxElem-1) || (iTmp_Index < 0)))
        {
            errParse.error("Assignment of '%s' to '%s'['%s'] is out of bounds"
                          , resultValue.value, symbol, index.value);
        }
        
        /*
        //Negative subscripts do not work for UNBOUNDED arrays.
        if(resultArray.structure == STIdentifier.UNBOUNDED_ARRAY && iIndex < 0)
        {
            errParse.error("'%s' is negative, invalid subscript for Unbounded Array '%s'"
                    , index.value, symbol);
        }*/

        //Setting a value to an index that is beyond the current contiguous size of the array.
        if(iIndex >= resultArray.valueList.size())
        {
            //Pad the arrayList with null values to initialize an index that's beyond continuous space.
            for(int i = resultArray.valueList.size(); i <= iIndex; i++)
            {

                //If the resultArray has been scaled, all padding will be of scaledValue
                if(resultArray.bScaled == true)
                {
                    //If the type of the resultArray and scaledValue aren't the same, we must to coerce.
                    if (resultArray.type != resultArray.scaledValue.type)
                    {
                        Utility.coerce(errParse, resultArray.type, resultArray.scaledValue, "ArrayAssignmentInvalidType");
                    }

                    resultArray.valueList.add(i, resultArray.scaledValue);
                }
                //This array has not previously been Scaled (bScaled = false)
                //Fill with nulls instead of scaledValue
                else
                {
                    resultArray.valueList.add(i, null);
                }
            }

            //If the type of the resultArray and resultValue aren't the same, we must to coerce.
            if(resultArray.type != resultValue.type)
            {
                Utility.coerce(errParse, resultArray.type, resultValue, "ArrayAssignmentInvalidType");
            }

            //We have to check if iIndex was a negative subscript one more time, to assign to the correct index.
            if(iIndex < 0)
            {
                //iIndex was negative.
                resultArray.valueList.set(iTmp_Index, resultValue);
            }
            else
            {
                //iIndex was 0 or positive (normal)
                resultArray.valueList.set(iIndex, resultValue);
            }
        }
        //Any other index range. (Should  be within a valid range by this point)
        else
        {
            //If the type of the resultArray and resultValue aren't the same, we must to coerce.
            if(resultArray.type != resultValue.type)
            {
                Utility.coerce(errParse, resultArray.type, resultValue, "ArrayAssignmentInvalidType");
            }

            //We have to check if iIndex was a negative subscript one more time, to assign to the correct index.
            if(iIndex < 0)
            {
                //iIndex was negative.
                resultArray.valueList.set(iTmp_Index, resultValue);
            }
            else
            {
                //iIndex was non-negative (normal)
                resultArray.valueList.set(iIndex, resultValue);
            }
        }
    }


    /**
     * TODO: Error cases have to be thought out.
     *
     * Gets the reference to the ResultValue object at the specific index within ResultArray.valueList
     * <p>
     * @param errParse     - Used for error handling.
     * @param arraySymbol  - Key for ResultArray (Array Variable name in Havabol)
     * @param resIndex     - The position in the ResultArray.valueList being retrieved.
     * @return resultValue - The reference to the ResultValue object in the ResultArray.valueList
     * @throws Exception   - ...
     */
    ResultValue getArrayElem(Parser errParse, String arraySymbol, ResultValue resIndex) throws Exception
    {

        ResultValue resultValue;
        ResultArray resultArray;
        //Get the array from the HashTable -- will check if its already in the SM
        resultArray = getResultArray(errParse, arraySymbol);

        //Index should ALWAYS be an Token.INTEGER.
        if(resIndex.type != Token.INTEGER)
        {
            Utility.coerce(errParse, Token.INTEGER, resIndex, "ArrayElementReference");
        }

        int iIndex = Integer.parseInt(resIndex.value);

        int  iTmp_Index = 0;

        //If we have a negative subscript...
        //We convert the negative subscript to its corresponding non-negative index.
        if(iIndex < 0)
        {
            iTmp_Index = resultArray.valueList.size() + iIndex;
        }

        if((resultArray.structure == STIdentifier.FIXED_ARRAY) && ((iIndex > resultArray.maxElem-1) || (iTmp_Index < 0)))
        {
            errParse.error("Reference to '%s'['%s'] is out of bounds"
                          ,arraySymbol , resIndex.value);
        }

        //If it's negative then we are going to
        if(iIndex < 0)
        {
            resultValue = resultArray.valueList.get(iTmp_Index);
        }
        else
        {
            resultValue = resultArray.valueList.get(iIndex);

        }

        return resultValue;

    }

    /**
     * TODO: Error cases need to thought out.
     * TODO: How does this affect UNBOUNDED ARRAY
     * Assigns the corresponding ResultArray.ValueList<ResultValue>'s to copy of the ResultValue scalar.
     * <p>
     * @param symbol - Key for the ResultArray (Array Variable name in Havabol)
     * @param scalar - The ResultValue that will be stored in all indices of a FIXED ARRAY.
     *               - The ResultValue that will be stored in all null indices of an UNBOUNDED ARRAY. ???
     */
    void scalarAssign(Parser errParse, String symbol, ResultValue scalar) throws Exception
    {
        ResultArray resultArray;

        //Get a reference to the corresponding ResultArray Object
        resultArray = getResultArray(errParse, symbol);

        //Immediately indicate that this ResultArray has been scaled and is default scale value.
        resultArray.bScaled = true;
        resultArray.scaledValue = scalar;

        if(resultArray.structure == STIdentifier.UNBOUNDED_ARRAY)
        {

            //For each ResultValue in resultArray.ValueList, change it's value and type (just in case)
            for (int i = 0; i < resultArray.valueList.size(); i++)
            {
                //The ResultValue at ith position get's a copy of the Scalar ResultValue instead.
                resultArray.valueList.set(i, Utility.getResultValueCopy(scalar));
                //Set the ArrayList as bScaled = true and give it the default scaledValue
            }
        }
        else if(resultArray.structure == STIdentifier.FIXED_ARRAY)
        {

            //For each ResultValue in resultArray.ValueList, change it's value and type (just in case)
            //TODO: GET INFORMATION ABOUT MAXELEM AND CHANGE FOR LOOP CONDITION TO THE SIZE OF THE ANSWER FROM CLARK
            for (int i = 0; i < resultArray.valueList.size(); i++)
            {
                resultArray.valueList.set(i, Utility.getResultValueCopy(scalar));
                //Set the ArrayList as bScaled = true and give it the default scaledValue
            }
            //Store the altered resultArray back into the StorageManager HashTable.
            putResultArray(errParse, symbol, resultArray);
        }
        //Undefined resultArray.structure
        else
        {
            //Parser did not assign a structure to the array. - Not sure how this would ever happen.
            errParse.error("Internal Error: Array '%s' has undefined structure", symbol );
        }
    }

    /**
     * TODO: Error cases need to thought out.
     *  Does a FIXED ARRAY Array-to-Array assignment.
     *  <p>
     *  Example:
     *  A1 = [1, 2, 3]
     *  A2 = [11, 12, 13, 14]
     *  A2 = A1
     *  Result: A2 = [1, 2, 3, 14]
     * @param errParse     - Parser used for error handling
     * @param targetSymbol - Key for the Target (Left-Side) ResultArray for HashMap sm.
     * @param sourceSymbol - Key for the Source (Right-Side) ResultArray for HashMap sm.
     */
    void ArrayToArrayAssign(Parser errParse, String targetSymbol, String sourceSymbol) throws Exception
    {

        ResultArray target;
        target = getResultArray(errParse, targetSymbol);

        ResultArray source;
        source = getResultArray(errParse, sourceSymbol);

        //Target is Smaller, fill to target's size.
        if(target.valueList.size() < source.valueList.size())
        {
            //filling to target's size.
            for(int i = 0; i < target.valueList.size(); i++)
            {
                target.valueList.set(i, Utility.getResultValueCopy(source.valueList.get(i)));
            }
        }
        //Target Equal or Larger, fill to source's size.
        else if(target.valueList.size() >= source.valueList.size())
        {
            //filling to source's size.
            for(int i = 0; i < source.valueList.size(); i++)
            {
                target.valueList.set(i, Utility.getResultValueCopy(source.valueList.get(i)));
            }
        }
    }
}
