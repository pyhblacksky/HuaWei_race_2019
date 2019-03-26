package Util;

import DataStruct.Car;
import DataStruct.Cross;
import DataStruct.Road;

import java.util.ArrayList;

/**
 * @Author: pyh
 * @Date: 2019/3/16 9:54
 * @Version 1.0
 * @Function:   工具类
 *      根据id获取路
 *      根据id获取车
 *      根据id获取路口
 */
public class Util {

    /**********************以下是根据id获取信息的方法**********************/
    //根据道路id获取道路
    public static Road getRoadFromId(int id, ArrayList<Road> roads){
        for(Road road : roads){
            if(road.getId() == id){
                return road;
            }
        }
        return null;
    }

    //根据id获取车辆信息
    public static Car getCarFromId(int id, ArrayList<Car> cars){
        for(Car car : cars){
            if(car.getId() == id){
                return car;
            }
        }
        return null;
    }

    //根据id获取cross信息
    public static Cross getCrossFromId(int id, ArrayList<Cross> crosses){
        for(Cross cross : crosses){
            if(id == cross.getId()){
                return cross;
            }
        }
        return null;
    }

    //根据两条路获取cross信息
    public static Cross getCrossFromTwoRoad(int road1, int road2, ArrayList<Cross> crosses){
        for(Cross cross : crosses){
            ArrayList<Integer> tempList = new ArrayList<>();
            tempList.add(cross.getLeftRoad().getId());
            tempList.add(cross.getDownRoad().getId());
            tempList.add(cross.getUpRoad().getId());
            tempList.add(cross.getRightRoad().getId());
            if(tempList.contains(road1) && tempList.contains(road2)){
                return cross;
            }
        }
        return null;
    }
}
