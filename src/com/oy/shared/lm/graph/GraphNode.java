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

import com.oy.shared.lm.graph.bo.GraphNodeBean;
import com.oy.shared.lm.graph.bo.NodeInfo;

public class GraphNode {

	GraphNodeBean node;
	GraphGroup group;

	GraphNode(GraphNodeBean node){
		this.node = node;
		
		node.getInfo().copyFieldsFrom(node.getGraph().getInfo());
		
		node.getInfo().setCaption("");		
		node.getInfo().setShapeRecord();
		node.getInfo().setModeSolid();
		node.getInfo().setFilled(true);
		node.getInfo().setRounded(false);
	}
	 
	public int getId(){
		return node.getNodeId();
	}
	
	public void setMemberOf(GraphGroup group){
		this.group = group;
		if (group != null){
			node.setMemberOf(group.group);
		} else {
			node.setMemberOf(null);
		}		
	}
	
	public GraphGroup getGroup(){
		return group;
	}
	
	public NodeInfo getInfo(){
		return node.getInfo();
	}
	
}
