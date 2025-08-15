package nsk.nu.api.color;

/** Minimal color conversions for UI/FX (no platform codes). */
public final class ColorUtil {
    private ColorUtil(){}
    public static float[] rgbToHsv(int r,int g,int b){
        float rf=r/255f,gf=g/255f,bf=b/255f;
        float max=Math.max(rf,Math.max(gf,bf)), min=Math.min(rf,Math.min(gf,bf));
        float d=max-min; float h;
        if (d==0) h=0; else if (max==rf) h = ((gf-bf)/d)%6f; else if (max==gf) h=((bf-rf)/d)+2f; else h=((rf-gf)/d)+4f;
        float s = max==0?0:d/max; float v = max;
        if (h<0) h+=6f; return new float[]{h*60f,s,v};
    }
    public static int hsvToRgb(float h,float s,float v){
        float c=v*s; float x=c*(1-Math.abs((h/60f)%2-1)); float m=v-c;
        float r=0,g=0,b=0; int hi=(int)Math.floor(h/60f)%6;
        switch(hi){case 0-> {r=c;g=x;} case 1-> {r=x;g=c;} case 2-> {g=c;b=x;} case 3-> {g=x;b=c;} case 4-> {r=x;b=c;} case 5-> {r=c;b=x;}}
        int ri=Math.round((r+m)*255), gi=Math.round((g+m)*255), bi=Math.round((b+m)*255);
        return (ri&0xFF)<<16 | (gi&0xFF)<<8 | (bi&0xFF);
    }
}