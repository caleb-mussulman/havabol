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
    
    
    public void error(String format, Object... varArgs) throws Exception
    {
        String diagnosticTxt = String.format(format, varArgs);
        throw new ParserException(this.iLineNr, diagnosticTxt, this.sourceFileNm);
    }
    
}
