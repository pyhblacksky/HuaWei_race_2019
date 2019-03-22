package Judgment;

import Astar.AFind;
import DataStruct.Cross;
import DataStruct.Road;
import Util.Util;

import java.util.*;

/**
 * @Author: pyh
 * @Date: 2019/3/20 14:00
 * @Version 1.0
 * @Function:   找有向图中的环
 */
public class FindLoop {

    private static ArrayList<Cross> crossList;
    private static ArrayList<Road> roadList;
    private static Set<Integer> searched;
    private static Set<List<Integer>> allCircles;//所有环
    private static Set<List<Integer>> allCirclesRoads;//保存成环的路

    private int[][] matrix;//建立邻接矩阵
    private final static int INF = 0;

    /**
     * 寻找环，对外接口
     * 保存形成环的路
     * @return Set 保存形成的路
     * */
    public static Set<List<Integer>> findLoop(ArrayList<Cross> crosses, ArrayList<Road> roads){
        crossList = new ArrayList<>(crosses);
        roadList = new ArrayList<>(roads);
        allCirclesRoads = new HashSet<>();
        allCircles = new HashSet<>();
        searched = new HashSet<>();

        FindLoop find = new FindLoop();
        find.buildMatrix();//生成矩阵
        //打印矩阵
        //for(int i = 0; i < find.matrix.length; i++){
        //    for(int j = 0; j < find.matrix[i].length; j++){
        //        System.out.print(find.matrix[i][j] + " ");
        //    }
        //    System.out.println();
        //}
        //System.out.println();

        for(int i = 1; i < find.matrix.length; i++){
            if(searched.contains(i))//搜索过以这个点出现的环
                continue;
            List<Integer> trace = new ArrayList<>();
            List<Integer> traceRoad = new ArrayList<>();
            findCycle(i, find.matrix, trace, traceRoad);//递归找环
        }

        //环的大小
        //System.out.println("总的道路环数（两个应该一致） : " + allCirclesRoads.size());
        //System.out.println("总的顶点环数（两个应该一致）: " + allCircles.size());
        //for(List<Integer> list : allCirclesRoads)
        //    System.out.println("circle: "+list);
        return allCirclesRoads;
    }

    /**
     * 构建矩阵
     * */
    private void buildMatrix(){
        //矩阵初始化，赋0
        matrix = new int[crossList.size()+1][crossList.size()+1];
        for(int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix[i].length; j++){
                matrix[i][j] = INF;
                if(i == j){matrix[i][j] = 0;}
            }
        }
        //赋值     && cross.getUpRoad().getId()
        //for(Cross cross : crossList){
        //    if(cross.getUpRoad() != null){
        //        updateMatrix(matrix, cross.getUpRoad(), cross);
        //    }
        //    if(cross.getDownRoad() != null){
        //        updateMatrix(matrix, cross.getDownRoad(), cross);
        //    }
        //    if(cross.getLeftRoad() != null){
        //        updateMatrix(matrix, cross.getLeftRoad(), cross);
        //    }
        //    if(cross.getRightRoad() != null){
        //        updateMatrix(matrix, cross.getRightRoad(), cross);
        //    }
        //}

        //根据道路矩阵赋值
        for(Road road : roadList){
            updateMatrix(matrix, road);
        }
    }

    /**
     * 根据 road 和 cross来更新矩阵值
     * */
    private void updateMatrix(int[][] matrix, Road road, Cross cross){
        Cross next = Util.getCrossFromId(road.getEnd(), crossList);
        if(next == cross || next == null){
            return;
        }

        matrix[cross.getId()][next.getId()] = 1;
        //双向路
        if(road.getDirected() == 1){
            matrix[next.getId()][cross.getId()] = 1;
        }
    }

    /**
     * 根据road来更新矩阵值， 矩阵的值为两点之间连接的道路id
     * */
    private void updateMatrix(int[][] matrix, Road road){
        int start = road.getStart();
        int end = road.getEnd();

        matrix[start][end] = road.getId();
        if(road.getDirected() == 1){
            matrix[end][start] = road.getId();
        }
    }

    /**
     * 寻找环
     *  v : 当前顶点
     *  matrix : 邻接矩阵
     *  trace : 顶点集合
     *  traceRoad : 路的集合
     * */
    private static void findCycle(int v, int[][]matrix, List<Integer> trace, List<Integer> traceRoad){
        int j = trace.indexOf(v);//返回指定字符在字符串中第一次出现处的索引，如果此字符串中没有这样的字符，则返回 -1
        if(j != -1) {//字符出现过，说明有环
            List<Integer> circle = new ArrayList<>();
            List<Integer> circleRoad = new ArrayList<>();
            while(j < trace.size()) {
                circle.add(trace.get(j));
                circleRoad.add(traceRoad.get(j));
                j++;
            }
            if(circle.size() == 2){//如果只有两个元素，跳过，不计算来回的情况
                return;
            }
            //Collections.sort(circle);//排序是为了去重
            allCircles.add(circle);
            //Collections.sort(circleRoad);
            allCirclesRoads.add(circleRoad);
            return;
        }

        trace.add(v);
        for(int i = 0; i < matrix.length; i++) {
            if(matrix[v][i] != 0){
                traceRoad.add(matrix[v][i]);//添加路
                searched.add(i);
                findCycle(i, matrix, trace, traceRoad);
                traceRoad.remove(traceRoad.size()-1);//回溯
            }
        }
        trace.remove(trace.size()-1);//回溯
    }

    //判断路是否存在，弃用
    private static boolean isRoadExist(Road road, Cross cross){
        if(road != null && road.getId() != -1 && road.getId() != 0){
            //1. 路的起点和路口id一致，说明从这个路口指向外的 2. 或者是双向路的情况
            if(road.getStart() == cross.getId() || (road.getEnd() == cross.getId() && road.getDirected() == 1)){
                return true;
            }
        }
        return false;
    }

    //测试函数
    /*
    public static void main(String[] args) {
        int n = 9;
        int[][] e={
                {0,  1,  0,  0,  0,  1,  0,  0,  0},
                {0,  0,  1,  0,  0,  0,  0,  0,  0},
                {0,  0,  0,  1,  0,  0,  0,  0,  0},
                {0,  0,  1,  0,  1,  0,  0,  0,  0},
                {0,  0,  0,  0,  0,  0,  1,  0,  0},
                {0,  0,  0,  0,  0,  0,  1,  0,  0},
                {0,  1,  0,  1,  0,  0,  0,  1,  0},
                {0,  0,  0,  0,  0,  0,  0,  0,  1},
                {0,  0,  0,  0,  1,  0,  0,  0,  0}
        };
        for(int i = 0; i < n; i++){
            if(searched.contains(i))
                continue;
            List<Integer> trace =new ArrayList<>();
            findCycle(i, e, trace);
        }

        for(List<Integer> list : allCircles)
            System.out.println("circle: "+list);
    }
    */
}