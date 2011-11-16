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

package com.oy.shared.lm.ant;

import com.oy.shared.lm.graph.Graph;

public class TaskOptions {
	
	// in/out
	public String inFile;
	public String [] inFileSet;
	public String dotFile;
	public String outFile;	
    public String exeFile;
    
    // styles
    public String fontName;
    public int fontSize;
    public String colors;
    
    // custom
    public String attr;
    public String nodeAttr;
    public String edgeAttr;
    
    // graph    
    public String caption;    
    public boolean rotated = false;
    public boolean detailed = true;
    public String includes;
    public String excludes;
    public boolean qualifiedNames = true;

    // XML
    public boolean expandEntityRef = false;
	
    public void applyTo(Graph graph){
		graph.getInfo().setRotated(rotated);
		graph.getInfo().setCaption(caption);
		graph.getInfo().setAttributes(attr);
		graph.getInfo().setNodeAttributes(nodeAttr);
		graph.getInfo().setEdgeAttributes(edgeAttr);
		
		if (fontName != null && !fontName.equals("")){
			graph.getInfo().setFontName(fontName);
		}
		
		if (fontSize >= 4 && fontSize <= 72){
			graph.getInfo().setFontSize(fontSize);
		}
    }
    
}
 