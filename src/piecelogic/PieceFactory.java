package piecelogic;

public class PieceFactory {

    public static Piece createPiece(PieceType pieceType, boolean isWhite){

        return switch (pieceType){
            case KING -> new King(isWhite);
            case PAWN -> new Pawn(isWhite);
            case KNIGHT -> new Knight(isWhite);
            case BISHOP -> new Bishop(isWhite);
            case ROOK -> new Rook(isWhite);
            case QUEEN -> new Queen(isWhite);

        };
    }
}
