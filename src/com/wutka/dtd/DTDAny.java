package com.wutka.dtd;

import java.io.*;

/** Represents the ANY keyword in an Element's content spec
 *
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/07/29 05:07:35 $ by $Author: pavels $
 */
public class DTDAny extends DTDItem
{
    public DTDAny()
    {
    }

/** Writes "ANY" to a print writer */
    public void write(PrintWriter out)
        throws IOException
    {
        out.print("ANY");
        cardinal.write(out);
    }

    public boolean equals(Object ob)
    {
        if (ob == this) return true;
        if (!(ob instanceof DTDAny)) return false;

        return super.equals(ob);
    }
}