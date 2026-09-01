package com.safaan.worldforge.game.player;

import com.safaan.worldforge.engine.blocks.Block;
import com.safaan.worldforge.engine.blocks.Blocks;
import com.safaan.worldforge.engine.physics.PlayerPhysics;
import com.safaan.worldforge.engine.world.World;

public final class Player {
    public final float[] position = {8f, 45f, 8f};
    public float yaw, pitch;
    public int selectedSlot;
    public final Block[] inventory = Blocks.HOTBAR.clone();
    private final PlayerPhysics physics = new PlayerPhysics();

    public void update(World world, float dt) { physics.update(world, position, Math.min(dt,.05f)); }
    public void jump() { physics.jump(); }
    public boolean grounded() { return physics.grounded(); }
    public Block selectedBlock() { return inventory[selectedSlot]; }
    public void select(int slot) { if(slot>=0 && slot<inventory.length) selectedSlot=slot; }
}
