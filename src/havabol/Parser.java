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
    public boolean bExpectingRtParen; // Determines how to treat the last ')' of an expression.
                                      // In the case of functions, we want it to act as a delimiter
                                      // for the functions parameters, so this value would be set
                                      // to true, so that we don't try to find the matching '('
    public boolean bGettingArraySize; // Used when calling 'expr' to parse the declared size of the array
                                      // Indicates that we want to evaluate the expression in the brackets
                                      // and not the array element reference
    public boolean bCalledExprFromStmts; // If 'expr' is called from 'statements', then we are on the current token
                                         // and do not want to call scanner to get the next token
    
    Parser(Scanner scan, SymbolTable symbolTable)
    {
        this.scan = scan;
        this.symbolTable = symbolTable;
        this.sourceFileNm = scan.sourceFileNm;
        this.bShowAssign = false;
        this.bShowExpr = false;
        this.bExpectingRtParen = false;
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
                    // Execute the appropriate function
                    switch(functionName)
                    {
                        case "debug":
                            debug();
                            break;
                        case "print":
                            print(bExec);
                            break;
                        case "LENGTH":
                        case "SPACES":
                        case "ELEM":
                        case "MAXELEM":
                            // Handle the parsing of these functions in 'expr', but indicate that we are on the first
                            // token of the expression, so 'expr' should not call scanner for the next token
                            bCalledExprFromStmts = true;
                            expr();
                            bCalledExprFromStmts = false;
                            // Check that the function statement ended with ';'
                            if(! scan.getNext().equals(";"))
                            {
                                error("Expected ';' after call to function '%s'", functionName);
                            }
                            break;
                        default:
                            // Only reached if we add a built-in function but haven't called it here
                            error("Unknown built-in function: '%s'", scan.currentToken.tokenStr);
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
                            + ", found '%s' type", resCond.type);
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
                        error("Expected a primitive value as the 'for' loop increment amount, found array '%s'", resLimit.value);
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
                    ResultValue resControlVarVal = symbolTable.retrieveVariableValue(this, variableStr);
                    if(resControlVarVal == null)
                    {
                        error("Control variable '%s' was re-declared but not initialized", variableStr);
                    }
                    
                    // Check that the control variable is still primitive and of type int
                    if(resControlVarVal.structure != STIdentifier.PRIMITVE)
                    {
                        error("Control variable '%s' was redeclared as an array, must be primitive", variableStr);
                    }
                    Utility.coerce(this, Token.INTEGER, resControlVarVal, "for loop control variable");
                    
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
                    
                    for(int i = 0; i < iForIterateNum; i++)
                    {
                        // Get the current element of the array
                        ResultValue resArrayElem = resArray.valueList.get(i);
                        
                        // Only iterate if there was actually an element at that index
                        if(resArrayElem != null)
                        {
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
                
                // TODO continue
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
            Utility.coerce(this, Token.INTEGER, resIndex, "declared size of array");
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
                
                // Get the target variable and check that it has been declared
                STIdentifier STVariable = (STIdentifier) symbolTable.getSymbol(variableStr);
                if(STVariable == null)
                {
                    error("Variable '%s' has not been declared", variableStr);
                }
                // Get the source of the assignment
                resAssign = expr();
                
                // For the first, second, and third assignment types, the target will involve an array
                if(STVariable.structure != STIdentifier.PRIMITVE)
                {
                    // 1) If the source is an array, then we have array to array assignment
                    if(resAssign.structure != STIdentifier.PRIMITVE)
                    {
                        // TODO Need to add coercion for array to array assignments
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
                        
                        // Get the part of the original string up until where the new strings starts (may be empty string)
                        String beginning = resString.value.substring(0, numIndex.integerValue);
                        
                        // Get the part of the original string after the end of the inserted string (may be empty string)
                        int iStartOfEnd = beginning.length() + resAssign.value.length();
                        String end = "";
                        if(iStartOfEnd < resString.value.length())
                        {
                            end = resString.value.substring(iStartOfEnd, resString.value.length()); 
                        }
                        
                        // Create the new string
                        resString.value = beginning + resAssign.value + end;
                    }
                    // 5) Otherwise, this is a regular assignment to a primitive
                    else
                    {
                        // Ensure that the value is the same type as the variable
                        Utility.coerce(this, STVariable.dclType, resAssign, "=");
                        symbolTable.storeVariableValue(this, variableStr, resAssign);
                    }
                }
                break;
                
            case "-=": // TODO Does not work with arrays
                resOp2 = expr();
                // Get the value of the target variable
                resOp1 = symbolTable.retrieveVariableValue(this, variableStr);
                // Subtract second operand from first operand
                resAssign = Utility.subtract(this, resOp1, resOp2, "-=");
                // Assign the result to the variable
                symbolTable.storeVariableValue(this, variableStr, resAssign);
                break;
            case "+=": // TODO Does not work with arrays
                resOp2 = expr();
                // Get the value of the target variable
                resOp1 = symbolTable.retrieveVariableValue(this, variableStr);
                // Add both operands
                resAssign = Utility.add(this, resOp1, resOp2, "+=");
                // Assign the result to the variable
                symbolTable.storeVariableValue(this, variableStr, resAssign);
                break;
            default:
                error("Expected assignment operator, found '%s'", operatorStr);
                resAssign = new ResultValue(); // This will never be reached
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
        String exprDelimiters = ",:;="; // The delimiters for an expression
        
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
        
        // Get the next token as long as it isn't an expression delimiter
        while((scan.currentToken.primClassif != Token.CONTROL) && (exprDelimiters.indexOf(scan.currentToken.tokenStr) < 0))
        {
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
                    // Now we are expecting the next token to be an operand or a binary operator
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
                            
                            // In the case of function, the final ')' acts as a delimiter for the parameters
                            // which may be multiple expressions. In this case, we don't want to find the
                            // matching '(' (which would be the one right after the function name). So, a
                            // function call will indicate that the last ')' is special through the
                            // bExpectingRtParen value, and we find that ')' because it will be followed by
                            // a ':' or ';'
                            if(bExpectingRtParen && (":;".indexOf(scan.nextToken.tokenStr) >= 0))
                            {
                                // Re-classify this token to be the special kind of ')'
                                scan.currentToken.primClassif = Token.RT_PAREN;
                                // Indicate that we found such a ')'
                                bFoundRtParen = true;
                                break;
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
                                if(popped.primClassif == Token.FUNCTION)
                                {
                                    bFoundParen = true;
                                    outList.add(popped);
                                    break;
                                }
                                
                                // Find a '[' before the '(' should be an error. One example:
                                //    4 * (3 + arr[x * y)]
                                if((popped.primClassif == Token.OPERAND) && (popped.subClassif == Token.IDENTIFIER))
                                {
                                    error("Expected ']' before ')'");
                                }
                                outList.add(popped);
                            }
                            
                            // TODO error message may change
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
                                
                                // Finding a '(' before the '[' should be an error. One example:
                                //    arr[x * (4 + y])
                                if(popped.tokenStr.equals("("))
                                {
                                    error("Expected ')' before ']'");
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
                        default:
                            error("Invalid separator, found '%s'", token.tokenStr);
                    }
                    break;  //Break out of case Token.SEPARATOR
                    
                case Token.FUNCTION:
                    // Since a function will return an operand, the occurrence of
                    // a function should be when an operand is expected
                    if(! bExpectingOperand)
                    {
                        error("Expecting an operator, found '%s'", token.tokenStr);
                    }
                    // Since we will throw away the '(', the next expected infix token should be an operand as well
                    
                    // Function is a built-in function
                    if(token.subClassif == Token.BUILTIN)
                    {
                        // Execute the appropriate function
                        switch(token.tokenStr)
                        {
                            // The 'debug' and 'print' functions are statements and
                            // should only be called from 'statements'
                            case "debug":
                                error("Call to 'debug' function should not be nested inside an expression");
                                break;
                            case "print":
                                error("Call to 'print' function should not be nested inside an expression");
                                break;
                            case "LENGTH":
                            case "SPACES":
                            case "ELEM":
                            case "MAXELEM":
                                // Check that there is a '(' after the function name
                                if(! scan.getNext().equals("("))
                                {
                                    error("Expected '(' after '%s'", token.tokenStr);
                                }
                                
                                // The function name will act as the '('
                                postfixStack.push(token);
                                break;
                            default:
                                // Only reached if we add a built-in function but haven't called it here
                                error("Unknown built-in function: '%s'", scan.currentToken.tokenStr);
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
            // Should not have any left parenthesis
            if(popped.tokenStr.equals("("))
            {
                // The second part of this error message is in regards to a
                // possible error from an expression call from a print statement
                errorLineNr(popped.iSourceLineNr, "Missing ')'. If not, may be missing ';' or ':'");
            }
            
            // Also should not have any left brackets (i.e., an array element reference)
            if(popped.identifierType == Token.ARRAY_ELEM)
            {
                errorLineNr(popped.iSourceLineNr, "Missing ']'");
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
        /*
        //-----
        System.err.println("---start---");
        for(Token t : outList)
        {
            System.err.println(t.tokenStr);
        }
        System.err.println(scan.currentToken.tokenStr);
        System.err.println(scan.currentToken.primClassif);
        System.err.println("----end----");
        //-----
        */
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
                                // TODO Use storage manager to get the element from the array and put back on stack
                                // MAKE SURE TO GET A COPY AND NOT A REFERENCE
                                
                                // Get the reference to the array's element at index 'resIndex'
                                ResultValue resArrayElemRef;
                                resArrayElemRef = symbolTable.storageManager.getArrayElem(this, outToken.tokenStr, resIndex);
                                
                                // We need a copy of the array's element, not a reference
                                ResultValue resArrayElemCopy = Utility.getResultValueCopy(resArrayElemRef);
                                
                                // Put that copy of the array's element on the evaluation stack
                                resultStack.push(resArrayElemCopy);
                            }
                        }
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
                                // This error message would only occur if we added a new operator to the language
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
                                resultStack.push(Utility.compare(this, Utility.GREATER_THAN_EQUAL, resOp1, resOp2));
                                break;
                                
                            default:
                                // This error message would only occur if we added a new operator to the language
                                // and forgot to add its appropriate case in this switch statement
                                errorWithCurrent("Unrecognized operator, found '%s'", outToken.tokenStr);
                        }
                    }
                    break;
                    
                case Token.FUNCTION:
                    // Function is a built-in function
                    if(outToken.subClassif == Token.BUILTIN)
                    {
                        ResultValue resOp; // Used as the parameter for the function
                        ResultArray resArrayOp; // Used for functions that require an array parameter
                        
                        // Execute the appropriate function
                        switch(outToken.tokenStr)
                        {
                            case "LENGTH":
                                resOp = resultStack.pop();
                                resultStack.push(Utility.LENGTH(this, resOp));
                                break;
                            case "SPACES":
                                resOp = resultStack.pop();
                                resultStack.push(Utility.SPACES(this, resOp));
                                break;
                            case "ELEM":
                                resOp = resultStack.pop();
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
                                // Check that the operand is an array
                                if(! (resOp instanceof ResultArray))
                                {
                                    error("Expected an array reference for the parameter to 'MAXELEM', found '%s'", resOp.value);
                                }
                                // Get the operand as a result array
                                resArrayOp = (ResultArray) resOp;
                                resultStack.push(Utility.MAXELEM(this, resArrayOp));
                                break;
                            default:
                                // Only reached if we add a built-in function but haven't called it here
                                error("Unknown built-in function: '%s'", scan.currentToken.tokenStr);
                        }
                    }
                    // Function is a user-defined function
                    else
                    {
                        error("User-defined functions have not been implemented yet");
                    }
                    break;
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
        if(! Arrays.asList("Assign", "Expr", "Token").contains(debugType))
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
            default:
                // Only reached if we add another debugger type and don't check for it
                error("The case for debug type '%s' was never added to the debug method...", debugType);
        }
    }
    
    /**
     * Prints a variable number of comma-separated expressions
     * Assumption: current token is on a "print" token
     * <p>
     * This function will continually make a call to expr
     * and print out the result for every comma-separated
     * expression, until it finds the matching ')'
     * @param bExec      whether or not we will execute the statements
     * @throws Exception
     */
    public void print(boolean bExec) throws Exception
    {
        int iPrintLineNr; // line number for beginning of print statement
        iPrintLineNr = scan.currentToken.iSourceLineNr;
        
        // Do we need to ignore execution of the printing?
        if(! bExec)
        {
            // Skip to the end of the print statement
            skipTo(iPrintLineNr, "print", ";");
            return;
        }
        
        // Get the next token and make sure it is "("
        if(! scan.getNext().equals("("))
        {
            error("Expected '(' for 'print' function");
        }
        
        // Indicate to the parser that we are expecting the special ')' at the end
        this.bExpectingRtParen = true;
        
        boolean bFirstTime; // Need to check that each expression was delimited by ',', except for the
                            // first time we enter the loop, since the current token will be on a '('
        bFirstTime = true;
        
        // Print each comma-separated expression until we hit the special ')' before the ';'
        while(scan.currentToken.primClassif != Token.RT_PAREN)
        {
            // If this is not the first time in the loop, then the
            // previous expression should have been terminated by ','
            if(! bFirstTime)
            {
                if(! scan.currentToken.tokenStr.equals(","))
                {
                    error("Unexpected delimiter in parameter to function 'print', found '%s'", scan.currentToken.tokenStr);
                }
            }
            // If it is the first time, just continue
            else
            {
                bFirstTime = false;
            }
            
            ResultValue resVal = expr();
            System.out.printf("%s ", resVal.value);
            
            // TODO check that the comma was delimiting
            
            // If the scanner is printing token information, print a
            // '\n'; otherwise, there will be output formatting errors
            if(scan.bShowToken)
            {
                System.out.printf("\n");
            }
        }
        
        // The parser should no longer be expecting a RT_PAREN
        this.bExpectingRtParen = false;
        
        // Print the newline, unless we had already printed one because
        // the scanner was printing token info
        if(! scan.bShowToken){
            System.out.print("\n");
        }
        
        // Check that the print statement was ended by ';'
        if(! scan.getNext().equals(";"))
        {
            error("Expected ';' after 'print' statement");
        }
    }
}
