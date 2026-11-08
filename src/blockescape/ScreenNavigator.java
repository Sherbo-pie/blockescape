package blockescape;

/** Lets screens ask the window to switch screens without knowing about it. */
public interface ScreenNavigator {
    void showHome();
    void showLevels();
    void startLevel(int levelNumber);
    void continueGame();
    void quit();
}
