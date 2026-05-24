package engine;

import piecelogic.Pawn;
import piecelogic.Piece;

public class MoveState {
    public final Piece capturedPiece;
    public final Piece movedPiece;
    public final boolean movingPieceHadMoved; // King or Rook prior state
    public final boolean castledRookHadMoved;// the rook in a castling move
    public final Pawn promotedPawn;//if a pawn get promoted it gets stored here
    public final Pawn previousEPPawn; // previous pawn vulnerable to en passant

    public MoveState(Piece movedPiece,Piece capturedPiece,Pawn previousEPPawn, Pawn promotedPawn, boolean movingPieceHadMoved, boolean castledRookHadMoved) {
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.promotedPawn = promotedPawn;
        this.previousEPPawn = previousEPPawn;
        this.movingPieceHadMoved = movingPieceHadMoved;
        this.castledRookHadMoved = castledRookHadMoved;
    }
}
