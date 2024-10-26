import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.*;

public class Main {
    public static final int X = 640;
    public static final int Y = 480;
    public static double GRAVITY = 1500; // Now can be modified by user
    public static double DRAG = 0.2;     // Now can be modified by user
    public static double BOUNCE = 0.9;    // Now can be modified by user
    public static final String TITLE = "2D Physics Engine with User Input";
    
    private static JFrame frame;
    private static Canvas canvas;
    private static BufferStrategy bufferStrategy;
    private static BufferedImage buffer;
    private static Graphics graphics;
    private static Graphics2D g2d;
    private static AffineTransform at;
    public static ArrayList<Spawn> living = new ArrayList<>();
    public static boolean isRunning = true;

    // User input fields
    private static JTextField gravityField;
    private static JTextField dragField;
    private static JTextField bounceField;
    private static JTextField velocityXField;
    private static JTextField velocityYField;
    private static JTextField massField;

    public static void main(String[] args) {
        
        initializeGUI();
        Thread moveEngine = new MoveEngine();
        moveEngine.start();
        runAnimation();
    }

    private static void initializeGUI() {
        // Create main frame
        frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(0, 2, 5, 5));

        // Add input fields
        gravityField = new JTextField(String.valueOf(GRAVITY));
        dragField = new JTextField(String.valueOf(DRAG));
        bounceField = new JTextField(String.valueOf(BOUNCE));
        velocityXField = new JTextField("100");
        velocityYField = new JTextField("0");
        massField = new JTextField("10");

        controlPanel.add(new JLabel("Gravity:"));
        controlPanel.add(gravityField);
        controlPanel.add(new JLabel("Drag:"));
        controlPanel.add(dragField);
        controlPanel.add(new JLabel("Bounce:"));
        controlPanel.add(bounceField);
        controlPanel.add(new JLabel("Initial Velocity X:"));
        controlPanel.add(velocityXField);
        controlPanel.add(new JLabel("Initial Velocity Y:"));
        controlPanel.add(velocityYField);
        controlPanel.add(new JLabel("Mass:"));
        controlPanel.add(massField);

        // Add spawn button
        JButton spawnButton = new JButton("Spawn Object");
        spawnButton.addActionListener(e -> spawnObject());

        controlPanel.add(spawnButton);

        // Apply button to update physics parameters
        JButton applyButton = new JButton("Apply Physics");
        applyButton.addActionListener(e -> updatePhysicsParameters());
        controlPanel.add(applyButton);

        // Initialize canvas
        canvas = new Canvas();
        canvas.setIgnoreRepaint(true);
        canvas.setSize(X, Y);

        // Add components to frame
        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(canvas, BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Set up double buffering
        canvas.createBufferStrategy(2);
        bufferStrategy = canvas.getBufferStrategy();

        // Create off-screen drawing surface
        buffer = new BufferedImage(X, Y, BufferedImage.TYPE_INT_RGB);
    }

    private static void spawnObject() {
        try {
            double vx = Double.parseDouble(velocityXField.getText());
            double vy = Double.parseDouble(velocityYField.getText());
            int mass = Integer.parseInt(massField.getText());
            
            // Spawn object at center of screen
            giveBirth(X/2, Y/2, vx, vy, mass);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, 
                "Please enter valid numbers for velocity and mass.",
                "Input Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void updatePhysicsParameters() {
        try {
            GRAVITY = Double.parseDouble(gravityField.getText());
            DRAG = Double.parseDouble(dragField.getText());
            BOUNCE = Double.parseDouble(bounceField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, 
                "Please enter valid numbers for physics parameters.",
                "Input Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static synchronized int giveBirth(int x, int y, double vx, double vy, int m) {
        living.add(new Spawn(x, y, vx, vy, m));
        return 0;
    }

    private static void runAnimation() {
        int fps = 0;
        int frames = 0;
        long totalTime = 0;
        long curTime = System.currentTimeMillis();
        long lastTime = curTime;

        while (isRunning) {
            try {
                lastTime = curTime;
                curTime = System.currentTimeMillis();
                totalTime += curTime - lastTime;
                if (totalTime > 1000) {
                    totalTime -= 1000;
                    fps = frames;
                    frames = 0;
                }
                frames++;

                // Clear back buffer
                g2d = buffer.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, X, Y);

                // Draw entities
                for (Spawn s : living) {
                    g2d.setColor(Color.BLACK);
                    g2d.fill(new Ellipse2D.Double(s.getX(), s.getY(), 
                        s.getRadius() * 2, s.getRadius() * 2));
                }

                // Display FPS
                g2d.setFont(new Font("Courier New", Font.PLAIN, 12));
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.format("FPS: %s", fps), 20, 20);

                // Render to screen
                graphics = bufferStrategy.getDrawGraphics();
                graphics.drawImage(buffer, 0, 0, null);
                if (!bufferStrategy.contentsLost()) bufferStrategy.show();

                Thread.sleep(15);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } finally {
                if (graphics != null) graphics.dispose();
                if (g2d != null) g2d.dispose();
            }
        }
    }
}