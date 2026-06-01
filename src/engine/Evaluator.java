package engine;

import chessboard.ChessboardLogic;
import piecelogic.Piece;

import java.util.concurrent.TimeUnit;

import static engine.ChessBot.*;

public class Evaluator {


    static final int[] PIECE_VALUES = {82,337,365,477,1025};// 0=P, 1=N, 2=B, 3=R, 4=Q

    final static int[] PROMOTION_SCORES = {40000,30000,35000,60000};//0=N, 1=B, 2=R, 3=Q

    static final int DRAW_SCORE = 0;

    //tables to assign points to piece locations depending on the game stage
    protected static final int[][] middlePst = new int[6][64];
    protected static final int[][] endPst = new int[6][64];
    // 0 -> pawn, 1 -> knight, 2 -> bishop, 3 -> rook, 4 -> queen, 5 -> king

    protected static int[][] killerMoves = new int[64][2];//[ply][moves]

    public static void initPst(){
        // Pawn
        middlePst[0] = new int[]{
                0,   0,   0,   0,   0,   0,   0,   0,
                98, 134,  61,  95,  68, 126,  34, -11,
                -6,   7,  26,  31,  65,  56,  25, -20,
                -14,  13,   6,  21,  23,  12,  17, -23,
                -27,  -2,  -5,  12,  17,   6,  10, -25,
                -26,  -4,  -4, -10,   3,   3,  33, -12,
                -35,  -1, -20, -23, -15,  24,  38, -22,
                0,   0,   0,   0,   0,   0,   0,   0
        };

        endPst[0] = new int[]{
                0,   0,   0,   0,   0,   0,   0,   0,
                178, 173, 158, 134, 147, 132, 165, 187,
                94, 100,  85,  67,  56,  53,  82,  84,
                32,  24,  13,   5,  -2,   4,  17,  17,
                13,   9,  -3,  -7,  -7,  -8,   3,  -1,
                4,   7,  -6,   1,   0,  -5,  -1,  -8,
                13,   8,   8,  10,  13,   0,   2,  -7,
                0,   0,   0,   0,   0,   0,   0,   0
        };

        // Knight
        middlePst[1] = new int[]{
                -167, -89, -34, -49,  61, -97, -15,-107,
                -73, -41,  72,  36,  23,  62,   7, -17,
                -47,  60,  37,  65,  84, 129,  73,  44,
                -9,  17,  19,  53,  37,  69,  18,  22,
                -13,   4,  16,  13,  28,  19,  21,  -8,
                -23,  -9,  12,  10,  19,  17,  25, -16,
                -29, -53, -12,  -3,  -1,  18, -14, -19,
                -105, -21, -58, -33, -17, -28, -19, -23
        };

        endPst[1] = new int[]{
                -58, -38, -13, -28, -31, -27, -63, -99,
                -25,  -8, -25,  -2,  -9, -25, -24, -52,
                -24, -20,  10,   9,  -1,  -9, -19, -41,
                -17,   3,  22,  22,  22,  11,   8, -18,
                -18,  -6,  16,  25,  16,  17,   4, -18,
                -23,  -3,  -1,  15,  10,  -3, -20, -22,
                -42, -20, -10,  -5,  -2, -20, -23, -44,
                -29, -51, -23, -15, -22, -18, -50, -64
        };

        // Bishop
        middlePst[2] = new int[]{
                -29,   4, -82, -37, -25, -42,   7,  -8,
                -26,  16, -18, -13,  30,  59,  18, -47,
                -16,  37,  43,  40,  35,  50,  37,  -2,
                -4,   5,  19,  50,  37,  37,   7,  -2,
                -6,  13,  13,  26,  34,  12,  10,   4,
                0,  15,  15,  15,  14,  27,  18,  10,
                4,  15,  16,   0,   7,  21,  33,   1,
                -33,  -3, -14, -21, -13, -12, -39, -21
        };

        endPst[2] = new int[]{
                -14, -21, -11,  -8,  -7,  -9, -17, -24,
                -8,  -4,   7, -12,  -3, -13,  -4, -14,
                2,  -8,   0,  -1,  -2,   6,   0,   4,
                -3,   9,  12,   9,  14,  10,   3,   2,
                -6,   3,  13,  19,   7,  10,  -3,  -9,
                -12,  -3,   8,  10,  13,   3,  -7, -15,
                -14, -18,  -7,  -1,   4,  -9, -15, -27,
                -23,  -9, -23,  -5,  -9, -16,  -5, -17
        };

        // Rook
        middlePst[3] = new int[]{
                32,  42,  32,  51,  63,   9,  31,  43,
                27,  32,  58,  62,  80,  67,  26,  44,
                -5,  19,  26,  36,  17,  45,  61,  16,
                -24, -11,   7,  26,  24,  35,  -8, -20,
                -36, -26, -12,  -1,   9,  -7,   6, -23,
                -45, -25, -16, -17,   3,   0,  -5, -33,
                -44, -16, -20,  -9,  -1,  11,  -6, -71,
                -19, -13,   1,  17,  16,   7, -37, -26
        };

        endPst[3] = new int[]{
                13,  10,  18,  15,  12,  12,   8,   5,
                11,  13,  13,  11,  -3,   3,   8,   3,
                7,   7,   7,   5,   4,  -3,  -5,  -3,
                4,   3,  13,   1,   2,   1,  -1,   2,
                3,   5,   8,   4,  -5,  -6,  -8, -11,
                -4,   0,  -5,  -1,  -7, -12,  -8, -16,
                -6,  -6,   0,   2,  -9,  -9, -11,  -3,
                -9,   2,   3,  -1,  -5, -13,   4, -20
        };

        // Queen
        middlePst[4] = new int[]{
                -28,   0,  29,  12,  59,  44,  43,  45,
                -24, -39,  -5,   1, -16,  57,  28,  54,
                -13, -17,   7,   8,  29,  56,  47,  57,
                -27, -27, -16, -16,  -1,  17,  -2,   1,
                -9, -26,  -9, -10,  -2,  -4,   3,  -3,
                -14,   2, -11,  -2,  -5,   2,  14,   5,
                -35,  -8,  11,   2,   8,  15,  -3,   1,
                -1, -18,  -9,  10, -15, -25, -31, -50
        };

        endPst[4] = new int[]{
                -9,  22,  22,  27,  27,  19,  10,  20,
                -17,  20,  32,  41,  58,  25,  30,   0,
                -20,   6,   9,  49,  47,  35,  19,   9,
                3,  22,  24,  45,  57,  40,  57,  36,
                -18,  28,  19,  47,  31,  34,  39,  23,
                -16, -27,  15,   6,   9,  17,  10,   5,
                -22, -23, -30, -16, -16, -23, -36, -32,
                -33, -28, -22, -43,  -5, -32, -20, -41
        };

        // King
        middlePst[5] = new int[]{
                -65,  23,  16, -15, -56, -34,   2,  13,
                29,  -1, -20,  -7,  -8,  -4, -38, -29,
                -9,  24,   2, -16, -20,   6,  22, -22,
                -17, -20, -12, -27, -30, -25, -14, -36,
                -49,  -1, -27, -39, -46, -44, -33, -51,
                -14, -14, -22, -46, -44, -30, -15, -27,
                1,   7,  -8, -64, -43, -16,   9,   8,
                -15,  36,  12, -54,   8, -28,  24,  14
        };

        endPst[5] = new int[]{
                -74, -35, -18, -18, -11,  15,   4, -17,
                -12,  17,  14,  17,  17,  38,  23,  11,
                10,  17,  23,  15,  20,  45,  44,  13,
                -8,  22,  24,  27,  26,  33,  26,   3,
                -18,  -4,  21,  24,  27,  23,   9, -11,
                -19,  -3,  11,  21,  23,  16,   7,  -9,
                -27, -11,   4,  13,  14,   4,  -5, -17,
                -53, -34, -21, -11, -28, -14, -24, -43
        };
    }

    public static int evaluate(ChessboardLogic chessboardLogic,int phase) {
        Piece[][] chessboard = chessboardLogic.getChessboard();

        int whiteEval = countMaterial(chessboard,true,phase);
        int blackEval = countMaterial(chessboard, false,phase);

        int evaluation = whiteEval - blackEval;
        int colour = chessboardLogic.isWhiteToMove() ? 1 : -1;
        return colour * evaluation;

    }

    public static int countMaterial(Piece[][] chessboard, boolean isWhite,int phase){

        int score = 0;

        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){

                Piece piece = chessboard[row][col];

                if (piece != null && !piece.isKing() && piece.isWhite() == isWhite ){

                    int index = 0;

                    switch (piece.getPieceType()){
                        case PAWN -> { score += PIECE_VALUES[0]; ; index = 0; }
                        case KNIGHT -> { score += PIECE_VALUES[1]; ; index = 1; }
                        case BISHOP -> { score += PIECE_VALUES[2]; ; index = 2; }
                        case ROOK -> { score += PIECE_VALUES[3]; ; index = 3; }
                        case QUEEN -> { score += PIECE_VALUES[4]; index = 4; }
                        case KING -> index = 5;
                    }

                    int square = row * 8 + col;
                    square ^= isWhite ? 0 : 56; // 56 flips the board vertically for black

                    int mgScore = middlePst[index][square];
                    int egScore = endPst[index][square];

                    int positionalScore = (mgScore * phase + egScore * (24 - phase)) / 24;
                    score += positionalScore;

                }
            }
        }

        return score;
    }

    private static int[] orderedNegamaxPrune(ChessboardLogic chessboardLogic, int depth, int alpha, int beta,int ply,long hash){

        if (depth == 0) {

            int phase = calculateOwnPhase(chessboardLogic, chessboardLogic.isWhiteToMove())
                    + calculateOwnPhase(chessboardLogic,!chessboardLogic.isWhiteToMove());
            int staticScore = evaluate(chessboardLogic,phase);

            return new int[]{staticScore, 0, 1};
            //return quiescenceSearch(chessboardLogic,alpha,beta,ply,hash);
            //quiescenceSearch slows down the responses too much
        }

        MoveList moveList = MoveGenerator.generateMoves(chessboardLogic,ply);

        int[] moves = moveList.moves;
        int[] scores = moveList.scores;
        int moveCount = moveList.size;

        if (moveCount == 0) {
            if (ChessboardLogic.isKingInCheck(chessboardLogic.isWhiteToMove(),chessboardLogic.getChessboard())){
                // By subtracting depth, the engine favors slower mates when losing, and faster mates when winning!
                return new int[]{-100000 + depth, 0, 1};            }
            return new int[]{0, 0, 1};
        }

        int bestScore = -100000;
        int bestMove = 0;
        int numPositions = 0;

        for (int i = 0; i < moveCount; i++){

            int bestIndex = i;

            // find bestScore move in remaining list
            for (int j = i+1; j < moveCount; j++){

                if (scores[j] > scores[bestIndex]){ // comparing winning capture value
                    bestIndex = j;
                }

            }

            swap(moves,i,bestIndex);
            swap(scores,i,bestIndex);

            int move = moves[i];

            MovePositionState movePositionState = doMoveAndUpdate(chessboardLogic,moves[i],hash);
            long newHash = movePositionState.position.hash;

            if (movePositionState.seenCount >= 1){//check for the 3 time repetition

                ChessBot.rollbackRepetitionMap(newHash);
                MoveGenerator.undoMove(chessboardLogic,move,movePositionState.moveState);

                int score = DRAW_SCORE;


                if (score > bestScore) {
                    bestScore = score;
                    bestMove = moves[i];
                }

                alpha = Math.max(alpha, score);
                if (alpha >= beta) {
                    boolean isCapture = ((move >> 12) & 1) == 1;
                    boolean isPromotion = ((move >> 16) & 7) > 0;

                    // Only store QUIET moves as killer moves
                    if (!isCapture && !isPromotion) {

                        // Shift the primary killer move to the secondary slot
                        // and store the new move in the primary slot.
                        if (killerMoves[ply][0] != move) { // Prevent duplicates
                            killerMoves[ply][1] = killerMoves[ply][0];
                            killerMoves[ply][0] = move;
                        }
                    }
                    break;
                }
                continue;


            }

            //in negamax the player's score = - (opponent score)
            //so when recursively calling the negamax function the alpha and the beta values are multiplied by -1 and
            //entered into the function with interchanged positions
            int[] negamaxPrune = orderedNegamaxPrune(chessboardLogic,depth - 1,-beta,-alpha,ply+1,newHash);

            int score = -negamaxPrune[0];

            if (score > bestScore) {

                bestScore = score;
                bestMove = moves[i]; // moves[i] is the currently evaluated move
            }

            alpha = Math.max(alpha,score);

            ChessBot.rollbackRepetitionMap(newHash);
            MoveGenerator.undoMove(chessboardLogic,move,movePositionState.moveState);

            numPositions += negamaxPrune[2];

            if (alpha >= beta) {
                boolean isCapture = ((move >> 12) & 1) == 1;
                boolean isPromotion = ((move >> 16) & 7) > 0;

                // Only store QUIET moves as killer moves
                if (!isCapture && !isPromotion) {

                    // Shift the primary killer move to the secondary slot
                    // and store the new move in the primary slot.
                    if (killerMoves[ply][0] != move) { // Prevent duplicates
                        killerMoves[ply][1] = killerMoves[ply][0];
                        killerMoves[ply][0] = move;
                    }
                }
                break; // PRUNE
            }
        }
        return new int[]{bestScore,bestMove,numPositions};

    }

    protected static int[] quiescenceSearch(ChessboardLogic chessboardLogic,int alpha, int beta,int ply,long hash){
        boolean isWhiteToMove = chessboardLogic.isWhiteToMove();

        int phase = calculateOwnPhase(chessboardLogic,isWhiteToMove) + calculateOwnPhase(chessboardLogic,!isWhiteToMove);
        int eval = evaluate(chessboardLogic,phase);
        int bestMove = 0;

        boolean inCheck = ChessboardLogic.isKingInCheck(chessboardLogic.isWhiteToMove(), chessboardLogic.getChessboard());
        int bestScore = -10000;

        if (!inCheck) {
            bestScore = eval;
            if (eval >= beta) {
                return new int[]{eval, bestMove, -1};
            }
            if (eval > alpha) {
                alpha = eval;
            }
        }

        MoveList moveList;
        if (inCheck) {
            moveList = MoveGenerator.generateMoves(chessboardLogic,ply);//GENERATE ALL MOVES IF IN CHECK
        } else {
            moveList = MoveGenerator.generateCaptures(chessboardLogic,ply);
        }

        if (!inCheck && eval > alpha){//best score we can guarantee
            alpha = eval;
        }

        int[] moves = moveList.moves;
        int[] scores = moveList.scores;
        int moveCount = moveList.size;

        if (moveCount == 0) {
            return new int[]{inCheck ? -100000 + ply : bestScore, bestMove, -1};//returns a higher score for a slower checkmate
        }

        for (int i = 0; i < moveCount; i++){

            if (moves[i] == 0) {
                continue;
            }

            int bestIndex = i;

            // find bestScore move in remaining list
            for (int j = i+1; j < moveCount; j++){

                if (scores[j] > scores[bestIndex]){ // comparing winning capture value
                    bestIndex = j;
                }

            }

            swap(moves,i,bestIndex);
            swap(scores,i,bestIndex);

            MovePositionState movePositionState = doMoveAndUpdate(chessboardLogic,moves[i],hash);

            int[] quiescenceSearch = quiescenceSearch(chessboardLogic,-beta,-alpha,ply+1,movePositionState.position.hash);
            int score = -quiescenceSearch[0];

            ChessBot.rollbackRepetitionMap(movePositionState.position.hash);
            MoveGenerator.undoMove(chessboardLogic,moves[i],movePositionState.moveState);

            if (score > bestScore){
                bestScore = score;
                bestMove = moves[i];

            }

            if (score >= beta){
                return new int[]{score,bestMove,-1};
            }

            if (score > alpha){
                alpha = score;
                bestMove = moves[i];
            }
        }
        return new int[]{bestScore,bestMove,-1};
    }

    protected static void orderedNegamaxPruner(ChessboardLogic chessboardLogic, int depth, int alpha, int beta,int ply,long hash){
        long start = System.nanoTime();
        int[] negamaxPrune = orderedNegamaxPrune(chessboardLogic,depth,alpha,beta,ply,hash);
        long end = System.nanoTime();

        long elapsedTimeNano = end - start;
        long elapsedTimeMilli = TimeUnit.NANOSECONDS.toMillis(elapsedTimeNano);

        double inSeconds = elapsedTimeMilli > 1000 ? elapsedTimeMilli /1000.0 : 0;
        String colour = chessboardLogic.isWhiteToMove() ? "White" : "Black";

        //System.out.printf("For : %s Depth : %2d    Number of Positions : %,15d      Time : %,9dms (%,.2fs)    Evaluation : %,5d\n",colour,depth, negamaxPrune[1],elapsedTimeMilli,inSeconds, negamaxPrune[2]);
        System.out.printf("For : %s Depth : %2d    Number of Positions : %,15d      Time : %,9dms (%,.2fs)\n",colour,depth, negamaxPrune[1],elapsedTimeMilli,inSeconds);
        int[] decryptedMove = ChessboardLogic.decryptMove(negamaxPrune[1]);// fromRow, fromCol, toRow, toCol, enPassant, castle,
        // promotion, doublePawnPush, winningCaptureValue, check

        int fromChessRow = ChessboardLogic.rowToChessRow(decryptedMove[0]);
        char fromFile = ChessboardLogic.colToFile(decryptedMove[1]);

        int toChessRow = ChessboardLogic.rowToChessRow(decryptedMove[2]);
        char toFile = ChessboardLogic.colToFile(decryptedMove[3]);

        Piece piece = chessboardLogic.getChessboard()[decryptedMove[0]][decryptedMove[1]];

        System.out.printf("Best move : %s ( %1c%1d -> %1c%1d )\n",piece,fromFile,fromChessRow,toFile,toChessRow);

        doMoveAndUpdate(chessboardLogic,negamaxPrune[1],hash);
    }

    public static void swap(int[] moves, int i, int bestIndex ){
        int temp = moves[i];
        moves[i] = moves[bestIndex];
        moves[bestIndex] = temp;
    }

    //wrapper method for doing a move in negamax search
    private static MovePositionState doMoveAndUpdate(ChessboardLogic chessboardLogic, int encryptedMove,long hash){
        Position position = updateHash(chessboardLogic,encryptedMove,hash);
        MoveState moveState = MoveGenerator.doMove(chessboardLogic,encryptedMove);
        int seenCount = repetition.getOrDefault(position.hash, 0);
        repetition.put(position.hash, seenCount +1);//update seenCount in map

        return new MovePositionState(moveState,position, seenCount);
    }

    public static int calculateOwnPhase(ChessboardLogic chessboardLogic,boolean isWhiteToMove){

        Piece[][] refBoard = chessboardLogic.getChessboard();
        int ownPhase = 0;

        for (int r = 0; r < 8; r++){
            for (int c = 0; c < 8; c++){
                Piece piece = refBoard[r][c];
                if (piece == null )
                    continue;

                if (piece.isWhite() != isWhiteToMove)
                    continue;

                switch (piece.getPieceType()){
                    case QUEEN -> ownPhase += 4;
                    case ROOK -> ownPhase += 2;
                    case BISHOP, KNIGHT -> ownPhase += 1;
                }
            }
        }
        return ownPhase;
    }

    //develop this method to reduce scanned positions
    protected static int scoreMove(int move, int moverPhase, int enemyPhase,int friendlyKingSquare,int enemyKingSquare ,int ply,
                                   boolean isFromSquareAttacked, boolean isToSquareAttacked, boolean isFromSquareProtected, boolean isToSquareProtected){

        int piece = (move >> 25) & 7;
        int index = piece - 1;// 0=P, 1=N, 2=B, 3=R, 4=Q, 5=K

        boolean colour = ((move >> 28) & 1) == 1;
        boolean check = ((move >> 24) & 1) == 1;

        int toSquare = ((move >> 6) & 63) ^ (colour ? 0 : 56);
        int fromSquare = (move & 63) ^ (colour ? 0 : 56);
        //56 flips the board vertically
        // allowing the same piece square table to be used for both colours

        //to get a better evaluation on the move's importance
        int mgScore = middlePst[index][toSquare] - middlePst[index][fromSquare];
        int egScore = endPst[index][toSquare] - endPst[index][fromSquare];

        int phase = moverPhase + enemyPhase;

        int score = ( mgScore * phase + egScore * (24 - phase)) / 24;

        score += ((move >> 15) & 1) == 1 ? 3500 : 0;//for a castle

        if (index < 5){
            int pieceVal = PIECE_VALUES[index];
            boolean isQueen = index == 4;
            if (index > 0) { // Non-pawns
                if (isFromSquareAttacked) score += pieceVal * (isQueen ? 6 : 4);
                if (isFromSquareProtected) score -= pieceVal * (isQueen ? 1 : 3);
                if (isToSquareAttacked) score -= pieceVal * (isQueen ? 6 : 4);
            }
            if (isToSquareProtected) score += pieceVal * (isQueen ? 3 : 2); // Includes pawns
        }


        int phaseDifference = moverPhase - enemyPhase;
        boolean largeAdvantage = phaseDifference >= 6;

        if (check) {

            if (isToSquareProtected && phaseDifference > 0 ) {
                // Define large advantage

                int distanceToEnemyKing = distanceToKing(enemyKingSquare, toSquare);

                // Max distance on a board is 14.
                int closingInBonus = (14 - distanceToEnemyKing) ;

                int checkingPieceVal = index < 5 ? PIECE_VALUES[index] : 0;

                // Scale scores to forcefully override killer moves and captures
                score += largeAdvantage ? (30000 + checkingPieceVal * 5 + (phaseDifference * 2000))
                                        : (15000 + checkingPieceVal * 2 + (phaseDifference * 1000));

                int pieceLethality = index < 5 ? PIECE_VALUES[index] / 100 : 1;
                int closingInAdvantage = closingInBonus * 100 * pieceLethality;
                score += closingInAdvantage;

            } else {
                score += 500; // Standard score for unprotected or disadvantageous checks
            }
        }

        boolean isCapture = (move >> 12 & 1) == 1;

        if (isCapture){ // if capture
            int winningCaptureValue = (move >> 19) & 31;

            if (winningCaptureValue >= 11)//better captures

                score += 10000 + (winningCaptureValue * 1000);

            else //losing trades e.g. Queen takes a pawn
                score += isToSquareAttacked ? -25000 : 10000 + (winningCaptureValue * 1000);

        }else {
            // Only apply Promotion OR Killer bonuses to non-captures
            int promotionIndex = ((move >> 16) & 7) - 1;

            if (promotionIndex >= 0 && promotionIndex < 4) {
                score += PROMOTION_SCORES[promotionIndex]; // 50,000 to 80,000 tier
            } else {
                // Killer moves strictly bound to the 40,000+ tier
                if (move == killerMoves[ply][0]) score += 26000;
                else if (move == killerMoves[ply][1]) score += 24000;
            }
        }

        boolean isEndgame = (moverPhase + enemyPhase) < 12 || enemyPhase < 5;

        if (isEndgame && largeAdvantage ){

            // Bring the Friendly King Closer to Assist
            //Some checkmates king to cut off escape squares.
            int kingToKingDistance = distanceToKing(enemyKingSquare,friendlyKingSquare);
            int mopUpScore = ((14 - kingToKingDistance) * 10000);

            score += mopUpScore;
        }


        //add more logic later
        return score;
    }

    private static int distanceToKing(int enemyKingSquare, int toSquare) {
        int toCol = toSquare % 8;
        int toRow = toSquare / 8;

        int enemyKingRow = enemyKingSquare / 8;
        int enemyKingCol = enemyKingSquare % 8;

        int rowDiff = Math.abs(toRow - enemyKingRow);
        int colDiff = Math.abs(toCol - enemyKingCol);

        // Calculate Manhattan Distance (Ranges from 0 to 14)
        // Lower distance means the piece is closer to the king
        return rowDiff + colDiff;
    }

}
