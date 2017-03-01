package havabol;

public class Parser
{

    public Scanner scan;
    public SymbolTable symbolTable;
    
    Parser(Scanner scan, SymbolTable symbolTable)
    {
        this.scan = scan;
        this.symbolTable = symbolTable;
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
    
    
}
