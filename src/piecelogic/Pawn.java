package piecelogic;

import chessboard.ChessboardLogic;
import static global.Global.shallowCopyBoard;

import java.util.List;

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
        moveSet.clear();//clear the list to remove earlier move

        if (isWhite() != chessboardLogic.isWhiteToMove()){
            validMoveSet = new int[0][2];
            return;
        }

        Piece[][] refBoard = chessboardLogic.getChessboard();

        //there are 4 general moves for a pawn
        // move 1 square forward, move 2 squares forward (only as the first move), take diagonally to the left and right
        //Pawns can only move forward, and it's different for the 2 teams
        int[][] tempMoves;
        if (isWhite()){
            tempMoves = new int[][]{{fromRow-1, fromCol}, {fromRow-2, fromCol}, {fromRow-1, fromCol+1}, {fromRow-1, fromCol-1}};
        } else {
            tempMoves = new int[][]{{fromRow+ 1, fromCol}, {fromRow+2, fromCol}, {fromRow+1, fromCol+1}, {fromRow+1,fromCol -1}};
        }

        //moving logic
        if ( ChessboardLogic.isIndexWithinBounds( tempMoves[0][0],tempMoves[0][1] ) ){

            if (refBoard[tempMoves[0][0]][tempMoves[0][1]] == null) {//1 square check
                moveSet.add(tempMoves[0]);

                if (ChessboardLogic.isIndexWithinBounds( tempMoves[1][0],tempMoves[1][1] )){

                    if (refBoard[tempMoves[1][0]][tempMoves[1][1]] == null && !getHasMoved(fromRow)) //2 square check
                        moveSet.add(tempMoves[1]);
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
                moveSet.add(tempMoves[2]);
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
                moveSet.add(tempMoves[3]);
            }
        }

        //en passant logic start
        Piece pieceToTheLeft = null,pieceToTheRight = null;

        if (ChessboardLogic.isIndexWithinBounds(fromRow,fromCol-1))
            pieceToTheLeft = refBoard[fromRow][fromCol-1];

        if (ChessboardLogic.isIndexWithinBounds(fromRow,fromCol+1))
            pieceToTheRight = refBoard[fromRow][fromCol+1];

        if (pieceToTheLeft instanceof Pawn pawnToTheLeft && pawnToTheLeft.getEnPassantVulnerable() && isWhite() != pawnToTheLeft.isWhite()){
            int dir = isWhite() ? -1 : 1;
            moveSet.add(new int[]{fromRow+dir,fromCol-1});
        }

        if (pieceToTheRight instanceof Pawn pawnToTheRight && pawnToTheRight.getEnPassantVulnerable() && isWhite() != pawnToTheRight.isWhite()){
            int dir = isWhite() ? -1 : 1;
            moveSet.add(new int[]{fromRow+dir,fromCol+1});
        }
        //en passant logic over

        filterIllegalMoves(chessboardLogic,moveSet, fromRow, fromCol);

        int validMoveCount = moveSet.size();
        validMoveSet = new int[validMoveCount][2];

        for (int i = 0; i < validMoveCount; i++){
            validMoveSet[i] = moveSet.get(i);
        }
    }

    @Override
    public boolean attacksSquare(Piece[][] refBoard, int pieceRow, int pieceCol, int targetRow, int targetCol) {

        int rowDir = isWhite() ? -1 : 1;

        return (targetRow == pieceRow+rowDir
                && (targetCol == pieceCol - 1 || targetCol == pieceCol + 1)) ;

    }

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

    @Override
    public void filterIllegalMoves(ChessboardLogic chessboardLogic, List<int[]> moveSet, int fromRow, int fromCol){

        Piece[][] refBoard;

        for (int i = moveSet.size()-1 ; i >= 0; i--){

            refBoard = shallowCopyBoard(chessboardLogic.getChessboard());

            int[] square = moveSet.get(i);

            Piece captured;

            if (Math.abs(fromCol - square[1]) == 1){
                    if (refBoard[ square[0] ][ square[1] ] == null ) {
                        
                    int dir = isWhite() ? 1 : -1;
                    captured = refBoard[square[0]+dir][square[1]] ;

                    if (!(captured instanceof Pawn)){
                        throw new IllegalArgumentException("The captured Piece using EnPassant is not a Pawn at filterIllegalMoves in Pawn ! ");
                    }

                    refBoard[square[0]+dir][square[1]] = null;

                } else {
                    captured = refBoard[ square[0] ][ square[1] ];
                }
            }

            refBoard[ square[0] ][ square[1] ] = refBoard[fromRow][fromCol];
            refBoard[fromRow][fromCol] = null;

            if ( chessboardLogic.isKingInCheck(isWhite(),refBoard) ){
                moveSet.remove(i);
            }

        }

    }

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
