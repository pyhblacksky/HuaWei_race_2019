package Judgment;

import DataStruct.Car;
import DataStruct.CarState;
import DataStruct.Cross;
import DataStruct.Road;
import Util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @Author: 侯磊  马纯诗         从C++改Java ：pyh
 * @Date: 2019/3/17 21:19
 * @Version 1.0
 * @Function:   通行路口规则，此处C++改编
 */
public class ThroughCrossRule {

    /**
     * 根据路口的ID得到连接的道路ID，并确定优先级
     * @param cross : 路口
     * @return 按照降序排列的Road列表
     * */
    public static ArrayList<Road> RoadPriority(Cross cross) {
        ArrayList<Road> Roads = new ArrayList<>();

        Road_regular(Roads, cross);

        //排序找到优先级最高的道路ID
        Collections.sort(Roads, new Comparator<Road>() {
            @Override
            public int compare(Road o1, Road o2) {
                return o1.getId() - o2.getId();
            }
        });
        ////返回优先级最高的道路,用道路id是否为-1来判断
        //Road maxPro = null;
        //for(Road road : Roads){
        //    if(road != null && road.getId() != -1){
        //        maxPro = road;
        //        break;
        //    }
        //}
        ////if(Roads.get(0).getId()!=-1)
        ////    return Roads.get(0);
        ArrayList<Road> res = new ArrayList<>();
        for(Road road : Roads){
            if(road != null && road.getId() != -1 && road.getId() != 0){
                res.add(road);
            }
        }
        return res;
    }

    /**
     * 根据Car的规划路径来判断其要直行还是左转还是右转。用0代表直行，1代表左转，2代表右转
     * 假设路径存放在Car的属性ArrayList<Road> roads中
     * @param AllCross 所有的路口的列表
     * @param AllRoads 所有的道路的列表
     * @param car 判断当前车的情况，其运行方向
     *
     * @return 返回0——直行  返回1——左转 返回2——右转     -1表示已到达终点
     */
    public static int JudgeDirection(Car car, ArrayList<Road> AllRoads, ArrayList<Cross> AllCross) {
        //获取Car当前所在道路的ID
        CarState carstate = car.getCarState();
        int CurrentRoadID = carstate.getRoadId();
        Road CurrentRoad = Util.getRoadFromId(CurrentRoadID, AllRoads);//遍历存放所有Road的vector<Road> AllRoads来寻找CurrentRoadID对应的CurrentRoad
        if(CurrentRoad == null){
            //当前道路为空，说明到达终点
            return -1;
        }
        //取出每辆车所对应的预计估行驶路径
        ArrayList<Road> path = new ArrayList<>(car.getRoads());
        int k = 0;//表示当前在第行驶的第几条路上
        for(Road road : path){
            if(road == CurrentRoad){
                break;
            }
            k++;
        }
        if(k >= path.size()-1){
            //k大于当前可行驶道路，说明到达终点，返回-1表示结束
            return -2;
        }
        Road NextRoad = path.get(k+1);//下一条要行驶的道路   k+1
        if(NextRoad == null){
            //下一条要行驶的道路为空，说明已经到达终点，返回-1
            return -1;
        }
        //根据CurrentRoad和NextRoad来确定连接它们的Cross路口
        int CurrentRoadStart = CurrentRoad.getStart();
        int CurrentRoadEnd = CurrentRoad.getEnd();
        int NextRoadStart = NextRoad.getStart();
        int NextRoadEnd = NextRoad.getEnd();
        //两条道路都有自己的start和end，当中相等的便是连接它们的Cross路口
        int CurrentCrossID = CurrentRoadStart == NextRoadStart ? CurrentRoadStart : CurrentRoadStart == NextRoadEnd ? CurrentRoadStart : CurrentRoadEnd;

        //遍历存放所有Cross的vector<Cross> AllCross来寻找CurrentCrossID对应的CurrentCross
        Cross CurrentCross = Util.getCrossFromId(CurrentCrossID, AllCross);

        ArrayList<Road> Roads = new ArrayList<>();

        Road_regular(Roads, CurrentCross);

        //判断直行还是左转还是右转
        int current = 0, next = 0;
        for (int i = 0; i < Roads.size(); i++) {
            if (Roads.get(i).getId() == CurrentRoad.getId()) {
                current = i;
            }
            if (Roads.get(i).getId() == NextRoad.getId()) {
                next = i;
            }
        }
        int result = -1;//表示计算出错
        //用0代表直行，1代表左转，2代表右转
        if ((next - current == 1) || (next - current == -3))
            result = 1;
        if ((next - current == 2) || (next - current == -2))
            result = 0;
        if ((next - current == 3) || (next - current == -1))
            result = 2;
        return result;
    }

    //生成当前路口的道路矩阵
    public static void Road_regular(ArrayList<Road> Roads, Cross cross) {
        //上下左右存路口的道路向量
        //if(cross.getUpRoad() != null && cross.getUpRoad().getId() != 0){
        //    Roads.add(cross.getUpRoad());
        //}
        //if(cross.getRightRoad() != null && cross.getRightRoad().getId() != 0){
        //    Roads.add(cross.getRightRoad());
        //}
        //if(cross.getDownRoad() != null && cross.getDownRoad().getId() != 0){
        //    Roads.add(cross.getDownRoad());
        //}
        //if(cross.getLeftRoad() != null && cross.getLeftRoad().getId() != 0){
        //    Roads.add(cross.getLeftRoad());
        //}
        Roads.add(cross.getUpRoad());
        Roads.add(cross.getRightRoad());
        Roads.add(cross.getDownRoad());
        Roads.add(cross.getLeftRoad());
    }

    /******************************************************修改版*******************************************************/

    /**
     * 根据3、4准则，返回头车下一条道路可行驶的最大距离（未考虑下一条路的实际状况）
     * -1 考察车道为空车道
     * -2 可能出现的未考虑到的情况
     **/
    public static int Pass_or_Not(Cross cross, int lane, ArrayList<Road> AllRoads, ArrayList<Cross> AllCross) {
        int Control_priority;
        int Topcar_loc = 0;
        int Direction_me;
        int vector_location = 0;

        int[] num0 = { 3,0,1,2 }; //右面，前一个标号
        int[] num1 = { 1,2,3,0 }; //左面，下一个标号
        int[] num2 = { 2,3,0,1 }; //对面， 对面标号
        ArrayList<ArrayList<Car>>  Matrix_temp;
        ArrayList<Road> Roads = new ArrayList<>();
        Road_regular(Roads, cross);  //四条道路顺时针形成道路向量
        Control_priority = cross.getControl_priority();
        Road road = Roads.get(Control_priority);
        if (road.getStart() == cross.getId()) {
            if (road.getDirected() == 1){//为双向路
                Matrix_temp = road.getMatrix_E2S();
                Topcar_loc = road.get_Topcar_location_E2S(lane+1);
            }  else{ //被忽略的情况
                Matrix_temp = null;
                Topcar_loc = -2;
            }
        } else {

            Matrix_temp = road.getMatrix_S2E();
            if(Matrix_temp == null){
                Topcar_loc = -2;
            } else
                Topcar_loc = road.get_Topcar_location_S2E(lane+1);
        }

        if (Topcar_loc == -1) return -1;  //-1表示本车道为空车道
        if (Topcar_loc == -2) return -2;//未考虑的情况

        Car car = Matrix_temp.get(lane).get(Topcar_loc);

        for (int i = 0; i <= 3; i++){   // 寻找我车所在道路在向量中的定位
            if (car.getCarState().getRoadId() == Roads.get(i).getId()){
                vector_location = i;
                break;
            }
        }

        int Car_Nextroad_V;
        int Max_dis_allow;

        Direction_me= JudgeDirection(car, AllRoads, AllCross);
        if (Direction_me == 0) { //我方直行

            Road road1 = Roads.get(num2[vector_location]);
            Car_Nextroad_V = Math.min(car.getMaxSpeed(), road1.getMaxSpeed());
            Max_dis_allow = road.getLength() - 1 - Topcar_loc;

            if (Max_dis_allow >= Car_Nextroad_V)   return 0;
            else                                   return (Car_Nextroad_V- Max_dis_allow);
        } else if (Direction_me == 1)  {//我方左转

            Road road1 = Roads.get(num1[vector_location]);
            Car_Nextroad_V = Math.min(car.getMaxSpeed(), road1.getMaxSpeed());
            Max_dis_allow = road.getLength() - 1 - Topcar_loc;

            if (Max_dis_allow >= Car_Nextroad_V)   return 0;
            else                                   return (Car_Nextroad_V - Max_dis_allow);

        } else if (Direction_me == 2) { //我方右转

            Road road1 = Roads.get(num0[vector_location]);
            Car_Nextroad_V = Math.min(car.getMaxSpeed(), road1.getMaxSpeed());
            Max_dis_allow = road.getLength() - 1 - Topcar_loc;

            if (Max_dis_allow >= Car_Nextroad_V)   return 0;
            else                                   return (Car_Nextroad_V - Max_dis_allow);

        } else if(Direction_me == -2){
            int Car_Thisroad_V = Math.min(car.getMaxSpeed(),road.getMaxSpeed());
            Max_dis_allow = road.getLength() - 1 - Topcar_loc;

            if (Max_dis_allow >= Car_Thisroad_V)   return 0;
            else                                   return  -2; //直接到终点
        }
        System.out.println("Pass_or_Not 返回值为-2   异常情况");
        return -2;//异常情况
    }

    /**
    * 考察我车所在车道是否具有行车控制权，没有则转让行车控制权给路口其他道路。
    * 路口的第一辆车，路口 ，车道数#判断车辆控制权及可否开车
     * @param cross 当前路口
     * @param AllCross 所有路口的列表
     * @param AllRoads 所有路的列表
     * @param car_Thisroad 将要行驶的车辆
     *
     * @return 返回是否更新矩阵
    * */
    public static boolean Go_control_priority(Car car_Thisroad, Cross cross, ArrayList<Road> AllRoads, ArrayList<Cross> AllCross) {
        //int lane;
        int Direction_me;//0——直行 1——左转   2——右转   -1——空车道    -2 到终点   -3——空路
        int[] num0 = { 3,0,1,2 }; //右面，前一个标号
        int[] num1 = { 1,2,3,0 }; //左面，下一个标号
        int[] num2 = { 2,3,0,1 }; //对面， 对面标号

        //此处是否初始化为0
        int vector_location = 0;  //我车所在道路 在道路向量中的定位
        int loc_temp = 0;//车在道路矩阵上的位置
        ArrayList<ArrayList<Car>>  Matrix_temp;//初始化

        ArrayList<Road> Roads = new ArrayList<>();
        Road_regular(Roads, cross);  //四条道路顺时针形成道路向量

        for (int i = 0; i <= 3; i++){   // 寻找我车所在道路在向量中的定位
            if (car_Thisroad.getCarState().getRoadId() == Roads.get(i).getId()){
                vector_location = i;
                break;
            }
        }

        cross.setControl_priority(vector_location);//默认优先级在我方，相当于初始化值



        Direction_me = JudgeDirection(car_Thisroad, AllRoads, AllCross);

        if (Direction_me == 0||Direction_me == -2){//我方直行
            cross.setControl_priority(vector_location);//优先级在我方道路

            return true;
        } else if (Direction_me == 1){//我方左转

            int vector_location0;
            int  Direction_r = -1;  //右侧车道1号路头车自己的行驶方向，-1表示空车道没车。

            vector_location0 = num0[vector_location];

            if (Roads.get(vector_location0).getStart() == cross.getId()){//道路方向向外
                if (Roads.get(vector_location0).getDirected() == 1){//并且为双向路
                    Matrix_temp = Roads.get(vector_location0).getMatrix_E2S();
                    //loc_temp = Roads.get(vector_location0).get_Topcar_location_E2S(lane );
                } else{//道路向外单向道
                    Matrix_temp = null;
                    loc_temp = -3;
                }
            } else {//道路方向向内
                Matrix_temp = Roads.get(vector_location0).getMatrix_S2E();
                if(Matrix_temp == null){
                    loc_temp = -3;
                }//无路
                //else
                //loc_temp = Roads.get(vector_location0).get_Topcar_location_S2E(lane );
            }

            int laneTemp = 0;
            loc_temp = Roads.get(vector_location0).getLength()-1;
            boolean jumpOut = false;
            for(int i = Roads.get(vector_location0).getLength()-1; i >= 0; i--) {
                for (int lane = 0; lane < Roads.get(vector_location0).getLanes(); lane++) {
                    if(Matrix_temp.get(lane).get(i).getId() != -1){
                        laneTemp = lane;
                        loc_temp = i;
                        jumpOut = true;
                        break;
                    }
                }
                if(jumpOut){
                    break;
                }
            }


            Car car_r = new Car(-1,-1,-1,-1,-1);
            if (loc_temp == -1)  Direction_r = -1;//空车道
            else if(loc_temp == -3) Direction_r = -3;//空路
            else {
                car_r = Matrix_temp.get(laneTemp).get(loc_temp);
                Direction_r = JudgeDirection(car_r, AllRoads, AllCross);
            }


            if ((Direction_r == 0 || Direction_r == -2) && car_r.getCarState().isWait()) {
                cross.setControl_priority(num0[vector_location]);

                return false;
            } else {
                cross.setControl_priority(vector_location);


                return true;
            }

        } else if (Direction_me == 2){ //我方右转
            int vector_location1;

            vector_location1 = num1[vector_location];
            int  Direction_l = -1;
            //cout << Roads[vector_location1].getStart() << "   " << cross.getId() << endl;
            if (Roads.get(vector_location1).getStart() == cross.getId()){//道路方向向外
                if (Roads.get(vector_location1).getDirected() == 1){//为双向路
                    Matrix_temp = Roads.get(vector_location1).getMatrix_E2S();
                } else{//道路向外单向道
                    Matrix_temp = null;
                    loc_temp = -3;
                }
            } else {//道路方向向内
                Matrix_temp = Roads.get(vector_location1).getMatrix_S2E();
                if(Matrix_temp == null){
                    loc_temp = -3;
                }
            }

            int laneTemp = 0;
            loc_temp = Roads.get(vector_location1).getLength()-1;
            boolean jumpOut = false;
            for(int i = Roads.get(vector_location1).getLength()-1; i >= 0; i--) {
                for (int lane = 0; lane < Roads.get(vector_location1).getLanes(); lane++) {
                    if(Matrix_temp.get(lane).get(i).getId() != -1){
                        laneTemp = lane;
                        loc_temp = i;
                        jumpOut = true;
                        break;
                    }
                }
                if(jumpOut){
                    break;
                }
            }

            Car car_l = new Car(-1,-1,-1,-1,-1);
            if (loc_temp == -1)  Direction_l = -1;
            else if(loc_temp == -3) Direction_l = -3;
            else {
                car_l = Matrix_temp.get(laneTemp).get(loc_temp);
                Direction_l = JudgeDirection(car_l, AllRoads, AllCross);
            }

            if ((Direction_l == 0 || Direction_l == -2) && car_l.getCarState().isWait()) {
                cross.setControl_priority(num1[vector_location]);


                return false;
            } else {
                int vector_location2;
                vector_location2 = num2[vector_location];

                int  Direction_op = -1;
                if (Roads.get(vector_location2).getStart() == cross.getId()){//道路方向向外
                    if (Roads.get(vector_location2).getDirected() == 1){//为双向路
                        Matrix_temp = Roads.get(vector_location2).getMatrix_E2S();
                    } else{//道路向外单向道
                        Matrix_temp = null;
                        loc_temp = -3;
                    }
                } else {//道路方向向内
                    Matrix_temp = Roads.get(vector_location2).getMatrix_S2E();
                    if(Matrix_temp == null){loc_temp = -3;}

                }

                laneTemp = 0;
                loc_temp = Roads.get(vector_location2).getLength()-1;
                jumpOut = false;
                for(int i = Roads.get(vector_location2).getLength()-1; i >= 0; i--) {
                    for (int lane = 0; lane < Roads.get(vector_location2).getLanes(); lane++) {
                        if(Matrix_temp.get(lane).get(i).getId() != -1){
                            laneTemp = lane;
                            loc_temp = i;
                            jumpOut = true;
                            break;
                        }
                    }
                    if(jumpOut){
                        break;
                    }
                }

                Car car_op = new Car(-1,-1,-1,-1,-1);
                if (loc_temp == -1)  Direction_op = -1;
                else  if(loc_temp == -3) Direction_op = -3;
                else {
                    car_op = Matrix_temp.get(laneTemp).get(loc_temp);
                    Direction_op = JudgeDirection(car_op, AllRoads, AllCross);
                }

                if (Direction_op == 1 && car_op.getCarState().isWait()) {
                    cross.setControl_priority(num2[vector_location]);


                    return false;
                } else {
                    cross.setControl_priority(vector_location);


                    return true;
                }
            }
        }
        return true;
        //cout << cross.getControl_priority() << "  " << cross.getId() << endl;
    }

    /**
    * 返回在实际情况下我车的的可走距离
    * */
    public static int GO_distance(Cross cross, int lane, ArrayList<Road> AllRoads, ArrayList<Cross> AllCross,int pass_or_not) {
        //实际前行定的距离
        //int pass_or_not = Pass_or_Not(cross, lane, AllRoads, AllCross);

        /********************start0**************************************/
        if (pass_or_not == -1) return -1;  //本车道为空车道 或 异常
        else if(pass_or_not == -2)
        {
            return -2;
        }
        else {
            /******************************start1**************************************/
            // 寻找我车所在道路在向量中的定位,获得这条路的矩阵
            int Control_priority;
            int Topcar_Thisroad_loc = 0;
            int Direction_me;
            int vector_location = 0;

            int[] num0 = { 3,0,1,2 }; //右面，前一个标号
            int[] num1 = { 1,2,3,0 }; //左面，下一个标号
            int[] num2 = { 2,3,0,1 }; //对面， 对面标号
            ArrayList<ArrayList<Car>>  Matrix_Thisroad = new ArrayList<>();
            ArrayList<Road> Roads = new ArrayList<>();
            Road_regular(Roads, cross);  //四条道路顺时针形成道路向量
            Control_priority = cross.getControl_priority();
            Road Thisroad = Roads.get(Control_priority);
            if (Thisroad.getStart() == cross.getId()) {
                if (Thisroad.getDirected() == 1){//为双向路

                    Matrix_Thisroad = Thisroad.getMatrix_E2S();
                    Topcar_Thisroad_loc = Thisroad.get_Topcar_location_E2S(lane+1);
                }
            } else {
                Matrix_Thisroad = Thisroad.getMatrix_S2E();
                Topcar_Thisroad_loc = Thisroad.get_Topcar_location_S2E(lane+1);
            }

            Car Topcar = Matrix_Thisroad.get(lane).get(Topcar_Thisroad_loc);

            for (int i = 0; i <= 3; i++){   // 寻找我车所在道路在向量中的定位
                if (Topcar.getCarState().getRoadId() == Roads.get(i).getId()){
                    vector_location = i;
                    break;
                }
            }

            /*********************************end1**************************************/

            /******************************start2**************************************/

            if (pass_or_not == 0) {  //下一道路行驶距离为0
                int Topcar_left_dis; //头车至路口的距离
                int Car_thisroad_V;   //本车时速

                Topcar_left_dis = Thisroad.getLength() - 1 - Topcar_Thisroad_loc;
                Car_thisroad_V = Math.min(Thisroad.getMaxSpeed(), Topcar.getMaxSpeed());

                CarState carstate_temp=Topcar.getCarState();
                carstate_temp.setPosition(Math.min(Car_thisroad_V, Topcar_left_dis)+Topcar_Thisroad_loc);
                Topcar.setCarState(carstate_temp);

                return (Math.min(Car_thisroad_V, Topcar_left_dis));  //要么行驶到路口，要么最大时速走一时间单位
            } else {//假如无车且不考虑道路长度,下一道路有理论最大可行驶距离
                int lane_temp=1;

                Road Next_road;
                int Lastcar_Nextroad_loc=-1;
                ArrayList<ArrayList<Car>>  Matrix_Nextroad;

                Direction_me = JudgeDirection(Topcar, AllRoads, AllCross);

                if(Direction_me==0) { //我方直行
                    Next_road = Roads.get(num2[vector_location]);

                    while (lane_temp <= Next_road.getLanes()) {
                        if (Next_road.getStart() != cross.getId()){
                            //道路方向向内
                            if (Next_road.getDirected() == 1){//并且为双向路
                                Matrix_Nextroad = Next_road.getMatrix_E2S();
                                Lastcar_Nextroad_loc = Next_road.get_Lastcar_location_E2S(lane_temp);
                            }
                        } else {//道路方向向外
                            Matrix_Nextroad = Next_road.getMatrix_S2E();
                            Lastcar_Nextroad_loc = Next_road.get_Lastcar_location_S2E(lane_temp);
                        }

                        if (Lastcar_Nextroad_loc == -1) {
                            Topcar.getCarState().setLane(lane_temp-1) ;
                            CarState carstate_temp=Topcar.getCarState();
                            carstate_temp.setPosition(Math.min(Next_road.getLength()-1, pass_or_not-1));
                            Topcar.setCarState(carstate_temp);


                            return (Math.min(Next_road.getLength(), pass_or_not));
                        } else if (Lastcar_Nextroad_loc == 0) {
                            if (lane_temp == Next_road.getLanes()) {
                                CarState carstate_temp=Topcar.getCarState();
                                carstate_temp.setPosition(Thisroad.getLength()-1);
                                Topcar.setCarState(carstate_temp);
                                return 0;
                            }
                            else                                      lane_temp = lane_temp + 1;
                        } else {
                            Topcar.getCarState().setLane(lane_temp-1);
                            CarState carstate_temp=Topcar.getCarState();
                            carstate_temp.setPosition(Math.min(Lastcar_Nextroad_loc, pass_or_not)-1);
                            Topcar.setCarState(carstate_temp);
                            return (Math.min(Lastcar_Nextroad_loc, pass_or_not));
                        }
                    }
                } else if(Direction_me == 1) { //我方左转

                    Next_road = Roads.get(num1[vector_location]);
                    int ondkfvm = lane_temp;

                    while (lane_temp <= Next_road.getLanes()) {
                        if (Next_road.getStart() != cross.getId()){
                            //道路方向向内
                            if (Next_road.getDirected() == 1){//并且为双向路
                                Matrix_Nextroad = Next_road.getMatrix_E2S();
                                Lastcar_Nextroad_loc = Next_road.get_Lastcar_location_E2S(lane_temp);
                            }
                        } else {//道路方向向外
                            Matrix_Nextroad = Next_road.getMatrix_S2E();
                            Lastcar_Nextroad_loc = Next_road.get_Lastcar_location_S2E(lane_temp);
                        }

                        if (Lastcar_Nextroad_loc == -1) {
                            Topcar.getCarState().setLane(lane_temp-1);
                            CarState carstate_temp=Topcar.getCarState();
                            carstate_temp.setPosition(Math.min(Next_road.getLength(), pass_or_not)-1);
                            Topcar.setCarState(carstate_temp);
                            return (Math.min(Next_road.getLength(), pass_or_not));
                        } else if (Lastcar_Nextroad_loc == 0) {
                            if (lane_temp == Next_road.getLanes()) {
                                CarState carstate_temp=Topcar.getCarState();
                                carstate_temp.setPosition(Thisroad.getLength()-1);
                                Topcar.setCarState(carstate_temp);
                                return 0;
                            }
                            else                                      lane_temp = lane_temp + 1;
                        } else {
                            Topcar.getCarState().setLane(lane_temp-1);
                            CarState carstate_temp=Topcar.getCarState();
                            carstate_temp.setPosition(Math.min(Lastcar_Nextroad_loc, pass_or_not)-1);
                            Topcar.setCarState(carstate_temp);
                            return (Math.min(Lastcar_Nextroad_loc, pass_or_not));
                        }
                    }
                } else if(Direction_me == 2) { //我方右转
                    Next_road = Roads.get(num0[vector_location]);

                    while (lane_temp <= Next_road.getLanes()) {
                        if (Next_road.getStart() != cross.getId()){
                            //道路方向向内
                            if (Next_road.getDirected() == 1){//并且为双向路
                                Matrix_Nextroad = Next_road.getMatrix_E2S();
                                Lastcar_Nextroad_loc = Next_road.get_Lastcar_location_E2S(lane_temp);
                            }
                        } else {//道路方向向外
                            Matrix_Nextroad = Next_road.getMatrix_S2E();
                            Lastcar_Nextroad_loc = Next_road.get_Lastcar_location_S2E(lane_temp);
                        }

                        if (Lastcar_Nextroad_loc == -1) {
                            CarState carstate_temp=Topcar.getCarState();
                            carstate_temp.setPosition(Math.min(Next_road.getLength(), pass_or_not)-1);
                            Topcar.setCarState(carstate_temp);
                            Topcar.getCarState().setLane(lane_temp-1);
                            return (Math.min(Next_road.getLength(), pass_or_not));
                        } else if (Lastcar_Nextroad_loc == 0) {
                            if (lane_temp == Next_road.getLanes()) {//当前存在位置可走
                                CarState carstate_temp=Topcar.getCarState();
                                carstate_temp.setPosition(Thisroad.getLength()-1);
                                Topcar.setCarState(carstate_temp);
                                return 0;
                            }
                            else                                      lane_temp = lane_temp + 1;
                        } else {
                            Topcar.getCarState().setLane(lane_temp-1);
                            CarState carstate_temp=Topcar.getCarState();
                            carstate_temp.setPosition(Math.min(Lastcar_Nextroad_loc, pass_or_not)-1);
                            Topcar.setCarState(carstate_temp);
                            return (Math.min(Lastcar_Nextroad_loc, pass_or_not));
                        }
                    }
                }
            }
            /********************end2**************************************/
        }
        /********************end0**************************************/

        System.out.println("GO_distance 返回值为0   异常情况");
        return 0;//？？？？？
    }

    /**
     * 非头车的行驶距离
     * 前面的Cross,本车车道lane，本道路Thisroad,本车Untopcar
     * */
    public static int Untopcar_Go_distance(Cross cross, int lane,Road Thisroad, Car Untopcar) {
        ArrayList<ArrayList<Car>>  Matrix_Thisroad;
        int front_car_loc=-1;

        if (Thisroad.getStart() == cross.getId()) {
            if (Thisroad.getDirected() == 1) {
                Matrix_Thisroad = Thisroad.getMatrix_E2S();
            } else{
                return -2;//忽略掉的情况
            }
        } else {
            Matrix_Thisroad = Thisroad.getMatrix_S2E();
        }

        for (int j = Untopcar.getCarState().getPosition()+1; j < Thisroad.getLength(); j++) {
            if ((Matrix_Thisroad.get(lane).get(j)).getId() != -1) {
                front_car_loc = j;
                break;
            }
        }

        if (front_car_loc == -1)
            return -1;   //本车是头车

        int car2car_distance = front_car_loc - Untopcar.getCarState().getPosition()-1;

        int Car_thisroad_V = Math.min(Thisroad.getMaxSpeed(), Untopcar.getMaxSpeed());

        Untopcar.getCarState().setPosition(Math.min(car2car_distance, Car_thisroad_V)+ Untopcar.getCarState().getPosition());

        return (Math.min(car2car_distance, Car_thisroad_V));
    }

}
