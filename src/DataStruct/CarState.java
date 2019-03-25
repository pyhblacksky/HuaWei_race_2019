package DataStruct;

import java.io.Serializable;

/**
 * @Author: pyh
 * @Date: 2019/3/14 8:37
 * @Version 1.0
 * @Function:   车在当前道路上行驶的状态
 */
public class CarState implements Serializable {
    private static final long serialVersionUID = 10L;//序列化

    public int roadId;     //当前车辆行驶的道路id
    public int lane;       //在当前道路上行驶的车道
    public int position;   //对应该路起始点的距离

    private boolean isWait; //是否处于路口等待状态
    private boolean isEnd;   //是否已经到达终点
    private boolean isRunning;//是否可行驶
    private boolean isInGarage;//是否在车库中

    public CarState(int roadId, int lane, int position){
        this.roadId = roadId;
        this.lane = lane;
        this.position = position;
        this.isInGarage = true;//默认在车库中
        this.isWait = false;//默认等待状态为false
    }
    public CarState(){
        this.isInGarage = true;//默认在车库中
        this.isWait = false;//默认等待状态为false
        this.isRunning = false;
    }

    //浅拷贝构造函数
    public CarState(CarState carState){
        this.roadId = carState.getRoadId();
        this.lane = carState.getLane();
        this.position = carState.getPosition();
        this.isInGarage = carState.isInGarage();//默认在车库中
        this.isWait = carState.isWait();
        this.isRunning = carState.isRunning();
        this.isEnd = carState.isEnd();
    }

    public int getRoadId() {
        return roadId;
    }

    public void setRoadId(int roadId) {
        this.roadId = roadId;
    }

    public int getLane() {
        return lane;
    }

    public void setLane(int lane) {
        this.lane = lane;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isWait() {
        return isWait;
    }

    public void setWait(boolean wait) {
        isWait = wait;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public boolean isInGarage() {
        return isInGarage;
    }

    public void setInGarage(boolean inGarage) {
        isInGarage = inGarage;
    }
}
