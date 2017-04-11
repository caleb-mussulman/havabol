package havabol;

import java.util.ArrayList;


/**
 * ResultArray is used to store all Havabol arrays as ArraList<ResultValue>
 *
 * ResultArray is a subclass of ResultValue - This decision was made in order to allow
 * storage of ResultArray objects as ResultValues within StorageManager
 *
 * Attributes:
 *  valueList - An ArrayList of ResultValue's
 *  maxElem   - The Maximum Declared sized of an Array in Havabol
 *  scaled    - Possibly used to keep track if this ResultArray object has been set to a scalar in previous Havabol code.
 *
 * Super Class Attributes:
 *  type           - INT, FLOAT, ...
 *  structure      - PRIMITIVE, FIXED or UNBOUNDED
 *  value          - used for nothing (may be used to store symbol if we so choose)
 *  terminatingStr - May be used
 */
public class ResultArray extends ResultValue
{

    //TODO: Does a constructor need to be made for this class?
    ArrayList<ResultValue> valueList = new ArrayList<>();
    int maxElem;
    ResultValue scaledValue;
    boolean bScaled = false;

}
