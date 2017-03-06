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
            // return ifStmts();
        }
        // Current token is start of while statement
        else if(scan.currentToken.tokenStr.equals("while"))
        {
            // return whileStmts();
        }
        // Current token is start of assignment statement
        else if((scan.currentToken.primClassif == Token.OPERAND) && (scan.currentToken.subClassif == Token.IDENTIFIER))
        {
            // return assignStmts();
        }
        // Current token is start of undefined statement
        else
        {
            error("unknown statement type: '%s'", scan.currentToken.tokenStr);
        }
        return null; // This will never be reached  
    }
}
