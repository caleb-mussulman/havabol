/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package havabol;

/**
 *
 * @author root
 */
public class STControl extends STEntry
{
   
    int subClassif;
    public static final int FLOW = 1;
    public static final int END = 2;
    public static final int DELCARE = 3;  
    
    public STControl(String symbol, int primClassif, int type)
    {
        super(symbol, primClassif);
        // TODO Auto-generated constructor stub
    }

}
