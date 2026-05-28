package piecelogic;

import chessboard.ChessboardLogic;
import engine.MoveState;

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

            if ( chessboardLogic.isKingInCheck(isWhite(), chessboardLogic.getChessboard()) ){
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

    protected void applyCheckFlag(Piece[][] refBoard,int[] validMoveSet){

        int[] kingPos = ChessboardLogic.getKingPos(!isWhite(),refBoard);

        for (int i = 0; i < validMoveSet.length; i++){

            int[] toSquare = ChessboardLogic.getToSquare(validMoveSet[i]);

            if (isPawn()){ //if a pawn is promoted it cannot be checked if the new piece attacks the king using the
                            //pawn attack check logic

                int promotion = (validMoveSet[i] >> 16) & 7;

                if (promotion > 0){

                    boolean attacked = false;

                    switch (promotion){
                        case 1 -> attacked = new Knight(isWhite()).attacksSquare(refBoard,toSquare[0],toSquare[1],kingPos[0],kingPos[1]);
                        case 2 -> attacked = new Bishop(isWhite()).attacksSquare(refBoard,toSquare[0],toSquare[1],kingPos[0],kingPos[1]);
                        case 3 -> attacked = new Rook(isWhite()).attacksSquare(refBoard,toSquare[0],toSquare[1],kingPos[0],kingPos[1]);
                        case 4 -> attacked = new Queen(isWhite()).attacksSquare(refBoard,toSquare[0],toSquare[1],kingPos[0],kingPos[1]);
                        default -> throw new IllegalArgumentException("Error at apply check flag at pawn promotion section !");
                    }

                    if (attacked){
                        validMoveSet[i] |= 1 << 24; //bit 24 → check
                    }
                    continue;
                }
            }

            if ( attacksSquare(refBoard,toSquare[0],toSquare[1],kingPos[0],kingPos[1]) ){
                validMoveSet[i] |= 1 << 24; //bit 24 → check
            }

        }
    }
}
