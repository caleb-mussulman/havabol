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
