package piecelogic;

import chessboard.ChessboardLogic;

import javax.swing.JOptionPane;

public class Pawn extends Piece{

    public static final int PIECE_VALUE = 1;
    private boolean check = false;
    private boolean enPassantVulnerable = false;

    public Pawn(boolean isWhite) {
        super(PieceType.PAWN, isWhite); 
    }

    @Override
    public void moveCheck(ChessboardLogic chessboardLogic, int fromRow, int fromCol) {
        moves.clear();//clear the list to remove earlier move

        if (isWhite() != chessboardLogic.isWhiteToMove()){
            validMoveSet = new int[0];
            return;
        }

        Piece[][] refBoard = chessboardLogic.getChessboard();
        int endRow = isWhite() ? 0 : 7;

        //there are 4 general moves for a pawn
        // move 1 square forward, move 2 squares forward (only as the first move), take diagonally to the left and right
        //Pawns can only move forward, and it's different for the 2 teams
        int[][] tempMoves;
        if (isWhite()){
            tempMoves = new int[][]{{fromRow-1, fromCol}, {fromRow-2, fromCol}, {fromRow-1, fromCol+1}, {fromRow-1, fromCol-1}};
        } else {
            tempMoves = new int[][]{{fromRow+ 1, fromCol}, {fromRow+2, fromCol}, {fromRow+1, fromCol+1}, {fromRow+1,fromCol -1}};
        }

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

        //moving logic
        if ( ChessboardLogic.isIndexWithinBounds( tempMoves[0][0],tempMoves[0][1] ) ){

            if (refBoard[tempMoves[0][0]][tempMoves[0][1]] == null) {//1 square check
                int move = fromRow * 8 + fromCol;
                move |= (tempMoves[0][0] * 8 + tempMoves[0][1]) << 6;
                moves.add(move);

                //promotion
                if (tempMoves[0][0] == endRow) {
                    for (int i = 1; i < 5; i++){
                        int promotion = fromRow * 8 + fromCol;
                        promotion |= (tempMoves[0][0] * 8 + tempMoves[0][1]) << 6;
                        promotion |= i << 16; //bits 16 (2 bits total) → promotion piece type
                        moves.add(promotion);
                    }
                }

                if (ChessboardLogic.isIndexWithinBounds( tempMoves[1][0],tempMoves[1][1] )){

                    if (refBoard[tempMoves[1][0]][tempMoves[1][1]] == null && !getHasMoved(fromRow)){ //2 square check
                        int doublePush = fromRow * 8 + fromCol;
                        doublePush |= (tempMoves[1][0] * 8 + tempMoves[1][1]) << 6;
                        doublePush |= 1 << 13;
                        moves.add(doublePush);
                    }
                }
            }
        }

        //capturing logic to the right (observer's  right)
        if ( ChessboardLogic.isIndexWithinBounds( tempMoves[2][0],tempMoves[2][1] ) ){
            int toRow = tempMoves[2][0];
            int toCol = tempMoves[2][1];
            if (       refBoard[toRow][toCol] != null
                    && refBoard[toRow][toCol].isWhite() != isWhite()
                    && !refBoard[toRow][toCol].isKing())
            {
                int move = fromRow * 8 + fromCol;
                move |= (toRow * 8 + toCol) << 6;
                move |= 1 << 12; //bit 12 → capture

                if (toRow == endRow) {
                    for (int i = 1; i < 5; i++){
                        int promotion = fromRow * 8 + fromCol;
                        promotion |= (toRow * 8 + toCol) << 6;
                        promotion |= i << 16; //bits 16 (2 bits total) → promotion piece type
                        moves.add(promotion);
                    }
                } else {
                    moves.add(move);
                }
            }
        }

        //capturing logic to the left (observer's left)
        if ( ChessboardLogic.isIndexWithinBounds( tempMoves[3][0],tempMoves[3][1] ) ){
            int toRow = tempMoves[3][0];
            int toCol = tempMoves[3][1];
            if (       refBoard[toRow][toCol] != null
                    && refBoard[toRow][toCol].isWhite() != isWhite()
                    && !refBoard[toRow][toCol].isKing())
            {
                int move = fromRow * 8 + fromCol;
                move |= (toRow * 8 + toCol) << 6;
                move |= 1 << 12; //bit 12 → capture

                if (toRow == endRow) {
                    for (int i = 1; i < 5; i++){
                        int promotion = fromRow * 8 + fromCol;
                        promotion |= (toRow * 8 + toCol) << 6;
                        promotion |= i << 16; //bits 16 (2 bits total) → promotion piece type
                        moves.add(promotion);
                    }
                } else {
                    moves.add(move);
                }
            }
        }

        //en passant logic start
        Piece pieceToTheLeft = null,pieceToTheRight = null;

        if (ChessboardLogic.isIndexWithinBounds(fromRow,fromCol-1))
            pieceToTheLeft = refBoard[fromRow][fromCol-1];

        if (pieceToTheLeft != null && pieceToTheLeft.isPawn()
                && ((Pawn) pieceToTheLeft).getEnPassantVulnerable() && isWhite() != pieceToTheLeft.isWhite()){
            int dir = isWhite() ? -1 : 1;
            int move = fromRow * 8 + fromCol;
            move |= ((fromRow+dir) * 8 + (fromCol-1)) << 6;
            move |= 1 << 14; //bit 14 → en passant
            moves.add(move);
        }

        if (ChessboardLogic.isIndexWithinBounds(fromRow,fromCol+1))
            pieceToTheRight = refBoard[fromRow][fromCol+1];

        if (pieceToTheRight instanceof Pawn pawnToTheRight && pawnToTheRight.getEnPassantVulnerable() && isWhite() != pawnToTheRight.isWhite()){
            int dir = isWhite() ? -1 : 1;
            int move = fromRow * 8 + fromCol;
            move |= ((fromRow+dir) * 8 + (fromCol+1)) << 6;
            move |= 1 << 14; //bit 14 → en passant
            moves.add(move);
        }
        //en passant logic over

        filterIllegalMoves(chessboardLogic,moves);

        int validMoveCount = moves.size();
        validMoveSet = new int[validMoveCount];

        for (int i = 0; i < validMoveCount; i++){
            validMoveSet[i] = moves.get(i);
        }
    }

    @Override
    public boolean attacksSquare(Piece[][] refBoard, int pieceRow, int pieceCol, int targetRow, int targetCol) {

        int rowDir = isWhite() ? -1 : 1;

        return (targetRow == pieceRow+rowDir
                && (targetCol == pieceCol - 1 || targetCol == pieceCol + 1)) ;

    }

    /*
    public static void clearAllEnPassantFlags(ChessboardLogic chessboardLogic){

        Piece[][] refBoard = chessboardLogic.getChessboard();

        for (int r = 0; r < 8; r++){
            for (int c = 0; c < 8; c++){

                Piece p = refBoard[r][c];

                if (p instanceof Pawn pawn){
                    pawn.setEnPassantVulnerable(false);
                }
            }
        }
    }

     */

    /*
    @Override
    public void filterIllegalMoves(ChessboardLogic chessboardLogic, List<Integer> moves){

        Piece[][] refBoard;

        for (int i = moves.size()-1 ; i >= 0; i--){

            int[] move = decryptMove(moves.get(i));// fromRow, fromCol, toRow, toCol, enPassant, castle, promotion, doublePawnPush, doublePawnPush

            int fromRow = move[0];
            int fromCol = move[1];

            int toRow = move[2];
            int toCol = move[3];

            boolean enPassant = move[4] == 1 ;
            int promoType = move[6];
            boolean promoted = promoType > 0;

            refBoard = shallowCopyBoard(chessboardLogic.getChessboard());

            if (Math.abs(fromCol - toCol) == 1){
                    if (enPassant) {
                        
                    int dir = isWhite() ? 1 : -1;
                    Piece captured = refBoard[toRow+dir][toCol] ;

                    if (!captured.isPawn()){
                        throw new IllegalArgumentException("The captured Piece using EnPassant is not a Pawn at filterIllegalMoves in Pawn ! ");
                    }

                    refBoard[toRow+dir][toCol] = null;
                }
            }

            int endRow = isWhite() ? 0 : 7;

            if (promoted && toRow == endRow){

                Piece newPiece = null;

                switch (promoType){
                    case 1 -> newPiece = PieceFactory.createPiece(PieceType.KNIGHT, isWhite());
                    case 2 -> newPiece = PieceFactory.createPiece(PieceType.BISHOP, isWhite());
                    case 3 -> newPiece = PieceFactory.createPiece(PieceType.ROOK, isWhite());
                    case 4 -> newPiece = PieceFactory.createPiece(PieceType.QUEEN, isWhite());
                }

                char file = colToFile(toCol);
                int rank = rowToChessRow(endRow);

                chessboardLogic.insertPieceToBoard(newPiece, refBoard,file, rank);

            }

            refBoard[toRow][toCol] = refBoard[fromRow][fromCol];
            refBoard[fromRow][fromCol] = null;

            if ( chessboardLogic.isKingInCheck(isWhite(),refBoard) ){
                moves.remove(i);
            }

        }

    }

     */

    public Piece promote(ChessboardLogic chessboardLogic){

        Piece newPiece;

        String[] options = { //promotion options
                    "Knight",
                    "Bishop",
                    "Rook",
                    "Queen",
        };

        int choice = JOptionPane.showOptionDialog(//the message box to choose which promotion happens
                null,                    
                "Choose the promotion:",
                "Promotion",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[3]//default is queen
        );
        

        switch (choice){ //the promotions
            case 0 -> newPiece = PieceFactory.createPiece(PieceType.KNIGHT, isWhite());
            case 1 -> newPiece = PieceFactory.createPiece(PieceType.BISHOP, isWhite());
            case 2 -> newPiece = PieceFactory.createPiece(PieceType.ROOK, isWhite());
            case 3 -> newPiece = PieceFactory.createPiece(PieceType.QUEEN, isWhite());
            default -> newPiece = PieceFactory.createPiece(PieceType.QUEEN, isWhite());
        }        

        return newPiece;    
    }  

    public boolean getHasMoved(int fromRow){

        int startingRow = isWhite() ? 6 : 1;

        return fromRow != startingRow;
    }

    public String toString(){
        String tag = isWhite() ? "White Pawn" : "Black Pawn";
        //tag += getFile()+""+getChessRow();
        return tag;
    }

    public void setEnPassantVulnerable(boolean enPassantVulnerable) {
        this.enPassantVulnerable = enPassantVulnerable;
    }

    public boolean getEnPassantVulnerable(){return enPassantVulnerable;}
}
