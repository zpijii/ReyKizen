package com.reykizen.panel;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.os.Build;
import android.view.*;
import android.widget.*;
import rikka.shizuku.Shizuku;
public class FloatingPanelService extends Service {
    private static final int NATIVE_W=2340,NATIVE_H=1080;
    private static final int DENSITY_NATIVE=345;
    private static final int DENSITY_43=230;
    private static final int DENSITY_11=280;
    private static final int NID=7799;
    private static final String CID="rk";
    private static final int SHIZUKU_CODE=1001;
    private WindowManager wm;
    private View root;
    private WindowManager.LayoutParams lp;
    private float tx,ty;
    private int lx,ly;
    private final Shizuku.OnRequestPermissionResultListener permListener=
        (requestCode,grantResult)->{
            if(grantResult==android.content.pm.PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Shizuku OK",Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this,"Shizuku denied",Toast.LENGTH_SHORT).show();
        };
    @Override public int onStartCommand(Intent i,int f,int id){
        return START_STICKY;
    }
    @Override public void onCreate(){
        super.onCreate();
        Shizuku.addRequestPermissionResultListener(permListener);
        mkCh();
        startForeground(NID,mkNotif());
        buildPanel();
    }
    private GradientDrawable mkBg(String hex){
        GradientDrawable d=new GradientDrawable();
        d.setColor(Color.parseColor(hex));
        d.setCornerRadius(0f);
        return d;
    }
    private void buildPanel(){
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        LinearLayout ll=new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackground(mkBg("#EE0A0A0A"));
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
        min.setText("  ✕  ");
        min.setTextColor(Color.WHITE);
        min.setTextSize(14f);
        min.setBackground(null);
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
        b43.setOnClickListener(v->{apply43();hl("43",ll);});
        b11.setOnClickListener(v->{apply11();hl("11",ll);});
        row1.addView(b43);row1.addView(b11);
        ll.addView(row1);
        LinearLayout row2=new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setPadding(0,6,0,0);
        Button bRst=mkBtn("↺ Reset","#333333");
        bRst.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        bRst.setOnClickListener(v->{applyReset();hl("none",ll);});
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
        lp=new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        lp.gravity=Gravity.TOP|Gravity.LEFT;
        lp.x=16;lp.y=180;
        ll.setOnTouchListener((v,e)->{
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                lx=lp.x;ly=lp.y;tx=e.getRawX();ty=e.getRawY();return true;
            }
            if(e.getAction()==MotionEvent.ACTION_MOVE){
                lp.x=lx+(int)(e.getRawX()-tx);
                lp.y=ly+(int)(e.getRawY()-ty);
                wm.updateViewLayout(root,lp);return true;
            }
            return false;
        });
        wm.addView(root,lp);
    }
    private void apply43(){
        if(!checkShizuku())return;
        runShellCmd("wm size "+NATIVE_W+"x3120");
        runShellCmd("wm density "+DENSITY_43);
        updateStatus("4:3");
    }
    private void apply11(){
        if(!checkShizuku())return;
        runShellCmd("wm size "+NATIVE_W+"x2340");
        runShellCmd("wm density "+DENSITY_11);
        updateStatus("1:1");
    }
    private void applyReset(){
        if(!checkShizuku())return;
        runShellCmd("wm size reset");
        runShellCmd("wm density "+DENSITY_NATIVE);
        updateStatus("Normal");
    }
    private boolean checkShizuku(){
        try{
            if(Shizuku.isPreV11()||Shizuku.getVersion()<11){
                Toast.makeText(this,"Shizuku too old",Toast.LENGTH_SHORT).show();
                return false;
            }
            if(Shizuku.checkSelfPermission()!=android.content.pm.PackageManager.PERMISSION_GRANTED){
                Shizuku.requestPermission(SHIZUKU_CODE);
                return false;
            }
            return true;
        }catch(Exception e){
            Toast.makeText(this,"Shizuku not running!",Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    private void runShellCmd(String cmd){
        try{
            java.lang.ProcessBuilder pb=new java.lang.ProcessBuilder(
                "/data/user/0/moe.shizuku.privileged.api/files/rish","-c",cmd);
            pb.redirectErrorStream(true);
            java.lang.Process proc=pb.start();
            proc.waitFor();
        }catch(Exception e){
            Toast.makeText(this,"Err: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
    private void updateStatus(String lbl){
        TextView st=root.findViewWithTag("status");
        if(st!=null){
            st.setText("● "+lbl);
            st.setTextColor(Color.parseColor(
                lbl.equals("Normal")?"#00FF88":"#FF2222"));
        }
    }
    private void hl(String tag,LinearLayout ll){
        for(String t:new String[]{"43","11"}){
            View v=ll.findViewWithTag(t);
            if(v instanceof Button)
                ((Button)v).setBackground(mkBg(t.equals(tag)?"#CC0000":"#1A1A1A"));
        }
    }
    private Button mkBtn(String txt,String bg){
        Button b=new Button(this);
        b.setText(txt);
        b.setTextColor(Color.WHITE);
        b.setBackground(mkBg(bg));
        b.setTextSize(11f);
        b.setPadding(8,16,8,16);
        return b;
    }
    private void mkCh(){
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel c=new NotificationChannel(CID,"Rey Kizen",
                NotificationManager.IMPORTANCE_MIN);
            getSystemService(NotificationManager.class)
                .createNotificationChannel(c);
        }
    }
    private Notification mkNotif(){
        Notification.Builder b=new Notification.Builder(this);
        if(Build.VERSION.SDK_INT>=26)b.setChannelId(CID);
        return b.setContentTitle("Rey Kizen")
            .setContentText("Panel aktif")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setPriority(Notification.PRIORITY_MIN)
            .build();
    }
    @Override public IBinder onBind(Intent i){return null;}
    @Override public void onDestroy(){
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(permListener);
        if(root!=null&&wm!=null)wm.removeView(root);
    }
}
