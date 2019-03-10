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

    //标记set，走过的顶点不能走
    private static Set<Integer> set = new HashSet<>();

    //从start出发到end，路口顶点集合为cross
    public static ArrayList<ArrayList<Road>> find(ArrayList<Cross> crossList, int start, int end){
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

        //找到路径, 添加
        if(start == end){
            ArrayList<Road> temp = new ArrayList<>(list);
            roads.add(temp);
            return;
        }

        //上下左右四个方向
        Road up = cross.getUpRoad();
        process(up, end, list, roads, crossList);

        Road left = cross.getLeftRoad();
        process(left, end, list, roads, crossList);

        Road down = cross.getDownRoad();
        process(down, end, list, roads, crossList);

        Road right = cross.getRightRoad();
        process(right, end, list, roads, crossList);

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

    //寻路过程函数
    private static void process(Road direct, int end, ArrayList<Road> list,
                                ArrayList<ArrayList<Road>> roads, ArrayList<Cross> crossList){
        if(direct != null && !list.contains(direct)){
            list.add(direct);
            //如果是双向，则有两种走向
            int id;
            if(direct.getDirected() == 1){
                id = direct.getEnd();
                if(!set.contains(id)){
                    set.add(id);  //走过的路
                    find(getCross(id, crossList), id, end, list, roads, crossList);  //寻找下一条路
                    set.remove(id);   //回溯
                }

                id = direct.getStart();
                if(!set.contains(id)){
                    set.add(id);  //走过的路
                    find(getCross(id, crossList), id, end, list, roads, crossList);  //寻找下一条路
                    set.remove(id);   //回溯
                }
            } else{
                //是单行道
                id = direct.getEnd();
                if(!set.contains(id)){
                    set.add(id);  //走过的路
                    find(getCross(id, crossList), id, end, list, roads, crossList);  //寻找下一条路
                    set.remove(id);   //回溯
                }
            }

            list.remove(list.size()-1);
        }
    }
}