package com.wutka.dtd;

import java.io.*;

/** Defines the method used for writing DTD information to a PrintWriter
 *
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/07/29 05:07:39 $ by $Author: pavels $
 */
public interface DTDOutput
{
    public void write(PrintWriter out) throws IOException;
}
