package com.oy.shared.lm.in;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.oy.shared.lm.ant.TaskOptions;
import com.oy.shared.lm.graph.Graph;
import com.oy.shared.lm.graph.GraphEdge;
import com.oy.shared.lm.graph.GraphFactory;
import com.oy.shared.lm.graph.GraphNode;
import com.oy.shared.lm.graph.IGraphModel;

public class WSDLtoGRAPH {
	
	private Graph graph = GraphFactory.newGraph();
	private Document doc;
	private Map nameToNode = new HashMap();
	private TaskOptions options;

	private String [] colors = new String [] {
		"#FFFFCE",
		"#9C0031"
	};
		 
	final static int COLOR_FILL = 0;	
	final static int COLOR_LINE = 1;
	
	// these schemas must be properly handled
	final static String NS_WSDL = "http://schemas.xmlsoap.org/wsdl/";
	final static String NS_SOAP = "http://schemas.xmlsoap.org/wsdl/soap/";
	
	WSDLtoGRAPH(){ 
	}
	
	public static IGraphModel load(TaskOptions options) throws IOException, SAXException, ParserConfigurationException {
		return new WSDLtoGRAPH().innerLoad(options);
	}
	
	private IGraphModel innerLoad(TaskOptions options) throws IOException, SAXException, ParserConfigurationException {
		this.options = options; 
		
		options.applyTo(graph);
		
		StringUtil.splitAndFill(options.colors, ",", this.colors);		
		
		doc = XMLUtil.parse(options.expandEntityRef, true, options.inFile);
		XMLUtil.assertRootElement(doc, "definitions");
	
		processServicesList( XMLUtil.getChildNodesWithName(doc.getDocumentElement(), "service", NS_WSDL));		
			 
		// remove excluded nodes
		StringUtil.filterNodesByName(nameToNode, options.includes, options.excludes, graph);
				
		return graph;
	}
	
	private void processServicesList(List nodes){	
				
		for (int i=0 ;i < nodes.size(); i++){
			Node node = (Node) nodes.get(i);
			
			String attr = "";
			NamedNodeMap nnm = node.getAttributes();
			for (int j=0; j < nnm.getLength(); j++){
				attr += nnm.item(j).getNodeName() + "=" + XMLUtil.getAttributeValue(node, nnm.item(j).getNodeName(), "<value>");
			}			
			
			String doc = getDocumentation(node);
			if (doc != null && !"".equals(doc)){
				attr += "\n" + doc;
			}
			
			String name = XMLUtil.getAttributeValue(node, "name", "<name>");
			GraphNode service = getOrCreateNode("<<wsdl:service>>\n" + name, "service", attr, colors[COLOR_FILL]);
			 
			processPortList(XMLUtil.getChildNodesWithName(node, "port", NS_WSDL), service);
		}
	}
	
	private void processPortList(List nodes, GraphNode service){
		for (int i=0 ;i < nodes.size(); i++){
			Node node = (Node) nodes.get(i);
			
			String desc = "";
			Node address = XMLUtil.getChildNodeWithName(node, "address", NS_SOAP); 
			if (address != null){
				desc += XMLUtil.getAttributeValue(address, "location", "<address>");
			}
			String doc = getDocumentation(node);
			if (doc != null & !"".equals(doc)){
				if (!"".equals(desc)){
					desc += "\n";
				}
				desc += doc;
			}
			
			String name = XMLUtil.getAttributeValue(node, "name", "<name>");			
			GraphNode port = getOrCreateNode("<<port>>\n" + name, "port", desc, colors[COLOR_FILL]);
			
			String bName = processPortBinding(node, port);
			
			// link service to port
			GraphEdge edge = graph.addEdge(service, port);
			edge.getInfo().setLineColor(colors[COLOR_LINE]);
			edge.getInfo().setCaption(bName);
			edge.getInfo().setArrowTailOdiamond();
			edge.getInfo().setArrowHeadNone(); 
		}		
	}

	private String processPortBinding(Node node, GraphNode port){
		String name = XMLUtil.getAttributeValue(node, "binding", "<binding>");
		Node binding = XMLUtil.getTageByQName(doc, name, "binding", NS_WSDL);
		if (binding == null){
			throw new RuntimeException("Error locating binding for port " + name);
		}
		String bName = XMLUtil.getAttributeValue(binding, "name", "<name>");
		
		processBindingPortType(binding, port);
		
		return bName;
	}
	
	private void processBindingPortType(Node node, GraphNode port){
		String name = XMLUtil.getAttributeValue(node, "type", "<type>");
		Node type = XMLUtil.getTageByQName(doc, name, "portType", NS_WSDL);
		if (port == null){
			throw new RuntimeException("Error locating binding for port " + name);
		}		
		
		String tName = XMLUtil.getAttributeValue(type, "name", "<name>");		
		GraphNode tNode = getOrCreateNode("<<wdsl:portType>>\n" + tName, "portType", null, colors[COLOR_FILL]);			
		   
		// link binding to port type
		GraphEdge bEdge = graph.addEdge(port, tNode);
		bEdge.getInfo().setModeDashed();
		bEdge.getInfo().setArrowHeadOnormal();
		bEdge.getInfo().setLineColor(colors[COLOR_LINE]);
		
		processPortTypeOperations(name, XMLUtil.getChildNodesWithName(type, "operation", NS_WSDL), tNode);
	}
	
	private String formatOperationInput(Node operation){
		String in = "";
		Node input = XMLUtil.getChildNodeWithName(operation, "input", NS_WSDL);
		if (input != null){
			Node msg = getMessageForInOutFaultNode(XMLUtil.getAttributeValue(input, "message", ""));
			List parts = XMLUtil.getChildNodesWithName(msg, "part");
			for (int j=0; j < parts.size(); j++){
				if (j != 0){
					in += "; ";
				}
				
				Node part = (Node) parts.get(j);
				String type = XMLUtil.getAttributeValue(part, "type", null);
				if (type == null || "".equals(type)){
					type = XMLUtil.getAttributeValue(part, "element", "<type>");
				}
				type = XMLUtil.simplifyName(doc, type);
				
				in += XMLUtil.getAttributeValue(part, "name", "<name>") + " : " + type;
			}
		}
		
		return in;
	}
	
	private String formatOperationOutput(Node operation){
		String out = ""; 
		Node output = XMLUtil.getChildNodeWithName(operation, "output", NS_WSDL);
		if (output != null){
			Node msg = getMessageForInOutFaultNode(XMLUtil.getAttributeValue(output, "message", ""));
			Node result = XMLUtil.getChildNodeWithName(msg, "part", NS_WSDL);
			
			String type = XMLUtil.getAttributeValue(result, "type", null);
			if (type == null || "".equals(type)){
				type = XMLUtil.getAttributeValue(result, "element", "<type>");
			}
			type = XMLUtil.simplifyName(doc, type);

			out += " : " + type;
		}
		
		return out;
	}

	private String formatOperationFault(Node operation){
		String fail = ""; 
		Node fault = XMLUtil.getChildNodeWithName(operation, "fault", NS_WSDL);
		if (fault != null){
			Node msg = getMessageForInOutFaultNode(XMLUtil.getAttributeValue(fault, "message", ""));
			Node result = XMLUtil.getChildNodeWithName(msg, "part", NS_WSDL);
			
			String type = XMLUtil.getAttributeValue(result, "type", null);
			if (type == null || "".equals(type)){
				type = XMLUtil.getAttributeValue(result, "element", "<type>");
			}
			type = XMLUtil.simplifyName(doc, type);

			fail += " throws " + type;
		} 
		
		return fail;
	}
	
	private void processPortTypeOperations(String portTypeName, List nodes, GraphNode portType){
		StringBuffer sb = new StringBuffer();
		
		for (int i=0 ;i < nodes.size(); i++){
			Node node = (Node) nodes.get(i);
			
			String name = XMLUtil.getAttributeValue(node, "name", "<name>");

			String in = formatOperationInput(node);
			String out = formatOperationOutput(node);
			String fault = formatOperationFault(node);

			sb.append(name + "(" + in + ")" + out + fault + "\n");
		}
		
		portType.getInfo().setCaption(sb.toString());
	}
	
	private Node getMessageForInOutFaultNode(String name){
		List messages = XMLUtil.getChildNodesWithName(doc.getDocumentElement(), "message", NS_WSDL);
		for (int i=0; i < messages.size(); i++){
			Node node = (Node)messages.get(i);
			
			String qName = XMLUtil.qualifyName(doc, name);
			String qNodeName = XMLUtil.qualifyName(doc, XMLUtil.getAttributeValue(node, "name", null));
			
			if (qName.equals(qNodeName)){
				return node;
			}
		}
		throw new RuntimeException("Error locating message with name " + name);
	}
	
	private String getDocumentation(Node node){
		List docs = XMLUtil.getChildNodesWithName(node, "documentation", NS_WSDL);
		String buf = "";
		for (int i=0; i < docs.size(); i++){
			Node doc = (Node) docs.get(i);
			NodeList children = doc.getChildNodes(); 
			for (int j=0; j< children.getLength(); j++){
				if (children.item(j).getNodeType() == Node.TEXT_NODE || children.item(j).getNodeType() == Node.CDATA_SECTION_NODE){
					buf += children.item(j).getNodeValue() + "\n";	
				}				
			}			
		}
		
		return buf;
	}

	
	private GraphNode getOrCreateNode(String name, String type, String desc, String color){
		name = name.trim();
		
		String key = type + "." + name;
		
		GraphNode node = (GraphNode) nameToNode.get(key);
		if (node == null){
			node = graph.addNode();
			nameToNode.put(key, node);
								
			node.getInfo().setFillColor(color);	
						
			node.getInfo().setHeader(name);
			node.getInfo().setCaption(desc);
			node.getInfo().setLineColor(colors[COLOR_LINE]);
			node.getInfo().setModeSolid();			
		} 
		
		return node;
	}
	
}
