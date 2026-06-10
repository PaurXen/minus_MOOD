package menu;

public final class LevelCatalog {
    private static final LevelEntry[] LEVELS = {
            new LevelEntry(
                    "Level 01 - Training Grounds",
                    "levels/level01.properties",
                    "Bigger beginner level with several enemies and obstacles."
            ),
            new LevelEntry(
                    "Level 02 - Storage Complex",
                    "levels/level02.properties",
                    "Large level with more open space and more enemies."
            ),
            new LevelEntry(
                    "Level 03 - Courtyard Barracks",
                    "levels/level03.properties",
                    "Large barracks map. Enemies do not spawn near you."
            ),
            new LevelEntry(
                    "Level 04 - Industrial Depot",
                    "levels/level04.properties",
                    "Very large depot map with many enemies placed far from spawn."
            ),
            new LevelEntry(
                    "Level 99 - Test Level",
                    "levels/level99.properties",
                    "Developer test level."
            )
    };

    private LevelCatalog() {
    }

    public static LevelEntry[] getLevels() {
        return LEVELS.clone();
    }

    public static LevelEntry getDefaultLevel() {
        return LEVELS[0];
    }
}