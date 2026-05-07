# -MOOD Technical Documentation

## Project Architecture

The project should be structured around separated responsibilities:

```text
src/
├── app/
│   └── Main.java
│
├── engine/
│   ├── GameLoop.java
│   ├── GameWorld.java
│   └── GamePanel.java
│
├── entities/
│   ├── Entity.java
│   ├── Player.java
│   └── EntityBody.java
│
├── map/
│   ├── Level.java
│   ├── LevelMetadata.java
│   ├── MapData.java
│   ├── Vertex.java
│   ├── LineDef.java
│   ├── SideDef.java
│   ├── Sector.java
│   └── SpawnPoint.java
│
├── collision/
│   ├── CollisionWorld.java
│   ├── CollisionBody.java
│   ├── CollisionResult.java
│   ├── CollisionFlags.java
│   └── SpatialIndex.java
│
├── render/
│   ├── MapRenderer.java
│   ├── EntityRenderer.java
│   └── DebugRenderer.java
│
├── io/
│   ├── LevelLoader.java
│   ├── LevelParser.java
│   └── MapCompiler.java
│
├── config/
│   ├── GameConfig.java
│   ├── GameSettings.java
│   └── KeyBindings.java
│
├── input/
│   └── InputHandler.java
│
└── math/
    ├── Vec2.java
    ├── Segment2D.java
    ├── Bounds2D.java
    └── GeometryMath.java
```

## Core Design Rules

```text
Map data describes space.
Collision system interprets space.
Renderer visualizes space.
GameWorld manages runtime state.
```

The map should not be built around temporary wall objects such as:

```text
Wall
LineWall
RectangleWall
DoorWall
WindowWall
RampWall
StepWall
```

Instead, the map should be represented using a structure:

```text
MapData
├── vertices
├── linedefs
├── sidedefs
├── sectors
└── spawns
```

A wall, door, window, ledge, ramp boundary, or trigger should be interpreted from:

```text
LineDef + Sector + SideDef + flags + height data
```

This avoids creating a separate class for every possible map feature.

---

# Package Descriptions

## `app`

The `app` package contains the application entry point.

### `Main`

Responsible for:

```text
starting the program
loading global settings
creating the game window
creating the main game panel/world
starting the game loop
```

Should not contain:

```text
collision logic
map parsing logic
rendering logic
gameplay rules
```

---

## `engine`

The `engine` package coordinates the main runtime systems.

### `GameLoop`

Responsible for:

```text
fixed update timing
delta time calculation
calling update methods
requesting rendering/repaint
```

Future expansion:

```text
pause support
variable time scale
FPS limiting
separate update/render timing
profiling update and render time
```

---

### `GameWorld`

Main runtime container for the current game session.

Responsible for:

```text
current Level
current MapData
active Player
active entities
CollisionWorld
game simulation update
runtime state
```

Example flow:

```text
read input
calculate intended movement
ask CollisionWorld for allowed movement
update player/entity positions
update runtime objects
```

Should not directly draw graphics.

Future expansion:

```text
enemy management
projectile management
door state management
trigger activation
level transitions
save/load runtime state
```

---

### `GamePanel`

Swing rendering surface and input focus component.

Responsible for:

```text
holding screen size
receiving repaint calls
owning Graphics2D drawing entry point
forwarding render work to renderers
holding input focus
```

Should not contain:

```text
collision math
map parsing
player movement rules
level format logic
```

Future expansion:

```text
fullscreen support
camera handling
render scaling
debug overlay toggles
different render modes
```

---

# `entities`

The `entities` package contains runtime objects that exist in the game world.

## `Entity`

Base concept for any object existing in the world.

Responsible for:

```text
entity id
position
angle
active state
common runtime data
```

Possible entity types:

```text
Player
Enemy
Projectile
Item
DoorObject
MovingPlatform
TriggerVolume
```

Future expansion:

```text
entity components
health
animation state
AI state
physics state
serialization
```

---

## `Player`

Represents the player runtime object.

Responsible for:

```text
player position
view angle
movement speed
rotation speed
player-specific state
```

Should not contain:

```text
map collision rules
sector transition rules
line intersection math
rendering code
```

Future expansion:

```text
health
inventory
weapons
current sector
camera height
crouching
jumping
interaction state
```

---

## `EntityBody`

Describes the physical body of an entity.

Responsible for:

```text
collision radius
collision height
step height
collision type
collision flags
```

Current simple model:

```text
circle body
x/y position
radius
```

Future Doom-like model:

```text
vertical cylinder
x/y position
radius
height
current floor height
maximum step height
```

Future expansion:

```text
projectile body
trigger-only body
non-blocking body
flying body
crouching body
custom hitboxes
```

---

# `map`

The `map` package contains static level and geometry data.

## `Level`

High-level level definition.

Responsible for:

```text
metadata
map data
spawn data
level objects
```

Preferred structure:

```text
Level
├── LevelMetadata
├── MapData
├── SpawnPoint
└── LevelObjects
```

Should not directly own runtime `Player`.

Correct responsibility split:

```text
Level stores where the player starts.
GameWorld creates and owns the actual Player.
```

Future expansion:

```text
multiple spawn points
level scripts
level objectives
music id
skybox/sky color
environment settings
```

---

## `LevelMetadata`

Stores descriptive and compatibility information.

Responsible for:

```text
level format version
level id
name
author
level version
description
```

Used for:

```text
debug display
level selection menu
editor information
format compatibility checks
```

Future expansion:

```text
difficulty info
required game version
tags
preview image path
music track
next level id
```

---

## `MapData`

Main geometry container.

Responsible for holding:

```text
vertices
linedefs
sidedefs
sectors
spawn points
map bounds
```

Replaces direct level storage of:

```text
walls
lineWalls
```

Conceptual structure:

```text
MapData
├── List<Vertex>
├── List<LineDef>
├── List<SideDef>
├── List<Sector>
└── List<SpawnPoint>
```

Future expansion:

```text
map bounds
spatial lookup data
BSP tree
visibility zones
navigation data
precomputed sector adjacency
```

---

## `Vertex`

A single point in 2D map space.

Responsible for:

```text
x coordinate
y coordinate
```

Used by:

```text
LineDef
Sector construction
map editor
collision checks
rendering
```

Should not contain:

```text
collision behavior
wall type
texture data
sector data
rendering code
```

Future expansion:

```text
editor id
snap group
height override if needed
metadata for generated maps
```

---

## `LineDef`

A boundary between two vertices.

Responsible for:

```text
start vertex
end vertex
front sector reference
back sector reference
front sidedef reference
back sidedef reference
collision flags
special type
```

Important rule:

```text
LineDef does not always mean wall.
LineDef means boundary.
```

Possible meanings:

```text
solid wall
door
window
sector boundary
trigger line
invisible blocker
ledge
one-way boundary
portal
```

Collision interpretation depends on:

```text
blocking flags
front/back sector
floor height difference
ceiling height
door/window state
special behavior
```

Future expansion:

```text
door actions
switch actions
teleport triggers
sound blocking
visibility blocking
projectile blocking
one-way collision
animated surfaces
```

---

## `SideDef`

Visual data for one side of a `LineDef`.

Responsible for:

```text
material or texture id
upper wall visual data
middle wall visual data
lower wall visual data
texture offset
transparency
color/tint
```

Reason for existence:

```text
A line can look different from each side.
```

Example:

```text
Room A sees stone.
Room B sees metal.
```

Future expansion:

```text
animated textures
transparent windows
damage surfaces
texture scrolling
light emission
decals
editor tags
```

---

## `Sector`

A closed area of the map.

Responsible for:

```text
floor height
ceiling height
floor material
ceiling material
light level
sector behavior
```

Examples:

```text
normal room
raised platform
stair step
pit
low tunnel
water area
damage floor
```

Basic sector data:

```text
floorHeight
ceilingHeight
floorMaterial
ceilingMaterial
lightLevel
```

Future expansion:

```text
sloped floors
ramps
moving floors
moving ceilings
water/lava
damage over time
sector gravity
ambient sounds
fog
colored lighting
secret areas
```

---

## `SpawnPoint`

Defines where something starts.

Responsible for:

```text
x position
y position
angle
sector id
spawn type
```

Spawn types:

```text
player
enemy
item
checkpoint
teleport destination
```

Should not be the runtime entity itself.

Future expansion:

```text
difficulty filtering
spawn conditions
spawn groups
random spawn selection
scripted spawn triggers
```

---

# `collision`

The `collision` package handles movement rules and collision resolution.

## `CollisionWorld`

Main collision system.

Responsible for:

```text
checking movement against map geometry
checking entity bodies against LineDefs
checking sector transitions
checking step height
checking ceiling height
resolving sliding
detecting trigger contact
returning movement results
```

Main question:

```text
Can this body move from position A to position B?
```

Collision checks should consider:

```text
blocking LineDefs
doors
windows
sector boundaries
floor height difference
ceiling clearance
one-way boundaries
triggers
```

Future expansion:

```text
moving doors
moving platforms
dynamic blockers
enemy collision
projectile collision
raycasts
line of sight checks
sound propagation checks
```

---

## `CollisionBody`

Physical collision description of an entity.

Responsible for:

```text
radius
height
step height
collision layer
collision mask
body type
```

Simple current form:

```text
circle
```

Future form:

```text
vertical cylinder
```

Possible body types:

```text
player
enemy
projectile
item
trigger
static obstacle
```

Future expansion:

```text
crouch height
flying body
ghost body
non-solid body
custom collision layers
```

---

## `CollisionResult`

Result of a movement or collision query.

Responsible for storing:

```text
was movement blocked
final allowed position
collision normal
hit LineDef
entered Sector
touched triggers
did slide occur
```

Better than returning only:

```text
true / false
```

Example result data:

```text
blocked = true
finalPosition = adjusted position
hitLine = line id
normal = wall normal
enteredSector = sector id
```

Future expansion:

```text
multiple hits
impact speed
surface material
sound effect trigger
damage trigger
collision priority
```

---

## `CollisionFlags`

Defines collision behavior.

Possible flags:

```text
BLOCKING
TRIGGER
TRANSPARENT
SHOOT_THROUGH
WALK_THROUGH
ONE_WAY
DOOR
WINDOW
LEDGE
PORTAL
```

Used by:

```text
LineDef
CollisionBody
CollisionWorld
```

Purpose:

```text
Avoid creating separate geometry classes for every wall behavior.
```

Future expansion:

```text
blocks player only
blocks enemies only
blocks projectiles only
blocks vision
blocks sound
requires key
is climbable
is destructible
```

---

## `SpatialIndex`

Optimization structure for querying nearby map geometry.

Responsible for:

```text
storing LineDefs by area
finding nearby LineDefs
reducing collision checks
improving map query speed
```

Initial implementation can be skipped for small maps.

Possible implementations:

```text
uniform grid
spatial hash
quadtree
BSP tree
```

Recommended early version:

```text
simple grid / spatial hash
```

Future expansion:

```text
dynamic object indexing
sector-based lookup
BSP rendering support
visibility culling
raycast acceleration
```

---

# `render`

The `render` package handles drawing.

Map and entity classes should not draw themselves.

## `MapRenderer`

Draws map geometry.

Responsible for:

```text
drawing sectors
drawing LineDefs
drawing wall surfaces
drawing floor/ceiling previews
drawing top-down map view
```

Should read from:

```text
MapData
Sector
LineDef
SideDef
```

Should not modify map data.

Future expansion:

```text
Doom-like raycast renderer
editor renderer
minimap renderer
sector lighting
texture rendering
fog
visibility clipping
```

---

## `EntityRenderer`

Draws runtime entities.

Responsible for:

```text
drawing player
drawing enemies
drawing projectiles
drawing items
drawing entity debug shapes
```

Should read from:

```text
Entity
Player
EntityBody
```

Future expansion:

```text
sprite rendering
animation frames
weapon rendering
billboard sprites
entity shadows
health bars
```

---

## `DebugRenderer`

Draws debugging information.

Responsible for:

```text
collision lines
vertex ids
LineDef ids
sector ids
player radius
current sector
wall normals
bounding boxes
spatial grid cells
trigger lines
```

Used for development and map generation debugging.

Future expansion:

```text
collision result visualization
raycast visualization
pathfinding debug
sector adjacency debug
performance overlay
map validation warnings
```

---

# `io`

The `io` package handles loading and converting level files.

## `LevelLoader`

Loads a level file from disk.

Responsible for:

```text
opening levelXX.properties
reading raw properties
passing data to parser
returning Level
```

Should not contain all map-building logic forever.

Future expansion:

```text
loading level folders
loading external map files
loading JSON/XML/custom formats
resource path resolution
error reporting
```

---

## `LevelParser`

Converts raw file data into structured level objects.

Responsible for parsing:

```text
metadata
vertices
linedefs
sidedefs
sectors
spawn points
objects
```

Understands:

```text
level format version
property names
data layout
default values
validation rules
```

Future expansion:

```text
multiple level format versions
format migration
human-readable error messages
editor export support
```

---

## `MapCompiler`

Prepares parsed map data for runtime use.

Responsible for:

```text
validating geometry
checking closed sectors
linking LineDefs to Sectors
linking SideDefs
building spatial index
calculating bounds
checking missing references
preparing collision data
```

Difference between parser and compiler:

```text
Parser reads the file format.
Compiler prepares the map for gameplay.
```

Future expansion:

```text
sector adjacency graph
BSP generation
visibility precomputation
collision mesh generation
map optimization
automatic normal calculation
map error reports
```

---

# `config`

The `config` package handles global configuration.

## `GameConfig`

Loads global configuration files.

Responsible for:

```text
loading game.properties
loading controls.properties
creating GameSettings
creating KeyBindings
```

Should not load level geometry.

Future expansion:

```text
video settings
audio settings
debug settings
developer options
config saving
```

---

## `GameSettings`

Stores global game settings.

Responsible for:

```text
window width
window height
target FPS
game title
game version
game build
debug mode
default level path
expected level format version
```

Should not contain:

```text
level geometry
specific wall data
current player state
runtime enemy data
```

Future expansion:

```text
fullscreen
resolution scale
audio volume
mouse sensitivity
render distance
field of view
```

---

## `KeyBindings`

Stores key mappings.

Responsible for:

```text
forward key
backward key
strafe left key
strafe right key
rotate left key
rotate right key
debug toggle key
```

Should describe controls, not current input state.

Future expansion:

```text
mouse bindings
controller bindings
rebinding support
multiple input profiles
```

---

# `input`

The `input` package tracks input state.

## `InputHandler`

Reads keyboard/mouse input.

Responsible for:

```text
key pressed state
key released state
input action state
debug toggle request
```

Should not directly move the player.

Correct flow:

```text
InputHandler stores input state.
GameWorld reads input state.
GameWorld decides intended movement.
CollisionWorld validates movement.
Player position is updated.
```

Future expansion:

```text
mouse look
controller support
input buffering
action mapping
menu input
text input for editor
```

---

# `math`

The `math` package contains reusable geometry and vector utilities.

## `Vec2`

2D vector.

Responsible for:

```text
x
y
addition
subtraction
scaling
normalization
length
dot product
distance
```

Used for:

```text
movement vectors
collision normals
wall directions
sliding
raycasts
```

Future expansion:

```text
cross product helper
angle conversion
rotation
projection
reflection
```

---

## `Segment2D`

Mathematical line segment.

Responsible for:

```text
start point
end point
direction
length
closest point calculation
```

Used by:

```text
LineDef geometry
collision checks
raycasts
editor tools
```

Should not contain:

```text
sector references
collision flags
texture data
rendering code
```

Future expansion:

```text
intersection tests
normal calculation
distance to point
splitting
clipping
```

---

## `Bounds2D`

Axis-aligned bounding box.

Responsible for:

```text
min x
min y
max x
max y
width
height
```

Used for:

```text
quick collision rejection
map bounds
camera bounds
spatial indexing
debug drawing
```

Future expansion:

```text
bounds merging
bounds intersection
circle bounds
segment bounds
sector bounds
```

---

## `GeometryMath`

Static geometry utility functions.

Responsible for:

```text
clamp
distance point to segment
circle vs segment
line intersection
point inside polygon
projection
normal calculation
```

Purpose:

```text
Keep geometry math out of GamePanel, Player, LineDef, and CollisionWorld.
```

Future expansion:

```text
ray vs segment
swept circle collision
polygon winding
sector area calculation
nearest point search
angle helpers
```

---

# Level File Structure

Current simple level layout:

```text
levels/
├── level00.properties
├── level01.properties
└── level02.properties
```

Future expanded layout:

```text
levels/
└── level00/
    ├── level.properties
    ├── map.properties
    ├── objects.properties
    └── scripts.properties
```

## `levelXX.properties`

Should contain level definition data:

```text
level metadata
player spawn position
vertices
linedefs
sidedefs
sectors
doors
triggers
items
enemy spawn points
materials
floor heights
ceiling heights
lighting
```

Should not contain runtime state:

```text
current player health
current player ammo
enemy current position
door currently half-open
particles
animation frame
temporary runtime state
```

Runtime state belongs in:

```text
GameWorld
save files
entity runtime objects
```

---

# Map Representation

The long-term map representation should be:

```text
Vertex = point in 2D space
LineDef = boundary between two vertices
SideDef = visual data for one side of a line
Sector = closed area with floor/ceiling/light/material data
```

Example room:

```text
A ---- B
|      |
| Room |
|      |
D ---- C
```

Represented as:

```text
Vertices:
A, B, C, D

LineDefs:
A-B
B-C
C-D
D-A

Sector:
Room uses those boundaries
```

An outer wall:

```text
front sector = Room
back sector = none
blocking = true
```

A doorway:

```text
front sector = Room A
back sector = Room B
blocking depends on door state
```

A step:

```text
front sector floor = 0
back sector floor = 8
movement allowed if step height is small enough
```

A ledge:

```text
front sector floor = 0
back sector floor = 80
movement blocked if height difference is too large
```

---

# Collision Model

Initial collision model:

```text
Player = circle
Map boundary = LineDef segment
Collision = circle vs blocking segment
```

Future collision model:

```text
Player = vertical cylinder
Map = sector-aware 2.5D geometry
Collision = cylinder vs LineDef + Sector heights
```

Movement process:

```text
1. Player wants to move by dx/dy.
2. GameWorld sends movement request to CollisionWorld.
3. CollisionWorld checks nearby LineDefs.
4. Blocking lines stop or slide movement.
5. Sector transitions check floor and ceiling height.
6. CollisionWorld returns CollisionResult.
7. GameWorld applies final position.
```

Main collision question:

```text
Can this body cross this LineDef?
```

The answer depends on:

```text
blocking flag
door state
window state
front/back sector
floor height difference
ceiling clearance
one-way flag
trigger behavior
body type
```

---

# Rendering Model

Rendering should be separate from data.

Avoid:

```text
lineDef.draw(g2)
sector.draw(g2)
player.draw(g2)
```

Prefer:

```text
MapRenderer draws MapData.
EntityRenderer draws Entities.
DebugRenderer draws debug information.
```

This allows multiple render modes:

```text
top-down debug view
Doom-like raycast view
editor view
minimap view
collision debug view
```

---

# Migration Plan

## Stage 1

Keep current `Wall` and `LineWall`.

Move collision math out of `GamePanel`.

Create:

```text
math/GeometryMath
collision/CollisionWorld
```

## Stage 2

Introduce:

```text
map/Vertex
map/LineDef
```

Make `LineWall` obsolete.

Collision becomes:

```text
circle vs blocking LineDef
```

## Stage 3

Introduce:

```text
map/MapData
```

Move map geometry from direct `Level` fields into `MapData`.

Replace:

```text
Level.walls
Level.lineWalls
```

With:

```text
Level.mapData
```

## Stage 4

Introduce:

```text
map/Sector
map/SideDef
```

Map becomes sector-aware.

## Stage 5

Add sector-aware collision:

```text
floor height checks
ceiling height checks
step height checks
sector transitions
```

## Stage 6

Add future geometry features:

```text
stairs
ramps
doors
windows
elevated platforms
ledges
triggers
moving sectors
```

---

# Long-Term Expansion Ideas

## Geometry

```text
sloped sectors
ramps
curved walls approximated by segments
moving floors
moving ceilings
destructible walls
secret doors
windows
portals
```

## Collision

```text
sliding along angled walls
swept collision to prevent tunneling
dynamic obstacles
entity vs entity collision
projectile collision
line of sight checks
sound propagation checks
```

## Rendering

```text
raycast renderer
textured walls
sector lighting
transparent walls
sprite entities
animated surfaces
minimap
editor overlay
```

## Level Editing

```text
in-game map editor
vertex editing
sector creation
line splitting
automatic sector detection
level validation
map export
```

## Runtime Systems

```text
doors
switches
triggers
enemy spawns
items
scripted events
save/load system
level transitions
```

---

# Final Architecture Rule

```text
Do not build the game around Wall objects.

Build the game around:
Vertex
LineDef
SideDef
Sector

Then let:
CollisionWorld decide how they block movement.
MapRenderer decide how they are drawn.
GameWorld decide how runtime state changes.
```
