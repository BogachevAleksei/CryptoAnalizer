package cryptoanalyser;

public abstract class MenuEntry {

    private final String title;

    public MenuEntry(String title) {
        this.title = title;
    }

    public abstract void run();

    public String getTitle() {
        return title;
    }
}
