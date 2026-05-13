# minus_MOOD Current Technical Structure

## Table of Contents

1. [Current Project State](#current-project-state)
2. [Active Runtime Flow](#active-runtime-flow)
3. [Current Source Tree](#current-source-tree)
4. [Package Overview](#package-overview)
5. [`app` Package](#app-package)
6. [`config` Package](#config-package)
7. [`input` Package](#input-package)
8. [`engine` Package](#engine-package)
9. [`entities` Package](#entities-package)
10. [`collision` Package](#collision-package)
11. [`map` Package](#map-package)
12. [`io` Package](#io-package)
13. [`math` Package](#math-package)
14. [`render` Package](#render-package)
15. [Level File Format](#level-file-format)
16. [MapData Runtime Contract](#mapdata-runtime-contract)
17. [Collision Contract](#collision-contract)
18. [Raycasting Contract](#raycasting-contract)
19. [Legacy Compatibility](#legacy-compatibility)
20. [Known Transitional Classes](#known-transitional-classes)
21. [Recommended Next Refactors](#recommended-next-refactors)

---

## Current Project State

Current active branch:

```text
map-collision-rework
```

Current active configuration:

```properties
game_version=0.1.2
level_format_version=2
default_level=levels/level99.properties
```

The project is transitioning from temporary wall-based level geometry to a Doom-like node/linedef/sector map representation.

Current active map structure:

```text
MapData
├── vertices
├── linedefs
├── sidedefs
├── sectors
└── spawn points
```

Current active level format:

```text
level_format_version=2
```

Version 2 level files use:

```text
sector_*
sidedef_*
vertex_*
linedef_*
player_spawn
```

The older version 1 level format is still partially supported through `LegacyMapAdapter`, which converts:

```text
rect_wall_*
line_wall_*
```

into `MapData`.

---

## Active Runtime Flow

Current runtime flow:

```text
Main
 ↓
GameConfig.loadGameSettings(...)
 ↓
GamePanel
 ↓
GameWorld
 ↓
LevelLoader
 ↓
LevelParser
 ↓
MapCompiler
 ↓
map.Level
 ↓
MapData
 ↓
CollisionWorld
 ↓
GameLoop update/render
 ↓
GamePanel draws MapData LineDefs
```

Detailed flow:

```text
1. Main loads game.properties.
2. Main creates a JFrame.
3. Main creates GamePanel.
4. GamePanel creates InputHandler.
5. GamePanel creates GameWorld.
6. GameWorld loads the default level through LevelLoader.
7. LevelLoader reads the .properties file.
8. LevelParser converts the file into map.Level.
9. MapCompiler validates the resulting MapData.
10. GameWorld creates Player from Level.playerSpawn.
11. GameWorld creates CollisionWorld from MapData.
12. GameLoop repeatedly calls update and repaint.
13. GameWorld handles movement and collision.
14. GamePanel draws the map, player, and debug info.
```

---

## Current Source Tree

Current intended package structure:

```text
src/
├── app/
│   └── Main.java
│
├── config/
│   ├── GameConfig.java
│   ├── GameSettings.java
│   └── KeyBindings.java
│
├── input/
│   └── InputHandler.java
│
├── engine/
│   ├── GameLoop.java
│   ├── GamePanel.java
│   └── GameWorld.java
│
├── entities/
│   ├── Entity.java
│   ├── EntityBody.java
│   └── Player.java
│
├── collision/
│   ├── CollisionBody.java
│   ├── CollisionFlags.java
│   ├── CollisionResult.java
│   ├── CollisionWorld.java
│   └── SpatialIndex.java
│
├── map/
│   ├── LegacyMapAdapter.java
│   ├── Level.java
│   ├── LevelMetadata.java
│   ├── LineDef.java
│   ├── LineFlag.java
│   ├── LineWall.java
│   ├── MapData.java
│   ├── Sector.java
│   ├── SideDef.java
│   ├── SpawnPoint.java
│   ├── SpawnType.java
│   ├── TestMapFactory.java
│   ├── Vertex.java
│   └── Wall.java
│
├── io/
│   ├── Level.java
│   ├── LevelLoader.java
│   ├── LevelParser.java
│   └── MapCompiler.java
│
├── math/
│   ├── Bounds2D.java
│   ├── GeometryMath.java
│   ├── Segment2D.java
│   └── Vec2.java
│
└── render/
    ├── DebugRenderer.java
    ├── EntityRenderer.java
    └── MapRenderer.java
```

Important transitional note:

```text
src/io/Level.java
```

still exists, but it is now legacy/orphaned and should be removed once all imports use:

```text
map.Level
```

---

## Package Overview

| Package | Purpose |
|---|---|
| `app` | Application entry point. |
| `config` | Global game and controls configuration. |
| `input` | Keyboard input state tracking. |
| `engine` | Game loop, runtime world, Swing panel bridge. |
| `entities` | Runtime entities such as Player and physical bodies. |
| `collision` | Movement collision using CollisionBody and MapData. |
| `map` | Static map model: vertices, linedefs, sectors, sides, spawns. |
| `io` | Loading, parsing, and validating level files. |
| `math` | Reusable geometry/vector utility classes. |
| `render` | Placeholder renderer package for future extraction from GamePanel. |

---

# Package and Class Structure

## `app` Package

### `Main`

Application entry point.

Responsibilities:

```text
load game settings
create JFrame
create GamePanel
start the game loop
```

Usage flow:

```text
Main.main(...)
 ├── GameConfig.loadGameSettings("config/game.properties")
 ├── new JFrame(...)
 ├── new GamePanel(settings)
 └── panel.startGameLoop()
```

Should not contain:

```text
collision logic
map parsing logic
player movement logic
rendering implementation
```

---

## `config` Package

### `GameConfig`

Responsible for loading configuration files.

Current expected files:

```text
config/game.properties
config/controls.properties
```

Responsibilities:

```text
load game settings
load controls
parse numeric/boolean/string properties
create GameSettings
create KeyBindings
```

### `GameSettings`

Data holder for global runtime settings.

Current important fields:

```text
gameTitle
gameVersion
gameBuild
gameAuthor
gameDescription

configVersion
levelFormatVersion
controlsFormatVersion

windowWidth
windowHeight
windowResizable

defaultLevel

debugMode
showDebugText
showCollisionShapes

targetFPS
```

Important current values:

```properties
level_format_version=2
default_level=levels/level99.properties
```

### `KeyBindings`

Stores key mapping values.

Responsibilities:

```text
move forward key
move backward key
strafe left key
strafe right key
rotate left key
rotate right key
debug toggle key
```

Does not store whether a key is currently pressed. That belongs to `InputHandler`.

---

## `input` Package

### `InputHandler`

Keyboard input state tracker.

Responsibilities:

```text
listen for keyPressed
listen for keyReleased
track movement booleans
track rotation booleans
track debug toggle request
```

Current exposed movement state:

```text
forward
backward
strafeLeft
strafeRight
rotateLeft
rotateRight
```

Current usage:

```text
GamePanel owns InputHandler
GameWorld reads InputHandler each update
```

Flow:

```text
keyboard event
 ↓
InputHandler updates booleans
 ↓
GameWorld.update(input, deltaTime)
 ↓
movement vector is calculated
```

---

## `engine` Package

### `GameLoop`

Threaded fixed-update loop.

Responsibilities:

```text
own game loop thread
calculate update timing
call update callback
call render callback
sleep briefly between iterations
```

Important internal interface:

```java
public interface Listener {
    void update(double deltaTime);
    void render();
}
```

Current usage:

```text
GamePanel creates GameLoop
GamePanel passes updateGame(...) as update callback
GamePanel passes repaint() as render callback
```

Current behavior:

```text
targetFPS controls fixed update step
deltaTime passed to update is 1.0 / targetFPS
render requests repaint after update processing
```

### `GameWorld`

Main runtime simulation container.

Responsibilities:

```text
load current level
own current map.Level
own current MapData
create and own Player
own CollisionWorld
update player rotation
update player movement
apply collision results
provide state to GamePanel
```

Current load flow:

```text
loadLevel(path)
 ├── LevelLoader.loadLevel(path, settings.levelFormatVersion)
 ├── mapData = currentLevel.mapData
 ├── player = createPlayerFromLevel(currentLevel)
 ├── collisionWorld = new CollisionWorld(mapData)
 └── playerBody = player.getBody()
```

Current player creation:

```text
Player is created from Level.playerSpawn
Player speed/radius/height/stepHeight are copied from Level player defaults
```

Current update flow:

```text
update(input, deltaTime)
 ├── handleRotation(input, deltaTime)
 └── handleMovement(input, deltaTime)
```

Current collision flow:

```text
movePlayer(dx, dy)
 ├── collisionWorld.moveAndApply(player.getBody(), dx, dy)
 └── player.setPosition(result.finalPosition)
```

Important getter methods:

```text
getCurrentLevel()
getPlayer()
getMapData()
getMapLines()
getCollisionWorld()
getPlayerBody()
```

### `GamePanel`

Swing panel and temporary renderer.

Responsibilities:

```text
own InputHandler
own GameWorld
own GameLoop
receive Swing repaint calls
draw background
draw current MapData
draw Player
draw debug information
```

Current drawing flow:

```text
paintComponent(...)
 ├── drawBackground(...)
 ├── drawMap(...)
 ├── drawPlayer(...)
 └── drawDebugInfo(...) + drawMapDebug(...) if enabled
```

Current map drawing:

```text
for each LineDef in gameWorld.getMapLines()
    choose color based on line behavior
    choose stroke based on collisionThickness
    draw line from start vertex to end vertex
```

Current debug drawing:

```text
player position
player angle
level name/version
collision radius
MapData line count
collision line count
raycast line count
vertex ids
linedef ids
```

Transitional note:

```text
GamePanel currently contains rendering code.
Future refactor should move this into:
- MapRenderer
- EntityRenderer
- DebugRenderer
```

---

## `entities` Package

### `Entity`

Base runtime object.

Responsibilities:

```text
own unique entity id
store angle
store active state
own EntityBody
provide position access through body
```

Important methods:

```text
getX()
getY()
getPosition()
setPosition(...)
moveBy(...)
getBody()
setBody(...)
activate()
deactivate()
```

Position is not stored directly in `Entity`; it is stored in the entity body.

### `EntityBody`

Physical entity body.

Extends:

```text
collision.CollisionBody
```

Adds:

```text
solid
collidable
```

Purpose:

```text
connect entity system to collision system
allow future entity-specific physical behavior
```

Current usage:

```text
Player owns EntityBody
CollisionWorld moves/checks EntityBody through CollisionBody base type
```

### `Player`

Runtime player entity.

Extends:

```text
Entity
```

Responsibilities:

```text
store player move speed
store player rotation speed
expose radius/height/stepHeight through body
```

Current fields:

```text
moveSpeed
rotationSpeed
```

Current body accessors:

```text
getRadius()
setRadius(...)
getHeight()
setHeight(...)
getStepHeight()
setStepHeight(...)
```

Player does not directly perform collision checks. Movement intent is calculated in `GameWorld`, and legality is decided by `CollisionWorld`.

---

## `collision` Package

### `CollisionBody`

Physical collision body data.

Current fields:

```text
Vec2 position
double radius
double height
double stepHeight
```

Current model:

```text
2D circle body with future 2.5D cylinder data
```

Current use:

```text
CollisionWorld checks body.position and body.radius
height and stepHeight are stored but not fully used yet
```

Future use:

```text
player cylinder height
ceiling clearance checks
step height checks
stairs
ramps
ledge behavior
```

### `CollisionResult`

Movement result returned by `CollisionWorld`.

Current data:

```text
startPosition
requestedPosition
finalPosition

blocked
blockedX
blockedY
slid
```

Purpose:

```text
avoid returning only true/false
allow GameWorld to apply final legal position
prepare for richer collision info later
```

Future possible additions:

```text
hit LineDef
collision normal
hit position
entered sector
triggered lines
surface material
```

### `CollisionFlags`

Collision behavior enum.

Current intended purpose:

```text
general collision behavior flags
```

Note:

```text
Current MapData line behavior mainly uses map.LineFlag.
CollisionFlags may be reused later for entity/body collision layers or removed if redundant.
```

### `CollisionWorld`

Main movement collision system.

Responsibilities:

```text
own MapData reference
move CollisionBody
check if position is blocked
check collision against MapData collision lines
return CollisionResult
```

Current data source:

```text
MapData.getCollisionLines()
```

Current collision method:

```text
circle vs LineDef segment
```

Current movement behavior:

```text
try X movement first
try Y movement second
if one axis is blocked, movement may slide along the other axis
```

Current collision check:

```text
for each LineDef in mapData.getCollisionLines()
    skip if !lineDef.blocksMovement()
    create Segment2D from LineDef
    collisionRadius = body.radius + lineDef.collisionThickness / 2
    if circle intersects segment -> blocked
```

Important:

```text
CollisionWorld now uses MapData, not hardcoded test data.
```

### `SpatialIndex`

Placeholder optimization class.

Current purpose:

```text
future broad-phase collision lookup
```

Potential future implementations:

```text
uniform grid
spatial hash
quadtree
BSP tree
sector-local line lists
```

Current status:

```text
not yet central to runtime collision
```

---

## `map` Package

The `map` package is the current core of the new architecture.

It defines the static level geometry used by:

```text
collision
future raycasting
debug drawing
future rendering
future map generation
future editor tools
```

### `Level`

Runtime level definition.

Current fields:

```text
LevelMetadata metadata
MapData mapData
SpawnPoint playerSpawn

playerMoveSpeed
playerRotationSpeed
playerRadius
playerHeight
playerStepHeight

objectSpawns
```

Purpose:

```text
combine metadata, static map data, and spawn configuration
```

Important distinction:

```text
map.Level is level definition.
engine.GameWorld owns the runtime Player object.
```

Current usage:

```text
LevelParser creates Level
MapCompiler validates Level
GameWorld creates Player from Level.playerSpawn
```

### `LevelMetadata`

Level descriptive and format data.

Fields:

```text
levelFormatVersion
id
name
author
version
description
```

Used by:

```text
debug overlay
loader compatibility checks
future level selection UI
```

### `MapData`

Main static map geometry container.

Stores:

```text
List<Vertex> vertices
List<LineDef> lineDefs
List<SideDef> sideDefs
List<Sector> sectors
List<SpawnPoint> spawnPoints
```

Important access methods:

```text
getVertices()
getLineDefs()
getSideDefs()
getSectors()
getSpawnPoints()
getCollisionLines()
getRaycastLines()
getBounds()
findVertexById(...)
findLineDefById(...)
findSideDefById(...)
findSectorById(...)
```

`getCollisionLines()` returns lines that affect movement:

```text
LineDef blocks movement
or
LineDef is trigger
```

`getRaycastLines()` returns lines useful for future raycasting:

```text
not hidden
and
(blocks ray or transparent)
```

### `Vertex`

2D map point.

Fields:

```text
id
x
y
```

Purpose:

```text
define endpoints for LineDef
```

Usage:

```text
LineDef.start
LineDef.end
debug drawing
raycasting intersection
collision segment construction
```

Does not contain collision or rendering behavior.

### `LineDef`

Main map boundary object.

Fields:

```text
id
start Vertex
end Vertex

frontSector
backSector

frontSide
backSide

flags
collisionThickness
```

Purpose:

```text
represent a map boundary between two vertices
```

A `LineDef` can represent:

```text
solid wall
sector border
door
window
trigger line
one-way boundary
portal
invisible blocker
ray-blocking wall
movement-blocking wall
```

Important methods:

```text
getStartPosition()
getEndPosition()
getSegment()
getBounds()
length()

hasFlag(...)
addFlag(...)
removeFlag(...)
getFlags()

blocksMovement()
blocksRay()
blocksProjectile()
isTransparent()
isTrigger()
isTwoSided()
isHidden()

getOtherSector(...)
getCollisionThickness()
setCollisionThickness(...)
```

Shared use:

```text
CollisionWorld uses LineDef for movement collision.
Future raycaster should use LineDef for ray hits.
GamePanel currently draws LineDef in top-down view.
```

### `LineFlag`

Enum describing map line behavior.

Current values:

```text
BLOCKS_MOVEMENT
BLOCKS_RAY
BLOCKS_PROJECTILE
TRANSPARENT
TRIGGER
TWO_SIDED
ONE_WAY
DOOR
WINDOW
PORTAL
HIDDEN
```

Important design detail:

```text
BLOCKS_MOVEMENT and BLOCKS_RAY are separate.
```

Examples:

```text
normal wall:
BLOCKS_MOVEMENT | BLOCKS_RAY | BLOCKS_PROJECTILE

invisible collision:
BLOCKS_MOVEMENT | HIDDEN

glass/window:
BLOCKS_MOVEMENT | TRANSPARENT

trigger line:
TRIGGER
```

### `SideDef`

Visual/material data for one side of a `LineDef`.

Fields:

```text
id
materialId
upperMaterialId
middleMaterialId
lowerMaterialId
xOffset
yOffset
transparent
```

Purpose:

```text
provide render/raycast material data for a line side
```

Future raycasting usage:

```text
determine wall texture/material
determine upper/middle/lower wall material
determine texture offset
determine transparency
```

### `Sector`

Area/region information.

Fields:

```text
id
floorHeight
ceilingHeight
floorMaterialId
ceilingMaterialId
lightLevel
```

Methods:

```text
getHeight()
hasEnoughVerticalSpace(bodyHeight)
```

Purpose:

```text
store 2.5D space information
```

Current collision use:

```text
stored and validated, but floor/ceiling collision is not fully implemented yet
```

Future use:

```text
step checks
ceiling clearance
floor/ceiling rendering
sector lighting
ramps/slopes
elevators
water/lava/damage sectors
```

### `SpawnPoint`

Spawn definition.

Fields:

```text
id
type
position
angle
sector
```

Purpose:

```text
describe where an entity starts
```

Current use:

```text
Level.playerSpawn creates Player in GameWorld
```

### `SpawnType`

Enum for spawn categories.

Current values:

```text
PLAYER
ENEMY
ITEM
CHECKPOINT
TELEPORT_DESTINATION
```

Current active usage:

```text
PLAYER
```

### `LegacyMapAdapter`

Compatibility adapter for old v1 wall data.

Purpose:

```text
convert old Wall and LineWall lists into MapData
```

Conversion behavior:

```text
Wall rectangle -> four solid LineDefs
LineWall -> one solid LineDef with collisionThickness
```

Used by:

```text
LevelParser.parseLegacyV1(...)
CollisionWorld legacy constructors if used
```

This class allows older level files to keep working while the new MapData architecture becomes standard.

### `Wall`

Temporary legacy rectangular wall.

Fields:

```text
x
y
width
height
```

Current status:

```text
legacy compatibility only
```

Should not be used by new systems.

### `LineWall`

Temporary legacy thick line wall.

Fields:

```text
x1
y1
x2
y2
thickness
```

Current status:

```text
legacy compatibility only
```

Should be replaced by:

```text
LineDef + collisionThickness
```

### `TestMapFactory`

Temporary hardcoded map factory.

Purpose:

```text
create a hardcoded MapData test map
```

Current status:

```text
transitional/debug utility
```

Since level format v2 loading is now available, normal runtime should prefer loading:

```text
levels/level99.properties
```

through `LevelLoader`.

---

## `io` Package

The `io` package loads and validates level data.

Current important direction:

```text
IO should return map.Level.
IO should not own runtime Player.
IO should not be the main level model.
```

### `LevelLoader`

File entry point for loading a level.

Responsibilities:

```text
open .properties file
load Java Properties
call LevelParser.parse(...)
return map.Level
```

Expected public usage:

```java
Level level = LevelLoader.loadLevel(path, expectedFormatVersion);
```

### `LevelParser`

Converts raw `.properties` data into `map.Level`.

Main responsibilities:

```text
validate level_format_version
parse metadata
parse player settings
parse player spawn
parse v1 legacy walls
parse v2 sectors
parse v2 sidedefs
parse v2 vertices
parse v2 linedefs
parse LineFlag sets
build MapData
call MapCompiler.compile(...)
```

Supported formats:

```text
format 1: legacy rect_wall / line_wall format
format 2: node/linedef/sector MapData format
```

Format switching:

```text
if actualFormatVersion == 1 -> parseLegacyV1(...)
if actualFormatVersion == 2 -> parseNodeMapV2(...)
```

Important parser helpers:

```text
requireProperty(...)
splitCsv(...)
parseDouble(...)
parseInt(...)
parseBoolean(...)
parseLineFlags(...)
requireVertex(...)
requireSector(...)
requireSideDef(...)
```

### `MapCompiler`

Validation and preparation step after parsing.

Current responsibilities:

```text
validate Level object
validate LevelMetadata exists
validate MapData exists
validate player spawn exists
validate player settings
validate unique vertex ids
validate unique linedef ids
validate unique sector ids
validate unique sidedef ids
validate linedef references
```

Current validation checks:

```text
no duplicate Vertex ids
no duplicate LineDef ids
no duplicate Sector ids
no duplicate SideDef ids

LineDef start cannot be null
LineDef end cannot be null
LineDef start and end cannot be same object
LineDef length cannot be zero
LineDef frontSector cannot be null
LineDef frontSide cannot be null

Player radius cannot be negative
Player height cannot be negative
Player step height cannot be negative
Player movement speed cannot be negative
Player rotation speed cannot be negative
```

Future role:

```text
build spatial index
precompute sector adjacency
validate closed sectors
precompute map bounds
prepare raycasting acceleration structures
```

### `io.Level`

Legacy/orphaned class.

Current status:

```text
still exists in source tree
should be removed
```

Reason:

```text
new IO returns map.Level
engine.GameWorld now uses map.Level
engine.GamePanel now uses map.Level
```

Do not use `io.Level` in new code.

---

## `math` Package

Reusable geometry and vector utilities.

### `Vec2`

Immutable 2D vector.

Fields:

```text
x
y
```

Methods:

```text
add(...)
subtract(...)
multiply(...)
divide(...)
dot(...)
length()
lengthSquared()
distanceTo(...)
distanceSquaredTo(...)
normalized()
perpendicularRight()
perpendicularLeft()
```

Used by:

```text
Entity position
CollisionBody position
SpawnPoint position
geometry math
line/segment calculations
```

### `Segment2D`

Mathematical line segment.

Fields:

```text
start Vec2
end Vec2
```

Methods:

```text
direction()
length()
lengthSquared()
closestPointTo(...)
distanceToPoint(...)
distanceSquaredToPoint(...)
normalLeft()
normalRight()
getBounds()
```

Used by:

```text
LineDef.getSegment()
CollisionWorld circle-vs-line collision
future raycasting
```

### `Bounds2D`

Axis-aligned bounding box.

Fields:

```text
minX
minY
maxX
maxY
```

Methods:

```text
fromPositionAndSize(...)
width()
height()
center()
contains(...)
intersects(...)
expanded(...)
closestPointTo(...)
```

Used by:

```text
MapData.getBounds()
LineDef.getBounds()
GeometryMath circle-vs-bounds checks
future spatial indexing
```

### `GeometryMath`

Static geometry helper class.

Current functions:

```text
clamp(...)
distancePointToSegment(...)
distanceSquaredPointToSegment(...)
closestPointOnSegment(...)
circleIntersectsSegment(...)
circleIntersectsRectangle(...)
circleIntersectsBounds(...)
pointInsideCircle(...)
dot(...)
```

Used by:

```text
CollisionWorld
Segment2D
Bounds2D
future raycasting helpers
```

---

## `render` Package

Current status:

```text
placeholder package
```

Classes:

```text
MapRenderer
EntityRenderer
DebugRenderer
```

Current rendering is still inside `GamePanel`.

Planned responsibility:

```text
MapRenderer:
    draw MapData, LineDefs, sectors, floors, walls

EntityRenderer:
    draw player, enemies, items, projectiles

DebugRenderer:
    draw debug overlays, vertex ids, line ids, collision shapes
```

---

# Level File Format

## Format Version 2

Current active level format:

```properties
level_format_version=2
```

Current default level:

```properties
default_level=levels/level99.properties
```

### Metadata

```properties
level_format_version=2
level_id=level99
name=Test Level 2
author=Pawel Turek
version=0.1.2
description=Test level using the new vertex/linedef/sector map format.
```

Parsed into:

```text
LevelMetadata
```

### Player Spawn and Settings

```properties
player_spawn=120,120,0,0
player_speed=180
player_rotation_speed=2.5
player_radius=6
player_height=48
player_step_height=12
```

Format:

```text
player_spawn=x,y,angle,sector_id
```

Parsed into:

```text
Level.playerSpawn
Level.playerMoveSpeed
Level.playerRotationSpeed
Level.playerRadius
Level.playerHeight
Level.playerStepHeight
```

Runtime use:

```text
GameWorld creates Player from Level.playerSpawn
```

### Sectors

```properties
sector_count=1
sector_0=0,128,floor_test,ceiling_test,1.0
```

Format:

```text
sector_<id>=floor_height,ceiling_height,floor_material,ceiling_material,light_level
```

Parsed into:

```text
Sector
```

### SideDefs

```properties
sidedef_count=2
sidedef_0=wall_default,wall_default,wall_default,0,0,false
sidedef_1=wall_inner,wall_inner,wall_inner,0,0,false
```

Format:

```text
sidedef_<id>=upper_material,middle_material,lower_material,x_offset,y_offset,transparent
```

Parsed into:

```text
SideDef
```

### Vertices

```properties
vertex_count=10
vertex_0=60,60
vertex_1=840,60
```

Format:

```text
vertex_<id>=x,y
```

Parsed into:

```text
Vertex
```

### LineDefs

```properties
linedef_count=9
linedef_0=0,1,0,-1,0,-1,BLOCKS_MOVEMENT|BLOCKS_RAY|BLOCKS_PROJECTILE,0
```

Format:

```text
linedef_<id>=start_vertex,end_vertex,front_sector,back_sector,front_sidedef,back_sidedef,flags,collision_thickness
```

Use `-1` for:

```text
no back sector
no back sidedef
```

Flags are separated with:

```text
|
```

Example:

```properties
BLOCKS_MOVEMENT|BLOCKS_RAY|BLOCKS_PROJECTILE
```

Available flags:

```text
BLOCKS_MOVEMENT
BLOCKS_RAY
BLOCKS_PROJECTILE
TRANSPARENT
TRIGGER
TWO_SIDED
ONE_WAY
DOOR
WINDOW
PORTAL
HIDDEN
```

---

# MapData Runtime Contract

`MapData` is the shared static geometry source.

Systems should access geometry through:

```text
MapData.getLineDefs()
MapData.getCollisionLines()
MapData.getRaycastLines()
MapData.getVertices()
MapData.getSectors()
MapData.getSideDefs()
```

Do not make collision or raycasting depend on:

```text
Wall
LineWall
TestMapFactory
GamePanel drawing state
```

Correct dependency direction:

```text
          MapData
          /     \
         /       \
CollisionWorld   Future Raycaster
```

Incorrect dependency direction:

```text
Raycaster -> CollisionWorld -> MapData
```

Raycasting and collision should be sibling systems, both reading from `MapData`.

---

# Collision Contract

Collision uses:

```text
CollisionBody
CollisionWorld
CollisionResult
MapData
LineDef
LineFlag
```

Current collision behavior:

```text
CollisionBody is treated as a circle.
LineDef is treated as a segment.
LineDef collisionThickness expands the effective collision radius.
Movement resolves by testing X axis then Y axis.
```

Collision line source:

```java
mapData.getCollisionLines()
```

A line blocks movement when:

```text
LineDef.blocksMovement() == true
```

Currently not implemented:

```text
sector floor height crossing
sector ceiling clearance
step height movement
doors
windows
one-way line logic
trigger activation
entity-vs-entity collision
```

---

# Raycasting Contract

Future raycasting should use:

```text
MapData
LineDef
SideDef
Sector
LineFlag
math.Segment2D
math.Vec2
```

Raycast line source:

```java
mapData.getRaycastLines()
```

A line is raycast-relevant when:

```text
!lineDef.isHidden()
and
(lineDef.blocksRay() || lineDef.isTransparent())
```

Raycaster should determine:

```text
ray/segment intersection
hit distance
hit position
which side was hit
wall material from SideDef
wall vertical span from Sector heights
whether ray stops or continues
```

Raycaster should not use:

```text
CollisionWorld.move(...)
CollisionResult
GamePanel drawing functions
legacy Wall / LineWall
```

---

# Legacy Compatibility

Version 1 level format uses:

```text
rect_wall_count
rect_wall_*
line_wall_count
line_wall_*
player_x
player_y
player_angle
```

Version 1 is parsed by:

```text
LevelParser.parseLegacyV1(...)
```

Then converted by:

```text
LegacyMapAdapter.fromLegacyWalls(...)
```

Conversion rules:

```text
Wall rectangle -> four solid LineDefs
LineWall -> one solid LineDef with collisionThickness
```

Legacy classes:

```text
map.Wall
map.LineWall
io.Level
```

Status:

```text
map.Wall and map.LineWall are still useful for compatibility.
io.Level should be removed because map.Level is now active.
```

---

# Known Transitional Classes

| Class | Status | Notes |
|---|---|---|
| `map.Wall` | Legacy | Old rectangle wall format. |
| `map.LineWall` | Legacy | Old thick-line wall format. |
| `map.LegacyMapAdapter` | Transitional | Converts old walls to MapData. Keep while v1 support exists. |
| `map.TestMapFactory` | Debug/transitional | Useful for hardcoded tests, but normal runtime should load level files. |
| `io.Level` | Orphaned legacy | Should be deleted; new code should use `map.Level`. |
| `render.MapRenderer` | Placeholder | Rendering still lives in `GamePanel`. |
| `render.EntityRenderer` | Placeholder | Entity rendering not extracted yet. |
| `render.DebugRenderer` | Placeholder | Debug rendering not extracted yet. |
| `collision.SpatialIndex` | Placeholder | Broad-phase optimization not active yet. |

---

# Recommended Next Refactors

## 1. Remove `io.Level`

Current active level type is:

```text
map.Level
```

Delete:

```text
src/io/Level.java
```

Then verify there are no imports of:

```java
import io.Level;
```

## 2. Move Rendering Out of `GamePanel`

Current `GamePanel` still contains:

```text
drawMap(...)
drawPlayer(...)
drawDebugInfo(...)
drawMapDebug(...)
```

Move these into:

```text
render.MapRenderer
render.EntityRenderer
render.DebugRenderer
```

Target direction:

```text
GamePanel.paintComponent(...)
 ├── mapRenderer.draw(...)
 ├── entityRenderer.draw(...)
 └── debugRenderer.draw(...)
```

## 3. Add Sector-Aware Collision

Current collision only checks:

```text
circle vs blocking LineDef segment
```

Next collision features:

```text
check front/back sector
check floor height difference
check player stepHeight
check ceiling clearance
allow crossing between compatible sectors
block crossing when height difference is too high
```

## 4. Add Raycasting Support

Raycaster should consume:

```text
MapData.getRaycastLines()
LineDef.getSegment()
SideDef material fields
Sector floor/ceiling heights
```

Recommended new package:

```text
raycast/
├── Raycaster.java
├── RaycastHit.java
└── RaycastResult.java
```

## 5. Improve `MapCompiler`

Future validation:

```text
validate sidedef/sector references are inside MapData
validate sector loops are closed
validate two-sided lines have back sector
validate transparent lines are two-sided when needed
validate player spawn is inside a sector
precompute map bounds
precompute adjacency
build SpatialIndex
```

## 6. Replace Legacy Test Data

Once v2 levels are stable:

```text
remove TestMapFactory from normal runtime
keep it only for tests if needed
stop adding new v1 levels
prefer level_format_version=2
```

---

# Current Architectural Rule

```text
MapData is the shared source of truth.
LineDef is the shared boundary object.
CollisionWorld interprets MapData for movement.
Future Raycaster interprets MapData for visibility.
GameWorld owns runtime entities.
GamePanel is currently a temporary renderer bridge.
```
