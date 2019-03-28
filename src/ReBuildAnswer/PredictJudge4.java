package ReBuildAnswer;

/**
 * @Author: pyh
 * @Date: 2019/3/27 8:09
 * @Version 1.0
 * @Function:
 *      只判断路上的车是否出现死锁
 */

import DataStruct.*;
import IO_Process.SerializableTest;
import Judgment.FindLoop;
import Util.Util;
import java.util.*;

public class PredictJudge4 {


    private static ArrayList<Car> carList;
    private static ArrayList<Cross> crossList;
    private static ArrayList<Road> roadList;

    /**
     * 此函数只考虑将路上的车行进完毕
     *
     * @param forbidRoadIdList  统计应该禁止的路
     *
     * @return forbidRoadIdList : 禁止路列表
     * @return 返回当前道路上的车是否可以走完
     * */
    public static boolean judgeNoDeadLock(ArrayList<Road> roads, ArrayList<Car> cars, ArrayList<Cross> crosses, ArrayList<Integer> forbidRoadIdList){
        /**************************数据预处理***************************/
        /**此处必须深拷贝，否则会出现已跑完的现象，需要完全一样的代码副本，实现clone接口**/
        roadList = new ArrayList<>(roads);
        carList = new ArrayList<>(cars);
        crossList = new ArrayList<>(crosses);

        /**************************************************************/
        /* 按时间片处理 */
        int Time = 1;//从时间1开始
        boolean forceJump = false;// 是否是强制跳出
        //driveCarInGarage(nextCar);//发出指定的车,剩下没发出的不管
        for(; ;Time++) {
            /*****测试——到达终点的车****/
            ArrayList<Car> endCar = getInEndCar();
            ArrayList<Car> roadHasCar = getInRoadCar();
            /*****************************/

            //行车完成，将所有车设置为可行状态,除了已经到达终点的车
            setCarCanRun();

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

            /******************************************************************/
            //统计道路占有率
            //ArrayList<Road> overPercent = new ArrayList<>();//加入超过某一占有率的路
            //for(Road road : roadList){
            //    if(roadPercent(road) > 0.9){//道路占有率超过90%，加入
            //        overPercent.add(road);
            //    }
            //}
            //if(overPercent.size() >= 4){
            //    for(Road road : overPercent){
            //        forbidRoadIdList.add(road.getId());
            //    }
            //    return false;
            //}

            /******************************************************************/
            //此时寻找满路，看是否可以形成环
            ArrayList<Road> fullRoads = countFullRoad();//获取满的路
            //for(Road road : fullRoads){
            //    if(!forbidRoadIdList.contains(road.getId())){
            //        forbidRoadIdList.add(road.getId());
            //    }
            //}
            //if(Time % 5 == 0){
            //    if(forbidRoadIdList.size() >= 4){
            //        return false;
            //    } else {
            //        return true;
            //    }
            //}
            Set<List<Integer>> loop = FindLoop.findLoop(crossList, fullRoads);//根据满的路找环
            //如果有环，则我认为是失败的，否则，是成功的
            if (loop.size() > 0) {
                //记录成环路
                for (List<Integer> tempList : loop) {
                    forbidRoadIdList.addAll(tempList);
                }
                return false;//有环，失败
            } else if(Time % 20 == 0) {
                return true;//无环，成功
            }

            /****************************************************************/

            /* 道路无车，则运行完毕*/
            if(isAllRoadEmpty()){
                break;
            }
        }
        return true;
    }

    /**
     * 统计道路占有情况
     * */
    private static double roadPercent(Road road){
        double vol = road.getLength() * road.getLanes();
        int count = 0;
        double p1 = 0.0;
        double p2 = 0.0;
        if(road.getDirected() == 1){
            ArrayList<ArrayList<Car>> Matrix = road.getMatrix_E2S();
            for(ArrayList<Car> lanes : Matrix){
                for(Car car : lanes){
                    if(car.getId() != -1){
                        count++;
                    }
                }
            }

            p1 = count / vol;
        }
        count = 0;

        ArrayList<ArrayList<Car>> Matrix = road.getMatrix_S2E();
        for(ArrayList<Car> lanes : Matrix){
            for(Car car : lanes){
                if(car.getId() != -1){
                    count++;
                }
            }
        }

        p2 = count / vol;

        return Math.max(p1, p2);
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

    /**
     * 重构发车函数，指定车列表发车
     * */
    private static void driveCarInGarage(ArrayList<Car> cars){
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
            }
        }
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
            boolean jumpOut = false;
            /*当前道路优先级未转让时，一直循环遍历这条路的车道*/
            while(!changeRoad) {
                //对当前道路的每个车道进行遍历扫描
                for(int i = road.getLength()-1; i >= 0; i--) {
                    for (int lane = 0; lane < road.getLanes(); lane++) {

                        /*此时路上没wait的车，跳出*/
                        if (!isRoadHasCarWait(road, 1)) {
                            jumpOut = true;
                            break;
                        }

                        Car nowCar = road.getMatrix_S2E().get(lane).get(i);//获取当前点的车，不一定是头车
                        if (nowCar == null || nowCar.getId() == -1) {//不存在头车，说明此时车道为空
                            continue;//直接下一车道
                        }

                        //如果头车已经行驶过，本车道正常车辆行驶
                        int topCarLocation = road.get_Topcar_location_S2E(lane+1);
                        Car topCar = road.getMatrix_S2E().get(lane).get(topCarLocation);
                        if (!topCar.getCarState().isWait()) {
                            //正常行驶，将本车道的车行驶完
                            Car normalCar = road.getMatrix_S2E().get(lane).get(i);
                            if (normalCar.getId() != -1 && normalCar.getCarState().isWait()) {
                                deleteRoadCar(normalCar, road, 1);//删除这条路上的该车
                                ThroughRule.Untopcar_Go_distance(cross, lane, road, normalCar);//设置距离
                                updateRoad(normalCar, road, normalCar.getCarState().getLane(), normalCar.getCarState().getPosition(), 1);
                                normalCar.getCarState().setRunning(false);
                                normalCar.getCarState().setWait(false);
                            }

                            if (isThisRoadCarComplete(road, 1) && !isRoadHasCarWait(road, 1)) {//当前路上的车已经全部完成，跳出
                                jumpOut = true;
                                break;
                            }
                            continue;
                        }

                        boolean isUpdate = ThroughRule.Go_control_priority(nowCar, cross, roadList, crossList);//是否可以更新道路矩阵
                        if (isUpdate) {   //可以更新矩阵
                            //反复调用
                            int pass_or_not = ThroughRule.Pass_or_Not(cross, lane, roadList, crossList);
                            int real_distance = ThroughRule.GO_distance(cross, lane, roadList, crossList, pass_or_not);

                            //过路口
                            if (real_distance == -5 || pass_or_not == 0) {
                                //被堵住，在当前道路可行最大距离
                                deleteRoadCar(nowCar, road, 1);
                                nowCar.getCarState().setPosition(road.getLength() - 1);
                                updateRoad(nowCar, road, nowCar.getCarState().getLane(), road.getLength() - 1, 1);
                                nowCar.getCarState().setRunning(false);
                                nowCar.getCarState().setWait(false);//表示完成过路口这个动作
                                continue;
                            }
                            if (real_distance == -2) {
                                //到终点
                                nowCar.getCarState().setEnd(true);
                                deleteRoadCar(nowCar, road, 1);
                                continue;
                            }

                            deleteRoadCar(nowCar, road, 1);//删除这条路上的该车
                            Road nextRoad = getNextRoad(nowCar);
                            nowCar.getCarState().setRoadId(nextRoad.getId());//更新车的下一条路的状态
                            if (nextRoad.getEnd() == cross.getId()) {//以下一条路的方向判断更新矩阵
                                nowCar.getCarState().setLane(putLane(nextRoad, -1));
                                nowCar.getCarState().setPosition(real_distance - 1);//放置的是矩阵对应位置
                                updateRoad(nowCar, nextRoad, nowCar.getCarState().getLane(), nowCar.getCarState().getPosition(), -1);//放入的方向是否正确？
                            } else {
                                nowCar.getCarState().setLane(putLane(nextRoad, 1));
                                nowCar.getCarState().setPosition(real_distance - 1);//放置的是矩阵对应位置
                                updateRoad(nowCar, nextRoad, nowCar.getCarState().getLane(), nowCar.getCarState().getPosition(), 1);//放入的方向是否正确？
                            }
                            //更新状态
                            nowCar.getCarState().setRunning(false);//设置车辆状态信息， 已经行进完成
                            nowCar.getCarState().setWait(false);//表示已经通过路口
                        } else {
                            //不能更新矩阵，优先级转让,切换道路
                            jumpOut = true;
                            break;
                        }
                    }
                    if(jumpOut){
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
            boolean jumpOut = false;
            /*当前道路优先级未转让时，一直循环遍历这条路的车道*/
            while(!changeRoad) {
                //对当前道路的每个车道进行遍历扫描
                for(int i = road.getLength() - 1; i >= 0; i--) {
                    for (int lane = 0; lane < road.getLanes(); lane++) {

                        /*此时路上没wait的车，跳出*/
                        if (!isRoadHasCarWait(road, -1)) {
                            jumpOut = true;
                            break;
                        }

                        Car nowCar = road.getMatrix_E2S().get(lane).get(i);//获取当前点的车，不一定是头车
                        if (nowCar == null || nowCar.getId() == -1) {//不存在头车，说明此时车道为空
                            continue;//直接下一车道
                        }

                        int topCarLocation = road.get_Topcar_location_E2S(lane+1);
                        Car topCar = road.getMatrix_E2S().get(lane).get(topCarLocation);
                        //如果头车已经行驶过，本车道正常车辆行驶
                        if (!topCar.getCarState().isWait()) {
                            //正常行驶，将本车道的车行驶完
                            Car normalCar = road.getMatrix_E2S().get(lane).get(i);
                            if (normalCar.getId() != -1 && normalCar.getCarState().isWait()) {
                                deleteRoadCar(normalCar, road, -1);//删除这条路上的该车
                                ThroughRule.Untopcar_Go_distance(cross, lane, road, normalCar);//设置距离
                                updateRoad(normalCar, road, normalCar.getCarState().getLane(), normalCar.getCarState().getPosition(), -1);
                                normalCar.getCarState().setRunning(false);
                                normalCar.getCarState().setWait(false);
                            }

                            if (isThisRoadCarComplete(road, -1) && !isRoadHasCarWait(road, -1)) {//当前路上的车已经全部完成，跳出
                                jumpOut = true;
                                break;
                            }
                            continue;
                        }

                        boolean isUpdate = ThroughRule.Go_control_priority(nowCar, cross, roadList, crossList);//是否可以更新道路矩阵
                        if (isUpdate) {   //可以更新矩阵
                            //反复调用
                            int pass_or_not = ThroughRule.Pass_or_Not(cross, lane, roadList, crossList);
                            int real_distance = ThroughRule.GO_distance(cross, lane, roadList, crossList, pass_or_not);

                            //过路口
                            if (real_distance == -5 || pass_or_not == 0) {//TODO 存在问题
                                //被堵住，在当前道路可行最大距离
                                deleteRoadCar(nowCar, road, -1);
                                nowCar.getCarState().setPosition(road.getLength() - 1);
                                updateRoad(nowCar, road, nowCar.getCarState().getLane(), road.getLength() - 1, -1);//顶到头
                                nowCar.getCarState().setRunning(false);
                                nowCar.getCarState().setWait(false);//表示完成过路口这个动作
                                continue;
                            }
                            if (real_distance == -2) {
                                //到终点
                                nowCar.getCarState().setEnd(true);
                                deleteRoadCar(nowCar, road, -1);
                                continue;
                            }


                            deleteRoadCar(nowCar, road, -1);//删除这条路上的该车
                            Road nextRoad = getNextRoad(nowCar);
                            nowCar.getCarState().setRoadId(nextRoad.getId());//更新车的下一条路的状态
                            if (nextRoad.getEnd() == cross.getId()) {//以下一条路的方向判断更新矩阵
                                nowCar.getCarState().setLane(putLane(nextRoad, -1));
                                nowCar.getCarState().setPosition(real_distance - 1);//放置的是矩阵对应位置
                                updateRoad(nowCar, nextRoad, nowCar.getCarState().getLane(), nowCar.getCarState().getPosition(), -1);//放入的方向是否正确？
                            } else {
                                nowCar.getCarState().setLane(putLane(nextRoad, 1));
                                nowCar.getCarState().setPosition(real_distance - 1);//放置的是矩阵对应位置
                                updateRoad(nowCar, nextRoad, nowCar.getCarState().getLane(), nowCar.getCarState().getPosition(), 1);//放入的方向是否正确？
                            }
                            //更新状态
                            nowCar.getCarState().setRunning(false);//设置车辆状态信息， 已经行进完成
                            nowCar.getCarState().setWait(false);//已经通过路口
                        } else {
                            //不能更新矩阵，优先级转让,切换道路
                            jumpOut = true;
                            break;
                        }
                    }
                    if(jumpOut){
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
    private static ArrayList<Road> countFullRoad(){
        ArrayList<Road> res = new ArrayList<>();
        for(Road road : roadList){
            //所有矩阵都有S2E矩阵
            ArrayList<ArrayList<Car>> Matrix = road.getMatrix_S2E();
            boolean flag = false;//跳出多层循环
            boolean canAdd = true;
            for(ArrayList<Car> lanes : Matrix){
                for(Car car : lanes){
                    if(car.getId() == -1){//说明有空位
                        flag = true;
                        break;
                    }
                }
                if(flag){
                    canAdd = false;
                    break;
                }
            }

            if(canAdd){res.add(road);}//添加进结果集

            /**************************************************************/
            if(road.getDirected() == 1 && !res.contains(road)){
                //此时遍历E2S矩阵
                Matrix = road.getMatrix_E2S();
                flag = false;//跳出多层循环
                canAdd = true;
                for(ArrayList<Car> lanes : Matrix){
                    for(Car car : lanes){
                        if(car.getId() == -1){
                            flag = true;
                            break;
                        }
                    }
                    if(flag){
                        canAdd = false;
                        break;
                    }
                }

                if(canAdd){res.add(road);}
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
        Road NextRoad = Util.getRoadFromId(path.get(k+1).getId(), roadList);//下一条要行驶的道路   k+1
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

}


