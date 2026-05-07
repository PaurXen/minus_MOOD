package map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Level {
    public LevelMetadata metadata;
    public MapData mapData;
    public SpawnPoint playerSpawn;

    private final List<SpawnPoint> objectSpawns = new ArrayList<>();

    public Level() {
        this.metadata = new LevelMetadata();
        this.mapData = new MapData();
    }

    public Level(LevelMetadata metadata, MapData mapData, SpawnPoint playerSpawn) {
        this.metadata = metadata == null ? new LevelMetadata() : metadata;
        this.mapData = mapData == null ? new MapData() : mapData;
        this.playerSpawn = playerSpawn;
    }

    public void addObjectSpawn(SpawnPoint spawnPoint) {
        if (spawnPoint != null) {
            objectSpawns.add(spawnPoint);
        }
    }

    public List<SpawnPoint> getObjectSpawns() {
        return Collections.unmodifiableList(objectSpawns);
    }
}
