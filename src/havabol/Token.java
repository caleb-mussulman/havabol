package havabol;

public class Token
{
    public String tokenStr = "";
    public int primClassif = 0;
    public int subClassif = 0;
    public int iSourceLineNr = 0;
    public int iColPos = 0;
    // Constants for primClassif
    public static final int OPERAND = 1;    // constants, identifier
    public static final int OPERATOR = 2;   // + - * / < > = ! 
    public static final int SEPARATOR = 3;  // ( ) , : ; [ ]
    public static final int FUNCTION = 4;   // TBD
    public static final int CONTROL = 5;    // TBD
    public static final int EOF = 6;        // EOF encountered
    public static final int RT_PAREN = 7;   // TBD
    // Constants for OPERAND's subClassif
    public static final int IDENTIFIER = 1;
    public static final int INTEGER    = 2; // integer constant
    public static final int FLOAT      = 3; // float constant
    public static final int BOOLEAN    = 4; // boolean constant
    public static final int STRING     = 5; // string constant
    public static final int DATE       = 6; // date constant
    public static final int VOID       = 7; // void
    // Constants for CONTROL's subClassif  (after Pgm 1)
    public static final int FLOW       = 10;// flow statement (e.g., if)
    public static final int END        = 11;// end statement (e.g., endif)
    public static final int DECLARE    = 12;// declare statement (e.g., Int)
    // Constants for FUNCTION's subClassif (definedby)
    public static final int BUILTIN    = 13;// builtin function (e.g., print)
    public static final int USER       = 14;// user defined
    
    // array of primClassif string values for the constants
    public static final String[] strPrimClassifM = 
        {"Undefined"
            , "OPERAND"     // 1
            , "OPERATOR"    // 2
            , "SEPARATOR"   // 3
            , "FUNCTION"    // 4
            , "CONTROL"     // 5
            , "EOF"         // 6
        };
    public static final int PRIM_CLASS_MAX = 6;
    // array of subClassif string values for the constants
    public static final String[] strSubClassifM = 
        {"Undefined"
            , "IDENTFIER"   // 1
            , "INTEGER"     // 2
            , "FLOAT"       // 3
            , "BOOLEAN"     // 4
            , "STRING"      // 5
            , "DATE"        // 6
            , "Void"        // 7
            , "**not used**"// 8
            , "**not used**"// 9 
            , "FLOW"        //10
            , "END"         //11
            , "DECLARE"     //12
        }; 
    public static final int OPERAND_SUB_CLASS_MIN = 1;
    public static final int OPERAND_SUB_CLASS_MAX = 7;
    public static final int CONTROL_SUB_CLASS_MIN = 10;
    public static final int CONTROL_SUB_CLASS_MAX = 12;
 
    public Token(String value)
    {
        this.tokenStr = value;
        // ??
    }
    public Token()
    {
        this("");   // invoke the other constructor
    }
    
    public void printToken()
    {
        String primClassifStr;
        String subClassifStr;
        // convert the primClassif to a string
        if (primClassif >= 0 
            && primClassif <= PRIM_CLASS_MAX)
            primClassifStr = strPrimClassifM[primClassif];
        else
            primClassifStr = "**garbage**";

        // convert the subClassif to a string
        switch(primClassif)
        {
            case Token.OPERAND:
                if (subClassif >= OPERAND_SUB_CLASS_MIN 
                        && subClassif <= OPERAND_SUB_CLASS_MAX)
                    subClassifStr = strSubClassifM[subClassif];
                else    
                    subClassifStr = "**garbage**";
                break;
            case Token.CONTROL:
                if (subClassif >= CONTROL_SUB_CLASS_MIN 
                        && subClassif <= CONTROL_SUB_CLASS_MAX)
                    subClassifStr = strSubClassifM[subClassif];
                else    
                    subClassifStr = "**garbage**";
                break;
            case Token.FUNCTION:
                if (subClassif == BUILTIN)
                    subClassifStr = "BUILTIN";
                else if (subClassif == USER)
                    subClassifStr = "USER";
                else
                    subClassifStr = "**garbage**";
                break;    
            default:
                subClassifStr = "-";
        }
    
        System.out.printf("%-11s %-12s %s\n"
            , primClassifStr
            , subClassifStr
            , tokenStr);
    }
}
