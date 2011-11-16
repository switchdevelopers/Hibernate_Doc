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

package com.oy.shared.lm.graph;

import java.util.ArrayList;
import java.util.List;

import com.oy.shared.lm.graph.bo.EdgeInfo;
import com.oy.shared.lm.graph.bo.GraphBean;
import com.oy.shared.lm.graph.bo.GraphEdgeBean;
import com.oy.shared.lm.graph.bo.GraphGroupBean;
import com.oy.shared.lm.graph.bo.GraphInfo;
import com.oy.shared.lm.graph.bo.GraphNodeBean;
import com.oy.shared.lm.graph.bo.GroupInfo;
import com.oy.shared.lm.graph.bo.NodeInfo;

public class Graph implements IGraphModel {

	private GraphBean graph;
	
	Graph(){ 	
		init();
	}
	
	private void init(){
		graph = new GraphBean();
		graph.setInfo(new GraphInfo());
		graph.setEdges(new ArrayList());
		graph.setNodes(new ArrayList());
		graph.setGroups(new ArrayList());
		
		graph.getInfo().setFontColor("black");
		graph.getInfo().setFontName("Helvetica");
		graph.getInfo().setFontSize(10);
		 
		graph.getInfo().setFillColor("white");	
		graph.getInfo().setLineColor("black");
	}
	
	public GraphInfo getInfo(){
		return graph.getInfo();
	}
	
	public void clear(){
		init();
	}
	
	public GraphNode addNode(){
		GraphNodeBean node = new GraphNodeBean();
		node.setGraph(graph);
		node.setNodeId(graph.getNodes().size());
		node.setInfo(new NodeInfo());
		
		GraphNode n = new GraphNode(node);
		graph.getNodes().add(n);
		return n;
	}
	
	public void removeNode(GraphNode node){
		int idx = ((List) graph.getNodes()).indexOf(node);
		
		if (idx == -1){
			throw new IllegalArgumentException("Node is not in the graph.");
		}
		
		((List) graph.getNodes()).remove(idx);
		
		// remove related edges
		GraphEdge [] edges = getEdges();
		for (int i=0; i < edges.length; i++){			
			if (edges[i].getFromNode() == node || edges[i].getToNode() == node){
				removeEdge(edges[i]);
			} 
		}
		
	}
	
	public GraphGroup addGroup(){
		GraphGroupBean node = new GraphGroupBean();
		node.setGraph(graph);
		node.setGroupId(graph.getGroups().size());
		node.setInfo(new GroupInfo());
		
		GraphGroup g = new GraphGroup(node);
		graph.getGroups().add(g);
		return g;
	} 
	
	public void removeGroup(GraphGroup group){
		int idx = ((List) graph.getGroups()).indexOf(group);
		
		if (idx == -1){
			throw new IllegalArgumentException("Group is not in the graph.");
		}
		
		((List) graph.getGroups()).remove(idx);
		
		// reset memberOf for linked nodes
		GraphNode [] nodes = getNodes();
		for (int i=0; i < nodes.length; i++){			 
			if (nodes[i].node.getMemberOf() == group.group){
				nodes[i].node.setMemberOf(null);
			}
		}
		
	}	
	
	public GraphEdge addEdge(GraphNode from, GraphNode to){
		if (from == null || to == null){
			throw new IllegalArgumentException("Please provide valid node instances.");
		}
		
		if (!graph.getNodes().contains(from) || !graph.getNodes().contains(to)){
			throw new IllegalArgumentException("Nodes are not contained by the graph.");
		}
		
		GraphEdgeBean edge = new GraphEdgeBean();
		edge.setGraph(graph);
		edge.setEdgeId(graph.getEdges().size());
		edge.setInfo(new EdgeInfo());
		
		GraphEdge e = new GraphEdge(edge, from, to);
		graph.getEdges().add(e);
		return e;
	}
	
	public void removeEdge(GraphEdge edge){
		int idx = ((List) graph.getEdges()).indexOf(edge);
		
		if (idx == -1){
			throw new IllegalArgumentException("Edge is not in the graph.");
		}
		
		((List) graph.getEdges()).remove(idx);
	}		
	
	public GraphNode [] getNodes(){
		return (GraphNode []) graph.getNodes().toArray(new GraphNode [] {});
	}
	
	public GraphEdge [] getEdges(){
		return (GraphEdge []) graph.getEdges().toArray(new GraphEdge [] {});
	}
	
	public GraphGroup [] getGroups(){
		return (GraphGroup []) graph.getGroups().toArray(new GraphGroup [] {});
	}

}
