/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package readdxfmy2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

/**
 *
 * @author User
 */
public class gen_roomgeometry {

    /**
     * @param args the command line arguments
     */
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
                   File file=new File("RoomGeometry.txt"); 
                   FileWriter fw=new FileWriter(file);
                   BufferedWriter bfw=new BufferedWriter(fw);
                   //int n=1;
                   while(rs_dr.next()){
                     int idc=Integer.parseInt(rs_dr.getString(1));
	             STRUCT dbObject2=(STRUCT)rs_dr.getObject(2);
	             JGeometry geom=JGeometry.load(dbObject2);
                     double len=geom.getNumPoints()*2;//一个点有二个浮点数值，为坐标   len为zb数组的长度
                     double zb[]=geom.getOrdinatesArray();
                     bfw.write(""+idc+",room,"+idc+","+idc);
                     for(int i=0;i<len;++i) {
                       bfw.write(","+(int)zb[i]);
                     }
                     bfw.flush();
                     bfw.newLine();
                     //n++;
                   }
                   bfw.close();
                   stmt.close();
                   
                   con.close();  
               } catch (ClassNotFoundException ex) {
                   Logger.getLogger(Readdxfmy2.class.getName()).log(Level.SEVERE, null, ex);
               }
    }
    
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, SQLException, IOException {
        // TODO code application logic here
        connect_database();
    }
}
