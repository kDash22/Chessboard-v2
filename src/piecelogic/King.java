package piecelogic;

import chessboard.ChessboardLogic;
import static chessboard.ChessboardLogic.getToSquare;

public class King extends Piece{

    private boolean hasMoved = false;

    public King(boolean isWhite) {
        super(PieceType.KING, isWhite);
    }

    @Override
    public void moveCheck(ChessboardLogic chessboardLogic, int fromRow, int fromCol) {
        moves.clear();//clear the list to remove earlier move

        if (isWhite() != chessboardLogic.isWhiteToMove()){
            validMoveSet = new int[0];
            return;
        }

        Piece[][] refBoard = chessboardLogic.getChessboard();

        //a king has 8 general moves
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
                */

            int toRow = fromRow + direction[0];
            int toCol = fromCol + direction[1];

            int move = fromRow * 8 + fromCol;

            move |= (toRow * 8 + toCol) << 6;

            if (ChessboardLogic.isIndexWithinBounds(toRow,toCol)){
                if (refBoard[toRow][toCol] == null) {
                    // Empty square
                    moves.add(move);
                } else if (refBoard[toRow][toCol].isWhite() != isWhite() && !refBoard[toRow][toCol].isKing()) {
                    // Enemy piece
                    move |= 1 << 12;
                    moves.add(move);
                }
            }

        }


        // King Proximity Rule
        // Check if adjacent squares contain an enemy King
        for (int i = moves.size() - 1; i >= 0; i--){

            int move = moves.get(i);
            int[] toSquare = getToSquare(move);
            boolean remove = false;

            for (int dr = -1; dr <= 1 && !remove; dr++) {
                for (int dc = -1; dc <= 1; dc++) {

                    //skip the destination square
                    if (dr == 0 && dc == 0) {
                        continue;
                    }

                    int adjRow = toSquare[0] + dr;
                    int adjCol = toSquare[1] + dc;

                    if (ChessboardLogic.isIndexWithinBounds(adjRow, adjCol)) {
                        Piece adjPiece = refBoard[adjRow][adjCol];
                        if (adjPiece != null && adjPiece.isKing() && adjPiece.isWhite() != isWhite()) {
                            remove = true;
                            break;
                        }

                    }
                }

            }
            if (remove) moves.remove(i);
        }
        // --- Castling Logic ---
        if (!getHasMoved()  && !chessboardLogic.isKingInCheck(isWhite(),refBoard)  ) {
            // King Side Castling
            if (refBoard[fromRow][7] != null && refBoard[fromRow][7].getPieceType() == PieceType.ROOK
                    && refBoard[fromRow][7].isWhite() == isWhite() &&!((Rook) refBoard[fromRow][7]).getHasMoved()) {

                if ( (refBoard[fromRow][5] == null && refBoard[fromRow  ][6] == null)
                        && !chessboardLogic.isSquareAttacked(!isWhite(),refBoard,fromRow,5)
                        && !chessboardLogic.isSquareAttacked(!isWhite(),refBoard,fromRow,6))  {

                    int move = fromRow * 8 + fromCol;
                    move |= (fromRow * 8 + 6) << 6;
                    move |= 1 << 15; //bit  15    → castling
                    moves.add(move);
                }
            }
            // Queen Side Castling
            if (refBoard[fromRow][0] != null && refBoard[fromRow][0].getPieceType() == PieceType.ROOK
                    && refBoard[fromRow][0].isWhite() == isWhite() && !((Rook) refBoard[fromRow][0]).getHasMoved()) {
                
                if (refBoard[fromRow][1] == null && refBoard[fromRow][2] == null && refBoard[fromRow][3] == null
                        && !chessboardLogic.isSquareAttacked(!isWhite(),refBoard,fromRow,2)
                        && !chessboardLogic.isSquareAttacked(!isWhite(),refBoard,fromRow,3)) {
                    int move = fromRow * 8 + fromCol;
                    move |= (fromRow * 8 + 2) << 6;
                    move |= 1 << 15; //bit  15    → castling
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
