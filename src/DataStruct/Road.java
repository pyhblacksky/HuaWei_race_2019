package DataStruct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: pyh
 * @Date: 2019/3/8 16:40
 * @Version 1.0
 * @Function:   路数据结构
 */
public class Road implements Serializable {
    //private static final long serialVersionUID = 10L;//序列化

    private int id; //道路id
    private int length; //道路长度
    private int MaxSpeed;//最高限速
    private int lanes;  //车道数
    private int start;  //起点id
    private int end;    //终点id
    private int directed;   //是否双向，1表示双向，0表示单向

    //该车道的权值
    private int weight = INF;
    private static final int INF = Integer.MAX_VALUE;//默认最大值,无通路

    public Road(int id, int length, int MaxSpeed, int lanes, int start, int end, int directed) {
        if(id == -1){//序号为-1，设置为空路
            return;
        }
        this.id = id;
        this.length = length;
        this.MaxSpeed = MaxSpeed;
        this.lanes = lanes;
        this.start = start;
        this.end = end;
        this.directed = directed;
        this.weight = length / MaxSpeed;

        /**
         * 根据C++新增
         * */
        //初始化
        this.matrix_E2S = new ArrayList<>(lanes);//初始化
        this.matrix_S2E = new ArrayList<>(lanes);
        //空车占位用id为-1的表示
        for (int i = 0; i < lanes; i++) {
            ArrayList<Car> emptyCarList = new ArrayList<>();
            for (int j = 0; j < length; j++) {
                Car emptyCar = new Car(-1, -1, -1, -1, -1);//此值设为空
                emptyCarList.add(emptyCar);
            }
            matrix_S2E.add(emptyCarList);
        }

        for (int i = 0; i < lanes; i++) {
            ArrayList<Car> emptyCarList = new ArrayList<>();
            for (int j = 0; j < length; j++) {
                Car emptyCar = new Car(-1, -1, -1, -1, -1);//此值设为空
                emptyCarList.add(emptyCar);
            }
            matrix_E2S.add(emptyCarList);
        }
    }

    //只有道路编号，起点终点id的路结构
    public Road(int id, int start, int end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    //Get和Set方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getMaxSpeed() {
        return MaxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        MaxSpeed = maxSpeed;
    }

    public int getLanes() {
        return lanes;
    }

    public void setLanes(int lanes) {
        this.lanes = lanes;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getDirected() {
        return directed;
    }

    public void setDirected(int directed) {
        this.directed = directed;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * 根据C++新增，路的具体结构       注意：矩阵中存储的车道和长度均从0开始，和题略有差异！！！
     *
     * 矩阵从左向右按开车的方向存
     * S2E表示从左到右
     * */
    //根据车道数和路长度建立路的行车结构 行表示车道，列表示长度的具体位置
    //实际上是一个矩阵,标号从1开始, 每个节点为一辆车，无车则为空
    private ArrayList<ArrayList<Car>> matrix_S2E;
    private ArrayList<ArrayList<Car>> matrix_E2S;

    //获取头车      从左到右
    public int get_Topcar_location_S2E(int lane) {
        int loc = -1;     //-1默认值，认为是空车道
        ArrayList<ArrayList<Car>> Matrix_temp = new ArrayList<>(this.matrix_S2E);
        for(int i = this.length - 1; i >= 0; i--){
            if (Matrix_temp.get(lane-1).get(i).getId() != -1) {
                loc = i;
                break;
            }
        }
        return loc;
    }
    //获取正常的车，没有runing的车
    public int get_Normalcar_location_S2E(int lane) {
        int loc = -1;     //-1默认值，认为是空车道
        ArrayList<ArrayList<Car>> Matrix_temp = new ArrayList<>(this.matrix_S2E);
        for(int i = this.length - 1; i >= 0; i--){
            if (Matrix_temp.get(lane-1).get(i).getId() != -1 && Matrix_temp.get(lane-1).get(i).getCarState().isRunning()) {
                loc = i;
                break;
            }
        }
        return loc;
    }

    //获取尾部车辆    从左到右
    public int get_Lastcar_location_S2E(int lane) {
        int loc = -1;
        ArrayList<ArrayList<Car>> Matrix_temp = new ArrayList<ArrayList<Car>>(this.matrix_S2E);
        for (int i = 0; i <= this.length - 1; i++) {
            if (Matrix_temp.get(lane-1).get(i).getId() != -1) {
                loc = i;
                break;
            }
        }
        return loc;
    }

    //获取头车  从右到左
    public int get_Topcar_location_E2S(int lane) {
        int loc = -1;     //-1默认值，认为是空车道
        ArrayList<ArrayList<Car>> Matrix_temp = new ArrayList<>(this.matrix_E2S);
        for (int i = this.length - 1; i >= 0; i--){
            if (Matrix_temp.get(lane-1).get(i).getId() != -1) {
                loc = i;
                break;
            }
        }
        return loc;
    }
    //获取正常的车，没有runing的车
    public int get_Normalcar_location_E2S(int lane){
        int loc = -1;     //-1默认值，认为是空车道
        ArrayList<ArrayList<Car>> Matrix_temp = new ArrayList<>(this.matrix_E2S);
        for (int i = this.length - 1; i >= 0; i--){
            if (Matrix_temp.get(lane-1).get(i).getId() != -1 && Matrix_temp.get(lane-1).get(i).getCarState().isRunning()) {
                loc = i;
                break;
            }
        }
        return loc;
    }
    //获取正常的车，wait为true的车
    public int getWaitCarlocation_E2S(int lane){
        int loc = -1;     //-1默认值，认为是空车道
        ArrayList<ArrayList<Car>> Matrix_temp = new ArrayList<>(this.matrix_E2S);
        for (int i = this.length - 1; i >= 0; i--){
            if (Matrix_temp.get(lane-1).get(i).getId() != -1 && Matrix_temp.get(lane-1).get(i).getCarState().isWait()) {
                loc = i;
                break;
            }
        }
        return loc;
    }
    public int getWaitCarlocation_S2E(int lane){
        int loc = -1;     //-1默认值，认为是空车道
        ArrayList<ArrayList<Car>> Matrix_temp = new ArrayList<>(this.matrix_S2E);
        for (int i = this.length - 1; i >= 0; i--){
            if (Matrix_temp.get(lane-1).get(i).getId() != -1 && Matrix_temp.get(lane-1).get(i).getCarState().isWait()) {
                loc = i;
                break;
            }
        }
        return loc;
    }


    //获取尾车，从右到左
    public int get_Lastcar_location_E2S(int lane) {
        int loc = -1;
        ArrayList<ArrayList<Car>> Matrix_temp = new ArrayList<>(this.matrix_E2S);
        for (int i = 0; i <= this.length - 1; i++){
            if (Matrix_temp.get(lane-1).get(i).getId() != -1) {
                loc = i;
                break;
            }
        }
        return loc;
    }

    public void setMatrix_S2E(Car car, int i, int j) {
        this.matrix_S2E.get(i).set(j, car);
    }

    public ArrayList<ArrayList<Car>> getMatrix_S2E() {
        return matrix_S2E;
    }

    public void setMatrix_E2S(Car car, int i, int j) {
        this.matrix_E2S.get(i).set(j, car);
    }

    public ArrayList<ArrayList<Car>> getMatrix_E2S() {
        return matrix_E2S;
    }
}









/******************************************************************************************************/
/**
 * 原数据结构废弃部分
 * */
//根据车道数和路长度建立路的行车结构 横坐标表示车道，纵坐标表示长度的具体位置
//实际上是一个矩阵,标号从1开始, 每个节点为一辆车，无车则为空
//private Car[][] matrix; //路的具体结构

////获取该路的状态
//public Car[][] getRoadStatue() {
//    return matrix;
//}
//
//    //确定该路的某个位置是否有车
//    public boolean hasCar(int lanes, int length){
//        return this.matrix[lanes][length] != null;
//    }
//
//    //根据车获取在当前路的坐标
//    public int[] getCarPosition(Car car){
//        int[] res = new int[]{-1, -1};//返回的默认值，如果返回这个值，说明车在该路不存在
//        for(int i = 1; i < this.matrix.length; i++){
//            for(int j = 1; j < this.matrix[i].length; j++){
//                if(this.matrix[i][j] == car){
//                    return new int[]{i, j};
//                }
//            }
//        }
//        return res;
//    }
//更新路的状态
//将车放入矩阵，需要进行判断
/**
 * car : 要更新的车
 * lanes : 要更新的车要去的车道，
 * length : 在该条路上，最大可行驶的距离
 * */
//public boolean setRoadStatue(Car car, int lanes, int length) {
//    //先获取当前车所处位置
//    int[] position = getCarPosition(car);
//
//    //length ： 当前车的最长可行驶距离
//    //遍历所有车道，从要更新的序号小的车道开始
//    for(int i = lanes; i < this.matrix.length; i++) {
//        //判断当前车道是否存在前车
//        int j = 1;
//        for (j = 1; j <= length; j++) {
//            if (hasCar(i, j)) {
//                break;
//            }
//        }
//        if (j >= length) {//说明在这段路上没有前车,直接更新
//            this.matrix[i][length] = car;
//            //将原有位置清空
//            this.matrix[position[0]][position[1]] = null;
//            return true;
//        }
//        if(j == 1){//说明该车道占满了，不能更新，需要切换车道
//            continue;
//        }
//        //此时受到前车限制，但是该车道还有空位，将本车更新在前车后,更新！
//        this.matrix[i][j-1] = car;
//        //将原有位置清空
//        this.matrix[position[0]][position[1]] = null;
//        return true;
//    }
//    return false;//更新失败,必须等待
//}