/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 由于这里要用到Graph类型，在readdxfmy2包里新建了Graph.java类。但是IndoorSTG中的Graph是indoorshow.Graph类型，不匹配。
 * 所以此文件用不到了。
 * 导入indoorSGT需要在IndoorSTG的代码库里增加代码
 */
package readdxfmy2;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hello
 */
public class import_to_indoorstg {
    
    public static void store_room_stg(List<List<Integer>> roomCandidates){//以IndoorSTG的方式存储房间 
        int floor=0;
        int type=0;//0是房间，4是边界
        boolean iscontext=false;
        Color fillc=null, borderc=Color.BLACK;
        int cx=100;
        int cy=100;
        int cw=500;
        int ch=400;
        Graph tgraph=new Graph(floor, type, iscontext, fillc, borderc, cx, cy, cw, ch, "");
        Graph t2graph=new Graph(floor,4,iscontext,fillc,borderc,600,100,200,0,"");
        Graph t3graph=new Graph(floor,4,iscontext,fillc,borderc,0,0,50,0,"");//type为4的时候，即线段，依次为x1,y1,x2,y2
        Graph t4graph=new Graph(floor,4,iscontext,fillc,borderc,50,0,50,50,"");
        Graph t5graph=new Graph(floor,4,iscontext,fillc,borderc,50,50,0,50,"");
        Graph t6graph=new Graph(floor,4,iscontext,fillc,borderc,0,50,0,0,"");
        ArrayList<Graph> tgraphs = new ArrayList<Graph>();
        tgraphs.add(tgraph);
        tgraphs.add(t2graph);
        tgraphs.add(t3graph);
        tgraphs.add(t4graph);
        tgraphs.add(t5graph);
        tgraphs.add(t6graph);
        /*Graph tgraph=null;
        for(int i=0;i<roomCandidates.size();++i){//遍历房间集
          List<Integer> rmlst=roomCandidates.get(i);//i号房间
          for(int j=0;j<rmlst.size();++j){
            Line templn=roomlns.get(rmlst.get(j));//roomlns的第rmlst.get(j)号线
            int x1=(int)templn.qd.x;
            int y1=(int)templn.qd.y;
            int x2=(int)templn.zd.x;
            int y2=(int)templn.zd.y;
            tgraph=new Graph(floor,4,iscontext,fillc,borderc,x1,y1,x2,y2,"");
            tgraphs.add(tgraph);
          }
        }
        //static void save() {
        for (Graph g : tgraphs) {
            g.setCurrent(false);
        }*/
        //int x = jfc.showSaveDialog(envirSet);
        //if (x == JFileChooser.APPROVE_OPTION) {
            //File f = jfc.getSelectedFile();
            File f=new File("test-floor0");
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
                oos.writeObject(tgraphs);
                oos.flush();
                oos.close();
            } catch (Exception e) {
            }
    }
    
    public static void save() {
        ArrayList<Graph> graphs = new ArrayList<Graph>();
        
        int floor=0;
        int type=0;//0是房间，4是边界
        boolean iscontext=false;
        Color fillc=null, borderc=Color.BLACK;
        int cx=100;
        int cy=100;
        int cw=500;
        int ch=400;
        Graph t3graph=new Graph(floor,4,iscontext,fillc,borderc,0,0,50,0,"");//type为4的时候，即线段，依次为x1,y1,x2,y2
        
        graphs.add(t3graph);
        for (Graph g : graphs) {
            g.setCurrent(false);
        }
        //int x = jfc.showSaveDialog(envirSet);
        //if (x == JFileChooser.APPROVE_OPTION) {
            File f = new File("imp_stg1");
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
                oos.writeObject(graphs);
                oos.flush();
                oos.close();
            } catch (Exception e) {
            }
        //}
    }
    
    public static void main(String[] args) throws SQLException, IOException {
        // TODO code application logic here
        save();
    }
}
