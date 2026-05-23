package piecelogic;

import chessboard.ChessboardLogic;

public class Rook extends Piece{

    public static final int PIECE_VALUE = 5;

    private boolean hasMoved = false;

    public Rook(boolean isWhite) {
        super(PieceType.ROOK, isWhite);
    }

    @Override
    public void moveCheck(ChessboardLogic chessboardLogic, int fromRow, int fromCol) {
        moves.clear();//clear the list to remove earlier move

        if (isWhite() != chessboardLogic.isWhiteToMove()){
            validMoveSet = new int[0];
            return;
        }

        Piece[][] refBoard = chessboardLogic.getChessboard();

        //a Rook can move in 4 directions
        int[][] directions = {{1,0},{0,1},{-1,0},{0,-1}};

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
    public boolean attacksSquare(Piece[][] refBoard, int pieceRow, int pieceCol, int targetRow, int targetCol) {

        if (targetRow != pieceRow && targetCol != pieceCol)
            return false;

        int rowDir = Integer.compare(targetRow,pieceRow);
        int colDir = Integer.compare(targetCol,pieceCol);

         int r = pieceRow + rowDir;
         int c = pieceCol + colDir;

        while (targetCol != c || targetRow != r){

            if (refBoard[r][c] != null){
                return false;

            }

            r += rowDir;
            c += colDir;
        }

        return true;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public boolean getHasMoved(){
        return hasMoved;
    }

    public String toString(){
        String tag = isWhite() ? "White Rook" : "Black Rook";
        //tag += getFile()+""+getChessRow();
        return tag;
    }
}
