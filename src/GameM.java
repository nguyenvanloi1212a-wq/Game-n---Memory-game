import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.io.*; 

public class GameM extends JFrame {

    // --- CAU HINH HE THONG ---
    private JPanel mainPanel; 
    private CardLayout cardLayout;
    
    // Cac man hinh con
    private JPanel menuPanel;
    private JPanel gamePanel;
    private JPanel instructPanel;

    // --- BIEN GAMEPLAY ---
    private JPanel boardPanel; 
    private JLabel lblTime, lblScore, lblHighScore; 
    private int rows, cols;
    private JButton[] buttons;
    private String[] hiddenValues;
    private Timer gameTimer, flipTimer;
    
    // Bien tinh diem
    private int seconds = 0;
    private int errors = 0;
    private int currentScore = 0; 
    private int matchesFound = 0;
    private int totalPairs = 0;
    
    // --- BIEN LUU TRU FILE ---
    private int highScore = 0; 
    private final String FILE_NAME = System.getProperty("user.home") + "/Desktop/highscore.txt";
    
    // Trang thai lat bai
    private JButton firstBtn = null;
    private int firstIndex = -1;
    private boolean isProcessing = false;

    // Bo icon
    private final String[] icons = {
        "\u2605", "\u2665", "\u2663", "\u2666", 
        "\u265A", "\u265E", "\u262F", "\u2602", 
        "\u260E", "\u2708", "\u266B", "\u2600", 
        "\u2744", "\u2622", "\u2693", "\u2654", 
        "\u26BD", "\u26BE", "\u26F3", "\u26F5"  
    };

    public GameM() {
        super("Game Lật Hình - Memory Game");
        setSize(850, 650); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Kiem tra file
        File f = new File(FILE_NAME);
        System.out.println(">> FILE DIEM NAM O DAY: " + f.getAbsolutePath());

        loadHighScore();

        // KHOI TAO LAYOUT
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        menuPanel = new JPanel();
        instructPanel = new JPanel();
        gamePanel = new JPanel();

        mainPanel.add(menuPanel, "MENU");
        mainPanel.add(instructPanel, "INSTRUCT");
        mainPanel.add(gamePanel, "GAME");

        add(mainPanel);

        // XAY DUNG GIAO DIEN
        createMenuScreen();
        createInstructionScreen(); // <--- DA THEM PHAN NAY KY LUONG
        
        cardLayout.show(mainPanel, "MENU"); 
        
        gameTimer = new Timer(1000, e -> {
            seconds++;
            lblTime.setText("Thời gian: " + seconds + "s");
        });

        flipTimer = new Timer(800, e -> flipBack());
        flipTimer.setRepeats(false);

        setVisible(true);
    }

    // --- 1. DOC FILE ---
    private void loadHighScore() {
        try {
            File f = new File(FILE_NAME);
            if (!f.exists()) {
                f.createNewFile();
                saveHighScore(0); 
            } else {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line = br.readLine();
                if (line != null && !line.isEmpty()) {
                    highScore = Integer.parseInt(line.trim());
                }
                br.close();
            }
        } catch (Exception e) {
            highScore = 0; 
        }
    }

    // --- 2. GHI FILE ---
    private void saveHighScore(int newScore) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME));
            bw.write(String.valueOf(newScore));
            bw.close();
            highScore = newScore;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- GIAO DIEN MENU ---
    private void createMenuScreen() {
        menuPanel.removeAll();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(230, 240, 255)); // Xanh nhat
        menuPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        JLabel title = new JLabel("MEMORY GAME");
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(new Color(0, 102, 204));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subTitle = new JLabel("Kỷ lục hiện tại: " + highScore + " điểm");
        subTitle.setFont(new Font("Arial", Font.ITALIC, 20));
        subTitle.setForeground(new Color(255, 102, 0));
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tao nut
        JButton btnEasy = createSimpleButton("Chế Độ Dễ (4x4)");
        JButton btnHard = createSimpleButton("Chế Độ Khó (6x4)");
        JButton btnInst = createSimpleButton("Hướng Dẫn");
        JButton btnExit = createSimpleButton("Thoát Game");

        // Gan su kien
        btnEasy.addActionListener(e -> startGame(4, 4));
        btnHard.addActionListener(e -> startGame(6, 4));
        
        // SU KIEN MO HUONG DAN
        btnInst.addActionListener(e -> {
            createInstructionScreen(); // Ve lai huong dan
            cardLayout.show(mainPanel, "INSTRUCT");
        });
        
        btnExit.addActionListener(e -> System.exit(0));

        menuPanel.add(Box.createVerticalGlue());
        menuPanel.add(title);
        menuPanel.add(subTitle);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        menuPanel.add(btnEasy);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        menuPanel.add(btnHard);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        menuPanel.add(btnInst);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        menuPanel.add(btnExit);
        menuPanel.add(Box.createVerticalGlue());
        
        menuPanel.revalidate();
        menuPanel.repaint();
    }
    
    private JButton createSimpleButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setMaximumSize(new Dimension(300, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    // --- GIAO DIEN HUONG DAN (DA SUA) ---
    private void createInstructionScreen() {
        instructPanel.removeAll(); // Xoa trang truoc khi ve
        instructPanel.setLayout(new BorderLayout());
        instructPanel.setBackground(Color.WHITE);

        // Noi dung huong dan
        JTextArea textArea = new JTextArea();
        textArea.setText(
            "\n   HƯỚNG DẪN & CÁCH TÍNH ĐIỂM:\n\n" +
            "   1. Luật chơi cơ bản:\n" +
            "      - Lật 2 ô hình giống nhau để ghi điểm.\n" +
            "      - Nếu sai, hình sẽ úp lại sau 0.8 giây.\n\n" +
            "   2. Hệ thống tính điểm:\n" +
            "      ✅ Lật Đúng: +100 điểm/cặp.\n" +
            "      ❌ Lật Sai:  -10 điểm/lần.\n\n" +
            "   3. Thưởng Tốc Độ (Chỉ khi chiến thắng):\n" +
            "      - Nếu bạn thắng dưới 60 giây:\n" +
            "      - Điểm thưởng = (60 - Thời gian chơi) x 10.\n\n" +
            "   4 Chúc bạn chơi vuii vẻ!! \n" +
            "   >> Kỷ lục điểm cao nhất sẽ được lưu vào file trên Desktop.\n"
             
        );
        textArea.setFont(new Font("Arial", Font.PLAIN, 18));
        textArea.setEditable(false); // Khong cho sua chu
        textArea.setMargin(new Insets(30, 50, 30, 50)); // Can le
        textArea.setBackground(new Color(245, 245, 245)); // Nen xam nhat

        // Nut quay lai
        JButton btnBack = new JButton("Quay Lại Menu");
        btnBack.setFont(new Font("Arial", Font.BOLD, 16));
        btnBack.setBackground(new Color(0, 102, 204));
        btnBack.setForeground(Color.WHITE);
        btnBack.addActionListener(e -> {
            createMenuScreen(); // Cap nhat lai menu
            cardLayout.show(mainPanel, "MENU");
        });
        
        JPanel botPanel = new JPanel();
        botPanel.setBackground(Color.WHITE);
        botPanel.setBorder(new EmptyBorder(20,0,20,0));
        botPanel.add(btnBack);

        instructPanel.add(textArea, BorderLayout.CENTER);
        instructPanel.add(botPanel, BorderLayout.SOUTH);
        
        instructPanel.revalidate();
        instructPanel.repaint();
    }

    // --- MAN HINH GAME ---
    private void startGame(int r, int c) {
        this.rows = r;
        this.cols = c;
        this.currentScore = 0; 
        
        gamePanel.removeAll();
        gamePanel.setLayout(new BorderLayout());
        
        // Top
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(240, 240, 240));
        topPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        lblTime = new JLabel("Thời gian: 0s");
        lblTime.setFont(new Font("Arial", Font.BOLD, 16));

        lblScore = new JLabel("Điểm: 0");
        lblScore.setForeground(new Color(0, 153, 0));
        lblScore.setFont(new Font("Arial", Font.BOLD, 18));
        
        lblHighScore = new JLabel("Top 1: " + highScore);
        lblHighScore.setForeground(Color.RED);
        lblHighScore.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        infoPanel.setOpaque(false);
        infoPanel.add(lblScore);
        infoPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        infoPanel.add(lblHighScore);

        topPanel.add(lblTime, BorderLayout.WEST);
        topPanel.add(infoPanel, BorderLayout.EAST);

        // Board
        boardPanel = new JPanel(new GridLayout(rows, cols, 5, 5));
        boardPanel.setBackground(Color.GRAY);
        boardPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        setupGameData();

        // Bottom
        JButton btnBackMenu = new JButton("Quay Về Menu Chính");
        btnBackMenu.setFont(new Font("Arial", Font.BOLD, 14));
        btnBackMenu.addActionListener(e -> {
            gameTimer.stop();
            createMenuScreen();
            cardLayout.show(mainPanel, "MENU");
        });
        
        gamePanel.add(topPanel, BorderLayout.NORTH);
        gamePanel.add(boardPanel, BorderLayout.CENTER);
        gamePanel.add(btnBackMenu, BorderLayout.SOUTH);

        gamePanel.revalidate();
        gamePanel.repaint();
        cardLayout.show(mainPanel, "GAME");
        
        gameTimer.restart();
    }

    private void setupGameData() {
        seconds = 0; errors = 0; matchesFound = 0;
        firstBtn = null; isProcessing = false;
        
        int totalCells = rows * cols;
        totalPairs = totalCells / 2;
        buttons = new JButton[totalCells];
        hiddenValues = new String[totalCells];

        ArrayList<String> cards = new ArrayList<>();
        for (int i = 0; i < totalPairs; i++) {
            String icon = (i < icons.length) ? icons[i] : String.valueOf(i);
            cards.add(icon);
            cards.add(icon);
        }
        Collections.shuffle(cards); 

        for (int i = 0; i < totalCells; i++) {
            hiddenValues[i] = cards.get(i);
            buttons[i] = new JButton("");
            buttons[i].setFont(new Font("Segoe UI Emoji", Font.BOLD, 32)); 
            buttons[i].setBackground(new Color(220, 230, 241));
            buttons[i].setFocusPainted(false);
            
            final int index = i;
            buttons[i].addActionListener(e -> onCardClick(index));
            boardPanel.add(buttons[i]);
        }
    }

    private void onCardClick(int index) {
        if (isProcessing || buttons[index].getText().length() > 0) return;

        buttons[index].setText(hiddenValues[index]);
        buttons[index].setBackground(Color.WHITE);

        if (firstBtn == null) {
            firstBtn = buttons[index];
            firstIndex = index;
        } else {
            if (hiddenValues[firstIndex].equals(hiddenValues[index])) {
                // DUNG
                firstBtn.setBackground(new Color(144, 238, 144)); 
                buttons[index].setBackground(new Color(144, 238, 144));
                firstBtn.setEnabled(false);
                buttons[index].setEnabled(false);
                
                currentScore += 100;
                lblScore.setText("Điểm: " + currentScore);
                
                firstBtn = null; 
                matchesFound++;
                checkWin();
            } else {
                // SAI
                firstBtn.setBackground(Color.PINK); 
                buttons[index].setBackground(Color.PINK);
                
                if(currentScore >= 10) currentScore -= 10;
                lblScore.setText("Điểm: " + currentScore);
                
                errors++;
                isProcessing = true; 
                flipTimer.start(); 
            }
        }
    }

    private void flipBack() {
        for (JButton btn : buttons) {
            if (btn.isEnabled() && btn.getText().length() > 0) {
                btn.setText("");
                btn.setBackground(new Color(220, 230, 241));
            }
        }
        firstBtn = null;
        isProcessing = false; 
    }

    private void checkWin() {
        if (matchesFound == totalPairs) {
            gameTimer.stop();
            
            int timeBonus = 0;
            if (seconds < 60) {
                timeBonus = (60 - seconds) * 10; 
            }
            int finalScore = currentScore + timeBonus;
            
            String msg = "Thời gian: " + seconds + "s\n" +
                         "Điểm Gốc: " + currentScore + "\n" +
                         "Thưởng Tốc Độ: " + timeBonus + "\n" +
                         "---------------------\n" +
                         "TỔNG ĐIỂM: " + finalScore;
            
            if (finalScore > highScore) {
                saveHighScore(finalScore); 
                msg += "\n\nCHÚC MỪNG! PHÁ KỶ LỤC MỚI!";
            } else {
                msg += "\n\nKỷ lục hiện tại: " + highScore;
            }
            
            JOptionPane.showMessageDialog(this, msg, "KẾT QUẢ", JOptionPane.INFORMATION_MESSAGE);
            
            createMenuScreen();
            cardLayout.show(mainPanel, "MENU"); 
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameM());
    }
}