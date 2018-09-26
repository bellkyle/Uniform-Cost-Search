import java.util.*;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;

class StateComparator implements Comparator<GameState>{
    public int compare(GameState a, GameState b){
        float xMin = b.x - 5;
        float xMax = b.x + 5;
        float yMin = b.y - 5;
        float yMax = b.y + 5;
        if(a.x  < xMin)
            return -1;
        else if(a.x > xMax)
            return 1;
        else if(a.y < yMin)
            return -1;
        else if(a.y > yMax)
            return 1;
        return 0;
    }
}

class QueueComparator implements Comparator<GameState>{
    public int compare(GameState a, GameState b){
        if(a.cost < b.cost)
            return -1;
        else if(a.cost > b.cost)
            return 1;
        return 0;
    }
}

class aStarQueueComparator implements Comparator<GameState>{
    public int compare(GameState a, GameState b){
        if((a.cost + a.heuristic) < (b.cost + b.heuristic))
            return -1;
        else if((a.cost + a.heuristic) > (b.cost + b.heuristic))
            return 1;
        return 0;
    }
}

class Agent {

    boolean debug = false;
    Stack<GameState> stack = new Stack<GameState>();
    GameState end = new GameState();
    ArrayList<GameState> list = new ArrayList<GameState>();
    PriorityQueue<GameState> frontier = new PriorityQueue<GameState>();

	void drawPlan(Graphics g, Model m) {
		g.setColor(Color.red);
		g.drawLine((int) m.getX(), (int) m.getY(), (int) m.getDestinationX(), (int) m.getDestinationY());
		int x = (int) m.getDestinationX();
		int y = (int) m.getDestinationY();
        for(int i = list.size()-1; i >= 0; i--) {
            g.drawLine(x, y, (int) list.get(i).x, (int) list.get(i).y);
            x = (int) list.get(i).x;
            y = (int) list.get(i).y;
        }
        GameState[] front = new GameState[frontier.size()];
        frontier.toArray(front);
        g.setColor(Color.green);
        for(int i = 0; i < front.length; i++){
            g.fillOval((int) front[i].x, (int) front[i].y,10,10);
        }
	}

	void update(Model m)
	{
		Controller c = m.getController();
        while(true) {

            MouseEvent e = c.nextMouseEvent();
            if (e == null)
                break;
            stack.removeAllElements();
            list.clear();
            frontier.clear();
            if (e.getButton() == e.BUTTON1) {
                GameState goal = new GameState();
                goal.x = e.getX();
                goal.y = e.getY();
                end = UCS(m, goal);
                if (end == null) {
                    System.out.println("Could not find a path");
                } else {
                    while (end.parent != null) {
                        stack.push(end);
                        list.add(end);
                        end = end.parent;
                    }
                    if (debug)
                        System.out.println(stack.size());
                }
            }
            if (e.getButton() == e.BUTTON3) {
                GameState goal = new GameState();
                goal.x = e.getX();
                goal.y = e.getY();
                end = aStar(m, goal);
                if (end == null) {
                    System.out.println("Could not find a path");
                } else {
                    while (end.parent != null) {
                        stack.push(end);
                        list.add(end);
                        end = end.parent;
                    }
                }
            }
        }
        if(!stack.isEmpty() && (m.getX() == m.getDestinationX()) && (m.getY() == m.getDestinationY())) {
            GameState stackState = stack.pop();
            list.remove(list.size()-1);
            m.setDestination(stackState.x, stackState.y);
        }

	}

	public GameState UCS(Model m, GameState goal){
	    GameState start = new GameState();
        PriorityQueue<GameState> queue = new PriorityQueue<GameState>(11, new QueueComparator());
        TreeSet<GameState> set = new TreeSet<GameState>(new StateComparator());
        start.cost = 0.0;
        start.parent = null;
        start.x = m.getX();
        start.y = m.getY();
        set.add(start);
        queue.add(start);
        while(!queue.isEmpty()){
            GameState state = queue.poll();
            if(isGoal(state,goal)){
                frontier = queue;
                return state;
            }
            for(int i = 0; i < 8; i++){
                GameState child = transition(state, i);
                float acost = 0;
                try {
                    acost = action(state, child, m);
                    if (set.contains(child)) {
                        GameState oldchild = set.floor(child);
                        if (state.cost + acost < oldchild.cost) {
                            oldchild.cost = state.cost + acost;
                            oldchild.parent = state;
                        }
                    } else {
                        child.cost = state.cost + acost;
                        child.parent = state;
                        queue.add(child);
                        set.add(child);
                    }
                }catch (ArrayIndexOutOfBoundsException e) {

                }
            }
        }

        return null;
    }

    public GameState aStar(Model m, GameState goal){
        GameState start = new GameState();
        PriorityQueue<GameState> queue = new PriorityQueue<GameState>(11, new aStarQueueComparator());
        TreeSet<GameState> set = new TreeSet<GameState>(new StateComparator());
        float lowestCost = lowestCost(m);
        start.parent = null;
        start.x = m.getX();
        start.y = m.getY();
        start.cost = 0.0;
        start.heuristic = heuristic(m, lowestCost, start, goal);
        set.add(start);
        queue.add(start);
        while(!queue.isEmpty()){
            GameState state = queue.poll();
            if(isGoal(state,goal)){
                frontier = queue;
                return state;
            }
            for(int i = 0; i < 8; i++){
                GameState child = transition(state, i);
                float acost = 0;
                try {
                    acost = action(state, child, m);
                    float heuristic = heuristic(m, lowestCost, child, goal);
                    if (set.contains(child)) {
                        GameState oldchild = set.floor(child);
                        if ((state.cost + acost)  < (oldchild.cost)) {
                            //System.out.println("CONTAINS");
                            oldchild.cost = state.cost + acost;
                            oldchild.parent = state;
                            oldchild.heuristic = heuristic(m, lowestCost, child, goal);
                        }
                    } else {
                        child.cost = state.cost + acost;
                        child.parent = state;
                        child.heuristic = heuristic(m, lowestCost, child, goal);
                        queue.add(child);
                        set.add(child);
                    }
                }catch (ArrayIndexOutOfBoundsException e) {

                }
            }
        }

        return null;
    }

    public boolean isGoal(GameState state, GameState goal){
	    float xMin = goal.x - 6;
	    float xMax = goal.x + 6;
	    float yMin = goal.y - 6;
	    float yMax = goal.y + 6;
	    if((state.x > xMin) && (state.x < xMax) && (state.y > yMin) && (state.y < yMax) )
	        return true;
	    else
	        return false;
    }

    public GameState transition(GameState state, int i){
	    GameState newState = new GameState();
	    newState.x = state.x;
	    newState.y = state.y;
        if(i == 0){
            newState.x = state.x + 10;
        }
        else if(i == 1){
            newState.y = state.y + 10;
        }
        else if(i == 2){
            newState.x = state.x - 10;
        }
        else if(i == 3){
            newState.y = state.y - 10;
        }
        else if(i == 4){
            newState.x = state.x + 10;
            newState.y = state.y + 10;
        }
        else if(i == 5){
            newState.x = state.x - 10;
            newState.y = state.y - 10;
        }
        else if(i == 6){
            newState.x = state.x + 10;
            newState.y = state.y - 10;
        }
        else if(i == 7){
            newState.x = state.x - 10;
            newState.y = state.y + 10;
        }
        return newState;
    }

    public float action(GameState state, GameState child, Model m){

	    float speed = m.getTravelSpeed(state.x, state.y);
        float dist;
        if(state.x == child.x || state.y == state.x)
            dist = 1;
        else
            dist = (float)Math.sqrt(2);
        return (dist/speed);
    }

    public float lowestCost(Model m){
	    float lowest = (1/m.getTravelSpeed(0,0));
	    for(int x =  0; x < m.XMAX; x += 10){
	        for(int y = 0; y < m.YMAX; y += 10){
	            float temp = (1/m.getTravelSpeed(x,y));
	            if (temp < lowest){
	                lowest = temp;
                }
            }

        }
        return lowest;
    }

    public float heuristic(Model m, float cost, GameState state, GameState goal){
        float distance = (float) (Math.sqrt((state.x - goal.x) * (state.x - goal.x) + (state.y - goal.y) * (state.y - goal.y)))/10;
        return distance * cost;
    }

	public static void main(String[] args) throws Exception
	{
		Controller.playGame();
	}
}
