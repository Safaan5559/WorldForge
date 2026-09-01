# WorldForge

#WORLD FORGE — PHASE 1

Build a small but REAL playable Android voxel sandbox called World Forge.

Do NOT implement multiplayer, NPCs, farming, weather, modding, advanced AI, Forge Logic, or other advanced systems yet.

Goal

Create the minimum working version of a custom voxel game engine.

The player must be able to:

1. Launch the game.
2. Enter a procedurally generated voxel world.
3. Look around.
4. Move.
5. Jump.
6. Break blocks.
7. Place blocks.
8. See a hotbar.
9. Select blocks from the hotbar.
10. Exit and reopen the world with the world saved.

CUSTOM ENGINE

Create a modular engine named:

WorldForge Engine

Separate engine code from game code.

Use modules such as:

engine/
  core/
  rendering/
  world/
  chunks/
  blocks/
  physics/
  input/
  save/

game/
  player/
  blocks/
  ui/
  worldgen/

android/

Do not use Unity or Unreal.

VOXEL WORLD

Use chunks.

Initial chunk size:

16 × 16 × 128

Only load chunks near the player.

Generate a deterministic world from a seed.

Include:

- Grass
- Dirt
- Stone
- Sand
- Water
- Wood
- Leaves

Generate simple terrain with hills, plains, and oceans.

RENDERING

Create an optimized voxel renderer.

Requirements:

- Only render visible block faces.
- Generate chunk meshes.
- Rebuild only chunks affected by block changes.
- Support transparent water.
- Add simple lighting.
- Add a sky.
- Add basic fog.
- Include frustum/distance culling where practical.

Prioritize Android performance.

#PLAYER

Implement:

- First-person camera
- Walking
- Sprinting
- Jumping
- Gravity
- Block collision
- Touch camera controls

Android controls:

- Left virtual joystick = movement
- Right side = camera
- Jump button
- Break button
- Place button
- Inventory button
- Hotbar

Make buttons large enough for phone screens.

#BLOCK INTERACTION

Allow the player to:

- Look at a block.
- Highlight the targeted block.
- Hold break to mine it.
- Remove the block.
- Place a selected block next to another block.

Add simple breaking particles.

#INVENTORY

Create a basic hotbar with 8 slots.

Allow the player to select blocks.

No advanced inventory system yet.

#SAVING

Save:

- World seed
- Player position
- Inventory
- Modified blocks
- World time

Use a background save operation when practical.

UI

Create:

Main Menu

WORLD FORGE

Buttons:

PLAY
CREATE WORLD
SETTINGS

In-game

#Display:

- Crosshair
- Hotbar
- Selected block
- Basic health indicator

PERFORMANCE

Prioritize:

- Low RAM usage
- Fast startup
- Stable frame rate
- Chunk streaming
- Efficient block storage
- Minimal object allocation
- No unnecessary dependencies

Target 60 FPS on capable Android devices.

DEVELOPMENT RULE

DO NOT try to implement the entire World Forge specification.

Only implement Phase 1.

Do not create hundreds of placeholder files.

Every implemented feature must actually work.

First create the project structure, then implement the engine, then build the playable prototype.

After implementation, compile the project and fix compilation/runtime errors.

The final result of this phase must be a working playable voxel sandbox, not a collection of placeholder classes.


Make it mobile friendly
