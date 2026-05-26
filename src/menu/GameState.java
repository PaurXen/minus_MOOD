package menu;

import input.InputHandler;

import java.awt.Graphics2D;

/**
 * Represents a single screen/mode in the game.
 *
 * <p>States are managed by {@link GameStateManager} as a stack.
 * Examples: MainMenuState, PlayingState, PauseState, GameOverState.
 *
 * <p>Each state receives input, update, and render calls from the game loop.
 */
public interface GameState {

    /** Called when this state becomes the active state (pushed onto the stack). */
    void enter();

    /** Called when this state is removed from the stack. */
    void exit();

    /**
     * Per-frame logic update.
     *
     * @param deltaTime seconds since last update
     * @param input     shared input handler (may be null for non-interactive states)
     */
    void update(double deltaTime, InputHandler input);

    /**
     * Draw this state to the screen.
     *
     * @param g2 the graphics context (origin is top-left of the panel)
     */
    void render(Graphics2D g2);

    /**
     * Whether this state blocks states below it from updating/rendering.
     * <p>Most states return {@code true}. Overlay states (like HUD effects)
     * return {@code false} so the state underneath continues to run.
     *
     * @return true if this state blocks lower states
     */
    default boolean isBlocking() {
        return true;
    }
}
