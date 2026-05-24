package piecelogic;

import chessboard.ChessboardLogic;

public class Knight extends Piece {

    public static final int PIECE_VALUE = 3;

    public Knight(boolean isWhite) {
        super(PieceType.KNIGHT, isWhite);
    }

    @Override
    public void moveCheck(ChessboardLogic chessboardLogic, int fromRow, int fromCol) {
        moves.clear();//clear the List to remove earlier moves

        if (isWhite() != chessboardLogic.isWhiteToMove()){
            validMoveSet = new int[0];
            return;
        }

        Piece[][] refBoard = chessboardLogic.getChessboard();

        //a knight has 8 possible moves or directions
        int[][] directions = {{2,1},{1,2},{-1,2},{-2,1},{-2,-1},{-1,-2},{1,-2},{2,-1}};

        for (int i = 0; i < 8; i++){
            /*
                    [ flags ][   to   ][  from  ]
                    bits12+   bits6-11    bits0-5

                    bit  12 → capture
                    bit  13 → double pawn push
                    bit  14 → en passant
                    bit  15 → castling

                    bit 16 (3 bits total) → promotion piece type
                */

            int toRow = fromRow + directions[i][0];
            int toCol = fromCol + directions[i][1];

            int move = fromRow * 8 + fromCol;

            move |= (toRow * 8 + toCol) << 6;

            if( ChessboardLogic.isIndexWithinBounds(toRow,toCol) ){

                if (refBoard[toRow][toCol] == null){
                    moves.add(move);

                } else if(refBoard[toRow][toCol].isWhite() != isWhite() && !refBoard[toRow][toCol].isKing()) {
                    move |= 1 << 12;
                    moves.add(move);

                }

            }

        }

        filterIllegalMoves(chessboardLogic,moves);

        int validMoveCount = moves.size();
        validMoveSet = new int[validMoveCount];

        for (int i = 0; i < validMoveCount; i++){
            validMoveSet[i] = moves.get(i);
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
