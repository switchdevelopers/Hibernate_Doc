/*
	Linguine Maps Programmatic Visualization Library
	Copyright (C) 2005 Pavel Simakov
	http://www.softwaresecretweapons.com
	
	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.
	
	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.
	
	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
*/

package com.oy.shared.lm.out;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DOTtoGIF {
	
    public static final String OS_NAME = System.getProperty("os.name");    
    public static final boolean IS_LINUX = OS_NAME.startsWith("Linux");    
	
	DOTtoGIF(){ }
	
	public static void transform(String dotExeFileName, String dotFileName, String outFormat, OutputStream out) throws IOException {
		new DOTtoGIF().innerTransform(dotExeFileName, dotFileName, outFormat, out);
	}
	
	public static void transform(String dotExeFileName, String dotFileName, String outFileName) throws IOException {
		new DOTtoGIF().innerTransform(dotExeFileName, dotFileName, outFileName);
	}	

	private String getFormatForFile(String outFileName){
		int idx = outFileName.lastIndexOf(".");
		if (idx == -1 || idx == outFileName.length() - 1){
			throw new IllegalArgumentException("Can't determine file name extention for file name " + outFileName); 
		}
		return outFileName.substring(idx + 1);
	}
	
	private String escape(String fileName){
		
		// Linux does not need " " around file names
		if (IS_LINUX){
			return fileName;
		}
		
		// Windows needs " " around file names; actually we do not 
		// need it always, only when spaces are present;
		// but it does not hurt to usem them always
		return "\"" + fileName + "\"";
		
	}
	
	private void innerTransform(String dotExeFileName, String dotFileName, String outFormat, OutputStream out) throws IOException {

		//
		// dot.exe works by taking *.dot file and piping 
		// results into another file, for example:
		// d:\graphviz-1.12\bin\dot.exe -Tgif c:\temp\ManualDraw.dot > c:\temp\ManualDraw.gif
		// so we follow that model here and read stdout until EOF
		// 
	
		final String exeCmd = 
			escape(dotExeFileName) + 
			" -T" + outFormat + " " + 
			escape(dotFileName);			
	
		Process p = Runtime.getRuntime().exec(exeCmd);
		
		InputStream is = p.getInputStream();				
		byte[] buf = new byte[32 * 1024];
        int len;
        while (true) {
        	len = is.read(buf);
        	if (len <= 0){
        		break;
        	}
    		out.write(buf, 0, len);
        }        
        is.close();		
	}

	private void innerTransform(String dotExeFileName, String dotFileName, String outFileName) throws IOException {

		//
		// dot.exe works by taking *.dot file and piping 
		// results into another file, for example:
		// d:\graphviz-1.12\bin\dot.exe -Tgif c:\temp\ManualDraw.dot > c:\temp\ManualDraw.gif
		// so we follow that model here and read stdout until EOF
		// 
	
		final String exeCmd = 
			escape(dotExeFileName) + 
			" -T" + getFormatForFile(outFileName) + " " + 
			escape(dotFileName) +
			" -o " + 
			escape(outFileName);			
	
		Process p = Runtime.getRuntime().exec(exeCmd);
		try {
			p.waitFor();
		} catch(Exception ie){
			System.out.println("Warning: failed to wait for native process to exit...");
		}
	}		
	
}
