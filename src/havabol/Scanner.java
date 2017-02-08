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
    public final static Set<String> logicalOperators = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] {"and", "or", "not", "in", "notin"})));
    public final static Set<String> controlFlow      = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] {"if", "else", "while", "for"})));
    public final static Set<String> controlEnd       = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] {"endif", "endwhile", "endfor"})));
    public final static Set<String> controlDeclare   = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] {"Int", "Float", "String", "Bool", "Date"})));
    
    
    public String sourceFileNm;
    public ArrayList<String> sourceLineM;
    public SymbolTable symbolTable;
    public char[] textCharM;
    public int iSourceLineNr;
    public int iColPos;
    public Token currentToken;
    public Token nextToken;
    
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
            textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
        }
        
        this.currentToken = new Token();
        this.nextToken = new Token();
        
        // Pre-loaded value to print the first line of input on first call to getNext()
        currentToken.iSourceLineNr = -1;
        
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
        
        // Covers the case when the file is empty.
        if(sourceLineM.isEmpty())
        {
            return "";
        }
        
        // If the line numbers between tokens are different, print new line of input.
        if(currentToken.iSourceLineNr != nextToken.iSourceLineNr)
        {
            iPrintLineNr = currentToken.iSourceLineNr;
            
            // Print all lines of input between the two tokens (possible blank lines).
            while(iPrintLineNr != nextToken.iSourceLineNr && nextToken.primClassif != Token.EOF)
            {
                System.out.printf("%3d %s\n", iPrintLineNr + 2, sourceLineM.get(iPrintLineNr + 1));
                iPrintLineNr++;
            }
            
        }
        
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
                    // Skip the rest of the comment by getting the next line and resetting the column position.
                    iSourceLineNr++;
                    textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
                    iColPos = 0;
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
            nextToken.iColPos = iTokenBeginIndex;
            nextToken.primClassif = Token.OPERAND;
            nextToken.subClassif = Token.STRING;
            
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
        
        // Token is an operator
        if( (charOperators.indexOf(nextToken.tokenStr) > -1) || (logicalOperators.contains(nextToken.tokenStr)) )
        {
            // Check if the operator is a two character operator
            if( (iColPos < textCharM.length) && (textCharM[iColPos] == '=') )
            {
                nextToken.tokenStr += "=";
                iColPos++;
            }
            nextToken.primClassif = Token.OPERATOR;
        }
        // Token is a separator
        else if(separators.indexOf(nextToken.tokenStr) > -1)
        {
            nextToken.primClassif = Token.SEPARATOR;
        }
        // Token is a control
        else if(controlFlow.contains(nextToken.tokenStr) || controlEnd.contains(nextToken.tokenStr) || controlDeclare.contains(nextToken.tokenStr))
        {
            nextToken.primClassif = Token.CONTROL;
            
            // Control token has a flow subclassification
            if(controlFlow.contains(nextToken.tokenStr))
            {
                nextToken.subClassif = Token.FLOW;
            }
            // Control token has an end subclassification
            else if(controlEnd.contains(nextToken.tokenStr))
            {
                nextToken.subClassif = Token.END;
            }
            // Control token has a declare subclassification
            else
            {
                nextToken.subClassif = Token.DECLARE;
            }
        }
        else
        {
            nextToken.primClassif = Token.OPERAND;
            
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
            // Operand must be an identifier
            else
            {
                nextToken.subClassif = Token.IDENTIFIER;
            }
        }
        return currentToken.tokenStr;
    }
}
