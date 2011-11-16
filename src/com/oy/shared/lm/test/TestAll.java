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
package com.oy.shared.lm.test;

import com.oy.shared.lm.ant.TaskOptions;
import java.io.File;
import java.util.Properties;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import com.oy.shared.lm.graph.Graph;
import com.oy.shared.lm.graph.GraphEdge;
import com.oy.shared.lm.graph.GraphFactory;
import com.oy.shared.lm.graph.GraphNode;
import com.oy.shared.lm.graph.IGraphModel;
import com.oy.shared.lm.in.HBMtoGRAPH;
import com.oy.shared.lm.out.GRAPHtoDOTtoGIF;
import org.hibernate.cfg.Configuration;

/*
Properties need for this to work: 

-Doy.sl.out.folder=E:\\dev\\3g\\eclipse-3.1\\oy-lm-1.2\\doc\\test	 	 	 	 
-Doy.sl.ant.file=E:\\dev\\3g\\eclipse-3.1\\oy-lm-1.2\\runme.xml
-Doy.sl.exe.file=E:\\dev\\3g\\eclipse-3.1\\oy-lm-1.2\\bin\\graphviz-2.4\\bin\\dot.exe

 */
public class TestAll {

    private final String antFile = "C:\\Users\\Personal\\Downloads\\oy-lm-1.4\\oy-lm-1.4\\runme.xml";
    private final String exeFile = "C:\\Users\\Personal\\Downloads\\oy-lm-1.4\\oy-lm-1.4\\bin\\graphviz-2.4\\bin\\dot.exe";
    private final String outFolder = "C:\\Users\\Personal\\Downloads\\oy-lm-1.4\\oy-lm-1.4\\bin\\graphviz-2.4\\bin";

    public static void main(String[] args) {
        new TestAll().main();
    }

    private void main() {

        String ant = antFile;
        String exe = exeFile;
        String dot = outFolder;
        if (ant == null || exe == null || dot == null) {
            throw new RuntimeException("Please define these properties oy.sl.ant.file, oy.sl.exe.file, oy.sl.out.folder.");
        }


        System.out.println("Started...");
        try {
            //testANT();
            //simpleDraw();
            //manualDraw();


            //Configuration conf = new Configuration();
            //conf.addFile("src/com/oy/shared/lm/test/hbm/ABC.hbm.xml");

            //conf.buildMappings();

            // new options, set caption and colors
            TaskOptions opt = new TaskOptions();
            opt.caption = "My Test";
            opt.colors = "#FF5937, black, blue";
            //opt.inFile="src/com/oy/shared/lm/test/hbm/ABC.hbm.xml";
            opt.inFileSet=new String[]{"src/com/oy/shared/lm/test/hbm/Animal.hbm.xml"};

            // create generic graph
            IGraphModel graph = HBMtoGRAPH.load(opt);

            // convert graph into image file
            GRAPHtoDOTtoGIF.transform(
                    graph,
                    "myTest.dot", "myTest.gif", "C:\\Users\\Personal\\Downloads\\oy-lm-1.4\\oy-lm-1.4\\bin\\graphviz-2.4\\bin\\dot.exe");



            System.out.print("Completed: OK, results in " + outFolder);
        } catch (Exception e) {
            System.out.print("\nCompleted: FAILED");
            e.printStackTrace();
        }
    }

    private void testANT() throws Exception {

        try {
            // setup and run project
            File buildFile = new File(antFile);
            Project p = new Project();
            p.init();
            ProjectHelper.getProjectHelper().parse(p, buildFile);
            p.executeTarget(p.getDefaultTarget());

            // load properties from project
            Properties props = new Properties();
            props.putAll(p.getUserProperties());
        } catch (Exception e) {
            System.out.println("Error accessing ant file " + antFile);
            e.printStackTrace();
            throw e;
        }
    }

    private void manualDraw() throws Exception {

        //
        // in this example it is clear that domain expert
        // must keep lots of internal state in his hands

        //
        // a, b, c, ab, bc, ca, *.getDecoration() are
        // examples of state that must be maintained by
        // the domain expert; it is clear that domain expert
        // must keep lots of internal state; this must
        // be avoided as mush as possible
        //

        // build graph
        Graph graph = GraphFactory.newGraph();
        graph.getInfo().setCaption("My test drawing");
        graph.getInfo().setRotated(true);

        GraphNode a = graph.addNode();
        a.getInfo().setShapeDiamond();
        a.getInfo().setCaption("blue on yellow\nred lines\nrounded");
        a.getInfo().setFillColor("yellow");
        a.getInfo().setLineColor("red");
        a.getInfo().setRounded(true);
        a.getInfo().setFontColor("blue");

        GraphNode b = graph.addNode();
        b.getInfo().setShapeTriangle();
        b.getInfo().setCaption("Red Courier");
        b.getInfo().setFillColor("red");
        b.getInfo().setFontName("Courier");

        GraphNode c = graph.addNode();
        c.getInfo().setHeader("header");
        c.getInfo().setCaption("blue with header/footer");
        c.getInfo().setFooter("footer");
        c.getInfo().setFillColor("blue");

        GraphEdge ab = graph.addEdge(a, b);
        ab.getInfo().setCaption("blue text on orange line");
        ab.getInfo().setModeDashed();
        ab.getInfo().setArrowTailDot();
        ab.getInfo().setArrowHeadOnormal();
        ab.getInfo().setFontColor("blue");
        ab.getInfo().setLineColor("orange");
        ab.getInfo().setModeDashed();

        GraphEdge bc = graph.addEdge(b, c);
        bc.getInfo().setCaption("B -> C");
        bc.getInfo().setHeadCaption("0..1");
        bc.getInfo().setTailCaption("0..n");
        bc.getInfo().setArrowHeadDiamond();
        bc.getInfo().setModeDotted();

        GraphEdge ca = graph.addEdge(c, a);
        ca.getInfo().setCaption("C -> A");
        ca.getInfo().setHeadCaption("0..2");
        ca.getInfo().setLineColor("red");
        ca.getInfo().setAttributes("decorate\"true\"");
        ca.getInfo().setModeBold();

        // output graph to *.gif
        final String dotFileName = outFolder + "\\ManualDraw.dot";
        final String gifFileName = outFolder + "\\ManualDraw.gif";

        GRAPHtoDOTtoGIF.transform(graph, dotFileName, gifFileName, exeFile);
    }

    private void simpleDraw() throws Exception {

        // build graph
        Graph graph = GraphFactory.newGraph();
        graph.getInfo().setCaption("My test drawing");

        GraphNode f = graph.addNode();
        f.getInfo().setCaption("father");
        f.getInfo().setFillColor("cyan");

        GraphNode m = graph.addNode();
        m.getInfo().setCaption("mother");
        m.getInfo().setFillColor("pink");

        GraphNode c = graph.addNode();
        c.getInfo().setCaption("child");
        m.getInfo().setFillColor("yellow");

        GraphEdge ab = graph.addEdge(f, c);
        ab.getInfo().setCaption("parent");

        GraphEdge bc = graph.addEdge(m, c);
        bc.getInfo().setCaption("parent");

        // output graph to *.gif
        final String dotFileName = outFolder + "\\SimpleDraw.dot";
        final String gifFileName = outFolder + "\\SimpleDraw.gif";

        GRAPHtoDOTtoGIF.transform(graph, dotFileName, gifFileName, exeFile);
    }
}
