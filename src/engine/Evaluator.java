package engine;

import chessboard.ChessboardLogic;
import piecelogic.Piece;

public class Evaluator {
    static int pawnValue = 1;
    static int knightValue = 3;
    static int bishopValue = 3;
    static int rookValue = 5;
    static int queenValue = 9;

    public static int evaluvate(ChessboardLogic chessboardLogic) {
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

                if (piece != null && !piece.isKing() && piece.isWhite() ){

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

    public static int search(ChessboardLogic chessboardLogic,int depth){

        if (depth == 0) return evaluvate(chessboardLogic);

        MoveGenerator generator = new MoveGenerator();

        MoveList moveList = generator.generateMoves(chessboardLogic);
        int[] moves = moveList.moves;
        int moveCount = moveList.size;

        if (moveCount == 0){

            boolean player = chessboardLogic.isWhiteToMove();

            if (player == chessboardLogic.isKingInCheck(player, chessboardLogic.getChessboard())){
                return Integer.MIN_VALUE;
            }
            return 0;
        }

        int bestEval = Integer.MIN_VALUE;

        for (int i = 0; i < moveCount; i++){
            MoveState moveState = MoveGenerator.doMove(chessboardLogic,moves[i]);
            int evaluation = search(chessboardLogic,depth - 1);
            bestEval = Integer.max(evaluation,bestEval);
            MoveGenerator.undoMove(chessboardLogic,moves[i],moveState);

        }

        return bestEval;
    }
}
