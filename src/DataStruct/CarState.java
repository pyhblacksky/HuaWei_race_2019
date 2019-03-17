package DataStruct;

/**
 * @Author: pyh
 * @Date: 2019/3/14 8:37
 * @Version 1.0
 * @Function:   车在当前道路上行驶的状态
 */
public class CarState {

    public int roadId;     //当前车辆行驶的道路id
    public int lane;       //在当前道路上行驶的车道
    public int position;   //对应该路起始点的距离

    CarState(int roadId, int lane, int position){
        this.roadId = roadId;
        this.lane = lane;
        this.position = position;
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
}
