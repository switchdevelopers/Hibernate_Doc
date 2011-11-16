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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.oy.shared.lm.graph.Graph;
import com.oy.shared.lm.graph.GraphNode;

public class StringUtil {

	public static void filterNodesByName(Map nameToNode, String includeText, String excludeText, Graph graph){
		includeNodesByName(nameToNode, includeText, graph);
		excludeNodesByName(nameToNode, excludeText, graph);
	}
	
	private static void includeNodesByName(Map nameToNode, String includeText, Graph graph){
		if (includeText == null || includeText.equals("")){
			return;
		}
		
		Set includes = new HashSet();
		StringUtil.splitAndFill(includeText, ",", includes);
		
		// remove excluded nodes
		Iterator iter = nameToNode.keySet().iterator();
		while(iter.hasNext()){
			String name = (String) iter.next();
			if (!includes.contains(name)){
				GraphNode node = (GraphNode) nameToNode.get(name);
				graph.removeNode(node);
			}
		}	
	}
	
	private static void excludeNodesByName(Map nameToNode, String excludesText, Graph graph){		
		Set excludes = new HashSet();
		StringUtil.splitAndFill(excludesText, ",", excludes);
		
		// remove excluded nodes
		String [] excludeNames = (String []) excludes.toArray(new String []{});
		for (int i=0; i < excludeNames.length; i++){
			GraphNode node = (GraphNode) nameToNode.get(excludeNames[i]);
			if (node != null){
				graph.removeNode(node);
			}
		}	
	}
	
	private static String [] split(String items, String separator){		
		// no items
		if (items == null || items.equals("")){
			return new String []{};
		}		
		items = items.replaceAll("\n", " ");
		
		// 1 item
		int idx = items.indexOf(separator);
		if (idx == -1){
			return new String []{items};
		}
		
		// many items
		String [] lines = items.split(separator);
		return lines;
	}
	
	public static void splitAndFill(String items, String separator, Set set){
		String [] lines = split(items, separator);
		for (int i=0; i < lines.length; i++){
			set.add(lines[i].trim());
		}				
	}
	
	public static void splitAndFill(String items, String separator, String [] list){
		String [] lines = split(items, separator);
		for (int i=0; i < lines.length; i++){
			if (i < list.length){
				list[i] = lines[i].trim();
			}
		}		
	}
	
	public static String stripPackageName(String name){
		int idx = name.lastIndexOf(".");
		if (idx == -1 || idx == name.length() - 1){
			return name;
		} 
		
		return name.substring(idx + 1);
	}
}
