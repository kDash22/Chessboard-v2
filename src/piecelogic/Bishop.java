package piecelogic;

import chessboard.ChessboardLogic;

public class Bishop extends Piece{

    public static final int PIECE_VALUE = 3;

    public Bishop(boolean isWhite) {
        super(PieceType.BISHOP, isWhite);
    }

    @Override
    public void moveCheck(ChessboardLogic chessboardLogic, int fromRow, int fromCol) {
        moves.clear();//clear the list to remove earlier move

        if (isWhite() != chessboardLogic.isWhiteToMove()){
            validMoveSet = new int[0];
            return;
        }

        Piece[][] refBoard = chessboardLogic.getChessboard();

        //a bishop can move in 4 directions
        int[][] directions = {{1,1},{-1,1},{-1,-1},{1,-1}};

        for (int[] direction : directions){
            /*
                    [ flags ][   to   ][  from  ]
                    bits12+   bits6-11    bits0-5
                */
            /*
                    bit  12     → capture
                    bit  13    → double pawn push
                    bit  14     → en passant
                    bit  15    → castling

                    bits 16 (2 bits total)   → promotion piece type
                */

            int toRow = fromRow + direction[0];
            int toCol = fromCol + direction[1];

            while(ChessboardLogic.isIndexWithinBounds(toRow,toCol)){

                int move = fromRow * 8 + fromCol;
                move |= (toRow * 8 + toCol) << 6;

                if (refBoard[toRow][toCol] == null){
                    moves.add(move);

                } else if (refBoard[toRow][toCol].isWhite() != isWhite() && refBoard[toRow][toCol].isKing()) {
                    break;
                } else if(refBoard[toRow][toCol].isWhite() != isWhite()) {
                    move |= 1 << 12;
                    moves.add(move);
                    break;
                } else {
                    break;
                }

                toRow += direction[0];
                toCol += direction[1];
            }
        }

        filterIllegalMoves(chessboardLogic,moves);

        int validMoveCount = moves.size();
        validMoveSet = new int[validMoveCount];

        for (int i = 0; i < validMoveCount; i++){
            validMoveSet[i] = moves.get(i);
        }
    }


    @Override
    public boolean attacksSquare(Piece[][] refBoard,int pieceRow, int pieceCol, int targetRow, int targetCol) {

        if (Math.abs(targetRow - pieceRow) != Math.abs(targetCol - pieceCol))
            return false;

        int rowDir = (targetRow > pieceRow) ? 1 : -1;
        int colDir = (targetCol > pieceCol) ? 1 : -1;

        int r = pieceRow+rowDir;
        int c = pieceCol+colDir;

        while (r != targetRow && c != targetCol){

            if (!ChessboardLogic.isIndexWithinBounds(r, c)) return false;

            if (refBoard[r][c] != null) return false;

            r += rowDir;
            c += colDir;
        }

        return true;
    }

    public String toString(){
        String tag = isWhite() ? "White Bishop" : "Black Bishop";
        //tag += getFile()+""+getChessRow();
        return tag;
    }
}
