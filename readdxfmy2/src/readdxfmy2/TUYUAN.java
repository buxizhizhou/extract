/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package readdxfmy2;
import java.io.Serializable;
import java.lang.Math;
//import org.apache.commons.math3.util.FastMath;
/**
 *
 * @author User
 */


abstract class tuys implements Serializable{
  private static final long serialVersionUID=123L;
  String name;
}

class Point extends tuys{
  double x,y;
  //String name="Point";
  public Point(){
    x=0.0;
    y=0.0;
    name="Point";
  }
  public Point(Point a){
    if(a!=null){
     x=a.x;
     y=a.y;
     name="Point";
    }
  }
  public Point(double a, double b){
    x=a;
    y=b;
    name="Point";
  }
  public void shift(double sx,double sy,double xz,double cx,double cy){//x、y进行缩放，关于原点选择xz弧度,加上插入点（即原点变换后的位置）的横纵坐标
    x=x*sx;                                                            //注意xz是弧度
    y=y*sy;
    double r=Math.sqrt(x*x+y*y);//点（x,y）相对于原点的长度
    double yhd=0;//点相对于原点的原始弧度
    if(x!=0 || y!=0) yhd=Math.acos(x/r);//必须不是原点，即r不会0时，才能除。
    if(y<0) yhd=-1*yhd;//由于a度和-a度的余弦值相同，所以要通过y坐标判断一下。 sin和cos在360度内都不是一一对应的。
    //System.out.print("x/r:");System.out.println(x/r);
    //System.out.print("yhd:");System.out.println(Math.toDegrees(yhd));
    //System.out.print("原始弧度："); System.out.println(yhd);
    //System.out.print("x,y:"); System.out.println(x); System.out.println(y);
    x=r*Math.cos(xz+yhd)+cx;//这里应该用r乘，而不是x或y。。。
    y=r*Math.sin(xz+yhd)+cy;   
    //System.out.print("修改后x,y:"); System.out.println(x); System.out.println(y);
  }
  public double distance(Point a){//计算该点与点a的距离
    double dist=Math.sqrt((x-a.x)*(x-a.x)+(y-a.y)*(y-a.y));
    return dist;
  }
  public double xdhd(Point a){//以该点为坐标原点，点a连接原点，相对于横轴正向的夹角弧度
     double r=distance(a);//半径长
     double hd=0;
     if(a.x!=x || a.y!=y) hd=Math.acos((a.x-x)/r);//必须不是圆心，即r不会0时，才能除。
     if(a.y<y) hd=-1*hd;//若点a在圆心c下方，则度数为负的。
     return hd;
  }
}

//线段类型
class Line extends tuys{
  Point qd,zd;
  double zb[]=new double[4];
  //String name="Line";
  public Line(Point a, Point b){
    if(a!=null && b!=null){
     qd=new Point(a);
     zd=new Point(b);
     //name="Line";
     zb[0]=qd.x;zb[1]=qd.y;zb[2]=zd.x;zb[3]=zd.y;
    }
  }
  public Line(double x1,double y1,double x2,double y2){
    qd=new Point();
    zd=new Point();
    qd.x=x1;
    qd.y=y1;
    zd.x=x2;
    zd.y=y2;
    //name="Line";
    zb[0]=qd.x;zb[1]=qd.y;zb[2]=zd.x;zb[3]=zd.y;
  }
  public Line(Line sl){
    qd=new Point(sl.qd);
    zd=new Point(sl.zd);
    zb[0]=qd.x;zb[1]=qd.y;zb[2]=zd.x;zb[3]=zd.y;
  }
  public void whzb(){//由起点和终点维护坐标对数组
    zb[0]=qd.x;zb[1]=qd.y;zb[2]=zd.x;zb[3]=zd.y;
  }
  public double length(){//计算线段长度
    double len=Math.sqrt((qd.x-zd.x)*(qd.x-zd.x)+(qd.y-zd.y)*(qd.y-zd.y));
    return len;
  }
  public boolean is_vertical(){//是否是竖直的
    double pc=Math.abs(qd.x-zd.x);
    if(pc<10) 
    {
        //System.out.println("is vertical");
        return true;
    }
    else return false;
  }
  public boolean is_horizontal(){//是否为水平线
    double pc=Math.abs(qd.y-zd.y);
    if(pc<10)
    {
        //System.out.println("is horizontal");
        return true;
    }    
    else return false;
  }
  public double getxl(){//计算线段斜率，调用之前应先判断是否为竖直直线
    if(Math.abs(qd.x-zd.x)<0.001) return 10000;//防止没判断竖直直线，返回一个夸张的斜率
    return (qd.y-zd.y)/(qd.x-zd.x);
  }
  public double getb(){//计算直线的截距，即y=kx+b中的b。调用该函数前要先判断是否为竖直线
    double k=getxl();
    return qd.y-k*qd.x;
  }
  public double distoln(Line ln){//计算与另一平行线段间的距离
    if(Math.abs(qd.x-zd.x)<5){//竖直线
      double miny1=qd.y<zd.y?qd.y:zd.y;
      double maxy1=qd.y>zd.y?qd.y:zd.y;
      double miny2=ln.qd.y<ln.zd.y?ln.qd.y:ln.zd.y;
      double maxy2=ln.qd.y>ln.zd.y?ln.qd.y:ln.zd.y;
      //System.out.print(miny1+" "+maxy1+" "+miny2+" "+maxy2+" ");
      double dist=0;
      if(((miny1<=maxy2) && (miny1>=miny2))||((maxy1<=maxy2) && (maxy1>=miny2))||((miny1<=miny2) && (maxy1>=maxy2))){
        dist=Math.abs(qd.x-ln.qd.x);
        //System.out.println("<");
      }
      else{
        //System.out.println();
        double qq=qd.distance(ln.qd);
        double qz=qd.distance(ln.zd);
        double zq=zd.distance(ln.qd);
        double zz=zd.distance(ln.zd);
        if(qq<qz && qq<zq && qq<zz) dist=qq;
        else if(qz<qq && qz<zq && qz<zz) dist=qz;
        else if(zq<qq && zq<qz && zq<zz) dist=zq;
        else dist=zz;
      }
      return dist;
    }
    //非竖直线
    double minx1=qd.x<zd.x?qd.x:zd.x;
    double maxx1=qd.x>zd.x?qd.x:zd.x;
    double minx2=ln.qd.x<ln.zd.x?ln.qd.x:ln.zd.x;
    double maxx2=ln.qd.x>ln.zd.x?ln.qd.x:ln.zd.x;
    double dist=0;
    //下面的if语句第一个条件有误，这里已改，之前的版本没改。
    if(((minx1<=maxx2)&&(minx1>=minx2))||((maxx1<=maxx2)&&(maxx1>=minx2))||((minx1<=minx2)&&(maxx1>=maxx2))){//两平行线有“重叠映射”部分，即可按点到直线距离计算
      double k=getxl();
      dist=Math.abs(getb()-ln.getb())/(Math.sqrt(k*k+1));
    }
    else{//两平行线段没有重叠部分，则计算两组首尾点的四种组合，取最小值
      double qq=qd.distance(ln.qd);
      double qz=qd.distance(ln.zd);
      double zq=zd.distance(ln.qd);
      double zz=zd.distance(ln.zd);
      if(qq<qz && qq<zq && qq<zz) dist=qq;
      else if(qz<qq && qz<zq && qz<zz) dist=qz;
      else if(zq<qq && zq<qz && zq<zz) dist=zq;
      else dist=zz;
    }
    return dist;
  }
}

//多段线类型
class LWpolyline extends tuys{
  Point num[];
  int plen;//点个数，即num数组的长度，闭合的话在构造时这里就重复了第一点
  double zb[];//点的xy坐标对数组，方便存入Oracle数据库
  public LWpolyline(int n,double xy[]){//点个数，坐标x、y数组
    plen=n;
    num=new Point[n];
    zb=new double[n*2];
    for(int i=0;i<n*2;++i){
      zb[i]=xy[i];
      //System.out.println(i);
      num[i/2]=new Point();//num数组中的每个元素还需要new Point   —— thanks for 室友LXK
      num[i/2].x=xy[i];
      ++i;
      zb[i]=xy[i];
      num[i/2].y=xy[i];
   }
   name="LWpolyline";
  }
  public LWpolyline(LWpolyline slw){
    this(slw.plen,slw.zb);
  }
  public void whzb(){//根据Point数组和长度，维护坐标对数组
    for(int i=0;i<plen;++i){
     zb[i*2]=num[i].x;
     zb[i*2+1]=num[i].y;
    }
  }
  public boolean isRect(){//判断是否为矩形
   if(plen!=4) return false;
   if(num[0].x==num[1].x && num[1].y==num[2].y && num[2].x==num[3].x && num[3].y==num[0].y && num[0].y!=num[1].y && num[1].x!=num[2].x && num[2].y!=num[3].y && num[3].x!=num[0].x)
    return true;
   else if(num[0].y==num[1].y && num[1].x==num[2].x && num[2].y==num[3].y && num[3].x==num[0].x && num[0].x!=num[1].x && num[1].y!=num[2].y && num[2].x!=num[3].x && num[3].y!=num[0].y)
    return true;
   else return false;
  }
  public Line[] lwlntoline(){//将多段线转化为等价的线段数组
   Line lns[]=new Line[plen-1];
   for(int i=0;i<plen-1;++i){
    lns[i]=new Line(num[i],num[i+1]);
   }
   return lns;
  }
}

//圆弧类型
class Arc extends tuys{
  Point center;
  double radius,qhd,zhd;//半径、起点弧度，终点弧  
  Point qd,td,zd;//起点、第三点、终点
  double zb[]=new double[6];//点的xy坐标，顺时针存储，CAD中的起点到终点一般为逆时针方向，则应先存终点  //注意可能需要通过构造函数来维护这个数组，如果利用它的话。
  /*public Arc(Point a,Point b,Point c){//起点、第三点、终点顺序初始化
    qd=new Point(a);
    td=new Point(b);
    zd=new Point(c);
    zb=new double[6];
    zb[0]=zd.x;zb[1]=zd.y;zb[2]=td.x;zb[3]=td.y;zb[4]=qd.x;zb[5]=qd.y; //必须以顺时针存   这里顺时针逆时针可能没有关系，因为三个点确定了一个弧，不会有方向差异。
    //zb[0]=qd.x;zb[1]=qd.y;zb[2]=td.x;zb[3]=td.y;zb[4]=zd.x;zb[5]=zd.y;
    name="Arc";
  }*/
  public Arc(Arc sac){
    this(sac.center,sac.radius,sac.qhd,sac.zhd);//构造函数可以相互调用，但得用this而不是函数名——感谢崔大治。。。
  }
  
  public Arc(Point cent,double r,double qds,double zds){
    center=new Point(cent);
    radius=r;
    qhd=qds;
    zhd=zds;
    computertp(center,r,qds,zds);//构造三点
    wh();
  }
  
  final public void computertp(Point cent,double rad,double qds,double zds){//构造三点
    if(zds<qds) zds=zds+Math.PI*2;//如果终点度数小于起点度数，则加上一个2PI，否则对计算第三点时有影响。
    double qx=cent.x+rad*Math.cos(qds);
    double qy=cent.y+rad*Math.sin(qds);
    double zx=cent.x+rad*Math.cos(zds);
    double zy=cent.y+rad*Math.sin(zds);
    //构造第三个点
    double tx=0.0,ty=0.0;
    double tds=(qds+zds)/2;
    double r=Math.sqrt((qx-cent.x)*(qx-cent.x)+(qy-cent.y)*(qy-cent.y));
    tx=r*Math.cos(tds)+cent.x;
    ty=r*Math.sin(tds)+cent.y;
    qd=new Point(qx,qy);
    td=new Point(tx,ty);
    zd=new Point(zx,zy);
  }
  
  final public void wh(){//由三个点维护坐标数组,以及维护其他成员变量
   zb[0]=zd.x;zb[1]=zd.y;zb[2]=td.x;zb[3]=td.y;zb[4]=qd.x;zb[5]=qd.y;
   radius=center.distance(qd);
   qhd=center.xdhd(qd);
   zhd=center.xdhd(zd);
  }
}


class Circle extends tuys{
  Point center;
  double radius;
  //String name="Circle";
  public Circle(Point c,double r){
   center=new Point(c);
   radius=r;
   //name="Circle";
  }
  public Circle(double cx,double cy,double r){
   this(new Point(cx,cy),r);
  }
  public Circle(Circle sccl){
   this(sccl.center,sccl.radius);
  }
}

 class Solid extends tuys{
   Point yi,er,san,si;//第一、二、三、四角点(左下，右下，左上，右上)
   double zb[];
   public Solid(Point a,Point b,Point c,Point d){
    yi=new Point(a);
    er=new Point(b);
    san=new Point(c);
    si=new Point(d);
    whzb();
   }
   
   public Solid(Solid sld){
    yi=new Point(sld.yi);
    er=new Point(sld.er);
    san=new Point(sld.san);
    si=new Point(sld.si);
    whzb();
   } 
   
   public void whzb(){//最终方法，不能被重写
    if(san.x==si.x && san.y==si.y){//判断第三点和第四点是否是同一点
     zb=new double[6+2];
     zb[0]=yi.x; zb[1]=yi.y;
     zb[2]=er.x; zb[3]=er.y;
     zb[4]=san.x; zb[5]=san.y;
     zb[6]=yi.x; zb[7]=yi.y;
    }
    else{
     zb=new double[8+2];
     zb[0]=yi.x; zb[1]=yi.y;
     zb[2]=er.x; zb[3]=er.y;
     zb[4]=si.x; zb[5]=si.y;//注意顺序
     zb[6]=san.x; zb[7]=san.y;
     zb[8]=yi.x; zb[9]=yi.y;
    }
   }
   
   public Line[] sldtoline(){
    Line lns[]=null;
    if(san.x==si.x && san.y==si.y){//判断第三点和第四点是否是同一点
     lns=new Line[3];
     lns[0]=new Line(yi,er);
     lns[1]=new Line(er,si);
     lns[2]=new Line(si,yi);
    }
    else{
     lns=new Line[4];
     lns[0]=new Line(yi,er);
     lns[1]=new Line(er,si);
     lns[2]=new Line(si,san);
     lns[3]=new Line(san,yi);
    }
    return lns;
   }
 }

//非DXF文件里的类型，是把最终识别出的房间，作为一个类型，方便存入Oracle Spatial
class Room extends tuys{
  Point pnum[];
  int plen;
  double zb[];
  public Room(int n,double xy[]){//点个数，坐标x、y数组
    plen=n;
    pnum=new Point[n];
    zb=new double[n*2];
    for(int i=0;i<n*2;++i){
      zb[i]=xy[i];
      pnum[i/2]=new Point();
      pnum[i/2].x=xy[i];
      ++i;
      zb[i]=xy[i];
      pnum[i/2].y=xy[i];
   }
  }
  public void whzb(){
    for(int i=0;i<plen;++i){
      zb[i*2]=pnum[i].x;
      zb[i*2+1]=pnum[i].y;
    }
  }
}

public class TUYUAN {
 //final public static double PI=3.1415926;
}