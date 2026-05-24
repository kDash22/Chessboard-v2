package chessboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import global.Global;
import piecelogic.*;

public class ChessboardGui extends JPanel {

    private static final int TILE_SIZE = 80;

    private ChessboardLogic chessboardLogic;

    private boolean pieceSelected = false;
    private int selectedCol = -1;
    private int selectedRow = -1;

    private Image wPawn, wKnight, wBishop, wRook, wQueen, wKing,
            bPawn, bKnight, bBishop, bRook, bQueen, bKing;

    public ChessboardGui(){
        //background color around the chessboard
        setBackground(Color.GRAY);
        loadPieces();
        System.out.println("ChessboardGui was created ! ");

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){

                selectPiece(e);



            }
        });
    }

    public boolean isPieceSelected(){
        return pieceSelected;
    }

    public void setChessboardLogic(ChessboardLogic chessboardLogic) {
        this.chessboardLogic = chessboardLogic;
    }

    public ChessboardLogic getChessboardLogic() {
        return chessboardLogic;
    }

    public void loadPieces() {
        try {
            //white
            wPawn = new ImageIcon(getClass().getResource("/pieceimages/white/white-pawn.png")).getImage();
            wKnight = new ImageIcon(getClass().getResource("/pieceimages/white/white-knight.png")).getImage();
            wBishop = new ImageIcon(getClass().getResource("/pieceimages/white/white-bishop.png")).getImage();
            wRook = new ImageIcon(getClass().getResource("/pieceimages/white/white-rook.png")).getImage();
            wQueen = new ImageIcon(getClass().getResource("/pieceimages/white/white-queen.png")).getImage();
            wKing = new ImageIcon(getClass().getResource("/pieceimages/white/white-king.png")).getImage();

            //black
            bPawn = new ImageIcon(getClass().getResource("/pieceimages/black/black-pawn.png")).getImage();
            bKnight = new ImageIcon(getClass().getResource("/pieceimages/black/black-knight.png")).getImage();
            bBishop = new ImageIcon(getClass().getResource("/pieceimages/black/black-bishop.png")).getImage();
            bRook = new ImageIcon(getClass().getResource("/pieceimages/black/black-rook.png")).getImage();
            bQueen = new ImageIcon(getClass().getResource("/pieceimages/black/black-queen.png")).getImage();
            bKing = new ImageIcon(getClass().getResource("/pieceimages/black/black-king.png")).getImage();
        } catch (Exception e) {
            throw new RuntimeException("Image loading failed", e);
        }
    }


    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        //set the font
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        FontMetrics fm = g2d.getFontMetrics();

        
        int boardSize = 8*TILE_SIZE;

        int offsetX = (getWidth()-boardSize) / 2;
        int offsetY = (getHeight()-boardSize) / 2;

        //dynamically shifts the origin point of the board
        g2d.translate(offsetX, offsetY);

        for(int row = 0; row < 8; row++){
            for(int col = 0; col <8; col++){

                if ((row + col) % 2 == 0) g2d.setColor(Color.LIGHT_GRAY);
                else g2d.setColor(Color.DARK_GRAY);

                g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                // force visibility
                if ((row + col) % 2 == 0) g2d.setColor(Color.BLACK);
                else g2d.setColor(Color.WHITE);

                //print first chessRow and first file only
                if (row == 7 ) {
                    //bottom right corner in the square
                    int x =  col * TILE_SIZE + (TILE_SIZE * 8 / 10) ;
                    int y = row * TILE_SIZE + (TILE_SIZE * 9 / 10 );

                    char file = (char) ('a' + col);
                    String label = ""+file;
                    g2d.drawString(label, x, y);
                }

                if (col == 0) {
                    //top left corner in the square
                    int x = col * TILE_SIZE + 5;
                    int y = row * TILE_SIZE + fm.getAscent()+5;
                    
                    int rank = 8 - row;
                    String label = ""+rank;
                    g2d.drawString(label, x, y);
                }
            
            }
                        
        }

        //draw the pieces
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = chessboardLogic.chessboard[row][col];

                if (piece == null) continue;

                Image pieceIcon = getPieceImage(piece);

                if (pieceIcon != null) {

                    g2d.drawImage(
                            pieceIcon,
                            col * TILE_SIZE,
                            row * TILE_SIZE,
                            TILE_SIZE-5,
                            TILE_SIZE-5,
                            null
                    );
                }
            }

        }

        //highlight a selected square
        if (selectedRow != -1 && selectedCol != -1) {
            Piece selected = chessboardLogic.chessboard[selectedRow][selectedCol];
            if (selected != null) {
                highlightSquare(g2d, selected, selectedRow , selectedCol);

                g2d.setColor(new Color(148,224,224,90));
                g2d.fillRect(selectedCol * TILE_SIZE, selectedRow * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
        highlightCheckedKing(g2d);
        g2d.translate(-offsetX, -offsetY);
    }

    public Image getPieceImage(Piece piece){

        return switch(piece){
            case Pawn pawn -> piece.isWhite()  ? wPawn : bPawn;
            case Knight knight -> piece.isWhite() ? wKnight : bKnight;
            case Bishop bishop -> piece.isWhite() ? wBishop : bBishop;
            case Rook rook -> piece.isWhite() ? wRook : bRook;
            case Queen queen -> piece.isWhite() ? wQueen : bQueen;
            case King king -> piece.isWhite() ? wKing : bKing;
            case null, default -> null;

        };

    }

    public void showGame(){
        JFrame frame = new JFrame("Chessboard");
        frame.setLayout(new BorderLayout());

        this.setPreferredSize(new Dimension(8*TILE_SIZE, 8*TILE_SIZE));

        frame.add(this, BorderLayout.CENTER);
        frame.setResizable(true);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        System.out.println("chessboardGui.showGame() was called ! ");
    }

    public void selectPiece(MouseEvent event){

        int[] square = getClickedSquare(event);

        int row = square[0];
        int col = square[1];

        if (row > 7 || col > 7 || row < 0 || col < 0){
            return;
        }

        Piece[][] refBoard = chessboardLogic.getChessboard();

        //if a piece is already selected
        if (pieceSelected ) {

            //seamless transition between the same color pieces when clicking
            if (refBoard[row][col] != null){

                //deselect if clicked on the same piece
                if (selectedCol == col && selectedRow == row){
                    selectedRow = -1;
                    selectedCol = -1;
                    pieceSelected = false;
                    repaint();

                    return;
                }

                //switch piece
                if (refBoard[row][col].isWhite() == refBoard[selectedRow][selectedCol].isWhite()){
                    selectedRow = row;
                    selectedCol = col;
                    repaint();

                    return;
                }
            }

            Piece movingPiece = refBoard[selectedRow][selectedCol] ;
            int[][] moveSet = movingPiece.getValidMoveSet();

            for (int i = 0; i < moveSet.length; i++){

                if (row == moveSet[i][0] && col == moveSet[i][1]){

                    chessboardLogic.movePiece(selectedRow,selectedCol,row,col);
                    break;
                }
            }

            pieceSelected = false;
            selectedCol = selectedRow = -1;
            repaint();

            return;
        }

        if (refBoard[row][col] != null) {

            if (refBoard[row][col].isWhite() && chessboardLogic.isWhiteToMove()) {
                pieceSelected = true;
                selectedCol = col;
                selectedRow = row;

                repaint();
            }

            if (!refBoard[row][col].isWhite() && !chessboardLogic.isWhiteToMove()){
                pieceSelected = true;
                selectedCol = col;
                selectedRow = row;
                repaint();
            }
        } else {
            selectedCol = selectedRow = -1;
            repaint();
        }

        //repaint();
        //System.out.println("is king in check : "+chessboardLogic.isKingInCheck(true));
    }

    private int[] getClickedSquare(MouseEvent event){

        int boardSideLength = 8 * TILE_SIZE;

        int offsetX = (getWidth()- boardSideLength) /  2;
        int offsetY = (getHeight()- boardSideLength) / 2;

        int adjustedX = event.getX() - offsetX;
        int adjustedY = event.getY() - offsetY;

        if (adjustedX < 0 || adjustedY < 0 || adjustedX >= boardSideLength || adjustedY >= boardSideLength) {
            return new int[]{-1, -1};
        }

        // This prevents misclassification of out-of-bounds clicks as tile (0,0).
        int screenCol = Math.floorDiv(adjustedX, TILE_SIZE);
        int screenRow = Math.floorDiv(adjustedY, TILE_SIZE);

        //returns the Piece[][] board index
        return new int[]{screenRow,screenCol};

    }

    //this must only run after a selection is made, otherwise it will throw a null pointer exception
    public void highlightSquare(Graphics2D g2d, Piece piece, int selectedRow, int selectedCol){

        if (piece == null) return;

        if (!pieceSelected) return;

        piece.moveCheck(chessboardLogic,selectedRow,selectedCol);
        int[][] moveSet = piece.getValidMoveSet();

        for (int[] move : moveSet) {
            int row = move[0];
            int col = move[1];

            // bounds check
            if (row < 0 || row >= 8 || col < 0 || col >= 8)
                continue;
            if (chessboardLogic.getChessboard()[row][col] != null) {
                g2d.setColor(new Color(255, 0, 0, 35));
            } else {
                g2d.setColor(new Color(0, 255, 0, 35));
            }

            g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

    }

    private void highlightCheckedKing(Graphics2D g2d){

        boolean whiteToMove = chessboardLogic.isWhiteToMove();
        Piece[][] refBoard = chessboardLogic.getChessboard();

        if (chessboardLogic.isKingInCheck(whiteToMove, refBoard)){
            int[] kingPos = chessboardLogic.getKingPos(whiteToMove, refBoard);
            int row = kingPos[0];
            int col = kingPos[1];

            g2d.setColor(new Color(255, 30, 30, 70));
            g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }       

    public static void main(String[] args){

        javax.swing.SwingUtilities.invokeLater(() -> {
            ChessboardLogic chessboardLogic = new ChessboardLogic();
            //chessboardLogic.newGame();
            chessboardLogic.customBoard();
            chessboardLogic.getChessboardGui().showGame();
        });

        System.out.println();
        System.out.println();

        /*
        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){
                Piece piece = chessboardLogic.chessboard[row][col];
                if (piece != null){
                    piece.moveCheck(chessboardLogic);
                    System.out.println(piece);
                    Global.printValidMoveSet(piece.getValidMoveSet());
                }
            }
        }
            */
    }

}
