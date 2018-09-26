public class GameState {
    public double cost;
    public double heuristic;
    GameState parent;
    public float x;
    public float y;
    GameState(){this.parent = null;}
    GameState(double cost, GameState parent){
        this.cost = cost;
        this.parent = parent;
    }
}
