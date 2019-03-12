package DataStruct;

import java.util.List;

/**
 * @Author: pyh
 * @Date: 2019/3/8 16:40
 * @Version 1.0
 * @Function:   路数据结构
 */
public class Road {

    private int id; //道路id
    private int length; //道路长度
    private int MaxSpeed;//最高限速
    private int lanes;  //车道数
    private int start;  //起点id
    private int end;    //终点id
    private int directed;   //是否双向，1表示双向，0表示单向

    //根据车道数和路长度建立路的行车结构 横坐标表示车道，纵坐标表示长度的具体位置
    //实际上是一个矩阵,标号从1开始, 每个节点为一辆车，无车则为空
    private Car[][] matrix; //路的具体结构

    public Road(int id, int length, int MaxSpeed, int lanes, int start, int end, int directed) {
        this.id = id;
        this.length = length;
        this.MaxSpeed = MaxSpeed;
        this.lanes = lanes;
        this.start = start;
        this.end = end;
        this.directed = directed;
        matrix = new Car[lanes+1][length+1];
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

    //获取该路的状态
    public Car[][] getRoadStatue() {
        return matrix;
    }

    //确定该路的某个位置是否有车
    public boolean hasCar(int lanes, int length){
        return this.matrix[lanes][length] != null;
    }

    //根据车获取在当前路的坐标
    public int[] getCarPosition(Car car){
        int[] res = new int[]{-1, -1};//返回的默认值，如果返回这个值，说明车在该路不存在
        for(int i = 1; i < this.matrix.length; i++){
            for(int j = 1; j < this.matrix[i].length; j++){
                if(this.matrix[i][j] == car){
                    return new int[]{i, j};
                }
            }
        }
        return res;
    }

    //更新路的状态
    //将车放入矩阵，需要进行判断
    /**
     * car : 要更新的车
     * lanes : 要更新的车要去的车道，
     * length : 在该条路上，最大可行驶的距离
     * */
    public boolean setRoadStatue(Car car, int lanes, int length) {
        //先获取当前车所处位置
        int[] position = getCarPosition(car);

        //length ： 当前车的最长可行驶距离
        //遍历所有车道，从要更新的序号小的车道开始
        for(int i = lanes; i < this.matrix.length; i++) {
            //判断当前车道是否存在前车
            int j = 1;
            for (j = 1; j <= length; j++) {
                if (hasCar(i, j)) {
                    break;
                }
            }
            if (j >= length) {//说明在这段路上没有前车,直接更新
                this.matrix[i][length] = car;
                //将原有位置清空
                this.matrix[position[0]][position[1]] = null;
                return true;
            }
            if(j == 1){//说明该车道占满了，不能更新，需要切换车道
                continue;
            }
            //此时受到前车限制，但是该车道还有空位，将本车更新在前车后,更新！
            this.matrix[i][j-1] = car;
            //将原有位置清空
            this.matrix[position[0]][position[1]] = null;
            return true;
        }
        return false;//更新失败,必须等待
    }

    //出道路为第一排，即矩阵的尾部，此处考虑双向车道的情况？？

}
