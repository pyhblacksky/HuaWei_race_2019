package Astar;

import DataStruct.*;
import IO_Process.BuildAnswer;
import IO_Process.IOProcess;
import Judgment.DeadLock;
import Judgment.FindLoop;
import Judgment.Judge;
import Judgment.ThroughCrossRule;
import Util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * @Author: pyh
 * @Date: 2019/3/8 9:27
 * @Version 1.0
 * @Function:
 *      测试函数
 *
 */
public class Main {

    public static void main(String[] args){
        try {
            /*********************************************读取文件********************************************/
            String readFile = "src\\Data\\bin\\";
            String config = "config_11\\";
            //读取道路
            String pathNameRoad = readFile + config +"Road.txt";//文件绝对路径
            ArrayList<String> roadStr = new ArrayList<>();
            IOProcess.ReadFile(pathNameRoad, roadStr);

            //读取车次
            String pathNameCar = readFile + config + "Car.txt";
            ArrayList<String> carStr = new ArrayList<>();
            IOProcess.ReadFile(pathNameCar, carStr);

            //读取交叉路口
            String pathNameCross = readFile + config + "Cross.txt";
            ArrayList<String> crossStr = new ArrayList<>();
            IOProcess.ReadFile(pathNameCross, crossStr);

            /***************************************数据读取完毕***************************************/

            ArrayList<Car> carList = IOProcess.StringToCar(carStr);
            HashMap<Integer, Road> map = new HashMap<>();
            ArrayList<Road> roadList = IOProcess.StringToRoad(roadStr, map);
            ArrayList<Cross> crossList = IOProcess.StringToCross(crossStr,map);

            /****************************************数据预处理完毕****************************************/

            //寻路
            long sumTimeStart = System.currentTimeMillis();
            for(Car car : carList){
                AFind.AFindPath(crossList, car);
            }

            /************************************路径重寻***********************************/
            //reRoad.reFind(carList, crossList , 10);
            ////统计是否有没有找到路的车，如果存在，降低禁止路的数量
            //int count = 0;
            //for(Car car : carList){
            //    if(car.getRoads() == null && car.getRoads().size() == 0){
            //        count++;
            //    }
            //}
            //System.out.println("存在没有通路的车：" + count);

            //Through函数测试——测试优先级
            //Cross crossTest = crossList.get(16);
            //Road roadTest = ThroughCrossRule.RoadPriority(crossTest);
            //System.out.println("路口"+crossTest.getId()+"优先级最高的通路是"+roadTest.getId());

            //Through函数测试——测试车辆状态，即行进方向
            //Car carTest = carList.get(1);
            //System.out.print(carTest.getId()+"行车路径为：");
            //for(Road road : carTest.getRoads()){
            //    if(road.getId() != 0){
            //        System.out.print(road.getId() + " ");
            //    }
            //}
            //System.out.println();
            ////注入carState
            //CarState carState = new CarState(5078, 1, 1);
            //carTest.setCarState(carState);
            //int d = ThroughCrossRule.JudgeDirection(carTest, roadList, crossList);
            //System.out.println("行驶方向为：" + d);

            //Through函数测试——道路优先级
            //将车放入道路矩阵
            //Util.getRoadFromId(5078, roadList).setMatrix_E2S(carList.get(10), 0,1);
            //Util.getRoadFromId(5070, roadList).setMatrix_E2S(carList.get(20),0,1);
            //Util.getRoadFromId(5071, roadList).setMatrix_E2S(carList.get(30),0,1);
            //int priority = ThroughCrossRule.Go_control_priority(carTest, Util.getCrossFromId(42, crossList), 1,roadList,crossList);
            //System.out.println("优先级为："+priority);
            //Pass_or_Not
            /******************************************************************************/
            //TODO:预测发车死锁，出现就回溯，重新寻路
            //假设car10739的5017号路不能走
            //ArrayList<Road> forbidRoadList = new ArrayList<>();
            //forbidRoadList.add(getRoadFromId(5017, roads));
            //AFind.AFindPath(crossList, getCarFromId(10739, carList), forbidRoadList);


            //对carList进行排序，按照car的权值进行排序
            Collections.sort(carList, new Comparator<Car>() {
                @Override
                public int compare(Car o1, Car o2) {
                    return o2.getWeight()-o1.getWeight();
                }
            });//根据路的权重排序

            long sumTimeEnd = System.currentTimeMillis();
            System.out.println("车的数量为"+carList.size()+ "时寻路花费的总时间为 ： " + (sumTimeEnd - sumTimeStart)+"ms");

            //计算道路总权值
            int allWeight = 0;
            for(Car car : carList){
                allWeight += car.getWeight();
            }
            System.out.println("道路总权值为：" + allWeight);

            //写入结果
            String writePath = readFile + config + "answer.txt";
            ArrayList<Answer> answerList = BuildAnswer.buildAnswer(carList);
            //ArrayList<Answer> answerList = BuildAnswer.buildAnswer(carList, roadList);
            /********************************************************************************/
            /**判题器测试*/
            int time = Judge.judge(carList, crossList, roadList, answerList);
            System.out.println(time);
            //找环测试
            //ArrayList<Road> testRoadList = new ArrayList<>(roadList.subList(10,50));
            //FindLoop.findLoop(crossList, testRoadList);
            //ArrayList<Road> testRoadList1 = new ArrayList<>(roadList.subList(0,50));
            //FindLoop.findLoop(crossList, testRoadList1);

            /******************************************************************************/
            //DeadLock.reFindRoad(carList, roadList, crossList, new ArrayList<>(answerList.subList(0, 100)));

            IOProcess.WriteFile(writePath, answerList);

            sumTimeEnd = System.currentTimeMillis();
            System.out.println("程序花费总时间为：" + (sumTimeEnd - sumTimeStart) + "ms");
            //System.out.println("answer大小：" + answerList.size());

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
