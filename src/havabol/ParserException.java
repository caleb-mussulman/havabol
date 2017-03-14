package havabol;

public class ParserException extends Exception {
    
  // Eclipse won't stop telling me we need this...
  private static final long serialVersionUID = -8791015214876501863L;
  public int iLineNr;
  public String diagnostic;
  public String sourceFileName;
  
  /**
   * 
   * <p>
   * @param iLineNr
   * @param diagnostic
   * @param sourceFileName 
   */
  public ParserException(int iLineNr, String diagnostic, String sourceFileName)
  {
    this.iLineNr = iLineNr;
    this.diagnostic = diagnostic;
    this.sourceFileName = sourceFileName;
  }
  // Exceptions are required to provide tosString()
  public String toString()
  {
      StringBuffer sb = new StringBuffer();      
      sb.append("Line ");
      sb.append(Integer.toString(iLineNr));
      sb.append(" ");
      sb.append(diagnostic);
      sb.append(", File: ");
      sb.append(sourceFileName);
      return sb.toString();
  }
}
