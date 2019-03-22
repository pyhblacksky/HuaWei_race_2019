package IO_Process;

import DataStruct.Answer;
import DataStruct.Car;
import DataStruct.Road;
import Util.Util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Author: pyh
 * @Date: 2019/3/18 14:17
 * @Version 1.0
 * @Function:   生成答案所需的数据结构
 */
public class BuildAnswer {

    /**
     * @Function: 返回答案的数据结构
     * 可调参数：   时间， 发车间隔， 每次发多少辆车
     * */
    public static ArrayList<Answer> buildAnswer(ArrayList<Car> carList){
        if(carList == null || carList.size() == 0){
            return null;
        }
        ArrayList<Answer> answers = new ArrayList<>();
        int time = 1;   //时间
        int count = 1;

        for(Car car : carList){
            if(time < car.getTime()){//如果准备发车时间大于计划发车时间，则以计划发车时间为准
                time = car.getTime();
            }
            Answer answer = new Answer(car.getId(), time, car.getRoads());
            answers.add(answer);
            if(count % 5 == 0){   //此处的模数为多少辆车发车
                time = car.getWeight();  //更改发车时间
            }
            count++;
        }
        return answers;
    }
    public static ArrayList<Answer> buildAnswer1(ArrayList<Car> carList){
        if(carList == null || carList.size() == 0){
            return null;
        }
        ArrayList<Answer> answers = new ArrayList<>();
        int time = 1;   //时间
        int count = 1;	//车辆计数
        int cut1 = carList.size()/6 * 5;//总长度的 5/6
        int cut2 = cut1 / 2; // 二次切分，中段路
        int t2 = 1;//区分时间
        /**************************第一部分*******************************/
        for(int i = 0; i < cut2; i++){
            Car car = carList.get(i);
            if(time < car.getTime()){//如果准备发车时间小于计划发车时间，则以计划发车时间为准
                time = car.getTime();
            }
            Answer answer = new Answer(car.getId(), time, car.getRoads());
            answers.add(answer);
            if(count % 170 == 0){   //模数为多少辆车发车
                time += car.getWeight()/3;  //预估的  1/3权
                t2 = time;
            }
            count++;
        }
        /********************************第二部分*************************************/
        for(int i = cut2; i < cut1; i++){
            Car car = carList.get(i);

            if(time < car.getTime()){//如果准备发车时间大于计划发车时间，则以计划发车时间为准
                time = car.getTime();
            }
            Answer answer = new Answer(car.getId(), time, car.getRoads());
            answers.add(answer);

            if(count % 220 == 0){
                time += car.getWeight()/3;
                t2 = time;
            }
            count++;
        }

        /*****************************并入第一部分***************************************/
        //将后半部分车加到前面
        for(int i = cut1; i < carList.size(); i++){
            Car car = carList.get(i);
            if(t2 == time){
                time = car.getWeight()/4;
                t2++;
            }
            if(time < car.getTime()){//如果准备发车时间小于计划发车时间，则以计划发车时间为准
                time = car.getTime();
            }
            Answer answer = new Answer(car.getId(), time, car.getRoads());
            answers.add(answer);
            if(count % 200 == 0){   //此处的模数为多少辆车发车
                time += car.getWeight()/4;  //更改发车时间
            }
            count++;
        }
        return answers;
    }

    /**
     * 重载，测试函数
     * */
    public static ArrayList<Answer> buildAnswer(ArrayList<Car> carList, ArrayList<Road> roadList){
        if(carList == null || carList.size() == 0){
            return null;
        }
        ArrayList<Answer> answers = new ArrayList<>();
        int time = 1;   //时间
        int count = 1;
        for(Car car : carList){
            if(time < car.getTime()){//如果准备发车时间大于计划发车时间，则以计划发车时间为准
                time = car.getTime();
            }
            Answer answer = new Answer(car.getId(), time, car.getRoads());
            inRoad(car.getRoads());
            answers.add(answer);
            if(count % 200 == 0){   //此处的模数为多少辆车发车
                time += car.getWeight()/3;  //更改发车时间
            }
            count++;
        }
        percentEverRoad(roadList,0);
        return answers;
    }


    /**
     * 道路填充车辆,计算当前道路利用率
     * */
    private static HashMap<Integer, Integer> map = new HashMap<>();
    private static void inRoad(ArrayList<Road> roads){
        for(Road road : roads){
            if(!map.containsKey(road.getId())){
                map.put(road.getId(), 1);
            } else{
                int num = map.get(road.getId()) + 1;
                map.put(road.getId(), num);
            }
        }
    }

    /**
     * 统计各个路所占百分比
     * */
    public static void percentEverRoad(ArrayList<Road> roadList, int time){
        double sumVol = 0;
        int sumRealVol = 0;
        for(int i : map.keySet()){
            Road road = Util.getRoadFromId(i, roadList);
            double vol = road.getLength() * road.getLanes();//这条路的容量
            sumVol += vol;
            int realVol = map.get(i);
            sumRealVol += realVol;
            double percent = realVol / vol;
            System.out.println("时间为："+time +"   " + i + "道路使用率为：" + percent);
        }
        System.out.println("道路总的利用率为：" + sumRealVol / sumVol);
    }
}
