package map;

public class LevelMetadata {
    public int levelFormatVersion;

    public String id;
    public String name;
    public String author;
    public String version;
    public String description;

    public LevelMetadata() {
        this.levelFormatVersion = 1;
        this.id = "unknown";
        this.name = "Unnamed Level";
        this.author = "Unknown";
        this.version = "0.0.0";
        this.description = "";
    }

    public LevelMetadata(
            int levelFormatVersion,
            String id,
            String name,
            String author,
            String version,
            String description
    ) {
        this.levelFormatVersion = levelFormatVersion;
        this.id = id;
        this.name = name;
        this.author = author;
        this.version = version;
        this.description = description;
    }

}
