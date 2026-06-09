package engine;

import chessboard.ChessboardLogic;
import piecelogic.*;

import java.util.concurrent.TimeUnit;

import static chessboard.ChessboardLogic.*;
import static engine.Evaluator.scoreMove;


public class MoveGenerator {

    public static MoveList generateMoves(ChessboardLogic chessboardLogic,int ply) {

        int[] moves = new int[256]; //maximum moves for any given turn is estimated to be about 218
        Piece[][] refBoard = chessboardLogic.getChessboard();
        int[] score = new int[256];

        int moveCount = 0;
        boolean isWhiteToMove = chessboardLogic.isWhiteToMove();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Piece p = refBoard[row][col];

                if (p == null) continue;

                if (p.isWhite() != isWhiteToMove) continue;

                p.moveCheck(chessboardLogic, row, col);
                int[] pValidMoveSet = p.getValidMoveSet();

                int moverPhase = Evaluator.calculateOwnPhase(chessboardLogic,isWhiteToMove);
                int enemyPhase = Evaluator.calculateOwnPhase(chessboardLogic,!isWhiteToMove);

                int enemyKingSquare = ChessboardLogic.getKingSquare(!isWhiteToMove,refBoard);
                int friendlyKingSquare = ChessboardLogic.getKingSquare(isWhiteToMove,refBoard);

                for (int i = 0; i < pValidMoveSet.length; i++) {
                    int move = pValidMoveSet[i];

                    int[] toSquare = ChessboardLogic.getToSquare(move);
                    int fromRow = (move & 0b111111) / 8;
                    int fromCol = (move & 0b111111) % 8;
                    /*
                        [ flags ][   to   ][  from  ]
                        bits12+   bits6-11    bits0-5
                     */
                    moves[moveCount] = move; // add flags if needed
                    boolean isToSquareAttacked = ChessboardLogic.isSquareAttacked(!isWhiteToMove,refBoard,toSquare[0],toSquare[1]);
                    boolean isFromSquareAttacked = ChessboardLogic.isSquareAttacked(!isWhiteToMove,refBoard,fromRow,fromCol);
                    boolean isToSquareProtected = ChessboardLogic.isSquareAttacked(isWhiteToMove,refBoard,toSquare[0],toSquare[1]);
                    boolean isFromSquareProtected = ChessboardLogic.isSquareAttacked(isWhiteToMove,refBoard,fromRow,fromCol);
                    score[moveCount] = scoreMove(pValidMoveSet[i],moverPhase,enemyPhase,friendlyKingSquare,enemyKingSquare,ply,isFromSquareAttacked,isToSquareAttacked,isFromSquareProtected,isToSquareProtected);
                    moveCount++;
                }

            }
        }
        return new MoveList(moves, score ,moveCount);
    }

    public static MoveList generateCaptures(ChessboardLogic chessboardLogic,int ply) {

        int[] moves = new int[256]; //maximum moves for any given turn is estimated to be about 218
        Piece[][] refBoard = chessboardLogic.getChessboard();
        int[] score = new int[256];

        int moveCount = 0;
        boolean isWhiteToMove = chessboardLogic.isWhiteToMove();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Piece p = refBoard[row][col];

                if (p == null) continue;

                if (p.isWhite() != isWhiteToMove) continue;

                p.moveCheck(chessboardLogic, row, col);
                int[] pValidMoveSet = p.getValidMoveSet();

                int enemyKingSquare = ChessboardLogic.getKingSquare(!isWhiteToMove,refBoard);
                int friendlyKingSquare = ChessboardLogic.getKingSquare(isWhiteToMove,refBoard);

                int moverPhase = Evaluator.calculateOwnPhase(chessboardLogic,isWhiteToMove);
                int enemyPhase = Evaluator.calculateOwnPhase(chessboardLogic,!isWhiteToMove);

                for (int i = 0; i < pValidMoveSet.length; i++) {

                    int move = pValidMoveSet[i];

                    boolean isCapture = ((move >> 12) & 1) == 1;
                    boolean isPromotion = ((move >> 16) & 7) > 0;//calculates promotions too

                    //If it is neither a capture nor a promotion, skip
                    if (!isCapture && !isPromotion) {
                        continue;
                    }
                    int[] toSquare = ChessboardLogic.getToSquare(move);
                    int fromRow = (move & 0b111111) / 8;
                    int fromCol = (move & 0b111111) % 8;
                    /*
                        [ flags ][   to   ][  from  ]
                        bits12+   bits6-11    bits0-5
                     */
                    moves[moveCount] = move; // add flags if needed
                    boolean isToSquareAttacked = ChessboardLogic.isSquareAttacked(!isWhiteToMove,refBoard,toSquare[0],toSquare[1]);
                    boolean isFromSquareAttacked = ChessboardLogic.isSquareAttacked(!isWhiteToMove,refBoard,fromRow,fromCol);
                    boolean isToSquareProtected = ChessboardLogic.isSquareAttacked(isWhiteToMove,refBoard,toSquare[0],toSquare[1]);
                    boolean isFromSquareProtected = ChessboardLogic.isSquareAttacked(isWhiteToMove,refBoard,fromRow,fromCol);
                    score[moveCount] = scoreMove(pValidMoveSet[i],moverPhase,enemyPhase,friendlyKingSquare,enemyKingSquare,ply,isFromSquareAttacked,isToSquareAttacked,isFromSquareProtected,isToSquareProtected);
                    moveCount++;
                }

            }
        }
        return new MoveList(moves, score ,moveCount);
    }

    public static MoveState doMove(ChessboardLogic chessboardLogic, Piece[][] refBoard,int encryptedMove) {

        boolean prevImmediateActionState = chessboardLogic.getImmediateAction();
        chessboardLogic.setImmediateAction(false);//resetting

        Pawn previousEPPawn = null;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (refBoard[r][c] != null && refBoard[r][c].isPawn() && ((Pawn) refBoard[r][c]).getEnPassantVulnerable()) {
                    previousEPPawn = ((Pawn) refBoard[r][c]);
                    previousEPPawn.setEnPassantVulnerable(false);
                    r = 8; break; // Optimization: Only 1 pawn can be vulnerable, break early
                }
            }
        }

        int[] move = decryptMove(encryptedMove);// fromRow, fromCol, toRow, toCol, enPassant, castle,
        // promotion, doublePawnPush, winningCaptureValue, check

        int fromRow = move[0];
        int fromCol = move[1];

        int toRow = move[2];
        int toCol = move[3];

        boolean enPassant = move[4] == 1 ;
        boolean castle = move[5] == 1;
        int promoType = move[6];
        boolean promoted = promoType > 0;
        boolean doublePawnPush = move[7] == 1;

        Piece movingPiece = refBoard[fromRow][fromCol];
        Piece capturedPiece = refBoard[toRow][toCol];

        boolean movingPieceHadMoved = false;

        if (movingPiece.isRook()) movingPieceHadMoved = ((Rook) movingPiece).getHasMoved();
        if (movingPiece.isKing()) movingPieceHadMoved = ((King) movingPiece).getHasMoved();

        boolean castledRookHadMoved = false;

        // Castling Execution Logic
        if (castle) {
            int rookOriginalCol = (toCol == 6) ? 7 : 0;
            int rookTargetCol = (toCol == 6) ? 5 : 3;

            Piece rook = refBoard[fromRow][rookOriginalCol];

            if (rook != null){
                if (rook.isRook()) {
                    castledRookHadMoved = ((Rook) rook).getHasMoved();
                    ((Rook) rook).setHasMoved(true);
                    refBoard[fromRow][rookTargetCol] = rook;
                    refBoard[fromRow][rookOriginalCol] = null;
                }
            }
        }

        //EnPassant Execution Logic
        int dir = movingPiece.isWhite() ? 1 : -1;

        if (isIndexWithinBounds(toRow+dir,toCol)){
            if (enPassant){
                capturedPiece = refBoard[toRow+dir][toCol];
                refBoard[toRow+dir][toCol] = null;
            }
        }

        //chessboardLogic.setImmediateAction(false);//resetting

        //en passant available setting logic, must be after en passant execution logic
        if (doublePawnPush) {
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

        if (movingPiece.isPawn() && promoted) {

            int endRow = movingPiece.isWhite() ? 0 : 7;

            if (toRow == endRow) {

                Piece newPiece = null;

                switch (promoType){
                    case 1 -> newPiece = PieceFactory.createPiece(PieceType.KNIGHT, movingPiece.isWhite());
                    case 2 -> newPiece = PieceFactory.createPiece(PieceType.BISHOP, movingPiece.isWhite());
                    case 3 -> newPiece = PieceFactory.createPiece(PieceType.ROOK, movingPiece.isWhite());
                    case 4 -> newPiece = PieceFactory.createPiece(PieceType.QUEEN, movingPiece.isWhite());
                    default -> throw new IllegalArgumentException("Invalid promotion type: " + promoType+" in doMove() !");
                }

                char file = colToFile(toCol);
                int rank = rowToChessRow(endRow);

                chessboardLogic.insertPieceToBoard(newPiece, refBoard,file, rank);            }

        }

        chessboardLogic.setWhiteToMove(!chessboardLogic.isWhiteToMove());

        //chessboardLogic.checkGameOver();
        if (movingPiece.isPawn() && promoted)
            return new MoveState(movingPiece,capturedPiece,previousEPPawn,(Pawn) movingPiece,movingPieceHadMoved,castledRookHadMoved,prevImmediateActionState);
        else
            return new MoveState(movingPiece,capturedPiece,previousEPPawn,null,movingPieceHadMoved,castledRookHadMoved,prevImmediateActionState);

    }

    public static void undoMove(ChessboardLogic chessboardLogic,Piece[][] refBoard ,int encryptedMove,MoveState moveState) {

        Piece capturedPiece = moveState.capturedPiece;

        int[] move = decryptMove(encryptedMove);// fromRow, fromCol, toRow, toCol, enPassant, castle,
        // promotion, doublePawnPush, winningCaptureValue, check

        int fromRow = move[0];
        int fromCol = move[1];

        int toRow = move[2];
        int toCol = move[3];

        boolean enPassant = move[4] == 1 ;
        boolean castle = move[5] == 1;
        int promoType = move[6];
        boolean promoted = promoType > 0;
        boolean doublePawnPush = move[7] == 1;

        Piece movedPiece = moveState.movedPiece;

        // Castling undo Logic
        if (castle) {

            int rookOriginalCol = (toCol == 6) ? 7 : 0;
            int rookTargetCol = (toCol == 6) ? 5 : 3;

            Piece rook = refBoard[fromRow][rookTargetCol];
            if (rook != null){
                if (rook.isRook()) {

                    ((Rook) rook).setHasMoved(moveState.castledRookHadMoved);
                    refBoard[fromRow][rookOriginalCol] = rook;
                    refBoard[fromRow][rookTargetCol] = null;
                }
            }
        }

        //EnPassant undo Logic
        int dir = movedPiece.isWhite() ? 1 : -1;
        if (isIndexWithinBounds(toRow+dir,toCol)){
            if (enPassant && capturedPiece != null && capturedPiece.isPawn()) {
                ((Pawn) capturedPiece).setEnPassantVulnerable(true);
                refBoard[toRow + dir][toCol] = capturedPiece;
            }
        }

        //en passant available unsetting logic, must be after en passant undo logic
        if (doublePawnPush) {
            ((Pawn) movedPiece).setEnPassantVulnerable(false);
        }

        if (moveState.previousEPPawn != null) {
            moveState.previousEPPawn.setEnPassantVulnerable(true);
        }

        if (promoted) {

            int endRow = movedPiece.isWhite() ? 0 : 7;

            if (endRow == toRow){
                char file = colToFile(fromCol);
                int rank = rowToChessRow(fromRow);

                chessboardLogic.insertPieceToBoard(moveState.promotedPawn, chessboardLogic.getChessboard(),file, rank);
            }
        }

        if (!promoted) { //checked and set separately
            refBoard[fromRow][fromCol] = movedPiece;
        }

        if (enPassant){
            refBoard[toRow][toCol] = null;
        } else {
            refBoard[toRow][toCol] = capturedPiece;
        }


        if(movedPiece.isRook() && ((Rook) movedPiece).getHasMoved()){
            ((Rook) movedPiece).setHasMoved(moveState.movingPieceHadMoved);
        }

        if(movedPiece.isKing()){

            if (((King) movedPiece).getHasMoved()) {
                ((King) movedPiece).setHasMoved(moveState.movingPieceHadMoved);
            }

        }

        chessboardLogic.setImmediateAction(moveState.prevImmediateActionState);
        chessboardLogic.setWhiteToMove(!chessboardLogic.isWhiteToMove());
    }

    // simulateMoves() recursively counts the number of leaf positions reachable
    // at the given depth (standard perft logic).
    public int simulateMoves(ChessboardLogic chessboardLogic, int depth, boolean whiteToMove) {

        if (depth == 0) return 1;

        MoveList moveList = generateMoves(chessboardLogic,0);//ply doesn't matter when checking all possibilities
        int[] moves = moveList.moves;
        int moveCount = moveList.size;

        int numPositions = 0;

        Piece[][] refBoard = chessboardLogic.getChessboard();
        for (int i = 0; i < moveCount; i++) {

            MoveState moveState = doMove(chessboardLogic,refBoard,moves[i]);

            // flip turn ONLY in recursion
            numPositions += simulateMoves(
                    chessboardLogic,
                    depth - 1,
                    !whiteToMove
            );

            undoMove(chessboardLogic,refBoard ,moves[i], moveState);
        }

        return numPositions;
    }

    //convenient method to print perft(performance test) results
    //from depth 1 up to max depth
    public static void runPerftUpToDepth(ChessboardLogic chessboardLogic,int maxDepth){
        //position enumeration by depth

        MoveGenerator mg = new MoveGenerator();

        for (int i = 1; i<=maxDepth; i++){

            long start = System.nanoTime();//to measure the amount of time it to for a simulation

            long numPositions = mg.simulateMoves(chessboardLogic,i, chessboardLogic.isWhiteToMove());

            long end = System.nanoTime();
            long elapsedTimeNano = end - start;
            long elapsedTimeMilli = TimeUnit.NANOSECONDS.toMillis(elapsedTimeNano);


            //System.out.println("Depth : "+i+"   Number of positions : "+numPositions+"  Time(ms) : "+elapsedTimeMilli);
            System.out.printf("\nDepth : %2d    Number of Positions : %,15d      Time(ms) : %,9d",i,numPositions,elapsedTimeMilli);

        }

    }


}
