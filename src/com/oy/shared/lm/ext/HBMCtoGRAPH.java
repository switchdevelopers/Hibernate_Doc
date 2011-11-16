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

package com.oy.shared.lm.ext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.OneToMany;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.Value;

import com.oy.shared.lm.ant.TaskOptions;
import com.oy.shared.lm.graph.Graph;
import com.oy.shared.lm.graph.GraphEdge;
import com.oy.shared.lm.graph.GraphFactory;
import com.oy.shared.lm.graph.GraphNode;
import com.oy.shared.lm.graph.IGraphModel;
import com.oy.shared.lm.in.StringUtil;

public class HBMCtoGRAPH {

	class Attribute {
		private String name;
		private String type;
		private String column;
	}
	
	private static final String ANON_CLASS = "<class>";
	
	private Graph graph = GraphFactory.newGraph();
	private Map nameToNode = new HashMap();
	private TaskOptions options;
	
	private String [] colors = new String [] {
		"yellow",	// node 
		"black", 	// reference
		"black"		// collections
	};
		
	HBMCtoGRAPH(){}

	public static IGraphModel load(TaskOptions options) throws IOException {
		Configuration conf = new Configuration();
		
		// add all documents to Configuration
		for (int i=0; i < options.inFileSet.length; i++){
			conf.addFile(options.inFileSet[i]);
		}
		
		return new HBMCtoGRAPH().innerLoad(options, conf);
	}	
	
	public static IGraphModel load(TaskOptions options, Configuration conf) throws IOException {
		return new HBMCtoGRAPH().innerLoad(options, conf);
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
	
	private List iterToList(Iterator iter){
		List list = new ArrayList();
		while(iter.hasNext()){
			list.add(iter.next());
		}		
		return list;
	}
	
	private IGraphModel innerLoad(TaskOptions options, Configuration conf) throws IOException {
		this.options = options;
		
		options.applyTo(graph);
		
		StringUtil.splitAndFill(options.colors, ",", this.colors);
		
		// parse document class mappings
		processClasses(
			(PersistentClass []) iterToList(
				conf.getClassMappings()
			).toArray(new PersistentClass[] {})
		);
					
		// remove excluded nodes
		StringUtil.filterNodesByName(nameToNode, options.includes, options.excludes, graph);
		
		return graph;
	}
	
	private void processClasses(PersistentClass [] nodes){
		for (int i=0; i < nodes.length; i++){
			
			PersistentClass node = nodes[i];
			
			String clazz = node.getClassName();
			GraphNode current = getOrCreateNode(clazz);

			// process attributes
			processProperties(node.getPropertyIterator(), current);
			
			// process subclasses
			processSubclasses(
				current, 
				(PersistentClass []) iterToList(
					node.getDirectSubclasses()
				).toArray(new PersistentClass [] {})
			);						
		}
	}
	
	private void processSubclasses(GraphNode parent, PersistentClass [] nodes){
		for (int i=0 ;i < nodes.length; i++){
			PersistentClass node = nodes[i];
			
			String clazz = node.getClassName();
			GraphNode current = getOrCreateNode(clazz);
			
			// relation
			GraphEdge edge = graph.addEdge(current, parent);
			edge.getInfo().setLineColor(colors[1]);
			edge.getInfo().setArrowHeadOnormal();
			
			// rename child node with its class
			current.getInfo().setHeader(node.getClassName());
		}		
	}

	private void processProperties(Iterator iter, GraphNode current){			
		List attrs = new ArrayList();
		while(iter.hasNext()){
			Property prop  = (Property) iter.next();
			Value val = prop.getValue();
			
			// one-to-one/many-to-one
			if (val instanceof ToOne || val instanceof Component){
				addToOneProp(prop, current);
				continue;
			}
			
			// collections
			if (val instanceof Collection){
				addCollectionProp(prop, current);
				continue;
			}
			
			// simple value
			if (options.detailed){
				if (val instanceof SimpleValue){
					Attribute attr = addSimpleProp((SimpleValue) prop.getValue(), prop.getName());
					attrs.add(attr);
					continue;
				}
			}

		}				
		
		String label = renderAttributes((Attribute []) attrs.toArray(new Attribute []{}));
		current.getInfo().setCaption(label);	
	}	
	
	private Attribute addSimpleProp(SimpleValue val, String name){
		if (!val.isSimpleValue()){
			throw new RuntimeException("Expected SimpleValue.");
		}
		
		Attribute attr = new Attribute(); 
		attr.name = name;	
		if (attr.name == null){
			Iterator iter = val.getColumnIterator();
			while(iter.hasNext()){
				Column col = (Column) iter.next(); 
				attr.name = col.getName();
				break;
			}			 
		}
		if(val instanceof Component) {
			attr.type = ((Component)val).getComponentClassName();
		} else {					
			attr.type = val.getType().getName();
		}
		
		return attr;
	}
		
	private void addToOneProp(Property prop, GraphNode current){
		Value val = prop.getValue();
			
		String clazz = null;
		boolean owned = false;
		
		if(val instanceof Component) {
			clazz = ((Component)prop.getValue()).getComponentClassName();
			owned = true;
		} else {		
			if (val instanceof ToOne){
				clazz = prop.getType().getName();
				owned = ((ToOne) val).isCascadeDeleteEnabled();
			} else {
				throw new RuntimeException("Expected ToOne or Component.");
			}
		}
		
		GraphNode child = getOrCreateNode(clazz);
											
		// relation 
		GraphEdge edge = graph.addEdge(current, child);			
		edge.getInfo().setCaption(prop.getName());
		edge.getInfo().setLineColor(colors[1]);
		
		// multiplicity
		if (val instanceof ManyToOne){
			edge.getInfo().setTailCaption("0..n");
			edge.getInfo().setHeadCaption("");	
		} else {
			edge.getInfo().setHeadCaption("");				
		}
		
		// ownership vs. association
		if (owned){
			edge.getInfo().setArrowTailDiamond(); 
		} else { 
			edge.getInfo().setModeDashed();
		}				
		
		// component attributes
		if (val instanceof Component){					
			processProperties(((Component)prop.getValue()).getPropertyIterator(), child);
		}			
	}
		
	private void addCollectionProp(Property prop, GraphNode current){		
		Value val = prop.getValue();
		if (!(val instanceof Collection)){
			throw new RuntimeException("Expected Collection.");
		}
		
		Collection col = (Collection) prop.getValue();
		Value elem = col.getElement();
		
		if (elem instanceof OneToMany){		
			String clazz = col.getElement().getType().getName();    
			GraphNode child = getOrCreateNode(clazz);
			
			// relation			
			GraphEdge edge = graph.addEdge(current, child);
			edge.getInfo().setCaption(prop.getName());
			edge.getInfo().setLineColor(colors[2]);
			edge.getInfo().setHeadCaption("0..n"); 
			edge.getInfo().setArrowTailDiamond();
		
			return;
		} 
			
		if (elem instanceof ManyToOne){
			String clazz = col.getElement().getType().getName();
			GraphNode child = getOrCreateNode(clazz);
			
			// relation			
			GraphEdge edge = graph.addEdge(current, child);
			edge.getInfo().setCaption(prop.getName());
			edge.getInfo().setLineColor(colors[2]);
			edge.getInfo().setArrowTailDiamond();
			edge.getInfo().setHeadCaption("0..n");
			edge.getInfo().setTailCaption("0..n");
			return;
		}
		
		if (elem instanceof Component){
			String clazz = prop.getName();
			GraphNode child = getOrCreateNode("<<" + col.getClass().getName() + ">>\n" + clazz);
			
			// relation			
			GraphEdge edge = graph.addEdge(current, child);
			edge.getInfo().setLineColor(colors[1]); 
			edge.getInfo().setArrowTailDiamond();

			processProperties(((Component)elem).getPropertyIterator(), child);
			return;
		}
		
		if (elem instanceof SimpleValue){
			String clazz = prop.getName();
			GraphNode child = getOrCreateNode("<<" + col.getClass().getName() + ">>\n" + clazz);
			
			// relation			
			GraphEdge edge = graph.addEdge(current, child);
			edge.getInfo().setLineColor(colors[1]);
			edge.getInfo().setArrowTailDiamond();
			
			List attrs = new ArrayList();
			Attribute attr = addSimpleProp((SimpleValue) elem, null);
			attrs.add(attr);			
			String label = renderAttributes((Attribute []) attrs.toArray(new Attribute []{}));
			child.getInfo().setCaption(label);	
 
			return;
		}		
		
		throw new RuntimeException("Unexpected collection element.");
	}											
	
	private GraphNode getOrCreateNode(String name){
		name = name.trim(); 
		
		GraphNode node = (GraphNode) nameToNode.get(name);
		if (node == null){
			node = graph.addNode();
			 
			if (!ANON_CLASS.equals(name)){
				node.getInfo().setFillColor(colors[0]);
			}
			node.getInfo().setHeader(name);
			node.getInfo().setModeSolid();

			nameToNode.put(name, node);
		} 
		
		return node;	
	}	
	
}