package havabol;

public class Token
{
    public String tokenStr = "";
    public int primClassif = 0;
    public int subClassif = 0;
    public int iSourceLineNr = 0;
    public int iColPos = 0;
    public int identifierType = -1;
    // Constants for primClassif
    public static final int OPERAND    = 1; // constants, identifier
    public static final int OPERATOR   = 2; // + - * / < > = ! 
    public static final int SEPARATOR  = 3; // ( ) , : ; [ ]
    public static final int FUNCTION   = 4; // TBD
    public static final int CONTROL    = 5; // TBD
    public static final int EOF        = 6; // EOF encountered
    public static final int RT_PAREN   = 7; // special ')' for delimiting functions
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
    // Constants for OPERATOR's subclassif (number of operands)
    public static final int UNARY      = 15;
    public static final int BINARY     = 16;
    // Constants for determining if an operand is an array or array element
    public static final int ARRAY_REF    = 41;
    public static final int ARRAY_ELEM   = 42;
    public static final int NOT_AN_ARRAY = 43;
    
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
            , "IDENTIFIER"  // 1
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
            case Token.OPERATOR:
                if(subClassif == UNARY)
                    subClassifStr = "UNARY";
                else
                    subClassifStr = "BINARY";
                break;
            default:
                subClassifStr = "-";
        }
        
        System.out.printf("%-11s %-12s ", primClassifStr, subClassifStr);
        
        // If token is a string, print out extra line containing hex value for
        // any possible non-printable characters in the string
        if(subClassif == Token.STRING)
        {
            hexPrint(41, tokenStr);
        }
        else
        {
            System.out.printf("%s\n", tokenStr);
        }
    }
    
    /**
     * Prints a string that may contain non-printable characters as two lines.  
     * <p>
     * On the first line, it prints printable characters by simply
     * printing the character.  For non-printable characters
     * in the string, it prints ". ".  
     * <p>
     * The second line prints a two character hex value for the non printable
     * characters in the string line.  For the printable characters, it prints 
     * a space.
     * <p>
     * It is sometimes necessary to print the first line on the end of
     * an existing line of output.  This would make it difficult to properly 
     * align the second line of output.  The indent parameter is for indenting 
     * the second line.
     * <p><blockquote><pre>
     * Example for the string "\tTX\tTexas\n"
     *      . TX. Texas.
     *      09  09     0A
     * </pre></blockquote><p>    
     * @param indent  the number of spaces to indent the second printed line
     * @param str     the string to print which may contain non-printable characters
    */
    public void hexPrint(int indent, String str)
    {
        int len = str.length();
        char [] charray = str.toCharArray();
        char ch;
        boolean bFoundUnprintable = false;
        // print each character in the string
        for (int i = 0; i < len; i++)
        {
            ch = charray[i];
            if (ch > 31 && ch < 127)   // ASCII printable characters
                System.out.printf("%c", ch);
            else
            {
                bFoundUnprintable = true;
                System.out.printf(". ");
            }
        }
        
        // Print the line containing the unprintable character's value
        // only if one of these characters was in the string
        if(bFoundUnprintable)
        {
            System.out.printf("\n");
            // indent the second line to the number of specified spaces
            for (int i = 0; i < indent; i++)
            {
                System.out.printf(" ");
            }
            // print the second line.  Non-printable characters will be shown
            // as their hex value.  Printable will simply be a space
            for (int i = 0; i < len; i++)
            {
                ch = charray[i];
                // only deal with the printable characters
                if (ch > 31 && ch < 127)   // ASCII printable characters
                    System.out.printf(" ", ch);
                else
                    System.out.printf("%02X", (int) ch);
            }   
        }
        System.out.printf("\n");
    }

    /**
     * Returns the precedence for the token if the token is
     * not yet in the post-fix stack
     * <p>
     * See Token.getPrecedence for more information
     * @param  parse            the parser so its error methods can be used
     * @return                  the precedence value
     * @throws ParserException  if this method was called with an invalid token
     */
    public int precedence(Parser parse) throws ParserException
    {
        return getPrecedence(parse, false);
    }
    
    /**
     * Returns the precedence for the token if the token is
     * already in the post-fix stack
     * <p>
     * See Token.getPrecedence for more information
     * @param  parse            the parser so its error methods can be used
     * @return                  the precedence value
     * @throws ParserException  if this method was called with an invalid token
     */
    public int stkPrecedence(Parser parse) throws ParserException
    {
        return getPrecedence(parse, true);
    }
    
    /**
     * Determines the precedence of the given operator
     * <p>
     * This method is used by Parser.expr() to determine the precedence of
     * an operator/separator. When evaluating an infix expression, the expression
     * is converted to a post-fix stack first. In order to properly parse the
     * infix expression, the tokens may have different precedence values
     * depending on whether they are already on the post-fix stack or not.
     * If the boolean 'stackPrecedence' is passed in with 'true', then return
     * the precedence value for the token as if it were already on the post-fix
     * stack; otherwise, return its other precedence value. The list of
     * precedences are as follows:
     * <p>
     * -----------------------------------------------------
     *      Symbol      Token Precedence    Stack Precedence
     * -----------------------------------------------------
     *      [           16                  0
     *      (           15                  2
     *      u-          12                  12
     *      ^           11                  10
     *      * /         9                   9
     *      + -         8                   8
     *      #           7                   7
     *      < > <= >=   6                   6
     *      == !=       6                   6
     *      in notin    6                   6
     *      not         5                   5
     *      and or      4                   4
     * -----------------------------------------------------
     * @param  parse            the parser so its error methods can be used
     * @param stackPrecedence   boolean to determine if the precedence is for
     *                          the token on the post-fix stack 
     * @return                  the precedence value
     * @throws ParserException  if this method was called with an invalid token
     */
    private int getPrecedence(Parser parse, boolean bStackPrecedence) throws ParserException
    {
        // We should only call this method if the token is an operator or separator
        if(! ((this.primClassif == Token.OPERATOR) || (this.primClassif == Token.SEPARATOR)) )
        {
            // User's havabol code should NEVER get this error. This error is for debugging and
            // if it occurs, it is because we wrote an improper call to this method
            String diagnosticTxt = String.format("Improper call to 'Token.precedence' for token '%s' that has primary"
                    + "classification '%s'", this.tokenStr, Token.strPrimClassifM[this.primClassif]);
            throw new ParserException(this.iSourceLineNr + 1, diagnosticTxt, "");
        }
        // Determine which operator the token is, and return its corresponding precedence value
        switch(this.tokenStr)
        {
            case "[":
                // Precedence if '[' is already in post-fix stack
                if(bStackPrecedence)
                {
                    return 0;
                }
                // Precedence if '[' is not yet in post-fix stack
                else
                {
                    return 16;
                }
            case "(":
                // Precedence if '(' is already in post-fix stack
                if(bStackPrecedence)
                {
                    return 2;
                }
                // Precedence if '(' is not yet in post-fix stack
                else
                {
                    return 15;
                }
            case "-":
                // Unary minus
                if(this.subClassif == Token.UNARY)
                {
                    return 12;
                }
                // Binary minus
                else
                {
                    return 8;
                }
            case "^":
                // Precedence if '^' is already in post-fix stack
                if(bStackPrecedence)
                {
                    return 10;
                }
                // Precedence if '^' is not yet in post-fix stack
                else
                {
                    return 11;
                }
            case "*":
            case "/":
                return 9;
            case "+":
                return 8;
            case "#":
                return 7;
            case "<":
            case ">":
            case "<=":
            case ">=":
            case "==":
            case "!=":
            case "in":
            case "notin":
                return 6;
            case "not":
                return 5;
            case "and":
            case "or":
                return 4;
            default:
                // This should really never be reached. If it is, we have improperly called this function
                parse.errorLineNr(this.iSourceLineNr + 1, "Invalid operator/separator on call to"
                                  + "Token.getPrecedence, found '%s'", this.tokenStr);
                return -1; // Never reached
        }
    }
    
    /**
     * Creates a ResultValue object from the current instance of the token
     * <p>
     * This method will create a ResultValue object, set its data type,
     * value, and structure according to the token's, and return that
     * resulting object
     * @param  parse - the parser so its error methods can be used
     * @return - the ResultValue that contains the token's values
     * @throws ParserException - if this method was called for a token that
     *                         - is not an operand
     *TODO ASSIGNING STRUCTURE AS PRIMITIVE FOR PROGRAM 3 (MAY NEED TO CHANGE)
     */
    public ResultValue toResultValue(Parser parse) throws ParserException
    {
        ResultValue resVal = new ResultValue(); // the result value to be returned
        // We should only call this method if the token is an operand
        if(! (this.primClassif == Token.OPERAND))
        {
            // User's havabol code should NEVER get this error. This error is for debugging and
            // if it occurs, it is because we wrote an improper call to this method
            parse.errorLineNr(this.iSourceLineNr + 1, "Improper call to 'Token.toResultValue' for token '%s' that has primary"
                              + "classification '%s'", this.tokenStr, Token.getType(parse, this.primClassif));
        }
        // Assign the data type
        resVal.type = this.subClassif;
        // Assign the value
        resVal.value = this.tokenStr;
        // Assign the structure (primitive for program 3, don't yet know how we will determine the structure)
        resVal.structure = STIdentifier.PRIMITVE;
        return resVal;
    }
    
    /**
     * Gets the string name for an operand based on the constant passed on
     * <p>
     * This method is passed in an int enumeration type that corresponds to
     * the defined constants for operands. If that value is not within the
     * range of the valid enumerated operands, then we will error out. This
     * will really only happen when we try to get the enumerated value of
     * some ResultValue that has not been initialized. So, this method is
     * primarily for consolidating on that error check
     * @param parse             the parse instance so its error methods can be used
     * @param enumType          the enumerated value of the type of operand
     * @return                  the string value of the corresponding enumeration
     * @throws ParserException  if the integer enumeration is not valid
     */
    public static String getType(Parser parse, int enumType) throws ParserException
    {
        // Make sure that the enumerated type is a valid value
        if((enumType < Token.OPERAND_SUB_CLASS_MIN) || (enumType > Token.OPERAND_SUB_CLASS_MAX))
        {
            parse.error("Can not access the enumerated type for an operand with enum value '%d'", enumType);
        }
        
        // It is a valid value so get its corresponding string value
        return Token.strSubClassifM[enumType];
    }
}
