package IO_Process;

import DataStruct.Car;
import DataStruct.Cross;
import DataStruct.Road;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * @Author: pyh
 * @Date: 2019/3/12 9:41
 * @Version 1.0
 * @Function:   对输入数据处理
 *
 */
public class IOProcess {

    /**
     * function : 读取文件
     * var : path : 要读取文件路径
     *      list : 存储读取结果的列表
     * */
    public static void ReadFile(String path, ArrayList<String> list){
        try {
            //防止文件读取失败
            File file = new File(path);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
            BufferedReader br = new BufferedReader(reader);
            String line = br.readLine();
            while(line != null){
                //去除空行
                if(line.trim().equals("")){
                    line = br.readLine();
                    continue;
                }
                if(line.charAt(0) == '#'){//#为注释行，忽略
                    line = br.readLine();
                    continue;
                }
                list.add(line);
                line = br.readLine();//按行读取
            }
            br.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * function : 写出文件
     * var :
     *      carList : 车的链表，当前链表中的车保存了对应走的路的信息
     * */
    public static void WriteFile(String path, ArrayList<Car> carList){
        try {
            File writeName = new File(path);
            writeName.createNewFile();//创建新文件
            BufferedWriter out = new BufferedWriter(new FileWriter(writeName));
            ArrayList<String> strList = CarsToString(carList);
            for(String str : strList){
                out.write(str+"\r\n");//    \r\n即为换行
            }
            out.flush();//把缓存区内容压入文件
            out.close();//关闭文件
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * function: 转换
     * 将carList转为String
     * */
    private static ArrayList<String> CarsToString(ArrayList<Car> carList){
        if(carList == null || carList.size() == 0){
            return null;
        }
        ArrayList<String> res = new ArrayList<>(carList.size());
        //int count = 1;
        //int time = 1;
        ////int mod = 700;
        //for(Car car : carList){
        //    String roadStr = "";
        //    for(Road road : car.getRoads()){
        //        roadStr += road.getId()+",";
        //    }
        //    roadStr = roadStr.substring(0, roadStr.length()-1);//去除最后一个逗号
        //    if(time < car.getTime()){//如果准备发车时间大于计划发车时间，则以计划发车时间为准
        //        time = car.getTime();
        //    }
        //    String str = "("+car.getId()+", "+time+", "+roadStr+")";
        //    res.add(str);//添加入ArrayList
        //
        //    if(count % 165 == 0){
        //        time += car.getWeight()/3.15;
        //    }
        //    //if(mod > 0 && count % mod == 0){//每5辆车,模数更改则为几辆车发
        //    //    time += car.getWeight()/3;//发车时间, 车的权重是预估走完的时间
        //    //    mod = mod-100;
        //    //}
        //    //if(mod <= 0){
        //    //    mod = 700;
        //    //}
        //    count++;
        //}
        /*****判断哪个时期堵车，发车数可变****/
        int count = 1;
        int time = 1;
        int cut1 = carList.size()/4 * 3; //切分数据
        int cut2 = cut1 / 2; // 二次切分，中段路
        int t2 = 1;
        for(int i = 0; i < cut2; i++){
            Car car = carList.get(i);

            String roadStr = "";
            for(Road road : car.getRoads()){
                roadStr += road.getId()+",";
            }
            roadStr = roadStr.substring(0, roadStr.length()-1);//去除最后一个逗号
            if(time < car.getTime()){//如果准备发车时间大于计划发车时间，则以计划发车时间为准
                time = car.getTime();
            }
            String str = "("+car.getId()+", "+time+", "+roadStr+")";
            res.add(str);//添加入ArrayList

            if(count % 150 == 0){
                time += car.getWeight()/3;
                t2 = time;
            }
            count++;

        }

        for(int i = cut2; i < cut1; i++){
            Car car = carList.get(i);

            String roadStr = "";
            for(Road road : car.getRoads()){
                roadStr += road.getId()+",";
            }
            roadStr = roadStr.substring(0, roadStr.length()-1);//去除最后一个逗号
            if(time < car.getTime()){//如果准备发车时间大于计划发车时间，则以计划发车时间为准
                time = car.getTime();
            }
            String str = "("+car.getId()+", "+time+", "+roadStr+")";
            res.add(str);//添加入ArrayList

            if(count % 200 == 0){
                time += car.getWeight()/3;
                t2 = time;
            }
            count++;
        }

        //将后半部分车加到前面
        for(int i = cut1; i < carList.size(); i++){
            Car car = carList.get(i);
            if(t2 == time){
                time = car.getWeight()/4;
                t2++;
            }
            String roadStr = "";
            for(Road road : car.getRoads()){
                roadStr += road.getId()+",";
            }
            roadStr = roadStr.substring(0, roadStr.length()-1);//去除最后一个逗号
            if(time < car.getTime()){//如果准备发车时间大于计划发车时间，则以计划发车时间为准
                time = car.getTime();
            }
            String str = "("+car.getId()+", "+time+", "+roadStr+")";
            res.add(str);//添加入ArrayList

            if(count % 200 == 0){
                time += car.getWeight()/4;
            }
            count++;
        }

        /**分开以不同权发车呢？**/
        //int mid = carList.size()/4;
        //int t1 = 1;
        //int count = 1;
        //for(int i = 0; i < mid; i++){
        //    Car car = carList.get(i);
        //    String roadStr = "";
        //    for(Road road : car.getRoads()){
        //        roadStr += road.getId()+",";
        //    }
        //    if(t1 < car.getTime()){//如果准备发车时间大于计划发车时间，则以计划发车时间为准
        //        t1 = car.getTime();
        //    }
        //    roadStr = roadStr.substring(0, roadStr.length()-1);//去除最后一个逗号
        //    String str = "("+car.getId()+", "+t1+", "+roadStr+")";
        //
        //    res.add(str);//添加入ArrayList
        //
        //    if(count % 75 == 0){
        //        t1 += car.getWeight()/3;
        //    }
        //    count++;
        //}
        //count = 1;
        //int t2 = 1;
        //for(int i = carList.size()-1; i >= mid; i--){
        //    Car car = carList.get(i);
        //    String roadStr = "";
        //    for(Road road : car.getRoads()){
        //        roadStr += road.getId()+",";
        //    }
        //    if(t2 < car.getTime()){//如果准备发车时间大于计划发车时间，则以计划发车时间为准
        //        t2 = car.getTime();
        //    }
        //    roadStr = roadStr.substring(0, roadStr.length()-1);//去除最后一个逗号
        //    String str = "("+car.getId()+", "+t2+", "+roadStr+")";
        //
        //    res.add(str);//添加入ArrayList
        //
        //    if(count % 75 == 0){
        //        t2 += car.getWeight()/3;
        //    }
        //    count++;
        //}
        return res;
    }


    /**
     * function : road数据处理
     *      String 列表转为 Road数据结构
     *      添加路id-road 的  kv结构
     * */
    public static ArrayList<Road> StringToRoad(ArrayList<String> list, HashMap<Integer, Road> map){
        if(list == null) {
            return null;
        }
        ArrayList<Road> res = new ArrayList<>();
        for(String str : list){
            //去除括号
            str = str.substring(1, str.length()-1);
            String[] strArr = str.split(",");//以逗号为分隔符
            int roadId = Integer.valueOf(strArr[0].trim());
            int length = Integer.valueOf(strArr[1].trim());
            int MaxValue = Integer.valueOf(strArr[2].trim());
            int lanes = Integer.valueOf(strArr[3].trim());
            int start = Integer.valueOf(strArr[4].trim());
            int end = Integer.valueOf(strArr[5].trim());
            int directed = Integer.valueOf(strArr[6].trim());
            Road road = new Road(roadId, length, MaxValue, lanes, start, end, directed);
            res.add(road);
            map.put(roadId, road);
        }
        return res;
    }

    /**
     * function : car数据处理
     *      String 列表转为 car数据结构
     * */
    public static ArrayList<Car> StringToCar(ArrayList<String> list){
        if(list == null) {
            return null;
        }
        ArrayList<Car> res = new ArrayList<>();
        for(String str : list){
            //去除括号
            str = str.substring(1, str.length()-1);
            String[] strArr = str.split(",");//以逗号为分隔符
            int id = Integer.valueOf(strArr[0].trim());
            int start = Integer.valueOf(strArr[1].trim());
            int end = Integer.valueOf(strArr[2].trim());
            int maxSpeed = Integer.valueOf(strArr[3].trim());
            int time = Integer.valueOf(strArr[4].trim());
            Car car = new Car(id, start, end, maxSpeed, time);
            res.add(car);
        }
        return res;
    }

    /**
     * function : cross数据处理
     *          String 列表转为 Cross数据结构
     * */
    public static ArrayList<Cross> StringToCross(ArrayList<String> crossList, HashMap<Integer, Road> map){
        if(crossList == null){
            return null;
        }

        ArrayList<Cross> res = new ArrayList<>();
        for(String str : crossList){
            //去除括号
            str = str.substring(1, str.length()-1);
            String[] strArr = str.split(",");//以逗号为分隔符
            int id = Integer.valueOf(strArr[0].trim());
            Road upRoad = null;
            Road leftRoad = null;
            Road downRoad = null;
            Road rightRoad = null;
            //如果为1，则为空路
            if(Integer.valueOf(strArr[1].trim()) != -1)
                upRoad = map.get(Integer.valueOf(strArr[1].trim()));
            if(Integer.valueOf(strArr[2].trim()) != -1)
                leftRoad = map.get(Integer.valueOf(strArr[2].trim()));
            if(Integer.valueOf(strArr[3].trim()) != -1)
                downRoad = map.get(Integer.valueOf(strArr[3].trim()));
            if(Integer.valueOf(strArr[4].trim()) != -1)
                rightRoad = map.get(Integer.valueOf(strArr[4].trim()));
            Cross cross = new Cross(id, upRoad, leftRoad, downRoad, rightRoad);
            res.add(cross);
        }
        return res;
    }
    /******************************************中间状态******************************************/
    /**
     * 保存中间寻路状态
     * path:写出路径
     * carList: car的信息
     * */
    public static void WriteTempState(String path, ArrayList<Car> carList){
        try {

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
