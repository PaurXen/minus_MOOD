package input;

import config.KeyBindings;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputHandler implements KeyListener {
    private KeyBindings bindings;

    public boolean forward;
    public boolean backward;
    public boolean strafeLeft;
    public boolean strafeRight;
    public boolean rotateLeft;
    public boolean rotateRight;

    private boolean debugToggleRequested = false;

    public InputHandler(KeyBindings bindings) {
        this.bindings = bindings;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == bindings.toggleDebug) {
            debugToggleRequested = true;
            return;
        }

        setKey(keyCode, true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        setKey(e.getKeyCode(), false);
    }

    private void setKey(int keyCode, boolean pressed) {
        if (keyCode == bindings.moveForward) {
            forward = pressed;
        } else if (keyCode == bindings.moveBackward) {
            backward = pressed;
        } else if (keyCode == bindings.strafeLeft) {
            strafeLeft = pressed;
        } else if (keyCode == bindings.strafeRight) {
            strafeRight = pressed;
        } else if (keyCode == bindings.rotateLeft) {
            rotateLeft = pressed;
        } else if (keyCode == bindings.rotateRight) {
            rotateRight = pressed;
        }
    }

    public boolean consumeDebugToggleRequest() {
        if (debugToggleRequested) {
            debugToggleRequested = false;
            return true;
        }

        return false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}