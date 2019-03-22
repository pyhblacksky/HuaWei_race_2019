package Judgment;

import DataStruct.*;
import Util.Util;

import java.util.*;

/**
 * @Author: pyh
 * @Date: 2019/3/17 14:38
 * @Version 1.0
 * @Function:   判题器
 *
 *  输入为car.txt,cross.txt,road.txt answer.txt， 输出为调度时间、总调度时间
 *
 */
public class Judge {

    private static ArrayList<Car> carList;
    private static ArrayList<Cross> crossList;
    private static ArrayList<Road> roadList;
    private static ArrayList<Answer> answerList;

    /**
     * @param cars car列表
     * @param crosses cross列表
     * @param roads road列表
     * @param answers answer列表
     *
     * @return time 返回调度时间
     * */
    public static int judge(ArrayList<Car> cars, ArrayList<Cross> crosses,
                            ArrayList<Road> roads, ArrayList<Answer> answers){
        /**************************数据预处理***************************/
        roadList = new ArrayList<>(roads);
        carList = new ArrayList<>(cars);
        crossList = new ArrayList<>(crosses);
        answerList = new ArrayList<>(answers);

        //answerList 按照时间降序排序
        Collections.sort(answerList, new Comparator<Answer>() {
            @Override
            public int compare(Answer o1, Answer o2) {
                return o1.getTime() - o2.getTime();
            }
        });

        /**************************************************************/
        /* 按时间片处理 */
        int Time = 1;//从时间1开始
        boolean deadLock = false;//标记是否出现死锁
        int count = 0;//计算已满道路数
        for(; ;Time++) {
            /*****测试——当前在路上的车****/
            ArrayList<Car> inRoadCar = getInRoadCar();
            //System.out.println(inRoadCar);
            /*****测试——到达终点的车****/
            ArrayList<Car> endCar = getInEndCar();
            //System.out.println(endCar);
            /*****************************/

            /*当所有在路上的车辆运行完成*/
            while(!isAllRoadCarComplete()){
                /* driveAllWaitCar() */
                //如果路上没车，跳过
                if(isAllRoadEmpty()){
                    break;
                }

                //ArrayList<Road> fullRoad = countFullRoad();//计算已满路
                //if(fullRoad.size() >= 4){//count>=3 是形成环的必要条件，此时判断这三条路是否成环
                //    if(conflict(fullRoad)){//出现死锁
                //        //将其中一条满的路封闭。破坏成环条件
                //        destroyLoop(inRoadCar, fullRoad.get(0));//传入当前在路上的车辆，寻找包括这条路的,将这条路加入禁止
                //        deadLock = true;
                //        break;
                //    }
                //}

                driveAllCarJustOnRoadToEndState();
            }

            if(deadLock){//通过标记跳出整个循环
                return Integer.MAX_VALUE;   //表示运行超时
            }

            /* 车库中的车辆上路行驶 */
            driveCarInGarage(Time);
            //行车完成，将所有车设置为可行状态,除了已经到达终点的车
            setCarCanRun();

            /* 车库无车，道路无车，则运行完毕*/
            if(isGarageEmpty() && isAllRoadEmpty()){
                break;
            }

        }
        return Time;//返回调度时间
    }

    /**
     * 车库中的车辆上路行驶
     * */
    private static void driveCarInGarage(int time){
        //车辆从某个节点出发
        for(Answer answer : answerList){
            if(time >= answer.getTime()){   //答案的发车时间小于发车时间，则发车.  取出车辆及其相应的可行道路,将车放入Road矩阵中
                Car car = Util.getCarFromId(answer.getCarId(), carList);//准备出发的车
                if(car.getCarState().isEnd()){  //如果该车已经到达终点，则不对这辆车做任何变化
                    continue;
                }
                if(!car.getCarState().isInGarage()){//如果该车已经不在车库，跳过
                    continue;
                }
                Road goCarRoad = null;
                int carStart = -1;//获取当前车的起点
                if(car != null && car.getRoads() != null && car.getRoads().size() != 0){
                    goCarRoad = car.getRoads().get(0);//取该车的可行路list中的第一个
                    carStart = car.getStart();
                }
                if(isRoadFull(goCarRoad, car)){  //如果前车已经将道路占满，则推迟发车
                    answer.setTime(time+1);
                    continue;
                }
                //确定道路行驶方向,存在双向和单向两种车道，车的起点和路的起点相同
                if(carStart == goCarRoad.getStart()){
                    //此时放入 S2E矩阵
                    int i = putLane(goCarRoad, 1);//表示车道
                    int j = putLength(goCarRoad, car,1);//表示放入长度
                    if(i == -1 || j == -1){//说明放不下，时间推迟，下一个车
                        answer.setTime(time+1);
                        continue;
                    }
                    goCarRoad.setMatrix_S2E(car, i, j);
                    //更新车的状态，当前所在道路及相关信息
                    car.getCarState().setRoadId(goCarRoad.getId());
                    car.getCarState().setRunning(false);//车已行动完毕
                    car.getCarState().setInGarage(false);//车从车库中发出
                } else if(carStart == goCarRoad.getEnd()){
                    //此时放入 E2S矩阵
                    int i = putLane(goCarRoad, -1);
                    int j = putLength(goCarRoad, car,-1);
                    if(i == -1 || j == -1){//说明放不下，时间推迟，下一个车
                        answer.setTime(time+1);
                        continue;
                    }
                    goCarRoad.setMatrix_E2S(car, i, j);
                    car.getCarState().setRoadId(goCarRoad.getId());
                    car.getCarState().setRunning(false);//车已行动完毕
                    car.getCarState().setInGarage(false);//车从车库中发出
                }
            } else{
                break;//超过本次时间，跳出
            }
        }
    }

    /* 调整所有道路上在道路上的车辆，让道路上车辆前进，只要不出路口且可以到达终止状态的车辆
     * 分别标记出来等待的车辆（要出路口的车辆，或者因为要出路口的车辆阻挡而不能前进的车辆）
     * 和终止状态的车辆（在该车道内可以经过这一次调度可以行驶其最大可行驶距离的车辆）*/
    /* 对所有车道进行调整 */
    private static void driveAllCarJustOnRoadToEndState(){
        //遍历每个路口，每个路口每条路按照优先级，车进行移动
        for(Cross cross : crossList){
            ArrayList<Road> priorityRoad = ThroughCrossRule.RoadPriority(cross);//当前路口优先级降序排列的道路
            boolean over = false;//标记循环调度是否结束
            while(true) {//循环调度
                for (Road road : priorityRoad) {//针对每条路
                    if (road.getEnd() == cross.getId()) {//此时说明是正向
                        if(isRoadEmpty(road, 1)){//如果这条路上没车，直接进入下一条
                            over = true;
                            continue;
                        }
                        if (nowRoadComplete(road, 1)) {//如果当前路上的车已经行进完毕，下一条路
                            over = true;
                            continue;
                        }

                        boolean changeRoad = false;//优先级是否转让
                        /*当前道路优先级未转让时，一直循环遍历这条路的车道*/
                        while(!changeRoad) {
                            //对当前道路的每个车道进行遍历扫描
                            for (int lane = 0; lane < road.getLanes(); lane++) {
                                int position = road.get_Topcar_location_S2E(lane+1);//获取当前位置的车的距离
                                if(position == -1){//说明是空车道，下一条车道
                                    if(isRoadEmpty(road, 1)){//此时路为空，跳出
                                        changeRoad = true;
                                        break;
                                    }
                                    continue;
                                }
                                Car topCar = road.getMatrix_S2E().get(lane).get(position);//获取车
                                if(topCar == null || topCar.getId() == -1){//不存在头车，说明此时车道为空
                                    continue;//直接下一车道
                                }
                                //如果头车已经行驶过，本车道正常车辆行驶
                                if(!topCar.getCarState().isRunning()){
                                    //正常行驶
                                    int normalPosition = road.get_Normalcar_location_S2E(lane+1);
                                    if(normalPosition != -1) {
                                        Car normalCar = road.getMatrix_S2E().get(lane).get(normalPosition);
                                        deleteRoadCar(normalCar, road, 1);//删除这条路上的该车
                                        ThroughCrossRule.Untopcar_Go_distance(cross, lane, road, normalCar);//设置距离
                                        updateRoad(normalCar, road, normalCar.getCarState().getLane(), normalCar.getCarState().getPosition(), 1);
                                        normalCar.getCarState().setRunning(false);
                                    }

                                    if(isThisRoadCarComplete(road, 1)){//当前路上的车已经全部完成，跳出
                                        changeRoad = true;
                                        break;
                                    }
                                    continue;
                                }
                                boolean isUpdate = ThroughCrossRule.Go_control_priority(topCar, cross, roadList, crossList);//是否可以更新道路矩阵
                                if (isUpdate) {   //可以更新矩阵
                                    //反复调用
                                    int pass_or_not = ThroughCrossRule.Pass_or_Not(cross, lane, roadList, crossList);
                                    int real_distance = ThroughCrossRule.GO_distance(cross, lane, roadList, crossList, pass_or_not);

                                    //不过路口
                                    if(pass_or_not==0) {
                                        deleteRoadCar(topCar, road, 1);//删除这条路上的该车
                                        updateRoad(topCar, road, topCar.getCarState().getLane(), topCar.getCarState().getPosition(), 1);
                                        topCar.getCarState().setRunning(false);//设置车辆状态信息， 已经行进完成
                                    } else {//过路口
                                        if(real_distance == 0){
                                            //被堵住，在原地
                                            topCar.getCarState().setRunning(false);
                                            continue;
                                        }
                                        if(real_distance == -2){
                                            //到终点
                                            topCar.getCarState().setEnd(true);
                                            deleteRoadCar(topCar, road, 1);
                                            continue;
                                        }

                                        deleteRoadCar(topCar, road, 1);//删除这条路上的该车
                                        Road nextRoad = getNextRoad(topCar);
                                        topCar.getCarState().setRoadId(nextRoad.getId());//更新车的下一条路的状态
                                        updateRoad(topCar, nextRoad, topCar.getCarState().getLane(), topCar.getCarState().getPosition(), 1);//放入的方向是否正确？
                                        topCar.getCarState().setRunning(false);//设置车辆状态信息， 已经行进完成
                                    }
                                } else {
                                    //不能更新矩阵，优先级转让,切换道路
                                    changeRoad = true;
                                    break;
                                }
                            }
                        }

                        //如果该路口的每条路上没有等待的车了,且车辆运行完毕，跳出循环
                        if(!isRoadHasCarWait(road, 1) && isRoadCarComplete(road, 1)){
                            over = true;
                            break;
                        }

                    } else if (road.getStart() == cross.getId()) {//此时说明是逆向
                        if(isRoadEmpty(road, -1)){//如果这条路上没车，直接进入下一条
                            over = true;
                            continue;
                        }
                        if (nowRoadComplete(road, -1)) {
                            over = true;
                            continue;
                        }
                        boolean changeRoad = false;//优先级是否转让
                        /*当前道路优先级未转让时，一直循环遍历这条路的车道*/
                        while(!changeRoad) {
                            //对当前道路的每个车道进行遍历扫描
                            for (int lane = 0; lane < road.getLanes(); lane++) {
                                int position = road.get_Topcar_location_E2S(lane+1);//获取当前位置的车的距离
                                if(position == -1){//说明是空车道，跳过
                                    if(isRoadEmpty(road, -1)){//此时路为空，跳出
                                        changeRoad = true;
                                        break;
                                    }
                                    continue;
                                }
                                Car topCar = road.getMatrix_E2S().get(lane).get(position);//获取车
                                if(topCar == null || topCar.getId() == -1){//不存在头车，说明此时车道为空
                                    continue;//直接下一车道
                                }
                                //如果头车已经行驶过，跳过
                                if(!topCar.getCarState().isRunning()){
                                    //正常行驶
                                    int normalPosition = road.get_Normalcar_location_E2S(lane+1);
                                    if(normalPosition != -1) {//车道不为空才执行
                                        Car normalCar = road.getMatrix_E2S().get(lane).get(normalPosition);
                                        deleteRoadCar(normalCar, road, -1);//删除这条路上的该车
                                        ThroughCrossRule.Untopcar_Go_distance(cross, lane, road, normalCar);//设置距离
                                        updateRoad(normalCar, road, normalCar.getCarState().getLane(), normalCar.getCarState().getPosition(), -1);
                                        normalCar.getCarState().setRunning(false);
                                    }

                                    if(isThisRoadCarComplete(road, -1)){//当前路上的车已经全部完成，跳出
                                        changeRoad = true;
                                        break;
                                    }
                                    continue;
                                }
                                boolean isUpdate = ThroughCrossRule.Go_control_priority(topCar, cross, roadList, crossList);//是否可以更新道路矩阵
                                if (isUpdate) {   //可以更新矩阵
                                    //反复调用
                                    int pass_or_not = ThroughCrossRule.Pass_or_Not(cross, lane,roadList, crossList);
                                    int real_distance = ThroughCrossRule.GO_distance(cross, lane, roadList, crossList, pass_or_not);

                                    //不过路口
                                    if(pass_or_not == 0) {
                                        deleteRoadCar(topCar, road, -1);//删除这条路上的该车
                                        updateRoad(topCar, road, topCar.getCarState().getLane(), topCar.getCarState().getPosition(), -1);
                                        topCar.getCarState().setRunning(false);//设置车辆状态信息， 已经行进完成
                                    } else {//过路口
                                        if(real_distance == 0){
                                            //被堵住，在原地
                                            topCar.getCarState().setRunning(false);
                                            continue;
                                        }
                                        if(real_distance == -2){
                                            //到终点
                                            topCar.getCarState().setEnd(true);
                                            deleteRoadCar(topCar, road, -1);
                                            continue;
                                        }

                                        deleteRoadCar(topCar, road, -1);//删除这条路上的该车
                                        Road nextRoad = getNextRoad(topCar);
                                        topCar.getCarState().setRoadId(nextRoad.getId());//更新车的下一条路的状态
                                        updateRoad(topCar, nextRoad, topCar.getCarState().getLane(), topCar.getCarState().getPosition(), -1);//放入的方向是否正确？
                                        topCar.getCarState().setRunning(false);//设置车辆状态信息， 已经行进完成
                                    }
                                } else {
                                    //不能更新矩阵，优先级转让,切换道路
                                    changeRoad = true;
                                    break;
                                }
                            }
                        }

                        //如果该路口的每条路上没有等待的车了,且车辆运行完毕，跳出循环
                        if(!isRoadHasCarWait(road, 1) && isRoadCarComplete(road, -1)){
                            over = true;
                            break;
                        }
                    }
                }

                if(over){
                    break;
                }
            }
        }
    }

    /**
     * 将所有车设置为可行驶状态，除了已经到达终点的车
     * */
    private static void setCarCanRun(){
        for(Car car : carList){
            if(car.getCarState().isEnd()){//此车已经到达终点，跳过
                continue;
            }
            car.getCarState().setRunning(true);//设为true，表示可行
        }
    }

    /**
     * 判断车库为空，即所有车已经发出
     * isInGarage表示在车库中
     * */
    private static boolean isGarageEmpty(){
        for(Car car : carList){
            if(car.getCarState().isInGarage()){
                return false;//说明车库中有车，车还未发完
            }
        }
        return true;//说明此时车库为空
    }

    /**
     * 当前道路上的所有车是否已经行进完成
     * */
    private static boolean isAllRoadCarComplete(){
        ArrayList<ArrayList<Car>> MatrixTemp;
        for(Road road : roadList){
            if(road.getDirected() == 1){//双向，遍历两个矩阵
                MatrixTemp = road.getMatrix_E2S();
                for(ArrayList<Car> lanesCar : MatrixTemp){
                    for(Car lengthCar : lanesCar){
                        if(lengthCar.getId() != -1 && lengthCar.getCarState().isRunning()){ //存在车，且车辆可行，返回false
                            return false;
                        }
                    }
                }

                MatrixTemp = road.getMatrix_S2E();
                for(ArrayList<Car> lanesCar : MatrixTemp){
                    for(Car lengthCar : lanesCar){
                        if(lengthCar.getId() != -1 && lengthCar.getCarState().isRunning()){ //存在车，且车辆可行，返回false
                            return false;
                        }
                    }
                }
            } else{
                //只遍历E2S矩阵
                MatrixTemp = road.getMatrix_S2E();
                for(ArrayList<Car> lanesCar : MatrixTemp){
                    for(Car lengthCar : lanesCar){
                        if(lengthCar.getId() != -1 && lengthCar.getCarState().isRunning()){ //存在车，且车辆可行，返回false
                            return false;
                        }
                    }
                }
            }
        }
        return true;//说明当前所有路上的车已经完成
    }
    /**
     * 当前道路上的车是否已经行进完成
     * PorN表示正反 PorN == -1；表示反， PorN == 1表示正
     * */
    private static boolean isThisRoadCarComplete(Road road, int PorN){
        if(PorN == 1){
            //遍历S2E矩阵
            for(ArrayList<Car> lanes : road.getMatrix_S2E()){
                for(Car car : lanes){
                    if(car.getId() != -1 && car.getCarState().isRunning()){
                        return false;
                    }
                }
            }
        } else if(PorN == -1){
            for(ArrayList<Car> lanes : road.getMatrix_E2S()){
                for(Car car : lanes){
                    if(car.getId() != -1 && car.getCarState().isRunning()){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 检测当前道路上的所有车是否已经运动完成
     * PorN表示正反 PorN == -1；表示反， PorN == 1表示正
     * */
    private static boolean nowRoadComplete(Road road, int PorN){
        //朝向问题
        ArrayList<ArrayList<Car>> MatrixTemp;
        if(PorN == 1){
            //此时遍历S2E矩阵
            MatrixTemp = road.getMatrix_S2E();
            for(ArrayList<Car> lanes : MatrixTemp){
                for(Car car : lanes){
                    if(car.getId() != -1 && car.getCarState().isRunning()){//可动，返回false
                        return false;
                    }
                }
            }
        } else if(PorN == -1){
            //反向，此时遍历E2S矩阵
            MatrixTemp = road.getMatrix_E2S();
            for(ArrayList<Car> lanes : MatrixTemp){
                for(Car car : lanes){
                    if(car.getId() != -1 && car.getCarState().isRunning()){//可动，返回false
                        return false;
                    }
                }
            }
        } else{
            //出现意外错误或者输入出错
            return false;
        }
        return true;//此时说明这条路上的所有车已经行进完毕
    }

    /**
     * 更新道路矩阵, 同时更新车的信息
     * PorN表示正反 PorN == -1；表示反， PorN == 1表示正
     * */
    private static void updateRoad(Car car, Road road, int putLane, int putLength, int PorN){
        if(road == null){
            return;
        }

        if(PorN == 1){
            //正向，更新S2E矩阵
            road.setMatrix_S2E(car, putLane, putLength);
            //更新车的状态
            car.getCarState().setLane(putLane);
            car.getCarState().setPosition(putLength);
        } else if(PorN == -1){
            //逆向，更新E2S矩阵
            road.setMatrix_E2S(car, putLane, putLength);
            //更新车的状态
            car.getCarState().setLane(putLane);
            car.getCarState().setPosition(putLength);
        }
    }

    /**
     * 删除矩阵上的该车
     * PorN表示正反 PorN == -1；表示反， PorN == 1表示正
     * */
    private static void deleteRoadCar(Car car, Road road, int PorN){
        if(car == null || road == null){
            return;
        }
        if(PorN == 1){
            //正向，更新S2E
            Car emptyCar = new Car(-1,-1,-1,-1,-1);//空车
            for(int i = 0; i < road.getMatrix_S2E().size(); i++){
                for(int j = 0; j < road.getMatrix_S2E().get(i).size(); j++){
                    if(road.getMatrix_S2E().get(i).get(j) == car){
                        road.setMatrix_S2E(emptyCar, i, j);
                        break;
                    }
                }
            }
        } else if(PorN == -1){
            //逆向，更新E2S
            Car emptyCar = new Car(-1,-1,-1,-1,-1);//空车
            for(int i = 0; i < road.getMatrix_E2S().size(); i++){
                for(int j = 0; j < road.getMatrix_E2S().get(i).size(); j++){
                    if(road.getMatrix_E2S().get(i).get(j) == car){
                        road.setMatrix_E2S(emptyCar, i, j);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 获取当前车行驶的下一条路
     * car 要获取下一条路的车
     * */
    private static Road getCarNextRoad(Car car){
        ArrayList<Road> roads = car.getRoads();
        int nowRoadId = car.getCarState().getRoadId();
        int index = -1; //获取下一条路的信息
        for(int i = 0; i < roads.size(); i++){
            if(nowRoadId == roads.get(i).getId()){
                index = i + 1;
                break;
            }
        }
        if(index == -1){//说明无路可走，要么已经发车，要么车已经到达终点
            return null;
        }
        car.getCarState().setRoadId(roads.get(index).getId());//更新当前行驶道路
        return roads.get(index);
    }

    /**
     * 判断道路是否已经占满
     * car:要发的车
     * goCarRoad：当前车要出发的路
     * */
    private static boolean isRoadFull(Road goCarRoad, Car car){
        //判断该车的方向
        if(car.getStart() == goCarRoad.getStart()){
            //此时是正向，判断S2E矩阵
            ArrayList<ArrayList<Car>> MatrixTemp = goCarRoad.getMatrix_S2E();
            for(ArrayList<Car> lane : MatrixTemp){
                for(Car judgeCar : lane){
                    if(judgeCar.getId() == -1){
                        //说明没满
                        return false;
                    }
                }
            }
        } else if(car.getStart() == goCarRoad.getEnd()){
            //此时是逆向，判断E2S矩阵
            ArrayList<ArrayList<Car>> MatrixTemp = goCarRoad.getMatrix_E2S();
            for(ArrayList<Car> lane : MatrixTemp){
                for(Car judgeCar : lane){
                    if(judgeCar.getId() == -1){
                        //说明没满
                        return false;
                    }
                }
            }
        }
        return true;//说明已满
    }

    /**
     * 寻找当前存在车的道路
     * */
    private static ArrayList<Road> hasCarRoad(){
        ArrayList<Road> res = new ArrayList<>();
        ArrayList<ArrayList<Car>> MatrixTemp;
        for(Road road : roadList){
            boolean flag = false;
            if(road.getDirected() == 1){
                //S2E
                MatrixTemp = road.getMatrix_S2E();
                for(ArrayList<Car> lanes : MatrixTemp){
                    for(Car car : lanes){
                        if(car.getId() != -1){//说明有车，直接跳出下一条路
                            flag = true;
                            res.add(road);
                            break;
                        }
                    }
                    if(flag){break;}
                }

                if(flag){
                    continue;//遍历下一条路,否则遍历下个矩阵
                }
                flag = false;
                MatrixTemp = road.getMatrix_E2S();
                for(ArrayList<Car> lanes : MatrixTemp){
                    for(Car car : lanes){
                        if(car.getId() != -1){//说明有车，直接跳出下一条路
                            flag = true;
                            res.add(road);
                            break;
                        }
                    }
                    if(flag){break;}
                }

            } else{
                //S2E矩阵
                MatrixTemp = road.getMatrix_S2E();
                for(ArrayList<Car> lanes : MatrixTemp){
                    for(Car car : lanes){
                        if(car.getId() != -1){//说明有车，直接跳出下一条路
                            flag = true;
                            res.add(road);
                            break;
                        }
                    }
                    if(flag){break;}
                }
            }
        }
        return res;
    }

    /**
     * 计算已满道路， 通过ArrayList传出
     * */
    private static ArrayList<Road> countFullRoad() {
        ArrayList<Road> res = new ArrayList<>();//保存已满路
        ArrayList<ArrayList<Car>> MatrixTemp;
        for (Road road : roadList) {
            boolean flag = false;//标记量，是否跳出
            if (road.getDirected() == 1) {//说明是双向路
                //其中一个矩阵满则认为路满，这里相当于放大了条件
                MatrixTemp = road.getMatrix_S2E();
                for (ArrayList<Car> lanes : MatrixTemp) {
                    for (Car car : lanes) {
                        if (car.getId() == -1) {//说明有空位，直接下一条路
                            flag = true;//说明路不满，跳出
                            break;
                        }
                    }
                    if (flag) { break; }
                }

                if (!flag) { //此时说明路满，直接遍历下一条路，否则继续遍历另外一个矩阵
                    res.add(road);
                    continue;
                }
                flag = false;//重新设置标记量
                //E2S矩阵
                MatrixTemp = road.getMatrix_E2S();
                for (ArrayList<Car> lanes : MatrixTemp) {
                    for (Car car : lanes) {
                        if (car.getId() == -1) {//说明有空位，直接下一条路
                            flag = true;
                            break;
                        }
                    }
                    if (flag) { break; }
                }

                if (flag) { continue; }
                res.add(road);//此时说明已满，count+1
            } else {
                //S2E矩阵
                MatrixTemp = road.getMatrix_S2E();
                for (ArrayList<Car> lanes : MatrixTemp) {
                    for (Car car : lanes) {
                        if (car.getId() == -1) {//说明有空位，直接下一条路
                            flag = true;
                            break;
                        }
                    }
                    if (flag) { break; }
                }

                if (flag) { continue; }
                res.add(road);//此时说明已满，count+1
            }
        }
        return res;
    }

    /**
     * 所有道路上的是否没车,遍历道路矩阵
     * */
    private static boolean isAllRoadEmpty(){
        ArrayList<ArrayList<Car>> MatrixTemp;
        for(Road road : roadList){
            if(road.getDirected() == 1){//双向，遍历两个矩阵
                MatrixTemp = road.getMatrix_E2S();
                for(ArrayList<Car> lanes : MatrixTemp){
                    for(Car car : lanes){
                        if(car.getId() != -1){//存在不为-1的车，说明车道上有车
                            return false;
                        }
                    }
                }

                MatrixTemp = road.getMatrix_S2E();
                for(ArrayList<Car> lanes : MatrixTemp){
                    for(Car car : lanes){
                        if(car.getId() != -1){//存在不为-1的车，说明车道上有车
                            return false;
                        }
                    }
                }
            } else{
                //遍历E2S矩阵
                MatrixTemp = road.getMatrix_E2S();
                for(ArrayList<Car> lanes : MatrixTemp){
                    for(Car car : lanes){
                        if(car.getId() != -1){//存在不为-1的车，说明车道上有车
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 判断当前道路上是否存在等待状态的车
     * road:需要判断的道路
     * PorN:正向还是反向， PorN == -1；表示反， PorN == 1表示正
     * */
    private static boolean isRoadHasCarWait(Road road, int PorN){
        ArrayList<ArrayList<Car>> MatrixTemp;
        if(PorN == 1){
            //遍历S2E
            MatrixTemp = road.getMatrix_S2E();
            for(ArrayList<Car> lanes : MatrixTemp){
                for(Car car : lanes){
                    if(car.getId() != 0 && car.getCarState().isWait()){
                        return true;
                    }
                }
            }
        } else if(PorN == -1){
            MatrixTemp = road.getMatrix_E2S();
            for(ArrayList<Car> lanes : MatrixTemp){
                for(Car car : lanes){
                    if(car.getId() != 0 && car.getCarState().isWait()){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断当前道路上的车是否运行完毕
     * PorN:正向还是反向， PorN == -1；表示反， PorN == 1表示正
     * */
    private static boolean isRoadCarComplete(Road road, int PorN){
        ArrayList<ArrayList<Car>> MatrixTemp;
        if(PorN == 1){
            //遍历S2E
            MatrixTemp = road.getMatrix_S2E();
            for(ArrayList<Car> lanes : MatrixTemp){
                for(Car car : lanes){
                    if(car.getId() != 0 && car.getCarState().isRunning()){
                        return false;
                    }
                }
            }
        } else if(PorN == -1){
            MatrixTemp = road.getMatrix_E2S();
            for(ArrayList<Car> lanes : MatrixTemp){
                for(Car car : lanes){
                    if(car.getId() != 0 && car.getCarState().isRunning()){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 判断当前道路是否有车
     * PorN:正向还是反向， PorN == -1；表示反， PorN == 1表示正
     * */
    private static boolean isRoadEmpty(Road road, int PorN){
        ArrayList<ArrayList<Car>> MatrixTemp;
        if(PorN == 1){
            //遍历S2E矩阵
            MatrixTemp = road.getMatrix_S2E();
            for(ArrayList<Car> lanes : MatrixTemp){
                for(Car car : lanes){
                    if(car.getId() != -1){//有车，说明道路不为空
                        return false;
                    }
                }
            }

        } else if(PorN == -1){
            MatrixTemp = road.getMatrix_E2S();
            for(ArrayList<Car> lanes : MatrixTemp){
                for(Car car : lanes){
                    if(car.getId() != -1){//有车，说明道路不为空
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 根据道路和正向反向确定放入第几个车道
     * PorN:正向还是反向， PorN == -1；表示反， PorN == 1表示正
     *
     * 如果返回-1，不能放入和更新矩阵
     * */
    private static int putLane(Road road, int PorN){
        ArrayList<ArrayList<Car>> MatrixTemp;
        if(PorN == 1){
            //正向，遍历S2E矩阵
            MatrixTemp = road.getMatrix_S2E();
            for(int i = 0; i < MatrixTemp.size(); i++){
                for(int j = 0; j < MatrixTemp.get(i).size(); j++){
                    Car car = MatrixTemp.get(i).get(j);
                    if(car.getId() == -1){//说明有空位
                        car.getCarState().setLane(i);//设置所放车道
                        return i;
                    } else{
                        break;//下一条车道
                    }
                }
            }
        } else if(PorN == -1){
            //逆向，遍历E2S矩阵
            MatrixTemp = road.getMatrix_E2S();
            for(int i = 0; i < MatrixTemp.size(); i++){
                for(int j = 0; j < MatrixTemp.get(i).size(); j++){
                    Car car = MatrixTemp.get(i).get(j);
                    if(car.getId() == -1){//说明有空位
                        car.getCarState().setLane(i);//设置所放车道
                        return i;//返回当前车道
                    } else{
                        break;//下一条车道
                    }
                }
            }
        }

        return -1;//放不下,说明所有车道已满
    }
    /**
     * 根据道路和正向反向确定放入当前车道的距离
     * PorN:正向还是反向， PorN == -1；表示反， PorN == 1表示正
     *
     * 如果返回-1，不能放入和更新矩阵
     * */
    private static int putLength(Road road, Car car, int PorN){
        ArrayList<ArrayList<Car>> MatrixTemp;
        int maxLength = Math.min(car.getMaxSpeed(), road.getMaxSpeed());//计算最大可行驶距离
        int position = -1;
        if(PorN == 1){
            //遍历S2E矩阵
            MatrixTemp = road.getMatrix_S2E();
            int lane = car.getCarState().getLane();//将要放入的车道
            //查看是否有前车
            for(int i = 0; i < maxLength; i++){
                if(MatrixTemp.get(lane).get(i).getId() == -1){//说明没车
                    position++;//可放位置+1
                } else {
                    break;//只要有车，跳出
                }
            }
            car.getCarState().setPosition(position);
        } else if(PorN == -1){
            //遍历E2S矩阵
            MatrixTemp = road.getMatrix_E2S();
            int lane = car.getCarState().getLane();//将要放入的车道
            //查看是否有前车
            for(int i = 0; i < maxLength; i++){
                if(MatrixTemp.get(lane).get(i).getId() == -1){//说明没车
                    position++;//可放位置+1
                } else {
                    break;//只要有车，跳出
                }
            }
            car.getCarState().setPosition(position);
        }

        return position;//放不下，车位已被占满
    }

    /**
     * 获取当前车辆的下一条行驶道路信息
     *
     * car ： 当前车辆
     * 返回 下一条道路信息
     * */
    public static Road getNextRoad(Car car){
        CarState carstate = car.getCarState();
        int CurrentRoadID = carstate.getRoadId();
        Road CurrentRoad = Util.getRoadFromId(CurrentRoadID, roadList);//遍历存放所有Road的vector<Road> AllRoads来寻找CurrentRoadID对应的CurrentRoad
        if(CurrentRoad == null){
            //当前道路为空，说明到达终点
            return null;
        }

        ArrayList<Road> path = new ArrayList<>(car.getRoads());
        int k = 0;//表示当前在第行驶的第几条路上
        for(Road road : path){
            if(road == CurrentRoad){
                break;
            }
            k++;
        }
        if(k >= path.size() - 1){
            //k大于当前可行驶道路，说明到达终点，返回-1表示结束
            return null;
        }
        Road NextRoad = path.get(k+1);//下一条要行驶的道路   k+1
        return NextRoad;
    }

    /**
     * 测试，获取当前在路上的车
     * */
    private static ArrayList<Car> getInRoadCar(){
        ArrayList<Car> res = new ArrayList<>();
        for(Road road : roadList){
            if(road.getDirected() == 1){
                //遍历两个矩阵
                for(ArrayList<Car> lanes : road.getMatrix_S2E()){
                    for(Car car : lanes){
                        if(car.getId() != -1){
                            res.add(car);
                        }
                    }
                }

                for(ArrayList<Car> lanes : road.getMatrix_E2S()){
                    for(Car car : lanes){
                        if(car.getId() != -1){
                            res.add(car);
                        }
                    }
                }
            } else{
                for(ArrayList<Car> lanes : road.getMatrix_S2E()){
                    for(Car car : lanes){
                        if(car.getId() != -1){
                            res.add(car);
                        }
                    }
                }
            }
        }
        return res;
    }
    /**
     * 测试，获取已经到的车
     * */
    private static ArrayList<Car> getInEndCar(){
        ArrayList<Car> res = new ArrayList<>();
        for(Car car : carList){
            if(car.getId() != -1 && car.getCarState().isEnd()){
                res.add(car);
            }
        }
        return res;
    }

    /**
     * 现死锁或者其他车辆无法运行等意外情况跳出循环停止运行
     * */
    private static boolean conflict(ArrayList<Road> fullRoad){
        Set<List<Integer>> loopSet = FindLoop.findLoop(crossList, hasCarRoad());//找出当前成环的路的序号
        List<Integer> fullRoadId = new ArrayList<>();
        for(Road road : fullRoad){
            fullRoadId.add(road.getId());
        }
        if(loopSet.contains(fullRoadId)){
            return true;
        }
        return false;
    }

    /**
     * 将其中一条满的路封闭。破坏成环条件
     * 传入当前在路上的车辆，寻找包括这条路的,将这条路加入车的禁止名单，重新寻路
     * */
    private static void destroyLoop(ArrayList<Car> inRoadCar, Road forbidRoad){
        ArrayList<Road> forbidRoadList = new ArrayList<>();
        //加入禁止路，重新找路,不可能返回上一个时间状态？？？？
        forbidRoadList.add(forbidRoad);
        for(Car car : inRoadCar){
            if(car.getRoads().contains(forbidRoad)){
                car.setForbidRoads(forbidRoadList);
            }
        }
    }
}































/**暂时废弃方法**/
/**
 private static void driveAllCarJustOnRoadToEndState(){

 ArrayList<Car> carRun = new ArrayList<>();//保存当前能动的车
 for (Road road : roadList){//观察当前道路矩阵,取出当前所有在路上的车
 if(road.getDirected() == 1){    //如果是双向车道，则遍历两个矩阵
 //遍历S2E矩阵
 MatrixTemp = road.getMatrix_S2E();
 for(ArrayList<Car> lanesCar : MatrixTemp){
 for(Car lengthCar : lanesCar){
 if(lengthCar.getId() != -1){//设定-1为空车
 carRun.add(lengthCar);
 }
 }
 }
 //遍历E2S矩阵
 MatrixTemp = road.getMatrix_E2S();
 for(ArrayList<Car> lanesCar : MatrixTemp){
 for(Car lengthCar : lanesCar){
 if(lengthCar.getId() != -1){//设定-1为空车
 carRun.add(lengthCar);
 }
 }
 }
 } else{
 //只遍历从 S2E矩阵
 MatrixTemp = road.getMatrix_S2E();
 for(ArrayList<Car> lanesCar : MatrixTemp){
 for(Car lengthCar : lanesCar){
 if(lengthCar.getId() != -1){//设定-1为空车
 carRun.add(lengthCar);
 }
 }
 }
 }
 }
 //针对可动的车，判断当前道路行驶还是路口等待
 for(Car car : carRun){
 if(carInCross(car)){//当前车可以行到路口，进行通过和不通过
 Cross cross = getNowCross(car);//获取当前路口信息
 Road roadPriority = ThroughCrossRule.RoadPriority(cross);//优先通行的道路
 if(canCross(car)){ //如果可以通过
 int length = ThroughCrossRule.Pass_or_Not(cross, lane, roadList, crossList);//返回通过的距离
 if(length == 0){
 Road nowRoad = Util.getRoadFromId(car.getCarState().getRoadId(), roadList);
 //两种情况，1，该车准备通过路口，2，这个路口就是该车的终点
 if(car.getEnd() == nowRoad.getEnd() || car.getEnd() == nowRoad.getStart()){//在这条路正向或逆向行驶
 car.getCarState().setEnd(true);//标记已经到达终点
 }
 update(nowRoad);//表示车无法通过路口, 更新当前这条道路的矩阵
 } else{
 //更新下条路的矩阵
 Road nextRoad = Util.getRoadFromId(nextRoadId, roadList);
 update(nextRoad);
 }
 car.getCarState().setRunning(false);//此时设置行车完成
 }
 car.getCarState().setWait(true);//不能通过路口，设置为等待状态
 } else{
 //车不靠近路口，在当前道路行驶，更新当前道路矩阵
 Road nowRoad = Util.getRoadFromId(car.getCarState().getRoadId(), roadList);
 update(nowRoad);
 car.getCarState().setRunning(false);//表示行车完成
 }
 }
 }


 //对当前道路的每个车道进行遍历扫描
 for (int lane = 0; lane < road.getLanes(); lane++) {
 //获取当前车道的头车, 注意：
 //当前车已运动过，寻找下一辆相对意义上的头车
 //TODO
 int topCarId = road.get_Topcar_location_S2E(lane);//当前车道的头车,考虑加入car参数进去
 Car topCar = Util.getCarFromId(topCarId, carList);
 if (topCar == null) {//没有头车，说明当前车道为空
 continue;
 }
 if (//如果头车不过路口，就在当前道路上行驶) {
 int putLane = lane;//保持原车道不变
 int putLength = putLength();
 updateRoad(topCar, road, putLane, putLength, 1);//更新当前道路矩阵
 //更新车的状态信息
 topCar.getCarState().setLane(lane);//保持原车道不变
 topCar.getCarState().setPosition(putLength);
 topCar.getCarState().setRunning(false);//行动完毕
 topCar.getCarState().setWait(false);
 continue;
 }
 //此时头车要过路口   TODO : 存在问题,优先级这个怎么使用
 //考察我车所在车道是否具有行车控制权，没有则转让行车控制权给路口其他道路
 ThroughCrossRule.Go_control_priority(topCar, cross, roadList, crossList);
 int priority = cross.getControl_priority();
 //如果没有优先权，标记车辆等待,转让给其他道路
 Car priorityCar = Util.getCarFromId(priority, carList);
 if (priority != 0) {
 over = false;//标记是否可跳出循环
 priorityCar.getCarState().setWait(true);//不具有优先级，标记等待
 //TODO 之后怎么处理？？？
 } else {
 //开始通过路口
 int length = ThroughCrossRule.Pass_or_Not(cross, lane, roadList, crossList);//通过后行驶的距离
 if (length == 0) {//此时停车等待
 updateRoad(priorityCar, road, lane, 0, 1);//0表示路口？
 }
 deleteRoadCar(priorityCar, road, 1);//更新本条路的矩阵,删除该车
 Road nextRoad = getCarNextRoad(priorityCar);//获取下一条路
 int putLane = putLane();
 int putLength = length;
 updateRoad(priorityCar, nextRoad, putLane, putLength, 1);//更新下一条路的矩阵
 //标记车为已行驶完毕
 priorityCar.getCarState().setRunning(false);
 priorityCar.getCarState().setWait(false);
 }
 }
 **/