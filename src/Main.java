import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.FileHandler;

public class Main {
    static int N, M, P;
    static String gameType;
    static char[][] board;
    static List<List<List<Integer>>> pieces = new ArrayList<>();
    static boolean solved = false;
    static int casesExamined = 0;

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Masukkan nama file: ");
        String filePath = scan.nextLine();
        try {
            readFile("../test/" + filePath);    
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        board = new char[N][M];
        for (int u=0; u<N; u++){
            for (int v=0; v<M; v++){
                board[u][v] = '.';
            }
        }
        solve();
        scan.close();
    }

    public static void readFile(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        Scanner scanFile = new Scanner(file);
        N = scanFile.nextInt();
        M = scanFile.nextInt();
        P = scanFile.nextInt();
        List<List<Integer>> currPiece = new ArrayList<>();
        char currentPiece = 'A';
        String line = scanFile.nextLine();
        scanFile.nextLine();
        int x = -1;
        while (scanFile.hasNextLine()){
            line = scanFile.nextLine();
            x++;
            for (int i=0; i<line.length(); i++){
               if (line.charAt(i) != ' ' && line.charAt(i)!= currentPiece){
                    pieces.add(currPiece);
                    currPiece = new ArrayList<>();
                    currentPiece = line.charAt(i);
                    x = 0;
                    break;
                }
            }
            for (int i=0; i<line.length(); i++){                
                if (line.charAt(i) == currentPiece){
                    List<Integer> square = new ArrayList<>();
                    square.add(x);
                    square.add(i);
                    currPiece.add(square);
                    square = new ArrayList<>();
                }
            }
        }
        pieces.add(currPiece);
        scanFile.close();
    }

    public static void rotate(List<List<Integer>> unrotated){
        for (int i=0; i<unrotated.size(); i++){
            List<Integer> rotating = new ArrayList<>();
            int x = unrotated.get(i).get(0);
            int y = unrotated.get(i).get(1);
            rotating.add(-1*y);
            rotating.add(x);
            unrotated.set(i, rotating);
        }
        adjust(unrotated);
    }
    
    public static void flip(List<List<Integer>> unflipped){ //parameter pieces.get()
        for (int i=0; i<unflipped.size(); i++){
            List<Integer> flipping = new ArrayList<>();
            int x = unflipped.get(i).get(0);
            int y = unflipped.get(i).get(1);
            flipping.add(-1*x);
            flipping.add(y);
            unflipped.set(i, flipping);
        }
        adjust(unflipped);
    }
    public static void adjust(List<List<Integer>> unajusted){
        int xmin = unajusted.get(0).get(0);
        int ymin = unajusted.get(0).get(1);
        for (int i=1; i<unajusted.size(); i++){
            int x = unajusted.get(i).get(0);
            int y = unajusted.get(i).get(1);
            if (x<xmin){
                xmin = x;
            }
            if (y<ymin){
                ymin = y;
            }
        }
        for (int i=0; i<unajusted.size();i++){
            List<Integer> adjusting = new ArrayList<>();
            adjusting.add(unajusted.get(i).get(0)-xmin);
            adjusting.add(unajusted.get(i).get(1)-ymin);
            unajusted.set(i,adjusting);
        }
    }

    public static void place(int pieceIdx, int x_move, int y_move){
        for(int k=0; k<pieces.get(pieceIdx).size(); k++){
            board[pieces.get(pieceIdx).get(k).get(0) + x_move][pieces.get(pieceIdx).get(k).get(1) + y_move] = (char) ('A'+pieceIdx);
        }
    }

    public static void remove(int pieceIdx){
        for (int u=0; u<N; u++){
            for (int v=0; v<M; v++){
                if (board[u][v] == 'A'+pieceIdx){
                    board[u][v] = '.';
                }
            }
        }
    }

    public static void solve() {
        long startTime = System.currentTimeMillis();
        if (solveHelper(0)) {
            solved = true;
            System.out.println("Solution found.");
        } else {
            System.out.println("No solution found.");
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("\nSearch time: " + duration + " ms");
        System.out.println("Cases examined: " + casesExamined);
        printBoard();
        saveToFile(solved, duration);
    }
    
    public static boolean solveHelper(int pieceIdx) {
        if (pieceIdx == pieces.size()) {
            return true;
        }
    
        for (int rotation = 0; rotation < 8; rotation++) {
            for (int n = 0; n < N; n++) {
                for (int m = 0; m < M; m++) {
                    casesExamined++;
                    if (canPlace(pieceIdx, n, m)) {
                        place(pieceIdx, n, m);
                        if (solveHelper(pieceIdx + 1)) {
                            return true;
                        }
                        remove(pieceIdx);
                    }
                }
            }
            if (rotation == 3) {
                flip(pieces.get(pieceIdx));
            } else {
                rotate(pieces.get(pieceIdx));
            }
        }
        return false;
    }
    
    public static boolean canPlace(int pieceIdx, int x_move, int y_move) {
        for (int k = 0; k < pieces.get(pieceIdx).size(); k++) {
            int x = pieces.get(pieceIdx).get(k).get(0) + x_move;
            int y = pieces.get(pieceIdx).get(k).get(1) + y_move;
            if (x >= N || y >= M || board[x][y] != '.') {
                return false;
            }
        }
        return true;
    }

    public static void printBoard() {
        String[] colors = {
            "\u001B[31m", // Red
            "\u001B[32m", // Green
            "\u001B[33m", // Yellow
            "\u001B[34m", // Blue
            "\u001B[35m", // Magenta
            "\u001B[36m", // Cyan
        };
        String reset = "\u001B[0m";

        for (int u = 0; u < N; u++) {
            for (int v = 0; v < M; v++) {
                if (board[u][v] == '.') {
                    System.out.print(board[u][v]);
                } else {
                    int colorIndex = (board[u][v] - 'A') % colors.length;
                    System.out.print(colors[colorIndex] + board[u][v] + reset);
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void saveToFile(boolean found, long duration) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Apakah ingin menyimpan? (y/n): ");
        String save = scan.nextLine();
        if (!save.equals("y")) {
            return;
        }
        System.out.println("Masukkan nama file untuk menyimpan: ");
        String fileName = scan.nextLine();
        String result = StrBuild(found, duration);
        try {
            FileWriter writer = new FileWriter("../test/" + fileName);
            writer.write(result);
            writer.close();
            System.out.println("Hasil berhasil disimpan ke file " + fileName);
        } catch (IOException e) {
            System.out.println("Terjadi kesalahan saat menyimpan jawaban.");
            e.printStackTrace();
        }
    }

    public static String StrBuild(boolean found, long duration) {
        StringBuilder result = new StringBuilder();
        if (found) {
            result.append("Solution found.\n");
        } else {
            result.append("Solution not found.\n");
        }
        result.append("Search time: " + duration + " ms\n");
        result.append("Cases examined: " + casesExamined + "\n\n");
        for (int u = 0; u < N; u++) {
            for (int v = 0; v < M; v++) {
                result.append(board[u][v]);
            }
            result.append("\n");
        }
        return result.toString();
    }
}