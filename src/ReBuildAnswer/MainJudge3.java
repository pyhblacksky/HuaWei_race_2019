package ReBuildAnswer;

/**
 * @Author: pyh
 * @Date: 2019/3/26 15:20
 * @Version 1.0
 * @Function:
 *      根据副判题器返回的结果，动态规划当前的路径
 */

import Astar.AFind;
import DataStruct.*;
import IO_Process.SerializableTest;
import Util.Util;
import java.util.*;

public class MainJudge3 {

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
    public static ArrayList<Answer> buildAnswer(ArrayList<Car> cars, ArrayList<Cross> crosses,
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
        ArrayList<Answer> finalAnswer = new ArrayList<>();//最终生成的Answer

        rebuildState();
        /* 按时间片处理 */
        int Time = 1;//从时间1开始
        for(; ;Time++) {
            //rebuildState();
            /*****测试——到达终点的车****/
            ArrayList<Car> endCar = getInEndCar();
            //System.out.println(endCar);
            ArrayList<Car> roadHasCar = getInRoadCar();
            /******测试——计算已满路******/
            ArrayList<Road> fullRoad = countFullRoad();//计算已满路
            /*****************************/

            /******************第一步********************/
            /*车道直线行驶的车,判断 是否有可执行或者不等待状态 的车*/
            while(HasCarCanRunOrNotWait()){
                /* driveAllWaitCar() */
                //如果路上没车，跳过
                if(isAllRoadEmpty()){
                    break;
                }
                //无论等待还是行进完成的车，运行过后running都设置为false
                for(Road road : roadList){
                    driveNormalRoadCar(road);
                }
            }

            /**********************第二步**************************/
            /*要过路口的车, 存在等待状态，不存在等待状态的车则跳过*/
            while(isExistWaitingCar()){
                //如果路上没车，跳过
                if(isAllRoadEmpty()){
                    break;
                }

                //按道路ID升序进行调度，只调度该道路出路口的方向
                for(Cross cross : crossList){
                    //获取该路口的道路优先级
                    ArrayList<Road> priorityRoad = ThroughRule.RoadPriority(cross);//按照道路序号升序
                    while(isCrossAllRoadCarWait(cross, priorityRoad)) {
                        for (Road road : priorityRoad) {
                            driveCrossCar(road, cross);
                        }
                    }
                }
            }

            if(isAllRoadEmpty()){//如果道路为空，发出第一批车
                ArrayList<Car> canRunCar = getInGarageCar();//getInGarageCar()还在车库中的车
                ArrayList<Car> nextCars = getPartList(canRunCar, 0, Math.min(canRunCar.size(), 300));
                ArrayList<Car> realCarList = driveFirstCarInGarage(nextCars);//默认这批次车可跑通，此处可调试
                //生成并记录answer
                //ArrayList<Car> inRoadCar = getInRoadCar();
                for(Car car : realCarList){
                    Answer answer = new Answer(car.getId(), Time + car.getRealTime(), car.getRoads());
                    finalAnswer.add(answer);
                }
            } else{
                if(Time % 3 == 0) {//5个时间单位操作一次?
                    /*假设发一批车，如果当前路上的车没有出现死锁，则我认为可行，正常发车,并添加进结果集*/
                    ArrayList<Car> inRoadCar = getInRoadCar();//当前在路上的车
                    ArrayList<Car> canRunCar = getInGarageCar();//getInGarageCar()还在车库中的车
                    if(canRunCar.size() != 0) {//此时可发车数不小于0才执行以下的行为
                        int count = 0;//计算进行了几次调整循环
                        int start = 0;
                        int end = Math.min(canRunCar.size(), 200);
                        while(true) {//调整直到可行
                            ArrayList<Car> nextCars = getPartList(canRunCar, start, end);//下一批要发出的车,可调节
                            ArrayList<Integer> forbidRoadList = new ArrayList<>();//禁止路列表
                            // /**********************序列化操作************************/
                            SerializableTest.AllSerialize(crossList, roadList, inRoadCar, nextCars, "AllObject.txt");
                            /*******************************************************/
                            if(PredictJudge3.judgeNoDeadLock(forbidRoadList)){//表示无环，说明成功，发车并添加入结果集
                                ArrayList<Car> realCarList = driveCarInGarage(nextCars);//本次真实可发车数
                                //此时返回可行，生成并记录answer，真实的发出这批车
                                for (Car car : realCarList) {
                                    //此处answer的时间是   当前时间+车在测试集中的实际发车时间
                                    car.setRealTime(car.getTime());
                                    Answer answer = new Answer(car.getId(), Time + car.getRealTime(), car.getRoads());
                                    finalAnswer.add(answer);
                                }
                                break;
                            } else{
                                //禁止的路
                                ArrayList<Road> forbidRoad = new ArrayList<>();
                                for(int i : forbidRoadList){
                                    forbidRoad.add(Util.getRoadFromId(i, roadList));
                                }

                                //调整当前道路上的车，改变其接下来的道路
                                reFindCarRoads(getInRoadCar(), forbidRoad);
                                break;
                                //rebuildState();
                            }
                            //if(count > 3){
                            //    break;
                            //}
                            //count++;
                        }
                    }
                }
            }

            //rebuildState();

            //行车完成，将所有车设置为可行状态,除了已经到达终点的车
            setCarCanRun();

            /* 车库无车，道路无车，则运行完毕*/
            if(isGarageEmpty() && isAllRoadEmpty()){
                break;
            }

        }
        return finalAnswer;
    }

    /**
     * 查看该路口的路的车是否已经进行完等待状态
     * */
    private static boolean isCrossAllRoadCarWait(Cross cross, ArrayList<Road> roads){
        for(Road road : roads){
            //根据road的起点终点判断遍历哪个矩阵
            if(road.getEnd() == cross.getId()){
                ArrayList<ArrayList<Car>> matrix = road.getMatrix_S2E();
                for(ArrayList<Car> lanes : matrix){
                    for(Car car : lanes){
                        if(car.getId() != -1 && car.getCarState().isWait()){
                            return true;
                        }
                    }
                }
            } else if(road.getStart() == cross.getId()){
                //双向，先遍历E2S
                ArrayList<ArrayList<Car>> matrix = road.getMatrix_E2S();
                for(ArrayList<Car> lanes : matrix){
                    for(Car car : lanes){
                        if(car.getId() != -1 && car.getCarState().isWait()){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /***
     * 重新寻路
     * */
    private static void reFindCarRoads(ArrayList<Car> inRoadCar, ArrayList<Road> forbidRoad){
        for(Car car : inRoadCar){
            for(Road road : forbidRoad) {
                if(car.getRoads().contains(road)) {
                    //车包含禁止路才进行下一步
                    int nowRoadId = car.getCarState().getRoadId();//改车当前道路的id
                    int nextRoadId = 0;//该车下一条路的id
                    ArrayList<Road> nextRoadList = new ArrayList<>();//下条路列表
                    for(int i = 0; i < car.getRoads().size(); i++){
                        Road tempRoad = car.getRoads().get(i);
                        if(tempRoad.getId() == nowRoadId && i+1 < car.getRoads().size()){
                            nextRoadId = car.getRoads().get(i+1).getId();
                            while(i < car.getRoads().size()){
                                nextRoadList.add(car.getRoads().get(i));
                                i++;
                            }
                            break;
                        }
                    }
                    if(!nextRoadList.contains(road)){
                        break;
                    }
                    if(nextRoadId == 0){//说明达到终点，不作处理
                        continue;
                    }
                    /*******从点重新寻路*******/
                    Cross nowCross = Util.getCrossFromTwoRoad(nowRoadId, nextRoadId, crossList);
                    Cross finishCross = Util.getCrossFromId(car.getEnd(), crossList);
                    if(nowCross == null || finishCross == null){
                        continue;
                    }
                    //重新寻路
                    AFind.AFindPath(crossList, car, nowCross, finishCross, forbidRoad, nowRoadId, roadList);
                }
            }
        }
    }

    /**
     * 状态重设
     * */
    private static void rebuildState(){
        for(Car car : carList){
            ArrayList<Road> getInRoadCarList = car.getRoads();
            ArrayList<Road> setInRoadCarList = new ArrayList<>();
            for(Road road : getInRoadCarList){
                setInRoadCarList.add(Util.getRoadFromId(road.getId(), roadList));
            }
            car.setRoads(setInRoadCarList);
        }
        for(Cross cross : crossList){
            Road upRoad = Util.getRoadFromId(cross.getUpRoad().getId(), roadList);
            if(upRoad != null) {
                cross.setUpRoad(upRoad);
            }
            Road downRoad = Util.getRoadFromId(cross.getDownRoad().getId(), roadList);
            if(downRoad != null) {
                cross.setDownRoad(downRoad);
            }
            Road leftRoad = Util.getRoadFromId(cross.getLeftRoad().getId(), roadList);
            if(leftRoad != null) {
                cross.setLeftRoad(leftRoad);
            }
            Road rightRoad = Util.getRoadFromId(cross.getRightRoad().getId(), roadList);
            if(rightRoad != null) {
                cross.setRightRoad(rightRoad);
            }
        }
    }

    /**
     * 过路口的情况
     * */
    private static void driveCrossCar(Road road, Cross cross){
        if (road.getEnd() == cross.getId()) {//此时说明是正向
            if(isRoadEmpty(road, 1)){//如果这条路上没车，直接进入下一条
                return;
            }
            if (nowRoadComplete(road, 1) && !isRoadHasCarWait(road, 1)) {//如果当前路上的车已经行进完毕且无等待的车，下一条路
                return;
            }

            //如果该路口的每条路上没有等待的车了,且车辆运行完毕，跳出循环
            if(!isRoadHasCarWait(road, 1) && isRoadCarComplete(road, 1)){
                return;
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

                    /*此时路上没wait的车，跳出*/
                    if(!isRoadHasCarWait(road, 1)){
                        changeRoad = true;
                        break;
                    }

                    Car topCar = road.getMatrix_S2E().get(lane).get(position);//获取车
                    if(topCar == null || topCar.getId() == -1){//不存在头车，说明此时车道为空
                        continue;//直接下一车道
                    }

                    //如果头车已经行驶过，本车道正常车辆行驶
                    if(!topCar.getCarState().isWait()){
                        //正常行驶，将本车道的车行驶完

                        int normalPosition = road.getWaitCarlocation_S2E(lane + 1);
                        if (normalPosition != -1) {
                            Car normalCar = road.getMatrix_S2E().get(lane).get(normalPosition);
                            if(normalCar.getCarState().isWait()) {
                                deleteRoadCar(normalCar, road, 1);//删除这条路上的该车
                                ThroughRule.Untopcar_Go_distance(cross, lane, road, normalCar);//设置距离
                                updateRoad(normalCar, road, normalCar.getCarState().getLane(), normalCar.getCarState().getPosition(), 1);
                                normalCar.getCarState().setRunning(false);
                                normalCar.getCarState().setWait(false);
                            }
                        }

                        if(isThisRoadCarComplete(road, 1) && !isRoadHasCarWait(road, 1)){//当前路上的车已经全部完成，跳出
                            changeRoad = true;
                            break;
                        }
                        continue;
                    }

                    boolean isUpdate = ThroughRule.Go_control_priority(topCar, cross, roadList, crossList);//是否可以更新道路矩阵
                    if (isUpdate) {   //可以更新矩阵
                        //反复调用
                        int pass_or_not = ThroughRule.Pass_or_Not(cross, lane, roadList, crossList);
                        int real_distance = ThroughRule.GO_distance(cross, lane, roadList, crossList, pass_or_not);

                        //过路口
                        if(real_distance == -5 || pass_or_not == 0){
                            //被堵住，在当前道路可行最大距离
                            deleteRoadCar(topCar, road, 1);
                            topCar.getCarState().setPosition(road.getLength()-1);
                            updateRoad(topCar, road, topCar.getCarState().getLane(), road.getLength()-1, 1);
                            topCar.getCarState().setRunning(false);
                            topCar.getCarState().setWait(false);//表示完成过路口这个动作
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
                        if(nextRoad.getEnd() == cross.getId()) {//以下一条路的方向判断更新矩阵
                            topCar.getCarState().setLane(putLane(nextRoad, -1));
                            topCar.getCarState().setPosition(real_distance-1);//放置的是矩阵对应位置
                            updateRoad(topCar, nextRoad, topCar.getCarState().getLane(), topCar.getCarState().getPosition(), -1);//放入的方向是否正确？
                        } else {
                            topCar.getCarState().setLane(putLane(nextRoad, 1));
                            topCar.getCarState().setPosition(real_distance-1);//放置的是矩阵对应位置
                            updateRoad(topCar, nextRoad, topCar.getCarState().getLane(), topCar.getCarState().getPosition(), 1);//放入的方向是否正确？
                        }
                        //更新状态
                        topCar.getCarState().setRunning(false);//设置车辆状态信息， 已经行进完成
                        topCar.getCarState().setWait(false);//表示已经通过路口
                    } else {
                        //不能更新矩阵，优先级转让,切换道路
                        changeRoad = true;
                        break;
                    }
                }
            }

        } else if (road.getStart() == cross.getId()) {//此时说明是逆向
            if(isRoadEmpty(road, -1)){//如果这条路上没车，直接进入下一条
                return;
            }
            if (nowRoadComplete(road, -1)  && !isRoadHasCarWait(road, -1)) {//如果当前路上的车已经行进完毕，下一条路
                return;
            }

            //如果该路口的每条路上没有等待的车了,且车辆运行完毕，跳出循环
            if(!isRoadHasCarWait(road, -1) && isRoadCarComplete(road, -1)){
                return;
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

                    /*此时路上没wait的车，跳出*/
                    if(!isRoadHasCarWait(road, -1)){
                        changeRoad = true;
                        break;
                    }

                    Car topCar = road.getMatrix_E2S().get(lane).get(position);//获取车
                    if(topCar == null || topCar.getId() == -1){//不存在头车，说明此时车道为空
                        continue;//直接下一车道
                    }

                    //如果头车已经行驶过，本车道正常车辆行驶
                    if(!topCar.getCarState().isWait()){
                        //正常行驶，将本车道的车行驶完
                        int normalPosition = road.getWaitCarlocation_E2S(lane + 1);
                        if (normalPosition != -1) {
                            Car normalCar = road.getMatrix_E2S().get(lane).get(normalPosition);
                            if(normalCar.getCarState().isWait()) {
                                deleteRoadCar(normalCar, road, -1);//删除这条路上的该车
                                ThroughRule.Untopcar_Go_distance(cross, lane, road, normalCar);//设置距离
                                updateRoad(normalCar, road, normalCar.getCarState().getLane(), normalCar.getCarState().getPosition(), -1);
                                normalCar.getCarState().setRunning(false);
                                normalCar.getCarState().setWait(false);
                            }
                        }

                        if(isThisRoadCarComplete(road, -1) && !isRoadHasCarWait(road, -1)){//当前路上的车已经全部完成，跳出
                            changeRoad = true;
                            break;
                        }
                        continue;
                    }

                    boolean isUpdate = ThroughRule.Go_control_priority(topCar, cross, roadList, crossList);//是否可以更新道路矩阵
                    if (isUpdate) {   //可以更新矩阵
                        //反复调用
                        int pass_or_not = ThroughRule.Pass_or_Not(cross, lane,roadList, crossList);
                        int real_distance = ThroughRule.GO_distance(cross, lane, roadList, crossList, pass_or_not);

                        //过路口
                        if(real_distance == -5 || pass_or_not == 0){//TODO 存在问题
                            //被堵住，在当前道路可行最大距离
                            deleteRoadCar(topCar, road, -1);
                            topCar.getCarState().setPosition(road.getLength()-1);
                            updateRoad(topCar, road, topCar.getCarState().getLane(), road.getLength()-1, -1);//顶到头
                            topCar.getCarState().setRunning(false);
                            topCar.getCarState().setWait(false);//表示完成过路口这个动作
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
                        if(nextRoad.getEnd() == cross.getId()) {//以下一条路的方向判断更新矩阵
                            topCar.getCarState().setLane(putLane(nextRoad, -1));
                            topCar.getCarState().setPosition(real_distance-1);//放置的是矩阵对应位置
                            updateRoad(topCar, nextRoad, topCar.getCarState().getLane(), topCar.getCarState().getPosition(), -1);//放入的方向是否正确？
                        } else {
                            topCar.getCarState().setLane(putLane(nextRoad, 1));
                            topCar.getCarState().setPosition(real_distance-1);//放置的是矩阵对应位置
                            updateRoad(topCar, nextRoad, topCar.getCarState().getLane(), topCar.getCarState().getPosition(), 1);//放入的方向是否正确？
                        }
                        //更新状态
                        topCar.getCarState().setRunning(false);//设置车辆状态信息， 已经行进完成
                        topCar.getCarState().setWait(false);//已经通过路口

                    } else {
                        //不能更新矩阵，优先级转让,切换道路
                        changeRoad = true;
                        break;
                    }
                }
            }
        }
    }

    /**
     * 判断 是否有可执行或者不等待状态 的车!!!注意此处的逻辑判定！！！
     *
     * 仅仅判断running，因为wait只在部分车中设置
     * */
    private static boolean HasCarCanRunOrNotWait(){
        ArrayList<Car> inRoadCar = new ArrayList<>(getInRoadCar());//获取在路上的所有车
        //遍历每一辆车
        for(Car car : inRoadCar){
            if(car.getCarState().isRunning()){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断 是否存在wait的车
     * */
    private static boolean isExistWaitingCar(){
        for(Road road : roadList){
            if(road.getDirected() == 1){
                //此时遍历E2S
                ArrayList<ArrayList<Car>> Matrix = road.getMatrix_E2S();
                for(ArrayList<Car> lanes : Matrix){
                    for(Car car : lanes){
                        if(car.getId() != -1 && car.getCarState().isWait()){//wait = true
                            return true;
                        }
                    }
                }
            }
            //遍历S2E矩阵
            ArrayList<ArrayList<Car>> Matrix = road.getMatrix_S2E();
            for(ArrayList<Car> lanes : Matrix){
                for(Car car : lanes){
                    if(car.getId() != -1 && car.getCarState().isWait()){
                        return true;
                    }
                }
            }
        }

        return false;//没有处于wait状态的车
    }

    /**
     * 将路上的车行驶，标记为行驶完毕状态
     * */
    private static void driveNormalRoadCar(Road road){
        if(road.getDirected() == 1){
            //多遍历一个E2S矩阵
            //遍历E2S矩阵
            ArrayList<ArrayList<Car>> Matrix = road.getMatrix_E2S();
            for(int lane = 0; lane < Matrix.size(); lane++){
                //从尾部遍历
                for(int i = Matrix.get(lane).size()-1; i >= 0; i--){
                    Car nowCar = Matrix.get(lane).get(i);
                    if(nowCar.getId() != -1){//不为空车
                        if(carCanThroughCross(nowCar, road, -1)){//如果车能通过路口,标记为等待
                            nowCar.getCarState().setWait(true);
                            nowCar.getCarState().setRunning(false);
                        } else {
                            //车向前走
                            //车辆如果行驶过程中，前方没有阻挡并且也不会出路口
                            if(!isCarStop(nowCar, road, lane, i)){//没有车阻挡
                                //TODO:更新状态和矩阵
                                //该车辆行驶可行驶的最大车速（v=min(最大车速，道路限速)）
                                // 此时该车辆在本次调度确定了该时刻的终止位置。该车辆标记为终止状态
                                int maxLen = Math.min(nowCar.getMaxSpeed(), road.getMaxSpeed());
                                int realDistance = nowCar.getCarState().getPosition() + maxLen;
                                if(carInS2E(nowCar, road)){//车在S2E矩阵中
                                    deleteRoadCar(nowCar, road, 1);//删除原车
                                    updateRoad(nowCar, road, lane, realDistance, 1);//更新矩阵
                                    //设置状态信息
                                    nowCar.getCarState().setPosition(realDistance);
                                } else if(carInE2S(nowCar, road)){//车在E2S矩阵中
                                    deleteRoadCar(nowCar, road, -1);//删除原车
                                    updateRoad(nowCar, road, lane, realDistance, -1);//更新矩阵
                                    //设置状态信息
                                    nowCar.getCarState().setPosition(realDistance);
                                }
                                nowCar.getCarState().setRunning(false);
                                continue;//下一辆车
                            }

                            //车辆如果行驶过程中，发现前方有车辆阻挡，且阻挡的车辆为等待车辆，则该辆车也被标记为等待行驶车辆
                            if(isCarStop(nowCar, road, lane, i) && getStopCar(nowCar, road, lane, i).getCarState().isWait()){
                                //此处不用更新矩阵
                                nowCar.getCarState().setWait(true);//标记该车为等待行驶车辆
                                nowCar.getCarState().setRunning(false);//行驶完成
                                continue;
                            }

                            //车辆如果行驶过程中，发现前方有车辆阻挡，且阻挡的车辆为终止状态车辆，则该辆车也被标记为终止车辆。
                            if(isCarStop(nowCar, road, lane, i) && !getStopCar(nowCar, road, lane, i).getCarState().isRunning()){
                                //TODO:更新状态和矩阵,有前车阻挡的情况
                                Car StopCar = getStopCar(nowCar, road, lane, i);
                                int carDistance = StopCar.getCarState().getPosition() - nowCar.getCarState().getPosition() - 1;//计算前车和本车的距离
                                int maxLen = Math.min(nowCar.getMaxSpeed(), road.getMaxSpeed());
                                int realDistance = nowCar.getCarState().getPosition()+Math.min(maxLen, carDistance);
                                if(carInS2E(nowCar, road)){//车在S2E矩阵中
                                    deleteRoadCar(nowCar, road, 1);//删除原车
                                    updateRoad(nowCar, road, lane, realDistance, 1);//更新矩阵
                                    //设置状态信息
                                    nowCar.getCarState().setPosition(realDistance);
                                } else if(carInE2S(nowCar, road)){
                                    deleteRoadCar(nowCar, road, -1);//删除原车
                                    updateRoad(nowCar, road, lane, realDistance, -1);//更新矩阵
                                    //设置状态信息
                                    nowCar.getCarState().setPosition(realDistance);
                                } else{
                                    System.out.println("车不在矩阵中");
                                }
                                nowCar.getCarState().setRunning(false);//行驶最大可行距离，标记
                            }
                        }
                    }
                }
            }

        }
        //遍历S2E矩阵
        ArrayList<ArrayList<Car>> Matrix = road.getMatrix_S2E();
        for(int lane = 0; lane < Matrix.size(); lane++){
            //从尾部遍历
            for(int i = Matrix.get(lane).size()-1; i >= 0; i--){
                Car nowCar = Matrix.get(lane).get(i);
                if(nowCar.getId() != -1){//不为空车
                    if(carCanThroughCross(nowCar, road, 1)){//如果车能通过路口,标记为等待
                        nowCar.getCarState().setWait(true);
                        nowCar.getCarState().setRunning(false);
                    } else {
                        //车向前走
                        //车辆如果行驶过程中，前方没有阻挡并且也不会出路口
                        if(!isCarStop(nowCar, road, lane, i)){//没有车阻挡
                            //TODO:更新状态和矩阵
                            //该车辆行驶可行驶的最大车速（v=min(最大车速，道路限速)）
                            // 此时该车辆在本次调度确定了该时刻的终止位置。该车辆标记为终止状态
                            int maxLen = Math.min(nowCar.getMaxSpeed(), road.getMaxSpeed());
                            int realDistance = nowCar.getCarState().getPosition() + maxLen;
                            if(carInS2E(nowCar, road)){//车在S2E矩阵中
                                deleteRoadCar(nowCar, road, 1);//删除原车
                                updateRoad(nowCar, road, lane, realDistance, 1);//更新矩阵
                                //设置状态信息
                                nowCar.getCarState().setPosition(realDistance);
                            } else if(carInE2S(nowCar, road)){
                                deleteRoadCar(nowCar, road, -1);//删除原车
                                updateRoad(nowCar, road, lane, realDistance, -1);//更新矩阵
                                //设置状态信息
                                nowCar.getCarState().setPosition(realDistance);
                            }  else{
                                System.out.println("车不在矩阵中");
                            }
                            nowCar.getCarState().setRunning(false);
                            continue;//下一辆车
                        }

                        //车辆如果行驶过程中，发现前方有车辆阻挡，且阻挡的车辆为等待车辆，则该辆车也被标记为等待行驶车辆
                        if(isCarStop(nowCar, road, lane, i) && getStopCar(nowCar, road, lane, i).getCarState().isWait()){
                            //此处不用更新矩阵
                            nowCar.getCarState().setWait(true);//标记该车为等待行驶车辆
                            nowCar.getCarState().setRunning(false);//行驶完成
                            continue;
                        }

                        //车辆如果行驶过程中，发现前方有车辆阻挡，且阻挡的车辆为终止状态车辆，则该辆车也被标记为终止车辆。
                        if(isCarStop(nowCar, road, lane, i) && !getStopCar(nowCar, road, lane, i).getCarState().isRunning()){
                            //TODO:更新状态和矩阵,有前车阻挡的情况
                            Car StopCar = getStopCar(nowCar, road, lane, i);
                            int carDistance = StopCar.getCarState().getPosition() - nowCar.getCarState().getPosition() - 1;//计算前车和本车的距离
                            int maxLen = Math.min(nowCar.getMaxSpeed(), road.getMaxSpeed());
                            int realDistance = nowCar.getCarState().getPosition()+Math.min(maxLen, carDistance);
                            if(carInS2E(nowCar, road)){//车在S2E矩阵中
                                deleteRoadCar(nowCar, road, 1);//删除原车
                                updateRoad(nowCar, road, lane, realDistance, 1);//更新矩阵
                                //设置状态信息
                                nowCar.getCarState().setPosition(realDistance);
                            } else if(carInE2S(nowCar, road)){
                                deleteRoadCar(nowCar, road, -1);//删除原车
                                updateRoad(nowCar, road, lane, realDistance, -1);//更新矩阵
                                //设置状态信息
                                nowCar.getCarState().setPosition(realDistance);
                            }  else{
                                System.out.println("车不在矩阵中");
                            }
                            nowCar.getCarState().setRunning(false);//行驶最大可行距离，标记
                        }
                    }
                }
            }
        }

    }

    /**
     * 确定车辆是否能过路口
     * PorN表示正反
     * */
    private static boolean carCanThroughCross(Car car, Road road, int PorN){
        Road nextRoad = getNextRoad(car);//获取车要行走的下一条路
        int nextRoadDistance;//下一条道路可行驶距离
        //如果获取到的下一条路为空，说明此时车过完路口即到达终点，下一条路可行使距离设置为0
        if(nextRoad == null || nextRoad.getId() == 0 || nextRoad.getId() == -1){
            nextRoadDistance = 0;
        } else {
            /*如果下一路可行最大距离有前车，则修正可行最大距离*/
            nextRoadDistance = Math.min(car.getMaxSpeed(), nextRoad.getMaxSpeed());
        }

        if( car.getCarState().getPosition() + Math.min(car.getMaxSpeed(), road.getMaxSpeed()) >= road.getLength()){
            return true;
        }

        int nowDistance = road.getLength() - 1 - car.getCarState().getPosition();
        if(nowDistance >= nextRoadDistance){
            return false;//不能过路口
        }


        return true;//可以过路口
    }

    /**
     * 判断是否有前车阻挡
     * */
    private static boolean isCarStop(Car car, Road road, int lane, int position){
        int maxLen = Math.min(car.getMaxSpeed(), road.getMaxSpeed());//最大可行距离
        if(road.getDirected() == 1){
            //E2S查找该车
            if(road.getMatrix_E2S().get(lane).get(position) == car){
                for(int i = position + 1; i < road.getMatrix_E2S().get(lane).size() && i <= position + maxLen; i++){
                    if(road.getMatrix_E2S().get(lane).get(i).getId() != -1){//此时说明有车
                        return true;
                    }
                }
            }
        }
        //S2E查找该车
        if(road.getMatrix_S2E().get(lane).get(position) == car){
            for(int i = position + 1; i < road.getMatrix_S2E().get(lane).size() && i <= position + maxLen; i++){
                if(road.getMatrix_S2E().get(lane).get(i).getId() != -1){//此时说明有车
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * 获取阻挡的前车
     * */
    private static Car getStopCar(Car car, Road road, int lane, int position){
        int maxLen = Math.min(car.getMaxSpeed(), road.getMaxSpeed());//最大可行距离
        if(road.getDirected() == 1){
            //E2S查找该车
            if(road.getMatrix_E2S().get(lane).get(position) == car){
                for(int i = position + 1; i < road.getMatrix_E2S().get(lane).size() && i <= position + maxLen; i++){
                    Car stopCar = road.getMatrix_E2S().get(lane).get(i);
                    if(stopCar.getId() != -1){//此时说明有车
                        return stopCar;
                    }
                }
            }
        }
        //S2E查找该车
        if(road.getMatrix_S2E().get(lane).get(position) == car){
            for(int i = position + 1; i < road.getMatrix_S2E().get(lane).size() && i <= position + maxLen; i++){
                Car stopCar = road.getMatrix_S2E().get(lane).get(i);
                if(stopCar.getId() != -1){//此时说明有车
                    return stopCar;
                }
            }
        }
        return null;
    }

    /**
     * 车是否在S2E矩阵中
     * */
    private static boolean carInS2E(Car car, Road road){
        ArrayList<ArrayList<Car>> Matrix = road.getMatrix_S2E();
        for(ArrayList<Car> lane : Matrix){
            for(Car findCar : lane){
                if(findCar.getId() != -1 && findCar.getId() == car.getId()){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 车是否在E2S矩阵中
     * */
    private static boolean carInE2S(Car car, Road road){
        ArrayList<ArrayList<Car>> Matrix = road.getMatrix_E2S();
        for(ArrayList<Car> lane : Matrix){
            for(Car findCar : lane){
                if(findCar.getId() != -1 && findCar.getId() == car.getId()){
                    return true;
                }
            }
        }
        return false;
    }
    /**********************************************************************************************************/

    /**
     * 重构发车函数，指定车列表发车
     * 将这一批次车发出去
     * */
    private static ArrayList<Car> driveCarInGarage(ArrayList<Car> cars){
        ArrayList<Car> res = new ArrayList<>();//真正发出去的车
        for (Car car : cars) {
            if (car.getCarState().isEnd()) {  //如果该车已经到达终点，则不对这辆车做任何变化
                continue;
            }
            if (!car.getCarState().isInGarage()) {//如果该车已经不在车库，跳过
                continue;
            }
            Road goCarRoad = null;
            int carStart = -1;//获取当前车的起点
            if (car != null && car.getRoads() != null && car.getRoads().size() != 0) {
                goCarRoad = car.getRoads().get(0);//取该车的可行路list中的第一个
                carStart = car.getStart();
            }
            if(goCarRoad == null){
                continue;
            }
            if (isRoadFull(goCarRoad, car)) {  //如果前车已经将道路占满，则推迟发车
                car.setRealTime(car.getRealTime() + 1);
                continue;
            }
            //确定道路行驶方向,存在双向和单向两种车道，车的起点和路的起点相同
            if (carStart == goCarRoad.getStart()) {
                //此时放入 S2E矩阵
                int i = putLane(goCarRoad, 1);//表示车道
                int j = putLength(goCarRoad, car, 1);//表示放入长度
                if (i == -1 || j == -1) {//说明放不下，时间推迟，下一个车
                    car.setRealTime(car.getRealTime() + 1);
                    continue;
                }
                goCarRoad.setMatrix_S2E(car, i, j);
                //更新车的状态，当前所在道路及相关信息
                car.getCarState().setRoadId(goCarRoad.getId());
                car.getCarState().setLane(i);//设置车道
                car.getCarState().setPosition(j);//设置当前位置
                car.getCarState().setRunning(false);//车已行动完毕
                car.getCarState().setInGarage(false);//车从车库中发出
                res.add(car);
            } else if (carStart == goCarRoad.getEnd()) {
                //此时放入 E2S矩阵
                int i = putLane(goCarRoad, -1);
                int j = putLength(goCarRoad, car, -1);
                if (i == -1 || j == -1) {//说明放不下，时间推迟，下一个车
                    car.setRealTime(car.getRealTime() + 1);
                    continue;
                }
                goCarRoad.setMatrix_E2S(car, i, j);
                car.getCarState().setRoadId(goCarRoad.getId());
                car.getCarState().setLane(i);//设置车道
                car.getCarState().setPosition(j);//设置当前位置
                car.getCarState().setRunning(false);//车已行动完毕
                car.getCarState().setInGarage(false);//车从车库中发出
                res.add(car);
            }
        }
        return res;
    }

    /**
     * 发出第一批车
     * */
    private static ArrayList<Car> driveFirstCarInGarage(ArrayList<Car> cars){
        ArrayList<Car> res = new ArrayList<>();//真正发出去的车
        for (Car car : cars) {
            if (car.getCarState().isEnd()) {  //如果该车已经到达终点，则不对这辆车做任何变化
                continue;
            }
            if (!car.getCarState().isInGarage()) {//如果该车已经不在车库，跳过
                continue;
            }
            Road goCarRoad = null;
            int carStart = -1;//获取当前车的起点
            if (car != null && car.getRoads() != null && car.getRoads().size() != 0) {
                goCarRoad = car.getRoads().get(0);//取该车的可行路list中的第一个
                carStart = car.getStart();
            }
            if(goCarRoad == null){
                continue;
            }
            if (isRoadFull(goCarRoad, car)) {  //如果前车已经将道路占满，则推迟发车
                car.setTime(car.getTime() + 1);
                continue;
            }
            //确定道路行驶方向,存在双向和单向两种车道，车的起点和路的起点相同
            if (carStart == goCarRoad.getStart()) {
                //此时放入 S2E矩阵
                int i = putLane(goCarRoad, 1);//表示车道
                int j = putLength(goCarRoad, car, 1);//表示放入长度
                if (i == -1 || j == -1) {//说明放不下，时间推迟，下一个车
                    car.setRealTime(car.getRealTime() + 1);
                    continue;
                }
                goCarRoad.setMatrix_S2E(car, i, j);
                //更新车的状态，当前所在道路及相关信息
                car.getCarState().setRoadId(goCarRoad.getId());
                car.getCarState().setLane(i);//设置车道
                car.getCarState().setPosition(j);//设置当前位置
                car.getCarState().setRunning(false);//车已行动完毕
                car.getCarState().setInGarage(false);//车从车库中发出
                car.setRealTime(car.getTime());
                res.add(car);
            } else if (carStart == goCarRoad.getEnd()) {
                //此时放入 E2S矩阵
                int i = putLane(goCarRoad, -1);
                int j = putLength(goCarRoad, car, -1);
                if (i == -1 || j == -1) {//说明放不下，时间推迟，下一个车
                    car.setTime(car.getTime() + 1);
                    continue;
                }
                goCarRoad.setMatrix_E2S(car, i, j);
                car.getCarState().setRoadId(goCarRoad.getId());
                car.getCarState().setLane(i);//设置车道
                car.getCarState().setPosition(j);//设置当前位置
                car.getCarState().setRunning(false);//车已行动完毕
                car.getCarState().setInGarage(false);//车从车库中发出
                car.setRealTime(car.getTime());
                res.add(car);
            }
        }
        return res;
    }

    /**
     * 判断当前车列表中是否有不在车库中的车
     * */
    private static boolean hasInGarageCar(ArrayList<Car> cars){
        for(Car car : cars){
            if(car.getId() != -1 && car.getCarState().isInGarage()){//存在在车库中的车
                return true;
            }
        }
        return false;
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
                    car.getCarState().setLane(i);//设置车道
                    car.getCarState().setPosition(j);//设置当前位置
                    car.getCarState().setRunning(false);//车已行动完毕
                    car.getCarState().setInGarage(false);//车从车库中发出
                    car.setRealTime(time);//设置车的真实发车时间
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
                    car.getCarState().setLane(i);//设置车道
                    car.getCarState().setPosition(j);//设置当前位置
                    car.getCarState().setRunning(false);//车已行动完毕
                    car.getCarState().setInGarage(false);//车从车库中发出
                    car.setRealTime(time);//设置车的真实发车时间
                }
            } else{
                break;//超过本次时间，跳出
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
            //car.getCarState().setLane(putLane);
            //car.getCarState().setPosition(putLength);
        } else if(PorN == -1){
            //逆向，更新E2S矩阵
            road.setMatrix_E2S(car, putLane, putLength);
            //更新车的状态
            //car.getCarState().setLane(putLane);
            //car.getCarState().setPosition(putLength);
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
                    if(road.getMatrix_S2E().get(i).get(j).getId() == car.getId()){
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
                    if(car.getId() != -1 && car.getCarState().isWait()){
                        return true;
                    }
                }
            }
        } else if(PorN == -1){
            MatrixTemp = road.getMatrix_E2S();
            for(ArrayList<Car> lanes : MatrixTemp){
                for(Car car : lanes){
                    if(car.getId() != -1 && car.getCarState().isWait()){
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
                        //car.getCarState().setLane(i);//设置所放车道
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
                        //car.getCarState().setLane(i);//设置所放车道
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
            //car.getCarState().setPosition(position);
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
            //car.getCarState().setPosition(position);
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
     * 判断本车道的车是否行驶完成
     * PorN:正向还是反向， PorN == -1；表示反， PorN == 1表示正
     *
     * 如果当前道路有未完成的车，返回false
     * */
    private static boolean isThisLaneCarComplete(Road road, int lane, int PorN){
        if(PorN == 1){
            //遍历S2E矩阵
            ArrayList<Car> nowLane = road.getMatrix_S2E().get(lane);
            for(Car car : nowLane){
                if(car.getId() != 0 && car.getCarState().isRunning()){
                    return false;
                }
            }
        } else if(PorN == -1){
            //遍历E2S矩阵
            ArrayList<Car> nowLane = road.getMatrix_E2S().get(lane);
            for(Car car : nowLane){
                if(car.getId() != 0 && car.getCarState().isRunning()){
                    return false;
                }
            }
        }
        return true;
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
     * 检测已经离开车库的车
     * 已浅拷贝
     * */
    private static ArrayList<Car> departGarage(){
        ArrayList<Car> res = new ArrayList<>();
        for(Car car : carList){
            if(car.getId() != -1 && !car.getCarState().isInGarage()){
                res.add(new Car(car));
            }
        }
        return res;
    }

    /**
     * 获取还在车库中的车。  相当于此时可选的发车车辆
     * 不使用浅拷贝
     * */
    private static ArrayList<Car> getInGarageCar(){
        ArrayList<Car> res = new ArrayList<>();
        for(Car car : carList){
            if(car.getId() != -1 && car.getCarState().isInGarage()){
                res.add(car);
            }
        }
        return res;
    }

    /**
     * 取出部分Car的操作函数
     * 返回一个目标列表
     * */
    private static ArrayList<Car> getPartList(ArrayList<Car> cars, int start, int end){
        if(start < 0 || start > cars.size() || end < 0 || end > cars.size() || start > end){
            throw new IllegalArgumentException("当前要截取的数据长度  小于或大于整个列表的长度");
        }
        if(end > cars.size()){
            end = cars.size();//调整长度防止越界
        }

        ArrayList<Car> res = new ArrayList<>();
        for(int i = start; i < end; i++){
            res.add(cars.get(i));
        }
        return res;
    }
}

