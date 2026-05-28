package engine;

import chessboard.ChessboardLogic;
import piecelogic.Piece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

    protected static int[] orderedNegamaxPrune(ChessboardLogic chessboardLogic, int depth, int alpha, int beta){

        if (depth == 0) {
            return new int[]{evaluate(chessboardLogic), 0, 1};
        }

        MoveGenerator generator = new MoveGenerator();
        MoveList moveList = generator.generateMoves(chessboardLogic);

        int[] moves = moveList.moves;
        int[] scores = moveList.scores;
        int moveCount = moveList.size;

        if (moveCount == 0) {
            if (ChessboardLogic.isKingInCheck(chessboardLogic.isWhiteToMove(),chessboardLogic.getChessboard())){
                return new int[]{Integer.MIN_VALUE, 0, 1};
            }
            return new int[]{evaluate(chessboardLogic), 0, 1};
        }

        int bestScore = Integer.MIN_VALUE;
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

            MoveState moveState = MoveGenerator.doMove(chessboardLogic,moves[i]);

            //in negamax the player's score = - (opponent score)
            //so when recursively calling the negamax function the alpha and the beta values are multiplied by -1 and
            //entered into the function with interchanged positions
            int[] negamaxPrune = orderedNegamaxPrune(chessboardLogic,depth - 1,-beta,-alpha);

            int score = -negamaxPrune[0];

            if (score > bestScore) {

                bestScore = score;
                bestMove = moves[i]; // moves[i] is the currently evaluated move
            }

            alpha = Math.max(alpha,score);

            MoveGenerator.undoMove(chessboardLogic,moves[i],moveState);

            numPositions += negamaxPrune[2];

            if (alpha >= beta) {
                break; // PRUNE
            }
        }
        return new int[]{bestScore,bestMove,numPositions};

    }

    public static void orderedNegamaxPruner(ChessboardLogic chessboardLogic, int depth, int alpha, int beta){
        long start = System.nanoTime();
        int[] negamaxPrune = orderedNegamaxPrune(chessboardLogic,depth,alpha,beta);
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
        MoveGenerator.doMove(chessboardLogic,negamaxPrune[1]);//for testing against other bots
    }

    public static void swap(int[] moves, int i, int bestIndex ){
        int temp = moves[i];
        moves[i] = moves[bestIndex];
        moves[bestIndex] = temp;
    }



}
