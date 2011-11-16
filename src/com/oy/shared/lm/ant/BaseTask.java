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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

public abstract class BaseTask extends Task {

	private List filesets = new ArrayList();
	
    protected TaskOptions options = new TaskOptions();
 
    public void addFileset(FileSet set) {
        filesets.add(set);
    }
    
    public void setInFile(String  value){ options.inFile = value; };
    public void setDotFile(String  value){ options.dotFile = value; };
    public void setOutFile(String  value){ options.outFile = value; };
    public void setExeFile(String  value){ options.exeFile = value; };    
    public void setColors(String value){ options.colors = value; };
    public void setIncludes(String  value){ options.includes = value; };
    public void setExcludes(String  value){ options.excludes = value; };
    public void setRotated(boolean value){ options.rotated = value; };
    public void setDetailed(boolean value){ options.detailed = value; };
    public void setCaption(String value){ options.caption = value; };
    public void setAttr(String value){ options.attr = value; };
    public void setNodeAttr(String value){ options.nodeAttr = value; };
    public void setEdgeAttr(String value){ options.edgeAttr = value; };       
    public void setFontName(String value){ options.fontName = value; };
    public void setFontSize(int value){ options.fontSize = value; };
    public void setExpandEntityRef(boolean value) { options.expandEntityRef = value; }
    public void setQualifiedNames(boolean value) { options.qualifiedNames = value; }
     
    protected void assertFileNameSet(String [] fileNames, String hint){
    	for (int i=0; i < fileNames.length; i++){
    		assertFileName(fileNames[i], hint);
    	}
    }
    
    protected void assertFileName(String fileName, String hint){
		if (!checkFileName(fileName)){
			throw new BuildException("Bad file name for " + hint + ".");
		}    	 
    }
    
    protected boolean checkFileName(String fileName){
    	boolean notValid = fileName == null || fileName.equals("");
    	return !notValid;
    }
    
    public void execute() {    	
    	try {
    		assertFileName(options.dotFile, "DOT file");
    		assertFileName(options.outFile, "OUT file");
    		            		
    		// create files name from file sets 
    		List files = new ArrayList();
            for (int i = 0; i < filesets.size(); i++) {
            	FileSet fs = (FileSet) filesets.get(i);     		
	    		if (fs != null){
	    			DirectoryScanner ds = fs.getDirectoryScanner(getProject());
	    			File dir = fs.getDir(getProject());
	    			String [] fileNames = ds.getIncludedFiles();
	    			for (int j=0; j < fileNames.length; j++){
	    				File fileName = new File(
    						dir.getAbsolutePath() + 
    						File.separatorChar + 
    						fileNames[j]
                        );
	    				files.add(fileName.getAbsolutePath());
	    			}
	    		} 
            }
            options.inFileSet = (String []) files.toArray(new String [] {});
    		
    		File exe = new File(options.exeFile);
    		if (!exe.exists()){
    			throw new BuildException("File " + options.exeFile + " does not exists, expecting fully qualified name for \"dot.exe\" from Graphviz distribution.");
    		}
    		
    		processInFile();
    	} catch (Exception e) {
    		e.printStackTrace();
			throw new BuildException("Error executing " + getClass() + " while processing input file " + options.inFile);    		
    	}
    }
    
    public abstract void processInFile() throws Exception;

}
