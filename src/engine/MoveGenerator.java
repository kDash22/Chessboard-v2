package engine;

import chessboard.ChessboardLogic;
import piecelogic.King;
import piecelogic.Pawn;
import piecelogic.Piece;
import piecelogic.Rook;

import static chessboard.ChessboardLogic.*;

public class MoveGenerator {

    boolean originalTurnState;

    public MoveList generateMoves(ChessboardLogic chessboardLogic) {
        int[] moves = new int[256]; //maximum moves for any given turn is estimated to be about 218
        Piece[][] refBoard = chessboardLogic.getChessboard();

        int moveCount = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Piece p = refBoard[row][col];

                if (p == null) continue;

                if (p.isWhite() != chessboardLogic.isWhiteToMove()) continue;

                p.moveCheck(chessboardLogic, row, col);
                int[] pValidMoveSet = p.getValidMoveSet();

                for (int i = 0; i < pValidMoveSet.length; i++) {
                    /*
                        [ flags ][   to   ][  from  ]
                        bits12+   bits6-11    bits0-5
                     */
                    moves[++moveCount] = pValidMoveSet[i]; // add flags if needed
                }

            }
        }
        return new MoveList(moves, moveCount);
    }

    public void doMove(ChessboardLogic chessboardLogic, int fromRow, int fromCol, int toRow, int toCol) {

        Piece[][] refBoard = chessboardLogic.getChessboard();

        Piece movingPiece = refBoard[fromCol][fromRow];

        // Castling Execution Logic
        if (movingPiece.isKing() && Math.abs(fromCol - toCol) == 2) {
            int rookOriginalCol = (toCol == 6) ? 7 : 0;
            int rookTargetCol = (toCol == 6) ? 5 : 3;

            Piece rook = refBoard[fromRow][rookOriginalCol];

            if (rook.isRook()) {

                ((Rook) rook).setHasMoved(true);
                refBoard[fromRow][rookTargetCol] = rook;
                refBoard[fromRow][rookOriginalCol] = null;
            }
        }

        //EnPassant Execution Logic
        if (chessboardLogic.getImmediateAction() && movingPiece.isPawn()) {

            if (Math.abs(fromCol - toCol) == 1 && refBoard[toRow][toCol] == null) {
                int dir = movingPiece.isWhite() ? 1 : -1;
                refBoard[toRow + dir][toCol] = null;
            }
            chessboardLogic.setImmediateAction(false);

        }

        Pawn.clearAllEnPassantFlags(chessboardLogic);//resetting

        //en passant available setting logic, must be after en passant execution logic
        if (movingPiece.isPawn() && Math.abs(fromRow - toRow) == 2) {
            Piece pieceToTheLeft = null, pieceToTheRight = null;

            if (isIndexWithinBounds(toRow, toCol - 1))
                pieceToTheLeft = refBoard[toRow][toCol - 1];

            if (isIndexWithinBounds(toRow, toCol + 1))
                pieceToTheRight = refBoard[toRow][toCol + 1];


            if ((pieceToTheLeft != null && pieceToTheLeft.isPawn() && pieceToTheLeft.isWhite() != chessboardLogic.isWhiteToMove())
                    || (pieceToTheRight != null && pieceToTheRight.isPawn() && pieceToTheRight.isWhite() != chessboardLogic.isWhiteToMove())){
                ((Pawn) movingPiece).setEnPassantVulnerable(true);
                chessboardLogic.setImmediateAction(true);
            }


        }

        refBoard[toRow][toCol] = movingPiece;
        refBoard[fromRow][fromCol] = null;

        if(movingPiece.isRook() && !((Rook) movingPiece).getHasMoved()){
            ((Rook) movingPiece).setHasMoved(true);
        }

        if(movingPiece.isKing()){

            if (!((King) movingPiece).getHasMoved()) {
                ((King) movingPiece).setHasMoved(true);
            }

        }

        if (movingPiece.isPawn()) {

            int endRow = movingPiece.isWhite() ? 0 : 7;

            char file = colToFile(toCol);
            int rank = rowToChessRow(endRow);

            if (movingPiece.isWhite() && toRow == endRow) {
               // chessboardLogic.insertPieceToBoard(((Pawn) movingPiece).promote(chessboardLogic), ,file, rank);
            }
        }

        chessboardLogic.setWhiteToMove(!chessboardLogic.isWhiteToMove());

        chessboardLogic.checkGameOver();

    }

    public void undoMove() {

    }
        
        




    public int applyFlags(Piece[][] refBoard, Piece p,int fromRow, int fromCol,int toRow, int toCol){

        /*
            bit  0     → capture
            bit  1     → double pawn push
            bit  2     → en passant
            bit  3     → castling

            bits 4-5   → promotion piece type
         */
        int flag = 0;
        if (refBoard[toRow][toCol] != null){
            //capture
            flag |= 1;
        }

        if (p.isKing() && Math.abs(fromCol - toCol) == 2){
            //castle
            flag |= 1 << 3;
        }

        if (p.isPawn()){

            if (Math.abs(fromRow - toRow) == 2){
                //en passant vulnerable
                flag |= 1 << 1;

            }

            if (Math.abs(fromCol - toCol) == 1 && refBoard[toRow][toCol] == null){
                //en passant happened
                flag |= 1 << 2;

            }

            int endRow = p.isWhite() ? 0 : 7;
            if (endRow == toRow){
                //promotion
                //flag

            }

        }








        return 0 ;

    }


}
