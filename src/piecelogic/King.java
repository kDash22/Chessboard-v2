package piecelogic;

import chessboard.ChessboardLogic;

public class King extends Piece{

    private boolean hasMoved = false;

    public King(boolean isWhite) {
        super(PieceType.KING, isWhite);
    }

    @Override
    public void moveCheck(ChessboardLogic chessboardLogic, int fromRow, int fromCol) {
        moveSet.clear();//clear the list to remove earlier move

        if (isWhite() != chessboardLogic.isWhiteToMove()){
            validMoveSet = new int[0][2];
            return;
        }

        Piece[][] refBoard = chessboardLogic.getChessboard();

        //a king has 8 general moves
        int[][] directions = {{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1},{0,-1},{1,-1}};

        for (int[] direction : directions){

            int toRow = fromRow + direction[0];
            int toCol = fromCol + direction[1];

            if (ChessboardLogic.isIndexWithinBounds(toRow,toCol)){
                if (refBoard[toRow][toCol] == null) {
                    // Empty square
                    moveSet.add(new int[]{toRow,toCol});
                } else if (refBoard[toRow][toCol].isWhite() != isWhite()) {
                    // Enemy piece
                    moveSet.add(new int[]{toRow,toCol});
                }
            }

        }


        // King Proximity Rule
        // Check if adjacent squares contain an enemy King
        for (int i = moveSet.size() - 1; i >= 0; i--){

            int[] move = moveSet.get(i);
            boolean remove = false;

            for (int dr = -1; dr <= 1 && !remove; dr++) {
                for (int dc = -1; dc <= 1; dc++) {

                    //skip the destination square
                    if (dr == 0 && dc == 0) {
                        continue;
                    }

                    int adjRow = move[0] + dr;
                    int adjCol = move[1] + dc;

                    if (ChessboardLogic.isIndexWithinBounds(adjRow, adjCol)) {
                        Piece adjPiece = refBoard[adjRow][adjCol];
                        if (adjPiece instanceof King && adjPiece.isWhite() != isWhite()) {
                            remove = true;
                            break;
                        }

                    }
                }

            }
            if (remove) moveSet.remove(i);
        }
        // --- Castling Logic ---
        if (!getHasMoved()  && !chessboardLogic.isKingInCheck(isWhite(),refBoard)  ) {
            // King Side Castling
            if (refBoard[fromRow][7] instanceof Rook rook && !rook.getHasMoved()) {

                if ( (refBoard[fromRow][5] == null && refBoard[fromRow  ][6] == null)
                        && !chessboardLogic.isSquareAttacked(!isWhite(),refBoard,fromRow,5)
                        && !chessboardLogic.isSquareAttacked(!isWhite(),refBoard,fromRow,6))  {
                    moveSet.add(new int[] {fromRow, 6});
                }
            }
            // Queen Side Castling
            if (refBoard[fromRow][0] instanceof Rook rook && !rook.getHasMoved()) {
                
                if (refBoard[fromRow][1] == null && refBoard[fromRow][2] == null && refBoard[fromRow][3] == null
                        && !chessboardLogic.isSquareAttacked(!isWhite(),refBoard,fromRow,2)
                        && !chessboardLogic.isSquareAttacked(!isWhite(),refBoard,fromRow,3)) {
                    moveSet.add(new int[] {fromRow, 2});
                }
            }
        }

        filterIllegalMoves(chessboardLogic,moveSet, fromRow, fromCol);

        //implement checks

        int validMoveCount = moveSet.size();
        validMoveSet = new int[validMoveCount][2];

        for (int i = 0; i < validMoveCount; i++){
            validMoveSet[i] = moveSet.get(i);
        }
    }

    @Override
    public boolean attacksSquare(Piece[][] refBoard,int pieceRow, int pieceCol, int targetRow, int targetCol) {
        return (Math.abs(targetRow - pieceRow) <= 1 && Math.abs(targetCol-pieceCol) <= 1) && !(Math.abs(targetRow - pieceRow) == 0 && Math.abs(targetCol-pieceCol) == 0);
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public boolean getHasMoved(){
        return hasMoved;
    }

    public String toString(){
        String tag = isWhite() ? "White King" : "Black King";
        //tag += getFile()+""+getChessRow();
        return tag;
    }
}
