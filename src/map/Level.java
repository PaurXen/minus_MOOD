package map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Level {
    public LevelMetadata metadata;
    public MapData mapData;

    public SpawnPoint playerSpawn;

    public double playerMoveSpeed = 180.0;
    public double playerRotationSpeed = 2.5;
    public double playerRadius = 6.0;
    public double playerHeight = 48.0;
    public double playerStepHeight = 12.0;

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
        if (spawnPoint == null) {
            return;
        }

        objectSpawns.add(spawnPoint);
        mapData.addSpawnPoint(spawnPoint);
    }

    public List<SpawnPoint> getObjectSpawns() {
        return Collections.unmodifiableList(objectSpawns);
    }

    public String getId() {
        return metadata.id;
    }

    public String getName() {
        return metadata.name;
    }

    public String getVersion() {
        return metadata.version;
    }

    public String getDescription() {
        return metadata.description;
    }

    public int getLevelFormatVersion() {
        return metadata.levelFormatVersion;
    }
}