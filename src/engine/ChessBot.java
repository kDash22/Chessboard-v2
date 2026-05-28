package engine;

import chessboard.ChessboardLogic;
import piecelogic.*;

import java.util.HashMap;
import java.util.Map;

import static chessboard.ChessboardLogic.decryptMove;

public class ChessBot {

    static Map<Long, Integer> repetition = new HashMap<>();
    static long hash ;

    private static final long[][] pieceSquare = new long[12][64];//[piece][square]
    // 0 - 5 white, 6-12 black
    private static final long[] sideKey = new long[2];//whose turn 0 -> white, 1 -> black
    private static final long[] castlingKey = new long[16];//castling states, 4 events, 2 ^ 4 combinations
    private static final long[] enPassantKey = new long[8];//which file can en passant happen on

    public void run(ChessboardLogic chessboardLogic, int depth){

        int[] negamax = Evaluator.orderedNegamaxPrune(chessboardLogic,depth,Integer.MIN_VALUE + 1,Integer.MAX_VALUE-1);
        int move = negamax[1];

    }

    static void fillTheTables(){
        long seed = 1;

        for (int p = 0; p < 12; p++){
            for (int sq = 0; sq < 64; sq++){
                seed = splitmix64(seed);
                pieceSquare[p][sq] = seed;
            }

        }

        for (int i = 0; i < 2; i++) {
            seed = splitmix64(seed);
            sideKey[i] = seed;
        }

        for (int i = 0; i < 16; i++) {
            seed = splitmix64(seed);
            castlingKey[i] = seed;
        }

        for (int i = 0; i < 8; i++) {
            seed = splitmix64(seed);
            enPassantKey[i] = seed;
        }
    }

    static void buildHash(ChessboardLogic chessboardLogic, int previousMove){

        Piece[][] refBoard = chessboardLogic.getChessboard();

        for (int r = 0; r < 8; r++){
            for (int c = 0; c < 8; c++){
                if (refBoard[r][c] != null){
                    int index = pieceIndex(refBoard[r][c]);
                    int square = r * 8 + c;
                    hash ^= pieceSquare[index][square];
                }
            }
        }

        hash ^= chessboardLogic.isWhiteToMove() ? sideKey[0] : sideKey[1];
        hash ^= castlingKey[castlingIndex(refBoard)];
        hash ^= enPassantKey[enpassantCol(refBoard)];

    }

    //imported from the internet
    static long splitmix64(long x) {

        x += 0x9e3779b97f4a7c15L;
        x = (x ^ (x >> 30)) * 0xbf58476d1ce4e5b9L;
        x = (x ^ (x >> 27)) * 0x94d049bb133111ebL;
        return x ^ (x >> 31);
    }

    static int pieceIndex(Piece piece){
        int index = -1;

        switch (piece.getPieceType()){
            case PAWN -> index = 0;
            case KNIGHT -> index = 1;
            case BISHOP -> index = 2;
            case ROOK -> index = 3;
            case QUEEN -> index = 4;
            case KING -> index = 5;
            default -> throw new IllegalArgumentException("Error at pieceIndex() in ChessBot ! ");
        }

        index = piece.isWhite() ? index : index+6;

        return index;
    }

    static int castlingIndex(Piece[][] chessboard){

        //4 bits, 0 -> white kingside, 1 -> white queenside, 2 -> black kingside, 3 -> black queenside

        int index = 0;

        int kingsideRookCol = 7;
        int queensideRookCol = 0;

        int kingCol = 4;

        int whiteKingRow = 7;
        int blackKingRow = 0;

        Piece whiteKing = chessboard[whiteKingRow][kingCol];
        Piece blackKing = chessboard[blackKingRow][kingCol];

        if (whiteKing != null && whiteKing.isWhite() && whiteKing.isKing()
                && !( (King) whiteKing ).getHasMoved()){

            Piece kingsideRook = chessboard[whiteKingRow][kingsideRookCol];
            Piece queensideRook = chessboard[whiteKingRow][queensideRookCol];

            if (kingsideRook != null && kingsideRook.isWhite() && kingsideRook.isRook()
                    && !( (Rook) kingsideRook ).getHasMoved()){

                index = 1;
            }

            if (queensideRook != null && queensideRook.isWhite() && queensideRook.isRook()
                    && !( (Rook) queensideRook ).getHasMoved()){

                index = (index << 1) | 1;
            }

        }

        if (blackKing != null && !blackKing.isWhite() && blackKing.isKing()
                && !( (King) blackKing ).getHasMoved()){

            Piece kingsideRook = chessboard[blackKingRow][kingsideRookCol];
            Piece queensideRook = chessboard[blackKingRow][queensideRookCol];

            if (kingsideRook != null && !kingsideRook.isWhite() && kingsideRook.isRook()
                    && !( (Rook) kingsideRook ).getHasMoved()){

                index = (index << 2) | 1;
            }

            if (queensideRook != null && !queensideRook.isWhite() && queensideRook.isRook()
                    && !( (Rook) queensideRook ).getHasMoved()){

                index = (index << 3) | 1;
            }

        }

        return index;
    }

    static int enpassantCol(Piece[][] refBoard){

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {

                if (refBoard[r][c] != null && refBoard[r][c].isPawn() && ((Pawn) refBoard[r][c]).getEnPassantVulnerable()) {
                    return c;// Optimization: Only 1 pawn can be vulnerable, break early
                }
            }
        }
        return -1;
    }

    //execute before changing the board
    public static Position doMove(ChessboardLogic chessboardLogic,int move){

        Piece[][] refBoard = chessboardLogic.getChessboard();
        boolean prevImmediateActionState = chessboardLogic.getImmediateAction();

        int oldEpCol = enpassantCol(refBoard);

        if (oldEpCol != -1){//remove it if it had enpassant
            hash ^= enPassantKey[oldEpCol];//remove old enpassant state
        }

        int oldCastleIndex = castlingIndex(refBoard);

        int[] decryptedMove = decryptMove(move);
        // fromRow, fromCol, toRow, toCol, enPassant, castle,
        // promotion, doublePawnPush, winningCaptureValue, check

        int fromRow = decryptedMove[0];
        int fromCol = decryptedMove[1];

        int toRow = decryptedMove[2];
        int toCol = decryptedMove[3];

        boolean enPassant = decryptedMove[4] == 1 ;
        boolean castle = decryptedMove[5] == 1;
        int promoType = decryptedMove[6];
        boolean promoted = promoType > 0;
        boolean doublePawnPush = decryptedMove[7] == 1;

        Piece movingPiece = refBoard[ fromRow ][ fromCol ];

        //remove piece from the fromSquare
        int fromSquare = fromRow * 8 + fromCol;
        hash ^= pieceSquare[pieceIndex(movingPiece)][fromSquare];

        Piece piece = movingPiece;

        if (promoted){//check if a promotion happens, if it does, the hash has to be calculated for
            //the new piece on the board not for the pawn

            switch (promoType) {
                case 1 -> piece = new Knight(movingPiece.isWhite());
                case 2 -> piece = new Bishop(movingPiece.isWhite());
                case 3 -> piece = new Rook(movingPiece.isWhite());
                case 4 -> piece = new Queen(movingPiece.isWhite());
                default -> throw new IllegalArgumentException(
                        "Invalid promotion type: " + promoType
                );
            }

        }

        int toSquare = toRow* 8 + toCol;
        hash ^= pieceSquare[pieceIndex(piece)][toSquare];

        //remove the captured piece from the toSquare
        Piece capturedPiece = null;
        if ( (move >> 12 & 1) == 1 ){//capture
            capturedPiece = refBoard[decryptedMove[2]][decryptedMove[3]];
            hash ^= pieceSquare[pieceIndex(capturedPiece)][toSquare];
        }

        //if enpassant remove capturedPawn
        if (enPassant){

            Piece pawn = refBoard[fromRow][toCol]; //row of victim pawn = from row of the attacker
            capturedPiece = pawn;

            if (pawn.getPieceType() != PieceType.PAWN)
                throw new IllegalArgumentException("Enpassanted piece is not a pawn at doMove() in ChessBot ! ");

            int enPassantSquare = fromRow * 8 + toCol;

            hash ^= pieceSquare[pieceIndex(pawn)][enPassantSquare];//remove victim pawn
        }

        //if castle change rook position
        if (castle){

            int rookOriginalCol = (toCol == 6) ? 7 : 0;
            int rookTargetCol = (toCol == 6) ? 5 : 3;

            Piece rook = refBoard[fromRow][rookOriginalCol];

            int rookOriginalSquare = fromRow * 8 + rookOriginalCol;
            int rookTargetSquare = fromRow * 8 + rookTargetCol;

            if (rook != null){

                if (rook.isRook()) {

                    hash ^= pieceSquare[pieceIndex(rook)][rookOriginalSquare];//remove rook from the original square
                    hash ^= pieceSquare[pieceIndex(rook)][rookTargetSquare];//add rook to the target square                }

                }
            }
        }

        if (movingPiece.isKing() && ((King)movingPiece).getHasMoved() ){
            int newCastleIndex = movingPiece.isWhite() ? oldCastleIndex & 0b1100 : oldCastleIndex & 0b0011;
            hash ^= castlingKey[oldCastleIndex];//remove old castle index
            hash ^= castlingKey[newCastleIndex];//apply the new castle index
        }

        if (movingPiece.isRook() && ((Rook)movingPiece).getHasMoved()){
            int flag;
            if (movingPiece.isWhite())
                flag = (fromCol == 7) ?  0b1110 : 0b1101 ; //king side or queen side castling permission withdrawal
            else
                flag = (fromCol == 7) ?  0b1011 : 0b0111 ; //king side or queen side castling permission withdrawal

            int newCastleIndex = oldCastleIndex & flag;
            hash ^= castlingKey[oldCastleIndex];//remove old castle key
            hash ^= castlingKey[newCastleIndex];//apply the new castle key
        }

        if (doublePawnPush){
            hash ^= enPassantKey[toCol];//setting the en passant column
        }

        int oldSide = chessboardLogic.isWhiteToMove() ? 0 : 1;
        hash ^= sideKey[oldSide];//remove last turn state

        int newSide = !chessboardLogic.isWhiteToMove() ? 0 : 1;
        hash ^= sideKey[newSide];//apply the new turn state

        return new Position(hash,oldCastleIndex,oldEpCol,oldSide,movingPiece, capturedPiece);
    }

    public static void undoMove(ChessboardLogic chessboardLogic,int encryptedMove, Position position){

        Piece[][] refBoard = chessboardLogic.getChessboard();

        int[] decryptedMove = decryptMove(encryptedMove);// fromRow, fromCol, toRow, toCol, enPassant, castle,
        // promotion, doublePawnPush, winningCaptureValue, check

        int fromRow = decryptedMove[0];
        int fromCol = decryptedMove[1];

        int toRow = decryptedMove[2];
        int toCol = decryptedMove[3];

        boolean enPassant = decryptedMove[4] == 1 ;
        boolean castle = decryptedMove[5] == 1;
        int promoType = decryptedMove[6];
        boolean promoted = promoType > 0;
        boolean doublePawnPush = decryptedMove[7] == 1;

        int oldCastlingRights = position.castlingRights;
        int oldEpCol = position.enPassantCol;
        int oldTurn = position.side;

        Piece movedPiece = position.movedPiece;

        if (oldEpCol != -1){
            hash ^= enPassantKey[oldEpCol];
        }

        if (doublePawnPush){
            hash ^= enPassantKey[toCol];//remove the new enpassant state if it happened in doMove
        }

        Piece pieceOnToSquare = movedPiece;

        if (promoted){//check if a promotion happens, if it does, the hash has to be calculated for
            //the new pieceOnToSquare on the board not for the pawn

            switch (promoType) {
                case 1 -> pieceOnToSquare = new Knight(pieceOnToSquare.isWhite());
                case 2 -> pieceOnToSquare = new Bishop(pieceOnToSquare.isWhite());
                case 3 -> pieceOnToSquare = new Rook(pieceOnToSquare.isWhite());
                case 4 -> pieceOnToSquare = new Queen(pieceOnToSquare.isWhite());
                default -> throw new IllegalArgumentException(
                        "Invalid promotion type: " + promoType
                );
            }

        }

        //remove movedPiece from the toSquare
        int toSquare = toRow* 8 + toCol;
        hash ^= pieceSquare[pieceIndex(pieceOnToSquare)][toSquare];

        //add movedPiece to the fromSquare
        int fromSquare = fromRow* 8 + fromCol;
        hash ^= pieceSquare[pieceIndex(movedPiece)][fromSquare];

        //add the capturedPiece to the toSquare
        if ( (encryptedMove >> 12 & 1) == 1 ){//capture
            hash ^= pieceSquare[pieceIndex(position.capturedPiece)][toSquare];
        }

        //if enpassant remove capturedPawn
        if (enPassant){

            Piece pawn = position.capturedPiece;

            if (pawn == null || !pawn.isPawn())
                throw  new IllegalArgumentException("The captured pieceOnToSquare is not a pawn at undoMove() in ChessBot !");


            int enPassantSquare = fromRow * 8 + toCol;

            hash ^= pieceSquare[pieceIndex(pawn)][enPassantSquare];//add the enpassanted pawn back
        }

        //if castle change rook position
        if (castle){

            int rookOriginalCol = (toCol == 6) ? 7 : 0;
            int rookTargetCol = (toCol == 6) ? 5 : 3;

            Piece movedRook = refBoard[fromRow][rookTargetCol];

            int rookOriginalSquare = fromRow * 8 + rookOriginalCol;
            int rookTargetSquare = fromRow * 8 + rookTargetCol;

            if (movedRook != null){

                if (movedRook.isRook()) {

                    hash ^= pieceSquare[pieceIndex(movedRook)][rookTargetSquare];//remove rook from the target square
                    hash ^= pieceSquare[pieceIndex(movedRook)][rookOriginalSquare];//add rook to the original square                }

                }
            }
        }

        hash ^= castlingKey[castlingIndex(refBoard)];//remove the new castling key
        hash ^= castlingKey[oldCastlingRights];//add the old castling key

        int newSide = !chessboardLogic.isWhiteToMove() ? 0 : 1;
        hash ^= sideKey[newSide];//remove the new turn state

        hash ^= sideKey[oldTurn];//add back last turn state
    }
}
