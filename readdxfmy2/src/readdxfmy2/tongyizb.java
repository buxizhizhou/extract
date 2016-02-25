/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

//!!!想完成坐标的统一。并没有完成。觉得从数据库中读出来再修改，还是要判断图元是什么类别。

package readdxfmy2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

/**
 *
 * @author hello
 */
public class tongyizb {
    public static double rmlnsminx=0.0;
    public static double rmlnsminy=0.0;
    
    public static double jzx=834;//进行统一校正的点坐标  (834,-156)是电三右部2的最小xy值
    public static double jzy=-156;
    
    public static void connect_database() throws InstantiationException, IllegalAccessException, SQLException, IOException{
           //建立数据库连接   
           String Driver="oracle.jdbc.driver.OracleDriver";    //连接数据库的方法    
           String URL="jdbc:oracle:thin:@127.0.0.1:1521:cad";    //cad为数据库的SID    
           String Username="cadadmin";    //用户名               
           String Password="cad";    //密码    
           String tableName="room";
           try {
                   Class.forName(Driver).newInstance();    //加载数据库驱动
                   Connection con=DriverManager.getConnection(URL,Username,Password);  
                   if(!con.isClosed())
                       System.out.println("Succeeded connecting to the Database!");
                   //Statement stmt=con.createStatement();
                   
                   String sql="SELECT * FROM "+tableName;
                   System.out.println("Executing query:'"+sql+"'");
                   Statement stmt=con.createStatement();
                   ResultSet rs_dr=stmt.executeQuery(sql);

                   while(rs_dr.next()){
                     int idc=Integer.parseInt(rs_dr.getString(1));
	             STRUCT dbObject2=(STRUCT)rs_dr.getObject(2);
	             JGeometry geom=JGeometry.load(dbObject2);
                     
                     double len=geom.getNumPoints()*2;//一个点有二个浮点数值，为坐标   len为zb数组的长度
                     double zb[]=geom.getOrdinatesArray();
                     for(int i=0;i<len;i=i+2) {
                       zb[i]=zb[i]+rmlnsminx-jzx;
                       zb[i+1]=zb[i+1]+rmlnsminy-jzy;
                     }
                     
                     
                   }
                   stmt.close();
                   
                   con.close();  
               } catch (ClassNotFoundException ex) {
                   Logger.getLogger(Readdxfmy2.class.getName()).log(Level.SEVERE, null, ex);
               }
    }
    
    public static void main()
    {
        
    }
}
