import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public class GameM extends JFrame {

    // --- CẤU HÌNH CHUNG ---
    private JPanel mainPanel; // Panel chính chứa các màn hình (CardLayout)
    private CardLayout cardLayout;
    
    // Các màn hình con
    private JPanel menuPanel;
    private JPanel gamePanel;
    private JPanel instructPanel;

    // --- BIẾN GAMEPLAY ---
    private JPanel boardPanel; // Bàn cờ chứa các nút
    private JLabel lblTime, lblScore;
    private int rows, cols;
    private JButton[] buttons;
    private String[] hiddenValues;
    private Timer gameTimer, flipTimer;
    private int seconds = 0, errors = 0, matchesFound = 0, totalPairs = 0;
    
    // Trạng thái lật bài
    private JButton firstBtn = null;
    private int firstIndex = -1;
    private boolean isProcessing = false;

    // Bộ icon (Unicode) cho game
    private final String[] icons = {
        "\u2605", "\u2665", "\u2663", "\u2666", // Sao, Tim, Chuon, Ro
        "\u265A", "\u265E", "\u262F", "\u2602", // Vua, Ma, AmDuong, O
        "\u260E", "\u2708", "\u266B", "\u2600", // DienThoai, MayBay, Nhac, MatTroi
        "\u2744", "\u2622", "\u2693", "\u2654", // Tuyet, PhongXa, MoNeo, Hau
        "\u26BD", "\u26BE", "\u26F3", "\u26F5"  // BongDa, BongChay, CoLop, Thuyen
    };

    public GameM() {
        super("Game Lật Hình - Memory Game");
        setSize(700, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Sử dụng CardLayout để chuyển đổi giữa Menu, Game, Hướng dẫn
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Khởi tạo các màn hình
        createMenuScreen();
        createInstructionScreen();
        // Game screen sẽ được tạo lại mỗi khi bấm Start

        mainPanel.add(menuPanel, "MENU");
        mainPanel.add(instructPanel, "INSTRUCT");

        add(mainPanel);
        cardLayout.show(mainPanel, "MENU"); // Hiện menu đầu tiên
        
        // Timer đếm giờ
        gameTimer = new Timer(1000, e -> {
            seconds++;
            lblTime.setText("Thời gian: " + seconds + "s");
        });

        // Timer úp bài tự động (0.8 giây)
        flipTimer = new Timer(800, e -> flipBack());
        flipTimer.setRepeats(false);

        setVisible(true);
    }

    // ==========================================
    // 1. MÀN HÌNH MENU (GIAO DIỆN CHÍNH)
    // ==========================================
    private void createMenuScreen() {
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(44, 62, 80)); // Màu xanh đậm hiện đại
        menuPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        // Tiêu đề
        JLabel title = new JLabel("MEMORY GAME");
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Các nút bấm
        JButton btnEasy = createStyledButton("Chế Độ Dễ (4x4)");
        JButton btnHard = createStyledButton("Chế Độ Khó (6x4)");
        JButton btnInst = createStyledButton("Hướng Dẫn");
        JButton btnExit = createStyledButton("Thoát Game");

        // Xử lý sự kiện nút
        btnEasy.addActionListener(e -> startGame(4, 4));
        btnHard.addActionListener(e -> startGame(6, 4));
        btnInst.addActionListener(e -> cardLayout.show(mainPanel, "INSTRUCT"));
        btnExit.addActionListener(e -> System.exit(0));

        // Thêm vào panel
        menuPanel.add(Box.createVerticalGlue());
        menuPanel.add(title);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 40))); // Khoảng cách
        menuPanel.add(btnEasy);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        menuPanel.add(btnHard);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        menuPanel.add(btnInst);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        menuPanel.add(btnExit);
        menuPanel.add(Box.createVerticalGlue());
    }

    // Hàm tạo nút đẹp
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setMaximumSize(new Dimension(300, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    // ==========================================
    // 2. MÀN HÌNH HƯỚNG DẪN
    // ==========================================
    private void createInstructionScreen() {
        instructPanel = new JPanel(new BorderLayout());
        instructPanel.setBackground(new Color(236, 240, 241));

        JTextArea textArea = new JTextArea();
        textArea.setText(
            "\n   HƯỚNG DẪN CHƠI GAME\n\n" +
            "   1. Mục tiêu: Tìm tất cả các cặp hình giống nhau.\n" +
            "   2. Cách chơi:\n" +
            "      - Click vào một ô để lật hình lên.\n" +
            "      - Click tiếp vào ô thứ hai.\n" +
            "      - Nếu giống nhau: Bạn được tính điểm.\n" +
            "      - Nếu khác nhau: Hình sẽ tự úp xuống sau 1 giây.\n\n" +
            "   3. Chế độ chơi:\n" +
            "      - Dễ: Bảng 4x4 (8 cặp hình).\n" +
            "      - Khó: Bảng 6x4 (12 cặp hình).\n\n" +
            "   Chúc bạn chơi vui vẻ!"
        );
        textArea.setFont(new Font("Arial", Font.PLAIN, 18));
        textArea.setEditable(false);
        textArea.setBackground(new Color(236, 240, 241));
        textArea.setMargin(new Insets(20, 40, 20, 40));

        JButton btnBack = new JButton("Quay Lại Menu");
        btnBack.setFont(new Font("Arial", Font.BOLD, 16));
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "MENU"));

        instructPanel.add(textArea, BorderLayout.CENTER);
        instructPanel.add(btnBack, BorderLayout.SOUTH);
    }

    // ==========================================
    // 3. MÀN HÌNH GAMEPLAY (XỬ LÝ CHÍNH)
    // ==========================================
    private void startGame(int r, int c) {
        this.rows = r;
        this.cols = c;
        
        // Tạo panel game mới mỗi lần chơi để reset sạch sẽ
        gamePanel = new JPanel(new BorderLayout());
        
        // --- Phần Top: Hiển thị thời gian và điểm ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(50, 50, 50));
        topPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        lblTime = new JLabel("Thời gian: 0s");
        lblTime.setForeground(Color.WHITE);
        lblTime.setFont(new Font("Arial", Font.BOLD, 16));

        lblScore = new JLabel("Lỗi: 0");
        lblScore.setForeground(Color.CYAN);
        lblScore.setFont(new Font("Arial", Font.BOLD, 16));

        topPanel.add(lblTime, BorderLayout.WEST);
        topPanel.add(lblScore, BorderLayout.EAST);

        // --- Phần Center: Bàn cờ ---
        boardPanel = new JPanel(new GridLayout(rows, cols, 5, 5));
        boardPanel.setBackground(Color.GRAY);
        boardPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Logic tạo dữ liệu
        setupGameData();

        // --- Phần Bottom: Nút quay lại ---
        JButton btnBackMenu = new JButton("Quay Về Menu Chính");
        btnBackMenu.setFont(new Font("Arial", Font.BOLD, 14));
        btnBackMenu.addActionListener(e -> {
            gameTimer.stop();
            cardLayout.show(mainPanel, "MENU");
        });
        
        gamePanel.add(topPanel, BorderLayout.NORTH);
        gamePanel.add(boardPanel, BorderLayout.CENTER);
        gamePanel.add(btnBackMenu, BorderLayout.SOUTH);

        // Thêm vào card layout và hiển thị
        mainPanel.add(gamePanel, "GAME");
        cardLayout.show(mainPanel, "GAME");
        
        // Bắt đầu đếm giờ
        gameTimer.restart();
    }

    private void setupGameData() {
        seconds = 0; errors = 0; matchesFound = 0;
        firstBtn = null; isProcessing = false;
        
        int totalCells = rows * cols;
        totalPairs = totalCells / 2;
        buttons = new JButton[totalCells];
        hiddenValues = new String[totalCells];

        // Tạo danh sách cặp hình
        ArrayList<String> cards = new ArrayList<>();
        for (int i = 0; i < totalPairs; i++) {
            String icon = (i < icons.length) ? icons[i] : String.valueOf(i);
            cards.add(icon);
            cards.add(icon);
        }
        Collections.shuffle(cards); // Xáo trộn

        // Tạo nút
        for (int i = 0; i < totalCells; i++) {
            hiddenValues[i] = cards.get(i);
            buttons[i] = new JButton("");
            buttons[i].setFont(new Font("Segoe UI Emoji", Font.BOLD, 32)); // Font Emoji cho đẹp
            buttons[i].setBackground(new Color(220, 230, 241));
            buttons[i].setFocusPainted(false);
            
            final int index = i;
            buttons[i].addActionListener(e -> onCardClick(index));
            boardPanel.add(buttons[i]);
        }
    }

    // --- LOGIC XỬ LÝ CLICK ---
    private void onCardClick(int index) {
        // Nếu đang chờ úp bài hoặc ô đã mở -> bỏ qua
        if (isProcessing || buttons[index].getText().length() > 0) return;

        // Lật bài
        buttons[index].setText(hiddenValues[index]);
        buttons[index].setBackground(Color.WHITE);

        if (firstBtn == null) {
            // Đây là thẻ thứ 1
            firstBtn = buttons[index];
            firstIndex = index;
        } else {
            // Đây là thẻ thứ 2
            if (hiddenValues[firstIndex].equals(hiddenValues[index])) {
                // ĐÚNG (Match)
                firstBtn.setBackground(new Color(46, 204, 113)); // Xanh lá
                buttons[index].setBackground(new Color(46, 204, 113));
                // Vô hiệu hóa nút
                firstBtn.setEnabled(false);
                buttons[index].setEnabled(false);
                
                firstBtn = null; // Reset
                matchesFound++;
                checkWin();
            } else {
                // SAI (Mismatch)
                firstBtn.setBackground(new Color(231, 76, 60)); // Đỏ
                buttons[index].setBackground(new Color(231, 76, 60));
                
                errors++;
                lblScore.setText("Lỗi: " + errors);
                
                isProcessing = true; // Khóa bàn phím
                flipTimer.start(); // Đợi 0.8s rồi úp lại
            }
        }
    }

    private void flipBack() {
        // Quét tìm các ô đang mở nhưng chưa hoàn thành để úp lại
        for (JButton btn : buttons) {
            if (btn.isEnabled() && btn.getText().length() > 0) {
                btn.setText("");
                btn.setBackground(new Color(220, 230, 241));
            }
        }
        firstBtn = null;
        isProcessing = false; // Mở khóa
    }

    private void checkWin() {
        if (matchesFound == totalPairs) {
            gameTimer.stop();
            JOptionPane.showMessageDialog(this, 
                "CHIẾN THẮNG!\nThời gian: " + seconds + "s\nSai: " + errors + " lần.",
                "Kết quả", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(mainPanel, "MENU"); // Về menu khi thắng
        }
    }

    public static void main(String[] args) {
        // Chạy trên luồng giao diện an toàn
        SwingUtilities.invokeLater(() -> new GameM());
    }
}