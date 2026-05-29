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

                    bit  12 → capture
                    bit  13 → double pawn push
                    bit  14 → en passant
                    bit  15 → castling

                    bit 16 (3 bits total) → promotion piece type
                    bit 19 (5 bits total) → Winning capture
                    bit 24 → check
                    bit 25 (3 bits total) → piece

                    1 = pawn
                    2 = knight
                    3 = Bishop
                    4 = Rook
                    5 = Queen
                    6 = King

                    bit 28 → color (white = 1, black = 0)

                */

            int toRow = fromRow + direction[0];
            int toCol = fromCol + direction[1];

            while( ChessboardLogic.isIndexWithinBounds(toRow,toCol) ){

                int move = fromRow * 8 + fromCol;
                move |= (toRow * 8 + toCol) << 6;

                if (refBoard[toRow][toCol] == null){
                    moves.add(move);

                } else if(refBoard[toRow][toCol].isWhite() != isWhite() && !refBoard[toRow][toCol].isKing()) {
                    move |= 1 << 12;
                    move |= winningCaptureValue(refBoard[toRow][toCol],PIECE_VALUE) << 19;//winning capture value
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
            int move = moves.get(i) | (5 << 25);//move made by Queen
            move |= isWhite() ? 1 << 28 : 0;//piece colour
            validMoveSet[i] = move;
        }

        applyCheckFlag(chessboardLogic,validMoveSet);

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
