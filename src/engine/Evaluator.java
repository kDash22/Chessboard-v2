package engine;

import chessboard.ChessboardLogic;
import piecelogic.Piece;

import java.util.concurrent.TimeUnit;

import static engine.ChessBot.*;

public class Evaluator {
    static int pawnValue = 82;
    static int knightValue = 337;
    static int bishopValue = 365;
    static int rookValue = 477;
    static int queenValue = 1025;
    static final int DRAW_SCORE = 0;

    //tables to assign points to piece locations depending on the game stage
    protected static final int[][] middlePst = new int[6][64];
    protected static final int[][] endPst = new int[6][64];
    // 0 -> pawn, 1 -> knight, 2 -> bishop, 3 -> rook, 4 -> queen, 5 -> king

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
                        case PAWN -> { score += pawnValue ; index = 0; }
                        case KNIGHT -> { score += knightValue ; index = 1; }
                        case BISHOP -> { score += bishopValue ; index = 2; }
                        case ROOK -> { score += rookValue ; index = 3; }
                        case QUEEN -> { score += queenValue; index = 4; }
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

    private static int[] orderedNegamaxPrune(ChessboardLogic chessboardLogic, int depth, int alpha, int beta,long hash){

        if (depth == 0) {
            return quiescenceSearch(chessboardLogic,alpha,beta,hash);
        }

        MoveList moveList = MoveGenerator.generateMoves(chessboardLogic);

        int[] moves = moveList.moves;
        int[] scores = moveList.scores;
        int moveCount = moveList.size;

        if (moveCount == 0) {
            if (ChessboardLogic.isKingInCheck(chessboardLogic.isWhiteToMove(),chessboardLogic.getChessboard())){
                return new int[]{-100000, 0, 1};
            }
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

            MovePositionState movePositionState = doMoveAndUpdate(chessboardLogic,moves[i],hash);
            long newHash = movePositionState.position.hash;

            if (movePositionState.seenCount >= 2){//check for the 3 time repetition

                undoMoveAndRollback(chessboardLogic,moves[i],movePositionState,newHash);

                int score = DRAW_SCORE;


                if (score > bestScore) {
                    bestScore = score;
                    bestMove = moves[i];
                }

                alpha = Math.max(alpha, score);
                if (alpha >= beta) break;
                continue;


            }

            //in negamax the player's score = - (opponent score)
            //so when recursively calling the negamax function the alpha and the beta values are multiplied by -1 and
            //entered into the function with interchanged positions
            int[] negamaxPrune = orderedNegamaxPrune(chessboardLogic,depth - 1,-beta,-alpha,newHash);

            int score = -negamaxPrune[0];

            if (score > bestScore) {

                bestScore = score;
                bestMove = moves[i]; // moves[i] is the currently evaluated move
            }

            alpha = Math.max(alpha,score);

            undoMoveAndRollback(chessboardLogic,moves[i],movePositionState,hash);

            numPositions += negamaxPrune[2];

            if (alpha >= beta) {
                break; // PRUNE
            }
        }
        return new int[]{bestScore,bestMove,numPositions};

    }

    protected static int[] quiescenceSearch(ChessboardLogic chessboardLogic,int alpha, int beta,long hash){

        int phase = calculatePhase(chessboardLogic);
        int eval = evaluate(chessboardLogic,phase);
        int bestMove = 0;

        if (eval >= beta){//opponent will never make that move
            return new int[]{eval,bestMove,-1};
        }

        if (eval > alpha){//best score we can guarantee
            alpha = eval;
        }

        MoveList moveList = MoveGenerator.generateCaptures(chessboardLogic);
        int[] moves = moveList.moves;
        int[] scores = moveList.scores;
        int moveCount = moveList.size;

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

            MovePositionState movePositionState = doMoveAndUpdate(chessboardLogic,moves[i],hash);

            int[] quiescenceSearch = quiescenceSearch(chessboardLogic,-beta,-alpha,movePositionState.position.hash);
            int score = -quiescenceSearch[0];

            undoMoveAndRollback(chessboardLogic,moves[i],movePositionState,movePositionState.position.hash);

            if (score >= beta){
                return new int[]{score,bestMove,-1};


            }

            if (score > alpha){
                alpha = score;
                bestMove = moves[i];
            }
        }
        return new int[]{alpha,bestMove,-1};
    }

    protected static void orderedNegamaxPruner(ChessboardLogic chessboardLogic, int depth, int alpha, int beta,long hash){
        long start = System.nanoTime();
        int[] negamaxPrune = orderedNegamaxPrune(chessboardLogic,depth,alpha,beta,hash);
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

        System.out.printf("Best move : %s ( %1c%1d -> %1c%1d \n)",piece,fromFile,fromChessRow,toFile,toChessRow);

        ChessBot.updateHash(chessboardLogic,negamaxPrune[1],hash);
        doMoveAndUpdate(chessboardLogic,negamaxPrune[1],hash);//for testing against other bots
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

    //wrapper method for undoing a move in negamax search
    private static void undoMoveAndRollback(ChessboardLogic chessboardLogic,int encryptedMove, MovePositionState movePositionState,long newHash){
        rollbackHash(chessboardLogic,encryptedMove,movePositionState.position,newHash);
        MoveGenerator.undoMove(chessboardLogic,encryptedMove,movePositionState.moveState);

        // Safely revert the repetition count
        if (movePositionState.seenCount == 0) {
            repetition.remove(newHash);
        } else {
            repetition.put(newHash, movePositionState.seenCount);
        }
    }

    public static int calculatePhase(ChessboardLogic chessboardLogic){

        Piece[][] refBoard = chessboardLogic.getChessboard();
        int phase = 0;

        for (int r = 0; r < 8; r++){
            for (int c = 0; c < 8; c++){
                Piece piece = refBoard[r][c];
                if (piece == null )
                    continue;

                switch (piece.getPieceType()){
                    case QUEEN -> phase += 4;
                    case ROOK -> phase += 2;
                    case BISHOP, KNIGHT -> phase += 1;
                }
            }
        }

        return phase;
    }
}
