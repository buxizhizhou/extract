/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 将算法预处理后的线，在CAD中画在一个全新的图层zhlns中，通过该文件把预处理后的线读到数据库中
 */
package readdxfmy2;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

/**
 *
 * @author hello
 */
public class read_zhlns_to_database {
    
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
    }
    
    public static void main()
    {
      
    }
    
}
