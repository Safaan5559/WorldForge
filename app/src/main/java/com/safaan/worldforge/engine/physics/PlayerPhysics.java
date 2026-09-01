package com.safaan.worldforge.engine.physics;

import com.safaan.worldforge.engine.blocks.Block;
import com.safaan.worldforge.engine.world.World;

public final class PlayerPhysics {
    private static final float GRAVITY = 22f;
    private static final float JUMP = 8f;
    private static final float HEIGHT = 1.8f;
    private static final float RADIUS = .3f;
    private float vy;
    private boolean grounded;

    public void jump() { if (grounded) { vy = JUMP; grounded = false; } }
    public boolean grounded() { return grounded; }

    public void update(World world, float[] p, float dt) {
        vy -= GRAVITY * dt;
        float nx=p[0], ny=p[1]+vy*dt, nz=p[2];
        if (collides(world,nx,ny,nz)) {
            if (vy < 0) { ny=(float)Math.floor(ny)+1.001f; grounded=true; }
            else ny=p[1];
            vy=0;
        } else grounded=false;
        p[0]=nx; p[1]=ny; p[2]=nz;
    }

    private boolean collides(World w,float x,float y,float z) {
        int minX=(int)Math.floor(x-RADIUS), maxX=(int)Math.floor(x+RADIUS);
        int minY=(int)Math.floor(y), maxY=(int)Math.floor(y+HEIGHT);
        int minZ=(int)Math.floor(z-RADIUS), maxZ=(int)Math.floor(z+RADIUS);
        for(int xx=minX;xx<=maxX;xx++) for(int yy=minY;yy<=maxY;yy++) for(int zz=minZ;zz<=maxZ;zz++)
            if(w.get(xx,yy,zz)!=Block.AIR && w.get(xx,yy,zz)!=Block.WATER) return true;
        return false;
    }
}
