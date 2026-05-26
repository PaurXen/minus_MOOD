package menu;

import input.InputHandler;

import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Stack-based game state manager.
 *
 * <p>States are pushed and popped like a call stack. The topmost <em>blocking</em>
 * state receives update and render calls. Non-blocking (transparent) states allow
 * the state below them to also update/render — useful for HUD overlays and
 * pause menus drawn on top of gameplay.
 *
 * <p>Usage example:
 * <pre>{@code
 *   GameStateManager gsm = new GameStateManager();
 *   gsm.push(new MainMenuState(gsm));               // start at menu
 *   gsm.replace(new PlayingState(gsm, world, input)); // transition to game
 *   gsm.push(new PauseState(gsm));                   // pause on top of game
 *   gsm.pop();                                       // resume
 * }</pre>
 */
public class GameStateManager {

    private final Deque<GameState> stack = new ArrayDeque<>();

    /**
     * Pushes a new state onto the stack. The previous top state is paused
     * (will not update/render if the new state is blocking).
     *
     * @param state the state to activate
     */
    public void push(GameState state) {
        state.enter();
        stack.push(state);
    }

    /**
     * Removes the topmost state from the stack. The state below resumes.
     *
     * @throws IllegalStateException if the stack is empty
     */
    public void pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Cannot pop from an empty state stack.");
        }
        stack.pop().exit();
    }

    /**
     * Replaces the topmost state with a new one.
     * Equivalent to {@code pop()} followed by {@code push(state)}.
     *
     * @param state the new state to activate
     */
    public void replace(GameState state) {
        if (!stack.isEmpty()) {
            stack.pop().exit();
        }
        push(state);
    }

    /**
     * Removes all states from the stack, calling {@code exit()} on each
     * from top to bottom.
     */
    public void clear() {
        while (!stack.isEmpty()) {
            stack.pop().exit();
        }
    }

    /**
     * Updates states from top of stack downward. Stops at the first blocking state.
     *
     * @param deltaTime seconds since last frame
     * @param input     shared input handler
     */
    public void update(double deltaTime, InputHandler input) {
        for (GameState state : stack) {
            state.update(deltaTime, input);
            if (state.isBlocking()) {
                break;
            }
        }
    }

    /**
     * Renders all states from bottom of stack upward.
     * Unlike updates (which stop at the first blocking state), all states
     * are rendered so that overlay states (pause, game over) can draw
     * on top of the gameplay state beneath them.
     *
     * @param g2 the graphics context
     */
    public void render(Graphics2D g2) {
        // Collect all states bottom-to-top
        Deque<GameState> renderOrder = new ArrayDeque<>();
        for (GameState state : stack) {
            renderOrder.push(state);
        }

        // Render in order (bottom of stack first)
        while (!renderOrder.isEmpty()) {
            renderOrder.pop().render(g2);
        }
    }

    /**
     * Returns the number of states currently in the stack.
     *
     * @return stack depth
     */
    public int depth() {
        return stack.size();
    }

    /**
     * Returns {@code true} if the state stack is empty.
     *
     * @return true if no states are active
     */
    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
