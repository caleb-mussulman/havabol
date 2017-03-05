    package havabol;

public class ResultValue
{
    
    int type;              // data type of the result
    String value;          // value of the result
    int structure;         // primitive, fixed array, unbounded array
    String terminatingStr; // used for end of lists of things (e.g., a list
                           // of statements might be terminated by "endwhile")
    
    // Constants for data type of result are located in Token's OPERAND subclassifications
    /*
     * public static final int INTEGER = 2; // integer constant
     * public static final int FLOAT   = 3; // float constant
     * public static final int BOOLEAN = 4; // boolean constant
     * public static final int STRING  = 5; // string constant
     * public static final int DATE    = 6; // date constant
     * public static final int VOID    = 7; // void
     */
    
    // Constants for data structure are located in STIdentifier
    /*
     * public static final int PRIMITVE        = 15; // primitive value structure
     * public static final int FIXED_ARRAY     = 16; // fixed size array structure
     * public static final int UNBOUNDED_ARRAY = 17; // unbounded array structure
     */
    /**
     * Creates a ResultValue object and initializes it with dummy values
     * <p>
     * ResultValue will be the result of many subroutines for the
     * recursive descent parser. The dummy values are to help catch
     * programming mistakes when making a ResultValue object in other
     * subroutines (i.e. comparing the result type without initializing
     * it first).
     */
    ResultValue(){
        // Initialize with dummy values
        type = -1;
        value = "";
        structure = -1;
        terminatingStr = "";
    }
}
