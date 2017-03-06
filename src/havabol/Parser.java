package havabol;

public class Parser
{
    
    public int iLineNr;
    public String sourceFileNm;
    public Scanner scan;
    public SymbolTable symbolTable;
    
    Parser(Scanner scan, SymbolTable symbolTable)
    {
        this.scan = scan;
        this.symbolTable = symbolTable;
        this.sourceFileNm = scan.sourceFileNm;
        this.iLineNr = 0; // THIS WILL CHANGE WHEN I ADD statements()
                          // not yet sure how this will be incremented
    }
    
    // This is a temporary method so we can still see the token output
    public void parse()
    {
        try
        {
            while (! scan.getNext().isEmpty())
            {
                // Print the final token result in table format
                scan.currentToken.printToken();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * A method for handling errors while parsing and exiting execution
     * <p>
     * The error method is simply for making error handling easier while
     * programming other parts of the code. It takes a string format and a variable
     * number of arguments depending on the format string and then throws a
     * ParserException using these values
     * @param format     the format string to print out
     * @param varArgs    the corresponding values to match with the format
     *                   specifiers in the format string
     * @throws Exception well, yeah...
     */
    public void error(String format, Object... varArgs) throws Exception
    {
        String diagnosticTxt = String.format(format, varArgs);
        throw new ParserException(this.iLineNr, diagnosticTxt, this.sourceFileNm);
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
        // Get the next token
        scan.getNext();
        
        // Current token is start of if statement
        if(scan.currentToken.tokenStr.equals("if"))
        {
            // return ifStmt();
        }
        // Current token is start of while statement
        else if(scan.currentToken.tokenStr.equals("while"))
        {
            // return whileStmt();
        }
        // Current token is start of assignment statement
        else if((scan.currentToken.primClassif == Token.OPERAND) && (scan.currentToken.subClassif == Token.IDENTIFIER))
        {
            // return assignStmt();
        }
        // Current token is start of undefined statement
        else
        {
            error("unknown statement type: '%s'", scan.currentToken.tokenStr);
        }
        return null; // This will never be reached  
    }
    
    public ResultValue ifStmt(boolean bExec)
    {
        ResultValue resTrueStmts;  // block of code after the 'if'
        ResultValue resFalseStmts; // block of code after the 'else'
        
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
                        errorWithCurrent("Expected ':' after 'else'");
                    }
                    resFalseStmts = statements(false);
                    
                    // 'if' control block must end with 'endif'
                    if(! resFalseStmts.terminatingStr.equals("endif"))
                    {
                        errorWithCurrent("Expected 'endif'");
                    }
                }
                // If it is not an 'else', then it must be an 'endif'
                else if(! resTrueStmts.terminatingStr.equals("endif"))
                {
                    errorWithCurrent("Expected 'endif'");
                }
                // 'endif' must be followed by a ';'
                if(! scan.getNext().equals(";"))
                {
                    errorWithCurrent("Expected ';' after 'endif'");
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
                        errorWithCurrent("Expected ':' after 'else'");
                    }
                    resFalseStmts = statements(true);
                    
                    // 'if' control block must end with 'endif'
                    if(! resFalseStmts.terminatingStr.equals("endif"))
                    {
                        errorWithCurrent("Expected 'endif'");
                    }
                }
                // If it is not an 'else', then it must be an 'endif'
                else if(! resTrueStmts.terminatingStr.equals("endif"))
                {
                    errorWithCurrent("Expected 'endif'");
                }
                // 'endif' must be followed by a ';'
                if(! scan.getNext().equals(";"))
                {
                    errorWithCurrent("Expected ';' after 'endif'");
                }
            }
        }
        else
        {
            // We are ignoring execution
            
            // Skip the 'if' condition
            skipTo("if", ":");
            
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
                    errorWithCurrent("Expected ':' after 'else'");
                }
                resFalseStmts = statements(false);
                
                // 'if' control block must end with 'endif'
                if(! resFalseStmts.terminatingStr.equals("endif"))
                {
                    errorWithCurrent("Expected 'endif'");
                }
            }
            // If it is not an 'else', then it must be an 'endif'
            else if(! resTrueStmts.terminatingStr.equals("endif"))
            {
                errorWithCurrent("Expected 'endif'");
            }
            // 'endif' must be followed by a ';'
            if(! scan.getNext().equals(";"))
            {
                errorWithCurrent("Expected ';' after 'endif'");
            }
        }
    }
}
