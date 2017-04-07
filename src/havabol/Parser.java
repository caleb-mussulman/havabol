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
    public boolean bDeclaringArray; // Used when calling 'expr' to parse the declared size of the array
                                    // Indicates that we want to evaluate the expression in the brackets
                                    // and not the array element reference
    
    Parser(Scanner scan, SymbolTable symbolTable)
    {
        this.scan = scan;
        this.symbolTable = symbolTable;
        this.sourceFileNm = scan.sourceFileNm;
        this.bShowAssign = false;
        this.bShowExpr = false;
        this.bExpectingRtParen = false;
        this.bDeclaringArray = false;
    }
    
    // This is a temporary method so we can still see the token output
    public void parse() throws Exception
    {
        statements(true);
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
            // Current token is the start of a function call
            else if(scan.currentToken.primClassif == Token.FUNCTION)
            {
                // Function is a built-in function
                if(scan.currentToken.subClassif == Token.BUILTIN)
                {
                    // Execute the appropriate function
                    switch(scan.currentToken.tokenStr)
                    {
                        case "debug":
                            debug();
                            break;
                        case "print":
                            print(bExec);
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
            
            // The resulting condition must be a boolean value
            if(resCond.type != Token.BOOLEAN)
            {
                errorLineNr(whileToken.iSourceLineNr, "Expected a 'BOOLEAN' type for the evaluation of 'while' statement's condition"
                           + ", found '%s' type", resCond.type);
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
        ResultValue resOp2;    // Result value of second operand
        ResultValue resOp1;    // Result value of first operand
        ResultValue resAssign; // Result Value to be assigned to variable
        switch(operatorStr)
        {
            case "=":
                STIdentifier STVariable = (STIdentifier) symbolTable.getSymbol(variableStr);
                resAssign = expr();
                // If the variable and value types don't match, then attempt to coerce the
                // type of the value to the type of the variable
                if(STVariable.dclType != resAssign.type)
                {
                    Utility.coerce(this, STVariable.dclType, resAssign, "=");
                }
                symbolTable.storeVariableValue(this, variableStr, resAssign);
                break;
            case "-=":
                resOp2 = expr();
                // Get the value of the target variable
                resOp1 = symbolTable.retrieveVariableValue(this, variableStr);
                // Subtract second operand from first operand
                resAssign = Utility.subtract(this, resOp1, resOp2, "-=");
                // Assign the result to the variable
                symbolTable.storeVariableValue(this, variableStr, resAssign);
                break;
            case "+=":
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
            error("Expected a variable for the target of an declaration");
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
            // knowledge of the declared array; so, we declay the array as in the above lines,
            // and set its max size afterwards
            if(bSecondOrThirdType)
            {
                // In these two declaration types, we want to call 'expr' to get the value of
                // the size. In order to make that parsing easier, we need to call 'expr' with
                // the current token on the array identifier; we will let 'expr' know that we
                // only want the value in the brackets, not the array element at the index of
                // that value
                scan.setPosition(declareToken);
                bDeclaringArray = true;
                ResultValue resSize = expr();
                this.iParseTokenLineNr = declareToken.iSourceLineNr;
                System.err.println("Before Utility.coerce call");
                
                Utility.coerce(this, Token.INTEGER, resSize, "declared size of array");
                
                System.err.println("After Utility.coerce call");
                resArray.maxElem = Integer.parseInt(resSize.value);
                System.err.println("Max elem: " + resArray.maxElem);
                bDeclaringArray = false;
            }
            
            // There may be a value list to initialize the array with
            if(scan.currentToken.tokenStr.equals("="))
            {
                // Go to the token after the '='
                scan.getNext();
                
                // Start initializing at index zero
                int iIndex = 0;
                
                // Keep assigning indices of the array, beginning at index zero, as long as there are
                // comma-separated values that are coercible to the type of the array
                do
                {
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
                    
                    // Assign the array element at the current index
                    symbolTable.storageManager.arrayAssign(this, variableStr, resVal, resIndex);
                    
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
        }
        
        // The declaration statement must be followed by ';'
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
        boolean bExpectingOperand = true; // Used to determine that the order of operators and operands
                                         // from the infix expression is valid
        boolean bFoundAnOperator = false; // If the debugger for an expression is turned on, we only want to
                                          // print expression results if they had at least on operator
        boolean bFoundRtParen = false; // Indicates that a ')' before a ':' or ';' was found
        String exprDelimiters = ",:;="; // The delimiters for an expression
        
        // Get the next token
        scan.getNext();
        
        // TODO For now, the delimiter for an expression is only "," ";" or ":"
        // Get the next token as long as it isn't an expression delimiter
        while(exprDelimiters.indexOf(scan.currentToken.tokenStr) < 0)
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
                    
                    // Get the stucture type
                    int iStructure = ((STIdentifier)STEntryResult).structure;
                    
                    // Check if this identifier is an array, array element, 
                    // or just a primitive and set the appropriate token field
                    if(iStructure == STIdentifier.PRIMITVE)
                    {
                        token.identifierType = Token.NOT_AN_ARRAY;
                        // It is just a primitive variable so add it to the post-fix list
                        outList.add(scan.currentToken);
                        // We just got an operand so the next token should not be an operand
                        bExpectingOperand = false;
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
                            // treat the array identifier as an operator an put on the
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
                                
                                // Find a '[' before the '(' should be an error. One example:
                                //    4 * (3 + arr[x * y)]
                                if((popped.primClassif == Token.OPERAND) && (popped.subClassif == Token.IDENTIFIER)
                                                                         && (popped.identifierType == Token.ARRAY_ELEM))
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
                                if((popped.primClassif == Token.OPERAND) && (popped.subClassif == Token.IDENTIFIER)
                                                                         && (popped.identifierType == Token.ARRAY_ELEM))
                                {
                                    bFoundArray = true;
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
        if(bDeclaringArray)
        {
            Token lastToken = outList.get(outList.size() - 1);
            outList.remove(lastToken);
        }
        
        // Evaluate the post-fix expression
        for(Token outToken : outList)
        {
            System.err.println("Token: " + outToken.tokenStr); // TODO remove this
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
            }
        }

        // There may not have even been an expression
        if(resultStack.isEmpty())
        {
            error("Expected an expression before '%s'", scan.currentToken.tokenStr);
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
        
        // Print each comma-separated expression until we hit the special ')' before the ';'
        while(scan.currentToken.primClassif != Token.RT_PAREN)
        {
            ResultValue resVal = expr();
            System.out.printf("%s ", resVal.value);
            
            // If the scanner is printing token information, print a
            // '\n'; otherwise, there will be output formatting errors
            if(scan.bShowToken)
            {
                System.out.printf("\n");
            }
        }
        
        // Print the newline, unless we had already printed one because
        // the scanner was printing token info
        if(! scan.bShowToken){
            System.out.print("\n");
        }
        
        // Move to the ';' after the ')'
        scan.getNext();
    }
}
