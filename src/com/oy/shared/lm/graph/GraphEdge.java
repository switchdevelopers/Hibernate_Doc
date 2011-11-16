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

import com.oy.shared.lm.graph.bo.EdgeInfo;
import com.oy.shared.lm.graph.bo.GraphEdgeBean;

public class GraphEdge {

	GraphEdgeBean edge;
	GraphNode from;
	GraphNode to;
	
	GraphEdge(GraphEdgeBean edge, GraphNode from, GraphNode to){
		this.edge = edge;		
		this.from = from;
		this.to = to;
		
		edge.setFromNode(from.node);
		edge.setToNode(from.node);
		
		edge.getInfo().copyFieldsFrom(edge.getGraph().getInfo());
		
		edge.getInfo().setCaption("");
		
		edge.getInfo().setFontSize(edge.getGraph().getInfo().getFontSize() - 2);
		
		edge.getInfo().setModeSolid();
		
		edge.getInfo().setLabelAngle(-25);
		edge.getInfo().setLabelDistance(1.5);
		
		edge.getInfo().setArrowSize(1);		
		edge.getInfo().setArrowHeadNormal();
		edge.getInfo().setArrowTailNone();
	}
	
	public int getId(){
		return edge.getEdgeId();
	}
	
	public GraphNode getFromNode(){
		return from;
	}
	
	public GraphNode getToNode(){
		return to;
	}
		
	public EdgeInfo getInfo(){
		return edge.getInfo();
	}
	
}

