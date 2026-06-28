import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CursorTrailFX extends JPanel {

    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private float currentHue = 0.0f;

    public CursorTrailFX() {
        // 1. Critical for Desktop Trail: Make the canvas background 100% transparent
        setOpaque(false);

        // 2. Loop to constantly grab the global cursor position anywhere on screen
        Timer timer = new Timer(16, e -> {
            // Get precise coordinates of the mouse on the entire monitor
            PointerInfo pointer = MouseInfo.getPointerInfo();
            if (pointer != null) {
                Point point = pointer.getLocation();
                spawnParticles(point.x, point.y);
            }
            updateParticles();
            repaint(); // Redraw the clear screen overlay
        });
        timer.start();
    }

    private void spawnParticles(int x, int y) {
        // Spawn 2 particles per frame
        for (int i = 0; i < 2; i++) {
            Color rainbowColor = Color.getHSBColor(currentHue, 0.9f, 1.0f);
            particles.add(new Particle(x, y, rainbowColor));
            
            // Cycle smoothly through rainbow colors
            currentHue += 0.005f;
            if (currentHue > 1.0f) currentHue = 0.0f;
        }
    }

    private void updateParticles() {
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle p = iterator.next();
            p.update();
            if (p.alpha <= 0) {
                iterator.remove();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Keeps the component transparent instead of painting solid gray/black
        super.paintComponent(g); 
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Render every floating particle over the desktop
        for (Particle p : particles) {
            p.draw(g2d);
        }
    }

    public static void main(String[] args) {
        // Get the total layout resolution of your monitor
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(screenSize);
        frame.setUndecorated(true); // Removes the close/minimize title bar frame lines

        // 2. Critical Configurations for Desktop Integration:
        frame.setBackground(new Color(0, 0, 0, 0)); // Make entire frame glass-transparent
        frame.setAlwaysOnTop(true);               // Force the trail to sit on top of all other windows
        
        // Tells Windows OS to ignore clicks on this frame and pass them through to underlying apps
        frame.setFocusableWindowState(false); 

        frame.add(new CursorTrailFX());
        frame.setVisible(true);
    }

    private static class Particle {
        double x, y;
        double vx, vy;
        int size = 18;
        int alpha = 255;
        Color color;

        public Particle(int startX, int startY, Color color) {
            this.x = startX;
            this.y = startY;
            this.color = color;
            // Float outwards naturally
            this.vx = (Math.random() - 0.5) * 1.5;
            this.vy = (Math.random() - 0.5) * 1.5;
        }

        public void update() {
            x += vx;
            y += vy;
            alpha -= 10; // Fades away faster over the desktop area
            if (alpha < 0) alpha = 0;
            if (size > 2) size -= 0.3;
        }

        public void draw(Graphics2D g2d) {
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g2d.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
        }
    }
}
