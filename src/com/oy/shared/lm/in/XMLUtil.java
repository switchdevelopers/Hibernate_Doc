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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDParser;

class XMLUtil {

	public static DTD parseDTD(String fileName) throws IOException {
		DTDParser parser = new DTDParser(new File(fileName), false);
        DTD dtd = parser.parse(true);         
        return dtd;
	}
	
	public static Document parse(boolean expandEntityReferences, boolean namespaceAware, String fileName) throws IOException, SAXException, ParserConfigurationException {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		factory.setNamespaceAware(namespaceAware);
		factory.setIgnoringElementContentWhitespace(true);
		
		factory.setValidating(false);		
		if(!expandEntityReferences){
			factory.setExpandEntityReferences(false);
		}
		
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		if(!expandEntityReferences){
	        builder.setEntityResolver(
	            new EntityResolver() {
	                public InputSource resolveEntity(String publicId, String systemId) {
	                    return new InputSource(new StringReader(""));			                
	                }
	            }
	        );
		}
		
		return builder.parse(fileName);		
	}

	public static void assertRootElement(Document doc, String rootElementName){
		assertRootElement(doc, rootElementName, null);
	}
	
	public static void assertRootElement(Document doc, String rootElementName, String namespaceURI){		
		if (!nodeHasNameNamespace(doc.getDocumentElement(), rootElementName, namespaceURI)){
			throw new IllegalArgumentException("Error parsing XML document, must have root element with name \"" + rootElementName + "\", found " + doc.getDocumentElement().getNodeName());	
		}
	}
	
	public  static boolean nodeHasNameNamespace(Node node, String name, String namespaceURI){
		String localName = node.getNodeName();
		
		String pre = node.getPrefix();
		if (pre != null){
			localName = localName.substring(pre.length() + 1); 
		}
		
		if (namespaceURI != null){
			if (!namespaceURI.equals(node.getNamespaceURI())){
				return false;
			}
		}
		
		return name.equals(localName);
	}

	public static Node getTageByQName(Document doc, String qname, String tagName, String namespaceURI){
		List bindings = XMLUtil.getChildNodesWithName(doc.getDocumentElement(), tagName, namespaceURI);
		
		qname = XMLUtil.qualifyName(doc, qname);
		for (int i=0 ;i < bindings.size(); i++){
			Node node = (Node) bindings.get(i);
			String name = XMLUtil.qualifyName(doc, XMLUtil.getAttributeValue(node, "name", "<name>")); 
			if (qname.equals(name)){ 
				return node;
			}
		}
	
		return null;
	}

	// this function strips namespace from name is namesapce is the same as tagetNamespace
	public static String simplifyName(Document doc, String name){
		if (name == null){
			return name;
		}
		
		// parse name into local name and prefix pre:local
		int idx = name.indexOf(":");
		String localName = name;
		if (idx != -1 && idx > 0){
			localName = name.substring(idx + 1);
		}
		
		return localName;
	}
	
	// this function resolves "pre:name" into "namespaceURL:name"
	public static String qualifyName(Document doc, String name){
		
		// parse name into local name and prefix pre:local
		int idx = name.indexOf(":");
		String localName = name;
		String pre = null;
		if (idx != -1 && idx > 0){
			localName = name.substring(idx + 1);
			pre = name.substring(0, idx);
		}
		
		// if no prefix was specified we use document namespaceURI
		if (pre == null){
			String ns = doc.getDocumentElement().getAttribute("targetNamespace");
			if (ns == null || "".equals(ns)){
				return doc.getDocumentElement().getNamespaceURI() + ":" + localName;	
			} else {
				return ns + ":" + localName;
			}
		}
		
		// resolve prefix into namespace URI
		String ns = doc.getDocumentElement().getAttribute("xmlns:" + pre);
		if (ns == null || "".equals(ns)){
			throw new RuntimeException("Bad namespace specified in " + name);
		}
		
		return ns + ":" + localName;
	}
	
    public static String getAttributeValue(Node node, String name, String defaultValue) {
        try {
            return node.getAttributes().getNamedItem(name).getNodeValue();
        } catch (Exception ex) {
        	return defaultValue;
        }
    }
    
	public static List getChildNodesWithName(Node node, String name){
		return getChildNodesWithName(node, name, null);
	}	
    
    public static List getChildNodesWithName(Node node, String name, String namespace){
		List all = new ArrayList();
		for (int i=0; i < node.getChildNodes().getLength(); i++){
			Node child = node.getChildNodes().item(i);
			if (nodeHasNameNamespace(child, name, namespace)){
				all.add(child);
			}
		}
		
		return all;    	
    }
    	
    public static Node getChildNodeWithName(Node node, String name){
    	return getChildNodeWithName(node, name, null);
    }
    
	public static Node getChildNodeWithName(Node node, String name, String namespaceURI){
		List all = getChildNodesWithName(node, name, namespaceURI);
		 
		if(all.size() > 1){
			throw new IllegalArgumentException("Expected only one node " + name + " for " + node);
		}
		
		if (all.size() > 0){
			return (Node) all.get(0);
		}
		return null;
	}     
	    
}
