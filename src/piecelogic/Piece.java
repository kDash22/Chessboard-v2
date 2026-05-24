package piecelogic;

import chessboard.ChessboardLogic;

import static global.Global.shallowCopyBoard;

import java.util.ArrayList;
import java.util.List;

public abstract class Piece {

    List<int[]> moveSet = new ArrayList<>();
    protected int[][] validMoveSet;

    private boolean isWhite;
    private PieceType pieceType;

    protected Piece(PieceType pieceType, boolean isWhite) {

        this.isWhite = isWhite;
        this.pieceType = pieceType;
        

    }

    public List<int[]> getPseudoLegalMoves() {
        return moveSet;
    }

    public int[][] getValidMoveSet() {
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

    public void filterIllegalMoves(ChessboardLogic chessboardLogic, List<int[]> moveSet, int fromRow, int fromCol){

        Piece[][] refBoard;

        for (int i = moveSet.size()-1 ; i >= 0; i--){

            refBoard = shallowCopyBoard(chessboardLogic.getChessboard());

            int[] square = moveSet.get(i);

            refBoard[ square[0] ][ square[1] ] = refBoard[fromRow][fromCol];
            refBoard[fromRow][fromCol] = null;

            if (this instanceof King && Math.abs(fromCol - square[1]) == 2){
                //handle castling move for king

                int rookFromCol = (square[1] == 6) ? 7 : 0;
                int rookToCol = (square[1] == 6) ? 5 : 3;

                refBoard[square[0]][rookToCol] = refBoard[square[0]][rookFromCol];
                refBoard[square[0]][rookFromCol] = null;
            }


            if ( chessboardLogic.isKingInCheck(isWhite(),refBoard) ){
                moveSet.remove(i);
            }

        }

    }

    public abstract boolean attacksSquare(Piece[][] refBoard,int pieceRow, int pieceCol, int targetRow, int targetCol);

    public abstract void moveCheck(ChessboardLogic chessboardLogic, int fromRow, int fromCol);
}
