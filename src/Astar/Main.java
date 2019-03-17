package Astar;

import DataStruct.Car;
import DataStruct.Cross;
import DataStruct.Road;
import IO_Process.IOProcess;

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

            //写入结果
            String writePath = readFile + config + "answer.txt";
            IOProcess.WriteFile(writePath, carList);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
