package DataStruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: pyh
 * @Date: 2019/3/8 16:49
 * @Version 1.0
 * @Function:   图结构,    以路口为基础结构建立图
 */

//以邻接矩阵的形式建立图
public class Graph{

    //顶点数组
    private Cross[] mVex;

    //根据给定的信息创建图,路信息和交叉路口信息
    public Graph(Map<Integer, Road> roads, List<Cross> nodes){
        mVex = new Cross[nodes.size()];

        for(Cross cross : nodes){

        }
    }
}


//邻接矩阵创建图，太占空间
class Graph1 {

    private int mVex; //顶点
    private int roadCount;//路的数量
    public Road[][] matrix;    //图的邻接矩阵表示

    public Graph1(ArrayList<Road> roads, int mVex){
        this.mVex = mVex;
        //顶点从1开始计数
        matrix = new Road[mVex+1][mVex+1];

        //图初始化
        for(int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix[i].length; j++){
                matrix[i][j] = emptyRoad();
            }
        }

        for(Road road : roads){
            matrix[road.getStart()][road.getEnd()] = road;
            if(road.getDirected() == 1){
                //说明是双向
                matrix[road.getEnd()][road.getStart()] = road;
            }
        }
    }

    //空路
    public Road emptyRoad(){
        Road road = new Road(-1,-1,-1,-1,-1,-1,-1);
        return road;
    }




    public int getmVex() {
        return mVex;
    }

    public void setmVex(int mVex) {
        this.mVex = mVex;
    }

    public int getRoadCount() {
        return roadCount;
    }

    public void setRoadCount(int roadCount) {
        this.roadCount = roadCount;
    }

}
