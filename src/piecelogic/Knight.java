package piecelogic;

import chessboard.ChessboardLogic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Knight extends Piece {

    public static final int PIECE_VALUE = 3;

    public Knight(boolean isWhite) {
        super(PieceType.KNIGHT, isWhite);
    }

    @Override
    public void moveCheck(ChessboardLogic chessboardLogic, int fromRow, int fromCol) {
        moveSet.clear();//clear the list to remove earlier move

        List<Integer> moves = new ArrayList<>();

        if (isWhite() != chessboardLogic.isWhiteToMove()){
            validMoveSet = new int[0][2];
            return;
        }

        Piece[][] refBoard = chessboardLogic.getChessboard();

        //a knight has 8 possible moves or directions
        int[][] directions = {{2,1},{1,2},{-1,2},{-2,1},{-2,-1},{-1,-2},{1,-2},{2,-1}};

        for (int i = 0; i < 8; i++){
            /*
                    [ flags ][   to   ][  from  ]
                    bits12+   bits6-11    bits0-5
                */
            /*
                    bit  0     → capture
                    bit  1     → double pawn push
                    bit  2     → en passant
                    bit  3     → castling

                    bits 4-5   → promotion piece type
                */

            int toRow = fromRow + directions[i][0];
            int toCol = fromCol + directions[i][1];

            int move = fromRow * 8 + fromCol;
            int flags = 0 ;

            move |= (toRow * 8 + toCol) << 6;

            if( ChessboardLogic.isIndexWithinBounds(toRow,toCol) ){

                if (refBoard[toRow][toCol] == null){
                    moveSet.add(new int[]{toRow, toCol});
                    moves.add(move);

                } else if (refBoard[toRow][toCol].isWhite() != isWhite() && refBoard[toRow][toCol].isKing()) {
                    break;

                } else if(refBoard[toRow][toCol].isWhite() != isWhite()) {
                    moveSet.add(new int[]{toRow, toCol});
                    move |= 1 << 12;
                    moves.add(move);


                }

            }

        }

        filterIllegalMoves(chessboardLogic,moveSet, fromRow, fromCol);

        int validMoveCount = moveSet.size();
        validMoveSet = new int[validMoveCount][2];

        for (int i = 0; i < validMoveCount; i++){
            validMoveSet[i] = moveSet.get(i);
        }

    }

    public String toString(){
        String tag = isWhite() ? "White Knight" : "Black Knight";
        //tag += getFile()+""+getChessRow();
        return tag;
    }

    @Override
    public boolean attacksSquare(Piece[][] refBoard, int pieceRow, int pieceCol, int targetRow, int targetCol) {

        int rowDiff = Math.abs(targetRow - pieceRow);
        int colDiff = Math.abs(targetCol - pieceCol);

        return ((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2));
    }
}
