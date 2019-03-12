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
            String config = "config_1\\";
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

            for(Car car : carList){
                long startTime = System.currentTimeMillis();//计算程序运行时间

                HashMap<Integer, Road> map = new HashMap<>();
                ArrayList<Road> roads = IOProcess.StringToRoad(roadStr, map);

                ArrayList<Cross> crossList = IOProcess.StringToCross(crossStr,map);

                //ArrayList<ArrayList<Road>> res = FindPath.find(crossList, car, new HashSet<>());//找到路线
                AFind find = new AFind(crossList);
                AFind.Node node = find.findPath(new AFind.Node(AFind.getCross(car.getStart(), crossList)),
                        new AFind.Node(AFind.getCross(car.getEnd(), crossList)));

                ArrayList<Road> res = new ArrayList<>();
                while(node != null){
                    if(node.road != null){
                        res.add(node.road);
                    }
                    node = node.parent;
                }
                System.out.print("car_id_"+ car.getId() + " 起点："+ car.getStart() + " 终点: " + car.getEnd() + "  所寻道路为：");
                for(Road road : res){
                    System.out.print(road.getId() + " ");
                }

                long endTime = System.currentTimeMillis();
                System.out.println("  程序运行时间为 ： " + (endTime - startTime) + "ms   ");
            }

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
