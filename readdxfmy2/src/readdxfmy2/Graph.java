/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package readdxfmy2;

import java.awt.Color;
import java.io.Serializable;

/**
 *
 * @author Administrator
 */
public class Graph implements Serializable {

    private java.awt.Color fillcolor;
    private java.awt.Color bordercolor;
    private int style;
    private int x1, y1, wide, height;
    private int floor;
    private boolean current;
    private boolean isContext = false;
    private String context;

    //构造函数用来初始化属性
    public Graph(int floor, int style, boolean iscontext, java.awt.Color fillcolor, java.awt.Color bordercolor, int x1, int y1, int wide, int height, String context) {
        this.floor = floor;
        this.style = style;
        this.isContext = iscontext;
        this.fillcolor = fillcolor;
        this.bordercolor = bordercolor;
        this.x1 = x1;
        this.y1 = y1;
        this.wide = wide;
        this.height = height;
        this.context = context;
    }

    public boolean isIsContext() {
        return isContext;
    }

    public void setIsContext(boolean isContext) {
        this.isContext = isContext;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public java.awt.Color getFillColor() {
        return fillcolor;
    }

    public void setFillColor(java.awt.Color color) {
        this.fillcolor = color;
    }

    public Color getBordercolor() {
        return bordercolor;
    }

    public void setBordercolor(Color bordercolor) {
        this.bordercolor = bordercolor;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWide() {
        return wide;
    }

    public void setWide(int wide) {
        this.wide = wide;
    }
}
