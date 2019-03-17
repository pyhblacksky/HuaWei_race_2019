package Astar;

import DataStruct.Car;
import DataStruct.Cross;
import DataStruct.Road;

import java.util.*;

/**
 * @Author: pyh
 * @Date: 2019/3/16 9:00
 * @Version 1.0
 * @Function:
 *      重新规划路径
 */
public class reRoad {
    /**
     * cars ： 是车的结果列表
     * @return HashMap<Road, Integer> 表示 某条路的出现次数Int
     * */
    private static HashMap<Road, Integer> roadRate(ArrayList<Car> cars){
        HashMap<Road, Integer> map = new HashMap<>();
        for(Car car : cars){
            if(car.getRoads() != null && car.getRoads().size() != 0){
                for(Road road : car.getRoads()){
                    if(!map.containsKey(road)){
                        map.put(road, 1);
                    } else{
                        int num = map.get(road) + 1;
                        map.put(road, num);
                    }
                }
            }
        }
        return map;
    }

    /**
     * 输出map结果
     * */
    private static void printMap(HashMap<Road, Integer> map){
        //map按照降序排序
        List<Map.Entry<Road,Integer>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list,new Comparator<Map.Entry<Road, Integer>>() {
            @Override
            public int compare(Map.Entry<Road, Integer> o1, Map.Entry<Road, Integer> o2) {
                return o2.getValue() - o1.getValue();
            }
        });

        for(Map.Entry<Road,Integer> mapping : list){
            System.out.println(mapping.getKey().getId() + " 经过了 " + mapping.getValue() + "次");
        }
        System.out.println();
    }

    /**
     * 选取出现频次高的路
     * k : 选择几条
     * @return 降序排列的路列表
     * */
    private static ArrayList<Road> selectKRate(int k, HashMap<Road, Integer> map){
        if(k <= 0 || k > map.size()){
            return null;
        }
        ArrayList<Road> res = new ArrayList<>(k);

        //map按照降序排序
        List<Map.Entry<Road,Integer>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list,new Comparator<Map.Entry<Road, Integer>>() {
            @Override
            public int compare(Map.Entry<Road, Integer> o1, Map.Entry<Road, Integer> o2) {
                return o2.getValue() - o1.getValue();
            }
        });

        for(int i = 0; i < k; i++){
            Road road = list.get(i).getKey();
            res.add(road);
        }
        return res;
    }

    /**
     * 重新寻路
     * carList:车列表
     * forbidRoad:禁止的道路id
     * */
    private static void reFind(ArrayList<Car> carList, ArrayList<Road> forbidRoad,
                              HashMap<Road, Integer> map, ArrayList<Road> roadList, ArrayList<Cross> crossList){
        for(Road road : forbidRoad){
            int k = map.get(road) / 2; //将低一半的行驶路
            for(Car car : carList){
                if(k == 0){
                    break;
                }
                if(car.getRoads().contains(road)){
                    //禁止该路下的寻路
                    AFind.AFindPath(crossList, car, road);
                }
                k--;
            }
        }
    }
    /**
     * 重新寻路，重载
     * 此时car含有 禁止通行的道路列表
     * */
    private static void reFind(ArrayList<Car> carList, ArrayList<Cross> crossList){
        for(Car car : carList){
            AFind.AFindPath(crossList, car);
        }
    }

    /**
     * 如果该车有这条路，放入其禁止通行的ArrayList
     *
     * */
    private static void putForbidIfHas(ArrayList<Road> forbidList, ArrayList<Car> carList, HashMap<Road, Integer> map){
        for(Road road : forbidList){
            int k = map.get(road) / 2;//该条路出现的次数
             for(Car car : carList){
                 if(car.getRoads().contains(road)){
                     car.addForbidRoads(road);
                     k--;
                 }
                 if(k == 0){
                     break;
                 }
            }
        }
    }

    /************************************************对外使用************************************************/
    /**
     * 对外提供功能的函数
     * 提供参数：carList, roadList, crossList, k 需要平衡的路的条数
     * */
    public static void reFind(ArrayList<Car> carList, ArrayList<Cross> crossList, int k){

        HashMap<Road, Integer> roadStatues = roadRate(carList);
        ArrayList<Road> forbidRoadList = selectKRate(k, roadStatues);
        if(forbidRoadList != null){
            putForbidIfHas(forbidRoadList,carList,roadStatues);
            reFind(carList, crossList);
            //reFind(carList, forbidRoadList, roadStatues, roadList, crossList);
        }
        roadStatues = roadRate(carList);
        printMap(roadStatues);
    }
}
