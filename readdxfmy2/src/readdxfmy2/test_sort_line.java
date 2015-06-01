/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package readdxfmy2;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author hello
 */
public class test_sort_line {
    
    public static void main(String[] args)
    {
      /*Line ln1=new Line(0,0,100,0);
      Line ln2=new Line(0,0,200,0);
      Line ln3=new Line (0,0,0,200);
      Line ln4=new Line(0,0,80,0);
      Line ln5=new Line(0,0,160,0);
      Line ln6=new Line(0,0,0,100);
      Line ln7=new Line(0,0,0,80);*/
      Line ln1=new Line( 10825.194370759387,20133.08524814454,15325.19273287511,20133.08524814454 );
      Line ln2=new Line(37825.19273287512,20133.085248144544, 42325.19273287512,20133.085248144544 );
      Line ln3=new Line(42325.19273287509,23333.085248144547, 46825.19273287511,23333.085248144547 );
      Line ln4=new Line( 37825.1927328751,23333.085248144547 ,42325.19273287509,23333.085248144547 );
      Line ln5=new Line( 10825.194370759387,23333.085248144536, 15325.192732875112,23333.085248144536 );
      Line ln6=new Line( 15325.19273287511,20133.08524814454 ,24325.19273287512,20133.08524814454 );
      Line ln7=new Line( 15325.192732875112,23333.085248144536 ,24325.19273287512,23333.085248144536 );
      Line ln8=new Line( 28825.192732875123,20133.085248144544, 37825.19273287512,20133.085248144544 );
      Line ln9=new Line( 28825.1927328751,23333.085248144547, 37825.1927328751,23333.085248144547 );
      Line ln10=new Line( 24325.19273287512,11433.085248144534, 28825.192732875123,11433.085248144534 );
      Line ln11=new Line( 46825.19273287511,11433.085248144534, 46825.19273287511,23333.085248144547 );
      //9 10 0 5 7 1 4 6 8 3 2
      
      List<Line> zhlns=new LinkedList();
      zhlns.add(ln1);
      zhlns.add(ln2);
      zhlns.add(ln3);
      zhlns.add(ln4);
      zhlns.add(ln5);
      zhlns.add(ln6);
      zhlns.add(ln7);
      zhlns.add(ln8);
      zhlns.add(ln9);
      zhlns.add(ln10);
      zhlns.add(ln11);
      
      List<Integer> drlns=new LinkedList();
      drlns.add(0);
      drlns.add(1);
      drlns.add(2);
      drlns.add(3);
      drlns.add(4);
      drlns.add(5);
      drlns.add(6);
      drlns.add(7);
      drlns.add(8);
      drlns.add(9);
      drlns.add(10);
      
      door_sort(drlns,zhlns,0,drlns.size()-1);
      
      for(int i=0;i<drlns.size();++i)
          System.out.print(drlns.get(i) +" ");
      
    }
    
    public static void door_sort(List<Integer> drlns,List<Line> zhlns,int p,int r)
   {
     if(p<r)
     {
       int q=partition(drlns,zhlns,p,r);
       door_sort(drlns,zhlns,p,q-1);
       door_sort(drlns,zhlns,q+1,r);
     }
   }
   
   public static int partition(List<Integer> drlns,List<Line> zhlns,int p,int r)
   {
     int x=drlns.get(r);
     Line lnx=zhlns.get(x);
     int i=p-1;
     for(int j=p;j<=r-1;++j)
     {
       int y=drlns.get(j);
       Line lny=zhlns.get(y);
       if(lny.comparetoline(lnx)<=0)
       {
         i=i+1;
         exchange(drlns,i,j);
       }
     }
     exchange(drlns,i+1,r);
     return i+1;
   }
   
   public static void exchange(List<Integer> drlns,int i,int j)
   {
     int ti=drlns.get(i);
     int tj=drlns.get(j);
     drlns.set(i, tj);
     drlns.set(j, ti);
   }
}
