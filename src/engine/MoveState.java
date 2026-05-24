package engine;

import piecelogic.Piece;

public class MoveState {
    public final Piece capturedPiece;
    public final Piece movedPiece;
    public final boolean movingPieceHadMoved; // King or Rook prior state
    public final boolean castledRookHadMoved;// the rook in a castling move
    public final Piece promotedPawn;//if a pawn get promoted it gets stored here
    public MoveState(Piece movedPiece,Piece capturedPiece, Piece promotedPawn, boolean movingPieceHadMoved, boolean castledRookHadMoved) {
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.promotedPawn = promotedPawn;
        this.movingPieceHadMoved = movingPieceHadMoved;
        this.castledRookHadMoved = castledRookHadMoved;
    }
}
