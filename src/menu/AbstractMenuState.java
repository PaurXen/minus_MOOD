package menu;

import input.InputHandler;

/**
 * Base class for menu screens with a vertical option list.
 *
 * <p>Handles the shared boilerplate: fields, edge-triggered UP/DOWN navigation,
 * ENTER confirmation, and option count management. Subclasses provide the
 * option labels, custom cancel behavior, and the confirm action.
 *
 * <p>Subclasses:
 * <ul>
 *   <li>{@link MainMenuState}</li>
 *   <li>{@link PauseState}</li>
 *   <li>{@link GameOverState}</li>
 * </ul>
 */
public abstract class AbstractMenuState implements GameState {

    protected final GameStateManager manager;
    protected final int windowWidth;
    protected final int windowHeight;

    protected int selectedIndex;

    private boolean upWasDown;
    private boolean downWasDown;

    protected AbstractMenuState(GameStateManager manager, int windowWidth, int windowHeight) {
        this.manager = manager;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    @Override
    public void enter() {
        selectedIndex = 0;
        upWasDown = false;
        downWasDown = false;
    }

    @Override
    public void update(double deltaTime, InputHandler input) {
        if (input == null) {
            return;
        }

        // Subclass may handle cancel (ESC) before navigation
        if (handleCancel(input)) {
            return;
        }

        // Edge-triggered UP/DOWN navigation
        if (input.menuUp) {
            if (!upWasDown) {
                selectedIndex = (selectedIndex - 1 + getOptionCount()) % getOptionCount();
                upWasDown = true;
            }
        } else {
            upWasDown = false;
        }

        if (input.menuDown) {
            if (!downWasDown) {
                selectedIndex = (selectedIndex + 1) % getOptionCount();
                downWasDown = true;
            }
        } else {
            downWasDown = false;
        }

        // ENTER = confirm
        if (input.consumeConfirm()) {
            onConfirm(selectedIndex);
        }
    }

    // ---------------------------------------------------------------
    // Subclass contract
    // ---------------------------------------------------------------

    /** Number of options in this menu. */
    protected abstract int getOptionCount();

    /** Called when the user presses ENTER on the given option index. */
    protected abstract void onConfirm(int index);

    /**
     * Called before navigation. If the subclass handles a cancel action
     * (usually ESC), return {@code true} to skip the rest of this frame.
     */
    protected boolean handleCancel(InputHandler input) {
        return false;
    }
}
