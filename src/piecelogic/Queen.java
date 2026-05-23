package piecelogic;

import chessboard.ChessboardLogic;

public class Queen extends Piece{

    public static final int PIECE_VALUE = 9;

    public Queen(boolean isWhite) {
        super(PieceType.QUEEN, isWhite);
    }

    @Override
    public void moveCheck(ChessboardLogic chessboardLogic, int fromRow, int fromCol) {
        moves.clear();//clear the list to remove earlier move

        if (isWhite() != chessboardLogic.isWhiteToMove()){
            validMoveSet = new int[0];
            return;
        }

        Piece[][] refBoard = chessboardLogic.getChessboard();

        //a Queen can move in 8 directions
        int[][] directions = {{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1},{0,-1},{1,-1}};

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

            while( ChessboardLogic.isIndexWithinBounds(toRow,toCol) ){

                int move = fromRow * 8 + fromCol;
                move |= (toRow * 8 + toCol) << 6;

                if (refBoard[toRow][toCol] == null){
                    moves.add(move);

                } else if (refBoard[toRow][toCol].isWhite() != isWhite() && refBoard[toRow][toCol].isKing()) {
                    break;
                } else if(refBoard[toRow][toCol].isWhite() != isWhite()) {
                    move |= 1 << 12;
                    moves.add(move);
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
    public boolean attacksSquare(Piece[][] refBoard, int pieceRow, int pieceCol, int targetRow, int targetCol) {

        Bishop bishopLogic = new Bishop(isWhite());

        Rook rookLogic = new Rook(isWhite());

        return bishopLogic.attacksSquare(refBoard, pieceRow, pieceCol, targetRow, targetCol)
                || rookLogic.attacksSquare(refBoard, pieceRow, pieceCol, targetRow, targetCol);
    }

    public String toString(){
        String tag = isWhite() ? "White Queen" : "Black Queen";
        //tag += getFile()+""+getChessRow();
        return tag;
    }
}
