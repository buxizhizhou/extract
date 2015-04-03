/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package readdxfmy2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author User
 */
public class test_bisearch {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        List<Integer> lst=new ArrayList();
        lst.add(1);
        lst.add(5);
        lst.add(3);
        lst.add(7);
        lst.add(4);
        lst.add(6);
        lst.add(8);
        lst.add(19);
        lst.add(20);
        Collections.sort(lst);
        for(int i=0;i<lst.size();++i)
          System.out.print(i+" ");
        System.out.println();
        for(int i=0;i<lst.size();++i)
          System.out.print(lst.get(i)+" ");
        System.out.println();
        int index=bi_search(lst,5);
        System.out.println(index);
        index=bi_search(lst,8);
        System.out.println(index);
    }
    
    public static int bi_search(List<Integer> lst,int num){
      int mid=-1;
      int low=0;
      int high=lst.size()-1;
      while(low<=high){
        mid=(low+high)/2;
        if(lst.get(mid) ==num) return mid;
        else if(lst.get(mid) >num){
          high=mid-1;
        }
        else low=mid+1;
      }
      return -1;
    }
}
