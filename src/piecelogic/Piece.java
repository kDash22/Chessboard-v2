package piecelogic;

import chessboard.ChessboardLogic;
import engine.MoveState;

import static chessboard.ChessboardLogic.isKingInCheck;
import static engine.MoveGenerator.doMove;
import static engine.MoveGenerator.undoMove;

import java.util.ArrayList;
import java.util.List;

public abstract class Piece {

    protected List<Integer> moves = new ArrayList<>();
    protected int[] validMoveSet;

    private boolean isWhite;
    private PieceType pieceType;

    protected Piece(PieceType pieceType, boolean isWhite) {

        this.isWhite = isWhite;
        this.pieceType = pieceType;
        

    }

    public List<Integer> getPseudoLegalMoves() {
        return moves;
    }

    public int[] getValidMoveSet() {
        return validMoveSet;
    }

    public PieceType getPieceType(){
        return pieceType;
    }

    public boolean isWhite(){
        return isWhite;
    }

    public boolean isKing(){
        return getPieceType() == PieceType.KING;
    }

    public boolean isPawn(){
        return getPieceType() == PieceType.PAWN;
    }

    public boolean isRook(){
        return getPieceType() == PieceType.ROOK;
    }

    public void filterIllegalMoves(ChessboardLogic chessboardLogic, List<Integer> moves){

        for (int i = moves.size() - 1; i >= 0; i--){

            int encryptedMove = moves.get(i);
            MoveState moveState = doMove(chessboardLogic,encryptedMove);

            if ( ChessboardLogic.isKingInCheck(isWhite(), chessboardLogic.getChessboard()) ){
                undoMove(chessboardLogic,encryptedMove,moveState);
                moves.remove(i);
                continue;
            }
            undoMove(chessboardLogic,encryptedMove,moveState);

        }

    }

    public int winningCaptureValue(Piece piece, int ownValue){

        int value = 0;

        switch (piece.getPieceType()){
            case QUEEN -> value = 9;
            case ROOK -> value = 5;
            case BISHOP, KNIGHT -> value = 3;
            case PAWN -> value = 1;
        }

        return 8+value-ownValue;
    }

    public abstract boolean attacksSquare(Piece[][] refBoard,int pieceRow, int pieceCol, int targetRow, int targetCol);

    public abstract void moveCheck(ChessboardLogic chessboardLogic, int fromRow, int fromCol);

    protected void applyCheckFlag(ChessboardLogic chessboardLogic,int[] validMoveSet){

        Piece[][] refBoard = chessboardLogic.getChessboard();

        for (int i = 0; i < validMoveSet.length; i++){

            int move = validMoveSet[i];

            MoveState moveState = doMove(chessboardLogic,move);

            if ( isKingInCheck(!isWhite(), refBoard) ){
                validMoveSet[i] |= 1 << 24; //bit 24 → check
            }

            undoMove(chessboardLogic,move,moveState);

        }
    }
}
