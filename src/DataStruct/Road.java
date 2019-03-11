package DataStruct;

import java.util.List;

/**
 * @Author: pyh
 * @Date: 2019/3/8 16:40
 * @Version 1.0
 * @Function:   路数据结构
 */
public class Road {

    private int id; //道路id
    private int length; //道路长度
    private int MaxSpeed;//最高限速
    private int lanes;  //车道数
    private int start;  //起点id
    private int end;    //终点id
    private int directed;   //是否双向，1表示双向，0表示单向

    //根据车道数和路长度建立路的行车结构 横坐标表示车道，纵坐标表示长度的具体位置
    //实际上是一个矩阵,标号从1开始, 每个节点为一辆车，无车则为空
    private Car[][] matrix; //路的具体结构

    public Road(int id, int length, int MaxSpeed, int lanes, int start, int end, int directed) {
        this.id = id;
        this.length = length;
        this.MaxSpeed = MaxSpeed;
        this.lanes = lanes;
        this.start = start;
        this.end = end;
        this.directed = directed;
        matrix = new Car[lanes+1][length+1];
    }

    //只有道路编号，起点终点id的路结构
    public Road(int id, int start, int end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    //Get和Set方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getMaxSpeed() {
        return MaxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        MaxSpeed = maxSpeed;
    }

    public int getLanes() {
        return lanes;
    }

    public void setLanes(int lanes) {
        this.lanes = lanes;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getDirected() {
        return directed;
    }

    public void setDirected(int directed) {
        this.directed = directed;
    }

    //获取该路的状态
    public Car[][] getRoadStatue() {
        return matrix;
    }

    //更新路的状态
    public void setRoadStatue(Car car, int lanes, int length) {
        this.matrix[lanes][length] = car;
    }

}
