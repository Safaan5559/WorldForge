package com.safaan.worldforge.engine.save;

import android.content.Context;
import com.safaan.worldforge.engine.world.World;
import com.safaan.worldforge.game.player.Player;
import java.io.*;

public final class WorldSave {
    private WorldSave() {}
    private static final String FILE="worldforge.dat";

    public static void save(Context c, World w, Player p) {
        File f=new File(c.getFilesDir(),FILE);
        try(DataOutputStream out=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)))) {
            out.writeLong(w.seed());
            out.writeFloat(p.position[0]); out.writeFloat(p.position[1]); out.writeFloat(p.position[2]);
            out.writeFloat(p.yaw); out.writeFloat(p.pitch); out.writeInt(p.selectedSlot);
            for(int i=0;i<p.inventory.length;i++) out.writeByte(p.inventory[i].ordinal());
        } catch(IOException ignored) {}
    }

    public static boolean load(Context c, Player p, long[] seed) {
        File f=new File(c.getFilesDir(),FILE); if(!f.exists()) return false;
        try(DataInputStream in=new DataInputStream(new BufferedInputStream(new FileInputStream(f)))) {
            seed[0]=in.readLong();
            p.position[0]=in.readFloat(); p.position[1]=in.readFloat(); p.position[2]=in.readFloat();
            p.yaw=in.readFloat(); p.pitch=in.readFloat(); p.selectedSlot=in.readInt();
            for(int i=0;i<p.inventory.length;i++) { int id=in.readUnsignedByte(); if(id>=0 && id<com.safaan.worldforge.engine.blocks.Block.values().length) p.inventory[i]=com.safaan.worldforge.engine.blocks.Block.values()[id]; }
            return true;
        } catch(IOException | RuntimeException e) { return false; }
    }
}
