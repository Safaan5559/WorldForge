package com.safaan.worldforge;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.*;
import com.safaan.worldforge.engine.blocks.Block;
import com.safaan.worldforge.engine.blocks.Blocks;
import com.safaan.worldforge.engine.rendering.WorldRenderer;
import com.safaan.worldforge.engine.world.World;

public final class MainActivity extends Activity {
    private WorldRenderer renderer;
    private Block selected = Blocks.HOTBAR[0];

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(1024,1024);
        long seed=getPreferences(0).getLong("seed",1234567L);
        renderer=new WorldRenderer(new World(seed));
        FrameLayout root=new FrameLayout(this);
        GLView view=new GLView(); root.addView(view,new FrameLayout.LayoutParams(-1,-1));
        TextView cross=text("+"); FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(70,70,Gravity.CENTER); root.addView(cross,cp);
        LinearLayout hotbar=new LinearLayout(this); hotbar.setOrientation(LinearLayout.HORIZONTAL);
        for(int i=0;i<Blocks.HOTBAR.length;i++){
            final Block b=Blocks.HOTBAR[i]; Button slot=button(String.valueOf(i+1));
            slot.setOnClickListener(v->{selected=b; renderer.setSelected(b);});
            hotbar.addView(slot,new LinearLayout.LayoutParams(0,78,1));
        }
        FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(-1,78,Gravity.BOTTOM); hp.setMargins(18,0,18,18); root.addView(hotbar,hp);
        Button jump=button("JUMP"),br=button("BREAK"),pl=button("PLACE");
        add(root,jump,Gravity.RIGHT|Gravity.BOTTOM,24,110,145,90);
        add(root,br,Gravity.RIGHT|Gravity.BOTTOM,185,110,145,90);
        add(root,pl,Gravity.RIGHT|Gravity.BOTTOM,346,110,145,90);
        jump.setOnClickListener(v->renderer.jump()); br.setOnClickListener(v->renderer.breakBlock()); pl.setOnClickListener(v->renderer.placeBlock(selected));
        Button left=button("◀"); add(root,left,Gravity.LEFT|Gravity.BOTTOM,28,112,82,82); left.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN||e.getAction()==MotionEvent.ACTION_MOVE)renderer.move(1,0,.016f);return true;});
        Button right=button("▶"); add(root,right,Gravity.LEFT|Gravity.BOTTOM,122,112,82,82); right.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN||e.getAction()==MotionEvent.ACTION_MOVE)renderer.move(-1,0,.016f);return true;});
        setContentView(root);
    }
    private void add(FrameLayout r,Button b,int gravity,int right,int bottom,int w,int h){FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(w,h,gravity);p.setMargins(0,0,right,bottom);r.addView(b,p);}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextSize(12);b.setAllCaps(false);return b;}
    private TextView text(String s){TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.WHITE);t.setTextSize(32);t.setGravity(Gravity.CENTER);return t;}
    @Override protected void onPause(){super.onPause();getPreferences(0).edit().putLong("seed",renderer.world.seed()).apply();}
    private final class GLView extends android.opengl.GLSurfaceView {
        float lx,ly;
        GLView(){super(MainActivity.this);setEGLContextClientVersion(2);setRenderer(renderer);setRenderMode(RENDERMODE_CONTINUOUSLY);}
        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(),y=e.getY();
            if(e.getAction()==MotionEvent.ACTION_DOWN){lx=x;ly=y;return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE){float dx=x-lx,dy=y-ly;if(x>getWidth()*0.42f)renderer.turn(dx,dy);lx=x;ly=y;return true;}
            return true;
        }
    }
}
