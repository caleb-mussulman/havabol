package havabol;

import java.io.*;
import java.util.*;

public class Scanner
{
    public final static String delimiters = " \t;:()\'\"=!<>+-*/[]#,^\n"; // terminate a token
    public final static String whitespace = " \t\n";
    public final static String charOperators = "+-*/<>=!^#";
    public final static String separators = "(),:;[]";
    @SuppressWarnings("serial")
    public final static Map<Character, Character> escapeChars = Collections.unmodifiableMap(new HashMap<Character, Character>(){{
                                                                put('"', '"'); put('\'', '\''); put('\\', '\\');
                                                                put('n', '\n'); put('t', '\t'); put('a', (char)0x07);          }});
    public final static List<String> tokensPrecedingUnaryMinus = Collections.unmodifiableList(Arrays.asList("=", "-=", "+=", "+"
                                                                 , "-", "*", "/", "^", ">", "<", ">=", "<=", "!=", "#", "and"
                                                                 , "or", "not", "if", "select", "while", "when", "(", "[", ","));
    public String sourceFileNm;
    public ArrayList<String> sourceLineM;
    public SymbolTable symbolTable;
    public char[] textCharM;
    public int iSourceLineNr;
    public int iColPos;
    public Token currentToken;
    public Token nextToken;
    public boolean bShowToken; // Determines whether or not to print the current token's information
    public boolean bInDebugStmt; // Used to make sure we don't print token information if
                                      // currently parsing a debug statement
    
    /**
     * Creates a Scanner object for scanning through the given file and
     * populating the given SymbolTable.
     * <p>
     * The given file is opened (if it exists) and all lines are read in to an
     * array list. The first line of input is read in (if it exists) and the first
     * token for the file is fetched by calling the getNext() method.
     * 
     * @param sourceFileNm The name of the source file to be read from
     * @param symbolTable The symbol table to be populated with tokens read from the file
     * @throws Exception if the file is not found
     *                   if there is an I/O error opening or reading from the file
     */
    public Scanner(String sourceFileNm, SymbolTable symbolTable) throws Exception
    {
        this.sourceFileNm = sourceFileNm;
        this.symbolTable = symbolTable;
        this.iSourceLineNr = 0;
        this.iColPos = 0;
        this.sourceLineM = new ArrayList<String>();
        this.bShowToken = false;
        this.bInDebugStmt = true;
        
        // Read all lines from input file
        try(BufferedReader buffReader = new BufferedReader(new FileReader(sourceFileNm)))
        {
            String inputLine;
            while((inputLine = buffReader.readLine()) != null)
            {
                sourceLineM.add(inputLine);
            }
        }
        catch(FileNotFoundException e)
        {
            throw new Exception("Could not find file " + sourceFileNm + "\n" + e);
        }
        catch(IOException e)
        {
            throw new Exception("IO error while opening/reading from file " + sourceFileNm + "\n" + e);
        }
        
        // Initialize first line of input if the file is not empty
        if(! sourceLineM.isEmpty())
        {
            this.textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
        }
        // Otherwise, just initialize to length 0 for a clean exit
        else
        {
            this.textCharM = new char [0];
        }
        
        this.currentToken = new Token();
        this.nextToken = new Token();
        
        // Pre-loaded value to print the first line of input on first call to getNext()
        this.currentToken.iSourceLineNr = -1;
        
        this.getNext();
    }
    
    /**
     * Sets the scanner's current scanning position
     * <p>
     * Takes a token and uses its line number and column position
     * to set the character array to where the scanner should start
     * scanning on that line on the next call to 'getnext()'. These
     * values would have been set in the token when it was scanned.
     * Then calls getNext() twice in order to put that token into
     * the scanner's current token as well as get the correct
     * look-ahead token in the scanner's next token
     * @param positionToken  the token to be used to set the position
     */
    public void setPosition(Token positionToken) throws Exception
    {
        this.iSourceLineNr = positionToken.iSourceLineNr;
        this.iColPos = positionToken.iColPos;
        textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
        this.getNext();
        this.getNext();
    }

    /**
     * Returns the string of the current token for the scanner. It will also
     * prepare the following token for the next call to this function.
     * <p>
     * The return value for the current token has already been determined. The
     * main task for this method is to prepare the following token for the next
     * call to this function. All whitespace will be scanned over until a next
     * token is reached, and that token will be classified and stored.
     * 
     * @return The string value for the current token.
     * @throws Exception if a string literal is not terminated on the same line
     *                   if a numeric constant contains multiple decimals
     *                   if a numeric constant contains invalid characters
     */
    public String getNext() throws Exception
    {
        boolean bFoundDecimal;
        char chCurrentChar;
        char chTokenBegin;
        char[] retCharM;
        int index;
        int iPrintLineNr;
        int iRet;
        int iTokenBeginIndex;
        int iTokenLength;
        String error;
        
        /*
        // If the line numbers between tokens are different, print new line of input.
        if(currentToken.iSourceLineNr != nextToken.iSourceLineNr)
        {
            iPrintLineNr = currentToken.iSourceLineNr;
            
            // Print all lines of input between the two tokens (possible blank lines).
            while(iPrintLineNr <= nextToken.iSourceLineNr && nextToken.primClassif != Token.EOF)
            {
                System.out.printf("%3d %s\n", iPrintLineNr + 2, sourceLineM.get(iPrintLineNr + 1));
                iPrintLineNr++;
            }
            
        }
        */
        currentToken = nextToken;
        nextToken = new Token();
        
        // Go through whitespace and comments until at a token or at the end of the file.
        while(true)
        {
            // Empty line or at the end of a line.
            if(iColPos >= textCharM.length)
            {
                iSourceLineNr++;
                
                // At the end of the file.
                if(iSourceLineNr >= sourceLineM.size())
                {
                    nextToken.tokenStr = "";
                    nextToken.primClassif = Token.EOF;
                    nextToken.iSourceLineNr = this.iSourceLineNr;
                    return currentToken.tokenStr;
                }
                
                // Get the next line and reset the column position.
                textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
                iColPos = 0;
            }
            // On line that contains characters.
            else
            {
                // Check if on whitespace.
                if(whitespace.indexOf(textCharM[iColPos]) > -1)
                {
                    // On whitespace so move forward.
                    while( (iColPos < textCharM.length) && (whitespace.indexOf(textCharM[iColPos]) > -1) )
                    {
                        iColPos++;
                    }
                }
                // Check if at the beginning of comment
                else if( ((iColPos + 1) < textCharM.length) && (textCharM[iColPos] == '/') && (textCharM[iColPos + 1] == '/') )
                {
                    // Skip the rest of the comment by setting the column position past the end of the line.
                    // Next iteration of the loop will get the next line, if it exists
                    iColPos = textCharM.length;
                }
                // On a token
                else
                {
                    break;
                }
            }
        }
        
        // At the beginning of the next token.
        iTokenBeginIndex = iColPos;
        iTokenLength = -1;
        chTokenBegin = textCharM[iTokenBeginIndex];
        
        // If token is a string literal, it will be scanned differently.
        if(chTokenBegin == '\"' || chTokenBegin == '\'')
        {
            iColPos++;
            iRet = 0;
            retCharM = new char[textCharM.length];
            
            // String literal token will have quotes removed so start at next index.
            iTokenBeginIndex = iColPos;
            
            // Try to find the end of the string literal up until the end of the line.
            while(iColPos < textCharM.length)
            {
                // Check if the current character is a backslash
                if(textCharM[iColPos] == '\\')
                {
                    // Go to the character after the backslash
                    iColPos++;
                    
                    // If the character after the backslash is a valid escape character, then replace it
                    // with its single byte hex value
                    if( (iColPos < textCharM.length) && (escapeChars.containsKey(textCharM[iColPos])) )
                    {
                        retCharM[iRet++] = escapeChars.get(textCharM[iColPos++]);
                    }
                    // If the character after the backslash is not a valid escape character, then error
                    else
                    {
                        error = "Line "+ (iSourceLineNr + 1) + " Unknown escape sequence: '\\"
                                + textCharM[iColPos] + "', File: " + sourceFileNm;
                        throw new Exception(error);
                    }
                }
                // If there is a matching quote, then this is the end of the string.
                else if(textCharM[iColPos] == chTokenBegin)
                {
                    // Ending of string literal will not include the quote character.
                    iTokenLength = iRet;
                    iColPos++;
                    break;
                }
                // Otherwise, just go to the next character
                else
                {
                    retCharM[iRet++] = textCharM[iColPos++];
                }
            }
            
            // If the string literal was not ended on the same line.
            if(iTokenLength == -1)
            {
                error = "Line "+ (iSourceLineNr + 1) + ": String literal not terminated on same line, File: " + sourceFileNm;
                throw new Exception(error);
            }
            
            // Initialize token as a String token.
            nextToken.tokenStr = new String(retCharM, 0, iTokenLength);
            nextToken.iSourceLineNr = this.iSourceLineNr;
            nextToken.iColPos = iTokenBeginIndex - 1; // Put the beginning position on the quote
            nextToken.primClassif = Token.OPERAND;
            nextToken.subClassif = Token.STRING;
            
            // The string may be a valid Date token
            if(Utility.isValidDate(nextToken.tokenStr))
            {
            	nextToken.subClassif = Token.DATE;
            }
            
            return currentToken.tokenStr;
        }
        
        // Token is not a string literal, so advance until end of line or a delimiter.
        while( (iColPos < textCharM.length) && (delimiters.indexOf(textCharM[iColPos]) == -1) )
        {
            iColPos++;
        }
        
        // Token is the delimiter itself.
        if(iTokenBeginIndex == iColPos)
        {
            iColPos++;
        }
        
        // Initialize the token.
        iTokenLength = iColPos - iTokenBeginIndex;
        nextToken.tokenStr = new String(textCharM, iTokenBeginIndex, iTokenLength);
        nextToken.iColPos = iTokenBeginIndex;
        nextToken.iSourceLineNr = this.iSourceLineNr;
        
        //Begin SymbolTable classifcations
        // Check if the token is in our global symbol table
        STEntry STEntryResult = symbolTable.getSymbol(nextToken.tokenStr);
        
        // Token has been pre-defined in the global symbol table
        if(STEntryResult != null)
        {
            nextToken.primClassif = STEntryResult.primClassif;
            
            // If token is control, add its type as the subclassification
            if (STEntryResult instanceof STControl)
            {
                nextToken.subClassif = ((STControl) STEntryResult).subClassif;
            }
            // If token is a function, add its return type as the subclassification
            else if(STEntryResult instanceof STFunction)
            {
                nextToken.subClassif = ((STFunction) STEntryResult).subClassif;
                
                // Don't print debug information if in a debug statement
                if(((STFunction) STEntryResult).symbol.equals("debug"))
                {
                    bInDebugStmt = true;
                }
            }
            // If token is an operator, add its number of operands as subclassification
            else if(STEntryResult instanceof STOperator)
            {
                nextToken.subClassif = ((STOperator) STEntryResult).subClassif;
            }
            // If token is an identifier, then it is an OPERAND IDENTIFIER that has
            // already been declared (i.e., it is currently in the symbol table)
            else if(STEntryResult instanceof STIdentifier)
            {
                nextToken.subClassif = Token.IDENTIFIER;
            }
        }   	
        // Token is an operator
        else if( (charOperators.indexOf(nextToken.tokenStr) > -1) )
        {
            nextToken.primClassif = Token.OPERATOR;
            nextToken.subClassif  = Token.BINARY;
            
            // Check if the operator is a two character operator
            if( (iColPos < textCharM.length) && (textCharM[iColPos] == '=') )
            {
                nextToken.tokenStr += "=";
                iColPos++;
            }
            // Check if the operator is minus sign
            else if(nextToken.tokenStr.equals("-"))
            {
                // Determine if the minus sign is a unary minus by checking what token precedes it
                if(tokensPrecedingUnaryMinus.contains(currentToken.tokenStr))
                {
                    nextToken.subClassif = Token.UNARY;
                }
            }
        }
        // Token is a separator
        else if(separators.indexOf(nextToken.tokenStr) > -1)
        {
            nextToken.primClassif = Token.SEPARATOR;
        }
        // its an operand by default.
        else
        {
            nextToken.primClassif = Token.OPERAND;

            // NOT IN HASHTABLE
            // Determine if operand is a numeric constant. Must begin with a digit.
            if( (chTokenBegin >= '0') && (chTokenBegin <= '9') )
            {
                bFoundDecimal = false;
                
                // Check every other character to see it is a digit (possibility of 1 decimal).
                for(index = 1; index < iTokenLength; index++)
                {
                    chCurrentChar = nextToken.tokenStr.charAt(index);
                    
                    // Check if there is a decimal.
                    if(chCurrentChar == '.')
                    {
                        // If there is a decimal, ensure that there is only one.
                        if(! bFoundDecimal)
                        {
                            bFoundDecimal = true;
                        }
                        // There are multiple decimals in the token.
                        else
                        {
                            error = "Line "+ (iSourceLineNr + 1) + " Numeric constant contains multiple decimals: '"
                                    + nextToken.tokenStr + "', File: " + sourceFileNm;
                            throw new Exception(error);
                        }
                    }
                    // Raise an error if there is a non-digit char.
                    else if( (chCurrentChar < '0') || (chCurrentChar > '9') )
                    {
                        error = "Line "+ (iSourceLineNr + 1) + " Numeric constant contains invalid characters: '"
                                + nextToken.tokenStr + "', File: " + sourceFileNm;
                        throw new Exception(error);
                    }
                }
                
                // Token is valid numeric constant. Now determine if it is float or integer.
                if(bFoundDecimal)
                {
                    nextToken.subClassif = Token.FLOAT;
                }
                else
                {
                    nextToken.subClassif = Token.INTEGER;
                }
            }
            // Determine if operand is a boolean constant
            else if(nextToken.tokenStr.equals("T") || nextToken.tokenStr.equals("F"))
            {
                nextToken.subClassif = Token.BOOLEAN;
            }
            // Otherwise token is an identifier
            else
            {
                nextToken.subClassif = Token.IDENTIFIER;
            }
        }
        
        // Print the current token if the debugger is on and the parser
        // is not currently on a debug statement
        if(bShowToken && (! bInDebugStmt))
        {
            System.out.println("\t\t...");
            System.out.print("\t\t");
            currentToken.printToken();
        }
        
        return currentToken.tokenStr;
    }
}

