/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 将算法预处理后的线，在CAD中画在一个全新的图层zhlns中，通过该文件把预处理后的线读到数据库中
 */
package readdxfmy2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

/**
 *
 * @author hello
 */
public class read_zhlns_to_database {
    
    public static String fileName=null;
    private static int zbsrid=32774;
    public static String pretbname="predxf";
    public static List<Point> allpnt=new ArrayList();//将所有的点不重复地保存
    
    public static void readEntities(BufferedReader bfr,String tucg) throws IOException, InstantiationException, IllegalAccessException, SQLException, ClassNotFoundException{
      int geonum=0;
      
      String s1=null,s2=null;
      int flag=0;//指示所读是否为ENTITIES段，1为是，0为否。
       while((s1 = bfr.readLine())!=null && (s2 = bfr.readLine())!=null){ //&& s2.equals("ENDSEC")==false){
         if(s1.equals("  0") && s2.equals("SECTION")){
            s1=bfr.readLine();
            s2=bfr.readLine();
            if(s1.equals("  2") && s2.equals("ENTITIES")) { flag=1; System.out.println("begin in Entities");continue; }   //开始读ENTITIES段
         }
         if(flag==0) continue;    //所读内容不是ENTITIES段
         if(flag==1 && s1.equals("  0") && s2.equals("ENDSEC")) { flag=0; System.out.println("end of Entities"); break; }  //读完ENTITIES段
         if(s1.equals("  0")){//不判断s1直接判断s2是不对的。
          if(s2.equals("LINE"))
          {//直线
            Line ln=readLine(bfr,tucg);
            if(ln!=null){
                JGeometry geo=JGeometry.createLinearLineString(ln.zb,2,zbsrid);
                store(pretbname,geo,geonum++);
            }
          }
          else if(s2.equals("LWPOLYLINE"))
          {//多段线
            LWpolyline lwln=readLWpolyline(bfr,tucg);
            if(lwln!=null){
                JGeometry geo=JGeometry.createLinearLineString(lwln.zb,2,zbsrid);
                store(pretbname,geo,geonum++);
            }
          }/*
          else if(s2.equals("CIRCLE")){//圆
            Circle ccle=readCircle(bfr,READENTITIES);
            if(ccle!=null){
            JGeometry geo=JGeometry.createCircle(ccle.center.x,ccle.center.y,ccle.radius,zbsrid);
            connect_database(geo);
            //allty.add(geo);
            }
          }
          else if(s2.equals("ARC")){//弧
            Arc arc=readArc(bfr,READENTITIES);
            if(arc!=null){
            JGeometry geo=JGeometry.createArc2d(arc.zb, 2, zbsrid);  //new JGeometry(2,zbsrid,elemI,ordinates);
            connect_database(geo);
            //allty.add(geo);
            }
          }
          else if(s2.equals("INSERT")){//参照块图元
            readInsert(bfr,READENTITIES);
          }
          else if(s2.equals("SOLID")){
            Solid sld=readSolid(bfr,READENTITIES);
            if(sld!=null){
            JGeometry geo=JGeometry.createLinearLineString(sld.zb,2,zbsrid);
            connect_database(geo);
            }
          }*/
         }//if-s1
       }
    }
    
    public static LWpolyline readLWpolyline(BufferedReader bfr,String tucg) throws IOException, InstantiationException, IllegalAccessException, SQLException{
             //这里设定一个模式moshi,如果为0，则是readBlocks调用，为1则是readEntities调用     
             double x=0,y=0;
             String s1=null,s2=null;
             while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals("  8")==false);  //图层
             String tc=new String(s2);
             while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 90")==false);  //顶点数
             int num=Integer.parseInt(s2.trim());
             while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 70")==false);  //闭合性
             int cls=Integer.parseInt(s2.trim());   //吐槽：尼玛坑爹啊，自己通过画图对比才知道这个标志多段线是否闭合啊（帮助文档里竟然写关闭，不是闭合。。。）
             if(cls==1) num=num+1;//闭合的话，最后重复第一个点
             double ordinates[] = new double[num*2];
             
             for(int i=0;i<(cls==1?num-1:num);++i){//中间的表达式，是让i小于原始的num
                 while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 10")==false);  //坐标
                 x=Double.parseDouble(s2);
                 while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 20")==false);  //坐标
                 y=Double.parseDouble(s2);
                 ordinates[i*2]=x;
                 ordinates[i*2+1]=y;
             }
             if(tc.equals(tucg)==false) return null;
             
             if(cls==1) { ordinates[2*num-2]=ordinates[0]; ordinates[2*num-1]=ordinates[1];}//重复第一点
             
             for(int i=0;i<num*2;i=i+2)
             {
               Point temppnt=new Point(ordinates[i],ordinates[i+1]);
               if(allpnt.contains(temppnt)==false)  allpnt.add(temppnt);
             }
             
             LWpolyline lwln=new LWpolyline(num,ordinates);
             
             return lwln;
    }
    
    public static Line readLine(BufferedReader bfr,String tucg) throws InstantiationException, IllegalAccessException, SQLException, IOException{
           //这里假设坐标是按照xyz的顺序给出的，若要与顺序无关，可以把下面程序改成在while循环里一直读，然后循环里做判断。
           double x1=0,x2=0,y1=0,y2=0,z1=0,z2=0;
           String s1=null,s2=null;
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals("  8")==false);  //图层
           String tc=new String(s2);
           //获取点坐标
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 10")==false);  //x坐标
           x1=Double.parseDouble(s2);
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 20")==false);
           y1=Double.valueOf(s2).doubleValue();
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 30")==false);
           z1=Double.valueOf(s2).doubleValue();
           
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 11")==false);  //x坐标
           x2=Double.valueOf(s2).doubleValue();   
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 21")==false);
           y2=Double.valueOf(s2).doubleValue();
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 31")==false);
           z2=Double.valueOf(s2).doubleValue();
           
           if(tc.equals(tucg)==false) return null;//不是所在图层
           
           Point p1=new Point(x1,y1);
           Point p2=new Point(x2,y2);
           if(allpnt.contains(p1)==false)  allpnt.add(p1);
           if(allpnt.contains(p2)==false)  allpnt.add(p2);
           
           Line l1=new Line(x1,y1,x2,y2);
           return l1;
    }
    
    public static void store(String tbName,JGeometry geo,int n) throws InstantiationException, IllegalAccessException, SQLException{//把编号为n的对象geo存入数据库表中       
           //建立数据库连接   
           String Driver="oracle.jdbc.driver.OracleDriver";    //连接数据库的方法    
           String URL="jdbc:oracle:thin:@127.0.0.1:1521:cad";    //cad为数据库的SID    
           String Username="cadadmin";    //用户名               
           String Password="cad";    //密码    
           //String tbName="room";
           try {
                   Class.forName(Driver).newInstance();    //加载数据库驱动
                   Connection con=DriverManager.getConnection(URL,Username,Password);  
                   if(!con.isClosed())
                       System.out.println("Succeeded connecting to the Database!");
                   
                   String sqlInsert="INSERT INTO "+tbName+"("+"idC"+","+" geom"+")"+" VALUES("+n+",?)";
                   System.out.println("Executing query:'"+sqlInsert+"'");
                   PreparedStatement stmt=con.prepareStatement(sqlInsert);
                   STRUCT dbObject=JGeometry.store(geo,con);
                   stmt.setObject(1, dbObject);
                   stmt.execute();
                   stmt.close();
                   
                   con.close();  
               } catch (ClassNotFoundException ex) {
                   Logger.getLogger(Readdxfmy2.class.getName()).log(Level.SEVERE, null, ex);
               }
    }
    
    public static void main(String[] args) throws FileNotFoundException, InstantiationException, IllegalAccessException, SQLException, IOException, ClassNotFoundException
    {
        fileName="C:\\\\Users\\\\hello\\\\Documents\\\\NetBeansProjects\\\\readdxflunwen\\\\电三右部2-zhlns.dxf";
        String tucg="zhlns";//线所在的图层名
        
        File file=new File(fileName);
        FileReader fr=new FileReader(file);
        BufferedReader bfr= new BufferedReader(fr);
        readEntities(bfr,tucg);
        bfr.close();
        fr.close();
        
        //存储点
        String pnttbname="pnt";
        for(int i=0;i<allpnt.size();++i)
        {
          Point tp=allpnt.get(i);
          JGeometry geopnt=new JGeometry(tp.x,tp.y,zbsrid);
          store(pnttbname,geopnt,i);
        }
    }
    
}
