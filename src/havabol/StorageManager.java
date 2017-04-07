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
        //Need to be able to test this... not sure if works as intended...
        sm.put(symbol,value);
    }
    
    /**
     * The function that will be used to get the value of the passed
     * symbol (variable) from the prase(?) to retrieve the value 
     * of the symbol(variable).
     * <p>
     * Quickly does a hash lookup for the passed String symbol and returns
     * the value as a string...
     * Conversion will be done by numeric class
     * @param symbol - Key for the ResultValue (Variable name in Havabol)
     * @return       - ResuultValue Object Reference from HashTable sm.
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
     * TODO: MAY BE REMOVED
     * @param errParse    - ...
     * @param symbol      - ...
     * @param resultArray - ...
     * @throws Exception  - ...
     */
    void putResultArray(Parser errParse, String symbol, ResultArray resultArray) throws Exception
    {
        sm.put(symbol, resultArray);
    }

    /**
     * TODO: MAY BE REMOVED
     * Gets a reference to the ResultArray Object within the HashTable sm.
     * <p>
     * @param symbol - Key for the ResultArray (Array Variable name in Havabol)
     * @return       - ResultArray Object Reference from HashTable sm.
     */
    ResultArray getResultArray(Parser errParse, String symbol) throws Exception
    {
        ResultArray resultArray;
        resultArray = (ResultArray) sm.get(symbol);
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
     * @param errParse
     * @param symbol
     * @param resultValue
     * @param index
     * @throws Exception
     */
    void arrayAssign(Parser errParse, String symbol, ResultValue resultValue, ResultValue index) throws Exception {

        ResultArray resultArray;
        resultArray = getResultArray(errParse, symbol);

        //We must get the iIndex (subscript) value from the ResultValue index
        int iIndex = Integer.parseInt(index.value);

        //If the array is FIXED and we are attempting to assign out of bounds of declared maxElem.
        if((resultArray.structure == STIdentifier.FIXED_ARRAY) && (iIndex > resultArray.maxElem-1))
        {
            errParse.error("Assignment of '%s' to '%s'['%s'] is out of bounds ", resultValue.value, symbol, index.value);
        }
        //Setting a value to an index that is beyond the contiguous size of the array.
        if(iIndex >= resultArray.valueList.size())
        {
            //Pad the arrayList with null values to initialize an index that's beyond continuous space.
            for(int i = resultArray.valueList.size(); i <= iIndex; i++)
            {
                resultArray.valueList.add(i,null);
            }
            resultArray.valueList.set(iIndex, resultValue);
        }
        //TODO: Negative subscripts...
        //Any other index range.
        else
        {
            //Assign the passed value to the index in the array.
            resultArray.valueList.set(iIndex, resultValue);
        }

        //Store the resultArray in the StorageManager
        putResultArray(errParse, symbol, resultArray);

    }

    /**
     * TODO: MAY BE CHANGED - USES POSSIBLE REMOVED FUNCTIONS
     * TODO: Error cases need to thought out.
     * TODO: How does this affect UNBOUNDED ARRAY?
     * Assigns the corresponding ResultArray.ValueList<ResultValue>'s to copy of the ResultValue scalar.
     * <p>
     * @param symbol - Key for the ResultArray (Array Variable name in Havabol)
     * @param scalar - The ResultValue that will be stored in all indices of a FIXED ARRAY.
     *               - The ResultValue that will be stored in all null indices of an UNBOUNDED ARRAY. ???
     */
    void scalarAssign(Parser errParse, String symbol, ResultValue scalar)throws Exception
    {
        //TODO: Additions of bScaled and ScaledValue
        ResultArray resultArray;

        //Get a reference to the corresponding ResultArray Object
        resultArray = getResultArray(errParse, symbol);

        if(resultArray.structure == STIdentifier.UNBOUNDED_ARRAY)
        {
            //if the valueList is empty
            if(resultArray.valueList.isEmpty())
            {
                //TODO: What happens here? -- bScaled and ScaledValue
            }
            //For each ResultValue in resultArray.ValueList, change it's value and type (just in case)
            for (int i = 0; i < resultArray.valueList.size(); i++)
            {
                //TODO: Attempted to use UTILITY.getResultValueCopy(Scalar) ... Doesn't work.
                resultArray.valueList.get(i).value = scalar.value;
                resultArray.valueList.get(i).type = scalar.type;
                //structure is not going to be of scalar.structure.
                //terminatingStr is not going to be of scalar.terminatingStr.
            }
            //Store the altered resultArray back into the StorageManager HashTable.
            putResultArray(errParse, symbol, resultArray);
        }
        else if(resultArray.structure == STIdentifier.FIXED_ARRAY)
        {

            //For each ResultValue in resultArray.ValueList, change it's value and type (just in case)
            for (int i = 0; i < resultArray.valueList.size(); i++)
            {
                //TODO: Attempted to use UTILITY.getResultValueCopy(Scalar) ... Doesn't work.
                resultArray.valueList.get(i).value = scalar.value;
                resultArray.valueList.get(i).type = scalar.type;
                //structure is not going to be of scalar.structure.
                //terminatingStr is not going to be of scalar.terminatingStr.
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
     * TODO: MAY BE CHANGED - USES POSSIBLE REMOVED FUNCTIONS
     * TODO: Error cases need to thought out.
     * TODO: How does this work with UNBOUNDED ARRAY?
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
                target.valueList.set(i, source.valueList.get(i));
            }
        }
        //Target Equal or Larger, fill to source's size.
        else if(target.valueList.size() >= source.valueList.size())
        {
            //filling to source's size.
            for(int i = 0; i < source.valueList.size(); i++)
            {
                target.valueList.set(i, source.valueList.get(i));
            }
        }
    }
}
