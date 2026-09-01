package com.safaan.worldforge.engine.world;

import com.safaan.worldforge.engine.blocks.Block;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class World {
    public static final int CHUNK_X=16, CHUNK_Z=16, HEIGHT=128;
    private final long seed;
    private final Map<Long, Block[]> chunks = new HashMap<>();
    private final Map<Long, Block> modified = new HashMap<>();
    public World(long seed){this.seed=seed;}
    public long seed(){return seed;}
    private long key(int cx,int cz){return (((long)cx)<<32) ^ (cz&0xffffffffL);}
    private Block[] chunk(int cx,int cz){
        long k=key(cx,cz); Block[] a=chunks.get(k); if(a!=null)return a;
        a=new Block[CHUNK_X*HEIGHT*CHUNK_Z]; Random r=new Random(seed ^ (cx*341873128712L) ^ (cz*132897987541L));
        for(int x=0;x<16;x++) for(int z=0;z<16;z++){
            int wx=cx*16+x,wz=cz*16+z; double n=Math.sin(wx*.055)*5+Math.cos(wz*.047)*4+Math.sin((wx+wz)*.017)*3;
            int h=Math.max(3,Math.min(70,34+(int)n));
            for(int y=0;y<HEIGHT;y++){ Block b=Block.AIR; if(y<h-4)b=Block.STONE; else if(y<h-1)b=Block.DIRT; else if(y==h-1)b=(h<18?Block.SAND:Block.GRASS); else if(y<18)b=Block.WATER; a[idx(x,y,z)]=b; }
        }
        chunks.put(k,a); return a;
    }
    private int idx(int x,int y,int z){return (y*16+z)*16+x;}
    public Block get(int x,int y,int z){if(y<0||y>=HEIGHT)return Block.AIR; int cx=Math.floorDiv(x,16),cz=Math.floorDiv(z,16),lx=Math.floorMod(x,16),lz=Math.floorMod(z,16); Block m=modified.get((((long)x&0x1fffffL)<<42)|(((long)y&0x7fL)<<35)|((long)z&0x7ffffffffL)); return m!=null?m:chunk(cx,cz)[idx(lx,y,lz)];}
    public void set(int x,int y,int z,Block b){if(y<0||y>=HEIGHT)return; long k=(((long)x&0x1fffffL)<<42)|(((long)y&0x7fL)<<35)|((long)z&0x7ffffffffL); modified.put(k,b); chunk(Math.floorDiv(x,16),Math.floorDiv(z,16))[idx(Math.floorMod(x,16),y,Math.floorMod(z,16))]=b;}
}
