package global;

import piecelogic.Piece;

import static chessboard.ChessboardLogic.*;

public class Global {

    public static void print2D(Object[][] arr) {
        for (int r = 0; r < arr.length; r++) {
            for (int c = 0; c < arr[r].length; c++) {

                if (arr[r][c] == null) {
                    System.out.print("null ");
                } else {
                    System.out.print(arr[r][c] + " ");
                }

            }
            System.out.println();
        }
    }

    public static void printValidMoveSet(int[][] arr) {
        if (arr == null ){
            System.out.println("move array null ! \n");
            return;
        }
        if (arr.length == 0){
            System.out.println("no moves ! \n");
            return;
        }
        for (int[] move : arr){
            int chessRow = rowToChessRow(move[0]);
            char chessCol = colToFile(move[1]);
            System.out.println("move : "+chessCol+chessRow);
        }
        System.out.println();
    }

    public static void print1D(Object[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == null) {
                System.out.print("null ");
            } else {
                System.out.print(arr[i] + " ");
            }
        }
        System.out.println();
    }

    public static void print2D(boolean[][] arr) {
        for (int r = 0; r < arr.length; r++) {
            for (int c = 0; c < arr[r].length; c++) {
                System.out.print(arr[r][c] + " ");
            }
            System.out.println();
        }
    }

    public static void print1D(boolean[] arr) {
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] ? "1 " : "0 ");
        }
        System.out.println();
    }

    public static Piece[][] shallowCopyBoard(Piece[][] board) {
        Piece[][] copy = new Piece[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                copy[r][c] = board[r][c];// intentional shallow copy; callers must not mutate shared Piece state through the copy
            }
        }
        return copy;
    }

}