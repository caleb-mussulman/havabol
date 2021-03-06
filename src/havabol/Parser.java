package havabol;

import java.util.*;

public class Parser
{
    public String sourceFileNm;
    public Scanner scan;
    public SymbolTable symbolTable;
    public int iParseTokenLineNr;
    public boolean bShowAssign; // Determines whether or not to print the variable and value
                                // of the current assignment
    public boolean bShowExpr; // Determines whether or not to print the result of the current
                              // expression (given that there was at least one operator)
    public boolean bShowPostfix; // Determines whether or not to print the result of the post-fix list
    public boolean bGettingArraySize; // Used when calling 'expr' to parse the declared size of the array
                                      // Indicates that we want to evaluate the expression in the brackets
                                      // and not the array element reference
    public boolean bCalledExprFromStmts; // If 'expr' is called from 'statements', then we are on the current token
                                         // and do not want to call scanner to get the next token
    // The following two lists are used as delimiters for 'expr'
    public final static List<String> assignmentTokens = Collections.unmodifiableList(Arrays.asList("=", "+=", "-=", "*=", "/="));
    public final static List<String> exprDelimiters   = Collections.unmodifiableList(Arrays.asList(":", ";")); // The delimiters for an expression
    
    Parser(Scanner scan, SymbolTable symbolTable)
    {
        this.scan = scan;
        this.symbolTable = symbolTable;
        this.sourceFileNm = scan.sourceFileNm;
        this.bShowAssign = false;
        this.bShowExpr = false;
        this.bShowPostfix = false;
        this.bGettingArraySize = false;
    }
    
    // This is a temporary method so we can still see the token output
    public void parse() throws Exception
    {
        ResultValue resStmtsReturn;
        resStmtsReturn = statements(true);
        
        // Check that execution ended from EOF
        if(resStmtsReturn.type != Token.EOF)
        {
            error("Unexpected control token, found '%s'", resStmtsReturn.terminatingStr);
        }
    }
    
    /**
     * A method for handling errors and exiting execution while parsing
     * <p>
     * The error method is simply for making error handling easier while
     * programming other parts of the code. It takes a string format and a variable
     * number of arguments depending on the format string and then throws a
     * ParserException using these values. It takes a specific line number, in case
     * the current line number would not be accurate for the error.
     * @param iLineNr    the line number indicating where the error occured
     * @param format     the format string to print out
     * @param varArgs    the corresponding values to match with the format
     *                   specifiers in the format string
     * @throws ParserException well, yeah...
     */
    public void errorLineNr(int iLineNr, String format, Object... varArgs) throws ParserException
    {
        String diagnosticTxt = String.format(format, varArgs);
        throw new ParserException(iLineNr + 1, diagnosticTxt, this.sourceFileNm);
    }
    
    /**
     * A simpler method for handling errors with the current token, and 
     * exiting execution while parsing.
     * <p>
     * This is similar to 'error' method, excpet that the line number is for the
     * current token being parsed, not scanned. This is primarily for accuracy of
     * error messages when parsing large expressions, where the error should be
     * for the current operator being evaluated, rather than the last token parsed
     * <p>
     * Simply calls errorLineNr with the line number of the current operator
     * being evaluated to create the error message. This is just for making
     * coding errors simpler
     * @param format     the format string to print out
     * @param varArgs    the corresponding values to match with the format
     *                   specifiers in the format string
     * @throws ParserException well, yeah...
     */
    public void errorWithCurrent(String format, Object... varArgs) throws ParserException
    {
        errorLineNr(this.iParseTokenLineNr, format, varArgs);
    }
    
    /**
     * A simpler method for handling errors and exiting execution while parsing
     * <p>
     * Simply calls errorLineNr with the scanner's current line number to create
     * the error message. This is just for making coding errors simpler
     * @param format     the format string to print out
     * @param varArgs    the corresponding values to match with the format
     *                   specifiers in the format string
     * @throws ParserException well, yeah...
     */
    public void error(String format, Object... varArgs) throws ParserException
    {
        errorLineNr(scan.currentToken.iSourceLineNr, format, varArgs);
    }
    
    /**
     * Determines the type of statement and invokes the corresponding subroutine
     * <p>
     * Gets the next token and determines which subroutine to call based on
     * that token. If there is no valid subroutine for that token, then error
     * @param bExec      whether or not we will execute the statements
     * @throws Exception if the given statement is an unknown type
     */
    public ResultValue statements(boolean bExec) throws Exception
    {
        ResultValue resValue = new ResultValue();
        
        // Keep executing statements until EOF or FLOW END
        while(true){
            // Get the next token
            scan.getNext();
            
            // We hit the end of file
            if(scan.currentToken.primClassif == Token.EOF)
            {
                // Return EOF token
                resValue.type = Token.EOF;
                resValue.terminatingStr = "";
                return resValue;
            }
            
            // Check if the current token is a end of flow token
            if((scan.currentToken.primClassif == Token.CONTROL) && (scan.currentToken.subClassif == Token.END))
            {
                resValue.type = Token.CONTROL;
                resValue.terminatingStr = scan.currentToken.tokenStr;
                return resValue;
            }
        
            // Current token is start of if statement
            if(scan.currentToken.tokenStr.equals("if"))
            {
                ifStmt(bExec);
            }
            // Current token is start of while statement
            else if(scan.currentToken.tokenStr.equals("while"))
            {
                whileStmt(bExec);
            }
            // Current token is start of for statement
            else if(scan.currentToken.tokenStr.equals("for"))
            {
                forStmt(bExec);
            }
            // Current token is start of assignment statement
            else if((scan.currentToken.primClassif == Token.OPERAND) && (scan.currentToken.subClassif == Token.IDENTIFIER))
            {
                assignStmt(bExec);
            }
            // Current token is start of declaration statement
            else if((scan.currentToken.primClassif == Token.CONTROL) && (scan.currentToken.subClassif == Token.DECLARE))
            {
                declareStmt(bExec);
            }
            // Current token is the start of a function call
            else if(scan.currentToken.primClassif == Token.FUNCTION)
            {
                // Save the name of the function for error message
                String functionName = scan.currentToken.tokenStr;
                
                // Function is a built-in function
                if(scan.currentToken.subClassif == Token.BUILTIN)
                {
                    // If we are not executing, then skip the function call
                    if(! bExec)
                    {
                        skipTo(scan.currentToken.iSourceLineNr, scan.currentToken.tokenStr, ";");
                    }
                    // Otherwise, execute the appropriate function
                    else
                    {
                        int iFunctionLineNr = scan.currentToken.iSourceLineNr; // line number that function call occurs on
                        switch(functionName)
                        {
                            case "debug":
                                debug();
                                break;
                            default:
                                // Handle the parsing of these functions in 'expr', but indicate that we are on the first
                                // token of the expression, so 'expr' should not call scanner for the next token
                                bCalledExprFromStmts = true;
                                expr();
                                bCalledExprFromStmts = false;
                                // Check that the function statement ended with ';'
                                if(! scan.currentToken.tokenStr.equals(";"))
                                {
                                    errorLineNr(iFunctionLineNr, "Expected ';' after call to function '%s'", functionName);
                                }
                                break;
                        }
                    }
                }
            }
            // Current token is start of undefined statement
            else
            {
                error("Unknown statement type: '%s'", scan.currentToken.tokenStr);
            }
        }
    }
    
    /**
     * Parses an 'if' control block
     * Assumption: current token is on an 'if'
     * <p>
     * Called with a value, determining whether the code is executed or
     * not. If the code is to be executed, evaluates the conditional
     * expression and executes the code if the result is 'true'. If
     * the code is not to be executed, it will skip through the code
     * until it gets to the matching 'endif'
     * @param  bExec     indicates whether the code should be executed or ignored
     * @throws Exception if the 'if' statement is not ended with an 'endif'
     *                   missing ';' after the 'endif'
     */
    public void ifStmt(boolean bExec) throws Exception
    {
        ResultValue resTrueStmts;  // block of code after the 'if'
        ResultValue resFalseStmts; // block of code after the 'else'
        int iIfLineNr; // line number that the if statement starts on
        
        iIfLineNr = scan.currentToken.iSourceLineNr;
        
        // Do we need to evaluate the condition?
        if(bExec)
        {
            // We are executing (not ignoring)
            ResultValue resCond = expr();
            
            // The conditional expression should be delimited by ':'
            if(! scan.currentToken.tokenStr.equals(":"))
            {
                error("Expected ':' after 'if' conditional expression, found '%s'", scan.currentToken.tokenStr);
            }
            
            // The resulting condition must be a boolean value
            if(resCond.type != Token.BOOLEAN)
            {
                errorLineNr(iIfLineNr, "Expected a 'BOOLEAN' type for the evaluation of 'if' statement's condition"
                            + ", found '%s' type", Token.getType(this, resCond.type));
            }
            
            // Did the condition return true?
            if(resCond.value.equals("T"))
            {
                // Cond returned true, execute the statements after the 'if'
                resTrueStmts = statements(true);
                // What ended the statements after the true part? 'else:' or 'endif;'
                // If it is an 'else', ignore the statements after the 'else'
                if(resTrueStmts.terminatingStr.equals("else"))
                {
                    // Has an else so ignore statements after the else
                    
                    // 'else' must be followed by a ':'
                    if(! scan.getNext().equals(":"))
                    {
                        error("Expected ':' after 'else'");
                    }
                    resFalseStmts = statements(false);
                    
                    // 'if' control block must end with 'endif'
                    if(! resFalseStmts.terminatingStr.equals("endif"))
                    {
                        error("Expected 'endif' for 'if' beginning on line %d", iIfLineNr);
                    }
                }
                // If it is not an 'else', then it must be an 'endif'
                else if(! resTrueStmts.terminatingStr.equals("endif"))
                {
                    error("Expected 'endif' for 'if' beginning on line %d", iIfLineNr);
                }
                // 'endif' must be followed by a ';'
                if(! scan.getNext().equals(";"))
                {
                    error("Expected ';' after 'endif'");
                }
            }
            else
            {
                // Cond returned false, ignore the statements after the 'if'
                resTrueStmts = statements(false);
                // What ended the statements after the true part? 'else:' or 'endif;'
                // If it is an 'else', execute the statements after the 'else'
                if(resTrueStmts.terminatingStr.equals("else"))
                {
                    // Has an else so execute statements after the else
                    
                    // 'else' must be followed by a ':'
                    if(! scan.getNext().equals(":"))
                    {
                        error("Expected ':' after 'else'");
                    }
                    resFalseStmts = statements(true);
                    
                    // 'if' control block must end with 'endif'
                    if(! resFalseStmts.terminatingStr.equals("endif"))
                    {
                        error("Expected 'endif' for 'if' beginning on line %d", iIfLineNr);
                    }
                }
                // If it is not an 'else', then it must be an 'endif'
                else if(! resTrueStmts.terminatingStr.equals("endif"))
                {
                    error("Expected 'endif' for 'if' beginning on line %d", iIfLineNr);
                }
                // 'endif' must be followed by a ';'
                if(! scan.getNext().equals(";"))
                {
                    error("Expected ';' after 'endif'");
                }
            }
        }
        else
        {
            // We are ignoring execution
            
            // Skip the 'if' condition
            skipTo(iIfLineNr, "if", ":");
            
            // Ignore the true part
            resTrueStmts = statements(false);
            // What ended the statements after the true part? 'else:' or 'endif;'
            // If it is an 'else', ignore the statements after the 'else'
            if(resTrueStmts.terminatingStr.equals("else"))
            {
                // Has an else so ignore statements after the else
                
                // 'else' must be followed by a ':'
                if(! scan.getNext().equals(":"))
                {
                    error("Expected ':' after 'else'");
                }
                resFalseStmts = statements(false);
                
                // 'if' control block must end with 'endif'
                if(! resFalseStmts.terminatingStr.equals("endif"))
                {
                    error("Expected 'endif' for 'if' beginning on line %d", iIfLineNr);
                }
            }
            // If it is not an 'else', then it must be an 'endif'
            else if(! resTrueStmts.terminatingStr.equals("endif"))
            {
                error("Expected 'endif' for 'if' beginning on line %d", iIfLineNr);
            }
            // 'endif' must be followed by a ';'
            if(! scan.getNext().equals(";"))
            {
                error("Expected ';' after 'endif'");
            }
        }
    }
    
    /**
     * Parses a 'while' control block
     * Assumption: current token is on a 'while'
     * <p>
     * Called with a value, determining whether the code is executed or
     * not. If the code is to be executed, evaluates the conditional
     * expression and executes the code if the result is 'true'. It
     * will keep executing the code after the while statement as long as
     * the expression evaluates to 'true'. If the code is not to be
     * executed, it will skip through the code until it gets to the
     * matching 'endwhile'
     * expression
     * @param bExec      indicates whether the code should be executed or ignored
     * @throws Exception if the 'while' block is not ended with an 'endwhile'
     *                   missing ';' after the 'endwhile'
     */
    public void whileStmt(boolean bExec) throws Exception
    {
        ResultValue resStmts;
        Token whileToken;
        
        // Save the 'while' token
        whileToken = scan.currentToken;
        
        // Do we need to evaluate the condition?
        if(bExec)
        {
            // We are executing (not ignoring)
            ResultValue resCond = expr();
            
            // The conditional expression should be delimited by ':'
            if(! scan.currentToken.tokenStr.equals(":"))
            {
                error("Expected ':' after 'while' conditional expression, found '%s'", scan.currentToken.tokenStr);
            }
            
            // The resulting condition must be a boolean value
            if(resCond.type != Token.BOOLEAN)
            {
                errorLineNr(whileToken.iSourceLineNr, "Expected a 'BOOLEAN' type for the evaluation of 'while' statement's condition"
                           + ", found '%s' with type '%s'", resCond.value, Token.getType(this, resCond.type));
            }
            
            // Continue in the while loop as long as the expression evaluates to true
            while(resCond.value.equals("T"))
            {
                // Execute the statements after the 'while'
                resStmts = statements(true);
                // 'while' control block must end with 'endwhile'
                if(! resStmts.terminatingStr.equals("endwhile"))
                {
                    error("Expected 'endwhile' for 'while' beginning on line %d", whileToken.iSourceLineNr);
                }
                // 'endwhile' must be followed by a ';'
                if(! scan.getNext().equals(";"))
                {
                    error("Expected ';' after 'endwhile'");
                }
                // Go back to the top of the while loop
                scan.setPosition(whileToken);
                //Re-evaluate the expression
                resCond = expr();
            }
            
            // The expression was false so go to the 'endwhile'
            resStmts = statements(false);
        }
        else
        {
            // We are ignoring execution
            
            // Skip the 'while' condition
            skipTo(whileToken.iSourceLineNr, "while", ":");
            // Ignore the statements after the while
            resStmts = statements(false);
        }
        
        // 'while' control block must end with 'endwhile'
        if(! resStmts.terminatingStr.equals("endwhile"))
        {
            error("Expected 'endwhile' for 'while' beginning on line %d", whileToken.iSourceLineNr);
        }
        // 'endwhile' must be followed by a ';'
        if(! scan.getNext().equals(";"))
        {
            error("Expected ';' after 'endwhile'");
        }
    }
    
    /**
     * Parses a 'for' control block
     * Assumption: current token is on a 'for'
     * <p>
     * TODO add description
     * <p>
     * There are four different types of 'for' statements, each
     * indicated by the initializations after the 'for' token:
     *    1) for cv = sv to limit by incr: // counting for
     *    2) for char in string:
     *    3) for item in array:
     *    4) for stringCV from string by delimiter:
     * @param bExec
     * @throws ParserException
     */
    public void forStmt(boolean bExec) throws Exception
    {
        ResultValue resStmts = new ResultValue();
        Token forToken;

        // Save the 'for' token
        forToken = scan.currentToken;

        // Do we need to evaluate the parameters?
        if(bExec)
        {
            // We are executing (not ignoring)

            // Next token should be the control variable used in the loop
            scan.getNext();
            if(scan.currentToken.primClassif != Token.OPERAND || scan.currentToken.subClassif != Token.IDENTIFIER)
            {
                error("Expected a control variable after 'for', found '%s'", scan.currentToken.tokenStr);
            }
            String variableStr = scan.currentToken.tokenStr;
            
            // Get the token after the variable
            scan.getNext();
            
            // 1) If we have a '=', then this is a counting 'for' loop
            if(scan.currentToken.tokenStr.equals("="))
            {
                // Get the source value for the control variable
                ResultValue resSourceVal = expr();
                
                // The source value should be a primitive and coercible to an int type
                if(resSourceVal.structure != STIdentifier.PRIMITVE)
                {
                    error("Expected a primitive value to assign to '%s', found array '%s'", variableStr, resSourceVal.value);
                }
                Utility.coerce(this, Token.INTEGER, resSourceVal, "for loop control variable initialization");
                
                // The token after the source value should be 'to'
                if(! scan.currentToken.tokenStr.equals("to"))
                {
                    error("Expected 'to' after control variable initialization, found '%s'", scan.currentToken.tokenStr);
                }
                
                // Get the limit
                ResultValue resLimit = expr();
                
                // The limit value should be a primitive and coercible to an int type
                if(resLimit.structure != STIdentifier.PRIMITVE)
                {
                    error("Expected a primitive value as the 'for' loop limit, found array '%s'", resLimit.value);
                }
                Utility.coerce(this, Token.INTEGER, resLimit, "for loop limit value");
                
                ResultValue resIncr; // The amount to increment the control variable by
                
                // There may be a 'by' token after the limit value indicating the increment amount
                if(scan.currentToken.tokenStr.equals("by"))
                {
                    // Get the increment amount
                    resIncr = expr();
                    
                    // The increment amount expression should be delimited by ':'
                    if(! scan.currentToken.tokenStr.equals(":"))
                    {
                        error("Expected ':' after 'for' increment value, found '%s'", scan.currentToken.tokenStr);
                    }
                    
                    // The increment amount should be primitive and coercible to an int type
                    if(resIncr.structure != STIdentifier.PRIMITVE)
                    {
                        error("Expected a primitive value as the 'for' loop increment amount, found array '%s'", resIncr.value);
                    }
                    Utility.coerce(this, Token.INTEGER, resIncr, "for loop increment value");
                }
                // If there is no 'by' token, then increment by 1 each iteration
                else
                {
                    resIncr = new ResultValue();
                    resIncr.value = "1";
                    resIncr.type = Token.INTEGER;
                    resIncr.structure = STIdentifier.PRIMITVE;
                    
                    // The limit amount expression should be delimited by ':'
                    if(! scan.currentToken.tokenStr.equals(":"))
                    {
                        error("Expected ':' after 'for' limit value, found '%s'", scan.currentToken.tokenStr);
                    }
                }
                
                // Declare the control variable and initialize to the source value
                STIdentifier STControlVar = new STIdentifier(variableStr, Token.OPERAND, Token.INTEGER, STIdentifier.NOT_A_PARAMETER
                                                                        , STIdentifier.PRIMITVE, STIdentifier.LOCAL);
                symbolTable.putSymbol(variableStr, STControlVar);
                symbolTable.storeVariableValue(this, variableStr, resSourceVal);
                
                // Get the control variable's value and limit's value as numerics
                Numeric numControlVar = new Numeric(this, resSourceVal, "for", "control variable");
                Numeric numLimit = new Numeric(this, resLimit, "for", "limit value");
                Numeric numIncr = new Numeric(this, resIncr, "for", "increment value");
                
                // If the user specified the increment amount, it should have been a positive integer value
                if(numIncr.integerValue <= 0)
                {
                    error("The increment value for 'for' must be a positive integer, found '%s'", resIncr.value);
                }
                
                // Continue in the 'for' loop as long as 'controlVar < limit'
                while(numControlVar.integerValue < numLimit.integerValue)
                {
                    // Execute the statements after the 'for' parameters
                    resStmts = statements(true);
                    
                    // 'for' control block must end with 'endfor'
                    if(! resStmts.terminatingStr.equals("endfor"))
                    {
                        error("Expected 'endfor' for 'for' beginning on line %d", forToken.iSourceLineNr);
                    }
                    // 'for' must be followed by a ';'
                    if(! scan.getNext().equals(";"))
                    {
                        error("Expected ';' after 'endfor'");
                    }
                    
                    // The user may re-declare the control variable and change it's value or type
                    
                    // If the control variable was re-declared, check that it was initialized as well
                    // TODO Change this ugly code...please
                    ResultValue resControlVarVal = symbolTable.storageManager.sm.get(variableStr);
                    if(resControlVarVal == null)
                    {
                        error("Control variable '%s' was re-declared but not initialized", variableStr);
                    }
                    
                    // Check that the control variable is still primitive and of type int
                    if(resControlVarVal.structure != STIdentifier.PRIMITVE)
                    {
                        error("Control variable '%s' was redeclared as an array, must be primitive", variableStr);
                    }
                    Utility.coerce(this, Token.INTEGER, resControlVarVal, "for loop control variable comparison");
                    
                    // Get the numeric value of the control variable's value
                    numControlVar = new Numeric(this, resControlVarVal, "for", "control variable");
                    
                    // Add the increment value to it
                    numControlVar.integerValue += numIncr.integerValue;
                    resControlVarVal.value = Integer.toString(numControlVar.integerValue);
                    
                    // Move back to the 'for' and skip past the initialization of its parameters
                    scan.setPosition(forToken);
                    skipTo(forToken.iSourceLineNr, "for", ":");
                }
                
                // 'controlVar >= limit' so go to the 'endfor'
                resStmts = statements(false);
                
            }
            // Check for the second and third types of 'for' loops
            else if(scan.currentToken.tokenStr.equals("in"))
            {
                // Get the expression to iterate over, after the 'in' token
                ResultValue resExpr = expr();
                
                // The expression should be delimited by ':'
                if(! scan.currentToken.tokenStr.equals(":"))
                {
                    error("Expected ':' after expression following 'in', found '%s'", scan.currentToken.tokenStr);
                }
                
                // 2) If the result is a primitive, then the 'for' loop is character iteration over a string
                if(resExpr.structure == STIdentifier.PRIMITVE)
                {
                    // Save the string to iterate over
                    String strIterate = resExpr.value;
                    
                    // Execute the statements in the 'for' loop for each character of the string
                    for(int i = 0; i < strIterate.length(); i++)
                    {
                        // Get the current character of the string
                        ResultValue resChar = new ResultValue();
                        resChar.value = Character.toString(strIterate.charAt(i));
                        resChar.type = Token.STRING;
                        resChar.structure = STIdentifier.PRIMITVE;
                        
                        // Declare the variable and store the character as the value
                        STIdentifier STChar = new STIdentifier(variableStr, Token.OPERAND, Token.STRING, STIdentifier.NOT_A_PARAMETER
                                                                          , STIdentifier.PRIMITVE, STIdentifier.LOCAL);
                        symbolTable.putSymbol(variableStr, STChar);
                        symbolTable.storeVariableValue(this, variableStr, resChar);
                        
                        // Execute the statements after the 'for' parameters
                        resStmts = statements(true);
                        
                        // 'for' control block must end with 'endfor'
                        if(! resStmts.terminatingStr.equals("endfor"))
                        {
                            error("Expected 'endfor' for 'for' beginning on line %d", forToken.iSourceLineNr);
                        }
                        
                        // 'for' must be followed by a ';'
                        if(! scan.getNext().equals(";"))
                        {
                            error("Expected ';' after 'endfor'");
                        }
                        
                        // Move back to the 'for' and skip past the initialization of its parameters
                        scan.setPosition(forToken);
                        skipTo(forToken.iSourceLineNr, "for", ":");
                    }
                    // There are no more characters in the string, so go to the 'endfor'
                    resStmts = statements(false);
                }
                // 3) If the result is not a primitive, then the 'for' loop is element iteration over an array
                else
                {
                    // Save the returned expression as a result array type
                    ResultArray resArray = (ResultArray) resExpr;
                    
                    int iForIterateNum; // The number of times to iterate over through the array
                    
                    /* TODO After program 5 is turned in change for loops such that:
                     *      Fixed-size array: Iterate from 0 to max-size, if the element exists
                     *      Unbounded  array: Iterate from 0 to sizeof ArrayList, checking
                     *                        the ArrayList size every iteration
                     */
                    
                    // Get the number of elements currently in the array
                    iForIterateNum = 0;
                    for(int i = 0; i < resArray.valueList.size(); i++)
                    {
                        ResultValue resCurrentElem = resArray.valueList.get(i);
                        if(resCurrentElem != null)
                        {
                            iForIterateNum++;
                        }
                    }
                    
                    int iCurrentElementAmount = 0;
                    for(int i = 0; i < resArray.valueList.size() && iCurrentElementAmount < iForIterateNum; i++)
                    {
                        // Get the current element of the array
                        ResultValue resArrayElem = resArray.valueList.get(i);
                        
                        // Only iterate if there was actually an element at that index
                        if(resArrayElem != null)
                        {
                            iCurrentElementAmount++;
                            // Declare the previously given variable
                            STIdentifier STItem= new STIdentifier(variableStr, Token.OPERAND, Token.STRING, STIdentifier.NOT_A_PARAMETER
                                    , STIdentifier.PRIMITVE, STIdentifier.LOCAL);
                            symbolTable.putSymbol(variableStr, STItem);
                            
                            // Get a copy of the array's element and store it as the variable's value
                            ResultValue resArrayElemCopy = Utility.getResultValueCopy(resArrayElem);
                            symbolTable.storeVariableValue(this, variableStr, resArrayElemCopy);
                            
                            // Execute the statements after the 'for' parameters
                            resStmts = statements(true);
                            
                            // 'for' control block must end with 'endfor'
                            if(! resStmts.terminatingStr.equals("endfor"))
                            {
                                error("Expected 'endfor' for 'for' beginning on line %d", forToken.iSourceLineNr);
                            }
                            
                            // 'for' must be followed by a ';'
                            if(! scan.getNext().equals(";"))
                            {
                                error("Expected ';' after 'endfor'");
                            }
                            
                            // Move back to the 'for' and skip past the initialization of its parameters
                            scan.setPosition(forToken);
                            skipTo(forToken.iSourceLineNr, "for", ":");
                        }
                    }
                    // We iterated over all the valid elements, so go to the 'endfor'
                    resStmts = statements(false);
                }
            }
            // 4) If we have a 'from', then the 'for' loop is iteration over a string by a specified delimiter
            else if(scan.currentToken.tokenStr.equals("from"))
            {
                // Save the string to iterate over and check that it is primitive
                ResultValue resIterStr = expr();
                if(resIterStr.structure != STIdentifier.PRIMITVE)
                {
                    error("Expected a primitive string value to iterate over, found array '%s'", resIterStr.value);
                }
                
                // The token after the string expression should be 'by'
                if(! scan.currentToken.tokenStr.equals("by"))
                {
                    error("Expected 'by' after string expression following 'from', found '%s'", scan.currentToken.tokenStr);
                }
                
                // Save the delimiting string and check that it is primitive
                ResultValue resDelimStr = expr();
                if(resDelimStr.structure != STIdentifier.PRIMITVE)
                {
                    error("Expected a primitive string value as the delimiter, found array '%s'", resDelimStr.value);
                }
                
                // The expression should be delimited by ':'
                if(! scan.currentToken.tokenStr.equals(":"))
                {
                    error("Expected ':' after expression following 'by', found '%s'", scan.currentToken.tokenStr);
                }
                
                int iStartOfSubstring = 0;
                int iEndOfSubstring = -1;
                
                // In the case when the delimiting string is empty, we will iterate character by character
                // If the iteration string is also empty, then there are no characters, so we don't want to
                // enter the 'for' loop at all
                if(resIterStr.value.isEmpty() && resDelimStr.value.isEmpty())
                {
                    iStartOfSubstring = 1; // This will prevent entering the below loop
                }
                
                // Execute the statements after the 'for' parameters as long as there is
                // another copy of the delimiting string in the iteration string or we hit
                // the end of the iteration string (special case when iteration string is empty)
                while((iStartOfSubstring <= resIterStr.value.length()))
                {
                    // Declare the variable to store the string
                    STIdentifier STString = new STIdentifier(variableStr, Token.OPERAND, Token.STRING, STIdentifier.NOT_A_PARAMETER
                                                                        , STIdentifier.PRIMITVE, STIdentifier.LOCAL);
                    symbolTable.putSymbol(variableStr, STString);
                    
                    // Store the entire string
                    ResultValue resStringCV = new ResultValue();
                    resStringCV.type = Token.STRING;
                    resStringCV.structure = STIdentifier.PRIMITVE;
                    symbolTable.storeVariableValue(this, variableStr, resStringCV);
                    
                    // If the delimiting string is empty, we will iterate character by character
                    if(resDelimStr.value.isEmpty())
                    {
                        iEndOfSubstring = iStartOfSubstring + 1;
                        resStringCV.value = resIterStr.value.substring(iStartOfSubstring, iEndOfSubstring);
                        
                        // If we are at the last character of the string, need to increment by 2 so
                        // we don't try to get the character after the end of the string
                        if(iStartOfSubstring == (resIterStr.value.length() - 1))
                        {
                            iStartOfSubstring += 2;
                        }
                        // Otherwise, go to the next character
                        else
                        {
                            iStartOfSubstring += 1;
                        }
                    }
                    // Otherwise, get a substring, up to the index of the next delimiting string
                    else
                    {
                        iEndOfSubstring = resIterStr.value.indexOf(resDelimStr.value, iStartOfSubstring);
                        
                        // If there was no match or if we are on the last substring, the
                        // 'indexOf' will have been negative
                        if(iEndOfSubstring < 0)
                        {
                            iEndOfSubstring = resIterStr.value.length();
                        }
                        resStringCV.value = resIterStr.value.substring(iStartOfSubstring, iEndOfSubstring);
                        iStartOfSubstring = iEndOfSubstring + resDelimStr.value.length();
                    }
                    
                    // Execute the statements after the 'for' parameters
                    resStmts = statements(true);
                    
                    // 'for' control block must end with 'endfor'
                    if(! resStmts.terminatingStr.equals("endfor"))
                    {
                        error("Expected 'endfor' for 'for' beginning on line %d", forToken.iSourceLineNr);
                    }
                    
                    // 'for' must be followed by a ';'
                    if(! scan.getNext().equals(";"))
                    {
                        error("Expected ';' after 'endfor'");
                    }
                    
                    // Move back to the 'for' and skip past the initialization of its parameters
                    scan.setPosition(forToken);
                    skipTo(forToken.iSourceLineNr, "for", ":");
                }
                
                // The delimiter was the end of the string so go to the 'endfor'
                resStmts = statements(false);
            }
            // Current token does not match any 'for' loop types
            else
            {
                error("Expected '=', 'in', or 'from' after '%s', found '%s'", variableStr, scan.currentToken.tokenStr);
            }
        }
        else
        {
            // We are ignoring execution

            // Skip the 'for' parameters
            skipTo(forToken.iSourceLineNr, "for", ":");
            
            // Ignore the statements after the 'for' parameters
            resStmts = statements(false);
        }
        
        // 'for' control block must end with 'endfor'
        if(! resStmts.terminatingStr.equals("endfor"))
        {
            error("Expected 'endfor' for 'for' beginning on line %d", forToken.iSourceLineNr);
        }
        
        // 'endfor' must be followed by a ';'
        if(! scan.getNext().equals(";"))
        {
            error("Expected ';' after 'endfor'");
        }
    }
    
    /**
     * Executes an assignment statement
     * Assumption: current token is on an OPERAND IDENTIFIER
     * <p>
     * An assignment statement is of the form:
     *     assignStmt := variable assignmentOperator expression
     *     
     * This method will take the result of expression and assign
     * its value to variable. Currently can execute assignments
     * with operators "=", "-=", and "+=". A misspelled beginning
     * of a statement will usually error out here as the scanner will
     * default the first token to a variable token if it had not
     * matched anything else (this is documented below).
     * @param bExec      indicates whether the code should be executed or ignored
     * @throws Exception the first token is not a variable
     *                   missing a valid assignment operator after the variable
     *                   the statement is invalid
     *                   
     */
    public void assignStmt(boolean bExec) throws Exception
    {
        boolean bArrayElemAssign; // Indicates if this is an assignment to an array/string index (i.e., there
                                  // are a pair of brackets indicating an element reference)
        int iAssignLineNr; // line number for beginning of assignment statement
        iAssignLineNr = scan.currentToken.iSourceLineNr;
        
        // Do we need to ignore execution of the assignment?
        if(! bExec)
        {
            // We are ignoring execution
            // Skip to the end of the assignment statement
            skipTo(iAssignLineNr, "assignment", ";");
            return;
        }
        // Otherwise, we are executing the assignment
        
        // A check to make sure this method was called with the correct current token
        if((scan.currentToken.primClassif != Token.OPERAND) || (scan.currentToken.subClassif != Token.IDENTIFIER))
        {
            error("Expected a variable for the target of an assignment, found '%s'", scan.currentToken.tokenStr);
        }
        String variableStr = scan.currentToken.tokenStr;
        
        ResultValue resIndex = new ResultValue(); // If we are assigning to an array element,
                                                  // this will be the value of the index
        bArrayElemAssign = false; // If this is an array element assignment, we need to know
                                  // later on after we have parsed past the brackets
        
        // Check if the token after the variable is a '['
        if(scan.nextToken.tokenStr.equals("["))
        {
            // We found brackets
            bArrayElemAssign = true;
            
            // Get rid of the
            // This is an assignment to an array/string index, so we need to get the value of the index.
            // We do this by calling 'expr' and indicating that we want the value in between the brackets
            // and not the element of the array/string at that index.
            
            bGettingArraySize = true;
            resIndex = expr();
            this.iParseTokenLineNr = scan.currentToken.iSourceLineNr;
            Utility.coerce(this, Token.INTEGER, resIndex, "index of assignment target");
            bGettingArraySize = false;
        }
        // If there was a '[', the call to 'expr' will land the current token on what
        // should be the operator; if not, we need to move to that next token
        else
        {
            scan.getNext();
        }
        
        // We should currently be on an assignment operator
        if(scan.currentToken.primClassif != Token.OPERATOR)
        {
            /*
             *  Since the scanner's base case for classification is OPERAND IDENTIFIER,
             *  it may classify a misspelled token as a variable. For example, if a user
             *  accidentally types "printf" instead of "print", the scanner would see the
             *  token as a variable and the parser would see it as the first token of the
             *  statement and call this function. In this case the error would occur here,
             *  and may not necessarily be an error with a missing assignment operator
             */
            error("Either undefined statement or missing assignment operator, found '%s'", scan.currentToken.tokenStr);
        }
        
        // Get the target variable and check that it has been declared
        STIdentifier STVariable = (STIdentifier) symbolTable.getSymbol(variableStr);
        if(STVariable == null)
        {
            error("Variable '%s' has not been declared", variableStr);
        }
        
        String operatorStr = scan.currentToken.tokenStr;
        ResultValue resOp2;    // Result value of second operand
        ResultValue resOp1;    // Result value of first operand
        ResultValue resAssign; // Result Value to be assigned to variable
        switch(operatorStr)
        {
            case "=":
                // Different types of assignment
                // 1)   array = array
                // 2)   array = scalar
                // 3)   array[index] = scalar
                // 4)   string[index] = string
                // 5)   scalar = scalar
                
                // Get the source of the assignment
                resAssign = expr();
                
                // For the first, second, and third assignment types, the target will involve an array
                if(STVariable.structure != STIdentifier.PRIMITVE)
                {
                    // 1) If the source is an array, then we have array to array assignment
                    if(resAssign.structure != STIdentifier.PRIMITVE)
                    {
                        // Array assignment to an array element is undefined
                        if(bArrayElemAssign)
                        {
                            error("Assignment from array '%s' to array element '%s[%s]' is undefined"
                                  , resAssign.value, variableStr, resIndex.value);
                        }
                        
                        // Coercion takes place on each element within method 'ArrayToArrayAssign'
                        symbolTable.storageManager.ArrayToArrayAssign(this, variableStr, resAssign.value);
                    }
                    // The source is a primitive type
                    else
                    {
                        // 2) If there were no brackets after the target variable,
                        //    then this is scalar to array assignment
                        if(! bArrayElemAssign)
                        {
                            // Ensure that the source is the same type as the array
                            Utility.coerce(this, STVariable.dclType, resAssign, "=");
                            symbolTable.storageManager.scalarAssign(this, variableStr, resAssign);
                        }
                        // 3) There were brackets after the target variable,
                        //    so this is an assignment to an array index
                        else
                        {
                            // Ensure that the source is the same type as the array
                            Utility.coerce(this, STVariable.dclType, resAssign, "=");
                            symbolTable.storageManager.arrayAssignElem(this, variableStr, resAssign, resIndex);
                        }
                    }
                }
                // For the fourth and fifth assignment types, the target is a primitive type
                else
                {
                    // 4) If there were brackets after the target variable, it must be a string index
                    if(bArrayElemAssign)
                    {
                        // Indexing a primitive is only valid for string types
                        if(STVariable.dclType != Token.STRING)
                        {
                            error("Indexing of a primitive variable is only valid for type 'STRING'"
                                  + ", found variable of type '%s'", STVariable.dclType);
                        }
                        
                        // Ensure that the source is a primitive
                        if(resAssign.structure != STIdentifier.PRIMITVE)
                        {
                            error("Assignment from array '%s' to string index '%s[%s]' is undefined"
                                  , resAssign.value, variableStr, resIndex.value);
                        }
                        
                        // Ensure that the source is a string as well
                        Utility.coerce(this, STVariable.dclType, resAssign, "=");
                        // Get the string value of the variable
                        ResultValue resString = symbolTable.retrieveVariableValue(this, variableStr);
                        
                        // Get the index as a numeric value
                        Numeric numIndex = new Numeric(this, resIndex, variableStr, "index");
                        
                        // If the index is negative, convert to its corresponding positive subscript
                        if(numIndex.integerValue < 0)
                        {
                            numIndex.integerValue = numIndex.integerValue + resString.value.length();
                        }
                        
                        // Now determine if the index is within bounds for the string
                        if(numIndex.integerValue < 0 || numIndex.integerValue >= resString.value.length())
                        {
                            error("Index '%s' out of bounds for 'STRING' variable '%s' with value '%s'"
                                  , resIndex.value, variableStr, resString.value);
                        }
                        
                        // Get the part of the original string up until where the InvalidDateFirstParam strings starts (may be empty string)
                        String beginning = resString.value.substring(0, numIndex.integerValue);
                        
                        // Get the part of the original string after the end of the inserted string (may be empty string)
                        int iStartOfEnd = beginning.length() + resAssign.value.length();
                        String end = "";
                        if(iStartOfEnd < resString.value.length())
                        {
                            end = resString.value.substring(iStartOfEnd, resString.value.length()); 
                        }
                        
                        // Create the InvalidDateFirstParam string
                        resString.value = beginning + resAssign.value + end;
                    }
                    // 5) Otherwise, this is a regular assignment to a primitive
                    else
                    {
                        // Ensure that the source is a primitive
                        if(resAssign.structure != STIdentifier.PRIMITVE)
                        {
                            error("Assignment from array '%s' to primitive variable '%s' is undefined"
                                  , resAssign.value, variableStr);
                        }
                        
                        // Ensure that the value is the same type as the variable
                        Utility.coerce(this, STVariable.dclType, resAssign, "=");
                        symbolTable.storeVariableValue(this, variableStr, resAssign);
                    }
                }
                break;
                
            case "-=":
                // Get the second operand
                resOp2 = expr();
                
                // Check that the second operand is primitive
                if(resOp2.structure != STIdentifier.PRIMITVE)
                {
                    error("Operation '-=' expected a primitive source value, found array '%s'", resOp2.value);
                }
                
                // Check if the target was an array element reference
                if(STVariable.structure != STIdentifier.PRIMITVE)
                {
                    // '-=' is defined for an array element (i.e., brackets) but not an array
                    if(! bArrayElemAssign)
                    {
                        error("Operation '-=' is not defined for an array reference as target of assignment, found '%s'", variableStr);
                    }
                    // Get the value of the array element
                    resOp1 = symbolTable.storageManager.getArrayElem(this, variableStr, resIndex);
                    // Subtract second operand from first operand
                    resAssign = Utility.subtract(this, resOp1, resOp2, "-=");
                    // Assign the result to the array at the given index
                    symbolTable.storageManager.arrayAssignElem(this, variableStr, resAssign, resIndex);
                }
                // Otherwise, the target was a primitive variable
                else
                {
                    // '-=' is not defined for indexing a string
                    if(bArrayElemAssign)
                    {
                        error("Operation '-=' is not defined for indexing a string, found '%s[%s]'", variableStr, resIndex.value);
                    }
                    resOp1 = symbolTable.retrieveVariableValue(this, variableStr);
                    // Subtract second operand from first operand
                    resAssign = Utility.subtract(this, resOp1, resOp2, "-=");
                    // Assign the result to the variable
                    symbolTable.storeVariableValue(this, variableStr, resAssign);
                }
                break;
                
            case "+=":
                // Get the second operand
                resOp2 = expr();
                
                // Check that the second operand is primitive
                if(resOp2.structure != STIdentifier.PRIMITVE)
                {
                    error("Operation '+=' expected a primitive source value, found array '%s'", resOp2.value);
                }
                
                // Check if the target was an array element reference
                if(STVariable.structure != STIdentifier.PRIMITVE)
                {
                    // '+=' is defined for an array element (i.e., brackets) but not an array
                    if(! bArrayElemAssign)
                    {
                        error("Operation '+=' is not defined for an array reference as target of assignment, found '%s'", variableStr);
                    }
                    // Get the value of the array element
                    resOp1 = symbolTable.storageManager.getArrayElem(this, variableStr, resIndex);
                    // Subtract second operand from first operand
                    resAssign = Utility.add(this, resOp1, resOp2, "+=");
                    // Assign the result to the array at the given index
                    symbolTable.storageManager.arrayAssignElem(this, variableStr, resAssign, resIndex);
                }
                // Otherwise, the target was a primitive variable
                else
                {
                    // '+=' is not defined for indexing a string
                    if(bArrayElemAssign)
                    {
                        error("Operation '+=' is not defined for indexing a string, found '%s[%s]'", variableStr, resIndex.value);
                    }
                    resOp1 = symbolTable.retrieveVariableValue(this, variableStr);
                    // Subtract second operand from first operand
                    resAssign = Utility.add(this, resOp1, resOp2, "+=");
                    // Assign the result to the variable
                    symbolTable.storeVariableValue(this, variableStr, resAssign);
                }
                break;
                
            default:
                error("Expected assignment operator, found '%s'", operatorStr);
                resAssign = new ResultValue(); // This will never be reached
        }
        
        // The assignment statement must be followed by ';'
        if(! scan.currentToken.tokenStr.equals(";"))
        {
            error("Expected ';' after assignment statement");
        }
        
        // Print the debug information for the variable and value of the assignment
        if(bShowAssign)
        {
            System.out.println("\t\t...\n");
            System.out.printf("\t\tVariable: %s\n", variableStr);
            System.out.printf("\t\tType:     %s\n", Token.strSubClassifM[resAssign.type]);
            System.out.printf("\t\tValue:    %s\n", resAssign.value);
        }
    }
    
    /**
     * Executes a declaration statement
     * Assumption: current token is on a CONTROL DECLARE
     * <p>
     * A declare statement is of the form:
     *     declareStmt := declare variable ';'
     *                  | declare variable assignStmt
     * 
     * This method will take the variable of type declare and 
     * create an instance of it in the symbol table, signifying
     * that it has been declared. The declaration may or may not 
     * be followed by an assignment as follows:
     *    Int x;
     *    Int y = 5;
     * NOTE: Unless/until user-defined functions are implemented,
     * each variable will be declared with LOCAL scope and
     * NOT_A_PARAMETER for its parameter passing type
     * -------------------------------------------------------------
     * @param bExec      indicates whether the code should be executed or ignored
     * @throws Exception the first token is not a declaration type
     *                   the token after the declaration type is not a variable
     *                   the declaration type is not valid
     *                   missing ';' after declaration
     */
    public void declareStmt(boolean bExec) throws Exception
    {
        int iDeclareLineNr; // line number for beginning of declare statement
        iDeclareLineNr = scan.currentToken.iSourceLineNr;
        
        // Do we need to ignore execution of the declaration?
        if(! bExec)
        {
            // We are ignoring execution
            // Skip to the end of the declare statement
            skipTo(iDeclareLineNr, "declare", ";");
            return;
        }
        // Otherwise, we are executing the declaration
        
        // A check to make sure this method was called with the correct current token
        if((scan.currentToken.primClassif != Token.CONTROL) || (scan.currentToken.subClassif != Token.DECLARE))
        {
            error("Expected a declaration type for the beginning of a declare statement, found '%s'", scan.currentToken.tokenStr);
        }
        String declareStr = scan.currentToken.tokenStr;
        
        // Save the declare token. This is to set the position of the scanner
        // for a call to 'expr' if we are declaring a fixed-size array
        Token declareToken = scan.currentToken;
        
        // Get the declaration type's constant
        int declareType;
        switch(declareStr)
        {
            case "Int":
                declareType = Token.INTEGER;
                break;
            case "Float":
                declareType = Token.FLOAT;
                break;
            case "String":
                declareType = Token.STRING;
                break;
            case "Bool":
                declareType = Token.BOOLEAN;
                break;
            case "Date":
                declareType = Token.DATE;
                break;
            default:
                declareType = -1; // This is to get rid of error saying the variable
                                  // may be uninitialized when putting in the symbol table
                error("Unknown declaration type, found '%s'", declareStr);
        }
        
        // Get the variable to be declared and check it
        scan.getNext();
        if((scan.currentToken.primClassif != Token.OPERAND) || (scan.currentToken.subClassif != Token.IDENTIFIER))
        {
            error("Expected a variable for the target of a declaration");
        }
        String variableStr = scan.currentToken.tokenStr;
        
        // Determine if we are declaring an array
        if(scan.nextToken.tokenStr.equals("["))
        {
            // We are declaring an array. Example declarations:
            // 1)    Int arr[] = 2, 3, 1;
            // 2)    Int arr[3];
            // 3)    Int arr[3] = 2, 3 ,1;
            // 4)    Int arr[unbound];
            // 5)    Int arr[unbound] = 2, 3, 1;
            
            boolean bFirstType; // If the array declaration is the first type, then the number
                                // of elements assigned will determine its max number of elements
            boolean bSecondOrThirdType; // If the array declaration is the second or third type, then
                                        // we have to call 'expr' to determine its declared size after
                                        // we have put it in the symbol table / storage manager
            bFirstType = false;
            bSecondOrThirdType = false;
            
            // Create an array to be declared and set its type
            ResultArray resArray = new ResultArray();
            resArray.type = declareType;
            resArray.value = variableStr;
            
            // Move to the '[' and determine the type of array declaration
            scan.getNext();
            
            // Check for the first type of array declaration statement
            if(scan.nextToken.tokenStr.equals("]"))
            {
                bFirstType = true;
                resArray.structure = STIdentifier.FIXED_ARRAY;
                resArray.maxElem = 0; // This will change depending on how many elements
                                      // we initialize the array with
                
                // Move to the token after the ']' and check that there is a '='
                scan.getNext();
                scan.getNext();
                if(! scan.currentToken.tokenStr.equals("="))
                {
                    error("Expected '=' for initialization of array, found '%s'", scan.currentToken);
                }
            }
            // Check for the fourth and fifth types of array declaration statements
            else if(scan.nextToken.tokenStr.equals("unbound"))
            {
                resArray.structure = STIdentifier.UNBOUNDED_ARRAY;
                
                // Check that the token after 'unbound' is a ']'
                scan.getNext();
                scan.getNext();
                if(! scan.currentToken.tokenStr.equals("]"))
                {
                    error("Expected ']', found '%s'", scan.currentToken.tokenStr);
                }
                // Move to the token after ']'
                scan.getNext();
            }
            // Otherwise, we have the second and third types of array declaration statements
            else
            {
                resArray.structure = STIdentifier.FIXED_ARRAY;
                bSecondOrThirdType = true;
            }
            
            // Declare the array variable by placing in the symbol table
            STIdentifier STArrayVariable = new STIdentifier(variableStr, Token.OPERAND, declareType, STIdentifier.NOT_A_PARAMETER
                                                                       , resArray.structure, STIdentifier.LOCAL);
            symbolTable.putSymbol(variableStr, STArrayVariable);
            
            // Also need to store the array in the storage manager
            symbolTable.storageManager.putResultArray(this, variableStr, resArray);
            
            // In the case of the second or third types of array declaration statements, the
            // array size is determined by a call to 'expr'. However, 'expr' must already have
            // knowledge of the declared array; so, we declare the array as in the above lines,
            // and set its max size afterwards
            if(bSecondOrThirdType)
            {
                // In these two declaration types, we want to call 'expr' to get the value of
                // the size. In order to make that parsing easier, we need to call 'expr' with
                // the current token on the array identifier; we will let 'expr' know that we
                // only want the value in the brackets, not the array element at the index of
                // that value
                scan.setPosition(declareToken);
                bGettingArraySize = true;
                scan.getNext(); // Setting 'bGettingArraySize' will cause 'expr' to not get the next token
                ResultValue resSize = expr();
                this.iParseTokenLineNr = declareToken.iSourceLineNr;
                Utility.coerce(this, Token.INTEGER, resSize, "declared size of array");
                resArray.maxElem = Integer.parseInt(resSize.value);
                bGettingArraySize = false;
            }
            
            // There may be a value list to initialize the array with
            if(scan.currentToken.tokenStr.equals("="))
            {
                // Start initializing at index zero
                int iIndex = 0;
                
                // Keep assigning indices of the array, beginning at index zero, as long as there are
                // comma-separated values that are coercible to the type of the array
                do
                {
                    // Get the token after the '=' or ','
                    scan.getNext();
                    
                    // If we found a unary minus, get the next token and make it a negative numeric value
                    if(scan.currentToken.tokenStr.equals("-") && scan.currentToken.subClassif == Token.UNARY)
                    {
                        scan.getNext();
                        ResultValue resValNegative = Utility.uminus(this, scan.currentToken.toResultValue(this));
                        scan.currentToken.tokenStr = resValNegative.value;
                    }
                    
                    // The value list must consist of operands that are not identifiers
                    if((scan.currentToken.primClassif != Token.OPERAND) || (scan.currentToken.subClassif == Token.IDENTIFIER))
                    {
                        error("Expected a value coercible to type '%s', found '%s'"
                              , Token.getType(this, resArray.type), scan.currentToken.tokenStr);
                    }
                    
                    // Get the token as a result value and attempt to coerce to the type of the array
                    ResultValue resVal = scan.currentToken.toResultValue(this);
                    Utility.coerce(this, resArray.type, resVal, "array initialization");
                    
                    // If we are parsing the first type of array declaration statement, the
                    // number of values determine the size of the array
                    if(bFirstType)
                    {
                        // This was initially set to 0, so every valid value we add to the
                        // array will simultaneously increase the size of the array
                        resArray.maxElem++;
                    }
                    
                    // Index needs to be a result value
                    ResultValue resIndex = new ResultValue();
                    resIndex.value = Integer.toString(iIndex);
                    resIndex.type = Token.INTEGER;
                    
                    // Assign the array element at the current index
                    symbolTable.storageManager.arrayAssignElem(this, variableStr, resVal, resIndex);
                    
                    iIndex++;
                    
                }while(scan.getNext().equals(",")); 
            }
        }
        // We are not declaring an array; we are declaring a primitive variable
        else
        {
            // Put the declared variable in the symbol table
            STIdentifier STVariable = new STIdentifier(variableStr, Token.OPERAND, declareType, STIdentifier.NOT_A_PARAMETER
                                                                  , STIdentifier.PRIMITVE, STIdentifier.LOCAL);
            symbolTable.putSymbol(variableStr, STVariable);
            
            // Move to the token after the operand
            scan.getNext();
            
            // There may be a value to initialize the variable with
            if(scan.currentToken.tokenStr.equals("="))
            {
                // Go to the token after '='
                scan.getNext();
                
                // If we found a unary minus, get the next token and make it a negative numeric value
                if(scan.currentToken.tokenStr.equals("-") && scan.currentToken.subClassif == Token.UNARY)
                {
                    scan.getNext();
                    ResultValue resValNegative = Utility.uminus(this, scan.currentToken.toResultValue(this));
                    scan.currentToken.tokenStr = resValNegative.value;
                }
                
                // The value must be an operand that is not an identifier
                if((scan.currentToken.primClassif != Token.OPERAND) || (scan.currentToken.subClassif == Token.IDENTIFIER))
                {
                    error("Expected a value coercible to type '%s', found '%s'"
                          , Token.getType(this, declareType), scan.currentToken.tokenStr);
                }
                
                // Get the token as a result value and attempt to coerce to the type of the array
                ResultValue resVal = scan.currentToken.toResultValue(this);
                Utility.coerce(this, declareType, resVal, "variable initialization");
                
                // Store the value for the variable
                symbolTable.storeVariableValue(this, variableStr, resVal);
                
                // Move to the token after the value
                scan.getNext();
            }
        }
        
        // The declaration statement must be followed by ';'
        if(! scan.currentToken.tokenStr.equals(";"))
        {
            error("Expected ';' after declaration statement");
        }
    }
    
    // Add documentation... TODO
    public ResultValue expr() throws Exception
    {
        ArrayList<Token> outList = new ArrayList<Token>(); // List to hold prefix expr
        Stack<Token> postfixStack = new Stack<Token>(); // Stack to hold the tokens as they are added to post-fix expr
        Stack<ResultValue> resultStack = new Stack<ResultValue>(); // Stack to hold values as they are
                                                                   // evaluated from the post-fix stack
        boolean bExpectingOperand = true; // Used to determine that the order of operators and operands
                                         // from the infix expression is valid
        boolean bFoundAnOperator = false; // If the debugger for an expression is turned on, we only want to
                                          // print expression results if they had at least on operator
        boolean bFoundRtParen = false; // Indicates that a ')' before a ':' or ';' was found
        
        
        
        // Get the next token
        // If declaring an array or if an array element is the target of an assignment,
        // then 'expr' was called while currently on the correct token. In this
        // case, we don't want to get the next token.
        // Similarly, if 'statements' calls 'expr', then it is already on the
        // current token, so we don't want to get the next one
        if(! (bGettingArraySize || bCalledExprFromStmts))
        {
            scan.getNext();
        }
        
        // Get the next token as long as it isn't an expression delimiter or the end of file
        while(true)
        {
            // It is possible to have a current token string that is ':' or ';', but
            // it may be within a user made string, e.g.,
            //     for word from sentence by ':':
            // The first colon is a user made string while the second is a delimiter
            if(! (scan.currentToken.primClassif == Token.OPERAND && scan.currentToken.subClassif == Token.STRING))
            {
                // If we hit a delimiter, then we are at the end of the expression
                if((scan.currentToken.primClassif == Token.CONTROL) || (exprDelimiters.contains(scan.currentToken.tokenStr))
                    || (scan.currentToken.primClassif == Token.EOF) || (assignmentTokens.contains(scan.currentToken.tokenStr)))
                {
                    break;
                }
            }
            Token token = scan.currentToken;
            switch(token.primClassif)
            {
                case Token.OPERAND:
                    // Error if we were expecting an operator
                    if(! bExpectingOperand)
                    {
                        error("Expecting an operator, found operand '%s'", token.tokenStr);
                    }
                    
                    // If the operand is not an identifier, just add to the post-fix list
                    if(token.subClassif != Token.IDENTIFIER)
                    {
                        outList.add(token);
                        bExpectingOperand = false;
                        break;
                    }
                    
                    // Check that the identifier has been declared
                    STEntry STEntryResult = symbolTable.getSymbol(token.tokenStr);
                    if(STEntryResult == null)
                    {
                        error("Variable '%s' has not been declared", token.tokenStr);
                    }
                    STIdentifier STVariable = (STIdentifier) STEntryResult;
                    
                    // Check if this identifier is an array, array element, 
                    // or just a primitive and set the appropriate token field
                    if(STVariable.structure == STIdentifier.PRIMITVE)
                    {
                        // We may need to get an index of a string (for partial string assignment or for splices)
                        if(STVariable.dclType == Token.STRING && scan.nextToken.tokenStr.equals("["))
                        {
                            token.identifierType = Token.ARRAY_ELEM;
                            // Need to remove the bracket; the corresponding ']' will
                            // match up with the string's identifier token
                            scan.getNext();
                            // Because we just got rid of a separator '[', the next token
                            // should be an operand
                            bExpectingOperand = true;
                            // We need the expression inside the brackets, so treat the string
                            // identifier as an operator and put it on the post-fix stack
                            postfixStack.push(token);
                        }
                        else
                        {
                            token.identifierType = Token.NOT_AN_ARRAY;
                            // It is just a primitive variable so add it to the post-fix list
                            outList.add(scan.currentToken);
                            // We just got an operand so the next token should not be an operand
                            bExpectingOperand = false;
                        }
                        
                    }
                    // In this case, the token is an array or array element
                    else
                    {
                        // This is an array element reference if there are brackets
                        if(scan.nextToken.tokenStr.equals("["))
                        {
                            token.identifierType = Token.ARRAY_ELEM;
                            // Need to remove the bracket; the corresponding ']' will
                            // match up with the array's identifier token
                            scan.getNext();
                            // Because we just had a separator '[', the next token
                            // should be an operand
                            bExpectingOperand = true;
                            // We need to parse the expression inside the brackets, so
                            // treat the array identifier as an operator and put on the
                            // post-fix stack
                            postfixStack.push(token);
                        }
                        // Otherwise, this is an array
                        else
                        {
                            token.identifierType = Token.ARRAY_REF;
                            // Because there was no bracket, the next token should not be an operand
                            bExpectingOperand = false;
                            // Even though this is an array, we can treat the reference like we would
                            // for a primitive variable and put it in the outlist
                            outList.add(token);
                        }
                    }
                    break;
                    
                case Token.OPERATOR:
                    // Found an operator for the expression debugger, if it is on
                    bFoundAnOperator = true;
                    
                    // Error if we were expecting an operand and we got a binary operator
                    // (e.g., x = 4 * * 5 is incorrect, while x = 4 * - 5 is correct
                    // since the "-" is a unary operator in the second case)
                    if(bExpectingOperand && token.subClassif == Token.BINARY)
                    {
                        error("Expecting an operand, found operator '%s'", token.tokenStr);
                    }
                    
                    // Pop tokens off the stack while they have a precedence greater
                    // than or equal to the current operator token
                    while(! postfixStack.isEmpty())
                    {
                        
                        if(token.precedence(this) > postfixStack.peek().stkPrecedence(this))
                        {
                            break;
                        }
                        //If current tokens precedence is less than or equal stk precedence
                        outList.add(postfixStack.pop());
                    }
                    // Add the operator to our post-fix list
                    postfixStack.push(token);
                    // Now we are expecting the next token to be an operand or a unary operator
                    bExpectingOperand = true;
                    break;
                    
                case Token.SEPARATOR:
                    switch(token.tokenStr)
                    {
                        case "(":
                            // Error if we were expecting an operator and found a "("
                            // (e.g., 3 (4 + 6) is invalid, while 3 * (4 + 6) is valid)
                            if(! bExpectingOperand)
                            {
                                error("Expecting an operator before '('");
                            }
                            postfixStack.push(token);
                            break;
                        case ")":
                            // Error if we were expecting an operand and found a ")"
                            // (e.g., 3 * (4 + ) is invalid, while 3 * (4 + 6) is valid)
                            if(bExpectingOperand)
                            {
                                error("Expecting an operand before ')'");
                            }
                            
                            boolean bFoundParen = false; // Signifies if we found the matching left parenthesis
                            
                            // Remove from the stack until the matching parenthesis is found.
                            // In the case of function calls, the name of the function acts as the '('
                            while(! postfixStack.isEmpty())
                            {
                                Token popped = postfixStack.pop();
                                
                                // Found matching parenthesis
                                if(popped.tokenStr.equals("("))
                                {
                                    bFoundParen = true;
                                    break;
                                }
                                
                                // Found matching function call's parenthesis
                                if(popped.primClassif == Token.FUNCTION  && (! popped.tokenStr.equals("IN")) 
                                                                         && (! popped.tokenStr.equals("NOTIN")))
                                {
                                    bFoundParen = true;
                                    outList.add(popped);
                                    break;
                                }
                                
                                // Error if we find a '[' before the '('
                                //    4 * (3 + arr[x * y)]
                                if((popped.primClassif == Token.OPERAND) && (popped.subClassif == Token.IDENTIFIER))
                                {
                                    error("Expected ']' before ')'");
                                }
                                
                                // Error if we find a '{' before the '('
                                if(popped.tokenStr.equals("{"))
                                {
                                    error("Expected '}' before ')'");
                                }
                                outList.add(popped);
                            }
                            
                            if(! bFoundParen)
                            {
                                error("Could not find matching '('. May be missing ';' or ':'?");
                            }
                            break;  //Break out of case ")"
                            
                        case "[":
                            // A '[' should only follow an array variable or a string variable. When
                            // this happens, we throw the '[' away and use the actual variable as the
                            // '['. So, if a '[' is encountered, we have not thrown it away, so it is an error
                            error("Expected an array variable or a variable of type 'STRING' before '['");
                            break;
                            
                        case "]":
                            // Error if we were expecting an operand and found a "]"
                            // (e.g., 'arr[4 + ]' is invalid, while 'arr[4 + 3]' is valid)
                            if(bExpectingOperand)
                            {
                                error("Expecting an operand before ']'");
                            }
                            
                            boolean bFoundArray = false; // Signifies if we found the matching array token
                            
                            // Remove from the stack until the matching array token is found
                            while(! postfixStack.isEmpty())
                            {
                                Token popped = postfixStack.pop();
                                // Check if the token was the matching array token
                                if((popped.primClassif == Token.OPERAND) && (popped.subClassif == Token.IDENTIFIER))
                                {
                                    bFoundArray = true;
                                    outList.add(popped);
                                    break;
                                }
                                
                                // Error if we find a '(' before the '['
                                // e.g., 'arr[x * (4 + y])'
                                if(popped.tokenStr.equals("("))
                                {
                                    error("Expected ')' before ']'");
                                }
                                
                                // Error if we find a '{' before the '['
                                if(popped.tokenStr.equals("{"))
                                {
                                    error("Expected '}' before ']'");
                                }
                                
                                outList.add(popped);
                            }
                            
                            // Otherwise, check that we found the array in the stack
                            // which signifies that we found the matching '[' 
                            if(! bFoundArray)
                            {
                                error("Could not find matching '['");
                            }
                            break;
                            
                        case ",":
                            // Error if we were expecting an operand and found a ','
                            // e.g., 'print(x + , "14")'
                            if(bExpectingOperand)
                            {
                                error("Expecting an operand before ','");
                            }
                            
                            // Commas are only valid for a function call
                            boolean bFoundFunction = false;
                            
                            // Remove from the stack until the matching function call is found
                            // For functions 'IN' and 'NOTIN', a comma separated value list begins with '{'
                            while(! postfixStack.isEmpty())
                            {
                                Token popped = postfixStack.pop();
                                if(popped.primClassif == Token.FUNCTION || popped.tokenStr.equals("{"))
                                {
                                    bFoundFunction = true;
                                    
                                    // We don't actually want to add the function to the
                                    // post-fix list until the corresponding ')' is found
                                    postfixStack.push(popped);
                                    break;
                                }
                                
                                // Finding a '(' before the ',' should be an error
                                // e.g., 'print( x * (y + 3, "14" );'
                                if(popped.tokenStr.equals("("))
                                {
                                    error("Expected ')' before ','");
                                }
                                
                                // Find a '[' before the ',' should be an error
                                // e.g., 'print( arr[x , "hello" );'
                                if((popped.primClassif == Token.OPERAND) && (popped.subClassif == Token.IDENTIFIER))
                                {
                                    error("Expected ']' before ','");
                                }
                                
                                // Token is not function or '{' so add to the post-fix list
                                outList.add(popped);
                            }
                            
                            // Check that the matching function call was found for the ','
                            if(! bFoundFunction)
                            {
                                error("Expected a corresponding function call before ','");
                            }
                            
                            // Now we are expecting the next token to be an operand or a unary operator
                            bExpectingOperand = true;
                            break;
                            
                        case "{":
                            // A '{' should only follow 'IN' or 'NOTIN'
                            Token lastToken = postfixStack.peek();
                            if(! (lastToken.tokenStr.equals("IN") || lastToken.tokenStr.equals("NOTIN")))
                            {
                                error("Unexpected '{', only valid following 'IN' or 'NOTIN'");
                            }
                            postfixStack.push(token);
                            break;
                            
                        case "}":
                            // Error if we were expecting an operand and found a "}"
                            // (e.g., '5 IN {14, 4, 3 + }'
                            if(bExpectingOperand)
                            {
                                error("Expecting an operand before '}'");
                            }
                            
                            boolean bFoundBracket = false; // Signifies if we found the matching left bracket
                            
                            // Remove from the stack until the matching bracket is found
                            while(! postfixStack.isEmpty())
                            {
                                Token popped = postfixStack.pop();
                                
                                // Found matching bracket
                                if(popped.tokenStr.equals("{"))
                                {
                                    bFoundBracket = true;
                                    
                                    // Create a token that signifies the end of a value list
                                    Token endValueList = new Token();
                                    endValueList.primClassif = Token.VALUE_LIST;
                                    endValueList.tokenStr = "VALUE_LIST"; // This is simply to make my debugging
                                                                          // of the post-fix list easier
                                    outList.add(endValueList);
                                    break;
                                }
                                
                                // Error if we found a '('
                                if((popped.tokenStr.equals("(")) || (popped.primClassif == Token.FUNCTION))
                                {
                                    error("Expected ')' before '}'");
                                }
                                
                                // Error if we found a '['
                                if((popped.primClassif == Token.OPERAND) && (popped.subClassif == Token.IDENTIFIER))
                                {
                                    error("Expected ']' before '}'");
                                }
                                
                                outList.add(popped);
                            }
                            
                            // Check that the matching function call was fond for the ','
                            if(! bFoundBracket)
                            {
                                error("Could not find matching '{'");
                            }
                            break;
                            
                        default:
                            error("Invalid separator, found '%s'", token.tokenStr);
                    }
                    break;  //Break out of case Token.SEPARATOR
                    
                case Token.FUNCTION:
                    // Function is a built-in function
                    if(token.subClassif == Token.BUILTIN)
                    {
                        // A token signifying the end of the function's parameters
                        Token endFuncArgs = new Token();
                        endFuncArgs.primClassif = Token.FUNC_ARGS;
                        endFuncArgs.tokenStr = "FUNC_ARGS"; // This is simply to make my debugging
                                                            // of the post-fix list easier
                            
                        // Execute the appropriate function
                        switch(token.tokenStr)
                        {
                            // The 'debug' function is a statement and should only be called from 'statements'
                            case "debug":
                                error("Invalid call to 'debug' function from an expression");
                                break;
                                
                            case "IN":
                            case "NOTIN":
                                // 'IN' and 'NOTIN' act more like operators, so we should
                                // be expecting an operator in the infix expression
                                if(bExpectingOperand)
                                {
                                    error("Expecting an operand, found '%s'", token.tokenStr);
                                }
                                
                                // Check that the function is followed by '{' or an array
                                if(! scan.nextToken.tokenStr.equals("{"))
                                {
                                    // It is not '{'. If it is an array it must be represented by a variable
                                    if(scan.nextToken.primClassif == Token.OPERAND && scan.nextToken.subClassif == Token.IDENTIFIER)
                                    {
                                        STIdentifier STVar = (STIdentifier) symbolTable.getSymbol(scan.nextToken.tokenStr);
                                        
                                        // Check if the identifier is not an array
                                        if(STVar.structure == STIdentifier.PRIMITVE)
                                        {
                                            error("Expected an array or value list after '%s'"
                                                  , scan.currentToken.tokenStr); 
                                        }
                                    }
                                    // It is not '{' or an array
                                    else
                                    {
                                        error("Expected an array or value list after '%s'"
                                                , scan.currentToken.tokenStr); 
                                    }
                                }
                                
                                // Add a token signifying the end of the value list
                                outList.add(endFuncArgs);
                                postfixStack.push(token);
                                
                                // These functions should be expecting an operand (a value list will evaluate to an operand)
                                bExpectingOperand = true;
                                break;
                                
                            default:
                                // Since all other functions will return an operand, the
                                // occurrence of a function should be when an operand is expected
                                if(! bExpectingOperand)
                                {
                                    error("Expecting an operator, found '%s'", token.tokenStr);
                                }
                                
                                // Check that there is a '(' after the function name
                                if(! scan.getNext().equals("("))
                                {
                                    error("Expected '(' after '%s'", token.tokenStr);
                                }
                                
                                // Add a token signifying the end of the function's parameters
                                outList.add(endFuncArgs);
                                
                                // The function name will act as the '('
                                postfixStack.push(token);
                                break;
                        }
                    }
                    // Function is a user-defined function
                    else
                    {
                        error("User-defined functions have not been implemented yet");
                    }
                    break;
                    
                default:
                    error("Unrecognized argument for expression, found '%s'. May be missing ';' or ':'?", token.tokenStr);
            }
            
            // This will only be set to true if we were expecting the special ')'
            // on the call to expr and we found one. In this case, the function
            // is going to want the special ')' as the delimiter we don't want
            // to get the next token, so we leave the loop
            if(bFoundRtParen)
            {
                break;
            }
            
            scan.getNext();
        }
        
        // Put the rest of expression's input stack to the postfix expression
        while(! postfixStack.isEmpty())
        {
            Token popped = postfixStack.pop();
            // Should not have any left parenthesis or functions, unless the function was
            // 'IN' or 'NOTIN' since they do not use parenthesis
            if(popped.tokenStr.equals("(") || ((popped.primClassif == Token.FUNCTION)
               && ((! popped.tokenStr.equals("IN")) && (! popped.tokenStr.equals("NOTIN")))))
            {
                // The second part of this error message is in regards to a
                // possible error from an expression call from a print statement
                errorLineNr(popped.iSourceLineNr, "Missing ')'. If not, may be missing ';' or ':', %s", popped.tokenStr);
            }
            
            // Also should not have any left brackets (i.e., an array element reference)
            if(popped.identifierType == Token.ARRAY_ELEM)
            {
                errorLineNr(popped.iSourceLineNr, "Missing ']'");
            }
            
            // Also should not have any left braces (i.e., value list)
            if(popped.tokenStr.equals("{"))
            {
                errorLineNr(popped.iSourceLineNr, "Missing '}'");
            }
            outList.add(popped);
        }
        
        // If declaring an array, we don't want to be accessing that array. We want the result of
        // the expression to be a value that will be used for the declaring the size of the array.
        // So, we just remove the array identifier token from the post-fix expression.
        if(bGettingArraySize)
        {
            Token lastToken = outList.get(outList.size() - 1);
            outList.remove(lastToken);
        }
        
        // This will be set from the 'debug' function
        if(bShowPostfix)
        {
            System.err.println("---start list---");
            for(Token t : outList)
            {
                System.err.println(t.tokenStr);
            }
            System.err.println("----end list----");
        }
        
        // Evaluate the post-fix expression
        for(Token outToken : outList)
        {
            // Set the current parsing line number, for error messages
            this.iParseTokenLineNr = outToken.iSourceLineNr;
            
            // Check the type of token
            switch(outToken.primClassif)
            {
                // Operands need to be put of the result stack
                case Token.OPERAND:
                    // The operand is an identifier, so get its associated value
                    // before putting it on the result stack
                    if(outToken.subClassif == Token.IDENTIFIER)
                    {
                        // If the identifier is an array/string element reference, we need to
                        // get the element at the given index. The index would be whatever 
                        // expression had been evaluated inside the brackets
                        if(outToken.identifierType == Token.ARRAY_ELEM)
                        {
                            STEntry STVarEntry = symbolTable.getSymbol(outToken.tokenStr);
                            if(STVarEntry == null)
                            {
                                // This should only happen if I incorrectly evaluated the infix expression
                                error("Variable '%s' has not been declared", outToken.tokenStr);
                            }
                            STIdentifier STVariable = (STIdentifier) STVarEntry;
                            
                            // If the variable is primitive, it is a string index
                            if(STVariable.structure == STIdentifier.PRIMITVE)
                            {
                                // Get the string
                                ResultValue resString = symbolTable.retrieveVariableValue(this, outToken.tokenStr);
                                
                                // Get the index, coerce to and int type, and convert to a numeric
                                ResultValue resIndex = resultStack.pop();
                                Utility.coerce(this, Token.INTEGER, resIndex, "string indexing");
                                Numeric numIndex = new Numeric(this, resIndex, outToken.tokenStr, "index");
                                
                                // If the index is negative, convert it to its corresponding positive index
                                if(numIndex.integerValue < 0)
                                {
                                    numIndex.integerValue = numIndex.integerValue + resString.value.length();
                                }
                                
                                // Ensure that the index is within the bounds of the string
                                if(numIndex.integerValue < 0 || numIndex.integerValue >= resString.value.length())
                                {
                                    error("Index '%s' out of bounds for 'STRING' variable '%s' with value '%s'"
                                            , resIndex.value, outToken.tokenStr, resString.value);
                                }
                                
                                // Create the result value that will hold the indexed character
                                ResultValue resChar = new ResultValue();
                                resChar.value = Character.toString(resString.value.charAt(numIndex.integerValue));
                                resChar.type = Token.STRING;
                                resChar.structure = STIdentifier.PRIMITVE;
                                
                                // Put the character that was indexed from the string onto the evaluation stack
                                resultStack.push(resChar);
                            }
                            // Otherwise, the variable is an array and we the element at the given index
                            else
                            {
                                ResultValue resIndex = resultStack.pop();
                                
                                // Get the reference to the array's element at index 'resIndex'
                                ResultValue resArrayElemRef;
                                resArrayElemRef = symbolTable.storageManager.getArrayElem(this, outToken.tokenStr, resIndex);
                                
                                // We need a copy of the array's element, not a reference
                                ResultValue resArrayElemCopy = Utility.getResultValueCopy(resArrayElemRef);
                                
                                // Put that copy of the array's element on the evaluation stack
                                resultStack.push(resArrayElemCopy);
                            }
                        }
                        // The identifier is not an array reference/string element reference
                        else
                        {
                            resultStack.push(symbolTable.retrieveVariableValue(this, outToken.tokenStr));
                        }
                    }
                    // Operand is not an identifier, so just put the operand on the result stack
                    else
                    {
                        resultStack.push(outToken.toResultValue(this));
                    }
                    break;
                    
                // Operators need to take the appropriate number of
                // operands off of the stack and evaluate them
                case Token.OPERATOR:
                    // Unary operator
                    if(outToken.subClassif == Token.UNARY)
                    {
                        // Check to see that there is an operand for the operation
                        if(resultStack.isEmpty())
                        {
                            errorWithCurrent("Missing operand for operation '%s'", outToken.tokenStr);
                        }
                        // An operand exists so retrieve it
                        ResultValue resOp = resultStack.pop();
                        
                        // Determine which unary operation to perform
                        switch(outToken.tokenStr)
                        {
                            case "-":
                                resultStack.push(Utility.uminus(this, resOp));
                                break;
                            case "not":
                                resultStack.push(Utility.not(this, resOp));
                                break;
                            default:
                                // This error message would only occur if we added a InvalidDateFirstParam operator to the language
                                // and forgot to add its appropriate case in this switch statement
                                errorWithCurrent("Unrecognized operator, found '%s'", outToken.tokenStr);
                        }
                    }
                    // Binary operator
                    else
                    {
                        // Check to see that there is the second operand for the operation
                        if(resultStack.isEmpty())
                        {
                            errorWithCurrent("Missing operands for operation '%s'", outToken.tokenStr);
                        }
                        // Get the second operand
                        ResultValue resOp2 = resultStack.pop();
                        
                        // Check to see that there is the first operand for the operation
                        if(resultStack.isEmpty())
                        {
                            errorWithCurrent("Missing second operand for operation '%s'", outToken.tokenStr);
                        }
                        // Get the first operand
                        ResultValue resOp1 = resultStack.pop();
                        
                        // Determine which binary operation to perform
                        switch(outToken.tokenStr)
                        {
                            case "^":
                                resultStack.push(Utility.exponent(this, resOp1, resOp2));
                                break;
                                
                            case "*":
                                resultStack.push(Utility.multiply(this, resOp1, resOp2));
                                break;
                                    
                            case "/":
                                resultStack.push(Utility.divide(this, resOp1, resOp2));
                                break;
                                    
                            case "+":
                                // The last parameter is to indicate 'add' is called from '+' as opposed to '+='
                                resultStack.push(Utility.add(this, resOp1, resOp2, "+"));
                                break;
                                    
                            case "-":
                                // The last parameter is to indicate 'subtract' is called from '-' as opposed to '-='
                                resultStack.push(Utility.subtract(this, resOp1, resOp2, "-"));
                                break;
                                    
                            case "#":
                                resultStack.push(Utility.concat(this, resOp1, resOp2));
                                break;
                                    
                            case "<":
                                resultStack.push(Utility.compare(this, Utility.LESS_THAN, resOp1, resOp2));
                                break;
                                    
                            case ">":
                                resultStack.push(Utility.compare(this, Utility.GREATER_THAN, resOp1, resOp2));
                                break;
                                    
                            case "<=":
                                resultStack.push(Utility.compare(this, Utility.LESS_THAN_EQUAL, resOp1, resOp2));
                                break;
                                    
                            case ">=":
                                resultStack.push(Utility.compare(this, Utility.GREATER_THAN_EQUAL, resOp1, resOp2));
                                break;
                                    
                            case "==":
                                resultStack.push(Utility.compare(this, Utility.EQUAL, resOp1, resOp2));
                                break;
                                    
                            case "!=":
                                resultStack.push(Utility.compare(this, Utility.NOT_EQUAL, resOp1, resOp2));
                                break;
                                    
                            case "and":
                                resultStack.push(Utility.compare(this, Utility.AND, resOp1, resOp2));
                                break;
                                    
                            case "or":
                                resultStack.push(Utility.compare(this, Utility.OR, resOp1, resOp2));
                                break;
                                
                            default:
                                // This error message would only occur if we added a InvalidDateFirstParam operator to the language
                                // and forgot to add its appropriate case in this switch statement
                                errorWithCurrent("Unrecognized operator, found '%s'", outToken.tokenStr);
                        }
                    }
                    break;
                    
                case Token.FUNCTION:
                    // Function is a built-in function
                    if(outToken.subClassif == Token.BUILTIN)
                    {
                        ResultValue resOp;      // Used as the parameter(s) for the functions
                        ResultValue resOp1;
                        ResultValue resOp2;
                        ResultValue resEndArgs; // Used to hold the end-of-function-arguments token
                        ResultArray resArrayOp; // Used for functions that require an array parameter
                        
                        // Execute the appropriate function
                        switch(outToken.tokenStr)
                        {
                            case "LENGTH":
                                resOp = resultStack.pop();
                                // Check that this was the only operand
                                if(resultStack.pop().type != Token.FUNC_ARGS)
                                {
                                    error("Invalid number of parameters for function 'LENGTH', expected 1 parameter");
                                }
                                resultStack.push(Utility.LENGTH(this, resOp));
                                break;
                                
                            case "SPACES":
                                resOp = resultStack.pop();
                                // Check that this was the only operand
                                if(resultStack.pop().type != Token.FUNC_ARGS)
                                {
                                    error("Invalid number of parameters for function 'SPACES', expected 1 parameter");
                                }
                                resultStack.push(Utility.SPACES(this, resOp));
                                break;
                                
                            case "ELEM":
                                resOp = resultStack.pop();
                                // Check that this was the only operand
                                if(resultStack.pop().type != Token.FUNC_ARGS)
                                {
                                    error("Invalid number of parameters for function 'ELEM', expected 1 parameter");
                                }
                                // Check that the operand is an array
                                if(! (resOp instanceof ResultArray))
                                {
                                    error("Expected an array reference for the parameter to 'ELEM', found '%s'", resOp.value);
                                }
                                // Get the operand as a result array
                                resArrayOp = (ResultArray) resOp;
                                resultStack.push(Utility.ELEM(this, resArrayOp));
                                break;
                                
                            case "MAXELEM":
                                resOp = resultStack.pop();
                                // Check that this was the only operand
                                if(resultStack.pop().type != Token.FUNC_ARGS)
                                {
                                    error("Invalid number of parameters for function 'MAXELEM', expected 1 parameter");
                                }
                                // Check that the operand is an array
                                if(! (resOp instanceof ResultArray))
                                {
                                    error("Expected an array reference for the parameter to 'MAXELEM', found '%s'", resOp.value);
                                }
                                // Get the operand as a result array
                                resArrayOp = (ResultArray) resOp;
                                resultStack.push(Utility.MAXELEM(this, resArrayOp));
                                break;
                                
                            case "print":
                                resOp = resultStack.pop();
                                
                                // The parameters for 'print' are in reverse order, so need to re-reverse them
                                Stack<ResultValue> printParamStack = new Stack<ResultValue>();
                                
                                // Get values to print as long as we have not hit the end of the print statements parameters
                                while(resOp.type != Token.FUNC_ARGS)
                                {
                                    printParamStack.push(resOp);
                                    resOp = resultStack.pop();
                                }
                                
                                // Print each parameter for the 'print' statement
                                while(! printParamStack.isEmpty())
                                {
                                    ResultValue resPrintParam = Utility.getResultValueCopy(printParamStack.pop());
                                    Utility.coerce(this, Token.STRING, resPrintParam, "print");
                                    System.out.printf("%s ", resPrintParam.value);
                                }
                                System.out.printf("\n");
                                
                                // 'print' returns a VOID type
                                ResultValue resPrintReturn = new ResultValue();
                                resPrintReturn.type = Token.VOID;
                                resultStack.push(resPrintReturn);
                                break;
                                
                            case "dateDiff":
                            case "dateAdj":
                            case "dateAge":
                                // Check that there are at least 3 items on stack before popping
                                if(resultStack.size() < 3)
                                {
                                    error("Invalid number of parameters for function '%s', expected 2 parameters", outToken.tokenStr);
                                }
                                // Get the two parameters for the date function
                                resOp2 = resultStack.pop();
                                resOp1 = resultStack.pop();
                                // Get the end-of-function-arguments token
                                resEndArgs = resultStack.pop();
                                // Check that these were the correct number of parameters for the date function (should always be 2)
                                if(resOp1.type == Token.FUNC_ARGS || resOp2.type == Token.FUNC_ARGS || resEndArgs.type != Token.FUNC_ARGS)
                                {
                                    error("Invalid number of parameters for function '%s', expected 2 parameters", outToken.tokenStr);
                                }
                                // Evaluate the appropriate date function and put the result back on stack
                                switch(outToken.tokenStr)
                                {
                                    case "dateDiff":
                                        resultStack.push(Utility.dateDiff(this, resOp1, resOp2));
                                        break;
                                    case "dateAdj":
                                        resultStack.push(Utility.dateAdj(this, resOp1, resOp2));
                                        break;
                                    case "dateAge":
                                        resultStack.push(Utility.dateAge(this, resOp1, resOp2));
                                        break;
                                }
                                break;
                                
                            case "IN":
                            case "NOTIN":
                                ResultValue resTopElem = resultStack.pop();
                                ResultArray resArrValueList;
                                
                                // Check if the second parameter to the function is a value list
                                if(resTopElem.type == Token.VALUE_LIST)
                                {
                                    // The stack contains a value list, so convert into an array
                                    resArrValueList = new ResultArray();
                                    resArrValueList.structure = STIdentifier.FIXED_ARRAY;
                                    
                                    // Keep adding values to from the value list to the array
                                    while(! resultStack.isEmpty())
                                    {
                                        ResultValue popped = resultStack.pop();
                                        
                                        if(popped.type == Token.FUNC_ARGS)
                                        {
                                            break;
                                        }
                                        
                                        // The value list should only consist of primitives
                                        if(popped.structure != STIdentifier.PRIMITVE)
                                        {
                                            errorWithCurrent("The value list for '%s' can only consist of primitives, found array '%s'"
                                                             , outToken.tokenStr, popped.value);
                                        }
                                        
                                        resArrValueList.valueList.add(popped);
                                    }
                                }
                                // The second parameter to the function is an array
                                else
                                {
                                    // Since it was an array, ensure it is the only parameter
                                    ResultValue resNextArg = resultStack.pop();
                                    if(resNextArg.type != Token.FUNC_ARGS)
                                    {
                                        errorWithCurrent("Expected only one array or value list after '%s', found '%s'"
                                                         , outToken.tokenStr, resTopElem.value); // Parameters are reversed on stack
                                    }
                                    
                                    // Check that the parameter is actually an array
                                    if(resTopElem.structure == STIdentifier.PRIMITVE)
                                    {
                                        errorWithCurrent("Expected an array or value list after '%s', found '%s'"
                                                         , outToken.tokenStr, resTopElem.value);
                                    }
                                    
                                    resArrValueList = (ResultArray) resTopElem;
                                }
                                
                                // Get the first parameter and check that it is a primitive
                                ResultValue resElem = resultStack.pop();
                                if(resElem.structure != STIdentifier.PRIMITVE)
                                {
                                    errorWithCurrent("Expected a primitive value as the first parameter for '%s', found array '%s'"
                                                     , outToken.tokenStr, resElem.value);
                                }
                                
                                ResultValue resBoolean = Utility.IN(this, resElem, resArrValueList);
                                
                                // If the function is 'NOTIN' just reverse the boolean result
                                if(outToken.tokenStr.equals("NOTIN"))
                                {
                                    resBoolean = Utility.not(this, resBoolean);
                                }
                                
                                resultStack.push(resBoolean);
                                break;
                                
                            default:
                                // Only reached if we add a built-in function but haven't called it here
                                error("Unknown built-in function: '%s'", outToken.tokenStr);
                        }
                    }
                    // Function is a user-defined function
                    else
                    {
                        error("User-defined functions have not been implemented yet");
                    }
                    break;
                    
                case Token.FUNC_ARGS:
                    // Put this as a result value on the stack to indicate to the
                    // corresponding function that it is at the end of its parameter list
                    ResultValue resEndFuncArgs = new ResultValue();
                    resEndFuncArgs.type = Token.FUNC_ARGS;
                    resEndFuncArgs.value = "END_FUNC_ARGS";
                    resultStack.push(resEndFuncArgs);
                    break;
                    
                case Token.VALUE_LIST:
                    // Put this as a result value on the stack to indicate to the
                    // function 'IN' or 'NOTIN' that the second parameter is a value list
                    // (i.e., all elements on the stack until FUNC_ARGS are part of the value list)
                    ResultValue resValueList = new ResultValue();
                    resValueList.type = Token.VALUE_LIST;
                    resValueList.value = "VALUE_LIST";
                    resultStack.push(resValueList);
            }
        }

        // There may not have even been an expression
        if(resultStack.isEmpty())
        {
            // Just in case the last token was EOF, make a more clear error message
            if(scan.currentToken.primClassif == Token.EOF)
            {
                error("Expected an expression before end of file");
            }
            else
            {
                error("Expected an expression before '%s'", scan.currentToken.tokenStr);
            }
            
        }
        ResultValue resReturnVal = resultStack.pop();
        
        // Print the debug information for the result of the current expression
        if(bShowExpr && bFoundAnOperator)
        {
            System.out.println("\t\t...");
            System.out.printf("\t\tType:  %s\n", Token.strSubClassifM[resReturnVal.type]);
            System.out.printf("\t\tValue: %s\n", resReturnVal.value);
        }
        
        return resReturnVal;
    }
    
    /**
     * Skips to a specified separator for the given statement
     * <p>
     * Takes in a specific separator to skip to for the given statement.
     * If the separator is not found, it will hit the end of the file and
     * throw an error. This method is not guaranteed to skip to the correct
     * separator for the given statement, it the expected separator is missing;
     * this may cause errors in code that follows.
     * @param  iLineNrCalledFrom  line number to provide a more accurate error message
     * @param  calledFrom         the subroutine that called skipTo
     * @param  skipToStr          the separator to search for
     * @throws Exception          if the separator was never found
     */
    public void skipTo(int iLineNrCalledFrom, String calledFrom, String skipToStr) throws Exception
    {
        scan.getNext();
        // Keep getting the next token unless we hit the end of file
        while(scan.currentToken.primClassif != Token.EOF)
        {
            // Token must be a separator to skip to
            if(scan.currentToken.primClassif == Token.SEPARATOR)
            {
                // We found the token to skip to
                if(scan.currentToken.tokenStr.equals(skipToStr))
                {
                    return;
                }
            }
            scan.getNext();
        }
        // We hit the end of file, so the skipTo string was never found
        errorLineNr(iLineNrCalledFrom, "No ending '%s' for '%s' statement", skipToStr, calledFrom);
    }
    
    /**
     * Prints debug information while executing code
     * Assumption: current token is on a "debug" token
     * <p>
     * This method allows the user to debug their code by printing out
     * underlying operatons. The debug function can turn on/off a debugger
     * to print out information for the following:
     *      - the currently scanned token's information
     *      - the variable and value of a an assignment statement
     *      - the evaluation of an expression
     * <p>
     * The syntax is as follows:
     *      debug <debugType> <onOff>;
     * debugType:= Assign
     *           | Expr
     *           | Token
     * onOff:= on
     *       | off
     * @throws Exception - if one of the given options to debug is invalid
     *                   - if missing ';'
     */
    public void debug() throws Exception
    {
        String debugType;  // The type of debug to call
        String debugOnOff; // Turn the debug on or turn it off
        boolean bDebug;    // Used for calling debuggers to turn on or off
        
        // Get the debug type
        debugType = scan.getNext();
        // Make sure the debug type is valid
        if(! Arrays.asList("Assign", "Expr", "Token", "Postfix").contains(debugType))
        {
            error("Invalid type for debug function, found '%s',", debugType);
        }
        
        // Get the "on" or "off" token and validate
        debugOnOff = scan.getNext();
        // Turn on debugger
        if(debugOnOff.equals("on"))
        {
            bDebug = true;
        }
        // Turn off debugger
        else if(debugOnOff.equals("off"))
        {
            bDebug = false;
        }
        // Invalid option, so error
        else
        {
            error("Invalid option for debug function, must specify 'on' or 'off', found '%s'", debugOnOff);
            bDebug = false; // Never reached
        }
        
        // Get the ';' before activating/deactivating a debugger
        if(! scan.getNext().equals(";"))
        {
            error("Expected ';' after debug statement");
        }
        
        // If Token debugging is on, don't want to print the tokens for
        // a debug statement. Now that we have parsed a debug statement,
        // indicate to the scanner that it is OK to print the token info
        scan.bInDebugStmt = false;
        
        // Turn the appropriate debugger on or off
        switch(debugType)
        {
            case "Assign":
                this.bShowAssign = bDebug;
                break;
                
            case "Expr":
                this.bShowExpr = bDebug;
                break;
                
            case "Token":
                scan.bShowToken = bDebug;
                break;
                
            case "Postfix":
                this.bShowPostfix = bDebug;
                break;
                
            default:
                // Only reached if we add another debugger type and don't check for it
                error("The case for debug type '%s' was never added to the debug method...", debugType);
        }
    }
}
