package havabol;

import java.util.*;

public class Parser
{
    public String sourceFileNm;
    public Scanner scan;
    public SymbolTable symbolTable;
    public int iParseTokenLineNr;
    
    Parser(Scanner scan, SymbolTable symbolTable)
    {
        this.scan = scan;
        this.symbolTable = symbolTable;
        this.sourceFileNm = scan.sourceFileNm;
    }
    
    // This is a temporary method so we can still see the token output
    public void parse()
    {
        try
        {   /* Previous invocation of havabol
            while (true)
            {
                // Print a column heading 
                System.out.printf("%-11s %-12s %s\n"
                        , "primClassif"
                        , "subClassif"
                        , "tokenStr");
                scan.getNext();
                if(scan.currentToken.primClassif == Token.EOF)
                {
                    break;
                }
                // Print the final token result in table format
                scan.currentToken.printToken();
            }*/
            
            statements(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
     * @throws Exception well, yeah...
     */
    public void errorLineNr(int iLineNr, String format, Object... varArgs) throws Exception
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
     * @throws Exception well, yeah...
     */
    public void errorWithCurrent(String format, Object... varArgs) throws Exception
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
     * @throws Exception well, yeah...
     */
    public void error(String format, Object... varArgs) throws Exception
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
                // resValue's terminating string is initialized to empty for EOF
                return resValue;
            }
            
            // Check if the current token is a end of flow token
            if((scan.currentToken.primClassif == Token.CONTROL) && (scan.currentToken.subClassif == Token.END))
            {
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
            // Current token is start of undefined statement
            else
            {
                error("unknown statement type: '%s'", scan.currentToken.tokenStr);
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
                // Call 'getNext' twice so that 'while' is the current token
                scan.getNext();
                scan.getNext();
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
        
        // Get the assignment operator and check it
        scan.getNext();
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
        ResultValue res02; // Result value of second operand
        ResultValue res01; // Result value of first operand
        Numeric nOp2;      // Numeric value of second operand
        Numeric nOp1;      // Numeric value of first operand
        switch(operatorStr)
        {
            case "=":
                res02 = expr();
                symbolTable.storeVariableValue(this, variableStr, res02);
                break;
            case "-=":
                res02 = expr();
                // Expression must be numeric, raise exception if not
                nOp2 = new Numeric(this, res02, "-=", "2nd operand");
                // Since it is numeric, get the value of the target variable
                res01 = symbolTable.retrieveVariableValue(this, variableStr);
                // Target variable must also be numeric
                nOp1 = new Numeric(this, res01, "-=", "1st operand");
                // Subtract second operand from first operand and assign the result to the variable
                symbolTable.storeVariableValue(this, variableStr, Utility.subtract(this, nOp1, nOp2));
                break;
            case "+=":
                res02 = expr();
                // Expression must be numeric, raise exception if not
                nOp2 = new Numeric(this, res02, "+=", "2nd operand");
                // Since it is numeric, get the value of the target variable
                res01 = symbolTable.retrieveVariableValue(this, variableStr);
                // Target variable must also be numeric
                nOp1 = new Numeric(this, res01, "+=", "1st operand");
                // Add both operands and assign the result to the variable
                symbolTable.storeVariableValue(this, variableStr, Utility.add(this, nOp1, nOp2));
                break;
            default:
                error("Expected assignment operator, found '%s'", operatorStr);
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
     * PROGRAM 3 ONLY: all variables are declared with primitive type
     * TODO NEED TO CHANGE FOR PROGRAM 4
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
        
        // Get the variable to be declared and check it
        scan.getNext();
        if((scan.currentToken.primClassif != Token.OPERAND) || (scan.currentToken.subClassif != Token.IDENTIFIER))
        {
            error("Expected a variable for the target of an declaration");
        }
        
        String variableStr = scan.currentToken.tokenStr;
        
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
        
        // Put the declared variable in the symbol table
        STIdentifier STVariable = new STIdentifier(variableStr, Token.OPERAND, declareType, STIdentifier.NOT_A_PARAMETER
                                                              , STIdentifier.PRIMITVE, STIdentifier.LOCAL);
        symbolTable.putSymbol(variableStr, STVariable);
        
        // Check if the declaration also contains an assignment
        if(scan.nextToken.primClassif == Token.OPERATOR)
        {
            assignStmt(bExec);
            return;
        }
        
        // There was no assignment, so declare statement must be followed by ';'
        if(! scan.getNext().equals(";"))
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
        boolean expectingOperand = true; // Used to determine that the order of operators and operands
                                         // from the infix expression is valid
        
        // Get the next token
        scan.getNext();
        // TODO For now, the delimiter for an expression is only "," ";" or ":"
        // Get the next token as long as it isn't "," ":" or ";"
        while(",:;".indexOf(scan.currentToken.tokenStr) < 0)
        {
            Token token = scan.currentToken;
            switch(token.primClassif)
            {
                case Token.OPERAND:
                    // Error if we were expecting an operator
                    if(! expectingOperand)
                    {
                        error("Expecting an operator, found operand '%s'", token.tokenStr);
                    }
                    // Add the operand to our post-fix list
                    outList.add(scan.currentToken);
                    // Now we are expecting the next token to be a binary operator
                    expectingOperand = false;
                    break;
                    
                case Token.OPERATOR:
                    // Error if we were expecting an operand and we got a binary operator
                    // (e.g., x = 4 * * 5 is incorrect, while x = 4 * - 5 is correct
                    // since the "-" is a unary operator in the second case)
                    if(expectingOperand && token.subClassif == Token.BINARY)
                    {
                        error("Expecting an operand, found operator '%s'", token.tokenStr);
                    }
                    
                    // Pop tokens off the stack while they have a precedence greater
                    // than or equal to the current operator token
                    while(! postfixStack.isEmpty())
                    {
                        
                        if(token.precedence() > postfixStack.peek().stkPrecedence())
                        {
                            break;
                        }
                        //If current tokens precedence is less than or equal stk precedence
                        outList.add(postfixStack.pop());
                    }
                    // Add the operator to our post-fix list
                    postfixStack.push(token);
                    // Now we are expecting the next token to be an operand or a binary operator
                    expectingOperand = true;
                    break;
                    
                case Token.SEPARATOR:
                    switch(token.tokenStr)
                    {
                        case "(":
                            // Error if we were expecting an operator and found a "("
                            // (e.g., 3 (4 + 6) is invalid, while 3 * (4 + 6) is valid)
                            if(! expectingOperand)
                            {
                                error("Expecting an operator before '('");
                            }
                            postfixStack.push(token);
                            break;
                        case ")":
                            // Error if we were expecting an operand and found a ")"
                            // (e.g., 3 * (4 + ) is invalid, while 3 * (4 + 6) is valid)
                            if(expectingOperand)
                            {
                                error("Expecting an operand before ')'");
                            }
                            boolean bFoundParen = false;// Signifies if we found the matching left parenthesis
                            // Remove from the stack until the matching parenthesis is found
                            while(! postfixStack.isEmpty())
                            {
                                Token popped = postfixStack.pop();
                                // Found matching parenthesis
                                if(popped.tokenStr.equals("("))
                                {
                                    bFoundParen = true;
                                    break;
                                }
                                outList.add(popped);
                            }
                            
                            // TODO error message may change
                            if(! bFoundParen)
                            {
                                error("Could not find matching '('");
                            }
                            break;
                        default:
                            error("Invalid separator, found '%s'", token.tokenStr);
                    }
                    break;
                default:
                    error("Unrecognized argument for expression, found '%s'. May be missing ';' or ':'?", token.tokenStr);
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
                error("Missing ')'");
            }
            outList.add(popped);
        }
        
      //TODO-------TEMPORARY------------------
        for(Token current: outList)
        {
            if(current.primClassif == Token.OPERAND)
            {
                System.out.println(current.tokenStr + " " + current.subClassif);
            }
            else
            {
                System.out.println(current.tokenStr);
            }
            
        }
        //--------------------------------------
        
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
                        resultStack.push(symbolTable.retrieveVariableValue(this, outToken.tokenStr));
                    }
                    // Operand is not an identifier, so just put the operand on the result stack
                    else
                    {
                        resultStack.push(outToken.toResultValue());
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
                        ResultValue resop = resultStack.pop();
                        
                        // Determine which unary operation to perform
                        switch(outToken.tokenStr)
                        {
                            case "-":
                                //Utility.unary();
                                break;
                            case "not":
                                //Utility.not();
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
                        
                        switch(outToken.tokenStr)
                        {
                            case "^":
                                break;
                                
                            case "*":
                                break;
                                    
                            case "/":
                                break;
                                    
                            case "+":
                                break;
                                    
                            case "-":
                                break;
                                    
                            case "#":
                                break;
                                    
                            case "<":
                                break;
                                    
                            case ">":
                                break;
                                    
                            case "<=":
                                break;
                                    
                            case ">=":
                                break;
                                    
                            case "==":
                                break;
                                    
                            case "!=":
                                break;
                                    
                            case "and":
                                break;
                                    
                            case "or":
                                break;
                                
                            default:
                                // This error message would only occur if we added a new operator to the language
                                // and forgot to add its appropriate case in this switch statement
                                errorWithCurrent("Unrecognized operator, found '%s'", outToken.tokenStr);
                        }
                    }
                    break;
            }
        }
        //TODO-------TEMPORARY------------------
        ResultValue TEMPRES = new ResultValue();
        return TEMPRES;
        //--------------------------------------
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
}
