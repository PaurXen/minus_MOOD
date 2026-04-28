import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputHandler implements KeyListener {
    public boolean forward;
    public boolean backward;
    public boolean strafeLeft;
    public boolean strafeRight;
    public boolean rotateLeft;
    public boolean rotateRight;

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> forward = true;
            case KeyEvent.VK_S -> backward = true;
            case KeyEvent.VK_A -> strafeLeft = true;
            case KeyEvent.VK_D -> strafeRight = true;
            case KeyEvent.VK_LEFT -> rotateLeft = true;
            case KeyEvent.VK_RIGHT -> rotateRight = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> forward = false;
            case KeyEvent.VK_S -> backward = false;
            case KeyEvent.VK_A -> strafeLeft = false;
            case KeyEvent.VK_D -> strafeRight = false;
            case KeyEvent.VK_LEFT -> rotateLeft = false;
            case KeyEvent.VK_RIGHT -> rotateRight = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}