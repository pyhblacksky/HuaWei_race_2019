package IO_Process;

import DataStruct.Car;
import DataStruct.Cross;
import DataStruct.Road;

import java.io.*;
import java.util.ArrayList;

/**
 * @Author: pyh
 * @Date: 2019/3/22 11:05
 * @Version 1.0
 * @Function:   序列化和反序列化测试
 */
public class SerializableTest {

    /************************************************************/
    //将roadList序列化和反序列化
    public static void RoadListSerialize(ArrayList<Road> roadList, String fileName){
        try{
            //序列化
            File file = new File(fileName);
            if(file.exists()){
                file.delete();
            }
            OutputStream op = new FileOutputStream(fileName);//保存的临时文件名

            ObjectOutputStream ops = new ObjectOutputStream(op);
            ops.writeObject(roadList);
            ops.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static ArrayList<Road> DeRoadListSerialize(String fileName){
        try {
            InputStream in = new FileInputStream(fileName);
            ObjectInputStream os = new ObjectInputStream(in);
            ArrayList<Road> roadList = (ArrayList<Road>) os.readObject();
            os.close();
            return roadList;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /************************************************************/
    //将Car序列化和反序列化
    public static void CarListSerialize(ArrayList<Car> carList, String fileName){
        try{
            //序列化
            File file = new File(fileName);
            if(file.exists()){
                file.delete();
            }
            OutputStream op = new FileOutputStream(fileName);//保存的临时文件名

            ObjectOutputStream ops = new ObjectOutputStream(op);
            ops.writeObject(carList);
            ops.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static ArrayList<Car> DeCarListSerialize(String fileName){
        try {
            InputStream in = new FileInputStream(fileName);
            ObjectInputStream os = new ObjectInputStream(in);
            ArrayList<Car> carList = (ArrayList<Car>) os.readObject();
            os.close();
            return carList;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /************************************************************/
    //将Cross序列化和反序列化
    public static void CrossListSerialize(ArrayList<Cross> crossList, String fileName){
        try{
            //序列化
            File file = new File(fileName);
            if(file.exists()){
                file.delete();
            }
            OutputStream op = new FileOutputStream(fileName);//保存的临时文件名

            ObjectOutputStream ops = new ObjectOutputStream(op);
            ops.writeObject(crossList);
            ops.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static ArrayList<Cross> DeCrossListSerialize(String fileName){
        try {
            InputStream in = new FileInputStream(fileName);
            ObjectInputStream os = new ObjectInputStream(in);
            ArrayList<Cross> CrossList = (ArrayList<Cross>) os.readObject();
            os.close();
            return CrossList;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**********************************************************************************/
    //序列化整个中间量
    public static void AllSerialize(ArrayList<Cross> crossList, ArrayList<Road> roadList,
                                    ArrayList<Car> inRoadCar, ArrayList<Car> nextCar, String fileName){
        try{
            //序列化
            File file = new File(fileName);
            if(file.exists()){
                file.delete();
            }
            OutputStream op = new FileOutputStream(fileName);//保存的临时文件名

            ObjectOutputStream ops = new ObjectOutputStream(op);
            ops.writeObject(roadList);
            ops.writeObject(crossList);
            ops.writeObject(inRoadCar);
            ops.writeObject(nextCar);
            ops.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static ArrayList<Object> DeAllSerialize(String fileName){
        try {
            InputStream in = new FileInputStream(fileName);
            ObjectInputStream os = new ObjectInputStream(in);

            ArrayList<Road> roadList = (ArrayList<Road>) os.readObject();
            ArrayList<Cross> crossList = (ArrayList<Cross>) os.readObject();
            ArrayList<Car> inRoadCar = (ArrayList<Car>) os.readObject();
            ArrayList<Car> nextCar = (ArrayList<Car>) os.readObject();

            ArrayList<Object> obj = new ArrayList<>();
            obj.add(roadList);
            obj.add(crossList);
            obj.add(inRoadCar);
            obj.add(nextCar);

            os.close();
            return obj;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


/**************************************************************************************************************/
/*
    //单个对象的序列化
    public static void main1(String[] args) throws Exception{
        Road road = new Road(1,1,2,1,2,4,1);
        Car car = new Car(2,3,4,5,6);
        road.setMatrix_S2E(car, 0, 0);

        //序列化
        OutputStream op = new FileOutputStream("a.txt");
        ObjectOutputStream ops = new ObjectOutputStream(op);
        ops.writeObject(road);
        ops.close();

        //反序列化
        InputStream in = new FileInputStream("a.txt");
        ObjectInputStream os = new ObjectInputStream(in);
        Road p = (Road) os.readObject();
        System.out.println(p);  //Person [name=vae, age=1]
        os.close();
    }

    //ArrayList的序列化
    public static void main(String[] args) throws Exception{
        Road road1 = new Road(1,1,2,1,2,4,1);
        Car car1 = new Car(2,3,4,5,6);
        road1.setMatrix_S2E(car1, 0, 0);

        Road road2 = new Road(2,3,4,5,6,7,1);
        Car car2 = new Car(11,4,3,5,6);
        road2.setMatrix_S2E(car2, 0, 0);

        Road road3 = new Road(1,1,2,1,2,4,1);
        Car car3 = new Car(2,3,4,5,6);
        road3.setMatrix_S2E(car3, 0, 0);

        ArrayList<Road> roadList = new ArrayList<>();
        roadList.add(road1);
        roadList.add(road2);
        roadList.add(road3);

        RoadListSerialize(roadList);

        ArrayList<Road> roadList2 = DeRoadListSerialize();
        System.out.println();
    }
*/
}
