package DataStruct;

import DataStruct.Road;

import java.util.ArrayList;

/**
 * @Author: pyh
 * @Date: 2019/3/17 14:46
 * @Version 1.0
 * @Function:   Answer的数据结构
 *
 */
public class Answer {

    private int carId;//车辆id
    private int time;//实际出发时间
    private ArrayList<Road> roads;//车辆实际行驶过的道路

    public Answer(int carId, int time, ArrayList<Road> roads) {
        this.carId = carId;
        this.time = time;
        this.roads = roads;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public ArrayList<Road> getRoads() {
        return roads;
    }

    public void setRoads(ArrayList<Road> roads) {
        this.roads = new ArrayList<>(roads);
    }
}
