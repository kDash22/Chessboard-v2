package engine;

public class MovePositionState {
    //wrapper class for MoveState and Position

    protected MoveState moveState;
    protected Position position;
    protected int seenCount;

    public MovePositionState(MoveState moveState, Position position,int seenCount){
        this.moveState = moveState;
        this.position = position;
        this.seenCount = seenCount;
    }

}
