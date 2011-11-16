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

import java.io.FileOutputStream;
import java.io.IOException;

import com.oy.shared.lm.graph.IGraphModel;

public class GRAPHtoDOTtoGIF {

	GRAPHtoDOTtoGIF(){}
	
	public static void transform(IGraphModel igraph, String dotFileName, String gifFileName, String dotExeFileName) throws IOException {
		new GRAPHtoDOTtoGIF().innerTransform(igraph, dotFileName, gifFileName, dotExeFileName);
	}
	
	public void innerTransform(IGraphModel igraph, String dotFileName, String gifFileName, String dotExeFileName) throws IOException {
		FileOutputStream dot = new FileOutputStream(dotFileName);
		try {
			GRAPHtoDOT.transform(igraph, dot);
			DOTtoGIF.transform(dotExeFileName, dotFileName, gifFileName);
		} finally {
			dot.close();
		} 		
	}
	
}
