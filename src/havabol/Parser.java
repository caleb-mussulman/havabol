package havabol;

public class Parser
{
    public String sourceFileNm;
    public Scanner scan;
    public SymbolTable symbolTable;
    
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
        {
            while (true)
            {
                scan.getNext();
                if(scan.currentToken.primClassif == Token.EOF)
                {
                    break;
                }
                // Print the final token result in table format
                scan.currentToken.printToken();
            }
            //statements(true);
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
        throw new ParserException(iLineNr, diagnosticTxt, this.sourceFileNm);
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
            if((scan.currentToken.primClassif == Token.FLOW) && (scan.currentToken.subClassif == Token.END))
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
                // return whileStmt();
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
     * Assumption: current token is on an 'if'
     * Parses an if statement
     * <p>
     * Called with a value, determining whether the code executed or
     * not. If the code is to be executed, evaluates the conditional
     * expression and executes the code depending on the result. If
     * the code is not to be executed, it will skip through the code
     * until it gets to the matching 'endif'
     * @param  bExec     indicates whether the code should be executed or ignored
     * @throws Exception if the 'if' statement is not ended with an 'endif'
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
    
    // TODO going to wait until he covers this in Flow lecture
    public void whileStmt(boolean bExec) throws Exception
    {
        int iWhileLineNr; // line number that the while statement starts on
        
        iWhileLineNr = scan.currentToken.iSourceLineNr;
        
        // Do we need to evaluate the condition?
        if(bExec)
        {
            // We are executing (not ignoring)
            ResultValue resCond = expr();
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
    
    // TODO
    public ResultValue expr() throws Exception
    {
        // TEMPORARY!!!
        skipTo(0,"expr",":");
        ResultValue tempRes = new ResultValue();
        tempRes.value = "F";
        return tempRes;
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
