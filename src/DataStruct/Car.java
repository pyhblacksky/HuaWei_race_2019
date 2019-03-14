package DataStruct;

/**
 * @Author: pyh
 * @Date: 2019/3/8 21:05
 * @Version 1.0
 * @Function:   车辆的数据结构
 */
public class Car {

    private int id;     //车辆id
    private int start;  //起点
    private int end;    //终点
    private int MaxSpeed;//最大速度
    private int time;   //出发时间

    private CarState carState;    //当前车辆行驶状态

    public Car(int id, int start, int end, int maxSpeed, int time) {
        this.id = id;
        this.start = start;
        this.end = end;
        MaxSpeed = maxSpeed;
        this.time = time;

    }

    public Car(int id, int start, int end, int maxSpeed, int time, CarState carState) {
        this.id = id;
        this.start = start;
        this.end = end;
        MaxSpeed = maxSpeed;
        this.time = time;
        this.carState = carState;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getMaxSpeed() {
        return MaxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        MaxSpeed = maxSpeed;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public CarState getCarState() {
        return carState;
    }

    public void setCarState(CarState carState) {
        this.carState = carState;
    }
}
