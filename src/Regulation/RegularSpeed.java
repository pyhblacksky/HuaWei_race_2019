package Regulation;

import DataStruct.Car;
import DataStruct.Road;

/**
 * @Author: pyh
 * @Date: 2019/3/11 11:07
 * @Version 1.0
 * @Function:   速度规则，车辆进入下一路口
 */
public class RegularSpeed {

    //通过路口
    /**
     * nowRoad : 当前道路
     * nextRoad : 将要行驶的道路
     * car : 行驶的车辆
     * nowRoadDistance : 当前道路剩余距离
     *
     * @return 下一路口应行驶的距离
     * */
    public static int passCross(Road nowRoad, Road nextRoad, Car car, int nowRoadDistance){
        int nowSpeed = countSpeed(nowRoad, car);
        int nextSpeed = countSpeed(nextRoad, car);
        if(nowRoadDistance >= nowSpeed){
            //剩余距离大于车速，继续保持这条道路行驶
            //TODO : write code here
        }

        //如果S1 >= V2 则不能通行，行至路口处
        if(nowRoadDistance >= nextSpeed){
            //有前车，则跟车

            //否则行到路口处
            return 0;
        }

        //有前车
        if(hasFrontCar(nextRoad, car)){
            //TODO : write code here
        }

        //如果可行驶距离范围内没有前车,按照规则行驶，否则跟车，
        //规则3   S2 <= Max(SV2 - S1, 0)
        int nextDistance = Math.max(0, nextSpeed - nowRoadDistance);


        return 0;
    }

    //车辆行驶的实际速度
    public static int countSpeed(Road road, Car car){
        return Math.min(road.getMaxSpeed(), car.getMaxSpeed());
    }

    //判断有无前车函数,通过道路来判断
    public static boolean hasFrontCar(Road road, Car car){

        return false;
    }
}
