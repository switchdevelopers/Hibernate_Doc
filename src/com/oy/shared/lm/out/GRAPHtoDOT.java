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

package com.oy.shared.lm.out;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import com.oy.shared.lm.graph.bo.BaseInfo;
import com.oy.shared.lm.graph.bo.EdgeInfo;
import com.oy.shared.lm.graph.bo.NodeInfo;
import com.oy.shared.lm.graph.GraphEdge;
import com.oy.shared.lm.graph.GraphNode;
import com.oy.shared.lm.graph.IGraphModel;

public class GRAPHtoDOT {	
	
	private StringBuffer sbEscape = new StringBuffer();
	private StringBuffer sbLabel = new StringBuffer();
	private PrintStream out;
	private IGraphModel igraph;
	
	GRAPHtoDOT(){}
		
	public static void transform(IGraphModel igraph, OutputStream out) throws IOException {
		new GRAPHtoDOT().innerTransform(igraph, out);
	}
	
	public void innerTransform(IGraphModel igraph, OutputStream out) throws IOException {
		this.igraph = igraph;
		
		this.out = new PrintStream(out);
		try { 
			render();		
		} finally{
			this.out = null;
		}
	}
		
	private String getNodeId(GraphNode node){
		return "NODE" + node.getId();
	}

	private String getEdgeId(GraphEdge edge){
		return getNodeId(edge.getFromNode()) + " -> " + getNodeId(edge.getToNode());
	}	
	
	private void render(){
		GraphNode [] nodes = igraph.getNodes();
		GraphEdge [] edges = igraph.getEdges();
		
		renderHeader();
		for (int i=0; i < nodes.length; i++){
			render(nodes[i]);
		}
		for (int i=0; i < edges.length; i++){
			render(edges[i]);
		}		
		renderFooter();		
	}
	
	private void renderHeader(){
		println("digraph LinguineMap {");
		println("  compound=true;");
						
		// color
		print("  bgcolor=\"" + igraph.getInfo().getFillColor() + "\";");
		
		// fonts
		renderFont(igraph.getInfo(), "", "", ";");
	
		// rank
		print(" ranksep=\"equally\";");		
				
		// orientation
		if (igraph.getInfo().getRotated()){
			print("  rankdir=\"LR\";");
		}
		
		// label
		String label = igraph.getInfo().getCaption(); 		
		if (label == null || label.equals("")){
			label = "";
		} else {
			label = label.toUpperCase() + "\n"; 
		} 
		
		label = label + "http://www.softwaresecretweapons.com";		
		print(" label=\"" + escText(label) + "\";");				
		
		// URL
		String url = "http://www.softwaresecretweapons.com";
		print(" URL=\"" + escText(url) + "\";");
		
		// atributes 
		renderAttributes(igraph.getInfo().getAttributes(), "", ";");
		
		// default node/edge styles 		
		println("");
		
		print("  edge ["); 				
			print("label=\"\"");
			renderFont(igraph.getInfo(), "", ",", "");
			renderFont(igraph.getInfo(), "label", ",", "");
			renderAttributes(igraph.getInfo().getEdgeAttributes(), ",", "");			
		println("];");
			  	
		// default node
		print("  node [");		
			print("label=\"\"");
			renderFont(igraph.getInfo(), "", ",", "");
			print(", shape=record");
			renderAttributes(igraph.getInfo().getNodeAttributes(), ",", "");
		println("];");		
	}
	 
	private void renderFooter(){
		println("}");
	}
		
	private void render(GraphNode node){
		print("  ");
		if (node.getGroup() != null){
			println("  subgraph cluster" + node.getGroup().getId() + " {");
			println("    label=" + esc(node.getGroup().getInfo().getCaption()) + ";");
			if (node.getGroup().getInfo().isModeFilled()){
				println("    style=filled;");	
				println("    color=" + node.getGroup().getInfo().getFillColor() + ";");
			}
			if (node.getGroup().getInfo().isModeOutlined()){
				println("    color=" + node.getGroup().getInfo().getLineColor() + ";");
			}
		}				
		print(getNodeId(node));
		print(" [");
		render(node.getInfo());
		println("];");
		if (node.getGroup() != null){
			println("  }");
		}						
	}
	
	private void render(GraphEdge edge){
		print("  ");
		print(getEdgeId(edge));
		print(" [");
		render(edge.getInfo());
		println("];");		
	}
	
	private String renderNodeStyle(NodeInfo decoration){
		String style = "";
		
		switch(decoration.getMode()){
			case NodeInfo.ModeEnum.SOLID: {
				style = "solid";
				break;
			}
			case NodeInfo.ModeEnum.DASHED: {
				style = "dashed";
				break;
			}
			case NodeInfo.ModeEnum.DOTTED: {
				style = "dotted";
				break;
			}
			case NodeInfo.ModeEnum.BOLD: {
				style = "bold";
				break;
			}
			default :{
				throw new IllegalArgumentException("Bad style " + style);
			}
		}
		
		if (decoration.getFilled()){
			style += ",filled";
		}
		if (decoration.getRounded()){
			style += ",rounded";
		}		
				
		return esc(style);
	}
	
	private String renderEdgeStyle(EdgeInfo decoration){
		String style = "";
		
		switch(decoration.getMode()){
			case NodeInfo.ModeEnum.SOLID: {
				style = "solid";
				break;
			}
			case NodeInfo.ModeEnum.DASHED: {
				style = "dashed";
				break;
			}
			case NodeInfo.ModeEnum.DOTTED: {
				style = "dotted";
				break;
			}
			case NodeInfo.ModeEnum.BOLD: {
				style = "bold";
				break;
			}
			default :{
				throw new IllegalArgumentException("Bad style " + style);
			}
		}
		
		return esc(style);
	}
	
	private String renderArrowStyle(int arrowTail){
		String style = "";
		
		switch(arrowTail){
			case EdgeInfo.ArrowTailEnum.NORMAL: {
				style = "normal";
				break;
			}
			case EdgeInfo.ArrowTailEnum.ONORMAL: {
				style = "onormal";
				break;
			}			
			case EdgeInfo.ArrowTailEnum.DIAMOND: {
				style = "diamond";
				break;
			}
			case EdgeInfo.ArrowTailEnum.ODIAMOND: {
				style = "odiamond";
				break;
			}			
			case EdgeInfo.ArrowTailEnum.DOT: {
				style = "dot";
				break;
			}
			case EdgeInfo.ArrowTailEnum.ODOT: {
				style = "odot";
				break;
			}
			case EdgeInfo.ArrowTailEnum.NONE: {
				style = "NONE";
				break;
			}
			default :{
				throw new IllegalArgumentException("Bad style " + style);
			}
		}
		
		return esc(style);		
	}
	
	private String escText(String text){
		return escText(text, "\\l");
	}
	
	private String escText(String text, String newLineSeparator){		
		sbEscape.setLength(0);
		for (int i=0; i < text.length(); i++){
			char c = text.charAt(i);
			if (
					('a' <= c && c <= 'z')
					||
					('A' <= c && c <= 'Z')
					||
					('0' <= c && c <= '9')
					||
					(c == '\n')
			){
				sbEscape.append(c);
			} else {
				if (c == '\t'){
					// skip
				} else {
					sbEscape.append("\\" + c);
				}
			}
		}
		
		// escape \n
		String [] lines = sbEscape.toString().split("\n");
		sbEscape.setLength(0);
		for (int i=0; i < lines.length; i++){
			sbEscape.append(lines[i]);
			sbEscape.append(newLineSeparator);  
		}				
		
		return sbEscape.toString();
	}
	
	private String renderNodeShape(NodeInfo decoration){
		String shape = "";
		
		switch(decoration.getShape()){
			case NodeInfo.ShapeEnum.RECORD: {
				shape = "record";
				break;
			}
			case NodeInfo.ShapeEnum.BOX: {
				shape = "box";
				break;
			}
			case NodeInfo.ShapeEnum.ELLIPSE: {
				shape = "ellipse";
				break;
			}			
			case NodeInfo.ShapeEnum.CIRCLE: {
				shape = "circle";
				break;
			}
			case NodeInfo.ShapeEnum.DIAMOND: {
				shape = "diamond";
				break;
			}			
			case NodeInfo.ShapeEnum.TRIANGLE: {
				shape = "triangle";
				break;
			}			
			case NodeInfo.ShapeEnum.INVTRIANGLE: {
				shape = "invtriangle";
				break;
			}			
			case NodeInfo.ShapeEnum.HEXAGON: {
				shape = "hexagon";
				break;
			}			
			case NodeInfo.ShapeEnum.OCTAGON: {
				shape = "octagon";
				break;
			}			
			case NodeInfo.ShapeEnum.PARALLELOGRAM: {
				shape = "parallelogram";
				break;
			}						
			default :{
				throw new IllegalArgumentException("Bad shape " + shape);
			}
		}
		
		return esc(shape);
	}
	
	private String renderNodeLabel(NodeInfo decoration){
				
		// set up control symbols for orientation
		String pipe = "|";
		if (decoration.getShape() != NodeInfo.ShapeEnum.RECORD){
			pipe = "\\l";
		}
		
		String open = "{";
		String close = "}";		
		if (igraph.getInfo().getRotated()){
			open = "";
			close = "";			
		}
		
		String header = noesc(decoration.getHeader());
		String footer = noesc(decoration.getFooter());
		String desc = noesc(decoration.getCaption());

		sbLabel.setLength(0);
		sbLabel.append(open);

		// set header
		if (!header.equals("")){
			sbLabel.append(escText(header, "\\n") + pipe);
		}
				
		// set desc
		if (!desc.equals("")){
			sbLabel.append(escText(desc));
		}
		
		// set footer
		if (!footer.equals("")){ 
			sbLabel.append(pipe + escText(footer, "\\n"));
		}
						
		sbLabel.append(close);

		return esc(sbLabel.toString());
	}
	
	private String renderEdgeLabel(String label){		
		return esc(label);
	}	
		
	private void render(NodeInfo decoration){
		print("  label=" + renderNodeLabel(decoration));
		print(", shape=" + renderNodeShape(decoration));
		print(", style=" + renderNodeStyle(decoration));
		print(", color=" + esc(decoration.getLineColor()));
		print(", fillcolor=" + esc(decoration.getFillColor()));
		
		renderFont(decoration, "", ",", "");
		
		renderAttributes(decoration.getAttributes(), ",", "");		
	} 
	
	private void render(EdgeInfo decoration){
		print("  label=" + renderEdgeLabel(decoration.getCaption()));
		print(", style=" + renderEdgeStyle(decoration));
		print(", color=" + esc(decoration.getLineColor()));
		
		print(", headlabel=" + renderEdgeLabel(decoration.getHeadCaption()));
		print(", taillabel=" + renderEdgeLabel(decoration.getTailCaption()));
		
		print(", arrowsize=" + noesc(decoration.getArrowSize()));
		
		print(", arrowhead=" + renderArrowStyle(decoration.getArrowHead()));
		print(", arrowtail=" + renderArrowStyle(decoration.getArrowTail()));
		
		print(", labelangle=" + noesc(decoration.getLabelAngle()));
		print(", labeldistance=" + noesc(decoration.getLabelDistance()));

		renderFont(decoration, "", ",", "");
		renderFont(decoration, "label", ",", "");
		
		renderAttributes(decoration.getAttributes(), ",", "");
	}
	 
	private void renderFont(BaseInfo decoration, String baseName, String prefix, String postfix){
		print(prefix + " " + baseName + "fontcolor=\"" + noesc(decoration.getFontColor()) + "\"" + postfix);
		print(prefix + " " + baseName + "fontname=\"" + noesc(decoration.getFontName()) + "\"" + postfix);
		print(prefix + " " + baseName + "fontsize=\"" + noesc(decoration.getFontSize()) + "\"" + postfix);		
	}
	
	private void renderAttributes(String attributes, String prefix, String postfix){
		String attr = noesc(attributes);
		if (!attr.equals("")){
			print(prefix + " " + attr + postfix);
		}
	}
	
	private void print(String value){
		out.print(value);
	}
	
	private void println(String value){
		out.println(value);
	}
	  
	private String esc(String value){
		if (value == null){ 
			value = "";
		}
		return "\"" + noesc(value.trim()) + "\""; 
	}
	
	private String noesc(String value){
		if (value == null){
			value = "";
		}
		return value.trim().replaceAll("\"", "\\\""); 
	}

	private String noesc(double value){
		return String.valueOf(value);
	}
			
}
