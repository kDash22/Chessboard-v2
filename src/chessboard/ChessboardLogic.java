package chessboard;

import java.util.List;

import piecelogic.*;

import javax.swing.*;

public class ChessboardLogic {

    protected ChessboardGui chessboardGui;

    private boolean whiteToMove ;

    protected Piece[][] chessboard = new Piece[8][8];//logical representation of the 8 x 8 board
    
    public static final List<Character> COLUMN_LETTERS = List.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h');

    private boolean immediateAction = false;

    public ChessboardLogic(){
        System.out.println("chessboardLogic obj created ! ");
        whiteToMove = true;
    }
    //setters
    public void setChessboard(Piece[][] board){
        if (board.length != 8 || board[0].length != 8){
            throw new IllegalArgumentException("Board size is not 8 x 8 ! : "+board.length+" x "+board[0].length);
        }
        this.chessboard = board;
    }

    public void setGui(ChessboardGui chessboardGui){
        this.chessboardGui = chessboardGui;
    }

    public void setImmediateAction(boolean immediateAction) {
        this.immediateAction = immediateAction;
    }

    public void setWhiteToMove(boolean whiteToMove) {
        this.whiteToMove = whiteToMove;
    }

    //getters
    public ChessboardGui getChessboardGui(){
        return chessboardGui;
    }

    public Piece[][] getChessboard() {
        return chessboard;
    }

    public boolean isWhiteToMove(){
        return whiteToMove;
    }

    public boolean getImmediateAction(){
        return immediateAction;
    }

    public void insertPieceToBoard(Piece piece, Piece[][] chessboard,char file, int chessRow){
        int col = fileToCol(file);
        int row = chessRowToRow(chessRow);

        this.chessboard[row][col] = piece;
        //System.out.println("piece inserted into the board ! ");
    }

    public void newGame(){

        chessboardGui.setChessboardLogic(this);
        setWhiteToMove(true);

        setChessboard(new Piece[8][8]);
        
        // white
        setupBackRank(true, 1);
        setupPawns(true, 2);

        // black
        setupBackRank(false, 8);
        setupPawns(false, 7);

        System.out.println("Chessboard.newGame() was called!");

    }

    //for testing purposes
    public void customBoard(){
        chessboardGui.setChessboardLogic(this);
        setWhiteToMove(true);

        Piece[][] emptyBoard = new Piece[8][8];
        setChessboard(emptyBoard);

        insertPieceToBoard(PieceFactory.createPiece(PieceType.KING, false), this.chessboard, 'e', 1);
        insertPieceToBoard(PieceFactory.createPiece(PieceType.KING, true), this.chessboard, 'e', 3);
        insertPieceToBoard(PieceFactory.createPiece(PieceType.KNIGHT, false), this.chessboard, 'e', 4);
        insertPieceToBoard(PieceFactory.createPiece(PieceType.BISHOP, false), this.chessboard, 'e', 5);
        insertPieceToBoard(PieceFactory.createPiece(PieceType.QUEEN, true), this.chessboard, 'd', 7);
        insertPieceToBoard(PieceFactory.createPiece(PieceType.PAWN, true), this.chessboard, 'a', 2);
        insertPieceToBoard(PieceFactory.createPiece(PieceType.PAWN, false), this.chessboard, 'a', 7);



    }

    public static boolean isIndexWithinBounds(int row, int col){
        return row < 8 && col < 8 && row >= 0 && col >= 0;
    }

    public void movePiece(int selectedRow, int selectedCol, int selectedToRow, int selectedToCol){

        Piece movingPiece = chessboard[selectedRow][selectedCol];

        int[] validMoveSet = movingPiece.getValidMoveSet();

        boolean found = false;

        for(int i = 0; i < validMoveSet.length; i++){

            int[] move = decryptMove(validMoveSet[i]);// fromRow, fromCol, toRow, toCol, enPassant, castle, promotion, doublePawnPush

            int toRow = move[2];
            int toCol = move[3];

            boolean enPassant = move[4] == 1 ;
            boolean castle = move[5] == 1;
            int promoType = move[6];
            boolean doublePawnPush = move[7] == 1;


            if (toRow == selectedToRow && toCol == selectedToCol){

                // Castling Execution Logic
                if (castle) {
                    int rookOriginalCol = (selectedToCol == 6) ? 7 : 0;
                    int rookTargetCol = (selectedToCol == 6) ? 5 : 3;

                    Piece rook = chessboard[selectedRow][rookOriginalCol];

                    if (rook.isRook()) {

                        ((Rook) rook).setHasMoved(true);
                        chessboard[selectedRow][rookTargetCol] = rook;
                        chessboard[selectedRow][rookOriginalCol] = null;
                    }
                }

                //EnPassant Execution Logic
                if (immediateAction ){

                    int dir = movingPiece.isWhite() ? 1 : -1;
                    if (isIndexWithinBounds(toRow+dir,toCol)) {
                        if (enPassant)
                            chessboard[selectedToRow + dir][selectedToCol] = null;

                        if (chessboard[selectedToRow + dir][selectedToCol] != null && chessboard[selectedToRow + dir][selectedToCol].isPawn())
                            ((Pawn) chessboard[selectedToRow + dir][selectedToCol]).setEnPassantVulnerable(false);
                    }
                    immediateAction = false;
                }

                //en passant available setting logic, must be after en passant execution logic
                if (doublePawnPush){
                    Piece pieceToTheLeft = null,pieceToTheRight = null;

                    if (isIndexWithinBounds(selectedToRow,selectedToCol-1))
                        pieceToTheLeft = chessboard[selectedToRow][selectedToCol-1];

                    if (isIndexWithinBounds(selectedToRow,selectedToCol+1))
                        pieceToTheRight = chessboard[selectedToRow][selectedToCol+1];

                    if ((pieceToTheLeft != null && pieceToTheLeft.isPawn() && pieceToTheLeft.isWhite() != whiteToMove)
                            || (pieceToTheRight != null && pieceToTheRight.isPawn() && pieceToTheRight.isWhite() != whiteToMove)){
                        ((Pawn) movingPiece).setEnPassantVulnerable(true);
                        immediateAction = true;
                    }

                }

                chessboard[selectedToRow][selectedToCol] = movingPiece;
                chessboard[selectedRow][selectedCol] = null;

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

                    char file = colToFile(selectedToCol);
                    int rank = rowToChessRow(endRow);

                    if (selectedToRow == endRow) {
                        insertPieceToBoard( ((Pawn) movingPiece).promote(this), this.chessboard,file, rank);
                    }

                }

                setWhiteToMove(!whiteToMove);

                checkGameOver();

                break;
            }

        }
    }

    //a method to check if a square is attacked by a specified color
    public boolean isSquareAttacked(boolean attackerIsWhite, Piece[][] refBoard, int row, int col){

        for (int pieceRow = 0; pieceRow < 8; pieceRow++){
            for (int pieceCol = 0; pieceCol < 8; pieceCol++ ){

                Piece piece = refBoard[pieceRow][pieceCol];
                if (piece != null &&
                        piece.isWhite() == attackerIsWhite &&
                        piece.attacksSquare(refBoard, pieceRow, pieceCol, row, col)) {

                    return true;
                }

            }
        }

        return false;
    }

    public int[] getKingPos(boolean isWhite, Piece[][] chessboard){

        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){

                Piece piece = chessboard[row][col];

                if (piece != null && piece.isKing() && piece.isWhite() == isWhite){
                    return new int[]{row,col};
                }

            }
        }
        throw new IllegalStateException("No king found for color : "+ (isWhite ? "white" : "black") );
    }

    public boolean isKingInCheck(boolean isWhite, Piece[][] chessboard){

        int[] kingPos = getKingPos(isWhite, chessboard);
        return isSquareAttacked(!isWhite, chessboard, kingPos[0], kingPos[1]);
    }

    private void setupBackRank(boolean isWhite, int rank) {
        PieceType[] order = {
                PieceType.ROOK,
                PieceType.KNIGHT,
                PieceType.BISHOP,
                PieceType.QUEEN,
                PieceType.KING,
                PieceType.BISHOP,
                PieceType.KNIGHT,
                PieceType.ROOK
        };

        char file = 'a';
        for (PieceType type : order) {
            insertPieceToBoard(
                    PieceFactory.createPiece(type,isWhite),
                    this.chessboard,
                    file,
                    rank
            );
            file++;
        }
    }

    private void setupPawns(boolean isWhite, int rank) {
        for (char file = 'a'; file <= 'h'; file++) {
            insertPieceToBoard(
                    PieceFactory.createPiece(PieceType.PAWN, isWhite),
                    this.chessboard,
                    file,
                    rank
            );
        }
    }

    public boolean hasNoLegalMoves(){

        //String colour = whiteToMove ? "White " : "Black ";
        //System.out.println("Checking if game over for "+colour+"! ");

        int validMoveCount = 0;

        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){

                if (chessboard[row][col] == null ) continue;

                if (chessboard[row][col].isWhite() != whiteToMove) continue;

                Piece piece = chessboard[row][col];
                piece.moveCheck(this,row,col);
                int moveCount = piece.getValidMoveSet().length;
                //System.out.print(moveCount+" ");
                validMoveCount = validMoveCount + moveCount;

            }
        }
        //System.out.println();

        //System.out.println("valid move count : "+validMoveCount);

        //String state = isGameOver ? "Game over ! " : "Not over ! ";
        //System.out.println((validMoveCount == 0)+"\n");

        return validMoveCount == 0;
    }

    public void checkGameOver(){
        boolean savedImmediateAction = immediateAction;
        boolean hasNoMoves = hasNoLegalMoves();
        immediateAction = savedImmediateAction;

        if (hasNoMoves) {

            Piece[][] chessboard = getChessboard();
            boolean turn = isWhiteToMove();

            SwingUtilities.invokeLater(() -> {
                if (isKingInCheck(turn, chessboard)) {
                    String winner = turn ? "Black" : "White";
                    System.out.println("CHECKMATE! " + winner + " wins!");
                    JOptionPane.showMessageDialog(chessboardGui, "CHECKMATE! " + winner + " wins!");
                } else {
                    System.out.println("STALEMATE!");
                    JOptionPane.showMessageDialog(chessboardGui, "STALEMATE! It's a draw.");
                }
            });

        }

    }

    // a method used to convert column letter into int to be used in arrays
    public static int fileToCol(Character file) {
        if (!COLUMN_LETTERS.contains(file)) {
            throw new IllegalArgumentException(" COLUMN LETTER NOT VALID ! : " + file);
        }
        return (file - 'a');
    }

    // a method used to convert array col number to chess column number
    public static char colToFile(int col) {
        if (col > 7 || col < 0)
            throw new IllegalArgumentException(" Array Column number must be between 0 and 7 ! :" + col);
        return (char) ('a' + col);
    }

    // a method used to convert chess rows into int to be used in arrays
    public static int chessRowToRow(int chessRow) {
        if (chessRow < 1 || chessRow > 8) {
            throw new IllegalArgumentException("chessRow must be between 1 and 8: " + chessRow);
        }
        return 8 - chessRow;
    }

    // a method used to convert chess rows into int to be used in arrays
    public static int rowToChessRow(int row) {
        if (row < 0 || row > 7) {
            throw new IllegalArgumentException("Array row number must be between 0 and 7: " + row);
        }
        return 8 - row;
    }

    //a method used to decrypt the int move
    public static int[] decryptMove(int move){

        int fromSquare = move & 63;
        int fromRow = fromSquare / 8;
        int fromCol = fromSquare % 8;

        int toSquare = (move >> 6) & 63; //shifting the encrypt right to get the normalised version of the toSquare
        int toRow = toSquare / 8;
        int toCol = toSquare % 8;

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

        int enPassant = (move >> 14) & 1;
        int castle = (move >> 15) & 1;
        int promotion = (move >> 16) & 3;
        int doublePawnPush = (move >> 13) & 1;

        return new int[]{fromRow, fromCol, toRow ,toCol, enPassant, castle, promotion, doublePawnPush};
    }

    public static int[] getToSquare(int move){
        int toSquare = (move >> 6) & 63; //shifting the encrypt right to get the normalised version of the toSquare
        int toRow = toSquare / 8;
        int toCol = toSquare % 8;

        return new int[]{toRow,toCol};
    }
}
