/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.jswitch.base.modelo.HibernateUtil;
import com.oy.shared.lm.ant.TaskOptions;
import com.oy.shared.lm.ext.HBMCtoGRAPH;
import com.oy.shared.lm.graph.IGraphModel;
import com.oy.shared.lm.out.GRAPHtoDOTtoGIF;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.cfg.AnnotationConfiguration;

/**
 *
 * @author Personal
 */
public class GenerarUML {

    public static void main(String[] args) {

        try {
            new GenerarUML().exampleOfUse();
        } catch (Exception ex) {
            Logger.getLogger(GenerarUML.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void exampleOfUse() throws Exception {


        // new Hibernate config, add one file
        //Configuration conf = new Configuration();
        //AnnotationConfiguration conf = new AnnotationConfiguration().configure();
        AnnotationConfiguration conf = HibernateUtil.getAnnotationConfiguration().configure();
        conf.buildMappings();
//        Session s = HibernateUtil.getSessionFactory().openSession();
//
//        s.createQuery("FROM "+Poliza.class.getName()).setMaxResults(1).uniqueResult();
//        s.close();

//        System.out.println(conf.getClassMapping("com.jswitch.polizas.modelo.maestra.Poliza"));

        // new options, set caption and colors
        TaskOptions opt = new TaskOptions();
        opt.caption = "My Test";
        opt.colors = "#FF5937, black, blue";
        opt.excludes = "com.jswitch.base.modelo.entidades.auditoria.AuditoriaBasica";

        // create generic graph
        IGraphModel graph = HBMCtoGRAPH.load(opt, conf);

        // convert graph into image file
        GRAPHtoDOTtoGIF.transform(
                graph,
                "myTest55.dot", "myTest55.gif", "C:\\Users\\Personal\\Downloads\\oy-lm-1.4\\oy-lm-1.4\\bin\\graphviz-2.4\\bin\\dot.exe");
    }
}
