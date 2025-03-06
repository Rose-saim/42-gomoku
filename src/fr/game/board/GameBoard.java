package fr.game.board;

import java.util.ArrayList;
import java.util.List;

public class GameBoard {
    private static final int GRID_SIZE = 19;
    private static final int WIN_SEQUENCE = 5;
    private static final int MAX_CAPTURES = 10;
    
    private final Stone[][] grid;
    private final List<Move> moveHistory;
    private final int[] captureCount;
    private int currentPlayer;
    private GameState state;
    
    public GameBoard() {
        grid = new Stone[GRID_SIZE][GRID_SIZE];
        moveHistory = new ArrayList<>();
        captureCount = new int[2];
        currentPlayer = 0;
        state = GameState.PLAYING;
        initializeBoard();
    }
    
    private void initializeBoard() {
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                grid[y][x] = Stone.EMPTY;
            }
        }
    }
    
    public boolean placePiece(int x, int y) {
        if (!isValidMove(x, y)) {
            return false;
        }
        
        grid[y][x] = currentPlayer == 0 ? Stone.BLACK : Stone.WHITE;
        moveHistory.add(new Move(x, y, currentPlayer));
        
        checkCaptures(x, y);
        if (captureCount[currentPlayer] >= MAX_CAPTURES) {
            state = currentPlayer == 0 ? GameState.BLACK_WINS : GameState.WHITE_WINS;
            return true;
        }
        
        if (checkWinningSequence(x, y)) {
            state = currentPlayer == 0 ? GameState.BLACK_WINS : GameState.WHITE_WINS;
            return true;
        }
        
        currentPlayer = 1 - currentPlayer;
        return true;
    }
    
    private boolean isValidMove(int x, int y) {
        if (x < 0 || x >= GRID_SIZE || y < 0 || y >= GRID_SIZE) {
            return false;
        }
        if (grid[y][x] != Stone.EMPTY) {
            return false;
        }
        return !createsDoubleFreeThree(x, y);
    }
    
    private void checkCaptures(int x, int y) {
        for (Direction dir : Direction.values()) {
            checkCaptureInDirection(x, y, dir);
        }
    }
    
    private void checkCaptureInDirection(int x, int y, Direction dir) {
        Stone current = grid[y][x];
        int dx = dir.getDx();
        int dy = dir.getDy();
        
        int x1 = x + dx;
        int y1 = y + dy;
        int x2 = x1 + dx;
        int y2 = y1 + dy;
        int x3 = x2 + dx;
        int y3 = y2 + dy;
        
        if (!isInBounds(x3, y3)) {
            return;
        }
        
        Stone opponent = current == Stone.BLACK ? Stone.WHITE : Stone.BLACK;
        if (grid[y1][x1] == opponent && grid[y2][x2] == opponent && grid[y3][x3] == current) {
            grid[y1][x1] = Stone.EMPTY;
            grid[y2][x2] = Stone.EMPTY;
            captureCount[currentPlayer] += 2;
        }
    }
    
    private boolean checkWinningSequence(int x, int y) {
        for (Direction dir : Direction.values()) {
            if (countSequence(x, y, dir) >= WIN_SEQUENCE) {
                return true;
            }
        }
        return false;
    }
    
    private int countSequence(int x, int y, Direction dir) {
        Stone current = grid[y][x];
        int count = 1;
        int dx = dir.getDx();
        int dy = dir.getDy();
        
        // Count in positive direction
        int nx = x + dx;
        int ny = y + dy;
        while (isInBounds(nx, ny) && grid[ny][nx] == current) {
            count++;
            nx += dx;
            ny += dy;
        }
        
        // Count in negative direction
        nx = x - dx;
        ny = y - dy;
        while (isInBounds(nx, ny) && grid[ny][nx] == current) {
            count++;
            nx -= dx;
            ny -= dy;
        }
        
        return count;
    }
    
    private boolean createsDoubleFreeThree(int x, int y) {
        Stone temp = grid[y][x];
        grid[y][x] = currentPlayer == 0 ? Stone.BLACK : Stone.WHITE;
        
        int freeThrees = 0;
        for (Direction dir : Direction.values()) {
            if (isFreeThree(x, y, dir)) {
                freeThrees++;
                if (freeThrees > 1) {
                    grid[y][x] = temp;
                    return true;
                }
            }
        }
        
        grid[y][x] = temp;
        return false;
    }
    
    private boolean isFreeThree(int x, int y, Direction dir) {
        Stone current = grid[y][x];
        int dx = dir.getDx();
        int dy = dir.getDy();
        
        int count = 1;
        boolean hasSpace = false;
        
        // Check forward
        int nx = x + dx;
        int ny = y + dy;
        while (isInBounds(nx, ny) && count < 4) {
            if (grid[ny][nx] == current) {
                count++;
            } else if (grid[ny][nx] == Stone.EMPTY) {
                hasSpace = true;
                break;
            } else {
                return false;
            }
            nx += dx;
            ny += dy;
        }
        
        // Check backward
        nx = x - dx;
        ny = y - dy;
        while (isInBounds(nx, ny) && count < 4) {
            if (grid[ny][nx] == current) {
                count++;
            } else if (grid[ny][nx] == Stone.EMPTY) {
                hasSpace = true;
                break;
            } else {
                return false;
            }
            nx -= dx;
            ny -= dy;
        }
        
        return count == 3 && hasSpace;
    }
    
    private boolean isInBounds(int x, int y) {
        return x >= 0 && x < GRID_SIZE && y >= 0 && y < GRID_SIZE;
    }
    
    public Stone getStone(int x, int y) {
        return grid[y][x];
    }
    
    public GameState getState() {
        return state;
    }
    
    public int getCurrentPlayer() {
        return currentPlayer;
    }
    
    public int getCaptureCount(int player) {
        return captureCount[player];
    }
    
    public enum Stone {
        EMPTY, BLACK, WHITE
    }
    
    public enum GameState {
        PLAYING, BLACK_WINS, WHITE_WINS, DRAW
    }
    
    public enum Direction {
        HORIZONTAL(1, 0),
        VERTICAL(0, 1),
        DIAGONAL1(1, 1),
        DIAGONAL2(1, -1);
        
        private final int dx;
        private final int dy;
        
        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
        
        public int getDx() { return dx; }
        public int getDy() { return dy; }
    }
    
    private static class Move {
        final int x;
        final int y;
        final int player;
        
        Move(int x, int y, int player) {
            this.x = x;
            this.y = y;
            this.player = player;
        }
    }
}
