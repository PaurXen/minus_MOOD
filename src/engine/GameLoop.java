package engine;

public class GameLoop implements Runnable {
    public interface Listener {
        void update(double deltaTime);

        void render();
    }

    private final double targetFPS;
    private final Listener listener;

    private Thread thread;
    private volatile boolean running = false;

    public GameLoop(double targetFPS, Listener listener) {
        if (targetFPS <= 0) {
            throw new IllegalArgumentException("targetFPS must be greater than zero.");
        }

        if (listener == null) {
            throw new IllegalArgumentException("GameLoop listener cannot be null.");
        }

        this.targetFPS = targetFPS;
        this.listener = listener;
    }

    public synchronized void start() {
        if (running) {
            return;
        }

        running = true;
        thread = new Thread(this, "GameLoop");
        thread.start();
    }

    public synchronized void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        final double drawInterval = 1_000_000_000.0 / targetFPS;

        long lastTime = System.nanoTime();
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / drawInterval;
            lastTime = now;

            while (delta >= 1) {
                listener.update(1.0 / targetFPS);
                delta--;
            }

            listener.render();

            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }
}