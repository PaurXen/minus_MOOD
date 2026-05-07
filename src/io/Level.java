package io;

import entities.Player;
import map.LineWall;
import map.Wall;

import java.util.ArrayList;

public class Level {
    public int levelFormatVersion;
    public String id;
    public String name;
    public String author;
    public String version;
    public String description;

    public Player player;

    public ArrayList<Wall> walls = new ArrayList<>();
    public ArrayList<LineWall> lineWalls = new ArrayList<>();
}
