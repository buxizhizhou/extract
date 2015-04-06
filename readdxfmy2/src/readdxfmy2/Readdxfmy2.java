/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 实现论文的版本
 * 增加找后继线的预处理优化
 * 非递归版本
 * 在非递归版本上增加的，把识别的房间存入Oracle Spatial
 * 在上一版本基础上修改：聚类，构建矩形，构造线
 * 在readdxfmy1基础上修改程序，能存入每个门对应的房间集合。 也修改了TUYUAN.java文件，增加了Line::is_vertical()函数
 * extract文件夹，即加入了git版本控制
 */
package readdxfmy2;

import java.awt.Color;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.spatial.geometry.*;
import oracle.jdbc.driver.*;
import oracle.sql.STRUCT;
/**
 *
 * @author User
 */
public class Readdxfmy2 {

    /**
     * @param args the command line arguments
     */
    //对不同文件不要忘了修改ypxl和图层的add
    /**/
    public static String fileName=null;
    
    public static double ypxl=550;//300;近似数
    public static double jsjl=380;//300;//平行线间的可近似距离，一般为墙宽度
    public static double szxpc=10;//竖直线的偏差
    public static double stair_jsjl=550;//当楼梯仅由一组平行线构成，而且它们之间没其他楼梯图层的线相连。它们之间的距离设为此值
    
    //final public static String fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxflunwen\\一层平面图_t3.dxf";
    //final public static String fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxflunwen\\一层平面图窗户有线.dxf";
    //final public static String fileName="C:\\\\Users\\\\User\\\\Documents\\\\NetBeansProjects\\\\readdxflunwen\\\\一层平面图_t3修改.dxf";
    final public static String tableName="cad";
    //final public static String fileName="标准层_t3.dxf";
    //final public static String fileName="C:\\\\Users\\\\User\\\\Documents\\\\NetBeansProjects\\\\readdxflunwen\\\\ts平行四边形.dxf";
    //final public static String fileName="C:\\\\Users\\\\User\\\\Documents\\\\NetBeansProjects\\\\readdxflunwen\\\\from某宾馆平面图设计方案.dxf";
    //final public static String fileName="C:\\\\Users\\\\User\\\\Documents\\\\NetBeansProjects\\\\readdxflunwen\\\\from某宾馆平面图设计方案修改.dxf";
    //final public static String fileName="C:\\\\Users\\\\User\\\\Documents\\\\NetBeansProjects\\\\readdxflunwen\\\\from惠州酒店十层平面图.dxf";
    //final public static String fileName="C:\\\\Users\\\\User\\\\Documents\\\\NetBeansProjects\\\\readdxflunwen\\\\from肖豹-中天宾馆图6.dxf";
    //final public static String fileName="ts方框.dxf";
    //final public static String fileName="ts双弧门.dxf";
    //final public static String fileName="ts简单房间.dxf";
    //final public static String fileName="ts简单房间修改.dxf";
    //final public static String fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxflunwen\\ts简单精确房间修改.dxf";
    //final public static String fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxflunwen\\ts两个房间.dxf";
    //final public static String fileName="ts简单两个房间.dxf";//只两个小房间，没找到大房间
    //final public static String fileName="ts两个房间修改2.5.dxf";
    //final public static String fileName="ts两个房间修改2.dxf";
    //final public static String fileName="ts两个房间修改3.dxf";
    //final public static String fileName="ts两个房间修改3-1.dxf";
    //final public static String fileName="ts两个房间修改4.dxf";
    //final public static String fileName="ts右边两个房间.dxf";
    //final public static String fileName="ts左边两个房间.dxf";
    //final public static String fileName="ts两个房间复制.dxf";
    //final public static String fileName="ts左边2号房间.dxf";
    //final public static String fileName="ts两个房间的左边一个房间.dxf";
    //final public static String fileName="ts右边三个房间.dxf";
    //final public static String fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxflunwen\\ts上面五个房间.dxf";
    //final public static String fileName="C:\\\\Users\\\\User\\\\Documents\\\\NetBeansProjects\\\\readdxflunwen\\\\ts去除右下角房间.dxf";
    //final public static String fileName="ts简单封闭右下角.dxf";
    //final public static String fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxflunwen\\ts走廊.dxf";
    //final public static String fileName="ts走廊修改.dxf";
    //final public static String fileName="ts单一走廊.dxf";
    //final public static String fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxflunwen\\ts单一原样走廊.dxf";
    //final public static String fileName="ts走廊加右一房间.dxf";
    
    //final public static String fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxflunwen\\ts住宅建筑左部.dxf";
    //final public static String fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxflunwen\\ts住宅建筑左半部分.dxf";
    //final public static String fileName="C:\\\\Users\\\\User\\\\Documents\\\\NetBeansProjects\\\\readdxflunwen\\\\from住宅建筑平面图左下.dxf";
    
    //final public static String fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxflunwen\\ts惠州酒店十层左上部.dxf";
    
    public static double tminx=100000000;//图纸的四个边界
    public static double tminy=100000000;
    public static double tmaxx=-100000000;
    public static double tmaxy=-100000000;
    
    public static int num=0;
    public static int cnt=0;
    final public static int zbsrid=32774;
    final public static int READBLOCKS=0;
    final public static int READENTITIES=1;
    final public static int CHANGXIAN=0;
    final public static int DUANXIAN=1;
    final public static int QQ=1;
    final public static int QZ=2;
    final public static int ZQ=3;
    final public static int ZZ=4;
    final public static int QIDIAN=0;
    final public static int ZHONGDIAN=1;
    public static List<Map<Integer,Integer>> qsuccessors=new ArrayList();//存储每根线的起点的后继线的索引号和标记(0代表邻接点为起点，1代表为终点)
    public static List<Map<Integer,Integer>> zsuccessors=new ArrayList();//存储每根线的终点的后继线的索引号和标记(索引为i的线与索引为MAP-INT的线交于后者的MAP-INT标记)

    
    public static List<List> lst=new ArrayList();//存储每个块的图元组成部分。  注意static
    public static List<String> kname=new ArrayList();//lst相应的块名称
    public static List<String> filter=new ArrayList();//不去读取图元的图层集合
    public static List<String> mentc=new ArrayList();//门所在图层集合
    public static List<String> fangjtc=new ArrayList();//房间所在图层集合
    public static List<String> strtc=new ArrayList();//楼梯所在的图层集合
    //public static List mentuy=new ArrayList();//存储门图层里的图元
    public static List<Line> doorlns=new ArrayList();//存储门图层里的线段图元
    public static List<Arc> doorarc=new ArrayList();//存储门图层里的弧图元
    public static List<Line> doorsteps=new LinkedList();//经识别门确定的结果门槛集合
    public static List<List> blockty=new ArrayList();//存储所有的块图元
    //public static List fangjtuy=new ArrayList();//存储房间图层里的图元
    public static List<Line> roomlns=new LinkedList();//存储房间图层里的线段图元
    public static List<Arc> roomarc=new ArrayList();//存储房间图层里的弧图元
    public static List<String> walltc=new ArrayList();//墙所在图层集合
    public static List walltuy=new ArrayList();//墙图层里的图元
    //public static String wintcnm="WINDOW";//门窗的图层名
    //public static List allty=new ArrayList();//存储所有的图元
    //public static List door=new ArrayList();//存由门变换过来的直线段
    public static List<Line> stairlns=new LinkedList();//存储楼梯图层里的线段图元
    
    public static String resflname="roomzb.txt";//结果文件，保存每个房间的编号，点坐标
    
    public static void connect_database(JGeometry geo){//调试用的，替换前一个函数，不读写数据库会快些。
     return ;   
    }
    /*
    public static void connect_database(JGeometry geo) throws InstantiationException, IllegalAccessException, SQLException{
           //建立数据库连接   
           String Driver="oracle.jdbc.driver.OracleDriver";    //连接数据库的方法    
           String URL="jdbc:oracle:thin:@127.0.0.1:1521:cad";    //cad为数据库的SID    
           String Username="cadadmin";    //用户名               
           String Password="cad";    //密码    
           //String tableName="cad";
           try {
                   Class.forName(Driver).newInstance();    //加载数据库驱动
                   Connection con=DriverManager.getConnection(URL,Username,Password);  
                   if(!con.isClosed())
                       System.out.println("Succeeded connecting to the Database!");
                   //Statement stmt=con.createStatement();
                   
                   String sqlInsert="INSERT INTO "+tableName+"("+"idC"+","+" geom"+")"+" VALUES("+cnt+",?)";
                   cnt++;
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
    }*/
    
    public static void store_door(String tbName,JGeometry geo,int n) throws InstantiationException, IllegalAccessException, SQLException{
           //建立数据库连接   
           String Driver="oracle.jdbc.driver.OracleDriver";    //连接数据库的方法    
           String URL="jdbc:oracle:thin:@127.0.0.1:1521:cad";    //cad为数据库的SID    
           String Username="cadadmin";    //用户名               
           String Password="cad";    //密码
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
    
    public static double pnt_dist(Point a,Point b){//返回两点间的直线距离
      return Math.sqrt((a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y));
    }
    
    public static boolean aqlpnt(Point a,Point b){//两点近似相等
      double dist=Math.sqrt((a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y));
      if(dist<ypxl) return true;
      else return false;
    }
    
    public static int aqlln(Line ln1,Line ln2){//两条线近似相等 返回0表示不近似相等，1表示起点和起点近似相等，2表示起点和终点近似相等
      /*if(aqlpnt(ln1.qd,ln2.qd) && aqlpnt(ln1.zd,ln2.zd)) return 1;
      else if(aqlpnt(ln1.qd,ln2.zd) && aqlpnt(ln1.zd,ln2.qd)) return 2;
      else return 0;*/
      //可能出现起点起点-终点终点，起点终点-终点起点这两种组合都能近似相等的情况，这样选择更近的那种
      double qq=pnt_dist(ln1.qd,ln2.qd);
      double qz=pnt_dist(ln1.qd,ln2.zd);
      double zq=pnt_dist(ln1.zd,ln2.qd);
      double zz=pnt_dist(ln1.zd,ln2.zd);
      if((qq<ypxl && zz<ypxl)||(qz<ypxl && zq<ypxl)){//近似相等的两种情况
        if(qq<ypxl && zz<ypxl && qz<ypxl && zq<ypxl){
          if(qq<qz) return 1;//两种都近似相等，选择最近的那种
          else return 2;
        }
        else{
          if(qq<ypxl && zz<ypxl) return 1;
          else return 2;
        }
     }
     else return 0;
    }
    
    public static boolean contns(Line ln1,Line ln2){//ln1包含ln2
      //暂时写不好~
      return false;
    }
    
    public static boolean pntadjln(Point p,Line ln){//点邻接于线
      if(aqlpnt(p,ln.qd)) return true;
      if(aqlpnt(p,ln.zd)) return true;
      return false;
    }
    
    public static int lnadjln(Line ln1,Line ln2){//线邻接于线，返回0表示不邻接，1表示两个起点邻接，2表示起点终点邻接，3表示终点起点邻接，4表示终点终点邻接
      /*if(pntadjln(ln1.qd,ln2)) return 1;
      if(pntadjln(ln1.zd,ln2)) return 2;
      return 0;*///线邻接于线，返回0表示不邻接，1表示邻接点为线1的起点，2表示为线1的终点
      //计算四种两点间距离，找最小值min，选最近的两点作为邻接点
      double qq=pnt_dist(ln1.qd,ln2.qd);
      double min=qq;
      double qz=pnt_dist(ln1.qd,ln2.zd);
      min=min<=qz?min:qz;
      double zq=pnt_dist(ln1.zd,ln2.qd);
      min=min<=zq?min:zq;
      double zz=pnt_dist(ln1.zd,ln2.zd);
      min=min<=zz?min:zz;
      if(min<ypxl){
        if(min==qq) return QQ;
        else if(min==qz) return QZ;
        else if(min==zq) return ZQ;
        else return ZZ;
      }
      else return 0;//不邻接
    }
    
    public static boolean paralell(Line ln1,Line ln2){//两线平行
      double vtor1x=ln1.zd.x-ln1.qd.x;
      double vtor1y=ln1.zd.y-ln1.qd.y;
      double vtor2x=ln2.zd.x-ln2.qd.x;
      double vtor2y=ln2.zd.y-ln2.qd.y;
      System.out.println("v1x:"+vtor1x+"\nv1y:"+vtor1y+"\nv2x:"+vtor2x+"\nv2y:"+vtor2y);
      System.out.println("abs:"+Math.abs(vtor1x*vtor2y-vtor2x*vtor1y));
      if(Math.abs(vtor1x*vtor2y-vtor2x*vtor1y)<(ypxl/10)){//平行：x1*y2=x2*y1   平行的这个近似量应该和点近似相等的近似量不同
        return true;
      }
      else return false;
    }
    
    public static Line concatenate(Line ln1,Line ln2){//将两线合二为一
      //if(paralell(ln1,ln2)==false) return null; 在调用这函数前判断相邻和平行即可
      /*
      if(aqlpnt(ln1.qd,ln2.qd)){
        return new Line(ln1.zd,ln2.zd);
      }
      else if(aqlpnt(ln1.qd,ln2.zd)) return new Line(ln2.qd,ln1.zd);
      else if(aqlpnt(ln1.zd,ln2.qd)) return new Line(ln1.qd,ln2.zd);
      else if(aqlpnt(ln1.zd,ln2.zd)) return new Line(ln1.qd,ln2.qd);
      else return null;*/
      //找到两个线最接近的两个点，然后合并。     上面的方法存在的问题是，有可能一个线的两个点都和另一个线邻接，这样的话，新构造的线就可能有问题。
      //Point p1=null,p2=null;
      Point p1=ln1.qd,p2=ln2.qd;
      double qq=pnt_dist(ln1.qd,ln2.qd);
      double qz=pnt_dist(ln1.qd,ln2.zd);
      double zq=pnt_dist(ln1.zd,ln2.qd);
      double zz=pnt_dist(ln1.zd,ln2.zd);
      if(qq<qz && qq<zq && qq<zz)      { p1=ln1.zd; p2=ln2.zd;}
      else if(qz<qq && qz<zq && qz<zz) { p1=ln1.zd; p2=ln2.qd;}
      else if(zq<qq && zq<qz && zq<zz) { p1=ln1.qd; p2=ln2.zd;}
      //else if(zz<qq && zz<qz && zz<zq) { p1=ln1.qd; p2=ln2.qd;}   假如有相等呢？
      else  { p1=ln1.qd; p2=ln2.qd;}//System.out.println(p1.x+" "+p1.y+" "+p2.x+" "+p2.y);
      return new Line(p1,p2);
    }
    
     public static void pre_process(List<Line> supset){//预处理，只针对线段
      //删除太短的线
      /*for(int i=0;i<supset.size();++i){
        if(supset.get(i).length()<ypxl-1)//太短的线
          supset.remove(i);  
      }*/         //删太短的线这个方法不是很好，这个阈值不好设定，可能有恰好大于这个阈值的呢？用ypxl(500+1)-1的话，走廊.dxf只识别两个，差一个大走廊
      
       //预处理规则2：去除冗余的线段（近似相等的线段）   自己加的，且平行关系   
      List<Integer> bh=new ArrayList();
      for(int i=0;i<supset.size();++i){
          Line ln1=supset.get(i);
          //求整个图纸的四个边界  之前在有的read..函数里写了一点，放在这里比较好，之前的还没删
          double minx=ln1.qd.x<ln1.zd.x?ln1.qd.x:ln1.zd.x;
          double maxx=ln1.qd.x>ln1.zd.x?ln1.qd.x:ln1.zd.x;
          double miny=ln1.qd.y<ln1.zd.y?ln1.qd.y:ln1.zd.y;
          double maxy=ln1.qd.y>ln1.zd.y?ln1.qd.y:ln1.zd.y;
          tminx=tminx<minx?tminx:minx;
          tmaxx=tmaxx>maxx?tmaxx:maxx;
          tminy=tminy<miny?tminy:miny;
          tmaxy=tmaxy>maxy?tmaxy:maxy;
          
          for(int j=i+1;j<supset.size();++j){
              Line ln2=supset.get(j);
              int res=aqlln(ln1,ln2);
              if(res!=0 && paralell(ln1,ln2)){//近似相等的线段   自己加的，且平行关系 
                /*System.out.println("去除近似相等的线段：");
                System.out.println("保留的线段,qdx:"+ln1.qd.x+" qdy:"+ln1.qd.y+" zdx:"+ln1.zd.x+" zdy:"+ln1.zd.y);
                System.out.println("去除的线段,qdx:"+ln2.qd.x+" qdy:"+ln2.qd.y+" zdx:"+ln2.zd.x+" zdy:"+ln2.zd.y);
                supset.remove(j);
                j--;  /* */
                //保留线段为下面的、左边的
                Point p1=ln1.qd,p2=ln2.zd;//近似相等的是哪两点，比较近似相等的点。     否则直接比较ln1.qd和ln2.qd的话，它们可能不近似相等
                //System.out.println("res:"+res);
                if(res==1){//起点和起点近似相等
                  p2=ln2.qd;
                }//else即为p1、p2初始值的情况，起点和终点近似相等
                if(Math.abs(p1.x-p2.x)<10){//认为x坐标相同
                  if(p1.y>p2.y){
                    /*System.out.println("去除近似相等的线段1：");
                    System.out.println("保留的线段,qdx:"+ln2.qd.x+" qdy:"+ln2.qd.y+" zdx:"+ln2.zd.x+" zdy:"+ln2.zd.y);
                    System.out.println("去除的线段,qdx:"+ln1.qd.x+" qdy:"+ln1.qd.y+" zdx:"+ln1.zd.x+" zdy:"+ln1.zd.y);
                    System.out.println("PLINE "+ln2.qd.x+","+ln2.qd.y+" "+ln2.zd.x+","+ln2.zd.y+" ");
                    System.out.println("PLINE "+ln1.qd.x+","+ln1.qd.y+" "+ln1.zd.x+","+ln1.zd.y+" ");*/
                    if(bh.contains(i)==false) bh.add(i);
                    //supset.remove(i);
                    //i--;
                    //break;
                  }
                  else{
                    /*System.out.println("去除近似相等的线段2：");
                    System.out.println("保留的线段,qdx:"+ln1.qd.x+" qdy:"+ln1.qd.y+" zdx:"+ln1.zd.x+" zdy:"+ln1.zd.y);
                    System.out.println("去除的线段,qdx:"+ln2.qd.x+" qdy:"+ln2.qd.y+" zdx:"+ln2.zd.x+" zdy:"+ln2.zd.y);
                    System.out.println("PLINE "+ln1.qd.x+","+ln1.qd.y+" "+ln1.zd.x+","+ln1.zd.y+" ");
                    System.out.println("PLINE "+ln2.qd.x+","+ln2.qd.y+" "+ln2.zd.x+","+ln2.zd.y+" "); */
                    //supset.remove(j);
                    //j--;
                    if(bh.contains(j)==false) bh.add(j);
                  }
                }
                else{//比较x坐标
                  if(p1.x>p2.x){
                    /*System.out.println("去除近似相等的线段3：");
                    System.out.println("保留的线段,qdx:"+ln2.qd.x+" qdy:"+ln2.qd.y+" zdx:"+ln2.zd.x+" zdy:"+ln2.zd.y);
                    System.out.println("去除的线段,qdx:"+ln1.qd.x+" qdy:"+ln1.qd.y+" zdx:"+ln1.zd.x+" zdy:"+ln1.zd.y);
                    System.out.println("PLINE "+ln2.qd.x+","+ln2.qd.y+" "+ln2.zd.x+","+ln2.zd.y+" ");
                    System.out.println("PLINE "+ln1.qd.x+","+ln1.qd.y+" "+ln1.zd.x+","+ln1.zd.y+" ");*/
                    //supset.remove(i);
                    //i--;
                    //break;
                    if(bh.contains(i)==false) bh.add(i);
                  }
                  else{
                    /*System.out.println("去除近似相等的线段4：");
                    System.out.println("保留的线段,qdx:"+ln1.qd.x+" qdy:"+ln1.qd.y+" zdx:"+ln1.zd.x+" zdy:"+ln1.zd.y);
                    System.out.println("去除的线段,qdx:"+ln2.qd.x+" qdy:"+ln2.qd.y+" zdx:"+ln2.zd.x+" zdy:"+ln2.zd.y);
                    System.out.println("PLINE "+ln1.qd.x+","+ln1.qd.y+" "+ln1.zd.x+","+ln1.zd.y+" ");
                    System.out.println("PLINE "+ln2.qd.x+","+ln2.qd.y+" "+ln2.zd.x+","+ln2.zd.y+" ");*/
                    //supset.remove(j);
                    //j--;
                    if(bh.contains(j)==false) bh.add(j);
                  }
                }
              }
          }
     }//应该先去重结束之后再处理其他规则
     //按编号排序后倒序删            因为像上面循环里注释掉的那样直接删，会出现有的线过早删除后，不能再屏蔽其他线
     Collections.sort(bh);
     for(int i=bh.size()-1;i>=0;--i){
       int k=bh.get(i);
       supset.remove(k);
     }
          
     for(int i=0;i<supset.size();++i){
          Line ln1=supset.get(i);
          for(int j=i+1;j<supset.size();++j){
              Line ln2=supset.get(j); 
              if(contns(ln2,ln1)){//预处理规则3：长线包含短线，？？？，短线被移除
                //TO-DO contns函数未实现
              }
              else if(paralell(ln1,ln2) ){//预处理规则4：相邻且平行的线，如果在邻接点处不与其他线有关系，则可合二为一
                //TO-DO 怎么判断邻接点的关系？是不是也应该考虑跟弧的关系？
               int t=lnadjln(ln1,ln2);//判断相邻
               Point p1=null,p2=null,adjp=null;
               if(t==0) continue;
               else if(t==1) { p1=ln1.zd; p2=ln2.zd; adjp=ln1.qd;}
               else if(t==2) { p1=ln1.zd; p2=ln2.qd; adjp=ln1.qd;}
               else if(t==3) { p1=ln1.qd; p2=ln2.zd; adjp=ln1.zd;}
               else { p1=ln1.qd; p2=ln2.qd; adjp=ln1.zd;}
               int k=0;
               for(;k<supset.size();++k){
                  if(k!=j && k!=i &&pntadjln(adjp,supset.get(k))){//增加判断邻接点处的线是否大于ypxl   // supset.get(k).length()>=ypxl &&
                    break;
                  }
                }
               if(k==supset.size()){
                 Line templn=new Line(p1,p2);
                 /*System.out.println("合并线段");
                 System.out.println("删线段1，qdx:"+ln1.qd.x+" qdy:"+ln1.qd.y+" zdx:"+ln1.zd.x+" zdy:"+ln1.zd.y);
                 System.out.println("PLINE "+ln1.qd.x+","+ln1.qd.y+" "+ln1.zd.x+","+ln1.zd.y+" ");
                 System.out.println("删线段2，qdx:"+ln2.qd.x+" qdy:"+ln2.qd.y+" zdx:"+ln2.zd.x+" zdy:"+ln2.zd.y);
                 System.out.println("PLINE "+ln2.qd.x+","+ln2.qd.y+" "+ln2.zd.x+","+ln2.zd.y+" ");
                 System.out.println("新线段，qdx:"+templn.qd.x+" qdy:"+templn.qd.y+" zdx:"+templn.zd.x+" zdy:"+templn.zd.y);
                 System.out.println("PLINE "+templn.qd.x+","+templn.qd.y+" "+templn.zd.x+","+templn.zd.y+" ");*/
                 supset.remove(j);
                 supset.remove(i);
                 i--;
                 supset.add(templn);
                 break;
               }
              }
          }
        }
     
     //自己加的，所有规则之后，如果一个线段的首尾端点还很近，则删了它
     /*for(int i=0;i<supset.size();++i){
       Line ln1=supset.get(i);
       if(pnt_dist(ln1.qd,ln1.zd)<250){
         supset.remove(i);
         i--;
       }
     }*/
    }
    
    public static void findAllSuccessors(List<Line> supset){
      for(int i=0;i<supset.size();++i){
        Map<Integer,Integer> qmap=new HashMap();
        Map<Integer,Integer> zmap=new HashMap();
        qsuccessors.add(qmap);
        zsuccessors.add(zmap);
      }
      for(int i=0;i<supset.size();++i){//遍历每条线
        Line ln1=supset.get(i);//Map<Integer,Integer> qdmap=new HashMap();Map<Integer,Integer> zdmap=new HashMap();
        for(int j=i+1;j<supset.size();++j){
          Line ln2=supset.get(j);
          int res=lnadjln(ln1,ln2);
          if(res==0) continue;//不相邻
          if(res==QQ){//起点起点相邻
            Map<Integer,Integer> q1map=qsuccessors.get(i);
            Map<Integer,Integer> q2map=qsuccessors.get(j);
            q1map.put(j, QIDIAN);
            q2map.put(i,QIDIAN);
          }
          else if(res==QZ){
            Map<Integer,Integer> q1map=qsuccessors.get(i);
            Map<Integer,Integer> z2map=zsuccessors.get(j);
            q1map.put(j, ZHONGDIAN);
            z2map.put(i, QIDIAN);
          }
          else if(res==ZQ){
            Map<Integer,Integer> z1map=zsuccessors.get(i);
            Map<Integer,Integer> q2map=qsuccessors.get(j);
            z1map.put(j, QIDIAN);
            q2map.put(i, ZHONGDIAN);
          }
          else if(res==ZZ){
            Map<Integer,Integer> z1map=zsuccessors.get(i);
            Map<Integer,Integer> z2map=zsuccessors.get(j);
            z1map.put(j, ZHONGDIAN);
            z2map.put(i, ZHONGDIAN);
          }
        }
      }
    }
     
    public static double huDu(Point a,Point c){//点c为圆心，点a相对于c的弧度。
      double r=Math.sqrt((a.x-c.x)*(a.x-c.x)+(a.y-c.y)*(a.y-c.y));//半径长
      double hd=0;
      if(a.x!=c.x || a.y!=c.y) hd=Math.acos((a.x-c.x)/r);//必须不是圆心，即r不会0时，才能除。
      if(a.y<c.y) hd=-1*hd;//若点a在圆心c下方，则度数为负的。
      return hd;
    }
    
    public static Solid readSolid(BufferedReader bfr,int moshi) throws IOException{
           //这里设定一个模式moshi,如果为0，则是readBlocks调用，为1则是readEntities调用   
           double x1=0,x2=0,y1=0,y2=0,z1=0,z2=0,x3=0,y3=0,z3=0,x4=0,y4=0,z4=0;
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
           
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 11")==false);  
           x2=Double.valueOf(s2).doubleValue();   
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 21")==false);
           y2=Double.valueOf(s2).doubleValue();
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 31")==false);
           z2=Double.valueOf(s2).doubleValue();
           
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 12")==false);  
           x3=Double.valueOf(s2).doubleValue();   
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 22")==false);
           y3=Double.valueOf(s2).doubleValue();
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 32")==false);
           z3=Double.valueOf(s2).doubleValue();
           
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 13")==false);
           x4=Double.valueOf(s2).doubleValue();   
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 23")==false);
           y4=Double.valueOf(s2).doubleValue();
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 33")==false);
           z4=Double.valueOf(s2).doubleValue();
           
           if(filter.contains(tc)) return null;
           Solid sld=new Solid(new Point(x1,y1),new Point(x2,y2),new Point(x3,y3),new Point(x4,y4));
           if(moshi==1){//readEntities调用，则要保存门或房间所在图层的图元
             if(mentc.contains(tc)){//mentuy.add(sld);
               Line lns[]=sld.sldtoline();
               int len=0;
               if(sld.san.x==sld.si.x && sld.san.y==sld.si.y){//判断第三点和第四点是否是同一点
                 len=3;
               }
               else len=4;
               for(int k=0;k<len;++k){
                //mentuy.add(lns[k]);
                doorlns.add(lns[k]);
               }
            }
            if(fangjtc.contains(tc)){//fangjtuy.add(sld);
              Line lns[]=sld.sldtoline();
              int len=0;
              if(sld.san.x==sld.si.x && sld.san.y==sld.si.y){//判断第三点和第四点是否是同一点
                len=3;
              }
              else len=4;
              for(int k=0;k<len;++k){
                //fangjtuy.add(lns[k]);
                roomlns.add(lns[k]);
              } 
            }
            if(walltc.contains(tc)){//墙图层
              Line lns[]=sld.sldtoline();
              int len=0;
              if(sld.san.x==sld.si.x && sld.san.y==sld.si.y){//判断第三点和第四点是否是同一点
                len=3;
              }
              else len=4;
              for(int k=0;k<len;++k){
                //fangjtuy.add(lns[k]);
                walltuy.add(lns[k]);
              }
            }
           }
           return sld;
    }
    
    public static Arc readArc(BufferedReader bfr,int moshi) throws IOException, InstantiationException, IllegalAccessException, SQLException{
           //这里设定一个模式moshi,如果为0，则是readBlocks调用，为1则是readEntities调用   
           double cx=0.0,cy=0.0,rad=0.0,qd=0.0,zd=0.0;
           String s1=null,s2=null;
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals("  8")==false);  //图层
           String tc=new String(s2);
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 10")==false); 
           cx=Double.parseDouble(s2);
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 20")==false); 
           cy=Double.parseDouble(s2);
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 40")==false); //半径
           rad=Double.parseDouble(s2);
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 50")==false); //起点弧度
           qd=Double.parseDouble(s2)/180*Math.PI;
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 51")==false); //终点弧度
           zd=Double.parseDouble(s2)/180*Math.PI;
          
           if(filter.contains(tc)) return null;
           
           Arc arc=new Arc(new Point(cx,cy),rad,qd,zd);
           if(moshi==1){//readEntities调用，则要保存门或房间所在图层的图元
             if(mentc.contains(tc)) doorarc.add(arc); //mentuy.add(arc);
             if(fangjtc.contains(tc)) roomarc.add(arc); //fangjtuy.add(arc);
             if(walltc.contains(tc)) walltuy.add(arc);
           }
           
           double minx=cx-rad;
           double maxx=cx+rad;
           double miny=cy-rad;
           double maxy=cy+rad;
           tminx=tminx<minx?tminx:minx;
           tmaxx=tmaxx>maxx?tmaxx:maxx;
           tminy=tminy<miny?tminy:miny;
           tmaxy=tmaxy>maxy?tmaxy:maxy;
           return arc;
    }
    
    public static Circle readCircle(BufferedReader bfr,int moshi) throws IOException, InstantiationException, IllegalAccessException, SQLException{
           //这里设定一个模式moshi,如果为0，则是readBlocks调用，为1则是readEntities调用   
           //圆是填充的，可以因此产生问题。可能可以用弧来代替圆，不知道弧的三点能不能有重合
           String s1=null,s2=null;
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals("  8")==false);  //图层
           String tc=new String(s2);
           double center_x=0.0,center_y=0.0,radius=0.0;
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 10")==false); //圆心
           center_x=Double.parseDouble(s2);
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 20")==false); 
           center_y=Double.parseDouble(s2);
           while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 40")==false); //抛弃z坐标的0值，读半径
           radius=Double.parseDouble(s2);
           
           if(filter.contains(tc)) return null;
           
           Circle ccle=new Circle(center_x,center_y,radius);
           if(moshi==1){//readEntities调用，则要保存门或房间所在图层的图元
             if(mentc.contains(tc)) ;//mentuy.add(ccle);  这里先没有存储圆
             if(fangjtc.contains(tc)) ;//fangjtuy.add(ccle);
             if(walltc.contains(tc)) walltuy.add(ccle);
           }
          
           double minx=center_x-radius;
           double maxx=center_x+radius;
           double miny=center_y-radius;
           double maxy=center_y+radius;
           tminx=tminx<minx?tminx:minx;
           tmaxx=tmaxx>maxx?tmaxx:maxx;
           tminy=tminy<miny?tminy:miny;
           tmaxy=tmaxy>maxy?tmaxy:maxy;
           
           return ccle;
    }
    
    public static Line readLine(BufferedReader bfr,int moshi) throws InstantiationException, IllegalAccessException, SQLException, IOException{
           //这里设定一个模式moshi,如果为0，则是readBlocks调用，为1则是readEntities调用
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
           
           if(filter.contains(tc)) return null;
           
           Line l1=new Line(x1,y1,x2,y2);
           
           if(moshi==1){//readEntities调用，则要保存门或房间所在图层的图元
             if(mentc.contains(tc)) doorlns.add(l1); //mentuy.add(l1);
             if(fangjtc.contains(tc)) roomlns.add(l1); //fangjtuy.add(l1);
             if(walltc.contains(tc)) walltuy.add(l1);
             if(strtc.contains(tc)) stairlns.add(l1);
           }
           
           double minx=x1<x2?x1:x2;
           double maxx=x1>x2?x1:x2;
           double miny=y1<y2?y1:y2;
           double maxy=y1>y2?y1:y2;
           tminx=tminx<minx?tminx:minx;
           tmaxx=tmaxx>maxx?tmaxx:maxx;
           tminy=tminy<miny?tminy:miny;
           tmaxy=tmaxy>maxy?tmaxy:maxy;/**/
           return l1;
    }
    
    public static LWpolyline readLWpolyline(BufferedReader bfr,int moshi) throws IOException, InstantiationException, IllegalAccessException, SQLException{
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
             
             double minx=tminx;
             double maxx=tmaxx;
             double miny=tminy;
             double maxy=tmaxy;/**/
             
             for(int i=0;i<(cls==1?num-1:num);++i){//中间的表达式，是让i小于原始的num
              while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 10")==false);  //坐标
              x=Double.parseDouble(s2);
              while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 20")==false);  //坐标
              y=Double.parseDouble(s2);
              ordinates[i*2]=x;
              ordinates[i*2+1]=y;
              minx=minx<x?minx:x;
              maxx=maxx>x?maxx:x;
              miny=miny<y?miny:y;
              maxy=maxy>y?maxy:y;/**/
             }
             if(cls==1) { ordinates[2*num-2]=ordinates[0]; ordinates[2*num-1]=ordinates[1];}//重复第一点
             if(filter.contains(tc)) return null;
             LWpolyline lwln=new LWpolyline(num,ordinates);
             if(moshi==1){//readEntities调用，则要保存门或房间所在图层的图元
             if(mentc.contains(tc)){//mentuy.add(lwln);
               //这里不是直接存多段线图元了，而是存等价的线段图元集合
               int len=lwln.plen-1;//plen个点构成plen-1个线段（闭合的时候点数是加1的，所以这句始终正确）
               Line lns[]=lwln.lwlntoline();
               for(int i=0;i<len;++i){
                //mentuy.add(lns[i]);
                doorlns.add(lns[i]);
               }
             }
             if(fangjtc.contains(tc)){//fangjtuy.add(lwln);
               int len=lwln.plen-1;
               Line lns[]=lwln.lwlntoline();
               for(int i=0;i<len;++i){
                //fangjtuy.add(lns[i]);
                roomlns.add(lns[i]);
               }
             }
             if(walltc.contains(tc)){
               int len=lwln.plen-1;
               Line lns[]=lwln.lwlntoline();
               for(int i=0;i<len;++i){
                //fangjtuy.add(lns[i]);
                walltuy.add(lns[i]);
               }
             }
             }
             
           tminx=tminx<minx?tminx:minx;
           tmaxx=tmaxx>maxx?tmaxx:maxx;
           tminy=tminy<miny?tminy:miny;
           tmaxy=tmaxy>maxy?tmaxy:maxy;/**/
           return lwln;
    }
    
    public static int find(String name){
      int i=0;
      int flag=0;//是否找到
      for(;i<kname.size();++i){
       if(kname.get(i).equals(name)) { flag=1; break;}
      }
      if(flag==1) return i;
      else return -1;
    }
    
    public static <T> List<T> deepCopy(List<T> src) throws IOException, ClassNotFoundException {  
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();  
    ObjectOutputStream out = new ObjectOutputStream(byteOut);  
    out.writeObject(src);  
    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());  
    ObjectInputStream in = new ObjectInputStream(byteIn);  
    @SuppressWarnings("unchecked")  
    List<T> dest = (List<T>) in.readObject();  
    return dest;  
    }   
    
    public static void readInsert(BufferedReader bfr,int moshi) throws IOException, InstantiationException, IllegalAccessException, SQLException, ClassNotFoundException{
      //这里设定一个模式moshi,如果为0，则是readBlocks调用，为1则是readEntities调用
      String s1=null,s2=null;
      while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals("  8")==false);  //图层
      String tc=new String(s2);
      while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals("  2")==false);  //块名称
      String name=s2;
      while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 10")==false);  //插入点x值
      double cx=Double.parseDouble(s2);
      while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 20")==false);  //插入点y值
      double cy=Double.parseDouble(s2);
      while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null && s1.equals(" 30")==false);  //插入点z值
      double cz=Double.parseDouble(s2);
      //System.out.println("hehehe");
      double sx=1,sy=1,sz=1,xz=0;//xyz的缩放比例、旋转角度（弧度）
      bfr.mark(100);//这里也需要mark下，不然组码30后直接结束跟着是下一图元的组码0，这样就回退到上一次这个函数里while循环mark的地方了。。    ——调了好长时间，看着几万行的txt啊。。才知道什么是bug。。
      while((s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null){
        if(s1.equals(" 41"))  sx =Double.parseDouble(s2);
        else if(s1.equals(" 42")) sy=Double.parseDouble(s2);
        else if(s1.equals(" 43")) sz=Double.parseDouble(s2);
        else if(s1.equals(" 50")) xz=Math.toRadians(Double.parseDouble(s2)); //xz=Double.parseDouble(s2)/180*Math.PI;//shift函数利用的是cos函数，为弧度，所以这里转换为弧度
        else if(s1.equals("  0")){//这里回退的判断条件只是下一个图元开始或块结尾，即组码为0
          bfr.reset();//回退到上一次mark的位置，即回退两行。回退的原因是可能这些可选属性均没有，然后读到了下一个图元的开头或者块的结尾。
          break;
        }
        else break;//这里没有处理70、71及拉伸方向等的情况。   
        bfr.mark(100);//标记输入流当前位置。参数为最大读入数据，足够大即可，这里随便选的100
      }
      
      if(filter.contains(tc)) return ;//该图层在要过滤的图层名中，则直接结束，不再存储。
      //System.out.print("kname:"); System.out.println(name);
      int indx=find(name); //System.out.print("indx:"); System.out.println(indx);
      if(indx<0) return ;
      //if(name.compareTo("$DorLib2D$00000001")!=0) return;//暂时先处理门1这一种情况。
      List tlst=new ArrayList(lst.get(indx));//List tlst=deepCopy(lst.get(indx));
      List JGlst=new ArrayList();//把这一参照块的图元集合起来
      List<Arc> AClst=new ArrayList();//存块定义中的弧
      
      for(int i=0;i<tlst.size();++i){//遍历lst的indx位置元素
   /**/if(tlst.get(i).getClass()==Line.class){//直线
         Line templ=new Line((Line)tlst.get(i));//先是对List的元素进行强制类型转换，然后再创建一个备份
         templ.qd.shift(sx, sy, xz,cx,cy);
         templ.zd.shift(sx, sy, xz,cx,cy);
         templ.whzb();                                                           //吐槽：我要死了，为什么下面的几个都知道维护坐标，这里忘了。。。
         if(moshi==1){//readEntities调用，则要保存门或房间所在图层的图元
             //if(mentc.contains(tc)) doorlns.add(templ); //mentuy.add(templ);
             if((mentc.contains(tc)==false) && fangjtc.contains(tc)) roomlns.add(templ); //fangjtuy.add(templ);
             //if(walltc.contains(tc)) walltuy.add(templ);
         }
         JGlst.add(templ);
         //存入Oracle
         JGeometry geo=JGeometry.createLinearLineString(templ.zb,2,zbsrid);
         connect_database(geo);
       }
       else if(tlst.get(i).getClass()==LWpolyline.class){//多段线
         LWpolyline templw=new LWpolyline((LWpolyline)tlst.get(i));
         for(int j=0;j<templw.plen;++j){
          templw.num[j].shift(sx, sy, xz,cx,cy);
         }
         templw.whzb();
         if(moshi==1){//readEntities调用，则要保存门或房间所在图层的图元
             /*if(mentc.contains(tc)){//mentuy.add(templw);
               int len=templw.plen-1;
               Line lns[]=templw.lwlntoline();
               for(int k=0;k<len;++k){
                //mentuy.add(lns[k]);
                doorlns.add(lns[k]);
               }
             }*/
             if((mentc.contains(tc)==false) && fangjtc.contains(tc)){//fangjtuy.add(templw);
               int len=templw.plen-1;
               Line lns[]=templw.lwlntoline();
               for(int k=0;k<len;++k){
                //fangjtuy.add(lns[k]);
                roomlns.add(lns[k]);
               }
             }
             /*if(walltc.contains(tc)){
               int len=templw.plen-1;
               Line lns[]=templw.lwlntoline();
               for(int k=0;k<len;++k){
                //fangjtuy.add(lns[k]);
                walltuy.add(lns[k]);
               }
             }*/
         }
         JGlst.add(templw);
         //存入Oracle
          JGeometry geo=null;//if(templw.close==1) geo=JGeometry.createLinearPolygon(templw.zb, 2, zbsrid);//不能用这个函数创建多边形，因为多边形内部是填充的
          geo=JGeometry.createLinearLineString(templw.zb,2,zbsrid);
          connect_database(geo);
       }
       else if(tlst.get(i).getClass()==Arc.class){//弧
         Arc tempac=new Arc((Arc)tlst.get(i));
         tempac.qd.shift(sx, sy, xz,cx,cy);
         tempac.td.shift(sx, sy, xz,cx,cy);
         tempac.zd.shift(sx, sy, xz,cx,cy);
         tempac.center.shift(sx, sy, xz, cx, cy);
         tempac.wh();
         if(moshi==1){//readEntities调用，则要保存门或房间所在图层的图元
             //if(mentc.contains(tc)) doorarc.add(tempac); //mentuy.add(tempac);
             if((mentc.contains(tc)==false) && fangjtc.contains(tc)) roomarc.add(tempac);//若该块在房间图层而且不在门图层，才加入到房间集合里。
             //if(walltc.contains(tc)) walltuy.add(tempac);
         }
         JGlst.add(tempac);
         //存入Oracle
          JGeometry geo=JGeometry.createArc2d(tempac.zb, 2, zbsrid);
          //System.out.print("插入点："); System.out.println(cx);System.out.println(cy);
          //System.out.print("Arc:"); System.out.println(tempac.qd.x);System.out.println(tempac.qd.y);System.out.println(tempac.td.x);System.out.println(tempac.td.y);System.out.println(tempac.zd.x);System.out.println(tempac.zd.y);
          connect_database(geo);
          AClst.add(tempac);
       }/**//**/
       else if(tlst.get(i).getClass()==Circle.class){//圆  //这里关于半径扩大的倍数可能有问题，也许用圆上三点来变换为会好些。
         Circle tempccl=new Circle((Circle)tlst.get(i));
         tempccl.center.shift(sx, sy, xz,cx,cy);
         tempccl.radius=tempccl.radius*sx;//横纵坐标都扩大sx倍，半径扩大sx倍。   半径扩大的倍数既不是不变，也不是横纵坐标扩大的倍数之积
         if(moshi==1){//readEntities调用，则要保存门或房间所在图层的图元
             //if(mentc.contains(tc)) ;//mentuy.add(tempccl);这里先没有保存圆
             if((mentc.contains(tc)==false) && fangjtc.contains(tc)) ;//fangjtuy.add(tempccl);
             //if(walltc.contains(tc)) walltuy.add(tempccl);
         }
         JGlst.add(tempccl);
         //存入Oracle
          JGeometry geo=JGeometry.createCircle(tempccl.center.x,tempccl.center.y,tempccl.radius,zbsrid);
          //System.out.println("Circle:"); System.out.println(tempccl.center.x);  System.out.println(tempccl.center.y); System.out.println(tempccl.radius);
          connect_database(geo);
       }
       else if(tlst.get(i).getClass()==Solid.class){
         Solid tempsld=new Solid((Solid)tlst.get(i));
         tempsld.yi.shift(sx, sy, xz, cx, cy);
         tempsld.er.shift(sx, sy, xz, cx, cy);
         tempsld.san.shift(sx, sy, xz, cx, cy);
         tempsld.si.shift(sx, sy, xz, cx, cy);
         tempsld.whzb();
         if(moshi==1){//readEntities调用，则要保存门或房间所在图层的图元
           /*if(mentc.contains(tc)){//mentuy.add(sld);
               Line lns[]=tempsld.sldtoline();
               int len=0;
               if(tempsld.san.x==tempsld.si.x && tempsld.san.y==tempsld.si.y){//判断第三点和第四点是否是同一点
                 len=3;
               }
               else len=4;
               for(int k=0;k<len;++k){
                //mentuy.add(lns[k]);
                doorlns.add(lns[k]);
               }
            }*/
            if((mentc.contains(tc)==false) && fangjtc.contains(tc)){//fangjtuy.add(sld);
              Line lns[]=tempsld.sldtoline();
              int len=0;
              if(tempsld.san.x==tempsld.si.x && tempsld.san.y==tempsld.si.y){//判断第三点和第四点是否是同一点
                len=3;
              }
              else len=4;
              for(int k=0;k<len;++k){
                //fangjtuy.add(lns[k]);
                roomlns.add(lns[k]);
              } 
            }
            /*if(walltc.contains(tc)){
              Line lns[]=tempsld.sldtoline();
              int len=0;
              if(tempsld.san.x==tempsld.si.x && tempsld.san.y==tempsld.si.y){//判断第三点和第四点是否是同一点
                len=3;
              }
              else len=4;
              for(int k=0;k<len;++k){
                //fangjtuy.add(lns[k]);
                walltuy.add(lns[k]);
              }
            }*/
         }
         JGlst.add(tempsld);
         JGeometry geo=JGeometry.createLinearLineString(tempsld.zb,2,zbsrid);
         connect_database(geo);
       }
      }//end of 遍历for
      
      if(moshi==1){
        if(mentc.contains(tc)) blockty.add(JGlst);
      }
      //return JGlst;
    }
    
    public static void readEntities(BufferedReader bfr) throws IOException, InstantiationException, IllegalAccessException, SQLException, ClassNotFoundException{
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
          if(s2.equals("LINE")){//直线
            Line ln=readLine(bfr,READENTITIES);
            if(ln!=null){
            JGeometry geo=JGeometry.createLinearLineString(ln.zb,2,zbsrid);
            connect_database(geo);
            //allty.add(geo);
            }
          }
          else if(s2.equals("LWPOLYLINE")){//多段线
            LWpolyline lwln=readLWpolyline(bfr,READENTITIES);
            if(lwln!=null){
            JGeometry geo=JGeometry.createLinearLineString(lwln.zb,2,zbsrid);
            connect_database(geo);
            //allty.add(geo);
            }
          }
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
          }/**/
          else if(s2.equals("INSERT")){//参照块图元
            readInsert(bfr,READENTITIES);
             /*List<JGeometry> Jlst=readInsert(bfr);
            for(int i=0;i<Jlst.size();++i){
             connect_database(Jlst.get(i));
            }*/
          }
          else if(s2.equals("SOLID")){
            Solid sld=readSolid(bfr,READENTITIES);
            if(sld!=null){
            JGeometry geo=JGeometry.createLinearLineString(sld.zb,2,zbsrid);
            connect_database(geo);
            }
          }
         }//if-s1
       }
    }
     
    public static void readBlocks(BufferedReader bfr) throws IOException, InstantiationException, IllegalAccessException, SQLException{
      String s1=null,s2=null;
      int flag=0;//指示所读是否为BLOCKS段，1为是，0为否。
      while((s1 = bfr.readLine())!=null && (s2 = bfr.readLine())!=null){
        if(s1.equals("  0") && s2.equals("SECTION")){
            s1=bfr.readLine();
            s2=bfr.readLine();
            //System.out.println("s1:"+s1+"    s2:"+s2);
            if(s1.equals("  2") && s2.equals("BLOCKS")) { flag=1; System.out.println("begin in Blocks");continue; }   //开始读BLOCKS段
         }
        if(flag==0) continue;    //所读内容不是BLOCKS段
        if(flag==1 && s1.equals("  0") && s2.equals("ENDSEC")) { flag=0; System.out.println("end of Blocks"); break; }  //读完BLOCKS段
        if(s1.equals("  0") && s2.equals("BLOCK")){//每个块条目
           int kflg=0;//块条目是否读完
           List tl=new ArrayList();
           while(s1.compareTo("  2")!=0) { s1=bfr.readLine(); s2=bfr.readLine(); }
           kname.add(s2);//加入块名称
           while(kflg==0 && (s1=bfr.readLine())!=null && (s2=bfr.readLine())!=null){
             if(s1.equals("100") && s2.equals("AcDbBlockEnd")){//该块条目读完
               kflg=1;
               lst.add(tl);
             }
             if(s1.equals("  0")){
               if(s2.equals("LINE")){
                 Line ln=readLine(bfr,READBLOCKS);
                 if(ln!=null) tl.add(ln);
               }
               else if(s2.equals("LWPOLYLINE")){
                 LWpolyline lwln=readLWpolyline(bfr,READBLOCKS);
                 if(lwln!=null) tl.add(lwln);
               }
               else if(s2.equals("ARC")){
                 Arc arc=readArc(bfr,READBLOCKS);
                 if(arc!=null) tl.add(arc);
               }
               else if(s2.equals("CIRCLE")){
                 Circle ccle=readCircle(bfr,READBLOCKS);
                 if(ccle!=null) tl.add(ccle);
               }
               else if(s2.equals("INSERT")){//嵌套块定义
                 
               }
               else if(s2.equals("SOLID")){
                 Solid sld=readSolid(bfr,READBLOCKS);
                 if(bfr!=null) tl.add(sld);
               }
             }
           }
        }
      }
    }
    
    public static void createindex() throws InstantiationException, IllegalAccessException, SQLException{
           //建立数据库连接   
           String Driver="oracle.jdbc.driver.OracleDriver";    //连接数据库的方法    
           String URL="jdbc:oracle:thin:@127.0.0.1:1521:indoor";    //indoor为数据库的SID    
           String Username="indooradmin";    //用户名               
           String Password="indoor";    //密码    
           //String tableName="cad";
           String colName="geom";
           try {
                   Class.forName(Driver).newInstance();    //加载数据库驱动
                   Connection con=DriverManager.getConnection(URL,Username,Password);  
                   if(!con.isClosed())
                       System.out.println("Succeeded connecting to the Database!");
                   Statement stmt=con.createStatement();
                   
                   String sql="delete from user_sdo_geom_metadata";
                   System.out.println("Executing query:'"+sql+"'");
                   //PreparedStatement stmt=con.prepareStatement(sqlInsert);
                   stmt.executeUpdate(sql);
                   
                   sql="insert into user_sdo_geom_metadata values('"+tableName+"','"+colName+"',sdo_dim_array(sdo_dim_element('x',0,10000,0.5),sdo_dim_element('y',0,10000,0.5)),32774)";
                   System.out.println("Executing query:'"+sql+"'");
                   stmt.executeUpdate(sql);
                   
                   sql="delete from user_sdo_index_metadata";
                   System.out.println("Executing query:'"+sql+"'");
                   stmt.executeUpdate(sql);
                   
                   sql="drop index cad_index";
                   System.out.println("Executing query:'"+sql+"'");
                   stmt.executeUpdate(sql);
                   
                   sql="create index cad_index on cad(geom) indextype is mdsys.spatial_index";
                   System.out.println("Executing query:'"+sql+"'");
                   stmt.executeUpdate(sql);
                   
                   stmt.close();
                   con.close();  
               } catch (ClassNotFoundException ex) {
                   Logger.getLogger(Readdxfmy2.class.getName()).log(Level.SEVERE, null, ex);
               }
    }
    
    public static void printPoint(String s,Point c){//用于调试。以CAD命令方式打印点
      System.out.println(s+" POINT "+c.x+","+c.y);
    }
    
    public static void printLine(String s,Line ln){//用于调试。以CAD命令方式打印线，起点终点顺序
      System.out.println(s+" PLINE "+ln.qd.x+","+ln.qd.y+" "+ln.zd.x+","+ln.zd.y);
    }
    
    public static boolean find_doorsteps(List<Line> lnlst,List<Arc> aclst,double dmin,double dmax,double hd){
    //以线集合lnlst和弧集合aclst，以及其他三个约束来找门，如果找到返回真。
      boolean flag=false;
      for(int i=0;i<aclst.size();++i){
        Arc ac=aclst.get(i);
        /*if(Math.abs(ac.center.x-33671)<10){
          System.out.println("arc-qd-zd: PLINE "+ac.qd.x+","+ac.qd.y+" "+ac.zd.x+","+ac.zd.y);
          System.out.println("center:"+ac.center.x+","+ac.center.y);
        }*/
        //sort out arcs that do not fit
        if(ac.radius<=dmin || ac.radius>=dmax) continue;
        if(Math.abs(ac.zhd-ac.qhd)<=Math.PI-hd || Math.abs(ac.zhd-ac.qhd)>=Math.PI+hd) continue;
        
        List<Line> doorCandidates=new ArrayList();//门槛候选集
        for(int j=0;j<lnlst.size();++j){
          Line ln=lnlst.get(j);
          //sort out lines that are too short or too long
          if(ln.length()<=dmin || ln.length()>=dmax) continue;
          //sort out lines not adj to arcs center
          if(pntadjln(ac.center,ln)==false) continue;
          //if a line from arcs center to one end point is found, the opposing line is a candidate for closed door
          if(pntadjln(ac.zd,ln)){
            printPoint("zd:",ac.zd);
            printLine("ln:",ln);
            Line nwln=new Line(ac.qd,ac.center);
            doorCandidates.add(nwln);
            break;//跳出循环   ！！！修改
            //if(Math.abs(ac.center.x-33669)<1){
            //  System.out.println("centery:"+ac.center.y+" qd,center");
            //}
          }
          else if(pntadjln(ac.qd,ln)){
            printPoint("qd:",ac.qd);
            printLine("ln:",ln);
            Line nwln=new Line(ac.zd,ac.center);
            doorCandidates.add(nwln);
            break;//跳出循环   ！！！修改
            //if(Math.abs(ac.center.x-33669)<1){
            //  System.out.println("centery:"+ac.center.y+" zd,center");
            //}
          }
          else{
            //if(Math.abs(ac.center.x-33669)<1){
            //  System.out.println("no pnt adj ln.");
            //}
          }
        }//for
        
        if(doorCandidates.size()==1){
          doorsteps.add(doorCandidates.get(0));
          flag=true;
        }
        else if(doorCandidates.size()>1){
          System.out.println("Candidates > 1");
          for(int j=0;j<doorCandidates.size();++j){
            Line cand=doorCandidates.get(j);
            boolean valid=false;
            for(int k=0;k<walltuy.size();++k){
              if(walltuy.get(k).getClass()==Line.class){
                Line templn=(Line)walltuy.get(k);
                if(contns(templn,cand)) { valid=true; break;}    //contns函数暂时还未实现
              }
            }//fork
            if(valid==false){
              doorCandidates.remove(j);
              j--;
            }
          }//forj
        if(doorCandidates.size()>1){
          //问题弧
        }
        else if(doorCandidates.size()==1){//自己加的
          doorsteps.add(doorCandidates.get(0));
          flag=true;
        }
       }//elseif
      }//fori
      return flag;
    }
    
    public static void Extract_Doors(double dmin,double dmax,double hd){//门槛的最小和最大值，门弧度相对于PI/2的可以偏差的弧度值
      //pre_process(doorlns);
      //TO-DO 前两步暂时没写
      //List<Line> doorsteps=new ArrayList();//门槛
        
      //处理非块图元
      find_doorsteps(doorlns,doorarc,dmin,dmax,hd);
      
      //处理块图元
      for(int i=0;i<blockty.size();++i){
        List<Line> lnlst=new ArrayList();//收集当前图元的线段，看之后是否加入到doorlns中
        List<Arc> aclst=new ArrayList();
        List lst=blockty.get(i);
        for(int j=0;j<lst.size();++j){
          if(lst.get(j).getClass()==Line.class){
            Line templn=(Line)lst.get(j);
            lnlst.add(templn);
          }
          else if(lst.get(j).getClass()==LWpolyline.class){
            LWpolyline templw=(LWpolyline)lst.get(j);
            int len=templw.plen-1;
            Line lns[]=templw.lwlntoline();
            for(int k=0;k<len;++k){
              lnlst.add(lns[k]);
            }
          }
          else if(lst.get(j).getClass()==Arc.class){
             Arc tempac=(Arc)lst.get(j);
             aclst.add(tempac);
          }
          else if(lst.get(j).getClass()==Solid.class){
            Solid tempsld=(Solid)lst.get(j);
            Line lns[]=tempsld.sldtoline();
              int len=0;
              if(tempsld.san.x==tempsld.si.x && tempsld.san.y==tempsld.si.y){//判断第三点和第四点是否是同一点
                len=3;
              }
              else len=4;
              for(int k=0;k<len;++k){
                //fangjtuy.add(lns[k]);
                lnlst.add(lns[k]);
              } 
          }
        }
        //对每一个块图元找门
        if(find_doorsteps(lnlst,aclst,dmin,dmax,hd)){
          //找到了门，则其余的线舍弃
        }
        else{//没有门，则可能是窗之类的构成墙体，所以加入到doorlns中，后面会用于构成房间
          doorlns.addAll(lnlst);
        }
      }
      
      //自己加的，处理双弧门
      for(int i=0;i<doorsteps.size();++i){
        for(int j=i+1;j<doorsteps.size();++j){
          Line ln1=doorsteps.get(i);
          Line ln2=doorsteps.get(j);
          double qq=pnt_dist(ln1.qd,ln2.qd);
          double zz=pnt_dist(ln1.zd,ln2.zd);
          double qz=pnt_dist(ln1.qd,ln2.zd);
          double zq=pnt_dist(ln1.zd,ln2.qd);
          if(((qq<ypxl)&&(zz<ypxl))||((qz<ypxl)&&(zq<ypxl))){//两线相同   比如两个门重合，则有相同的门槛。这样的话直接用下面的来合并就会出错。。
            doorsteps.remove(j);
            j--;
            continue;
          }
          if(lnadjln(ln1,ln2)!=0 && paralell(ln1,ln2)){
            //System.out.println(" 1qdx:"+ln1.qd.x+" 1qdy:"+ln1.qd.y);
            //System.out.println(" 1zdx:"+ln1.zd.x+" 1zdy:"+ln1.zd.y);
            //System.out.println(" 2qdx:"+ln2.qd.x+" 2qdy:"+ln2.qd.y);
            //System.out.println(" 2zdx:"+ln2.zd.x+" 2zdy:"+ln2.zd.y);
            Line nwln=concatenate(ln1,ln2);
            doorsteps.remove(j);
            doorsteps.remove(i);//应该先remove(j)，不会影响到i
            i--;
            doorsteps.add(nwln);
            break;
          }
        }
      }
      
      System.out.print("门的个数：");
      System.out.println(doorsteps.size());
      for(int i=0;i<doorsteps.size();++i){
        Line ln=doorsteps.get(i);
        //System.out.print("门 "+i);
        System.out.println("PLINE "+ln.qd.x+","+ln.qd.y+" "+ln.zd.x+","+ln.zd.y+" ");
      }/**/
    }
    
    public static void printfj(List<Integer> fj,List<Integer> bz){
      System.out.print("PLINE ");
      for(int i=0;i<fj.size();++i){
        Line ln=roomlns.get(fj.get(i));//!!!是get(fj.get(i))不是get(i)
        int b=bz.get(i);
        Point qd=ln.qd;
        Point zd=ln.zd;
        if(b==ZHONGDIAN){
          qd=ln.zd;
          zd=ln.qd;
        }
        System.out.print(qd.x+","+qd.y+" "+zd.x+","+zd.y+" ");
        if((i+1)%7==0) { System.out.println(); System.out.print(" ");} 
      }
      System.out.println();
    }
    
    public static void printscc(int sy,int bz){
      Line ln=roomlns.get(sy);  
      System.out.println("PLINE "+ln.qd.x+","+ln.qd.y+" "+ln.zd.x+","+ln.zd.y+" ");
      System.out.println("后继线标志："+bz);
    }
    
    public static void printqueue(Queue<List<Integer>> queue,Queue<List<Integer>> quebz){
      int len=queue.size();
      for(int i=0;i<len;++i){
        List<Integer> quehd=queue.remove();
        List<Integer> hdbz=quebz.remove();
        printfj(quehd,hdbz);
        queue.add(quehd);
        quebz.add(hdbz);
      }
    }
    
    public static void store_predxf(Double pylen) throws InstantiationException, IllegalAccessException, SQLException{
      for(int i=0;i<roomlns.size();++i){
        Line ln=roomlns.get(i);
        double tempzb[]=new double[4];//尼玛，数组名后的[]里不能有数组长度
        tempzb[0]=ln.zb[0];
        tempzb[1]=ln.zb[1]-pylen;//y坐标向下偏移一个pylen长度
        tempzb[2]=ln.zb[2];
        tempzb[3]=ln.zb[3]-pylen;
        JGeometry geo=JGeometry.createLinearLineString(tempzb,2,zbsrid);
        store("predxf",geo,i);
      }
    }
    
    public static void my_preprocess(List<Line> supset,List<List<Line>> lns,int moshi) throws IOException{
      /*List<List<List<Line>>> djlns=new ArrayList();//将线段集合按斜率等价划分后的等价类集合，其中每个相同的斜率内再按距离聚类
      List<Double> djsy=new ArrayList();//djlns等价类集合中每个等价类的斜率。索引上下对应。索引0位置存的是竖直线集合，斜率存10000
      djsy.add(new Double(10000));
      List<List<Line>> linglst=new ArrayList();
      djlns.add(linglst);//djlns索引0处是竖直线等价类
      for(int i=0;i<supset.size();++i){
        Line ln=supset.get(i);
        if(Math.abs(ln.qd.x-ln.zd.x)<0.001){
          if(djlns.get(0)==null){//竖直线放在索引0处
            List<List<Line>> templst=new ArrayList();
            List<Line> tlst=new ArrayList();
            tlst.add(ln);
            templst.add(tlst);
            djlns.add(templst);
          }
          else{
            List<List<Line>> templst=djlns.get(0);
            boolean flag=false;//是否与一小类中的线段足够接近
            for(int j=0;j<templst.size();++j){//遍历同一斜率中的小类
              List<Line> tlst=templst.get(j);
              if(near_line(tlst,ln,jsjl)){
                tlst.add(ln);
                flag=true;
                break;
              }
            }//for
            if(flag==false){//说明，新线和同斜率的小类都不足够接近，则新加一个以该线为基础的小类
              List<Line> tlst=new ArrayList();
              tlst.add(ln);
              templst.add(tlst);//增加一个新的小类
            }
          }
        }
        else{//非竖直线
          double lnxl=ln.getxl();
          int lnsy=xl_find(djsy,lnxl);
          if(lnsy==djsy.size()){//没找到该斜率
            djsy.add(lnxl);
            List<List<Line>> templst=new ArrayList();
            List<Line> tlst=new ArrayList();
            tlst.add(ln);
            templst.add(tlst);
            djlns.add(templst);
          }
          else{
            List<List<Line>> templst=djlns.get(lnsy);
            boolean flag=false;//是否与一小类中的线段足够接近
            for(int j=0;j<templst.size();++j){//遍历同一斜率中的小类
              List<Line> tlst=templst.get(j);
              if(near_line(tlst,ln,jsjl)){//找到足够接近的小类
                tlst.add(ln);
                flag=true;
                break;
              }
            }//for
            if(flag==false){//说明，新线和同斜率的小类都不足够接近，则新加一个以该线为基础的小类
              List<Line> tlst=new ArrayList();
              tlst.add(ln);
              templst.add(tlst);//增加一个新的小类
            }
          }//else
        }
      }//for
      //对同一斜率的小类再进行合并
      for(int i=0;i<djlns.size();++i){
        List<List<Line>> templst=djlns.get(i);
        for(int j=0;j<templst.size();++j){//每个小类
          for(int k=j+1;k<templst.size();++k){
            
          }  
        }
      }*/
      
      List<List<Line>> djlns=new ArrayList();//将线段集合按斜率等价划分后的等价类集合
      List<Double> djsy=new ArrayList();//djlns等价类集合中每个等价类的斜率。索引上下对应。索引0位置存的是竖直线集合，斜率存10000
      djsy.add(new Double(10000));
      djsy.add(new Double(0));
      List<Line> linglst=new ArrayList();
      List<Line> yilst=new ArrayList();
      djlns.add(linglst);//djlns索引0处是竖直线等价类
      djlns.add(yilst);//djlns索引1处是水平线等价类
      for(int i=0;i<supset.size();++i){
        Line ln=supset.get(i);
        //System.out.println(i+" qdx:"+ln.qd.x+" qdy:"+ln.qd.y);
        //System.out.println("  zdx:"+ln.zd.x+" zdy:"+ln.zd.y);
        //System.out.println("PLINE "+ln.qd.x+","+ln.qd.y+" "+ln.zd.x+","+ln.zd.y+" ");
        if(ln.is_vertical()){
          //if(djlns.get(0)==null){//竖直线放在索引0处
          //  List<Line> templst=new ArrayList();
         //   templst.add(ln);
         //   djlns.add(templst);
         // }
         // else{
            List<Line> templst=djlns.get(0);
            templst.add(ln);
         // }
        }
        else if(ln.is_horizontal()){
            //水平线放在索引1处
            List<Line> templst=djlns.get(1);
            templst.add(ln);
        }
        else{//非竖直非水平线
          double lnxl=ln.getxl();
          int lnsy=xl_find(djsy,lnxl);
          if(lnsy==djsy.size()){//没找到该斜率
            djsy.add(lnxl);
            List<Line> templst=new ArrayList();
            templst.add(ln);
            djlns.add(templst);
          }
          else{
            List<Line> templst=djlns.get(lnsy);
            templst.add(ln);
          }//else
        }
      }//for
      
      /*//计算每个小类中每条线段的邻近线
      List<List<List<Line>>> ljx=new ArrayList();
      for(int i=0;i<djlns.size();++i){
        List<Line> ttlst=djlns.get(i);
        List<List<Line>> templst=new ArrayList();
        for(int j=0;j<ttlst.size();++j){
          List<Line> tlst=new ArrayList();
          templst.add(tlst);
        }
        ljx.add(templst);
      }
      for(int i=0;i<djlns.size();++i){//遍历每个斜率
        List<Line> templst=djlns.get(i);
        for(int j=0;j<templst.size();++j){//遍历每个小类
          Line ln1=templst.get(j);
          for(int k=j+1;k<templst.size();++k){
            Line ln2=templst.get(k);
            if(ln1.distoln(ln2)<jsjl){
              ljx.get(i).get(j).add(ln2);
              ljx.get(i).get(k).add(ln1);
            }
          }
        }
      }*/
      
      //聚类
      List<List<List<Line>>> jllns=new ArrayList();
      for(int i=0;i<djlns.size();++i)//遍历每一个同斜率的集合
      {
        List<Line> txllst=djlns.get(i);//同斜率的集合  实际上是对每一个txllst聚类得到一个txljjllst，放在jllns集合
        List<List<Line>> txljjllst=new ArrayList();//同斜率后，且距离近的聚类后集合  同斜率下面的，以near-line聚类后簇的集合
        for(int j=0;j<txllst.size();++j)//遍历相同斜率集合中的每一条线，去找近距离线
        {
          Line ln1=txllst.get(j);
          List<Line> ncu=new ArrayList();//以ln1找near-line后构成的新簇
          ncu.add(ln1);
          for(int k=j+1;k<txllst.size();++k)
          {
            Line ln2=txllst.get(k);
            if(ln1.distoln(ln2)<jsjl)//near-line的阈值
            {
              ncu.add(ln2);
            }
          }
          //将新簇与旧簇比较
          boolean flag3=false;//标记新簇ncu是否和已存在的簇集合中哪个簇同类
          for(int k=0;k<ncu.size();++k)//遍历新簇
          {
            Line ln3=ncu.get(k);//新簇中的ln3线
            boolean flag2=false;//标记ln3在簇集合中是否找到同类的簇
            List<Line> cur_cu=ncu;
            for(int y=0;y<txljjllst.size();++y)//遍历旧簇的集合
            {
              List<Line> jcu=txljjllst.get(y);//已存在的旧簇
              boolean flag=false;//标记ln3与jcu是否同类
              for(int x=0;x<jcu.size();++x)//遍历旧簇
              {
                Line ln4=jcu.get(x);//旧簇中的ln4线
                if(ln3==ln4)
                {
                  flag=true;
                  break;
                }
                else if(ln3.distoln(ln4)<jsjl)
                {
                  flag=true;
                  break;
                }
              }
              if(flag)//新簇与jcu同类
              {
                flag2=true;
                for(int z=0;z<cur_cu.size();z++)
                {
                  Line templn=cur_cu.get(z);
                  if(jcu.contains(templn)==false)  jcu.add(templn);
                }
                if(cur_cu!=ncu)//当前簇cur_cu已经合并到了jcu中，如果当前簇不是ncu的话，在应将其从簇集合strs中删除
                {
                  txljjllst.remove(cur_cu);
                  y--;
                }
                cur_cu=jcu;
              }
            }
            if(flag2)
            {
              flag3=true;
              break;
            }
          }
          if(flag3==false)//新簇ncu与已存在的簇都不同类，则追加到txljlllst
          {
            txljjllst.add(ncu);
          }
        }
        jllns.add(txljjllst);
      }
      /*List<List<List<Line>>> jllns=new ArrayList();//同斜率后，再按距离聚类后的结果集合
      for(int i=0;i<djlns.size();++i){//遍历每个斜率
        List<Line> templst=djlns.get(i);
        List<List<Line>> templst2=new ArrayList();
        for(int j=0;j<templst.size();++j){//遍历同斜率的每根线
          Line sln=templst.get(j);
          List<Line> tlst=new ArrayList();//新构建的一个簇
          tlst.add(sln);
          for(int k=0;k<tlst.size();++k){//遍历簇里面的线，去找近距离线
            Line ln=tlst.get(k);
            for(int m=j+1;m<templst.size();++m){//在同斜率的集合中找
              Line tln=templst.get(m);
              //System.out.println("ln.distoln:"+ln.distoln(tln));
              if(ln.distoln(tln)<jsjl){
                tlst.add(tln);
                templst.remove(tln);//这里remove掉，可能对聚类有影响
                m--;
                //j--;
              }
            }
          }
          templst2.add(tlst);         
        }
        jllns.add(templst2);
      }旧的聚类，可能有问题*/
      
      /*//print
      File file=new File("ts.txt"); 
      FileWriter fw=new FileWriter(file);
      BufferedWriter bfw=new BufferedWriter(fw); 
      for(int i=0;i<jllns.size();++i){
        List<List<Line>> templst=jllns.get(i);
        for(int j=0;j<templst.size();++j){//每个小类
          bfw.write(i+" "+j);
          bfw.newLine();
          bfw.write("PLINE");
          List<Line> tlst=templst.get(j);
          for(int k=0;k<tlst.size();++k){
            Line ln=tlst.get(k);
            bfw.write(" "+ln.qd.x+","+ln.qd.y+" "+ln.zd.x+","+ln.zd.y+" ");
            bfw.newLine();
          }
        }
        bfw.newLine();
        bfw.newLine();
        bfw.flush();//注意这个
      }
      bfw.close();*/
      
      //下面都是针对水平和竖直的线簇
      //构造外包平行四边形
      List<List<List<Double>>> ssdjbj=new ArrayList();//四个边界。左、右、下、上   同一斜率，同一近似距离内，四个边界
      for(int i=0;i<jllns.size();++i){//遍历斜率
        if(i!=0 && i!=1) continue;//如果不是垂直或水平的，则暂不处理
        List<List<Line>> templst=jllns.get(i);
        List<List<Double>> templst2=new ArrayList();
        for(int j=0;j<templst.size();++j){//遍历距离划分的小集合
          List<Line> tlst=templst.get(j);
          List<Double> tlst2=new ArrayList();
          double minx=100000000;
          double maxx=-100000000;
          double miny=100000000;
          double maxy=-100000000;
          for(int k=0;k<tlst.size();++k){//遍历小集合内的线
            Line templn=tlst.get(k);
            minx=minx<templn.qd.x?minx:templn.qd.x;
            minx=minx<templn.zd.x?minx:templn.zd.x;
            maxx=maxx>templn.qd.x?maxx:templn.qd.x;
            maxx=maxx>templn.zd.x?maxx:templn.zd.x;
            miny=miny<templn.qd.y?miny:templn.qd.y;
            miny=miny<templn.zd.y?miny:templn.zd.y;
            maxy=maxy>templn.qd.y?maxy:templn.qd.y;
            maxy=maxy>templn.zd.y?maxy:templn.zd.y;
          }
          if((maxx-minx<0.1) || (maxy-miny<0.1)) continue;
          tlst2.add(minx);
          tlst2.add(miny);
          tlst2.add(maxx);
          tlst2.add(miny);
          tlst2.add(maxx);
          tlst2.add(maxy);
          tlst2.add(minx);
          tlst2.add(maxy);
          tlst2.add(minx);
          tlst2.add(miny);
          templst2.add(tlst2);
        }
        ssdjbj.add(templst2);
      }
      
      //打印平行四边形
      File file=new File("pxsbx.txt"); 
      FileWriter fw=null;
      if(moshi==CHANGXIAN){
        fw=new FileWriter(file);
      }
      else
        fw=new FileWriter(file,true);//构造函数中的第二个参数true表示以追加形式写文件
      BufferedWriter bfw=new BufferedWriter(fw); 
      for(int i=0;i<ssdjbj.size();++i){
        List<List<Double>> templst=ssdjbj.get(i);
        //System.out.println("size:"+templst.size());
        for(int j=0;j<templst.size();++j){//每个小类
          //bfw.write(i+" "+j);
          bfw.newLine();
          bfw.write("PLINE");
          List<Double> tlst=templst.get(j);
          for(int k=0;k<tlst.size();k=k+2){
            bfw.write(" "+tlst.get(k)+","+tlst.get(k+1));
          }
          bfw.write(" ");
          bfw.newLine();
        }
        bfw.newLine();
        bfw.newLine();
        bfw.flush();//注意这个
      }
      bfw.close();
      
      //由外包平行四边形构造线
      /*List<Line> lns=new ArrayList();
      for(int i=0;i<djbj.size();++i){
        List<List<Double>> templst=djbj.get(i);
        for(int j=0;j<templst.size();++j){
          List<Double> tlst=templst.get(j);
          double minx=tlst.get(0);
          double miny=tlst.get(1);
          double maxx=tlst.get(2);
          double maxy=tlst.get(5);
          double qx=0,qy=0,zx=0,zy=0;
          if(maxx-minx<maxy-miny){
            qx=(maxx+minx)/2;
            qy=miny;
            zx=(maxx+minx)/2;
            zy=maxy;
          }
          else{
            qx=minx;
            qy=(maxy+miny)/2;
            zx=maxx;
            zy=(maxy+miny)/2;
          }
          Line ln=new Line(qx,qy,zx,zy);
          lns.add(ln);
        }
      }
      
      //打印构造后的线
      File file3=new File("gzx.txt"); 
      FileWriter fw3=new FileWriter(file3);
      BufferedWriter bfw3=new BufferedWriter(fw3); 
      for(int i=0;i<lns.size();++i){
        Line templn=lns.get(i);
        bfw3.write("PLINE "+templn.qd.x+","+templn.qd.y+" "+templn.zd.x+","+templn.zd.y+" ");
        bfw3.newLine();
        bfw3.flush();//注意这个
      }
      bfw3.close();
      */
      
      //由外包平行四边形构造线，lns是List<List<Line>>版本，即把同斜率的放在一个List中
      //List<List<Line>> lns=new ArrayList();
      /*if(moshi==DUANXIAN){//对于短线集合，一个平行四边形构成两条短线
        for(int i=0;i<djbj.size();++i){
          List<List<Double>> templst=djbj.get(i);//同一斜率内，所有以近似距离划分的小类
          List<Line> templst2=new ArrayList();
          for(int j=0;j<templst.size();++j){
            List<Double> tlst=templst.get(j);
            double minx=tlst.get(0);
            double miny=tlst.get(1);
            double maxx=tlst.get(2);
            double maxy=tlst.get(5);
            double qx=0,qy=0,zx=0,zy=0;
            
              qx=(maxx+minx)/2;
              qy=miny;
              zx=(maxx+minx)/2;
              zy=maxy;
            Line ln=new Line(qx,qy,zx,zy);
            templst2.add(ln);
            
              qx=minx;
              qy=(maxy+miny)/2;
              zx=maxx;
              zy=(maxy+miny)/2;
            Line ln2=new Line(qx,qy,zx,zy);
            templst2.add(ln2);
                
          }
          lns.add(templst2);
        }
      }
      else{*/
        for(int i=0;i<ssdjbj.size();++i){
          List<List<Double>> templst=ssdjbj.get(i);//同一斜率内，所有以近似距离划分的小类
          List<Line> templst2=new ArrayList();
          for(int j=0;j<templst.size();++j){
            List<Double> tlst=templst.get(j);
            double minx=tlst.get(0);
            double miny=tlst.get(1);
            double maxx=tlst.get(2);
            double maxy=tlst.get(5);
            double qx=0,qy=0,zx=0,zy=0;
            if(maxx-minx<maxy-miny){
              qx=(maxx+minx)/2;
              qy=miny;
              zx=(maxx+minx)/2;
              zy=maxy;
            }
            else{
              qx=minx;
              qy=(maxy+miny)/2;
              zx=maxx;
              zy=(maxy+miny)/2;
            }
            Line ln=new Line(qx,qy,zx,zy);
            //if(ln.length()>jsjl) 
                templst2.add(ln);
          }
          lns.add(templst2);
        }
      /*}*/
      
      //针对斜线，进行上述类似处理
      //构造外包平行四边形
      List<List<List<Double>>> xxdjbj=new ArrayList();//四个边界。从右上点逆时针，8个坐标
      for(int i=0;i<jllns.size();++i){//遍历斜率
        if(i==0 || i==1) continue;//不处理垂直的和水平的线簇
        List<List<Line>> templst=jllns.get(i);
        List<List<Double>> templst2=new ArrayList();
        for(int j=0;j<templst.size();++j){//遍历距离划分的小集合
          List<Line> tlst=templst.get(j);
          List<Double> tlst2=new ArrayList();
          double minx=100000000;
          double maxx=-100000000;
          double miny=100000000;
          double maxy=-100000000;
          for(int k=0;k<tlst.size();++k){//遍历小集合内的线   此循环是先得到最小和最大y值
            Line templn=tlst.get(k);
            minx=minx<templn.qd.x?minx:templn.qd.x;
            minx=minx<templn.zd.x?minx:templn.zd.x;
            maxx=maxx>templn.qd.x?maxx:templn.qd.x;
            maxx=maxx>templn.zd.x?maxx:templn.zd.x;
            miny=miny<templn.qd.y?miny:templn.qd.y;
            miny=miny<templn.zd.y?miny:templn.zd.y;
            maxy=maxy>templn.qd.y?maxy:templn.qd.y;
            maxy=maxy>templn.zd.y?maxy:templn.zd.y;
          }
          if((maxx-minx<0.1) || (maxy-miny<0.1)) continue;
          double smaxx=-1000000000;
          double sminx=1000000000;
          double xmaxx=-1000000000;
          double xminx=1000000000;
          for(int k=0;k<tlst.size();++k)//这次遍历是计算每条线当y分别等于ymax和ymin时对应的x值，即x上和x下。然后比较得出ymax时最大和最小的x值，ymin时最大和最小的x值
          {
            Line templn=tlst.get(k);
            double xl=templn.getxl();
            double xb=templn.getb();
            double xshang=(maxy-xb+0.0)/(xl+0.0);
            double xxia=(miny-xb+0.0)/(xl+0.0);
            smaxx=xshang>smaxx?xshang:smaxx;
            sminx=xshang<sminx?xshang:sminx;
            xmaxx=xxia>xmaxx?xxia:xmaxx;
            xminx=xxia<xminx?xxia:xminx;
          }
          tlst2.add(smaxx);
          tlst2.add(maxy);
          tlst2.add(sminx);
          tlst2.add(maxy);
          tlst2.add(xminx);
          tlst2.add(miny);
          tlst2.add(xmaxx);
          tlst2.add(miny);
          tlst2.add(smaxx);//重复第一个点
          tlst2.add(maxy);
          templst2.add(tlst2);
        }
        xxdjbj.add(templst2);
      }
      //打印平行四边形
      File file2=new File("pxsbx.txt"); 
      FileWriter fw2=new FileWriter(file2,true);//构造函数中的第二个参数true表示以追加形式写文件
      BufferedWriter bfw2=new BufferedWriter(fw2); 
      for(int i=0;i<xxdjbj.size();++i){
        List<List<Double>> templst=xxdjbj.get(i);
        //System.out.println("size:"+templst.size());
        for(int j=0;j<templst.size();++j){//每个小类
          //bfw.write(i+" "+j);
          bfw2.newLine();
          bfw2.write("PLINE");
          List<Double> tlst=templst.get(j);
          for(int k=0;k<tlst.size();k=k+2){
            bfw2.write(" "+tlst.get(k)+","+tlst.get(k+1));
          }
          bfw2.write(" ");
          bfw2.newLine();
        }
        bfw2.newLine();
        bfw2.newLine();
        bfw2.flush();//注意这个
      }
      bfw2.close();
      //由外包平行四边形构造线。即简化步骤
      for(int i=0;i<xxdjbj.size();++i){
          List<List<Double>> templst=xxdjbj.get(i);//同一斜率内，所有以近似距离划分的小类
          List<Line> templst2=new ArrayList();
          for(int j=0;j<templst.size();++j){
            List<Double> tlst=templst.get(j);
            double smaxx=tlst.get(0);
            double sminx=tlst.get(2);
            double xminx=tlst.get(4);
            double xmaxx=tlst.get(6);
            double maxy=tlst.get(1);
            double miny=tlst.get(5);
            double qx=0,qy=0,zx=0,zy=0;
            //因为你简化后的线的斜率肯定是和原来线斜率要一样，所以可以直接下面这样对qx等赋值
            qx=(smaxx+sminx)/2;
            qy=maxy;
            zx=(xmaxx+xminx)/2;
            zy=miny;
            Line ln=new Line(qx,qy,zx,zy);
            //if(ln.length()>jsjl) 
                templst2.add(ln);
          }
          lns.add(templst2);
        }
        
      //打印构造后的线
      File file3=new File("gzx.txt"); 
      FileWriter fw3=null;
      if(moshi==CHANGXIAN){
        fw3=new FileWriter(file3);
      }
      else
        fw3=new FileWriter(file3,true);//构造函数中的第二个参数true表示以追加形式写文件
      BufferedWriter bfw3=new BufferedWriter(fw3); 
      for(int i=0;i<lns.size();++i){
        List<Line> templst=lns.get(i);
        for(int j=0;j<templst.size();++j){
          Line templn=templst.get(j);
          bfw3.write("PLINE "+templn.qd.x+","+templn.qd.y+" "+templn.zd.x+","+templn.zd.y+" ");
          bfw3.newLine();
          bfw3.flush();//注意这个
        }
      }
      bfw3.close();
      
      
    }
    
    public static void lns_with_jd(List<List<Line>> lns,List<Line> zhlns) throws IOException{
      //计算交点，并划分线段，得到最终的线段集合
      List<List<List<Point>>>  lnps=new ArrayList();//相应于lns的每条线被划分成的点集合
      for(int i=0;i<lns.size();++i){//初始化
        List<Line> templst=lns.get(i);
        List<List<Point>>  templst2=new ArrayList();
        for(int j=0;j<templst.size();++j){
          List<Point> tlst2=new ArrayList();
          templst2.add(tlst2);
        }
        lnps.add(templst2);
      }
      for(int i=0;i<lns.size();++i){
        List<Line> templst=lns.get(i);
        for(int m=0;m<templst.size();++m){//i循环中的每一条线
          Line ln1=templst.get(m);
          lnps.get(i).get(m).add(ln1.qd);
          for(int j=i+1;j<lns.size();++j){//与其他不同斜率线求交点
            List<Line> templst2=lns.get(j);
            for(int n=0;n<templst2.size();++n){
              Line ln2=templst2.get(n);
              //lnps.get(j).get(n).add(ln2.qd);
              Point jdp=jiaodian(ln1,ln2);
              if(is_online(jdp,ln1,jsjl)&&is_online(jdp,ln2,jsjl)){//交点在两条线上
                lnps.get(i).get(m).add(jdp);
                lnps.get(j).get(n).add(jdp);
              }
            }
          }
          lnps.get(i).get(m).add(ln1.zd);
        }
      }
      
      //点集合排序
      for(int i=0;i<lnps.size();++i){
        List<List<Point>> templst=lnps.get(i);
        for(int j=0;j<templst.size();++j){
          List<Point> tlst=templst.get(j);
          Collections.sort(tlst, new Comparator<Point>() {
            public int compare(Point arg0, Point arg1) {
                if(Math.abs(arg0.x-arg1.x)<szxpc){//竖直线的点
                  if(arg0.y<arg1.y) return -1;
                  else return 1;
                }
                if(arg0.x<arg1.x) return -1;
                else return 1;
            }
          });
        }
      }
      
      //由排序后的点构造线
      //List<Line> zhlns=new ArrayList();//最终用于构造房间的线段集合
      for(int i=0;i<lnps.size();++i){
        List<List<Point>> templst=lnps.get(i);
        for(int j=0;j<templst.size();++j){
          List<Point> tlst=templst.get(j);
          for(int k=0;k<tlst.size()-1;++k){
            Point p=null;
            Point q=null;
            /*if(k==0){
              p=tlst.get(k);
              Point r=tlst.get(k+1);
              if(p.distance(r)<=jsjl){//如果第0点和第1点足够接近，则用第0点和第2点直接构造线了
                q=tlst.get(k+2);
                k++;
              }
              else{
                q=r;
              }
            }
            else if(k==tlst.size()-3){
              p=tlst.get(k);//第n-3点
              Point r=tlst.get(k+1);//第n-2点
              q=tlst.get(k+2);//第n-1点，即最后一点，n为tlst集合大小
              if(r.distance(q)<=jsjl){//如果最后两点距离足够接近，则由倒数第三点和倒数最后一点直接构成线段，而不是两个线段     这里和上个if的判断，都是鉴于之前求得的交点可能不在线上的原因
                k++;
              }
              else{
                q=r;
              }
            }
            else{*/
              p=tlst.get(k);
              q=tlst.get(k+1);
            /*}*/
            //if(p.distance(q)<jsjl) continue;
            Line ln=new Line(p,q);
            zhlns.add(ln);
          }
        }
      }
      
      //打印最后的线段集合
      File file4=new File("zhlns.txt"); 
      FileWriter fw4=null;
      //if(moshi==CHANGXIAN){
        fw4=new FileWriter(file4);
      //}
      //else
        //fw4=new FileWriter(file4,true);//构造函数中的第二个参数true表示以追加形式写文件
      BufferedWriter bfw4=new BufferedWriter(fw4); 
      for(int i=0;i<zhlns.size();++i){
          Line templn=zhlns.get(i);
          bfw4.write("PLINE "+templn.qd.x+","+templn.qd.y+" "+templn.zd.x+","+templn.zd.y+" ");
          bfw4.newLine();
          bfw4.flush();//注意这个
      }
      bfw4.close();
    }
    
    public static Point jiaodian(Line ln1,Line ln2){//假设ln1和ln2斜率不同，则返回它们的交点
      if(Math.abs(ln1.qd.x-ln1.zd.x)<0.001){//ln1是竖直线，则ln2不是竖直线了
        double xl2=ln2.getxl();
        double b2=ln2.getb();
        double y=xl2*ln1.qd.x+b2;
        return new Point(ln1.qd.x,y);
      }
      if(Math.abs(ln2.qd.x-ln2.zd.x)<0.001){
        double xl1=ln1.getxl();
        double b1=ln1.getb();
        double y=xl1*ln2.qd.x+b1;
        return new Point(ln2.qd.x,y);
      }
      //没有竖直线
      double k1=ln1.getxl();
      double b1=ln1.getb();
      double k2=ln2.getxl();
      double b2=ln2.getb();
      double x=(b2-b1)/(k1-k2);
      double y=k1*x+b1;
      return new Point(x,y);
    }
    
    public static boolean is_online(Point p,Line ln1,double distyz){//判断交点是否近似在线上，近似量为distyz。近似量是用以考虑这个误差的，交点在端点很近的地方
      //因为是交点，所以可以简便判断
      if(((p.x<ln1.qd.x)&&(p.x>ln1.zd.x))||((p.x<ln1.zd.x)&&(p.x>ln1.qd.x))) return true;//因为是交点，所以此判断说明交点在起点和终点之间
      if(((p.y<ln1.qd.y)&&(p.y>ln1.zd.y))||((p.y<ln1.zd.y)&&(p.y>ln1.qd.y))) return true;
      double dist1=p.distance(ln1.qd);
      if(dist1<distyz) return true;
      double dist2=p.distance(ln1.zd);
      if(dist2<distyz) return true;
      return false;
    }
    
    /*public static void addlntoset(Line ln,List<List<Line>> set,List<List<List<Line>>> ljx,int i,int j,List<Line> ljxlst){
      if(ln_inset(ln,set)==false){
         List<Line> tlst2=new ArrayList();
         tlst2.add(ln);
         for(int k=0;k<ljxlst.size();++k){//遍历邻近线
              Line templn=ljxlst.get(k);
              if(tlst2.contains(templn)==false){
                tlst2.add(templn);
                addlntoset(templn,set,ljx,i,j,ljx.get(i).get());
              }
         }
         set.add(tlst2);
      }
    }
    
    public static boolean near_set(List<Line> lst1,List<Line> lst2,double dst){//判断两个小类集合是否足够接近，只要两个集合中各有一线之间足够接近
      for(int i=0;i<lst1.size();++i){
        Line ln1=lst1.get(i);
        for(int j=0;j<lst)
      }
    }*/
    
    public static boolean near_line(List<Line> lst,Line ln,double dst){//判断一个线是否和一个线段集合足够接近
      for(int i=0;i<lst.size();++i){
        double distance=ln.distoln(lst.get(i));
        if(distance<dst) return true;
      }
      return false;
    }
    
    public static int xl_find(List<Double> djsy,double xl){//在djsy集合中找斜率xl，找到返回索引号；否则返回集合大小。
      int i=2;
      for(;i<djsy.size();++i){//0号和1号是竖直和水平线，0号的斜率是随便设的。
        if(Math.abs(djsy.get(i)-xl)<1) return i;
      }
      return i;
    }
    
    public static void rm_pre_process(List<Line> dxlns){
      //得到短线集合
      for(int i=0;i<roomlns.size();++i){
        Line ln=roomlns.get(i);
        if(ln.length()<=jsjl){
          roomlns.remove(i);
          i--;
          dxlns.add(ln);
        }
      }
      //讲短线中和门相邻的去除
    }
    
    public static double parallelln_sumdist(Line ln1,Line ln2){//计算两条平行线，对应端点之间距离和。返回最小距离和
      double dist1=ln1.qd.distance(ln2.qd)+ln1.zd.distance(ln2.zd);
      double dist2=ln1.qd.distance(ln2.zd)+ln1.zd.distance(ln2.qd);
      if(dist1<dist2) return dist1;
      else return dist2;
    }
    
    public static boolean parallel_contain(Line ln1,Line ln2){//两条平行线，判断ln2是否包含ln1，包含的意思是ln1对ln2的投影在ln2上。
      //没有考虑斜线的情况
      if(ln1.is_vertical()){
        double miny1=ln1.qd.y<ln1.zd.y?ln1.qd.y:ln1.zd.y;
        double maxy1=ln1.qd.y>ln1.zd.y?ln1.qd.y:ln1.zd.y;
        double miny2=ln2.qd.y<ln2.zd.y?ln2.qd.y:ln2.zd.y;
        double maxy2=ln2.qd.y>ln2.zd.y?ln2.qd.y:ln2.zd.y;
        if((miny1>=miny2)&&(maxy1<=maxy2)) return true;
        else return false;
      }
      double minx1=ln1.qd.x<ln1.zd.x?ln1.qd.x:ln1.zd.x;
      double maxx1=ln1.qd.x>ln1.zd.x?ln1.qd.x:ln1.zd.x;
      double minx2=ln2.qd.x<ln2.zd.x?ln2.qd.x:ln2.zd.x;
      double maxx2=ln2.qd.x>ln2.zd.x?ln2.qd.x:ln2.zd.x;
      if((minx1>=minx2)&&(maxx1<=maxx2)) return true;
      else return false;
    }
    
    public static boolean parallelln(Line ln1,Line ln2){//按照斜率来看两条线是否平行。之前的parallel函数判断平行好像有问题
      if(ln1.is_vertical()&&ln2.is_vertical()) return true;
      else if((ln1.is_vertical()==false)&&(ln2.is_vertical()==false)){
        double xl1=ln1.getxl();
        double xl2=ln2.getxl();
        if(Math.abs(xl1-xl2)<1) return true;
        else return false;
      }
      else return false;
    }
    
    public static void find_doorlns(List<Line> zhlns,List<Line> doorsteps,List<Integer> drlns){//由于门槛和墙线融合后进行了找平行四边形等的变化，最后形成最后的短线集合zhlns，这里由doorstep来找对应的短线
      for(int i=0;i<doorsteps.size();++i){
        Line dr=doorsteps.get(i);
        double mindist=1000000000000.0;
        int minindx=-1;
        for(int j=0;j<zhlns.size();++j){
          Line templn=zhlns.get(j);
          if(parallelln(dr,templn)&&parallel_contain(dr,templn)){
            //System.out.println("Hello!"+i);
            double dist=parallelln_sumdist(dr,templn);
            if(dist<mindist){
              mindist=dist;
              minindx=j;
            }
          }
        }
        drlns.add(minindx);
      }
    }
    
    public static void save_doorlns(List<Line> zhlns,List<Integer> drlns) throws IOException{
      File file=new File("zhdoorsteps.txt"); 
      FileWriter fw=new FileWriter(file);
      BufferedWriter bfw=new BufferedWriter(fw);
      for(int i=0;i<drlns.size();++i){
        int k=drlns.get(i);
        if(k<0){
          bfw.write("No line.");
          bfw.newLine();
          continue;
        }
        Line ln=zhlns.get(k);
        bfw.write("PLINE "+ln.qd.x+","+ln.qd.y+" "+ln.zd.x+","+ln.zd.y+" ");
        bfw.newLine();
      }
      bfw.flush();
      bfw.close();
    }
    
    public static void Extract_Rooms() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException{
      System.out.println("墙线个数:"+roomlns.size());
      List<Line> dxlns=new ArrayList();//短线集合
//      rm_pre_process(dxlns);  //如果把这里注释掉，不把短线拿出来单独处理，则可能导致简化后的墙线偏离墙的轮廓一点
      
      roomlns.addAll(doorsteps);//合并提取房间所需要的线段，这里还没有考虑房间的弧线段   假设门槛不进行预处理的，方便确定门槛在roomlns中的索引
      boolean flag=false;//标识房间图层是否含门图层的，如果有，需要把门图层的线在后面加入到roomlns中
      for(int i=0;i<mentc.size();++i){
        if(fangjtc.contains(mentc.get(i))){
          flag=true;
          break;
        }
      }
      if(flag) roomlns.addAll(doorlns);
      
      List<List<Line>> templns=new ArrayList();
      my_preprocess(roomlns,templns,CHANGXIAN);
//      my_preprocess(dxlns,templns,DUANXIAN);
      List<Line> zhlns=new ArrayList();//用于抽取房间的线段集合
      lns_with_jd(templns,zhlns);
      
      //找门槛对应的zhlns
      List<Integer> drlns=new ArrayList();//门槛线对应的zhlns下标
      find_doorlns(zhlns,doorsteps,drlns);
      save_doorlns(zhlns,drlns);
      
      double pylen=tmaxy-tminy+20;//20是原图的tminy与新图的tmaxy之间的间隔    偏移长度
      //store_predxf(pylen);
      findAllSuccessors(zhlns);
      List<Integer> quchux=new ArrayList();
      for(int i=0;i<zhlns.size();++i){
        if((qsuccessors.get(i).size()==0)||(zsuccessors.get(i).size()==0)){//如果一个线以某端点去找邻接线为空，则不会构成房间
          quchux.add(i);
        }
      }
      List<List<Integer>> roomCandidates=new LinkedList();//房间候选集
      List<List<Integer>> roombz=new LinkedList();//相应房间线段间连接的标志
      
      for(int i=0;i<zhlns.size();++i){//遍历线段
        if(quchux.contains(i)) continue;
        Line ln=zhlns.get(i);
        Queue<List<Integer>> queue=new LinkedList();//多边形队列
        Queue<List<Integer>> quebz=new LinkedList();//多边形线段间标志的列表队列，每个列表的元素表示当前线与上一线邻接点的标志
        Queue<List<Integer>> queysx=new LinkedList();//每个多边形对应的已删除线的索引
        List<Integer> csx=new ArrayList();//初始线的索引列表
        List<Integer> csbz=new ArrayList();//初始线的标志
        List<Integer> ys=new ArrayList();//初始已删线
        csx.add(i);//起始线的索引
        queue.add(csx);
        csbz.add(QIDIAN);//起始线找后继线邻接点是终点
        quebz.add(csbz);
        ys.add(i);
        queysx.add(ys);
        boolean rmflag=false;//当前线是否找到房间
        while(queue.isEmpty()==false){
          if(queue.size()>300) break;
          
          //System.out.println("queue:");
          //printqueue(queue,quebz);
          //System.out.println(num++);
          //System.out.println("队列大小:"+queue.size());
          List<Integer> quehd=queue.remove();//队首元素多边形
          //if(quehd.size()>20) break;
          int weisy=quehd.get(quehd.size()-1);//队首元素的最后一根线的索引
          List<Integer> hdbz=quebz.remove();//队首多边形的线段间标志列表
          int biaoz=hdbz.get(hdbz.size()-1);//队首多边形的尾线进行寻找后继线时邻接点的标志
          ys=queysx.remove();
          //printfj(quehd,hdbz);
          
          if(rmflag || quehd.size()>20){// && quehd.size()!=roomCandidates.get(roomCandidates.size()-1).size()如果当前线已找到房间，而队首多边形的线的个数与最新加入的房间的线个数不等，则说明生长相同步长的多边形已遍历结束
            break;//跳出while循环
          }
          
          Point wp=null;//尾线的邻接点，即现多边形的尾点
          if(biaoz==ZHONGDIAN){//与上一线交于终点，则尾点为起点
            wp=zhlns.get(weisy).qd;
          }
          else{
            wp=zhlns.get(weisy).zd; 
          }
          
          if(quehd.size()>3 && aqlpnt(zhlns.get(quehd.get(0)).qd,wp)){//多边形的尾点和首点近似相等
            roomCandidates.add(quehd);
            roombz.add(hdbz);
            rmflag=true;  //break;
            continue;
          }
          
          Map<Integer,Integer> mphjx=null;//尾线的后继线索引、标志
          if(biaoz==ZHONGDIAN){
            mphjx=qsuccessors.get(weisy);
          }
          else{
            mphjx=zsuccessors.get(weisy);   
          }
          
          Iterator it = mphjx.entrySet().iterator();
          while(it.hasNext()){//遍历后继线
              Map.Entry entry=(Map.Entry)it.next();
              int hjxsy=(Integer)entry.getKey();//尾线的后继线的索引
              if(quchux.contains(hjxsy)) continue;
              int hjxbz=(Integer)entry.getValue();//尾线的后继线标志
              //printscc(hjxsy,hjxbz);
              if(ys.contains(hjxsy)) continue;//如果队首多边形的已删线列表中含有该后继线索引，则跳过
              List<Integer> hjxlst=deepCopy(quehd);
              hjxlst.add(hjxsy);//增加后继线
              queue.add(hjxlst);//包含了后继线的列表入队
              List<Integer> bzlst=deepCopy(hdbz);
              bzlst.add(hjxbz);//增加新线的标志
              quebz.add(bzlst);//后继线标志 入 标志队列
              List<Integer> hjxys=deepCopy(ys);
              hjxys.addAll(mphjx.keySet());
              queysx.add(hjxys);
          }
       }
     }
     
     post_process4(roomCandidates,roombz,2000*1000,zhlns);//2平方米的阈值
     /*
      //pre_process(roomlns);
      //my_preprocess(roomlns);
      int yslen=roomlns.size();
      roomlns.addAll(doorsteps);//合并提取房间所需要的线段，这里还没有考虑房间的弧线段   假设门槛不进行预处理的，方便确定门槛在roomlns中的索引
      List<Line> zhlns=new ArrayList();//用于抽取房间的线段集合
      my_preprocess(roomlns,zhlns);
      //原来版本，找所有多边形。
      double pylen=tmaxy-tminy+20;//20是原图的tminy与新图的tmaxy之间的间隔    偏移长度
      //store_predxf(pylen);
      List<Integer> doorsy=new ArrayList();//门线的索引集合
      for(int i=0;i<doorsteps.size();++i){
        doorsy.add(yslen+i);
      }
      findAllSuccessors(zhlns);
      List<List<Integer>> roomCandidates=new LinkedList();//房间候选集
      List<List<Integer>> roombz=new LinkedList();//相应房间线段间连接的标志
      List<Integer> doornum=new ArrayList();//对应上面的相应房间经过的门的个数
      
      for(int i=0;i<zhlns.size();++i){//遍历线段
     // for(int j=0;j<doorsteps.size();++j){//遍历门槛
        //int i=j+yslen;
        Line ln=zhlns.get(i);
        if(aqlpnt(ln.qd,ln.zd)) continue;
        Queue<List<Integer>> queue=new LinkedList();//多边形队列
        Queue<List<Integer>> quebz=new LinkedList();//多边形线段间标志的列表队列，每个列表的元素表示当前线与上一线邻接点的标志
        Queue<Integer> quedn=new LinkedList();//多边形的门个数
        Queue<List<Integer>> queysx=new LinkedList();//每个多边形对应的已删除线的索引
        List<Integer> csx=new ArrayList();//初始线的索引列表
        List<Integer> csbz=new ArrayList();//初始线的标志
        List<Integer> ys=new ArrayList();//初始已删线
        csx.add(i);//起始线的索引
        queue.add(csx);
        int dnum=0;//当前多边形所含门的个数
        if(doorsy.contains(i)) dnum++;
        quedn.add(dnum);
        csbz.add(QIDIAN);//起始线找后继线邻接点是终点
        quebz.add(csbz);
        ys.add(i);
        queysx.add(ys);
        boolean rmflag=false;//当前线是否找到房间
        while(queue.isEmpty()==false){
          //System.out.println("queue:");
          //printqueue(queue,quebz);
          //System.out.println(num++);
          //System.out.println("队列大小:"+queue.size());
          List<Integer> quehd=queue.remove();//队首元素多边形
          int weisy=quehd.get(quehd.size()-1);//队首元素的最后一根线的索引
          List<Integer> hdbz=quebz.remove();//队首多边形的线段间标志列表
          int biaoz=hdbz.get(hdbz.size()-1);//队首多边形的尾线进行寻找后继线时邻接点的标志
          int dm=quedn.remove();//队首多边形的门个数
          ys=queysx.remove();
          //printfj(quehd,hdbz);
          
          if(rmflag && quehd.size()!=roomCandidates.get(roomCandidates.size()-1).size()){// && quehd.size()!=roomCandidates.get(roomCandidates.size()-1).size()如果当前线已找到房间，而队首多边形的线的个数与最新加入的房间的线个数不等，则说明生长相同步长的多边形已遍历结束
            break;//跳出while循环
          }
          
          Point wp=null;//尾线的邻接点，即现多边形的尾点
          if(biaoz==ZHONGDIAN){//与上一线交于终点，则尾点为起点
            wp=zhlns.get(weisy).qd;
          }
          else{
            wp=zhlns.get(weisy).zd; 
          }
          
          if(quehd.size()>3 && aqlpnt(zhlns.get(quehd.get(0)).qd,wp)){//多边形的尾点和首点近似相等
            roomCandidates.add(quehd);
            roombz.add(hdbz);
            doornum.add(dm);
            rmflag=true;  break;
            //continue;
          }
          
          Map<Integer,Integer> mphjx=null;//尾线的后继线索引、标志
          if(biaoz==ZHONGDIAN){
            mphjx=qsuccessors.get(weisy);
          }
          else{
            mphjx=zsuccessors.get(weisy);   
          }
          
          Iterator it = mphjx.entrySet().iterator();
          while(it.hasNext()){//遍历后继线
              Map.Entry entry=(Map.Entry)it.next();
              int hjxsy=(Integer)entry.getKey();//尾线的后继线的索引
              int hjxbz=(Integer)entry.getValue();//尾线的后继线标志
              //printscc(hjxsy,hjxbz);
              if(ys.contains(hjxsy)) continue;//如果队首多边形的已删线列表中含有该后继线索引，则跳过
              List<Integer> hjxlst=deepCopy(quehd);
              hjxlst.add(hjxsy);//增加后继线
              queue.add(hjxlst);//包含了后继线的列表入队
              int drm=dm;
              if(doorsy.contains(hjxsy)) drm++;
              quedn.add(drm);
              List<Integer> bzlst=deepCopy(hdbz);
              bzlst.add(hjxbz);//增加新线的标志
              quebz.add(bzlst);//后继线标志 入 标志队列
              List<Integer> hjxys=deepCopy(ys);
              hjxys.addAll(mphjx.keySet());
              queysx.add(hjxys);
          }
       }
     }
     
     post_process3(roomCandidates,roombz,doornum,2000*1000,zhlns);
      */
     /*
     findAllSuccessors(zhlns);
     List<List<Integer>> roomCandidates=new LinkedList();//房间候选集，存的是线在zhlns集合中的索引
     List<List<Integer>> roombz=new LinkedList();//相应房间线段间连接的标志
  
     for(int i=0;i<zhlns.size();++i){//遍历线段
        Line ln=zhlns.get(i);
        List<List<Integer>> queue=new LinkedList();//多边形队列
        List<List<Integer>> quebz=new LinkedList();//多边形线段间标志的列表队列，每个列表的元素表示当前线与上一线邻接点的标志
        List<List<Integer>> queysx=new LinkedList();//每个多边形对应的已删除线的索引
        List<Integer> csx=new ArrayList();//初始线的索引列表
        List<Integer> csbz=new ArrayList();//初始线的标志
        List<Integer> ys=new ArrayList();//初始已删线
        csx.add(i);//起始线的索引
        queue.add(csx);
        csbz.add(QIDIAN);//起始线找后继线邻接点是终点
        quebz.add(csbz);
        ys.add(i);
        queysx.add(ys);
        while(queue.isEmpty()==false){
          for(int j=0;j<queue.size();++j){//遍历所有房间
            List<Integer> temprm=queue.get(j);//当前房间
            int weisy=temprm.get(temprm.size()-1);//房间的最后一条线在zhlns中的索引
            List<Integer> tempbz=quebz.get(j);//当前房间的各线段间连接标志
            int wbz=tempbz.get(tempbz.size()-1);//当前房间的尾线进行找后继线时的邻接点标志
            List<Integer> tempys=queysx.get(j);//当前房间对应的已删线
            Point wp=null;//尾线的邻接点，即多边形的尾点
            if(wbz==ZHONGDIAN){//与上一线交于终点，则尾点为起点
              wp=zhlns.get(weisy).qd;
            }
            else{
              wp=zhlns.get(weisy).zd;
            }
            //判断
            if(){
            
            }
            //
            Map<Integer,Integer> mphjx=null;//尾线的后继线索引、标志
            if(wbz==ZHONGDIAN){
              mphjx=qsuccessors.get(weisy);
            }
            else{
              mphjx=zsuccessors.get(weisy);   
            }
            Iterator it = mphjx.entrySet().iterator();
            while(it.hasNext()){//遍历后继线
              Map.Entry entry=(Map.Entry)it.next();
              int hjxsy=(Integer)entry.getKey();//尾线的后继线的索引
              int hjxbz=(Integer)entry.getValue();//尾线的后继线标志
              //printscc(hjxsy,hjxbz);
              if(ys.contains(hjxsy)) continue;//如果队首多边形的已删线列表中含有该后继线索引，则跳过
              List<Integer> hjxlst=deepCopy(temprm);
              hjxlst.add(hjxsy);//增加后继线
              queue.add(hjxlst);//包含了后继线的列表入队
              List<Integer> bzlst=deepCopy(tempbz);
              bzlst.add(hjxbz);//增加新线的标志
              quebz.add(bzlst);//后继线标志 入 标志队列
              List<Integer> hjxys=deepCopy(ys);
              hjxys.addAll(mphjx.keySet());
              queysx.add(hjxys);
            }
            
          }
       }
     }*/
     //房间含有哪些门，可以在最后的房间集合中判断
     Collections.sort(drlns);
//     save_doorsteps(drlns,zhlns);
     for(int i=0;i<drlns.size();++i)
       System.out.print(drlns.get(i)+" ");
     List<List<Integer>>  door_fj=new ArrayList();//第n个元素表示第n号门经过的房间集合
     for(int i=0;i<drlns.size();++i){
       List<Integer> templst=new ArrayList();
       door_fj.add(templst);
     }
     //List<List<Integer>>  drnm=new ArrayList();//各房间经过的几个门
     for(int i=0;i<roomCandidates.size();++i){//遍历房间
       List<Integer> drlst=new ArrayList();//当前房间包含的门
       List<Integer> rmlst=roomCandidates.get(i);//当前房间
       for(int j=0;j<rmlst.size();++j){//遍历当前房间的每根线的索引号
         int num=rmlst.get(j);
         System.out.print(num+" ");
         List<Integer> tlst=new ArrayList();//当前房间经过了哪些门的索引号
         if(bi_search(drlns,num,tlst)){//当前房间的线是否在门集合中，是门集合中第几个线
           for(int k=0;k<tlst.size();++k){
             door_fj.get(tlst.get(k)).add(i);//在第tlst.get(k)号门的对应房间集合添加第i号房间
           } 
         }
       }
       System.out.println("\n"+i);
     }
     save_topo(door_fj);
     
     File file=new File(resflname); 
     FileWriter fw=new FileWriter(file);
     BufferedWriter bfw=new BufferedWriter(fw);
     System.out.println("房间个数："+roomCandidates.size());
     for(int i=0;i<roomCandidates.size();++i){//遍历房间  
       List<Integer> rmlst=roomCandidates.get(i);
       List<Integer> rmbz=roombz.get(i);
       //System.out.println("房间"+i+"  "+rmlst.size());
       
       int fjlen=rmlst.size()*4;//坐标个数。  构成该房间的点个数:线段数*2。坐标个数为点个数的2倍。  没有重复首点是因为调用createLinearPolygon函数，可自动补上首点
       double fjzb[]=new double[fjlen];
       
       
       //bfw.write("房间号："+i+"  线段数："+rmlst.size());//每组首行是 “房间编号  房间含有的线段数”
       bfw.newLine();
       bfw.flush();
       bfw.write("PLINE ");
       StringBuffer str=new StringBuffer("PLINE ");
       for(int j=0;j<rmlst.size();++j){//下面每行是 “线编号 起点x坐标 y坐标 终点x坐标 y坐标”
         Line templn=zhlns.get(rmlst.get(j));
         Point tempqd=templn.qd;
         Point tempzd=templn.zd;
         int bz=rmbz.get(j);//当前线段进行下一次后继时的邻接点，初始为ZHONGDIAN
         if(bz==ZHONGDIAN){
           tempqd=templn.zd;
           tempzd=templn.qd;
         }
         //System.out.println("线"+j+", "+tempqd.x+","+tempqd.y+" "+tempzd.x+","+tempzd.y);
         
         fjzb[j*4]=tempqd.x;
         fjzb[j*4+1]=tempqd.y;
         //fjzb[j*4+1]=tempqd.y-pylen*2;//y坐标向下偏移两个pylen距离。
         fjzb[j*4+2]=tempzd.x;
         fjzb[j*4+3]=tempzd.y;
         //fjzb[j*4+3]=tempzd.y-pylen*2;
         //if(j==0) { fjzb[fjlen-1]=tempqd.y; fjzb[fjlen-2]=tempqd.x;}//重复首点
         
         bfw.write(tempqd.x+","+tempqd.y+" "+tempzd.x+","+tempzd.y+" ");
         str.append(tempqd.x+","+tempqd.y+" "+tempzd.x+","+tempzd.y+" ");
         if((j+1)%6==0) { bfw.newLine(); bfw.write(" "); str.append(" \n ");}
       }//for-j
       
       JGeometry geo=JGeometry.createLinearPolygon(fjzb, 2, zbsrid);
//       store("room",geo,i);
       
       //System.out.println(str);
       bfw.newLine();
       bfw.flush();
     }//for-i
     bfw.close();
     System.out.println("房间个数："+roomCandidates.size());
     
     //以IndoorSTG方式存储房间
     //store_room_stg(roomCandidates);
   }
    
    public static void save_doorsteps(List<Integer> lst,List<Line> zhlns) throws IOException, InstantiationException, IllegalAccessException, SQLException{
      File file=new File("doorstepszb.txt"); 
      FileWriter fw=new FileWriter(file);
      BufferedWriter bfw=new BufferedWriter(fw);
      for(int i=0;i<lst.size();++i){
        bfw.write(""+i);
        Line ln=zhlns.get(lst.get(i));
        double midx=(ln.qd.x+ln.zd.x)/2;
        double midy=(ln.qd.y+ln.zd.y)/2;
        bfw.write(" "+midx+","+midy+",0");
        bfw.flush();
      }
      bfw.close();
      
        //存入数据库
      for(int i=0;i<doorsteps.size();++i){
        Line ln=doorsteps.get(i);
        double zb[]=new double[2];
        zb[0]=(ln.qd.x+ln.zd.x)/2;
        zb[1]=(ln.qd.y+ln.zd.y)/2;
        //zb[3]=0;
        JGeometry geo=JGeometry.createPoint(zb, 2, zbsrid);
        store("door",geo,i);
      }
    }
    
    public static void save_topo(List<List<Integer>> lst) throws IOException{
      File file=new File("topo.txt"); 
      FileWriter fw=new FileWriter(file);
      BufferedWriter bfw=new BufferedWriter(fw);
      for(int i=0;i<lst.size();++i){
        List<Integer> templst=lst.get(i);
        bfw.write(""+i);
        for(int j=0;j<templst.size();++j){
          bfw.write(" "+templst.get(j));
        }
        bfw.newLine();
        bfw.flush();
      }
      bfw.close();
    }
    
    public static boolean bi_search(List<Integer> lst,int num,List<Integer> tlst){//折半查找。然后在tlst中返回lst中所有等于num值的索引号
      int mid=-1;
      int low=0;
      int high=lst.size()-1;
      while(low<=high){
        mid=(low+high)/2;
        if(lst.get(mid) ==num) break;
        else if(lst.get(mid) >num){
          high=mid-1;
        }
        else low=mid+1;
      }
      if(low>high) return false;//没找到
      tlst.add(mid);//mid索引处是要找的，然后在mid索引的前后再找有没有
      for(int i=mid-1;i>0;--i){
        if(lst.get(i)==num) tlst.add(i);
        else break;
      }
      for(int i=mid+1;i<lst.size();++i){
        if(lst.get(i)==num) tlst.add(i);
        else break;
      }
      return true;
    }
    
    /*public static void store_room_stg(List<List<Integer>> roomCandidates){//以IndoorSTG的方式存储房间 
        int floor=0;
        int type=0;//0是房间，4是边界
        boolean iscontext=false;
        Color fillc=null, borderc=Color.BLACK;
        int cx=100;
        int cy=100;
        int cw=500;
        int ch=400;
        //Graph tgraph=new Graph(floor, type, iscontext, fillc, borderc, cx, cy, cw, ch, "");
        //Graph t2graph=new Graph(floor,4,iscontext,fillc,borderc,600,100,200,0,"");
        //Graph t3graph=new Graph(floor,4,iscontext,fillc,borderc,0,0,50,0,"");//type为4的时候，即线段，依次为x1,y1,x2,y2
        //Graph t4graph=new Graph(floor,4,iscontext,fillc,borderc,50,0,50,50,"");
        //Graph t5graph=new Graph(floor,4,iscontext,fillc,borderc,50,50,0,50,"");
        //Graph t6graph=new Graph(floor,4,iscontext,fillc,borderc,0,50,0,0,"");
        ArrayList<Graph> tgraphs = new ArrayList<Graph>();
        Graph tgraph=null;
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
        }
        //int x = jfc.showSaveDialog(envirSet);
        //if (x == JFileChooser.APPROVE_OPTION) {
            //File f = jfc.getSelectedFile();
            File f=new File("test-floor1");
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
                oos.writeObject(tgraphs);
                oos.flush();
                oos.close();
            } catch (Exception e) {
            }
    }*/
    
   public static void post_process4(List<List<Integer>> rmcddts,List<List<Integer>> rmbz,double mianji,List<Line> zhlns){
      //构造所有房间的最小外包矩形   优化：可以在房间构造过程中构造
      List<List<Double>> ret=new LinkedList();//所有房间的房间外包矩形
      for(int i=0;i<rmcddts.size();++i){
        List<Integer> fj=rmcddts.get(i);
        List<Double> fjret=new ArrayList();//房间fj的minx、maxx、miny、maxy
        double maxx=-10000000000.0;
        double minx=10000000000.0;
        double maxy=-10000000000.0;
        double miny=10000000000.0;
        for(int j=0;j<fj.size();++j){
          Line templn=zhlns.get(fj.get(j));
          Point tempqd=templn.qd;
          if(tempqd.x>maxx) maxx=tempqd.x;
          if(tempqd.x<minx) minx=tempqd.x;
          if(tempqd.y>maxy) maxy=tempqd.y;
          if(tempqd.y<miny) miny=tempqd.y;
          
          Point tempzd=templn.zd;
          if(tempzd.x>maxx) maxx=tempzd.x;
          if(tempzd.x<minx) minx=tempzd.x;
          if(tempzd.y>maxy) maxy=tempzd.y;
          if(tempzd.y<miny) miny=tempzd.y;
        }
        
        //过滤面积小于给定阈值的候选。
        if((maxx-minx)*(maxy-miny)<mianji){
          rmcddts.remove(i);
          rmbz.remove(i);
          i--;
          continue;
        }
        
        fjret.add(minx);
        fjret.add(maxx);
        fjret.add(miny);
        fjret.add(maxy);
        ret.add(fjret);
      }
      
      //通过比较最小外包矩形，有相同最小外包矩形的视为相同房间，保留边数较多的
      for(int i=0;i<rmcddts.size();++i){
        List<Integer> fj1=rmcddts.get(i);
        List<Double> fjret1=ret.get(i);
        for(int j=i+1;j<rmcddts.size();++j){
          List<Integer> fj2=rmcddts.get(j);
          List<Double> fjret2=ret.get(j);
          if(fjretsame(fjret1,fjret2)){
              if(fj1.size()>fj2.size()){
                rmcddts.remove(j);
                rmbz.remove(j);
                ret.remove(j);
                j--;
                continue;
              }
              else{
                rmcddts.remove(i);
                rmbz.remove(i);
                ret.remove(i);
                i--;
                break;
              }//else
          }//if-fjretsame
        }//for-j
      }//for-i
      /*代码OK//保存编号，之后统一删除。是为了防止过早的删除，导致它不能屏蔽其他房间（这种情况主要是在之前用新点数来判断的时候，会出现大房间屏蔽走廊的情况。用矩形判断好像不会出现）
      List<Integer> bh=new ArrayList();//保存要删的房间编号
      for(int i=0;i<rmcddts.size();++i){
        List<Point> fj1=rmcddts.get(i);
        List<Double> fjret1=ret.get(i);
        for(int j=i+1;j<rmcddts.size();++j){
          List<Point> fj2=rmcddts.get(j);
          List<Double> fjret2=ret.get(j);
          if(fjretsame(fjret1,fjret2)){//最小外包矩形相同
            if((fj1.size())<=(fj2.size())){
              if(bh.contains(j)==false) bh.add(j);
              //System.out.println(j);
              //if(containnum(bh,j)==false) bh.add(new Integer(j));
            }
            else{
              if(bh.contains(i)==false) bh.add(i);
              //System.out.println(i);
              //if(containnum(bh,i)==false) bh.add(new Integer(i));
            }
          }
        }
      }
      
      //按保存的要删房间编号，排序后，倒序删除
      for(int i=0;i<bh.size();++i)
        System.out.print(bh.get(i)+" ");
      System.out.println("");
      Collections.sort(bh);
      for(int i=0;i<bh.size();++i)
        System.out.print(bh.get(i)+" ");//这里打印get(i)的，却写成了i
      System.out.println("");
      for(int i=bh.size()-1;i>=0;--i){
        rmcddts.remove((int)bh.get(i));//!!!要死了，这里删除的是bh.get(i)，不是i  卡了好久
        ret.remove((int)bh.get(i));
        doornum.remove((int)bh.get(i));
      }*/
      
      //删除组合多边形
      //不构建新矩形，直接比较，这样的话利用retcontain函数的话，可以和上一个去重循环合并了。
      for(int i=0;i<ret.size();++i){
        List<Double> ret1=ret.get(i);
        for(int j=i+1;j<ret.size();++j){
          List<Double> ret2=ret.get(j);
          int res=retcontain(ret1,ret2);
          if(res==1){
            rmcddts.remove(j);
            rmbz.remove(j);
            ret.remove(j);
            j--;
          }
          else if(res==2){
            rmcddts.remove(i);
            rmbz.remove(i);
            ret.remove(i);
            i--;
            break;
          }
        }
      }
    }
    
    public static void post_process3(List<List<Integer>> rmcddts,List<List<Integer>> rmbz,List<Integer> doornum,double mianji,List<Line> zhlns){
      //构造所有房间的最小外包矩形   优化：可以在房间构造过程中构造
      List<List<Double>> ret=new LinkedList();//所有房间的房间外包矩形
      for(int i=0;i<rmcddts.size();++i){
        List<Integer> fj=rmcddts.get(i);
        List<Double> fjret=new ArrayList();//房间fj的minx、maxx、miny、maxy
        double maxx=-10000000000.0;
        double minx=10000000000.0;
        double maxy=-10000000000.0;
        double miny=10000000000.0;
        for(int j=0;j<fj.size();++j){
          Line templn=zhlns.get(fj.get(j));
          Point tempqd=templn.qd;
          if(tempqd.x>maxx) maxx=tempqd.x;
          if(tempqd.x<minx) minx=tempqd.x;
          if(tempqd.y>maxy) maxy=tempqd.y;
          if(tempqd.y<miny) miny=tempqd.y;
          
          Point tempzd=templn.zd;
          if(tempzd.x>maxx) maxx=tempzd.x;
          if(tempzd.x<minx) minx=tempzd.x;
          if(tempzd.y>maxy) maxy=tempzd.y;
          if(tempzd.y<miny) miny=tempzd.y;
        }
        
        //过滤面积小于给定阈值的候选。
        if((maxx-minx)*(maxy-miny)<mianji){
          rmcddts.remove(i);
          rmbz.remove(i);
          i--;
          continue;
        }
        
        fjret.add(minx);
        fjret.add(maxx);
        fjret.add(miny);
        fjret.add(maxy);
        ret.add(fjret);
      }
      
      //通过比较最小外包矩形，有相同最小外包矩形的视为相同房间，保留点数较小的
      //用外包矩形而不是新点数来判断是否相同，不会出现如大图里的右下角大房间屏蔽走廊的情况。所以可以直接删，而不是存编号  比存编号统一删节省太多时间，对"右下角简单封闭.dxf"一个七分多种，一个三百三十多分钟
      for(int i=0;i<rmcddts.size();++i){
        List<Integer> fj1=rmcddts.get(i);
        List<Double> fjret1=ret.get(i);
        for(int j=i+1;j<rmcddts.size();++j){
          List<Integer> fj2=rmcddts.get(j);
          List<Double> fjret2=ret.get(j);
          if(fjretsame(fjret1,fjret2)){
            int dn1=doornum.get(i);
            int dn2=doornum.get(j);
            if(dn1>dn2){//保留门槛数多的——引导像走廊之类的选择经过门比较多的
              rmcddts.remove(j);
              rmbz.remove(j);
              ret.remove(j);
              doornum.remove(j);
              j--;
              continue;
            }
            else if(dn1<dn2){
              rmcddts.remove(i);
              rmbz.remove(i);
              ret.remove(i);
              doornum.remove(i);
              i--;
              break;
            }
            else{//门槛数相同，保留线段数比较少的——门槛数也相同，可能是同一房间，方便起见，保留线段数少的
              if(fj1.size()<=fj2.size()){
                rmcddts.remove(j);
                rmbz.remove(j);
                ret.remove(j);
                doornum.remove(j);
                j--;
                continue;
              }
              else{
                rmcddts.remove(i);
                rmbz.remove(i);
                ret.remove(i);
                doornum.remove(i);
                i--;
                break;
              }//else
            }//else
          }//if-fjretsame
        }//for-j
      }//for-i
      /*代码OK//保存编号，之后统一删除。是为了防止过早的删除，导致它不能屏蔽其他房间（这种情况主要是在之前用新点数来判断的时候，会出现大房间屏蔽走廊的情况。用矩形判断好像不会出现）
      List<Integer> bh=new ArrayList();//保存要删的房间编号
      for(int i=0;i<rmcddts.size();++i){
        List<Point> fj1=rmcddts.get(i);
        List<Double> fjret1=ret.get(i);
        for(int j=i+1;j<rmcddts.size();++j){
          List<Point> fj2=rmcddts.get(j);
          List<Double> fjret2=ret.get(j);
          if(fjretsame(fjret1,fjret2)){//最小外包矩形相同
            if((fj1.size())<=(fj2.size())){
              if(bh.contains(j)==false) bh.add(j);
              //System.out.println(j);
              //if(containnum(bh,j)==false) bh.add(new Integer(j));
            }
            else{
              if(bh.contains(i)==false) bh.add(i);
              //System.out.println(i);
              //if(containnum(bh,i)==false) bh.add(new Integer(i));
            }
          }
        }
      }
      
      //按保存的要删房间编号，排序后，倒序删除
      for(int i=0;i<bh.size();++i)
        System.out.print(bh.get(i)+" ");
      System.out.println("");
      Collections.sort(bh);
      for(int i=0;i<bh.size();++i)
        System.out.print(bh.get(i)+" ");//这里打印get(i)的，却写成了i
      System.out.println("");
      for(int i=bh.size()-1;i>=0;--i){
        rmcddts.remove((int)bh.get(i));//!!!要死了，这里删除的是bh.get(i)，不是i  卡了好久
        ret.remove((int)bh.get(i));
        doornum.remove((int)bh.get(i));
      }*/
      
      //删除组合多边形
      //不构建新矩形，直接比较，这样的话利用retcontain函数的话，可以和上一个去重循环合并了。
      for(int i=0;i<ret.size();++i){
        List<Double> ret1=ret.get(i);
        for(int j=i+1;j<ret.size();++j){
          List<Double> ret2=ret.get(j);
          int res=retcontain(ret1,ret2);
          if(res==1){
            rmcddts.remove(j);
            rmbz.remove(j);
            ret.remove(j);
            doornum.remove(j);
            j--;
          }
          else if(res==2){
            rmcddts.remove(i);
            rmbz.remove(i);
            ret.remove(i);
            doornum.remove(i);
            i--;
            break;
          }
        }
      }
      
      /*代码OK//用上述ret去构造新的ret，用以过滤组合多边形
      List<List<Double>> newret=new ArrayList();//由上述ret去构造新的ret，用以过滤组合多边形
      for(int i=0;i<ret.size();++i){
        List<Double> ret1=ret.get(i);
        for(int j=i+1;j<ret.size();++j){
          List<Double> ret2=ret.get(j);
          List<Double> nret=constructret(ret1,ret2);
          newret.add(nret);
        }
      }
      //在新矩形里看是否能找到之前的房间矩形
      for(int i=0;i<ret.size();++i){
        List<Double> ret1=ret.get(i);
        for(int j=0;j<newret.size();++j){
          List<Double> ret2=newret.get(j);
          if(fjretsame(ret1,ret2)){
            rmcddts.remove(i);
            ret.remove(i);
            i--;
            break;
          }
        }
      }*/
      
      /*找新点方法，会有问题
      bh.clear();
      for(int i=0;i<rmcddts.size();++i){
        List<Point> fj1=rmcddts.get(i);
        for(int j=i+1;j<rmcddts.size();++j){
          List<Point> fj2=rmcddts.get(j);
          int res=fjcontain(fj1,fj2);
          if(res==0) continue;
          else if(res==1){
            System.out.println(i+" in "+j);
            if(bh.contains(j)==false) bh.add(j);//要删的房间编号是包含的那个，不是被包含的那个 
          }
          else if(res==2){
            System.out.println(j+" in "+i);
            if(bh.contains(i)==false) bh.add(i);
          }
          else{//返回-1两个房间近似相等，因为在之前的步骤以及去重过了，这样就很奇怪
            System.out.println("!!!WARNING: THE TWO ROOMS ARE APPROXIMATELY EQUAL!!!--------------------------------------------");
          }
        }
      }
      
      for(int i=0;i<bh.size();++i)
        System.out.print(bh.get(i)+" ");
      System.out.println("");
      Collections.sort(bh);
      for(int i=bh.size()-1;i>=0;--i){
        rmcddts.remove((int)bh.get(i));//!!!前面才改过，这里又写成了i。。。
        doornum.remove((int)bh.get(i));
        ret.remove((int)bh.get(i));
      }*/
    }
    
    public static int retcontain(List<Double> ret1,List<Double> ret2){//返回0代表无关系，1代表ret1被包含，2代表ret2被包含，-1代表相同 
      //最小外包矩形有三个值相同视为包含关系
      double minx1=ret1.get(0),maxx1=ret1.get(1),miny1=ret1.get(2),maxy1=ret1.get(3);
      double minx2=ret2.get(0),maxx2=ret2.get(1),miny2=ret2.get(2),maxy2=ret2.get(3);
      if(aqldouble(minx1,minx2) && aqldouble(miny1,miny2) && aqldouble(maxy1,maxy2)){//左半部分相同
        if(aqldouble(maxx1,maxx2)) return -1;
        else if(maxx1<maxx2) return 1;
        else return 2;
      }
      else if(aqldouble(minx1,minx2) && aqldouble(maxy1,maxy2) && aqldouble(maxx1,maxx2)){//上半部分相同
        if(aqldouble(miny1,miny2)) return -1;
        else if(miny1<miny2) return 2;
        else return 1;
      }
      else if(aqldouble(maxy1,maxy2) && aqldouble(maxx1,maxx2) && aqldouble(miny1,miny2)){//右半部分相同
        if(aqldouble(minx1,minx2)) return -1;
        else if(minx1<minx2) return 2;//注意思考:被包含的是ret2而不是ret1
        else return 1;
      }
      else if(aqldouble(minx1,minx2) && aqldouble(maxx1,maxx2) && aqldouble(miny1,miny2)){//下半部分相同
        if(aqldouble(maxy1,maxy2)) return -1;
        else if(maxy1<maxy2) return 1;
        else return 2;
      }
      else return 0;
        
      //  
      /*if(aqldouble(minx1,minx2) && aqldouble(miny1,miny2)){//左边相同
        if(maxx1<maxx2 && maxy1<maxy2) return 1;
        else if(maxx1>maxx2 && maxy1>maxy2) return 2;
        else return 0;
      }
      else if(aqldouble(miny1,miny2) && aqldouble(maxy1,maxy2)){//上边相同
        if(minx1<minx2 && maxx1<maxx2) return 1;
        else if(minx1>minx2 && maxx1>maxx2) return 2;
        else return 0;
      }
      else if(aqldouble(maxy1,maxy2) && aqldouble(maxx1,maxx2)){//右边相同
        if(miny1<miny2 && minx1<minx2) return 1;
        else if(miny1>miny2 && minx1>minx2) return 2;
        else return 0;
      }
      else if(aqldouble(minx1,minx2) && aqldouble(maxx1,maxx2)){//下边相同
        if(miny1<miny2 && maxy1<maxy2) return 1;
        else if(miny1>miny2 && maxy1>maxy2) return 2;
        else return 0;
      }
      else return 0;*/
    }
    
    public static List<Double> constructret(List<Double> ret1,List<Double> ret2){//由两个小矩形构造包含这两个的最小外包矩形
      //本来这里应该判断两个矩形是否邻接，以及怎样邻接。这里没有判断，直接进行了构造
      double minx1=ret1.get(0),maxx1=ret1.get(1),miny1=ret1.get(2),maxy1=ret1.get(3);
      double minx2=ret2.get(0),maxx2=ret2.get(1),miny2=ret2.get(2),maxy2=ret2.get(3);
      //if(aqldouble(minx1,))
      List<Double> nret=new ArrayList();
      nret.add(minx1<minx2?minx1:minx2);
      nret.add(maxx1>maxx2?maxx1:maxx2);
      nret.add(miny1<miny2?miny1:miny2);
      nret.add(maxy1>maxy2?maxy1:maxy2);
      return nret;
    }
    
    public static boolean aqldouble(double x,double y){
      double d=Math.abs(x-y);
      if(d<ypxl) return true;
      else return false;
    }
    
    public static boolean containnum(List<Integer> ilst,int n){//在ilst里是否找到整数n
      for(int i=0;i<ilst.size();++i){
        if((int)ilst.get(i)==n){
          return true;
        }
      }
      return false;
    }
    
    public static int fjcontain(List<Point> fj1,List<Point> fj2){//根据两个房间相互之间的新点个数，与给定阈值比较来判断谁包含谁
    //返回0表示两者无关系，1表示fj1被包含，2表示fj2被包含，0表示两者粗略近似相等
      int cnt1=0;//fj1在fj2中找不到近似点的点个数
      for(int i=0;i<fj1.size();++i){
        Point pnt1=fj1.get(i);
        boolean flag=false;
        for(int j=0;j<fj2.size();++j){
          Point pnt2=fj2.get(j);
          if(aqlpnt(pnt1,pnt2)){//pnt1在fj2中找到近似点
            flag=true;
            break;
          }
        }
        if(flag==false) cnt1++;
      }
      
      int cnt2=0;//fj2在fj1中找不到近似点的点个数
      for(int i=0;i<fj2.size();++i){
        Point pnt1=fj2.get(i);
        boolean flag=false;
        for(int j=0;j<fj1.size();++j){
          Point pnt2=fj2.get(j);
          if(aqlpnt(pnt1,pnt2)){
            flag=true;
            break;
          }
        }
        if(flag==false)  cnt2++;
      }
      
      int len1=fj1.size()/4, len2=fj2.size()/4;//阈值不好设定
      /*if(cnt1>len1 && cnt2>len2) return 0;//两者不同
      else if(cnt1<=len1 && cnt2>len2) return 1;//fj1被包含于fj2(fj2包含fj1) fj1没有足够多的新点，而fj2有足够多的新点
      else if(cnt1>len1 && cnt2<=len2) return 2;//fj2被包含于fj1
      else return -1;//两者近似相等*/
      
      /*//不能通过上述的一个有足够多新点另一个没有足够多新点来判断包含关系，因为可能被包含者没有足够多新点、而包含者也只没有足够多新点
      if(cnt1>len1 && cnt2>len2) return 0;
      else{
        if(fj1.size()<fj2.size()) return 1;//如果两房间中有一个没有足够多的新点，则把点数多的那个视为包含者
        else return 2;
      }*/
      
      if(cnt1==0) return 1;
      if(cnt2==0) return 2;
      return -1;
    } 
   
    public static boolean fjretsame(List<Double> fj1,List<Double> fj2){//比较最小外包矩形
      if(Math.abs(fj1.get(0)-fj2.get(0))<ypxl && Math.abs(fj1.get(1)-fj2.get(1))<ypxl && Math.abs(fj1.get(2)-fj2.get(2))<ypxl && Math.abs(fj1.get(3)-fj2.get(3))<ypxl){
        return true;
      }
      else return false;
    }
    
    public static boolean fangjsame2(List<Point> fj1,List<Point> fj2) throws IOException, IOException, ClassNotFoundException{//判断两个房间是否相同，不保留邻接点版本
      /*if(fj1.size()!=fj2.size()) return false;
      int len=fj2.size();
      for(int i=0;i<len;++i){
        if(aqlpnt(fj1.get(0),fj2.get(i))){//房间1的首点在房间2中的索引
          int cnt=0;//boolean flag=true;
          //正向匹配
          for(int j=1;j<fj1.size();++j){
            if(aqlpnt(fj1.get(j),fj2.get((i+j)%len))==false) { cnt++;}//{ flag=false; break;}//对len取余是%，不是/
          }
          if(cnt<1/3*fj1.size() && cnt<1/3*fj2.size()) return true;//if(flag) return true;
          //反向匹配
          cnt=0;//flag=true;
          for(int j=1;j<fj1.size();++j){
            if(aqlpnt(fj1.get(j),fj2.get((i-j+len)%len))==false) cnt++;//{ flag=false; break;}
          }
          if(cnt<1/3*fj1.size() && cnt<1/3*fj2.size()) return true;//if(flag) return true;
        }
      }
    return false;*/
    /*//有一半个数相同点对
    List<Point> room1=fj1;//deepCopy(fj1);
    List<Point> room2=fj2;//deepCopy(fj2);
    int cnt=0;
    for(int i=0;i<room1.size();++i){
      for(int j=0;j<room2.size();++j){
        if(aqlpnt(room1.get(i),room2.get(j))){
          cnt++;
          room1.remove(i);
          i--;
          room2.remove(j);
          break;
        }
      }
    }
    if(cnt>(room1.size()/2)) return false;
    else return true;*/
    
    //if(Math.abs(fj1.size()-fj2.size())>16) return false;//房间点数相差太大，则认为是不同房间
    //有2/3的点相同
    int cnt1=0;//记录房间1在房间2里没近似点的点个数
    for(int i=0;i<fj1.size();++i){
      boolean flag=false;
      for(int j=0;j<fj2.size();++j){
        if(aqlpnt(fj1.get(i),fj2.get(j))) { flag=true; break;}
      }
      if(flag==false) cnt1++;
    }
    int cnt2=0;
    for(int i=0;i<fj2.size()-1;++i){
      boolean flag=false;
      for(int j=0;j<fj1.size()-1;++j){
        if(aqlpnt(fj2.get(i),fj1.get(j))) { flag=true; break;}
      }
      if(flag==false) cnt2++;
    }
    if((cnt1<fj1.size()/3)||(cnt2<fj2.size()/3)) return true;//两个房间中没近似点的点个数都少于1/3，则认为两房间相同
    else return false;/**/
    //return false;
 }
    
    public static Map<Integer,Line> findsuccessorln(Point p,List<Line> lnset){//在集合lnset中找p的后继线(即ln的后继线,p为ln已经加入多边形的尾点，这个点不一定是ln的终点)，应先在lnset中删除ln自身
      //List<Line> successor=new ArrayList();                  //函数返回的是后继线和它在lnset集合中相应的索引
      Map<Integer,Line> sucmp=new TreeMap(); 
      for(int i=0;i<lnset.size();++i){
        Line templn=lnset.get(i);
        //if(i!=k && pntadjln(ln.zd,templn)){//这个是排除自身版本
        if(pntadjln(p,templn)){//这个没有判断是否为自身，在调用该函数前先删除自身
          sucmp.put(i,templn);
        }
      }
      return sucmp;
    }
    
    public static void Extract_Stairs() throws IOException{
      List<List<Line>> strs=new LinkedList();//内部的List是一个楼梯的线集合
      for(int i=0;i<stairlns.size();++i)
      {
        List<Line> ncu=new ArrayList();//一个楼梯集合或簇
        Line ln1=stairlns.get(i);
        ncu.add(ln1);
        for(int j=i+1;j<stairlns.size();++j)//从stairlns集合中找和ln1线相交的线，构成集合culst
        {
          Line ln2=stairlns.get(j);
          if(parallelln(ln1,ln2))//平行线
          {//对于平行线，看是否包含，以及距离够近
            if((parallel_contain(ln1,ln2)||parallel_contain(ln2,ln1))==false) continue;//如果没有包含关系，则不是一类
            double dis=ln1.distoln(ln2);
            if(dis<stair_jsjl)
            { ncu.add(ln2);}
          }
          else
          {//对非平行线，看是否相交
            Point jd=inter_point(ln1,ln2);//两线交点
            //if(jd==null) continue;平行则无交点
            if(is_online(jd,ln1,jsjl/10) && is_online(jd,ln2,jsjl/10))//交点在两个线上，即两线相交。这里近似量不能选择jsjl，否则可能将两个距离为jsjl的楼梯识别为一个了。
            {
              ncu.add(ln2);
              /*stairlns.remove(j);  这里不能remove，因为remove掉了，ln2就不能去找相交线了
            j--;
            i--;*/
            }
          }
        }
        //新簇是否和旧簇同类
        boolean fg=false;//新簇ncu的线 是否和 已存在的簇中的线 相交
        for(int j=0;j<ncu.size();++j)//对于新簇，遍历每一个线，看是否与以前的簇有相交，如果有，则合并
        {
          Line ln3=ncu.get(j);
          boolean fg2=false;//ln3与已存在的簇是否相交
          List<Line> cur_cu=ncu;//当新簇与多个簇相交时，应该把cur_cu加入到发现的相交的老簇中    当前簇，始终指向合并了ncu的那个簇
          for(int k=0;k<strs.size();++k)//遍历strs集合，即处理每个簇
          {
            List<Line> jcu=strs.get(k);//每个簇
            boolean flag=false;//ln3与cu中线是否相交
            for(int x=0;x<jcu.size();++x)//遍历每个簇
            {
              Line ln4=jcu.get(x);
              if(ln3==ln4)//比如ln2第一次被加入到ln1的簇里，而ln2自己也可以去形成它的相交线簇的。这样,ln2在两个簇里
              {
                flag=true;
                break;
              }
              if(parallelln(ln3,ln4))//平行线
              {//对于平行线，看是否包含，以及距离够近
                if((parallel_contain(ln3,ln4)||parallel_contain(ln3,ln4))==false) continue;//如果没有包含关系，则不是一类
                double dis=ln3.distoln(ln4);
                if(dis<stair_jsjl)
                { flag=true; break;}
              }
              else
              {//对非平行线，看是否相交
                Point jd=inter_point(ln3,ln4);
                if(jd==null) continue;
                if(is_online(jd,ln3,jsjl/10) && is_online(jd,ln4,jsjl/10))//两线相交
                {
                  flag=true;
                  break;
                }
              }
            }
            if(flag)
            {
              //cu.addAll(cur_cu);  这里不能简单地addAll，因为存在重复的线。当然了这对外包矩形没有影响
              for(int y=0;y<cur_cu.size();++y)
              {
                Line templn=cur_cu.get(y);
                if(jcu.contains(templn)==false)
                  jcu.add(templn);
              }
              if(cur_cu!=ncu)//当前簇cur_cu已经合并到了jcu中，如果当前簇不是ncu的话，在应将其从簇集合strs中删除
              {
                strs.remove(cur_cu);
                k--;
              }
              fg2=true;
              //break;  这里不能break，因为可能新簇与多个已存在的簇相交
              cur_cu=jcu;
            }
          }
          if(fg2)
          {
            fg=true;
            break;  //这里似乎和flag一样不应该break
          }
        }
        if(fg==false) strs.add(ncu);//如果新簇和已存在的簇没有相交线，则加到strs集合
      }
      
      /*由于前面的聚类时，考虑了平行线按包含关系和近距离聚到一起，所以不存在下面考虑的情况了。
      //由于有的楼梯画作一组平行线，而无连接它们的线。所以这里搜集所有大小为1的簇
      List<Line> sgllns=new LinkedList();
      for(int i=0;i<strs.size();++i)//遍历每个簇
      {
        List<Line> tempstr=strs.get(i);
        if(tempstr.size()==1) sgllns.add(tempstr.get(0));
      }
      //对sgnlns聚类，标准是：平行线且距离足够近的为一类   聚类的过程和上面的类似，只不过标准不太一样
      List<List<Line>> cus=new LinkedList();//聚类后簇的集合
      for(int i=0;i<sgllns.size();++i)
      {
        List<Line> ncu=new ArrayList();//新簇
        Line ln1=sgllns.get(i);
        ncu.add(ln1);
        for(int j=i+1;j<sgllns.size();++j)
        {
          Line ln2=sgllns.get(j);
          if(parallelln(ln1,ln2) && ln1.distoln(ln2)<stair_jsjl)//成员函数distoln是计算到另一平行线的距离
          {
            ncu.add(ln2);
          }
        }
        //新簇和旧簇比较
        boolean fg3=false;//新簇是否与cus中已存在的簇属于一类
        for(int j=0;j<ncu.size();++j)//遍历新簇
        {
          Line ln3=ncu.get(j);
          List<Line> cur_cu=ncu;
          boolean fg2=false;//ln3是否与cus中已存在的簇属于一类
          for(int k=0;k<cus.size();++k)
          {
            List<Line> jcu=cus.get(k);//旧簇
            boolean fg=false;//ln3是否与jcu属于一类
            for(int y=0;y<jcu.size();++y)//遍历旧簇
            {
              Line ln4=jcu.get(y);
              if(ln3==ln4) { fg=true; break;}
              if(parallelln(ln3,ln4)==false) break;//如果不平行，则这个簇整个都不会与ln3平行
              if(ln3.distoln(ln4)<stair_jsjl)//满足要求，合并两个簇
              {
                fg=true;
                break;
              }
            }
            if(fg)
            {
              for(int x=0;x<cur_cu.size();++x)
              {
                Line ln4=cur_cu.get(x);
                if(jcu.contains(ln4)==false)  jcu.add(ln4);
              }
              cur_cu=jcu;//新簇已合并到了jcu中，cur_cu始终指向新簇所合并到的簇
              fg2=true;
            }
          }
          if(fg2==true)//两个=号啊！！！
          {
            fg3=true;
            break;
          }
        }
        if(fg3==false) cus.add(ncu);
      }
      
      strs.addAll(cus);//将由上面单线集合聚类而来的簇加入*/
      
      //区别出电梯。判断电梯的标准：有一对 相交且交点平分它们自己 的线。（即矩形的对角线）
      List<Integer> lftnm=new ArrayList();//电梯在strs中的序号集合
      for(int i=0;i<strs.size();++i)
      {
        List<Line> tempstr=strs.get(i);
        boolean flag2=false;//标记tempstr是否为电梯
        for(int j=0;j<tempstr.size();++j)//遍历楼梯
        {
          Line ln1=tempstr.get(j);
          boolean flag=false;//标记当前线是否找到一个合要求的另一线
          for(int k=j+1;k<tempstr.size();++k)
          {
            Line ln2=tempstr.get(k);
            Point jd=inter_point(ln1,ln2);
            if(jd==null) continue;//两线平行，无交点
            if(is_online(jd,ln1,0) && is_online(jd,ln2,0))//两线相交
            {
              double dist1q=jd.distance(ln1.qd);
              double dist1z=jd.distance(ln1.zd);
              if(aproximately_equal_double(dist1q,dist1z,1)==false)//交点不平分线ln1
                  continue;
              double dist2q=jd.distance(ln2.qd);
              double dist2z=jd.distance(ln2.zd);
              if(aproximately_equal_double(dist2q,dist2z,1))//交点平分线ln2
              {
                flag=true;
                System.out.println("电梯:");
                System.out.println(dist1q+" "+dist1z+" "+dist2q+" "+dist2z);
                break;
              }
            }
          }
          if(flag)
          {
            flag2=true;
            break;
          }
        }
        if(flag2) lftnm.add(i);
      }
      
      //构造每个簇的外包矩形
      List<List<Double>> strmbr=new LinkedList();
      List<List<Double>> lftmbr=new LinkedList();//因为下面有一个少于4根线就跳过的步骤，所以不能把电梯的mbr也存入strmbr，然后用下标取出。那个步骤让下标已经对应不上。
      for(int i=0;i<strs.size();++i)//遍历每个簇
      {
        List<Line> tempstr=strs.get(i);
        if(tempstr.size()<4) continue;//默认少于4条线不构成一个楼梯
        double minx=1000000000.0,miny=1000000000.0,maxx=-1000000000.0,maxy=-1000000000;//最大最小值初值的设置
        for(int j=0;j<tempstr.size();++j)
        {
          Line templn=tempstr.get(j);
          if(templn.qd.x<minx) minx=templn.qd.x;
          if(templn.zd.x<minx) minx=templn.zd.x;
          if(templn.qd.x>maxx) maxx=templn.qd.x;
          if(templn.zd.x>maxx) maxx=templn.zd.x;
          if(templn.qd.y<miny) miny=templn.qd.y;
          if(templn.zd.y<miny) miny=templn.zd.y;
          if(templn.qd.y>maxy) maxy=templn.qd.y;
          if(templn.zd.y>maxy) maxy=templn.zd.y;
        }
        List<Double> tempmbr=new ArrayList();
        tempmbr.add(minx);
        tempmbr.add(maxx);
        tempmbr.add(miny);
        tempmbr.add(maxy);
        if(lftnm.contains(i)) lftmbr.add(tempmbr);
        else strmbr.add(tempmbr);
      }
      
      //写入到文件
      File file2=new File("电梯.txt");//保存电梯
      FileWriter fw2=new FileWriter(file2);
      BufferedWriter bfw2=new BufferedWriter(fw2);
      for(int i=0;i<lftmbr.size();++i)//遍历每个mbr
      {
        List<Double> tempmbr=lftmbr.get(i);
        double minx=tempmbr.get(0);
        double maxx=tempmbr.get(1);
        double miny=tempmbr.get(2);
        double maxy=tempmbr.get(3);
        bfw2.write("PLINE ");
        bfw2.write(maxx+","+maxy+" "+minx+","+maxy+" "+minx+","+miny+" "+maxx+","+miny+" "+maxx+","+maxy+" ");
        bfw2.newLine();
      }
      bfw2.flush();
      bfw2.close();
      
      File file=new File("楼梯.txt");//保存楼梯
      FileWriter fw=new FileWriter(file);
      BufferedWriter bfw=new BufferedWriter(fw);
      for(int i=0;i<strmbr.size();++i)//遍历每个mbr
      {
        List<Double> tempmbr=strmbr.get(i);
        double minx=tempmbr.get(0);
        double maxx=tempmbr.get(1);
        double miny=tempmbr.get(2);
        double maxy=tempmbr.get(3);
        bfw.write("PLINE ");
        bfw.write(maxx+","+maxy+" "+minx+","+maxy+" "+minx+","+miny+" "+maxx+","+miny+" "+maxx+","+maxy+" ");
        bfw.newLine();
      }
      bfw.flush();
      bfw.close();//没有这两句，竟然文件为空
    }
    
    public static boolean aproximately_equal_double(double da, double db, double yz)
    {//判断db和da是否近似相等，阈值为yz
      return Math.abs(db-da)<=yz;
    }
    
    public static Point inter_point(Line ln1,Line ln2){//相比于jiaodian函数，这是普通地计算两条线的交点，如果无交点则返回null
      //if(Math.abs(ln1.qd.x-ln1.zd.x)<0.001 && Math.abs(ln2.qd.x-ln2.zd.x)<0.001) return null;//两条竖直线
      if(parallelln(ln1,ln2)) return null;//平行线无交点
      if(Math.abs(ln1.qd.x-ln1.zd.x)<0.001){//ln1是竖直线，则ln2不是竖直线了
        double xl2=ln2.getxl();
        double b2=ln2.getb();
        double y=xl2*ln1.qd.x+b2;
        return new Point(ln1.qd.x,y);
      }
      if(Math.abs(ln2.qd.x-ln2.zd.x)<0.001){
        double xl1=ln1.getxl();
        double b1=ln1.getb();
        double y=xl1*ln2.qd.x+b1;
        return new Point(ln2.qd.x,y);
      }
      //没有竖直线
      double k1=ln1.getxl();
      double b1=ln1.getb();
      double k2=ln2.getxl();
      double b2=ln2.getb();
      //if(k1==k2) return null;//平行线无交点，这里k1、k2的相等比较是否要引入一个近似量
      double x=(b2-b1)/(k1-k2);
      double y=k1*x+b1;
      return new Point(x,y);
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
       // TODO code application logic here
       long startTime=System.currentTimeMillis();   //获取开始时间
       //fileName="C:\\\\Users\\\\User\\\\Documents\\\\NetBeansProjects\\\\readdxfmy1\\data\\test.dxf";
       /*fileName="C:\\Users\\hello\\Documents\\NetBeansProjects\\readdxfmy1\\data\\教学楼-右1.dxf";
       ypxl=550;//300;近似数
       jsjl=240+5;//300;//平行线间的可近似距离，一般为墙宽度
       szxpc=5;//竖直线的偏差
       mentc.add("WINDOW");
       fangjtc.add("WINDOW");
       //fangjtc.add("COLUMN");
       fangjtc.add("WALL");
       strtc.add("STAIR");*/
       
       /*fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxfmy1\\data\\教学楼-右2.dxf";
       ypxl=550;//300;近似数
       jsjl=370+5;//300;//平行线间的可近似距离，一般为墙宽度
       szxpc=5;//竖直线的偏差
       mentc.add("WINDOW");
       fangjtc.add("WINDOW");
       //fangjtc.add("COLUMN");
       fangjtc.add("WALL");*/
       
       /*fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxfmy1\\data\\教学楼-中1.dxf";
       ypxl=550;//300;近似数
       jsjl=370+5;//300;//平行线间的可近似距离，一般为墙宽度
       szxpc=5;//竖直线的偏差
       mentc.add("WINDOW");
       fangjtc.add("WINDOW");
       //fangjtc.add("COLUMN");
       fangjtc.add("WALL");*/
       
       /*fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxfmy1\\data\\教学楼-中2.dxf";
       ypxl=550;//300;近似数
       jsjl=370+5;//300;//平行线间的可近似距离，一般为墙宽度
       szxpc=5;//竖直线的偏差
       mentc.add("WINDOW");
       fangjtc.add("WINDOW");
       //fangjtc.add("COLUMN");
       fangjtc.add("WALL");*/
       
       /*fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxfmy1\\data\\教学楼-中3.dxf";
       ypxl=550;//300;近似数
       jsjl=370+5;//300;//平行线间的可近似距离，一般为墙宽度
       szxpc=5;//竖直线的偏差
       mentc.add("WINDOW");
       fangjtc.add("WINDOW");
       //fangjtc.add("COLUMN");
       fangjtc.add("WALL");*/
       
       /*fileName="C:\\Users\\hello\\Documents\\NetBeansProjects\\readdxfmy1\\data\\教学楼-左1.dxf";
       ypxl=550;//300;近似数
       jsjl=370+5;//300;//平行线间的可近似距离，一般为墙宽度
       szxpc=5;//竖直线的偏差
       mentc.add("WINDOW");
       //fangjtc.add("WINDOW");
       //fangjtc.add("COLUMN");
       fangjtc.add("WALL");
       strtc.add("STAIR");
       stair_jsjl=550;*/
       
       /*fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxfmy1\\data\\教学楼-左2.dxf";
       ypxl=550;//300;近似数
       jsjl=370+5;//300;//平行线间的可近似距离，一般为墙宽度
       szxpc=5;//竖直线的偏差
       mentc.add("WINDOW");
       fangjtc.add("WINDOW");
       //fangjtc.add("COLUMN");
       fangjtc.add("WALL");*/
       
       /*fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxfmy1\\data\\教学楼-左3.dxf";
       ypxl=550;//300;近似数
       jsjl=370+5;//300;//平行线间的可近似距离，一般为墙宽度
       szxpc=5;//竖直线的偏差
       mentc.add("WINDOW");
       fangjtc.add("WINDOW");
       //fangjtc.add("COLUMN");
       fangjtc.add("WALL");*/
       
       /*fileName="C:\\Users\\User\\Documents\\NetBeansProjects\\readdxfmy1\\data\\某宾馆平面图-1.dxf";
       ypxl=550;//300;近似数
       jsjl=240+5;//300;//平行线间的可近似距离，一般为墙宽度
       szxpc=5;//竖直线的偏差
       mentc.add("WINDOW");
       fangjtc.add("WINDOW");
       //fangjtc.add("COLUMN");
       fangjtc.add("WALL");*/
       
       /*fileName="C:\\\\Users\\\\hello\\\\Documents\\\\NetBeansProjects\\\\readdxflunwen\\\\ts斜线.dxf";
       //fileName="C:\\\\Users\\\\hello\\\\Documents\\\\NetBeansProjects\\\\readdxflunwen\\\\ts外包平行四边形.dxf";
       ypxl=550;//300;近似数
       jsjl=340+50;//300;//平行线间的可近似距离，一般为墙宽度
       szxpc=5;//竖直线的偏差
       mentc.add("WINDOW");
       fangjtc.add("WINDOW");
       //fangjtc.add("COLUMN");
       fangjtc.add("WALL");
       strtc.add("STAIR");*/
       
       //fileName="C:\\\\Users\\\\hello\\\\Documents\\\\NetBeansProjects\\\\readdxflunwen\\\\一层平面图.dxf";
       /*fileName="C:\\\\Users\\\\hello\\\\Documents\\\\NetBeansProjects\\\\readdxflunwen\\\\ts楼梯.dxf";
       ypxl=550;//300;近似数
       jsjl=240+50;//300;//平行线间的可近似距离，一般为墙宽度
       szxpc=5;//竖直线的偏差
       mentc.add("WINDOW");
       fangjtc.add("WINDOW");
       fangjtc.add("COLUMN");
       fangjtc.add("WALL");
       strtc.add("STAIR");*/
       
       fileName="C:\\\\Users\\\\hello\\\\Documents\\\\NetBeansProjects\\\\readdxflunwen\\\\电三-三层.dxf";
       ypxl=550;//300;近似数
       jsjl=340+50;//300;//平行线间的可近似距离，一般为墙宽度
       szxpc=5;//竖直线的偏差
       mentc.add("WINDOW");
       fangjtc.add("WINDOW");
       //fangjtc.add("0");
       fangjtc.add("COLUMN");
       fangjtc.add("WALL");
       strtc.add("STAIR");/**/
       
       //stair_jsjl=jsjl;//暂时设为这个值
       
       File file=new File(fileName);
       FileReader fr=new FileReader(file);
       BufferedReader bfr= new BufferedReader(fr);
       System.out.println("here!");  
       String bz=new String("AXIS");//标注线（包括轴网标注的圆以及引伸它的直线）
       String zx=new String("DOTE");//红色轴线
       String bk=new String("加粗线");//最外边的边框矩形
       String tk=new String("图框");//右下角的图信息及作者信息边框
       String mz=new String("PUB_WALL");//图名
       String nb=new String("DIM_ELEV");//内部注释 
       String zs=new String("DIM_SYMB");//
       Scanner reader=new Scanner(System.in);
       /*String temps=null;
       System.out.print("输入标注线图层：");
       if((temps=reader.nextLine()).trim().compareTo("")!=0) bz=new String(temps);
       filter.add(bz);
       System.out.print("输入轴线图层：");
       if((temps=reader.nextLine()).trim().compareTo("")!=0) zx=new String(temps);
       filter.add(zx);
       System.out.print("输入边框线图层：");
       if((temps=reader.nextLine()).trim().compareTo("")!=0) bk=new String(temps);
       filter.add(bk);
       System.out.print("输入右下角信息边框图层：");
       if((temps=reader.nextLine()).trim().compareTo("")!=0) tk=new String(temps);
       filter.add(tk);*/
       
       filter.add(bz);
       filter.add(zx);
       filter.add(bk);
       filter.add(tk);
       filter.add(mz);
       filter.add(nb);
       filter.add(zs);
       
       /*
       while(true){
         System.out.print("输入其他图层或结束(q)：");
         temps=reader.nextLine();
         if(temps.compareTo("q")==0) break;
         if(temps.trim().compareTo("")!=0) filter.add(temps);
       }
       //输入提取门所需图层名
       while(true){
         System.out.print("输入门所在图层或结束(q)：");
         temps=reader.nextLine();
         if(temps.compareTo("q")==0) break;
         if(temps.trim().compareTo("")!=0) mentc.add(temps);
       }
       //输入提取房间所需的图层名
       while(true){
         System.out.print("输入房间所在的图层或结束(q)：");
         temps=reader.nextLine();
         if(temps.compareTo("q")==0) break;
         if(temps.trim().compareTo("")!=0) fangjtc.add(temps);
       }
       //输入墙所在的图层名
       while(true){
         System.out.print("输入墙所在的图层或结束(q)：");
         temps=reader.nextLine();
         if(temps.compareTo("q")==0) break;
         if(temps.trim().compareTo("")!=0) walltc.add(temps);
       }
       //输入误差值
       System.out.print("输入可接受近似值：");
       temps=reader.nextLine();
       ypxl=Double.parseDouble(temps);
       */
       
       //mentc.add("0");
       //mentc.add("WINDOW");
       //fangjtc.add("WINDOW");
       //fangjtc.add("COLUMN");
       //fangjtc.add("WALL");
       //fangjtc.add("0");//晕死了，调试简单精确房间时，一房间的三条边都是WALL图层，另一个是0图层，结果一直少读一条边。。。
       //walltc.add("WALL");
       //walltc.add("COLUMN");
       
       readBlocks(bfr);
       readEntities(bfr);
       //createindex(); //创建索引。  感觉还是可以放在SQL文件里，因为创建数据库表还是要执行SQL文件的。在这里执行，如果索引不存在，drop index句就会异常。
       System.out.println("There!");
       Extract_Stairs();
       Extract_Doors(500,1300,10/Math.PI);
             //my_preprocess(roomlns,roomlns);
       Extract_Rooms();
       System.out.println("tmaxx:"+tmaxx+"\ntmaxy:"+tmaxy+"\ntminx:"+tminx+"\ntminy:"+tminy);
       bfr.close();
       fr.close();
       
       long endTime=System.currentTimeMillis(); //获取结束时间
       System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
    }//main
    
}