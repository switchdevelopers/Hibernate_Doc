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

package com.oy.shared.lm.in;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.oy.shared.lm.ant.TaskOptions;
import com.oy.shared.lm.graph.Graph;
import com.oy.shared.lm.graph.GraphEdge;
import com.oy.shared.lm.graph.GraphFactory;
import com.oy.shared.lm.graph.GraphNode;
import com.oy.shared.lm.graph.IGraphModel;

import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDAttribute;
import com.wutka.dtd.DTDCardinal;
import com.wutka.dtd.DTDChoice;
import com.wutka.dtd.DTDElement;
import com.wutka.dtd.DTDItem;
import com.wutka.dtd.DTDMixed;
import com.wutka.dtd.DTDName;
import com.wutka.dtd.DTDSequence;

public class DTDtoGRAPH {

	private Graph graph = GraphFactory.newGraph(); 
	private Map nameToNode = new HashMap();
	private TaskOptions options;
	
	private String [] colors = new String [] {
		"yellow",	
		"red", 	
		"black",	
		"blue"		
	};
	
	final static int COLOR_NORMAL = 0;
	final static int COLOR_LINE_CHOICE = 1;
	final static int COLOR_LINE_SEQUENCE = 2;
	final static int COLOR_LINE_MIXED = 3;

	
	
	DTDtoGRAPH(){ 
	}
	 
	public static IGraphModel load(TaskOptions options) throws IOException, SAXException, ParserConfigurationException {
		return new DTDtoGRAPH().innerLoad(options);
	}
	
	private IGraphModel innerLoad(TaskOptions options) throws IOException, SAXException, ParserConfigurationException {
		this.options = options;
		
		options.applyTo(graph);
		
		StringUtil.splitAndFill(options.colors, ",", this.colors);
		
		DTD doc = XMLUtil.parseDTD(options.inFile);
						
		// parse document, add all targets and all target dependencies
		processEntitiesList(doc.elements.elements());
		
		// remove excluded nodes
		StringUtil.filterNodesByName(nameToNode, options.includes, options.excludes, graph);
		
		return graph;
	}
	
	class Dependency {
		private String name;
		private String cardinality;
		private boolean isChoice;
		private boolean isSequence;
	}
	
	private void walkDependencyTree(DTDItem item, List list, boolean isChoice, boolean isSequence){
		if (item instanceof DTDChoice) {
            DTDItem[] items = ((DTDChoice) item).getItems();
            for (int i=0; i < items.length; i++) {
                walkDependencyTree(items[i], list, true, false);
            }
        }
        if (item instanceof DTDSequence) {
            DTDItem[] items = ((DTDSequence) item).getItems();
            for (int i=0; i < items.length; i++) {
            	walkDependencyTree(items[i], list, false, true);
            }
        }
        if (item instanceof DTDMixed) {
            DTDItem[] items = ((DTDMixed) item).getItems();
            for (int i=0; i < items.length; i++) {
            	walkDependencyTree(items[i], list, false, false);
            }
        }
        
        if (item instanceof DTDName) {
        	DTDName name = (DTDName) item;
        	Dependency depends = new Dependency ();
        	depends.isChoice = isChoice;
        	depends.isSequence = isSequence;
        	depends.name = name.value;
        	
        	depends.cardinality = "";
        	if (item.cardinal == DTDCardinal.OPTIONAL){
        		depends.cardinality = "?";
        	}
        	if (item.cardinal == DTDCardinal.ZEROMANY){
        		depends.cardinality = "*";
        	}
        	if (item.cardinal == DTDCardinal.ONEMANY){
        		depends.cardinality = "+";
        	}
        	
        	list.add(depends);
        }
	}
	
	private void processEntitiesList(Enumeration elements){
		
		while(elements.hasMoreElements()){
            DTDElement entity = (DTDElement) elements.nextElement();
            
			String name = entity.getName();
			GraphNode current = getOrCreateNode(name);
			
			// configure the node
			styleNode(current, name);
		
			// configure attributes
			String label = "";
			Enumeration attrs = entity.attributes.elements();
			while(attrs.hasMoreElements()){
                DTDAttribute attr = (DTDAttribute) attrs.nextElement();
                label = label + attr.getName() + "\n";  
			}
			if (options.detailed){
				current.getInfo().setCaption(label);
			}
			
			// configure dependencies
			DTDItem item = entity.getContent();
			List list = new ArrayList(); 
			walkDependencyTree(item, list, false, false);
			for (int i=0; i < list.size(); i++){
				Dependency depends = (Dependency) list.get(i);
				
				GraphNode dependant = getOrCreateNode(depends.name);
				GraphEdge edge = graph.addEdge(current, dependant);
				edge.getInfo().setHeadCaption(depends.cardinality);				
				
				if (depends.isChoice){
					edge.getInfo().setModeDashed();
					edge.getInfo().setLineColor(colors[COLOR_LINE_CHOICE]);
				}  else {
					if (depends.isSequence){
						edge.getInfo().setLineColor(colors[COLOR_LINE_SEQUENCE]);
					} else {
						edge.getInfo().setLineColor(colors[COLOR_LINE_MIXED]);
					}
				}
			}
		}
	}
	
	private GraphNode getOrCreateNode(String name){
		name = name.trim();
		
		GraphNode node = (GraphNode) nameToNode.get(name);
		if (node == null){
			node = graph.addNode();
			nameToNode.put(name.trim(), node);
		} 
		
		return node;
	}
	
	private void styleNode(GraphNode node, String name){
		node.getInfo().setFillColor(colors[COLOR_NORMAL]);
		node.getInfo().setHeader(name);
		node.getInfo().setModeSolid();
	}
		
}
