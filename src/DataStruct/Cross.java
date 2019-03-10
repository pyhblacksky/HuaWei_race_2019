package DataStruct;

import java.util.ArrayList;

/**
 * @Author: pyh
 * @Date: 2019/3/8 16:55
 * @Version 1.0
 * @Function:   交叉路口,作为顶点
 */
public class Cross {

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
}
