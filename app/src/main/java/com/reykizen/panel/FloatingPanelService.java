package com.reykizen.panel;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.PixelFormat;
import android.os.*;
import android.view.*;
import android.widget.*;
public class FloatingPanelService extends Service {
    private static final int NATIVE_W=2340,NATIVE_H=1080,W43=1440,W11=1080;
    private static final int NID=7799;
    private static final String CID="rk";
    private WindowManager wm;
    private View root;
    private WindowManager.LayoutParams p;
    private float tx,ty;
    private int lx,ly;
    @Override public void onCreate(){
        super.onCreate();
        mkCh();startForeground(NID,mkNotif());buildPanel();
    }
    private void buildPanel(){
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        LinearLayout ll=new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackgroundColor(Color.parseColor("#EE0A0A0A"));
        ll.setPadding(20,14,20,14);
        LinearLayout hdr=new LinearLayout(this);
        hdr.setOrientation(LinearLayout.HORIZONTAL);
        hdr.setGravity(Gravity.CENTER_VERTICAL);
        TextView ttl=new TextView(this);
        ttl.setText("👑 REY KIZEN");
        ttl.setTextColor(Color.parseColor("#FF2222"));
        ttl.setTextSize(13f);
        ttl.setTypeface(null,Typeface.BOLD);
        ttl.setLayoutParams(new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT,1f));
        TextView dot=new TextView(this);
        dot.setText("●");
        dot.setTextColor(Color.parseColor("#00FF88"));
        dot.setTextSize(10f);
        TextView min=new TextView(this);
        min.setText(" X ");
        min.setTextColor(Color.WHITE);
        min.setTextSize(14f);
        min.setOnClickListener(v->stopSelf());
        hdr.addView(ttl);hdr.addView(dot);hdr.addView(min);
        ll.addView(hdr);
        View div=new View(this);
        div.setBackgroundColor(Color.parseColor("#FF2222"));
        LinearLayout.LayoutParams dlp=new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,2);
        dlp.setMargins(0,8,0,8);
        div.setLayoutParams(dlp);
        ll.addView(div);
        LinearLayout row1=new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        Button b43=mkBtn("4:3\nClassic","#CC0000");
        Button b11=mkBtn("1:1\nSquare","#1A1A1A");
        LinearLayout.LayoutParams blp=new LinearLayout.LayoutParams(
            0,LinearLayout.LayoutParams.WRAP_CONTENT,1f);
        blp.setMargins(0,0,6,0);
        b43.setLayoutParams(blp);
        b11.setLayoutParams(new LinearLayout.LayoutParams(
            0,LinearLayout.LayoutParams.WRAP_CONTENT,1f));
        b43.setTag("43");b11.setTag("11");
        b43.setOnClickListener(v->{apply(W43,"4:3");hl("43",ll);});
        b11.setOnClickListener(v->{apply(W11,"1:1");hl("11",ll);});
        row1.addView(b43);row1.addView(b11);
        ll.addView(row1);
        LinearLayout row2=new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setPadding(0,6,0,0);
        Button bRst=mkBtn("↺ Reset","#333333");
        bRst.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        bRst.setOnClickListener(v->{apply(NATIVE_W,"Normal");hl("none",ll);});
        row2.addView(bRst);
        ll.addView(row2);
        TextView st=new TextView(this);
        st.setTag("status");
        st.setText("● Normal");
        st.setTextColor(Color.parseColor("#00FF88"));
        st.setTextSize(10f);
        st.setGravity(Gravity.CENTER);
        st.setPadding(0,8,0,0);
        ll.addView(st);
        root=ll;
        int type=Build.VERSION.SDK_INT>=26?
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:
            WindowManager.LayoutParams.TYPE_PHONE;
        p=new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        p.gravity=Gravity.TOP|Gravity.LEFT;
        p.x=16;p.y=180;
        ll.setOnTouchListener((v,e)->{
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                lx=p.x;ly=p.y;tx=e.getRawX();ty=e.getRawY();return true;
            }
            if(e.getAction()==MotionEvent.ACTION_MOVE){
                p.x=lx+(int)(e.getRawX()-tx);
                p.y=ly+(int)(e.getRawY()-ty);
                wm.updateViewLayout(root,p);return true;
            }
            return false;
        });
        wm.addView(root,p);
    }
    private void apply(int w,String lbl){
        TextView st=root.findViewWithTag("status");
        if(st!=null){
            st.setText("● "+lbl);
            st.setTextColor(Color.parseColor(
                lbl.equals("Normal")?"#00FF88":"#FF2222"));
        }
        try{
            Class<?> cls=Class.forName("android.view.IWindowManager$Stub");
            IBinder b=(IBinder)Class.forName("android.os.ServiceManager")
                .getMethod("getService",String.class).invoke(null,"window");
            Object iwm=cls.getMethod("asInterface",IBinder.class).invoke(null,b);
            iwm.getClass().getMethod("setForcedDisplaySize",
                int.class,int.class,int.class).invoke(iwm,0,w,NATIVE_H);
        }catch(Exception e){
            Toast.makeText(this,"Grant via Shizuku/ADB dulu!",
                Toast.LENGTH_LONG).show();
        }
    }
    private void hl(String tag,LinearLayout ll){
        for(String t:new String[]{"43","11"}){
            View v=ll.findViewWithTag(t);
            if(v instanceof Button)
                ((Button)v).setBackgroundColor(Color.parseColor(
                    t.equals(tag)?"#CC0000":"#1A1A1A"));
        }
    }
    private Button mkBtn(String txt,String bg){
        Button b=new Button(this);
        b.setText(txt);b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.parseColor(bg));
        b.setTextSize(11f);b.setPadding(8,16,8,16);
        return b;
    }
    private void mkCh(){
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel c=new NotificationChannel(CID,"Rey Kizen",
                NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class)
                .createNotificationChannel(c);
        }
    }
    private Notification mkNotif(){
        Notification.Builder b=new Notification.Builder(this);
        if(Build.VERSION.SDK_INT>=26)b.setChannelId(CID);
        return b.setContentTitle("Rey Kizen")
            .setContentText("Panel aktif")
            .setSmallIcon(android.R.drawable.ic_menu_compass).build();
    }
    @Override public IBinder onBind(Intent i){return null;}
    @Override public void onDestroy(){
        super.onDestroy();
        if(root!=null&&wm!=null)wm.removeView(root);
    }
}
