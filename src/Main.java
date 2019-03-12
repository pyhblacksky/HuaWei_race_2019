import DataStruct.Car;
import DataStruct.Cross;
import DataStruct.Road;
import IO_Process.IOProcess;

import java.util.*;

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

                ArrayList<ArrayList<Road>> res = FindPath.find(crossList, car, new HashSet<>());//找到路线

                long endTime = System.currentTimeMillis();
                System.out.print("car_id_"+ car.getId() + "程序运行时间为 ： " + (endTime - startTime) + "ms   ");

                //测试输出结果
                System.out.println("car_id_"+ car.getId() + " 路径的总数 ：" + res.size());
            }

            /*
            long startTime = System.currentTimeMillis();//计算程序运行时间

            HashMap<Integer, Road> map = new HashMap<>();
            ArrayList<Road> roads = StringToRoad(roadStr, map);

            ArrayList<Cross> crossList = StringToCross(crossStr,map);
            ArrayList<ArrayList<Road>> res = FindPath.find(crossList, 20, 2);//找到路线

            long endTime = System.currentTimeMillis();
            System.out.print("程序运行时间为 ： " + (endTime - startTime) + "ms   ");

            //测试输出结果
            System.out.println(" 路径的总数 ：" + res.size());
            */

            /*
            for(ArrayList<Road> list : res){
                int sum = 0;
                for(Road road : list){
                    sum += road.getLength();
                    System.out.print(road.getId() + " ");
                }
                System.out.print("该条路的总距离 ： " + sum);
                System.out.println();
            }*/

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
