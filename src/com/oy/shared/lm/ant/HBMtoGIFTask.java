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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.oy.shared.lm.graph.IGraphModel;
import com.oy.shared.lm.in.HBMtoGRAPH;
import com.oy.shared.lm.out.GRAPHtoDOTtoGIF;

public class HBMtoGIFTask extends BaseTask {
 	
	public void processInFile() throws Exception {    
		
		// append file name to the list
		if (checkFileName(options.inFile)){			
			List all = new ArrayList(); 
			all.add(options.inFile);
			all.addAll(Arrays.asList(options.inFileSet));
			options.inFileSet = (String []) all.toArray(new String [] {}); 
		}
		assertFileNameSet(options.inFileSet, "Hibernate mapping file");		
		
		IGraphModel graph = HBMtoGRAPH.load(options);    	
		GRAPHtoDOTtoGIF.transform(graph, options.dotFile, options.outFile, options.exeFile);
    }
    
}
