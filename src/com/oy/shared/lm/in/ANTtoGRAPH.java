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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.oy.shared.lm.ant.TaskOptions;
import com.oy.shared.lm.graph.Graph;
import com.oy.shared.lm.graph.GraphEdge;
import com.oy.shared.lm.graph.GraphFactory;
import com.oy.shared.lm.graph.GraphGroup;
import com.oy.shared.lm.graph.GraphNode;
import com.oy.shared.lm.graph.IGraphModel;

public class ANTtoGRAPH {

	private Graph graph = GraphFactory.newGraph(); 
	private Map nameToNode = new HashMap();
	private Map nameToGroup = new HashMap();
	private TaskOptions options;
	
	private String [] colors = new String [] {
		"yellow", 
		"lightyellow",	
		"gold",
		"black",
		"black",
		"black",
		"lightblue"
	};
	
	final static int COLOR_NORMAL = 0;
	final static int COLOR_CONDITIONAL = 1;
	final static int COLOR_DEFAULT = 2;
	final static int COLOR_LINE_NORMAL = 3;
	final static int COLOR_LINE_ANTCALL = 4;
	final static int COLOR_LINE_ANT = 5;
	final static int COLOR_GROUP = 6;
	
	ANTtoGRAPH(){ 
	}
	
	public static IGraphModel load(TaskOptions options) throws IOException, SAXException, ParserConfigurationException {
		return new ANTtoGRAPH().innerLoad(options);
	}
	
	private IGraphModel innerLoad(TaskOptions options) throws IOException, SAXException, ParserConfigurationException {
		this.options = options; 
		
		options.applyTo(graph);
		
		StringUtil.splitAndFill(options.colors, ",", this.colors);		
		
		Document doc = XMLUtil.parse(options.expandEntityRef, false, options.inFile);
		XMLUtil.assertRootElement(doc, "project");
			
		// parse document, add all targets and all target dependencies
		processTargetList(XMLUtil.getChildNodesWithName(doc.getDocumentElement(), "target"));
		
		// color default
		String def = XMLUtil.getAttributeValue(doc.getDocumentElement(), "default", "");
		GraphNode defNode = (GraphNode) nameToNode.get(def);
		if (defNode != null){
			defNode.getInfo().setFillColor(colors[COLOR_DEFAULT]);
		}
		
		// simplify graph to remove unneeded edges
		simplifyGraph();
		
		// remove excluded nodes
		StringUtil.filterNodesByName(nameToNode, options.includes, options.excludes, graph);
				
		return graph;
	}
	
	private void simplifyGraph(){
		//
		// at this popint we have too many edges; for example
		// {A -> B, B -> C, A -> C}
		// we need to simplify this graph by removing {A -> C} dependency
		// as it can be derived from {A -> B, B -> C};
		// in plain english this means that we must kee the 
		// shortest paths between nodes; ot in other words "if node A
		// depends directly and B and indirectly on C, and node B
		// indirectly depends on C, indirect dependency of A on C must be 
		// removed
		//
		
		// incomplete
		;
	}
	
	private void processTargetList(List nodes){
		for (int i=0 ;i < nodes.size(); i++){
			Node node = (Node) nodes.get(i);
			
			String name = XMLUtil.getAttributeValue(node, "name", "<name>");
			String desc = XMLUtil.getAttributeValue(node, "description", null);
			String ifText = XMLUtil.getAttributeValue(node, "if", "");
			String unlessText = XMLUtil.getAttributeValue(node, "unless", "");
			
			GraphNode current = getOrCreateNode(name);
			styleNode(current, name, desc);			
			
			// set footer
			String footer = "";
			if (!ifText.equals("")){
				footer += "if(" + ifText + ")";
			}
			if (!unlessText.equals("")){
				footer += " unless(" + unlessText + ")";
			}			
			current.getInfo().setFooter(footer);
			
			// set style if conditional
			boolean conditional = !ifText.equals("") || !unlessText.equals("");
			if (conditional){
				current.getInfo().setFillColor(colors[COLOR_CONDITIONAL]);
			}
			
			// configure <antcall> dependencies
			List antcall = XMLUtil.getChildNodesWithName(node, "antcall");
			processAntcallList(current, antcall);
			
			// configure <ant> dependencies
			List ant = XMLUtil.getChildNodesWithName(node, "ant");
			processAntList(current, ant);
			
			// configure depedencies
			String depends = XMLUtil.getAttributeValue(node, "depends", null);
			if (depends == null || "".equals(depends.trim())){
				;
			} else {				
				String [] dependants = depends.split(",");				
				for (int j=0; j < dependants.length; j++){
					String dname = dependants[j].trim();
					
					GraphNode dependant = getOrCreateNode(dname);					
					styleNode(dependant, dname, null);
					
					GraphEdge edge = graph.addEdge(current, dependant);
					edge.getInfo().setLineColor(colors[COLOR_LINE_NORMAL]);
				}
			}
			
		}
	}
	
	private void processAntcallList(GraphNode parent, List nodes){
		for (int i=0 ;i < nodes.size(); i++){
			Node node = (Node) nodes.get(i); 
			
			String name = XMLUtil.getAttributeValue(node, "target", "<target>");
			
			GraphNode current = getOrCreateNode(name);			
			styleNode(current, name, null);
					
			// configure depedencies
			GraphEdge edge = graph.addEdge(parent, current);
			edge.getInfo().setLineColor(colors[COLOR_LINE_ANTCALL]);
		}
	}	
	
	private void processAntList(GraphNode parent, List nodes){
		for (int i=0 ;i < nodes.size(); i++){
			Node node = (Node) nodes.get(i);
			
			String file = XMLUtil.getAttributeValue(node, "antfile", "<none>");
			String name = XMLUtil.getAttributeValue(node, "target", "<target>");
			
			GraphNode current = getOrCreateNode(file + "." + name);			
			styleNode(current, name, null);
			
			// attach to group
			GraphGroup group = (GraphGroup) nameToGroup.get(file);
			if (group == null){
				group = graph.addGroup();
				group.getInfo().setCaption(file);
				group.getInfo().setModeFilled();
				group.getInfo().setFillColor(colors[COLOR_GROUP]);
				 
				nameToGroup.put(file, group);
			}
			current.setMemberOf(group);
			  
			// configure depedencies
			GraphEdge edge = graph.addEdge(parent, current);
			edge.getInfo().setLineColor(colors[COLOR_LINE_ANT]);
			edge.getInfo().setModeDashed(); 
		}
	}		
	
	private GraphNode getOrCreateNode(String name){
		name = name.trim();
		
		GraphNode node = (GraphNode) nameToNode.get(name);
		if (node == null){
			node = graph.addNode();
			nameToNode.put(name, node);
		} 
		
		return node;
	}
	
	private void styleNode(GraphNode node, String name, String desc){
		node.getInfo().setFillColor(colors[COLOR_NORMAL]);
		
		node.getInfo().setHeader(name);
		
		//if (options.detailed){
			node.getInfo().setCaption(desc);
		//}
		
		node.getInfo().setModeSolid();
	}
		
}

