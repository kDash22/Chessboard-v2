package piecelogic;

import chessboard.ChessboardLogic;

public class Queen extends Piece{

    public static final int PIECE_VALUE = 9;

    public Queen(boolean isWhite) {
        super(PieceType.QUEEN, isWhite);
    }

    @Override
    public void moveCheck(ChessboardLogic chessboardLogic, int fromRow, int fromCol) {
        moveSet.clear();//clear the list to remove earlier move

        if (isWhite() != chessboardLogic.isWhiteToMove()){
            validMoveSet = new int[0][2];
            return;
        }

        Piece[][] refBoard = chessboardLogic.getChessboard();

        //a Queen can move in 8 directions
        int[][] directions = {{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1},{0,-1},{1,-1}};

        for (int[] direction : directions){
            int toRow = fromRow + direction[0];
            int toCol = fromCol + direction[1];

            while( ChessboardLogic.isIndexWithinBounds(toRow,toCol) ){

                if (refBoard[toRow][toCol] == null){
                    moveSet.add(new int[]{toRow, toCol});

                } else if (refBoard[toRow][toCol].isWhite() != isWhite() && refBoard[toRow][toCol].isKing()) {
                    break;
                } else if(refBoard[toRow][toCol].isWhite() != isWhite()) {
                    moveSet.add(new int[]{toRow, toCol});
                    break;
                } else {
                    break;
                }

                toRow += direction[0];
                toCol += direction[1];
            }
        }

        filterIllegalMoves(chessboardLogic,moveSet, fromRow, fromCol);

        int validMoveCount = moveSet.size();
        validMoveSet = new int[validMoveCount][2];

        for (int i = 0; i < validMoveCount; i++){
            validMoveSet[i] = moveSet.get(i);
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
