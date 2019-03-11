package Astar;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: pyh
 * @Date: 2019/3/11 15:19
 * @Version 1.0
 * @Function:   A* 寻路算法     目的：测试
 *      关于A*算法 ： 路径代价的估算：F = G + H
 *          G表示的是从起点到当前结点的实际路径代价
 *          H表示当前结点到达最终结点的估计代价
 *          F表示当前结点所在路径从起点到最终点预估的总路径代价
 *
 *      效率优化方向： 开启列表用二叉堆，关闭列表用LinkedHashSet
 *
 *      考虑D*算法，  是一种动态 A*
 */
public class AStar {

    /**
     * 模拟的测试矩阵，0为可行路，1为障碍物
     * */
    public static final int[][] NODES = {
            { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 1, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 1, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 1, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 1, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    };

    //步长，用于计算，走一步的实际代价
    public static final int STEP = 10;

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
        int topX = currentNode.x;
        int topY = currentNode.y - 1;
        //可以到达且在关闭表中不存在
        if (canReach(topX, topY) && !exists(closeList, topX, topY)) {
            arrayList.add(new Node(topX, topY));
        }
        //下
        int bottomX = currentNode.x;
        int bottomY = currentNode.y + 1;
        if (canReach(bottomX, bottomY) && !exists(closeList, bottomX, bottomY)) {
            arrayList.add(new Node(bottomX, bottomY));
        }
        //左
        int leftX = currentNode.x - 1;
        int leftY = currentNode.y;
        if (canReach(leftX, leftY) && !exists(closeList, leftX, leftY)) {
            arrayList.add(new Node(leftX, leftY));
        }
        //右
        int rightX = currentNode.x + 1;
        int rightY = currentNode.y;
        if (canReach(rightX, rightY) && !exists(closeList, rightX, rightY)) {
            arrayList.add(new Node(rightX, rightY));
        }
        return arrayList;
    }

    /**
     * 是否可以到达，主要判断有无障碍物
     * */
    public boolean canReach(int x, int y) {
        if (x >= 0 && x < NODES.length && y >= 0 && y < NODES[0].length) {
            return NODES[x][y] == 0;
        }
        return false;
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
     * 计算H，这里是使用直线距离
     * */
    private int calcH(Node end, Node node) {
        int step = Math.abs(node.x - end.x) + Math.abs(node.y - end.y);
        return step * STEP;
    }

    //寻找目标点是否在范围内
    public static Node find(List<Node> nodes, Node point) {
        for (Node n : nodes)
            if ((n.x == point.x) && (n.y == point.y)) {
                return n;
            }
        return null;
    }

    //由点判断是否存在于表中
    public static boolean exists(List<Node> nodes, Node node) {
        for (Node n : nodes) {
            if ((n.x == node.x) && (n.y == node.y)) {
                return true;
            }
        }
        return false;
    }

    //由坐标判断是否存在于表中
    public static boolean exists(List<Node> nodes, int x, int y) {
        for (Node n : nodes) {
            if ((n.x == x) && (n.y == y)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 节点结构
     * */
    public static class Node {
        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int x;
        public int y;

        public int F;
        public int G;
        public int H;

        /**
         * 估算距离 ： F = G + H     G 为规定的走一步的花费，   H为距离计算值，即当前点到目的地点的距离
         * */
        public void calcF() {
            this.F = this.G + this.H;
        }

        public Node parent;
    }

    //主函数，运行测试
    public static void main(String[] args) {
        Node startNode = new Node(5, 1);//起点
        Node endNode = new Node(5, 5);//终点
        Node parent = new AStar().findPath(startNode, endNode);

        //打印原图
        for (int i = 0; i < NODES.length; i++) {
            for (int j = 0; j < NODES[0].length; j++) {
                System.out.print(NODES[i][j] + ", ");
            }
            System.out.println();
        }
        ArrayList<Node> arrayList = new ArrayList<>();

        while (parent != null) {
            // System.out.println(parent.x + ", " + parent.y);
            arrayList.add(new Node(parent.x, parent.y));
            parent = parent.parent;
        }
        System.out.println("\n");

        //打印寻路效果图
        for (int i = 0; i < NODES.length; i++) {
            for (int j = 0; j < NODES[0].length; j++) {
                if (exists(arrayList, i, j)) {
                    System.out.print("#, ");//寻找到的路径用 '#'来表示
                } else {
                    System.out.print(NODES[i][j] + ", ");
                }

            }
            System.out.println();
        }

    }
}
