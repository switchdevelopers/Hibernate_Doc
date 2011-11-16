package com.oy.shared.lm.ant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.oy.shared.lm.ext.HBMCtoGRAPH;
import com.oy.shared.lm.graph.IGraphModel;
import com.oy.shared.lm.in.HBMtoGRAPH;
import com.oy.shared.lm.out.GRAPHtoDOTtoGIF;

public class HBMCtoGIFTask extends BaseTask {

	public void processInFile() throws Exception {    
		
		// append file name to the list
		if (checkFileName(options.inFile)){			
			List all = new ArrayList(); 
			all.add(options.inFile);
			all.addAll(Arrays.asList(options.inFileSet));
			options.inFileSet = (String []) all.toArray(new String [] {}); 
		}
		assertFileNameSet(options.inFileSet, "Hibernate mapping file");		

		//create generic graph 
		IGraphModel graph = HBMCtoGRAPH.load(options);
				
		GRAPHtoDOTtoGIF.transform(graph, options.dotFile, options.outFile, options.exeFile);
    }

}
