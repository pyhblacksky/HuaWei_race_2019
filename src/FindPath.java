import DataStruct.Cross;
import DataStruct.Graph;
import DataStruct.Road;

import java.util.*;

/**
 * @Author: pyh
 * @Date: 2019/3/9 10:06
 * @Version 1.0
 * @Function:       寻找路径
 *
 */
public class FindPath{

    //标记set，走过的顶点不能走
    static Set<Integer> set = new HashSet<>();

    //从start出发到end，路口顶点集合为cross
    public static ArrayList find(ArrayList<Cross> crossList, int start, int end){
        ArrayList<ArrayList<Road>> roads = new ArrayList<>();
        for(Cross cross : crossList){
            //找到起点
            if(cross.getId() == start){
                set.add(start);
                find(cross, start, end, new ArrayList<>(), roads, crossList);
                break;
            }
        }
        return roads;
    }

    //迭代寻路
    private static void find(Cross cross, int start, int end,
                             ArrayList<Road> list, ArrayList<ArrayList<Road>> roads, ArrayList<Cross> crossList){
        if(cross == null){
            return;
        }

        //找到路径
        if(start == end){
            ArrayList<Road> temp = new ArrayList<>(list);
            roads.add(temp);
            return;
        }

        //上下左右四个方向
        if(cross.getUpRoad() != null){
            list.add(cross.getUpRoad());
            int id = cross.getUpRoad().getEnd();
            set.add(id);  //走过的路
            find(getCross(id, crossList), id, end, list, roads, crossList);  //寻找下一条路
            set.remove(id);   //回溯
        }
        if(cross.getLeftRoad() != null){
            list.add(cross.getLeftRoad());
            int id = cross.getLeftRoad().getEnd();

            set.add(id);  //走过的路
            find(getCross(id, crossList), id, end, list, roads, crossList);  //寻找下一条路
            set.remove(id);   //回溯
        }
        if(cross.getDownRoad() != null){
            list.add(cross.getDownRoad());
            int id = cross.getDownRoad().getEnd();
            set.add(id);  //走过的路
            find(getCross(id, crossList), id, end, list, roads, crossList);  //寻找下一条路
            set.remove(id);   //回溯
        }
        if(cross.getRightRoad() != null){
            list.add(cross.getRightRoad());
            int id = cross.getRightRoad().getEnd();
            set.add(id);  //走过的路
            find(getCross(id, crossList), id, end, list, roads, crossList);  //寻找下一条路
            set.remove(id);   //回溯
        }

    }

    //根据id获取cross
    private static Cross getCross(int id, ArrayList<Cross> crossList){
        for(Cross cross : crossList){
            if(cross.getId() == id){
                return cross;
            }
        }
        return null;
    }

}



/*
public class FindPath {

    //遍历这层的路，id不为-1即可
    static void find(Graph graph, int start, int end, ArrayList<Road> roads,
                     ArrayList<ArrayList<Road>> res, HashSet<Road> passRoad){
        if(graph == null){
            return;
        }

        if(start >= graph.matrix.length){
            return;
        }

        if(start == end && !res.contains(roads)){
            ArrayList<Road> temp = new ArrayList<>(roads);
            res.add(temp);
            return;
        }

        //走过的十字路不能重复走
        //走过的路不能重复走

    }
    private static Stack<Road> stack = new Stack<>();
    private static Map<Road, Boolean> status = new HashMap<>();//确定遍历状态
    public static void DFS(Graph graph, int start, int end){
        if(graph == null){
            return;
        }

        for(int i = 0; i < graph.matrix[start].length; i++){
            Road road = graph.matrix[start][i];
            int id = road.getId();
            if(id != -1){
                stack.push(road);
                status.put(road, true);
                dfsLoop();
            }
        }
    }

    public static void dfsLoop(){
        if(stack.isEmpty()){
            return;
        }

        Road stackTop = stack.peek();

    }

}
*/