package fr.game.board;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BoardDisplay extends JFrame {
    private static final int CELL_DIM = 35;
    private static final int MARGIN = 25;
    private final Surface surface;
    private final JLabel infoText;
    private final GameBoard board;
    private final BrainEngine brain;
    
    public BoardDisplay() {
        setTitle("Strategic Board Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        surface = new Surface();
        infoText = new JLabel("Game Started");
        board = new GameBoard();
        brain = new BrainEngine(4, 5);
        
        setupLayout();
        setupGame();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(surface, BorderLayout.CENTER);
        add(infoText, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void setupGame() {
        surface.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handlePlayerMove(e.getX(), e.getY());
            }
        });
    }
    
    private void handlePlayerMove(int mouseX, int mouseY) {
        int x = (mouseX - MARGIN) / CELL_DIM;
        int y = (mouseY - MARGIN) / CELL_DIM;
        
        if (x >= 0 && x < 19 && y >= 0 && y < 19) {
            if (board.placePiece(x, y)) {
                surface.repaint();
                updateGameState();
                
                if (board.getState() == GameBoard.GameState.PLAYING) {
                    makeComputerMove();
                }
            }
        }
    }
    
    private void makeComputerMove() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                infoText.setText("Computer is thinking...");
                BrainEngine.Position move = brain.findBestMove(board);
                
                if (move != null) {
                    board.placePiece(move.x, move.y);
                    SwingUtilities.invokeLater(() -> {
                        surface.repaint();
                        updateGameState();
                        infoText.setText(String.format("Computer move took %.2f seconds", 
                            brain.getThinkingTime()));
                    });
                }
                return null;
            }
        }.execute();
    }
    
    private void updateGameState() {
        GameBoard.GameState state = board.getState();
        if (state != GameBoard.GameState.PLAYING) {
            String winner = state == GameBoard.GameState.BLACK_WINS ? "Black" : "White";
            infoText.setText(winner + " wins!");
        }
    }
    
    private class Surface extends JPanel {
        public Surface() {
            setPreferredSize(new Dimension(
                CELL_DIM * 19 + 2 * MARGIN,
                CELL_DIM * 19 + 2 * MARGIN
            ));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
            
            drawGrid(g2d);
            drawStones(g2d);
        }
        
        private void drawGrid(Graphics2D g2d) {
            g2d.setColor(new Color(205, 170, 125));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            g2d.setColor(Color.BLACK);
            for (int i = 0; i < 19; i++) {
                g2d.drawLine(
                    MARGIN + i * CELL_DIM, MARGIN,
                    MARGIN + i * CELL_DIM, MARGIN + 18 * CELL_DIM
                );
                g2d.drawLine(
                    MARGIN, MARGIN + i * CELL_DIM,
                    MARGIN + 18 * CELL_DIM, MARGIN + i * CELL_DIM
                );
            }
            
            // Draw star points
            int[] starPoints = {3, 9, 15};
            for (int x : starPoints) {
                for (int y : starPoints) {
                    g2d.fillOval(
                        MARGIN + x * CELL_DIM - 3,
                        MARGIN + y * CELL_DIM - 3,
                        6, 6
                    );
                }
            }
        }
        
        private void drawStones(Graphics2D g2d) {
            for (int y = 0; y < 19; y++) {
                for (int x = 0; x < 19; x++) {
                    GameBoard.Stone stone = board.getStone(x, y);
                    if (stone != GameBoard.Stone.EMPTY) {
                        drawStone(g2d, x, y, stone);
                    }
                }
            }
        }
        
        private void drawStone(Graphics2D g2d, int x, int y, GameBoard.Stone stone) {
            int stoneSize = (int)(CELL_DIM * 0.9);
            int xPos = MARGIN + x * CELL_DIM - stoneSize/2;
            int yPos = MARGIN + y * CELL_DIM - stoneSize/2;
            
            if (stone == GameBoard.Stone.BLACK) {
                g2d.setColor(Color.BLACK);
                g2d.fillOval(xPos, yPos, stoneSize, stoneSize);
            } else {
                g2d.setColor(Color.WHITE);
                g2d.fillOval(xPos, yPos, stoneSize, stoneSize);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(xPos, yPos, stoneSize, stoneSize);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BoardDisplay().setVisible(true);
        });
    }
}
