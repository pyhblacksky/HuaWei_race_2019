package Astar;

import DataStruct.Car;
import DataStruct.Cross;
import DataStruct.Road;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * @Author: pyh
 * @Date: 2019/3/11 19:46
 * @Version 1.0
 * @Function:
 *      A*实现寻路，适应于本题交通的情况
 *
 */
public class AFind {

    /**
     * 获取寻路信息
     * crossList: 节点的列表
     * car : 寻路车辆
     * 最后保存的路径信息在car.roads里面
     * */
    public static void AFindPath(ArrayList<Cross> crossList, Car car){
        AFind find = new AFind(crossList, car);
        //如果车的禁止列表不为空
        if(car.getForbidRoads() != null && car.getForbidRoads().size() != 0){
            for(Road road : car.getForbidRoads()){
                find.forbidRoad(road);
            }
        }
        AFind.Node node = find.findPath(new AFind.Node(AFind.getCross(car.getStart(), crossList)),
                new AFind.Node(AFind.getCross(car.getEnd(), crossList)));

        ArrayList<Road> res = new ArrayList<>();
        //反转链表，因为结果集是反的
        AFind.Node newHead = null;
        while(node != null){
            AFind.Node temp = node.parent;
            node.parent = newHead;
            newHead = node;
            node = temp;
        }
        //添加结果集
        int weight = 0;
        while(newHead != null){
            if(newHead.road != null){
                res.add(newHead.road);
                weight += newHead.road.getWeight();
            }
            newHead = newHead.parent;
        }
        car.setWeight(weight);//设置当前车的权重
        car.setRoads(res);//更新当前车所走的通路
        car.setRealTime(weight/2);//更新出发时间
    }
    /**
     * 重载函数
     * roadList ： 该列表里的路禁止通行，更新可行矩阵
     * */
    public static void AFindPath(ArrayList<Cross> crossList, Car car, ArrayList<Road> forbidRoadList){
        AFind find = new AFind(crossList, car);
        if(forbidRoadList != null && forbidRoadList.size() != 0){
            for(Road road : forbidRoadList){
                find.forbidRoad(road);
            }
        }

        AFind.Node node = find.findPath(new AFind.Node(AFind.getCross(car.getStart(), crossList)),
                new AFind.Node(AFind.getCross(car.getEnd(), crossList)));

        ArrayList<Road> res = new ArrayList<>();
        //反转链表，因为结果集是反的
        AFind.Node newHead = null;
        while(node != null){
            AFind.Node temp = node.parent;
            node.parent = newHead;
            newHead = node;
            node = temp;
        }
        //添加结果集
        int weight = 0;
        while(newHead != null){
            if(newHead.road != null){
                res.add(newHead.road);
                weight += newHead.road.getWeight();
            }
            newHead = newHead.parent;
        }
        car.setWeight(weight);//设置当前车的权重
        car.setRoads(res);//更新当前车所走的通路
    }
    /**
     * 重载函数
     * 单条路禁止
     * */
    public static void AFindPath(ArrayList<Cross> crossList, Car car, Road forbidRoad){
        AFind find = new AFind(crossList, car);
        if(forbidRoad != null){
            find.forbidRoad(forbidRoad);
        }

        AFind.Node node = find.findPath(new AFind.Node(AFind.getCross(car.getStart(), crossList)),
                new AFind.Node(AFind.getCross(car.getEnd(), crossList)));

        ArrayList<Road> res = new ArrayList<>();
        //反转链表，因为结果集是反的
        AFind.Node newHead = null;
        while(node != null){
            AFind.Node temp = node.parent;
            node.parent = newHead;
            newHead = node;
            node = temp;
        }
        //添加结果集
        int weight = 0;
        while(newHead != null){
            if(newHead.road != null){
                res.add(newHead.road);
                weight += newHead.road.getWeight();
            }
            newHead = newHead.parent;
        }
        car.setWeight(weight);//设置当前车的权重
        car.setRoads(res);//更新当前车所走的通路
    }
    /**
     * 重载函数
     * 起点到终点的寻路，即根据ID
     * startId：起点
     * endId：终点
     * */
    public static void AFindPath(ArrayList<Cross> crossList, Car car, int startId, int endId){
        AFind find = new AFind(crossList, car);
        AFind.Node node = find.findPath(new AFind.Node(getCross(startId, crossList)),
                new AFind.Node(getCross(endId, crossList)));

        ArrayList<Road> res = new ArrayList<>();
        //反转链表，因为结果集是反的
        AFind.Node newHead = null;
        while(node != null){
            AFind.Node temp = node.parent;
            node.parent = newHead;
            newHead = node;
            node = temp;
        }
        //添加结果集
        int weight = 0;
        while(newHead != null){
            if(newHead.road != null){
                res.add(newHead.road);
                weight += newHead.road.getWeight();
            }
            newHead = newHead.parent;
        }
        car.setWeight(weight);//设置当前车的权重
        car.setRoads(res);//更新当前车所走的通路
    }
    /**************************************************************************************************************/
    ArrayList<Cross> crossList;
    Car car;

    //需要建立矩阵
    int[][] matrix;
    private final static int INF = -1;

    /**
     * 构造函数，传入crossList和car的信息，car用于更新路的权重
     * */
    public AFind(ArrayList<Cross> crossList, Car car){
        this.crossList = crossList;
        this.car = car;
        //矩阵初始化
        matrix = new int[crossList.size()+1][crossList.size()+1];
        for(int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix[i].length; j++){
                matrix[i][j] = INF;
                if(i == j){matrix[i][j] = 0;}
            }
        }
        //赋值     && cross.getUpRoad().getId()
        for(Cross cross : crossList){
            if(cross.getUpRoad() != null){
                updateMatrix(matrix, cross.getUpRoad(), cross);
            }
            if(cross.getDownRoad() != null){
                updateMatrix(matrix, cross.getDownRoad(), cross);
            }
            if(cross.getLeftRoad() != null){
                updateMatrix(matrix, cross.getLeftRoad(), cross);
            }
            if(cross.getRightRoad() != null){
                updateMatrix(matrix, cross.getRightRoad(), cross);
            }
        }
    }
    /**
     * 根据 road 和 cross来更新矩阵值
     * */
    private void updateMatrix(int[][] matrix, Road road, Cross cross){
        Cross next = getCross(road.getEnd(), crossList);
        if(next == cross || next == null){
            return;
        }
        int weight = road.getWeight();
        matrix[cross.getId()][next.getId()] = weight;
        //双向路
        if(road.getDirected() == 1){
            matrix[next.getId()][cross.getId()] = weight;
        }
    }
    /**
     * 根据 路 封闭矩阵某点的方法
     * */
    private void forbidRoad(Road road){
        int i = road.getStart();
        int j = road.getEnd();
        if(road.getDirected() == 1){
            matrix[i][j] = -1;
            matrix[j][i] = -1;
        } else{
            matrix[i][j] = -1;
        }
    }

    private static final int STEP = 10;//步长，用于计算，走一步的实际代价

    private ArrayList<Node> openList = new ArrayList<>();   //开放列表
    private ArrayList<Node> closeList = new ArrayList<>();  //封闭列表

    /**
     * 找到开放列表中 估值函数F 最小的节点, 可以考虑使用堆，直接弹出
     * */
    private Node findMinFNodeInOpenList() {
        Node tempNode = openList.get(0);
        for (Node node : openList) {
            if (node.F < tempNode.F) {
                tempNode = node;
            }
        }
        return tempNode;
    }

    /**
     * 找到该节点的相邻节点
     * @return 返回相邻节点的表
     * */
    private ArrayList<Node> findNeighborNodes(Node currentNode) {
        ArrayList<Node> arrayList = new ArrayList<>();

        // 只考虑上下左右，不考虑斜对角  与道路行规则一致
        Cross cross = currentNode.cross;
        if(cross != null) {
            for(int i = 1; i < matrix.length; i++) {
                Cross next = getCross(i, crossList);    //下一个要到达的路口
                if (canReach(cross, i) && !exists(closeList, next)) {
                    //判断具有相同道路, 利用hash添加公共路
                    HashSet<Road> roads = new HashSet<>();
                    roads.add(cross.getUpRoad());
                    roads.add(cross.getDownRoad());
                    roads.add(cross.getLeftRoad());
                    roads.add(cross.getRightRoad());

                    Road road = null;
                    for(Road r : roads){
                        if(r != null && next != null) {
                            if (r == next.getUpRoad()) {
                                road = r;
                                break;
                            }
                            if (r == next.getLeftRoad()) {
                                road = r;
                                break;
                            }
                            if (r == next.getDownRoad()) {
                                road = r;
                                break;
                            }
                            if (r == next.getRightRoad()) {
                                road = r;
                                break;
                            }
                        }
                    }

                    if(road != null){
                        //此处更新权值，但是怎么更新?
                        if(currentNode.road != null){
                            matrix[cross.getId()][i] = currentNode.road.getWeight();
                        }
                        //添加进邻居
                        arrayList.add(new Node(next, road, car));
                    }
                }
            }
        }

        return arrayList;
    }

    /**
     * 是否可以到达
     * cross : 起点路口
     * i ： 下一个要到达的路口id
     * */
    private boolean canReach(Cross cross, int i) {
        return matrix[cross.getId()][i] != -1 && matrix[cross.getId()][i] != 0;
    }

    /**
     * 此为主要功能函数
     * 寻路：起点到终点
     * */
    private Node findPath(Node startNode, Node endNode) {

        // 把起点加入 open list
        openList.add(startNode);

        while (openList.size() > 0) {
            /*
            //打印矩阵
            System.out.println("第"+count+"次矩阵：");
            count++;
            for(int i = 0; i < matrix.length; i++){
                for(int j = 0; j < matrix[i].length; j++){
                    System.out.print(matrix[i][j] + " ");
                }
                System.out.println();
            }
            System.out.println();
            */
            // 遍历 open list ，查找 F值最小的节点，把它作为当前要处理的节点
            Node currentNode = findMinFNodeInOpenList();
            // 从open list中移除
            openList.remove(currentNode);
            // 把这个节点移到 close list
            closeList.add(currentNode);

            ArrayList<Node> neighborNodes = findNeighborNodes(currentNode);//当前点的邻居
            for (Node node : neighborNodes) {
                //node 在 open 表中，更新值
                if (exists(openList, node)) {
                    foundPoint(currentNode, node);
                } else{
                    //没有在表中，计算值并添加进开放表？？ 怎么更新
                    notFoundPoint(currentNode, endNode, node);
                }
            }
            //找到终点，open表中存在endNode，则返回
            if (find(openList, endNode) != null) {
                return find(openList, endNode);
            }
        }
        //表示已经无路可寻，返回最后寻找的结果
        return find(openList, endNode);
    }

    /**
     *  更新值
     * */
    private void foundPoint(Node tempStart, Node node) {
        int G = calcG(tempStart, node);
        if (G < node.G) {
            node.parent = tempStart;
            node.G = G;
            node.calcF();
        }
    }

    /**
     * 重新计算并添加进开放表中，节点连接
     * */
    private void notFoundPoint(Node tempStart, Node end, Node node) {
        node.parent = tempStart;
        node.G = calcG(tempStart, node);
        node.H = calcH(end, node);
        node.calcF();
        openList.add(node);
    }

    /**
     * 计算G，实际走一步的代价
     * */
    private int calcG(Node start, Node node) {
        int G = STEP;
        int parentG = node.parent != null ? node.parent.G : 0;
        return G + parentG;
    }

    /**
     * 计算H, 此处采用逼近的思想，预估值为两个序号点只之差，比如从1->36 . 会通过可行路，逐步逼近36
     * */
    private int calcH(Node end, Node node) {
        int step = 1;
        //根据序号之差来更新
        Random random = new Random();//随机数
        //step += Math.abs(end.cross.getId() - node.cross.getId());
        /*
        if(end.road != null && node.road != null){
            step += Math.abs(end.road.getWeight() - node.road.getWeight());
        }
        */

        //以当前road权值来选择，可能是局部最优解不是全局最优解
        if(node.road != null){
            step += node.road.getWeight();
        }

        return step * 4;// 4 是权重，自调节，不同值下得到的路的总权重不同
    }

    //寻找目标点是否在范围内
    public static Node find(List<Node> nodes, Node point) {
        for (Node n : nodes)
            if (n.cross == point.cross) {
                return n;
            }
        return null;
    }

    //由cross判断是否存在于表中
    private static boolean exists(List<Node> nodes, Cross cross) {
        for (Node n : nodes) {
            if (n.cross == cross) {
                return true;
            }
        }
        return false;
    }
    public static boolean exists(List<Node> nodes, Node node) {
        for (Node n : nodes) {
            if (n.cross == node.cross) {
                return true;
            }
        }
        return false;
    }

    /**
     *  以cross为顶点的Node
     * */
    public static class Node {
        public Node(Cross cross, Road road, Car car) {
            this.cross = cross;
            this.road = road;
            this.road.setWeight(road.getLength() / Math.min(car.getMaxSpeed(), road.getMaxSpeed()));
        }
        public Node(Cross cross){
            this.cross = cross;
        }
        public Node(Cross cross, ArrayList<Road> roads, Car car){
            this.cross = cross;
            for(Road road : roads){
                road.setWeight(road.getLength() / Math.min(car.getMaxSpeed(), road.getMaxSpeed()));
            }
        }

        public Cross cross;
        public Road road;

        public int F;
        public int G;
        public int H;

        /**
         * 估算距离 ：F = G + H     G 为规定的走一步的花费，   H为距离计算值，即当前点到目的地点的距离
         * */
        public void calcF() {
            this.F = this.G + this.H;
        }

        public Node parent;
    }

    /**
     * @Function:  根据id获取cross
     * @param
     * id : 路口顶点的id
     *    crossList : 顶点集合
     * */
    private static Cross getCross(int id, ArrayList<Cross> crossList){
        for(Cross cross : crossList){
            if(cross.getId() == id){
                return cross;
            }
        }
        return null;
    }
}