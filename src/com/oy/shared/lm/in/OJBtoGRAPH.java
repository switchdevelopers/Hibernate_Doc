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

public class OJBtoGRAPH {

	private StringBuffer sbLabel = new StringBuffer();
	private Graph graph = GraphFactory.newGraph();
	private Map nameToNode = new HashMap();
	private Map nodeToGroup = new HashMap();	
	private TaskOptions options;
	
	private String [] colors = new String [] {
		"yellow", 
		"black",
		"black"	
	};
	
	final static int COLOR_NORMAL = 0;
	final static int COLOR_LINE_REFERENCE = 1;
	final static int COLOR_LINE_COLLECTION = 2;
	
	OJBtoGRAPH(){ 
	}
	
	public static IGraphModel load(TaskOptions options) throws IOException, SAXException, ParserConfigurationException {
		return new OJBtoGRAPH().innerLoad(options);
	}
	
	private IGraphModel innerLoad(TaskOptions options) throws IOException, SAXException, ParserConfigurationException {
		this.options = options;
		
		options.applyTo(graph);

		StringUtil.splitAndFill(options.colors, ",", this.colors);
		
		Document doc = XMLUtil.parse(options.expandEntityRef, false, options.inFile);
		XMLUtil.assertRootElement(doc, "descriptor-repository");
		
		// parse document, add all targets and all target dependencies
		processClassDescList(XMLUtil.getChildNodesWithName(doc.getDocumentElement(), "class-descriptor"));
		
		// remove excluded nodes
		StringUtil.filterNodesByName(nameToNode, options.includes, options.excludes, graph);
				
		return graph;
	}
	
	private void processClassDescList(List nodes){
		for (int i=0 ;i < nodes.size(); i++){
			Node node = (Node) nodes.get(i);
			
			String clazz = XMLUtil.getAttributeValue(node, "class", "<class>");
			GraphNode current = getOrCreateNode(clazz);
			
			// configure the node
			styleNode(current, clazz);
		
			// walk reference-descriptor
			List references = XMLUtil.getChildNodesWithName(node, "reference-descriptor");
			processReferenceDescList(current, references);

			// extends
			List extend = XMLUtil.getChildNodesWithName(node, "extent-class");
			processExtendDescList(current, extend);
			
			
			// walk collection-descriptor
			List collections = XMLUtil.getChildNodesWithName(node, "collection-descriptor");
			processCollectionDescList(current, collections);
			
			// walk attributes
			List fields = XMLUtil.getChildNodesWithName(node, "field-descriptor");
			String fieldLabels = getLabelForFields(fields);
			if (options.detailed){
				current.getInfo().setCaption(fieldLabels);
			}
		}
	}
	
	private String getLabelForFields(List nodes){
		sbLabel.setLength(0);		 
		for (int i=0; i < nodes.size(); i++){
			Node node = (Node) nodes.get(i);
			
			String name = XMLUtil.getAttributeValue(node, "name", "<name>");
			String type = XMLUtil.getAttributeValue(node, "jdbc-type", "<jdbc-type>");			

			sbLabel.append(name + ": " + type);
			
			sbLabel.append("\n");
		}
		
		return sbLabel.toString(); 
	}
	
	private void processReferenceDescList(GraphNode parent, List nodes){
		for (int i=0 ;i < nodes.size(); i++){
			Node node = (Node) nodes.get(i);
			
			String class_ref = XMLUtil.getAttributeValue(node, "class-ref", "<ref>");
			GraphNode current = getOrCreateNode(class_ref);
			
			// configure depedencies 
			GraphEdge edge = graph.addEdge(parent, current);			
			   
			// realtion name
			String name = XMLUtil.getAttributeValue(node, "name", "<name>");
			edge.getInfo().setCaption(name);
			edge.getInfo().setLineColor(colors[COLOR_LINE_REFERENCE]);
			
			// ownership vs. association
			String auto_delete = XMLUtil.getAttributeValue(node, "auto-delete", "none");
			if (auto_delete.equals("object")){
				edge.getInfo().setArrowTailDiamond(); 
			} else {
				edge.getInfo().setModeDashed();
			}
		}
	}	
	
	private void processExtendDescList(GraphNode parent, List nodes){
		for (int i=0 ;i < nodes.size(); i++){
			Node node = (Node) nodes.get(i);
			
			String class_ref = XMLUtil.getAttributeValue(node, "class-ref", "<ref>");
			GraphNode current = getOrCreateNode(class_ref);
			
			// configure depedencies 
			GraphEdge edge = graph.addEdge(current, parent); 			
			
			// realtion name
			edge.getInfo().setLineColor(colors[COLOR_LINE_REFERENCE]);
			edge.getInfo().setArrowHeadOnormal();
			
			// attach to group
			GraphGroup group = (GraphGroup) nodeToGroup.get(parent);
			if (group == null){
				group = graph.addGroup();				 
				nodeToGroup.put(parent, group);
			} 
			current.setMemberOf(group);			
		}
	}	
	
	private void processCollectionDescList(GraphNode parent, List nodes){
		for (int i=0 ;i < nodes.size(); i++){
			Node node = (Node) nodes.get(i);
			
			String class_ref = XMLUtil.getAttributeValue(node, "element-class-ref", "<element-class-ref>");
			GraphNode current = getOrCreateNode(class_ref);
					
			// configure depedencies 
			GraphEdge edge = graph.addEdge(parent, current);
			edge.getInfo().setLineColor(colors[COLOR_LINE_COLLECTION]);
			
			// realtion name
			String name = XMLUtil.getAttributeValue(node, "name", "<name>");
			edge.getInfo().setCaption(name);
			edge.getInfo().setHeadCaption("0..n");
			
			// ownership vs. association
			String auto_delete = XMLUtil.getAttributeValue(node, "auto-delete", "none");
			if (auto_delete.equals("object")){
				edge.getInfo().setArrowTailDiamond(); 
			} else {
				edge.getInfo().setModeDashed();
			}
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
	
	private String formatNodeName(String name){		
		if (!options.qualifiedNames){
			name = name.trim();
			return StringUtil.stripPackageName(name);
		}
		return name;
	}	
	
	private void styleNode(GraphNode node, String name){		
		node.getInfo().setFillColor(colors[COLOR_NORMAL]);
		node.getInfo().setHeader(formatNodeName(name));
		node.getInfo().setModeSolid();
	}	
}
