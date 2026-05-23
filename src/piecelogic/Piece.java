package piecelogic;

import chessboard.ChessboardLogic;

import static global.Global.shallowCopyBoard;
import static chessboard.ChessboardLogic.decryptMove;

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

    public boolean isPawn(){
        return getPieceType() == PieceType.PAWN;
    }

    public boolean isRook(){
        return getPieceType() == PieceType.ROOK;
    }

    public void filterIllegalMoves(ChessboardLogic chessboardLogic, int[] moves){

        Piece[][] refBoard;

        for (int i = moves.length - 1; i >= 0; i--){

            int[] move = decryptMove(moves[i]);// fromRow, fromCol, toRow, toCol, enPassant, castle, promotion

            int fromRow = move[0];
            int fromCol = move[1];

            int toRow = move[2];
            int toCol = move[3];

            boolean enPassant = move[4] == 1 ;
            boolean castle = move[5] == 1;
            boolean promoted = move[6] > 0;
            

            refBoard = shallowCopyBoard(chessboardLogic.getChessboard());

            refBoard[ square[0] ][ square[1] ] = refBoard[fromRow][fromCol];
            refBoard[fromRow][fromCol] = null;

            if (this.isKing() && Math.abs(fromCol - square[1]) == 2){
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
