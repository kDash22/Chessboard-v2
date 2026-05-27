package engine;

import chessboard.ChessboardLogic;
import piecelogic.Piece;

import java.util.concurrent.TimeUnit;

public class Evaluator {
    static int pawnValue = 1;
    static int knightValue = 3;
    static int bishopValue = 3;
    static int rookValue = 5;
    static int queenValue = 9;

    public static int evaluate(ChessboardLogic chessboardLogic) {
        Piece[][] chessboard = chessboardLogic.getChessboard();

        int whiteEval = countMaterial(chessboard,true);
        int blackEval = countMaterial(chessboard, false);

        int evaluation = whiteEval - blackEval;
        int colour = chessboardLogic.isWhiteToMove() ? 1 : -1;
        return colour * evaluation;

    }

    public static int countMaterial(Piece[][] chessboard, boolean isWhite){

        int material = 0;

        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){

                Piece piece = chessboard[row][col];

                if (piece != null && !piece.isKing() && piece.isWhite() == isWhite ){

                     switch (piece.getPieceType()){
                        case PAWN -> material += pawnValue;
                        case KNIGHT -> material += knightValue;
                        case BISHOP -> material += bishopValue;
                        case ROOK -> material += rookValue;
                        case QUEEN -> material += queenValue;
                    }

                }
            }
        }

        return material;
    }

    private static int[] negamax(ChessboardLogic chessboardLogic, int depth){

        if (depth == 0) {
            return new int[]{evaluate(chessboardLogic), 1};
        }

        MoveGenerator generator = new MoveGenerator();


        MoveList moveList = generator.generateMoves(chessboardLogic);
        int[] moves = moveList.moves;
        int moveCount = moveList.size;

        if (moveCount == 0) {
            if (chessboardLogic.isKingInCheck(chessboardLogic.isWhiteToMove(),chessboardLogic.getChessboard())){
                return new int[]{Integer.MIN_VALUE,1};
            }
            return new int[]{evaluate(chessboardLogic), 1};
        }

        int best = Integer.MIN_VALUE;
        int numPositions = 0;

        for (int i = 0; i < moveCount; i++){

            MoveState moveState = MoveGenerator.doMove(chessboardLogic,moves[i]);
            int[] negamax = negamax(chessboardLogic,depth - 1);

            int score = -negamax[0];
            best = Math.max(best,score);

            MoveGenerator.undoMove(chessboardLogic,moves[i],moveState);

            numPositions += negamax[1];
        }
        return new int[]{best,numPositions};
    }

    private static int[] minimax(ChessboardLogic chessboardLogic, int depth, boolean maximizingPlayer){
        //maximizing player is white and minimizing player is black

        if (depth == 0)
            return new int[]{evaluate(chessboardLogic),1};


        MoveGenerator generator = new MoveGenerator();


        MoveList moveList = generator.generateMoves(chessboardLogic);
        int[] moves = moveList.moves;
        int moveCount = moveList.size;

        if (moveCount == 0) {
            if (chessboardLogic.isKingInCheck(chessboardLogic.isWhiteToMove(),chessboardLogic.getChessboard())){
                return new int[]{Integer.MIN_VALUE,1};
            }
            return new int[]{evaluate(chessboardLogic), 1};
        }

        if (maximizingPlayer){
            int maxEval = Integer.MIN_VALUE;
            int numPositions = 0;

            for (int i = 0; i < moveCount; i++){
                MoveState moveState = MoveGenerator.doMove(chessboardLogic,moves[i]);
                int[] minimax = minimax(chessboardLogic,depth - 1,false);
                int evaluation = minimax[0];
                numPositions += minimax[1];
                maxEval = Integer.max(evaluation,maxEval);
                MoveGenerator.undoMove(chessboardLogic,moves[i],moveState);
            }
            return new int[]{maxEval,numPositions};

        } else {
            int minValue = Integer.MAX_VALUE;
            int numPositions = 0;

            for (int i = 0; i < moveCount; i++){
                MoveState moveState = MoveGenerator.doMove(chessboardLogic,moves[i]);
                int[] minimax = minimax(chessboardLogic,depth - 1,true);
                int evaluation = minimax[0];
                numPositions += minimax[1];
                minValue = Integer.min(evaluation,minValue);
                MoveGenerator.undoMove(chessboardLogic,moves[i],moveState);
            }
            return new int[]{minValue,numPositions};
        }
    }

    private static int[] negamaxPrune(ChessboardLogic chessboardLogic, int depth, int alpha, int beta){

        if (depth == 0) {
            return new int[]{evaluate(chessboardLogic), 1};
        }

        MoveGenerator generator = new MoveGenerator();
        MoveList moveList = generator.generateMoves(chessboardLogic);

        int[] moves = moveList.moves;
        int moveCount = moveList.size;

        if (moveCount == 0) {
            if (chessboardLogic.isKingInCheck(chessboardLogic.isWhiteToMove(),chessboardLogic.getChessboard())){
                return new int[]{Integer.MIN_VALUE,1};
            }
            return new int[]{evaluate(chessboardLogic), 1};
        }

        int best = Integer.MIN_VALUE;
        int numPositions = 0;

        for (int i = 0; i < moveCount; i++){

            MoveState moveState = MoveGenerator.doMove(chessboardLogic,moves[i]);
            int[] negamaxPrune = negamaxPrune(chessboardLogic,depth - 1,-beta,-alpha);

            int score = -negamaxPrune[0];
            best = Math.max(best,score);
            alpha = Math.max(alpha,score);

            MoveGenerator.undoMove(chessboardLogic,moves[i],moveState);

            numPositions += negamaxPrune[1];

            if (alpha >= beta) {
                break; // PRUNE
            }
        }
        return new int[]{best,numPositions};

    }

    private static int[] orderedNegamaxPrune(ChessboardLogic chessboardLogic, int depth, int alpha, int beta){

        if (depth == 0) {
            return new int[]{evaluate(chessboardLogic), 1};
        }

        MoveGenerator generator = new MoveGenerator();
        MoveList moveList = generator.generateMoves(chessboardLogic);

        int[] moves = moveList.moves;
        int[] scores = moveList.scores;
        int moveCount = moveList.size;

        if (moveCount == 0) {
            if (chessboardLogic.isKingInCheck(chessboardLogic.isWhiteToMove(),chessboardLogic.getChessboard())){
                return new int[]{Integer.MIN_VALUE,1};
            }
            return new int[]{evaluate(chessboardLogic), 1};
        }

        int best = Integer.MIN_VALUE;
        int numPositions = 0;

        for (int i = 0; i < moveCount; i++){

            int bestIndex = i;

            // find best move in remaining list
            for (int j = i+1; j < moveCount; j++){

                if (scores[j] == 0) // if not capture
                    continue;

                if (scores[j] > scores[bestIndex]){ // comparing winning capture value
                    bestIndex = j;
                }

            }

            swap(moves,i,bestIndex);
            swap(scores,i,bestIndex);

            MoveState moveState = MoveGenerator.doMove(chessboardLogic,moves[i]);
            int[] negamaxPrune = negamaxPrune(chessboardLogic,depth - 1,-beta,-alpha);

            int score = -negamaxPrune[0];
            best = Math.max(best,score);
            alpha = Math.max(alpha,score);

            MoveGenerator.undoMove(chessboardLogic,moves[i],moveState);

            numPositions += negamaxPrune[1];

            if (alpha >= beta) {
                break; // PRUNE
            }
        }
        return new int[]{best,numPositions};

    }

    public static void minimaxer(ChessboardLogic chessboardLogic, int depth, Boolean maximizingPlayer){

        long start = System.nanoTime();
        int[] minimax = minimax(chessboardLogic,depth,maximizingPlayer);
        long end = System.nanoTime();

        long elapsedTimeNano = end - start;
        long elapsedTimeMilli = TimeUnit.NANOSECONDS.toMillis(elapsedTimeNano);

        System.out.printf("\nDepth : %2d    Number of Positions : %,15d      Time(ms) : %,9d    Evaluation : %,5d",depth,minimax[1],elapsedTimeMilli,minimax[0]);

    }

    public static void negamaxer(ChessboardLogic chessboardLogic, int depth){
        long start = System.nanoTime();
        int[] negamax = negamax(chessboardLogic,depth);
        long end = System.nanoTime();

        long elapsedTimeNano = end - start;
        long elapsedTimeMilli = TimeUnit.NANOSECONDS.toMillis(elapsedTimeNano);

        double inSeconds = elapsedTimeMilli > 1000 ? elapsedTimeMilli /1000.0 : 0;
        String colour = chessboardLogic.isWhiteToMove() ? "White" : "Black";

        System.out.printf("For : %s Depth : %2d    Number of Positions : %,15d      Time : %,9dms (%,.2fs)    Evaluation : %,5d\n",colour,depth, negamax[1],elapsedTimeMilli,inSeconds, negamax[0]);
    }

    public static void negamaxPruner(ChessboardLogic chessboardLogic, int depth, int alpha, int beta){
        long start = System.nanoTime();
        int[] negamaxPrune = negamaxPrune(chessboardLogic,depth,alpha,beta);
        long end = System.nanoTime();

        long elapsedTimeNano = end - start;
        long elapsedTimeMilli = TimeUnit.NANOSECONDS.toMillis(elapsedTimeNano);

        double inSeconds = elapsedTimeMilli > 1000 ? elapsedTimeMilli /1000.0 : 0;
        String colour = chessboardLogic.isWhiteToMove() ? "White" : "Black";

        System.out.printf("For : %s Depth : %2d    Number of Positions : %,15d      Time : %,9dms (%,.2fs)    Evaluation : %,5d\n",colour,depth, negamaxPrune[1],elapsedTimeMilli,inSeconds, negamaxPrune[0]);
    }

    public static void oderedNegamaxPruner(ChessboardLogic chessboardLogic, int depth, int alpha, int beta){
        long start = System.nanoTime();
        int[] negamaxPrune = orderedNegamaxPrune(chessboardLogic,depth,alpha,beta);
        long end = System.nanoTime();

        long elapsedTimeNano = end - start;
        long elapsedTimeMilli = TimeUnit.NANOSECONDS.toMillis(elapsedTimeNano);

        double inSeconds = elapsedTimeMilli > 1000 ? elapsedTimeMilli /1000.0 : 0;
        String colour = chessboardLogic.isWhiteToMove() ? "White" : "Black";

        System.out.printf("For : %s Depth : %2d    Number of Positions : %,15d      Time : %,9dms (%,.2fs)    Evaluation : %,5d\n",colour,depth, negamaxPrune[1],elapsedTimeMilli,inSeconds, negamaxPrune[0]);
    }

    public static void swap(int[] moves, int i, int bestIndex ){
        int temp = moves[i];
        moves[i] = moves[bestIndex];
        moves[bestIndex] = temp;
    }

}
