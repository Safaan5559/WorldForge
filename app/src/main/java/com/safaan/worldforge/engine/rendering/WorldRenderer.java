package com.safaan.worldforge.engine.rendering;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import com.safaan.worldforge.engine.blocks.Block;
import com.safaan.worldforge.engine.world.World;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/** Mobile-first OpenGL ES 2 voxel renderer and first-person controller. */
public final class WorldRenderer implements GLSurfaceView.Renderer {
    public final World world;
    public float x=8f,y,z=8f,yaw=0f,pitch=-8f;
    public boolean sprint;

    private float vy,inputF,inputS;
    private boolean grounded;
    private int program,pa,ca,mu;
    private FloatBuffer vertices,colors;
    private int vertexCount;
    private long lastNs;
    private int viewportW=1,viewportH=1;
    private final float[] projection=new float[16],view=new float[16],vp=new float[16];

    public WorldRenderer(World w){
        world=w;
        y=world.surfaceY(8,8)+1.01f;
        grounded=true;
    }

    public void turn(float dx,float dy){
        yaw=(yaw+dx*.16f)%360f;
        pitch=Math.max(-88f,Math.min(88f,pitch+dy*.16f));
    }
    public void setInput(float forward,float strafe){
        inputF=Math.max(-1f,Math.min(1f,forward));
        inputS=Math.max(-1f,Math.min(1f,strafe));
    }
    public void setSprint(boolean value){sprint=value;}
    public void jump(){
        if(grounded){vy=6.3f;grounded=false;}
    }

    private boolean collides(float px,float py,float pz){
        final float r=.28f, h=1.8f;
        int minX=(int)Math.floor(px-r),maxX=(int)Math.floor(px+r);
        int minY=(int)Math.floor(py),maxY=(int)Math.floor(py+h);
        int minZ=(int)Math.floor(pz-r),maxZ=(int)Math.floor(pz+r);
        for(int bx=minX;bx<=maxX;bx++)
            for(int by=minY;by<=maxY;by++)
                for(int bz=minZ;bz<=maxZ;bz++)
                    if(world.solid(bx,by,bz)) return true;
        return false;
    }

    private void move(float dx,float dy,float dz){
        if(dx!=0f&&!collides(x+dx,y,z)) x+=dx;
        if(dz!=0f&&!collides(x,y,z+dz)) z+=dz;
        if(dy!=0f){
            if(!collides(x,y+dy,z)) { y+=dy; grounded=false; }
            else { if(dy<0) grounded=true; vy=0f; }
        }
    }

    public void update(float dt){
        dt=Math.min(.05f,Math.max(0f,dt));
        double a=Math.toRadians(yaw);
        float speed=sprint?6.2f:4.2f;
        float fx=(float)Math.sin(a),fz=(float)Math.cos(a);
        float sx=(float)Math.cos(a),sz=-(float)Math.sin(a);
        float len=(float)Math.sqrt(inputF*inputF+inputS*inputS);
        float scale=len>1f?1f/len:1f;
        move((fx*inputF+sx*inputS)*scale*speed*dt,0f,
             (fz*inputF+sz*inputS)*scale*speed*dt);
        vy-=18f*dt;
        move(0f,vy*dt,0f);
        if(y<1f){y=1f;vy=0f;grounded=true;}
        world.tick();
    }

    public void breakBlock(){
        int[] h=raycast();
        if(h!=null && world.get(h[0],h[1],h[2])!=Block.AIR)
            world.set(h[0],h[1],h[2],Block.AIR);
    }

    public void placeBlock(Block block){
        if(block==null||block==Block.AIR)return;
        int[] h=raycast();
        if(h==null)return;
        int px=h[0]+h[3],py=h[1]+h[4],pz=h[2]+h[5];
        if(py<0||py>=World.HEIGHT||world.get(px,py,pz)!=Block.AIR)return;
        if(!collides(px+.5f,py,pz+.5f)) world.set(px,py,pz,block);
    }

    /** Returns targeted block and the previous empty-cell normal: x,y,z,nx,ny,nz. */
    public int[] raycast(){
        double a=Math.toRadians(yaw),p=Math.toRadians(pitch);
        double dx=Math.sin(a)*Math.cos(p),dy=Math.sin(p),dz=Math.cos(a)*Math.cos(p);
        int lastX=(int)Math.floor(x),lastY=(int)Math.floor(y+1.62f),lastZ=(int)Math.floor(z);
        for(float d=.15f;d<=8f;d+=.035f){
            int bx=(int)Math.floor(x+dx*d),by=(int)Math.floor(y+1.62f+dy*d),bz=(int)Math.floor(z+dz*d);
            if(world.get(bx,by,bz)!=Block.AIR)
                return new int[]{bx,by,bz,lastX-bx,lastY-by,lastZ-bz};
            lastX=bx;lastY=by;lastZ=bz;
        }
        return null;
    }

    @Override public void onSurfaceCreated(GL10 gl,EGLConfig cfg){
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_DITHER);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glClearColor(.48f,.72f,.95f,1f);

        String vs="attribute vec3 a;attribute vec4 c;uniform mat4 m;varying vec4 v;void main(){gl_Position=m*vec4(a,1.0);v=c;}";
        String fs="precision mediump float;varying vec4 v;void main(){gl_FragColor=v;}";
        int s=shader(GLES20.GL_VERTEX_SHADER,vs),t=shader(GLES20.GL_FRAGMENT_SHADER,fs);
        program=GLES20.glCreateProgram();
        GLES20.glAttachShader(program,s);GLES20.glAttachShader(program,t);
        GLES20.glLinkProgram(program);
        pa=GLES20.glGetAttribLocation(program,"a");
        ca=GLES20.glGetAttribLocation(program,"c");
        mu=GLES20.glGetUniformLocation(program,"m");
        lastNs=System.nanoTime();
    }

    private int shader(int type,String src){
        int s=GLES20.glCreateShader(type);
        GLES20.glShaderSource(s,src);GLES20.glCompileShader(s);
        return s;
    }

    @Override public void onSurfaceChanged(GL10 gl,int w,int h){
        viewportW=Math.max(1,w);viewportH=Math.max(1,h);
        GLES20.glViewport(0,0,viewportW,viewportH);
        Matrix.perspectiveM(projection,0,70f,viewportW/(float)viewportH,.08f,160f);
    }

    @Override public void onDrawFrame(GL10 gl){
        long now=System.nanoTime();
        float dt=(now-lastNs)/1_000_000_000f;lastNs=now;
        update(dt);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);

        double a=Math.toRadians(yaw),p=Math.toRadians(pitch);
        float cp=(float)Math.cos(p);
        float eyeX=x+(float)Math.sin(a)*cp,eyeY=y+1.62f+(float)Math.sin(p),
              eyeZ=z+(float)Math.cos(a)*cp;
        Matrix.setLookAtM(view,0,x,y+1.62f,z,eyeX,eyeY,eyeZ,0f,1f,0f);
        Matrix.multiplyMM(vp,0,projection,0,view,0);

        if(vertices!=null&&vertexCount>0){
            GLES20.glUseProgram(program);
            GLES20.glUniformMatrix4fv(mu,1,false,vp,0);
            vertices.position(0);colors.position(0);
            GLES20.glVertexAttribPointer(pa,3,GLES20.GL_FLOAT,false,0,vertices);
            GLES20.glEnableVertexAttribArray(pa);
            GLES20.glVertexAttribPointer(ca,4,GLES20.GL_FLOAT,false,0,colors);
            GLES20.glEnableVertexAttribArray(ca);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(pa);
            GLES20.glDisableVertexAttribArray(ca);
        }

        // Rebuild continuously only after world mutations; movement itself does not rebuild geometry.
        if(vertexCount==0) buildMesh();
    }

    /** Correct cube winding. The previous implementation had malformed face vertices causing the large triangles/gaps. */
    private void buildMesh(){
        Mesh m=new Mesh(65536);
        final int radius=7;
        int minX=(int)Math.floor(x)-radius,maxX=(int)Math.floor(x)+radius;
        int minZ=(int)Math.floor(z)-radius,maxZ=(int)Math.floor(z)+radius;

        for(int bx=minX;bx<=maxX;bx++)
            for(int bz=minZ;bz<=maxZ;bz++)
                for(int by=0;by<World.HEIGHT;by++){
                    Block b=world.get(bx,by,bz);
                    if(b==Block.AIR)continue;
                    for(int f=0;f<6;f++){
                        Block n=world.get(bx+DX[f],by+DY[f],bz+DZ[f]);
                        if(isVisible(b,n))m.face(bx,by,bz,f,b);
                    }
                }

        vertices=m.vertices();
        colors=m.colors();
        vertexCount=m.vertexCount();
    }

    private boolean isVisible(Block b,Block n){
        if(n==Block.AIR)return true;
        if(b==Block.WATER)return n!=Block.WATER;
        return n==Block.WATER;
    }

    private static final int[] DX={1,-1,0,0,0,0};
    private static final int[] DY={0,0,1,-1,0,0};
    private static final int[] DZ={0,0,0,0,1,-1};

    private static final class Mesh{
        float[] v,c;int n;
        Mesh(int initial){v=new float[initial*3];c=new float[initial*4];}
        void ensure(int verts){
            int need=n+verts*3;
            if(need>v.length)v=java.util.Arrays.copyOf(v,Math.max(need,v.length*2));
            int cneed=n/3*4+verts*4;
            if(cneed>c.length)c=java.util.Arrays.copyOf(c,Math.max(cneed,c.length*2));
        }
        void add(float x,float y,float z,float r,float g,float b,float a){
            ensure(1);int vi=n,ci=(n/3)*4;
            v[vi]=x;v[vi+1]=y;v[vi+2]=z;
            c[ci]=r;c[ci+1]=g;c[ci+2]=b;c[ci+3]=a;n+=3;
        }
        void face(int x,int y,int z,int f,Block b){
            final float[][] q={
                {1,0,0, 1,1,0, 1,1,1, 1,0,1},
                {0,0,0, 0,0,1, 0,1,1, 0,1,0},
                {0,1,0, 0,1,1, 1,1,1, 1,1,0},
                {0,0,0, 1,0,0, 1,0,1, 0,0,1},
                {0,0,1, 1,0,1, 1,1,1, 0,1,1},
                {0,0,0, 0,1,0, 1,1,0, 1,0,0}
            };
            final int[] t={0,1,2,0,2,3};
            float[] col=color(b);float light=f==2?1f:(f==3?.55f:.78f);
            float alpha=b==Block.WATER?.62f:1f;
            for(int i:t){
                int k=i*3;
                add(x+q[f][k],y+q[f][k+1],z+q[f][k+2],
                    col[0]*light,col[1]*light,col[2]*light,alpha);
            }
        }
        FloatBuffer vertices(){
            return buffer(java.util.Arrays.copyOf(v,n));
        }
        FloatBuffer colors(){
            return buffer(java.util.Arrays.copyOf(c,(n/3)*4));
        }
        int vertexCount(){return n/3;}
        static FloatBuffer buffer(float[] a){
            FloatBuffer f=ByteBuffer.allocateDirect(a.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            f.put(a).position(0);return f;
        }
        static float[] color(Block b){
            switch(b){
                case GRASS:return new float[]{.28f,.72f,.18f};
                case DIRT:return new float[]{.48f,.30f,.13f};
                case STONE:return new float[]{.48f,.50f,.53f};
                case SAND:return new float[]{.86f,.73f,.43f};
                case WATER:return new float[]{.08f,.38f,.82f};
                case WOOD:return new float[]{.48f,.27f,.10f};
                case LEAVES:return new float[]{.18f,.55f,.16f};
                default:return new float[]{0f,0f,0f};
            }
        }
    }
}
