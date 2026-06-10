package input;

import config.KeyBindings;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Captures keyboard input and exposes it as simple boolean flags.
 *
 * <p>Two categories of input are tracked:
 * <ul>
 *   <li><b>Continuous</b> — {@code true} while the key is held (movement, rotation, attack).</li>
 *   <li><b>One-shot</b> — consumed once per press via dedicated methods (debug toggle,
 *       menu confirm/cancel).</li>
 * </ul>
 *
 * <p>Usage in game states:
 * <pre>{@code
 *   // Continuous: read directly
 *   if (input.forward) { moveForward(); }
 *
 *   // One-shot: consume
 *   if (input.consumeConfirm()) { activateSelection(); }
 * }</pre>
 */
public class InputHandler implements KeyListener {

    private final KeyBindings bindings;
    private boolean attackRequested = false;
    // --- Continuous (held) booleans ---

    /** W / move_forward */
    public boolean forward;
    /** S / move_backward */
    public boolean backward;
    /** A / strafe_left */
    public boolean strafeLeft;
    /** D / strafe_right */
    public boolean strafeRight;

    /** LEFT / rotate_left */
    public boolean rotateLeft;
    /** RIGHT / rotate_right */
    public boolean rotateRight;

    /** SPACE / attack */
    public boolean attack;
    /** E / interact */
    private boolean interactRequested = false;
    /** M / toggle_map */
    private boolean mapToggleRequested = false;

    /** UP / menu_up */
    public boolean menuUp;
    /** DOWN / menu_down */
    public boolean menuDown;

    // --- One-shot request flags ---

    private boolean debugToggleRequested = false;
    private boolean confirmRequested = false;
    private boolean cancelRequested = false;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------

    public InputHandler(KeyBindings bindings) {
        if (bindings == null) {
            throw new IllegalArgumentException("KeyBindings cannot be null.");
        }
        this.bindings = bindings;
    }

    public boolean consumeAttackRequest() {
        if (attackRequested) {
            attackRequested = false;
            return true;
        }

        return false;
    }
    // ---------------------------------------------------------------
    // KeyListener implementation
    // ---------------------------------------------------------------

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == bindings.toggleDebug) {
            debugToggleRequested = true;
            return;
        }
        if (keyCode == bindings.confirm) {
            confirmRequested = true;
            return;
        }
        if (keyCode == bindings.cancel) {
            cancelRequested = true;
            return;
        }

        if (keyCode == bindings.attack && !attack) {
            attackRequested = true;
        }
        if (keyCode == bindings.interact) {
            interactRequested = true;
            return;
        }
        if (keyCode == bindings.toggleMap) {
            mapToggleRequested = true;
            return;
        }

        setKey(keyCode, true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        setKey(e.getKeyCode(), false);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // not used
    }

    // ---------------------------------------------------------------
    // Key mapping
    // ---------------------------------------------------------------

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
        } else if (keyCode == bindings.attack) {
            attack = pressed;
        } else if (keyCode == bindings.menuUp) {
            menuUp = pressed;
        } else if (keyCode == bindings.menuDown) {
            menuDown = pressed;
        }
    }

    // ---------------------------------------------------------------
    // One-shot consumers
    // ---------------------------------------------------------------

    /**
     * Consumes a pending debug-toggle request. Returns {@code true} exactly once
     * per key press.
     */
    public boolean consumeDebugToggleRequest() {
        if (debugToggleRequested) {
            debugToggleRequested = false;
            return true;
        }
        return false;
    }

    /**
     * Consumes a pending confirm request (ENTER key). Used by menu states.
     */
    public boolean consumeConfirm() {
        if (confirmRequested) {
            confirmRequested = false;
            return true;
        }
        return false;
    }

    /**
     * Consumes a pending cancel request (ESCAPE key). Used by menu states
     * and gameplay for pausing.
     */
    public boolean consumeCancel() {
        if (cancelRequested) {
            cancelRequested = false;
            return true;
        }
        return false;
    }

    public boolean consumeInteract() {
        if (interactRequested) {
            interactRequested = false;
            return true;
        }

        return false;
    }

    public boolean consumeMapToggleRequest() {
        if (mapToggleRequested) {
            mapToggleRequested = false;
            return true;
        }

        return false;
    }
}
