package engine;

import piecelogic.Piece;

public class Position {
    long hash;

    int castlingRights;
    int enPassantCol;
    int side;
    Piece movedPiece;
    Piece capturedPiece;

    public Position(long hash, int castlingRights, int enPassantCol, int side, Piece movedPiece,Piece capturedPiece){
        this.castlingRights = castlingRights;
        this.enPassantCol = enPassantCol;
        this.side = side;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
    }
}
