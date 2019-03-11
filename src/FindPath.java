import DataStruct.Cross;
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

    /**
     * @Function : 寻路函数
     * @param
     * crossList 所有路口的集合
     *           start : 起点
     *           end ： 终点
     *           set : 标记set，走过的顶点(路口)不能走.寻路一次，set需要开辟新空间
     * */
    public static ArrayList<ArrayList<Road>> find(ArrayList<Cross> crossList, int start, int end, HashSet<Integer> set){
        ArrayList<ArrayList<Road>> roads = new ArrayList<>();
        for(Cross cross : crossList){
            //找到起点
            if(cross.getId() == start){
                set.add(start);
                find(cross, start, end, new ArrayList<>(), roads, crossList, set);
                break;
            }
        }
        return roads;
    }

    /**
     * @Function: 迭代实现寻路功能
     * @param
     *       cross : 路口顶点
     *       start : 起点
     *       end : 终点
     *       list : 存储单条路径的list
     *       roads : 存储结果集合
     *       crossList : 顶点集合
     *       set : 标记set，走过的顶点(路口)不能走.
     * */
    private static void find(Cross cross, int start, int end,
                             ArrayList<Road> list, ArrayList<ArrayList<Road>> roads,
                             ArrayList<Cross> crossList, HashSet<Integer> set){
        if(cross == null){
            return;
        }

        //找到路径, 添加
        if(start == end){
            ArrayList<Road> temp = new ArrayList<>(list);
            roads.add(temp);
            return;
        }

        //上下左右四个方向
        Road up = cross.getUpRoad();
        process(up, end, list, roads, crossList, set);

        Road left = cross.getLeftRoad();
        process(left, end, list, roads, crossList, set);

        Road down = cross.getDownRoad();
        process(down, end, list, roads, crossList, set);

        Road right = cross.getRightRoad();
        process(right, end, list, roads, crossList, set);

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

    /**
     * @Function: 根据方向寻路的过程函数
     * @param
     * direct : 方向（上下左右）
     *        end : 终点
     *        list : 存储单条路径的list
     *        roads : 存储结果集合
     *        crossList : 顶点集合
     *        set : 标记set，走过的顶点(路口)不能走.
     * */
    private static void process(Road direct, int end, ArrayList<Road> list,
                                ArrayList<ArrayList<Road>> roads,
                                ArrayList<Cross> crossList, HashSet<Integer> set){
        if(direct != null && !list.contains(direct)){
            list.add(direct);
            //如果是双向，则有两种走向
            int id;
            if(direct.getDirected() == 1){
                id = direct.getEnd();
                if(!set.contains(id)){
                    set.add(id);  //走过的路
                    find(getCross(id, crossList), id, end, list, roads, crossList, set);  //寻找下一条路
                    set.remove(id);   //回溯
                }

                id = direct.getStart();
                if(!set.contains(id)){
                    set.add(id);  //走过的路
                    find(getCross(id, crossList), id, end, list, roads, crossList, set);  //寻找下一条路
                    set.remove(id);   //回溯
                }
            } else{
                //是单行道
                id = direct.getEnd();
                if(!set.contains(id)){
                    set.add(id);  //走过的路
                    find(getCross(id, crossList), id, end, list, roads, crossList, set);  //寻找下一条路
                    set.remove(id);   //回溯
                }
            }

            list.remove(list.size()-1);
        }
    }
}