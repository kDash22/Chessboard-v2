package chessboard;

import java.util.List;

import piecelogic.*;

public class ChessboardLogic {

    protected ChessboardGui chessboardGui;

    private boolean whiteToMove ;

    protected Piece[][] chessboard = new Piece[8][8];//logical representation of the 8 x 8 board
    
    public static final List<Character> COLUMN_LETTERS = List.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h');

    private boolean immediateAction = false;

    public ChessboardLogic(){
        System.out.println("chessboardLogic obj created ! ");
        chessboardGui = new ChessboardGui();
        whiteToMove = true;
    }
    //setters
    public void setChessboard(Piece[][] board){
        if (board.length != 8 || board[0].length != 8){
            throw new IllegalArgumentException("Board size is not 8 x 8 ! : "+board.length+" x "+board[0].length);
        }
        this.chessboard = board;
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

    public void insertPieceToBoard(Piece piece, char file, int chessRow){
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

        insertPieceToBoard(PieceFactory.createPiece(PieceType.KING,false),'e',1);
        insertPieceToBoard(PieceFactory.createPiece(PieceType.KING,true),'e',3);
        insertPieceToBoard(PieceFactory.createPiece(PieceType.KNIGHT,false),'e',4);
        insertPieceToBoard(PieceFactory.createPiece(PieceType.BISHOP,false),'e',5);
        insertPieceToBoard(PieceFactory.createPiece(PieceType.QUEEN,true),'d',7);
        insertPieceToBoard(PieceFactory.createPiece(PieceType.PAWN, true), 'a', 2);
        insertPieceToBoard(PieceFactory.createPiece(PieceType.PAWN, false), 'a', 7);


    }

    public static boolean isIndexWithinBounds(int row, int col){
        return row < 8 && col < 8 && row >= 0 && col >= 0;
    }

    public void movePiece(int selectedRow, int selectedCol, int selectedToRow, int selectedToCol){

        Piece movingPiece = chessboard[selectedRow][selectedCol];

        int[][] validMoveSet = movingPiece.getValidMoveSet();

        for(int i = 0; i < validMoveSet.length; i++){

            int r = validMoveSet[i][0];
            int c = validMoveSet[i][1];

            if (r == selectedToRow && c== selectedToCol){

                // Castling Execution Logic
                if (movingPiece instanceof King && Math.abs(selectedCol - selectedToCol) == 2) {
                    int rookOriginalCol = (selectedToCol == 6) ? 7 : 0;
                    int rookTargetCol = (selectedToCol == 6) ? 5 : 3;

                    Piece rook = chessboard[selectedRow][rookOriginalCol];

                    if (rook instanceof Rook) {

                        ((Rook) rook).setHasMoved(true);
                        chessboard[selectedRow][rookTargetCol] = rook;
                        chessboard[selectedRow][rookOriginalCol] = null;
                    }
                }

                //EnPassant Execution Logic
                if (immediateAction && movingPiece instanceof Pawn pawn){

                    if (Math.abs(selectedCol - selectedToCol) == 1 && chessboard[selectedToRow][selectedToCol] == null ){
                        int dir = pawn.isWhite() ? 1 : -1;
                        chessboard[selectedToRow+dir][selectedToCol] = null;
                    }
                    immediateAction = false;

                }

                Pawn.clearAllEnPassantFlags(this);//resetting

                //en passant available setting logic, must be after en passant execution logic
                if (movingPiece instanceof Pawn pawn && Math.abs(selectedRow-selectedToRow) == 2){
                    Piece pieceToTheLeft = null,pieceToTheRight = null;

                    if (isIndexWithinBounds(selectedToRow,selectedToCol-1))
                        pieceToTheLeft = chessboard[selectedToRow][selectedToCol-1];

                    if (isIndexWithinBounds(selectedToRow,selectedToCol+1))
                        pieceToTheRight = chessboard[selectedToRow][selectedToCol+1];


                    if ((pieceToTheLeft instanceof Pawn && pieceToTheLeft.isWhite() != whiteToMove)
                            || (pieceToTheRight instanceof Pawn && pieceToTheRight.isWhite() != whiteToMove)){
                        pawn.setEnPassantVulnerable(true);
                        immediateAction = true;
                    }

                }

                chessboard[selectedToRow][selectedToCol] = movingPiece;
                chessboard[selectedRow][selectedCol] = null;
                setWhiteToMove(!whiteToMove);

            }

        }

        if(movingPiece instanceof Rook rook && !rook.getHasMoved()){
                rook.setHasMoved(true);
        }

        if(movingPiece instanceof King king ){

            if (!king.getHasMoved()) {
                king.setHasMoved(true);
            }

        }

        if (movingPiece instanceof Pawn pawn) {

            int endRow = pawn.isWhite() ? 0 : 7;

            char file = colToFile(selectedToCol);
            int rank = rowToChessRow(endRow);
            
            if (pawn.isWhite() && selectedToRow == endRow) {
                insertPieceToBoard(pawn.promote(this), file, rank);
            } else if (!pawn.isWhite() && selectedToRow == endRow) {
                insertPieceToBoard(pawn.promote(this), file, rank);
            }
            
        }
        checkGameOver();
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

                if (piece instanceof King king && king.isWhite() == isWhite){
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
                    file,
                    rank
            );
        }
    }

    public boolean checkGameOver(){

        String colour = whiteToMove ? "White " : "Black ";
        System.out.println("Checking if game over for "+colour+"! ");

        int validMoveCount = 0;

        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){

                if (chessboard[row][col] == null ) continue;

                if (chessboard[row][col].isWhite() != whiteToMove) continue;

                Piece piece = chessboard[row][col];
                piece.moveCheck(this,row,col);
                int moveCount = piece.getValidMoveSet().length;
                System.out.print(moveCount);
                validMoveCount = validMoveCount + moveCount;

            }
        }
        System.out.println();

        boolean isGameOver = validMoveCount == 0;
        System.out.println("valid move count : "+validMoveCount);

        String state = isGameOver ? "Game over ! " : "Not over ! ";
        System.out.println(state+"\n");

        return isGameOver;
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
}
