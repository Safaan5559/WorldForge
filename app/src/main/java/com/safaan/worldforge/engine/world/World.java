package com.safaan.worldforge.engine.world;

import com.safaan.worldforge.engine.blocks.Block;
import java.io.*;
import java.util.*;

/** Deterministic mutable voxel world. */
public final class World {
 public static final int CHUNK_SIZE=16,HEIGHT=128,SEA_LEVEL=20;
 private final long seed; private final Map<Long,Block[]> chunks=new HashMap<>(); private final Map<String,Block> modified=new HashMap<>(); private long worldTime;
 public World(long seed){this.seed=seed;} public long seed(){return seed;} public long worldTime(){return worldTime;} public void tick(){worldTime++;}
 private static long ck(int x,int z){return (((long)x)<<32)^(z&0xffffffffL);} private static int i(int x,int y,int z){return(y*16+z)*16+x;} private static String p(int x,int y,int z){return x+","+y+","+z;}
 private static int hash(int x,int z){long h=((long)x*0x9E3779B97F4A7C15L)^((long)z*0xC2B2AE3D27D4EB4FL);h^=h>>>30;h*=0xBF58476D1CE4E5B9L;h^=h>>>27;return(int)(h^(h>>>32))&0x7fffffff;}
 private Block[] generate(int cx,int cz){Block[] a=new Block[16*HEIGHT*16];Arrays.fill(a,Block.AIR);for(int lx=0;lx<16;lx++)for(int lz=0;lz<16;lz++){int wx=cx*16+lx,wz=cz*16+lz;double n=Math.sin(wx*.055)*6+Math.cos(wz*.047)*5+Math.sin((wx+wz)*.021)*3+Math.cos((wx-wz)*.013)*2;int h=Math.max(4,Math.min(86,36+(int)Math.round(n)));for(int y=0;y<=h;y++)a[i(lx,y,lz)]=y<h-5?Block.STONE:y<h-1?Block.DIRT:(h<=SEA_LEVEL+1?Block.SAND:Block.GRASS);if(h<SEA_LEVEL)for(int y=h+1;y<=SEA_LEVEL;y++)a[i(lx,y,lz)]=Block.WATER;if(h>SEA_LEVEL+4&&hash(wx,wz)%53==0){for(int y=h;y<h+5&&y<HEIGHT;y++)a[i(lx,y,lz)]=Block.WOOD;for(int ox=-2;ox<=2;ox++)for(int oz=-2;oz<=2;oz++)for(int oy=h+3;oy<=h+6&&oy<HEIGHT;oy++)if(Math.abs(ox)+Math.abs(oz)+(oy==h+6?1:0)<=3&&lx+ox>=0&&lx+ox<16&&lz+oz>=0&&lz+oz<16)a[i(lx+ox,oy,lz+oz)]=Block.LEAVES;}}return a;}
 private Block[] chunk(int cx,int cz){long k=ck(cx,cz);Block[] a=chunks.get(k);if(a==null){a=generate(cx,cz);chunks.put(k,a);}return a;}
 public Block get(int x,int y,int z){if(y<0||y>=HEIGHT)return Block.AIR;Block b=modified.get(p(x,y,z));if(b!=null)return b;return chunk(Math.floorDiv(x,16),Math.floorDiv(z,16))[i(Math.floorMod(x,16),y,Math.floorMod(z,16))];}
 public void set(int x,int y,int z,Block b){if(y<0||y>=HEIGHT||b==null)return;modified.put(p(x,y,z),b);chunk(Math.floorDiv(x,16),Math.floorDiv(z,16))[i(Math.floorMod(x,16),y,Math.floorMod(z,16))]=b;}
 public boolean solid(int x,int y,int z){Block b=get(x,y,z);return b!=Block.AIR&&b!=Block.WATER;}
 public void save(File f,float x,float y,float z,float yaw,float pitch,Block[] inv,int selected)throws IOException{File t=new File(f+".tmp");try(DataOutputStream o=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(t)))){o.writeInt(0x57464F52);o.writeInt(3);o.writeLong(seed);o.writeLong(worldTime);o.writeFloat(x);o.writeFloat(y);o.writeFloat(z);o.writeFloat(yaw);o.writeFloat(pitch);o.writeInt(selected);o.writeInt(inv.length);for(Block b:inv)o.writeByte(b.ordinal());o.writeInt(modified.size());for(Map.Entry<String,Block>e:modified.entrySet()){String[]q=e.getKey().split(",");o.writeInt(Integer.parseInt(q[0]));o.writeInt(Integer.parseInt(q[1]));o.writeInt(Integer.parseInt(q[2]));o.writeByte(e.getValue().ordinal());}}if(f.exists()&&!f.delete())throw new IOException("Cannot replace save");if(!t.renameTo(f))throw new IOException("Cannot create save");}
 public static SaveData load(File f)throws IOException{try(DataInputStream in=new DataInputStream(new BufferedInputStream(new FileInputStream(f)))){if(in.readInt()!=0x57464F52||in.readInt()!=3)throw new IOException("Unsupported save");SaveData s=new SaveData(in.readLong());s.time=in.readLong();s.x=in.readFloat();s.y=in.readFloat();s.z=in.readFloat();s.yaw=in.readFloat();s.pitch=in.readFloat();s.selected=in.readInt();int n=in.readInt();s.inventory=new Block[n];for(int i=0;i<n;i++)s.inventory[i]=Block.values()[in.readUnsignedByte()];int c=in.readInt();for(int i=0;i<c;i++)s.modified.put(p(in.readInt(),in.readInt(),in.readInt()),Block.values()[in.readUnsignedByte()]);return s;}}
 public void apply(SaveData s){worldTime=s.time;modified.clear();modified.putAll(s.modified);chunks.clear();}
 public static final class SaveData{public final long seed;public long time;public float x,y,z,yaw,pitch;public int selected;public Block[] inventory;public final Map<String,Block> modified=new HashMap<>();SaveData(long seed){this.seed=seed;}}
}
