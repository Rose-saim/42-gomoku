package fr.game.board;

import java.util.*;

public class BrainEngine {
    private final int searchDepth;
    private final int branchingFactor;
    private final Random randomGen;
    private long startThinkTime;
    
    public BrainEngine(int searchDepth, int branchingFactor) {
        this.searchDepth = searchDepth;
        this.branchingFactor = branchingFactor;
        this.randomGen = new Random();
    }
    
    public Position findBestMove(GameBoard board) {
        startThinkTime = System.nanoTime();
        List<Position> possibleMoves = generateMoves(board);
        if (possibleMoves.isEmpty()) {
            return null;
        }
        
        Position bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        
        for (Position move : possibleMoves) {
            GameBoard tempBoard = cloneBoard(board);
            tempBoard.placePiece(move.x, move.y);
            
            int score = evaluatePosition(tempBoard, searchDepth - 1, alpha, beta, false);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, bestScore);
        }
        
        return bestMove;
    }
    
    private List<Position> generateMoves(GameBoard board) {
        List<Position> moves = new ArrayList<>();
        Set<Position> considered = new HashSet<>();
        
        // Consider moves near existing pieces first
        for (int y = 0; y < 19; y++) {
            for (int x = 0; x < 19; x++) {
                if (board.getStone(x, y) != GameBoard.Stone.EMPTY) {
                    addAdjacentMoves(board, x, y, considered, moves);
                }
            }
        }
        
        // If no pieces on board, start near center
        if (moves.isEmpty()) {
            int center = 19 / 2;
            moves.add(new Position(center, center));
        }
        
        // Limit branching factor
        if (moves.size() > branchingFactor) {
            Collections.shuffle(moves, randomGen);
            return moves.subList(0, branchingFactor);
        }
        
        return moves;
    }
    
    private void addAdjacentMoves(GameBoard board, int x, int y, Set<Position> considered, List<Position> moves) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int newX = x + dx;
                int newY = y + dy;
                Position pos = new Position(newX, newY);
                
                if (isValidPosition(newX, newY) && 
                    board.getStone(newX, newY) == GameBoard.Stone.EMPTY &&
                    !considered.contains(pos)) {
                    considered.add(pos);
                    moves.add(pos);
                }
            }
        }
    }
    
    private int evaluatePosition(GameBoard board, int depth, int alpha, int beta, boolean maximizing) {
        if (depth == 0 || board.getState() != GameBoard.GameState.PLAYING) {
            return calculateScore(board);
        }
        
        List<Position> moves = generateMoves(board);
        if (moves.isEmpty()) {
            return 0;
        }
        
        if (maximizing) {
            int maxScore = Integer.MIN_VALUE;
            for (Position move : moves) {
                GameBoard tempBoard = cloneBoard(board);
                if (tempBoard.placePiece(move.x, move.y)) {
                    int score = evaluatePosition(tempBoard, depth - 1, alpha, beta, false);
                    maxScore = Math.max(maxScore, score);
                    alpha = Math.max(alpha, maxScore);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            for (Position move : moves) {
                GameBoard tempBoard = cloneBoard(board);
                if (tempBoard.placePiece(move.x, move.y)) {
                    int score = evaluatePosition(tempBoard, depth - 1, alpha, beta, true);
                    minScore = Math.min(minScore, score);
                    beta = Math.min(beta, minScore);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return minScore;
        }
    }
    
    private int calculateScore(GameBoard board) {
        int score = 0;
        
        // Evaluate captures
        score += board.getCaptureCount(board.getCurrentPlayer()) * 1000;
        score -= board.getCaptureCount(1 - board.getCurrentPlayer()) * 1000;
        
        // Evaluate winning conditions
        if (board.getState() == GameBoard.GameState.BLACK_WINS) {
            return board.getCurrentPlayer() == 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        } else if (board.getState() == GameBoard.GameState.WHITE_WINS) {
            return board.getCurrentPlayer() == 1 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
        
        // Add positional scoring
        for (int y = 0; y < 19; y++) {
            for (int x = 0; x < 19; x++) {
                GameBoard.Stone stone = board.getStone(x, y);
                if (stone != GameBoard.Stone.EMPTY) {
                    int stoneValue = evaluateStonePosition(board, x, y);
                    if (stone == GameBoard.Stone.BLACK) {
                        score += board.getCurrentPlayer() == 0 ? stoneValue : -stoneValue;
                    } else {
                        score += board.getCurrentPlayer() == 1 ? stoneValue : -stoneValue;
                    }
                }
            }
        }
        
        return score;
    }
    
    private int evaluateStonePosition(GameBoard board, int x, int y) {
        int score = 0;
        GameBoard.Stone stone = board.getStone(x, y);
        
        for (GameBoard.Direction dir : GameBoard.Direction.values()) {
            int sequence = countSequence(board, x, y, dir, stone);
            score += Math.pow(10, sequence);
        }
        
        // Bonus for center control
        int distanceToCenter = Math.abs(x - 9) + Math.abs(y - 9);
        score += (19 - distanceToCenter) * 2;
        
        return score;
    }
    
    private int countSequence(GameBoard board, int x, int y, GameBoard.Direction dir, GameBoard.Stone stone) {
        int count = 1;
        int dx = dir.getDx();
        int dy = dir.getDy();
        
        // Count forward
        int nx = x + dx;
        int ny = y + dy;
        while (isValidPosition(nx, ny) && board.getStone(nx, ny) == stone) {
            count++;
            nx += dx;
            ny += dy;
        }
        
        // Count backward
        nx = x - dx;
        ny = y - dy;
        while (isValidPosition(nx, ny) && board.getStone(nx, ny) == stone) {
            count++;
            nx -= dx;
            ny -= dy;
        }
        
        return count;
    }
    
    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < 19 && y >= 0 && y < 19;
    }
    
    private GameBoard cloneBoard(GameBoard original) {
        // Implementation of board cloning would go here
        // For now, we'll assume GameBoard has a copy constructor or clone method
        return original;
    }
    
    public double getThinkingTime() {
        return (System.nanoTime() - startThinkTime) / 1_000_000_000.0;
    }
    
    public static class Position {
        final int x;
        final int y;
        
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Position position = (Position) o;
            return x == position.x && y == position.y;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}
