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

public class HBMtoGRAPH {
	
	private static final String ANON_CLASS = "<class>";
	
	private Graph graph = GraphFactory.newGraph();
	private Map nameToNode = new HashMap();
	private Map nodeToGroup = new HashMap();
	private Map keyToClass = new HashMap();
	private TaskOptions options;
	
	private String [] colors = new String [] {
		"yellow",	// node 
		"black", 	// reference
		"black"		// collections
	};
	
	
	HBMtoGRAPH(){ 
	}
	
	public static IGraphModel load(TaskOptions options) throws IOException, SAXException, ParserConfigurationException {
		return new HBMtoGRAPH().innerLoad(options);
	}
	
	private IGraphModel innerLoad(TaskOptions options) throws IOException, SAXException, ParserConfigurationException {
		this.options = options;
		
		options.applyTo(graph);
		
		StringUtil.splitAndFill(options.colors, ",", this.colors);
		
		// parse all documents
		Document [] docs = new Document[options.inFileSet.length];  
		for (int i=0; i < options.inFileSet.length; i++){		
			docs[i] = XMLUtil.parse(options.expandEntityRef, false, options.inFileSet[i]);
			XMLUtil.assertRootElement(docs[i], "hibernate-mapping");				
		}
		
		// parse document, add all targets and all target dependencies
		for (int i=0; i < docs.length; i++){
			processClassDescList(XMLUtil.getChildNodesWithName(docs[i].getDocumentElement(), "class"));
		}

		
		// we have to do two pass here; we nee to have all keys already listed before
		// traversal
		graph.clear();
		nameToNode.clear();		
		options.applyTo(graph);
		
		for (int i=0; i < docs.length; i++){
			processClassDescList(XMLUtil.getChildNodesWithName(docs[i].getDocumentElement(), "class"));
		}
		
		// remove excluded nodes
		StringUtil.filterNodesByName(nameToNode, options.includes, options.excludes, graph);
		
		return graph;
	}
	
	private String resolveClazz(String clazz, String name){
		if (clazz == null){
			String impliedClass = (String) keyToClass.get(name);
			if (impliedClass != null){
				return impliedClass;
			} else {
				return ANON_CLASS;		
			}
		}
		return clazz;
	}
		
	class Attribute {
		private String name;
		private String type;
		private String column;
	}
	
	private String renderAttributes(Attribute [] attrs){
		String label = "";
		for (int j=0; j < attrs.length; j++){
			Attribute attr = attrs[j];			
			if (attr.name == null){
				if (attr.column != null){
					attr.name = attr.column;
				} else {
					attr.name = "<name>";
				}
			}
			if (attr.type == null){
				attr.type = ": <type>";
			} else {
				attr.type = ": " + attr.type;
			}			
			label = label + "\n" + attr.name + attr.type;
		}		
		
		return label;
	}

	private Attribute getAttribute(Node node){
		Attribute attr = new Attribute();
		attr.name = XMLUtil.getAttributeValue(node, "name", null);
		attr.column = XMLUtil.getAttributeValue(node, "column", null);
		attr.type = XMLUtil.getAttributeValue(node, "type", null);
		
		return attr;			
	}
	
	private void addAttribute(Node node, List list){
		list.add(getAttribute(node));			
	}
	
	private void walkAttributeTree(Node node, GraphNode gnode, List list){
		List attrs = XMLUtil.getChildNodesWithName(node, "id");		
		attrs.addAll(XMLUtil.getChildNodesWithName(node, "collection-id"));
		attrs.addAll(XMLUtil.getChildNodesWithName(node, "property"));				
		attrs.addAll(XMLUtil.getChildNodesWithName(node, "index"));
		attrs.addAll(XMLUtil.getChildNodesWithName(node, "list-index"));
		attrs.addAll(XMLUtil.getChildNodesWithName(node, "map-key"));
		attrs.addAll(XMLUtil.getChildNodesWithName(node, "any"));
		for (int i=0; i < attrs.size() ; i++){
			addAttribute((Node) attrs.get(i), list);
		}		
				
		//
		// We do not want to show "key" attributes, it is implied in the relations;
		// it makes sence for the "bag" however; we need "key" attributes to 
		// resolve the other end of one-to-one and many-to-one relation
		//
		List keys = XMLUtil.getChildNodesWithName(node, "key");
		for (int i=0; i < keys.size() ; i++){
			Node key = (Node) keys.get(i);
			
			String column = XMLUtil.getAttributeValue(key, "column", null);
			if (column != null){
				keyToClass.put(column, gnode.getInfo().getHeader());
			}		
		}		
	}
	
	private List processClassDescList(List nodes){
		List all = new ArrayList();
		
		for (int i=0 ;i < nodes.size(); i++){
			Node node = (Node) nodes.get(i);
			List attrs = new ArrayList();
			
			String clazz = XMLUtil.getAttributeValue(node, "name", "<name>");
			GraphNode current = getOrCreateNode(clazz);

			all.add(current);
			
			// process references
			List many2one_one2one = XMLUtil.getChildNodesWithName(node, "many-to-one");
			many2one_one2one.addAll(XMLUtil.getChildNodesWithName(node, "one-to-one"));
			processManyToOneOrOneToOneList(current, many2one_one2one);
			
			// process components
			List components = XMLUtil.getChildNodesWithName(node, "component");
			processComponentList(current, components);
			
			// process subclasses
			List subclasses = XMLUtil.getChildNodesWithName(node, "subclass");
			subclasses.addAll(XMLUtil.getChildNodesWithName(node, "union-subclass"));
			subclasses.addAll(XMLUtil.getChildNodesWithName(node, "joined-subclass"));
			processSubclassesList(current, subclasses);			
									
			// process collections
			List collections = XMLUtil.getChildNodesWithName(node, "map");
			collections.addAll(XMLUtil.getChildNodesWithName(node, "set"));
			collections.addAll(XMLUtil.getChildNodesWithName(node, "list"));
			collections.addAll(XMLUtil.getChildNodesWithName(node, "bag"));
			collections.addAll(XMLUtil.getChildNodesWithName(node, "idbag"));			
			collections.addAll(XMLUtil.getChildNodesWithName(node, "array"));
			collections.addAll(XMLUtil.getChildNodesWithName(node, "primitive-array"));			
			processCollectionsList(current, collections);			
			processElementList(current, collections);
			
			// add attributes			 		
			if (options.detailed){				
				walkAttributeTree(node, current, attrs);
				String label = renderAttributes((Attribute []) attrs.toArray(new Attribute []{}));
				current.getInfo().setCaption(label);
			}
		}
		
		return all;
	}	
	
	private void processManyToOneOrOneToOneList(GraphNode parent, List nodes){
		for (int i=0 ;i < nodes.size(); i++){
			Node node = (Node) nodes.get(i);
			
			String name = XMLUtil.getAttributeValue(node, "name", "<name>");
			String class_ref = XMLUtil.getAttributeValue(node, "class", null);			
			class_ref = resolveClazz(class_ref, name);
						
			// infer class from other end of the relation
			if (class_ref == null){
				class_ref= "<class-ref>";
			}	
			
			// capture this node as having class unresolved
			GraphNode current = getOrCreateNode(class_ref);
			
			// configure depedencies 
			GraphEdge edge = graph.addEdge(parent, current);			
			
			// relation			
			edge.getInfo().setCaption(name);
			edge.getInfo().setLineColor(colors[1]);
			
			if ("many-to-one".equals(node.getNodeName())){
				edge.getInfo().setTailCaption("0..n");
				edge.getInfo().setHeadCaption("");	
			} else {
				edge.getInfo().setHeadCaption("");				
			}
									
			// ownership vs. association
			String cascade = XMLUtil.getAttributeValue(node, "cascade", "none");
			if (cascade.equals("all") || cascade.equals("delete")){
				edge.getInfo().setArrowTailDiamond(); 
			} else {
				edge.getInfo().setModeDashed();
			}
		}
	} 
	
	private void processComponentList(GraphNode parent, List nodes){
		List newnodes = processClassDescList(nodes);
		for (int i=0 ;i < newnodes.size(); i++){
			GraphNode current = (GraphNode) newnodes.get(i);
			Node node = (Node) nodes.get(i);
						
			// relation			
			String name = XMLUtil.getAttributeValue(node, "name", "<name>");
			String clazz = XMLUtil.getAttributeValue(node, "class", "<" + name + ">");
			
			GraphEdge edge = graph.addEdge(parent, current);
			edge.getInfo().setCaption(name);
			edge.getInfo().setLineColor(colors[1]);
			edge.getInfo().setArrowTailDiamond();
			
			// rename child node with its class
			current.getInfo().setHeader(formatNodeName(clazz));
		}		
	}
	
	private void processSubclassesList(GraphNode parent, List nodes){
		List newnodes = processClassDescList(nodes);
		for (int i=0 ;i < newnodes.size(); i++){
			GraphNode current = (GraphNode) newnodes.get(i);
			Node node = (Node) nodes.get(i);
						  
			// relation			
			String name = XMLUtil.getAttributeValue(node, "name", "<name>");
			 
			GraphEdge edge = graph.addEdge(current, parent);
			edge.getInfo().setLineColor(colors[1]);
			edge.getInfo().setArrowHeadOnormal();
			
			// rename child node with its class
			current.getInfo().setHeader(formatNodeName(name));
			
			// attach to group
			GraphGroup group = (GraphGroup) nodeToGroup.get(parent);
			if (group == null){
				group = graph.addGroup();				 
				nodeToGroup.put(parent, group);
			}
			current.setMemberOf(group);
		}		
	}	
	
	private void processElementList(GraphNode parent, List nodes){
		for (int i=0 ;i < nodes.size(); i++){
			Node node = (Node) nodes.get(i);
			
			Node element = XMLUtil.getChildNodeWithName(node, "element");		
			if (element == null){
				continue;
			}
			 
			// new entity
			String name = XMLUtil.getAttributeValue(node, "name", "<name>");
			GraphNode current = getOrCreateNode("<<" + node.getNodeName() + ">>\n" + name);
						
			// render element as attributes
			List attrs = new ArrayList();
			walkAttributeTree(node, current, attrs);
			attrs.add(getAttribute(element));
			
			String label = renderAttributes( (Attribute[]) attrs.toArray( new Attribute[]{}));
			current.getInfo().setCaption(label);
			
			// relation
			GraphEdge edge = graph.addEdge(parent, current);
			edge.getInfo().setLineColor(colors[1]);
			edge.getInfo().setArrowTailDiamond();
			edge.getInfo().setArrowHeadNormal();
		}		
	}
	
	private void processCollectionsList(GraphNode parent, List nodes){
		for (int i=0 ;i < nodes.size(); i++){
			Node node = (Node) nodes.get(i);
		
			// many-to-many
			{
				List many2many = XMLUtil.getChildNodesWithName(node, "many-to-many");
				for (int j=0 ;j < many2many.size(); j++){
					Node many2manyNode = (Node) many2many.get(j);
				
					String name = XMLUtil.getAttributeValue(node, "name", "<name>");
					String clazz = XMLUtil.getAttributeValue(many2manyNode, "class", ANON_CLASS);
					GraphNode current = getOrCreateNode(clazz);
					
					// relation			
					GraphEdge edge = graph.addEdge(parent, current);
					edge.getInfo().setCaption(name);
					edge.getInfo().setLineColor(colors[1]);
					edge.getInfo().setHeadCaption("0..n");
					edge.getInfo().setTailCaption("0..n");
					edge.getInfo().setArrowTailDiamond();
					
					List attrs = new ArrayList();
					walkAttributeTree(node, parent, attrs);
				}
			}
			
			// one-to-many
			{
				List one2many = XMLUtil.getChildNodesWithName(node, "one-to-many");
				for (int j=0 ;j < one2many.size(); j++){
					Node one2manyNode = (Node) one2many.get(j);
				
					String name = XMLUtil.getAttributeValue(node, "name", "<name>");
					String clazz = XMLUtil.getAttributeValue(one2manyNode, "class", ANON_CLASS);
					GraphNode current = getOrCreateNode(clazz);
					
					// relation			
					GraphEdge edge = graph.addEdge(parent, current);
					edge.getInfo().setCaption(name);
					edge.getInfo().setLineColor(colors[1]);
					edge.getInfo().setHeadCaption("0..n");
					edge.getInfo().setArrowTailDiamond();
					
					List attrs = new ArrayList();
					walkAttributeTree(node, parent, attrs);					
				}
			}
			
			// composite elements
			{
				String name = XMLUtil.getAttributeValue(node, "name", "<name>");
				List element = XMLUtil.getChildNodesWithName(node, "composite-element");
				element.addAll(XMLUtil.getChildNodesWithName(node, "nested-composite-element"));
								
				class Inline {
					private void processCompositeList(String parentName, GraphNode parent, List nodes, String roleName){
						List newnodes = processClassDescList(nodes);
						for (int i=0 ;i < newnodes.size(); i++){
							GraphNode current = (GraphNode) newnodes.get(i);
							Node node = (Node) nodes.get(i);
										
							// relation			
							String clazz = XMLUtil.getAttributeValue(node, "class", ANON_CLASS);
							
							GraphEdge edge = graph.addEdge(parent, current);
							edge.getInfo().setCaption(roleName);
							edge.getInfo().setLineColor(colors[1]);
							edge.getInfo().setHeadCaption("0..n");
							edge.getInfo().setArrowTailDiamond();
							
							// rename child node with its class
							current.getInfo().setHeader(formatNodeName(clazz));							
						}		
					}			
				}
				
				new Inline().processCompositeList(name, parent, element, name);  
			}
						
		}		
	}		
	
	private String formatNodeName(String name){		
		if (!options.qualifiedNames){
			name = name.trim();
			return StringUtil.stripPackageName(name);
		}
		return name;
	}
	
	private GraphNode getOrCreateNode(String name){
		name = name.trim(); 				
		GraphNode node = (GraphNode) nameToNode.get(name);
		if (node == null){
			node = graph.addNode();
			nameToNode.put(name, node);
			
			styleNode(node, name);
		} 		
		return node;
	}
	
	private void styleNode(GraphNode node, String name){
		if (!ANON_CLASS.equals(name)){
			node.getInfo().setFillColor(colors[0]);
		}
		node.getInfo().setHeader(formatNodeName(name));
		node.getInfo().setModeSolid();
	}
	
}
