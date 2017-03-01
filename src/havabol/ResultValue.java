package havabol;

public class ResultValue
{
    
    int type;
    String value;
    int structure;
    String terminatingStr;
    
    public static final int INTEGER  = 2; // integer constant
    public static final int FLOAT    = 3; // float constant
    public static final int BOOLEAN  = 4; // boolean constant
    public static final int STRING   = 5; // string constant
    public static final int DATE     = 6; // date constant
    public static final int VOID     = 7; // void
    
    public static final int PRIMITVE        = 15; // primitive value structure
    public static final int FIXED_ARRAY     = 16; // fixed size array structure
    public static final int UNBOUNDED_ARRAY = 17; // unbounded array structure
    
    ResultValue(){
        // Initialize with dummy values
        type = -1;
        value = "";
        structure = -1;
        terminatingStr = "";
    }
}
