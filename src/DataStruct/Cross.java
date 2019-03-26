package DataStruct;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @Author: pyh
 * @Date: 2019/3/8 16:55
 * @Version 1.0
 * @Function:   交叉路口,作为顶点
 */
public class Cross implements Serializable {

    //利用Cross的信息，作为顶点
    private int id;
    private Road upRoad;
    private Road rightRoad;
    private Road downRoad;
    private Road leftRoad;

    public Cross(int id, Road upRoad, Road rightRoad, Road downRoad, Road leftRoad) {
        this.id = id;
        this.upRoad = upRoad;
        this.rightRoad = rightRoad;
        this.downRoad = downRoad;
        this.leftRoad = leftRoad;
        this.Control_priority = -1;
    }
    public Cross(int id, Road upRoad, Road rightRoad, Road downRoad, Road leftRoad, int Control_priority) {
        this.id = id;
        this.upRoad = upRoad;
        this.rightRoad = rightRoad;
        this.downRoad = downRoad;
        this.leftRoad = leftRoad;
        this.Control_priority = Control_priority;
    }

    //浅拷贝构造函数
    public Cross(Cross cross){
        this.id = cross.getId();
        this.upRoad = new Road(cross.getUpRoad());
        this.rightRoad = new Road(cross.getRightRoad());
        this.downRoad = new Road(cross.getDownRoad());
        this.leftRoad = new Road(cross.getLeftRoad());
        this.Control_priority = cross.getControl_priority();
    }

    //get 和 set方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Road getUpRoad() {
        return upRoad;
    }

    public void setUpRoad(Road upRoad) {
        this.upRoad = upRoad;
    }

    public Road getRightRoad() {
        return rightRoad;
    }

    public void setRightRoad(Road rightRoad) {
        this.rightRoad = rightRoad;
    }

    public Road getDownRoad() {
        return downRoad;
    }

    public void setDownRoad(Road downRoad) {
        this.downRoad = downRoad;
    }

    public Road getLeftRoad() {
        return leftRoad;
    }

    public void setLeftRoad(Road leftRoad) {
        this.leftRoad = leftRoad;
    }

    /**
     * 根据C++新增
     * 获取路口的控制权最高的车辆的ID
     * */
    private int Control_priority;

    public int getControl_priority() {
        return Control_priority;
    }

    public void setControl_priority(int Control_priority) {
        this.Control_priority = Control_priority;
    }
}
