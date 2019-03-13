package Astar;

import DataStruct.Car;
import DataStruct.Cross;
import DataStruct.Road;
import IO_Process.IOProcess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @Author: pyh
 * @Date: 2019/3/8 9:27
 * @Version 1.0
 * @Function:   实现文件读取txt和写入txt
 *
 *
 */
public class Main {

    public static void main(String[] args){
        try {
            /*********************************************读取文件********************************************/
            String readFile = "src\\Data\\bin\\";
            String config = "config_10\\";
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

            long sumTimeStart = System.currentTimeMillis();
            int count = 0;//计算没有寻到路的数量
            for(Car car : carList){
                long startTime = System.currentTimeMillis();//计算程序运行时间

                HashMap<Integer, Road> map = new HashMap<>();
                ArrayList<Road> roads = IOProcess.StringToRoad(roadStr, map);
                ArrayList<Cross> crossList = IOProcess.StringToCross(crossStr,map);

                //A*寻路，返回一个Node链表
                AFind find = new AFind(crossList, car);
                AFind.Node node = find.findPath(new AFind.Node(AFind.getCross(car.getStart(), crossList)),
                        new AFind.Node(AFind.getCross(car.getEnd(), crossList)));

                ArrayList<Road> res = new ArrayList<>();
                //反转链表，因为结果集是反的
                AFind.Node newHead = null;
                while(node != null){
                    AFind.Node temp = node.parent;
                    node.parent = newHead;
                    newHead = node;
                    node = temp;
                }
                //添加结果集
                while(newHead != null){
                    if(newHead.road != null){
                        res.add(newHead.road);
                    }
                    newHead = newHead.parent;
                }
                if(res.size() == 0){count++;}
                System.out.print("car_id_"+ car.getId() + " 起点："+ car.getStart() + " 终点: " + car.getEnd() + "  所寻道路为：");
                for(Road road : res){
                    System.out.print(road.getId() + " ");
                }

                long endTime = System.currentTimeMillis();
                System.out.println("  程序运行时间为 ： " + (endTime - startTime) + "ms   ");
            }
            System.out.println("没有寻到路的数量为 ： " + count);
            long sumTimeEnd = System.currentTimeMillis();
            System.out.println("车的数量为"+carList.size()+ "时寻路花费的总时间为 ： " + (sumTimeEnd - sumTimeStart)+"ms");


            //写入结果
            /*
            String writePath = "src\\Data\\answer.txt";
            IOProcess.WriteFile(writePath, carList);
            */

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
