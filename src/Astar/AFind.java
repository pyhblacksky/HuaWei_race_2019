package Astar;

import DataStruct.Cross;
import DataStruct.Road;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: pyh
 * @Date: 2019/3/11 19:46
 * @Version 1.0
 * @Function:
 *      A*实现寻路
 *
 */
public class AFind {

    ArrayList<Cross> crossList;

    public AFind(ArrayList<Cross> crossList){
        this.crossList = crossList;
    }

    //步长，用于计算，走一步的实际代价
    public static final int STEP = 1;

    private ArrayList<Node> openList = new ArrayList<>();   //开放列表
    private ArrayList<Node> closeList = new ArrayList<>();  //封闭列表

    /**
     * 找到开放列表中 估值函数F 最小的节点, 可以考虑使用堆，直接弹出
     * */
    public Node findMinFNodeInOpenList() {
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
    public ArrayList<Node> findNeighborNodes(Node currentNode) {
        ArrayList<Node> arrayList = new ArrayList<>();

        // 只考虑上下左右，不考虑斜对角  与道路行规则一致
        //上
        Road up = currentNode.cross.getUpRoad();
        process(up, arrayList);

        //下
        Road down = currentNode.cross.getDownRoad();
        process(down, arrayList);

        //左
        Road left = currentNode.cross.getLeftRoad();
        process(left, arrayList);

        //右
        Road right = currentNode.cross.getLeftRoad();
        process(right, arrayList);

        return arrayList;
    }
    //过程函数
    private void process(Road road, ArrayList<Node> arrayList){
        if(road != null) {
            //考虑道路是否双向
            if (road.getDirected() == 1) {
                Cross cross = getCross(road.getEnd(), crossList);
                //可以到达且在关闭表中不存在
                if (canReach(cross) && !exists(closeList, cross)) {
                    arrayList.add(new Node(cross, road));
                }

                cross = getCross(road.getStart(), crossList);
                if (canReach(cross) && !exists(closeList, cross)) {
                    arrayList.add(new Node(cross, road));
                }
            } else {
                Cross cross = getCross(road.getEnd(), crossList);
                //可以到达且在关闭表中不存在
                if (canReach(cross) && !exists(closeList, cross)) {
                    arrayList.add(new Node(cross, road));
                }
            }
        }
    }

    /**
     * 是否可以到达
     * */
    public boolean canReach(Cross cross) {
        return cross != null;
    }

    /**
     * 此为主要功能函数
     * 寻路：起点到终点
     * */
    public Node findPath(Node startNode, Node endNode) {

        // 把起点加入 open list
        openList.add(startNode);

        while (openList.size() > 0) {
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
                } else {
                    //没有在表中，计算值并添加进开放表
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
     * 计算H??????
     * */
    private int calcH(Node end, Node node) {
        int step = 1;
        if(end.cross.getUpRoad() != null && node.cross.getUpRoad() != null){
            step += Math.abs(end.cross.getUpRoad().getWeight() - node.cross.getUpRoad().getWeight());
        }
        if(end.cross.getDownRoad() != null && node.cross.getDownRoad() != null){
            step += Math.abs(end.cross.getDownRoad().getWeight() - node.cross.getDownRoad().getWeight());
        }
        if(end.cross.getLeftRoad() != null && node.cross.getLeftRoad() != null){
            step += Math.abs(end.cross.getLeftRoad().getWeight() - node.cross.getLeftRoad().getWeight());
        }
        if(end.cross.getRightRoad() != null && node.cross.getRightRoad() != null){
            step += Math.abs(end.cross.getRightRoad().getWeight() - node.cross.getRightRoad().getWeight());
        }

        return step * STEP;
    }

    //寻找目标点是否在范围内
    public static Node find(List<Node> nodes, Node point) {
        for (Node n : nodes)
            if (n.cross == point.cross) {
                return n;
            }
        return null;
    }

    //由坐标判断是否存在于表中
    public static boolean exists(List<Node> nodes, Cross cross) {
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
        public Node(Cross cross, Road road) {
            this.cross = cross;
            this.road = road;
        }
        public Node(Cross cross){
            this.cross = cross;
        }

        Cross cross;
        Road road;

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
    public static Cross getCross(int id, ArrayList<Cross> crossList){
        for(Cross cross : crossList){
            if(cross.getId() == id){
                return cross;
            }
        }
        return null;
    }
}
