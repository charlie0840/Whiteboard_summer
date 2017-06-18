package com.example.charlie0840.whiteboard1;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import org.alljoyn.cops.peergroupmanager.PeerGroupManager;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StreamCorruptedException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class NetworkActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ImageButton currPaint, eraseBtn, redoBtn, undoBtn, brushBtn;
    private FloatingActionButton fab;
    private CheckBox classCheckbox;

    private TextView collegename_text, classname_text, username_text;

    private WebView web;

    private LinearLayout linearLayout, linearLayout1;

    private Animation previousAnim, nextAnim;

    private BusHandler mBusHandler;
    private UIHandler  mUIHandler;
    public ServerSocket serverSocket;

    public Bitmap curr, temp;
    public DrawingView drawView;
    public boolean breakOut = false, isProgrammed = false, isStarted = false, inSession = false;

    // Layout Views

    public String session_id;
    public int breakCount = 0;

    public MyServerThread serverThread;

    public String port;

    private String name, groupname = "No Groups Available", class_name, class_id = "", iCurrentSelection = "No Groups Available", user_name, ipAddress,
            college_name, role, session_name, type, user_id, school_id, email, value,
            selectclass_url = "https://moviphones.com/whiteboard/class/classenroll.php",
            classinfo_url = "https://moviphones.com/whiteboard/class/classinfo.php",
            classenroll_url = "https://moviphones.com/whiteboard/class/classenroll.php",
            getclasslist_url = "https://moviphones.com/whiteboard/school_user/schooluser_getclasslist.php";

    private int asked = 0, mWidth, mHeight, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;
    private float smallBrush = 5, mediumBrush = 10, largeBrush = 15;
    private boolean pic = false, joined = false, reset = false, showNB = false, isNew = true, showBar = false;

    private String[] classList;
    private ArrayList<String> classArray = new ArrayList<>();
    public ArrayList<Bitmap> bitmapArray = new ArrayList<>();

    List<Peers> permitMap = new ArrayList<Peers>();

    Map<String, String> nickMap = new HashMap<String, String>();
    Map<String, ArrayList<Object>> groupMap = new HashMap<String, ArrayList<Object>>();
    Map<String, String> groupHost = new HashMap<String, String>();
    Map<String, String> groupNew = new HashMap<String, String>();
    Map<String, String> groupCode = new HashMap<String, String>();

    /**
     * Used to store the last screen title. For use in { #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_network);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mUIHandler = new UIHandler(this);
        /* Make all AllJoyn calls through a separate handler thread to prevent blocking the UI. */
        HandlerThread busThread = new HandlerThread("BusHandler");
        busThread.start();
        mBusHandler = new BusHandler(busThread.getLooper(), mUIHandler, this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        user_name = intent.getStringExtra("user_name");
        college_name = intent.getStringExtra("college_name");
        role = intent.getStringExtra("role");
        user_id = intent.getStringExtra("user_id");
        school_id = intent.getStringExtra("school_id");
        email = intent.getStringExtra("email");
        classList = intent.getStringArrayExtra("classlist");
        bitmapArray.add(0, curr);

//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        drawView = (DrawingView)findViewById(R.id.drawing_canvas);
        drawView.setBoolean(true);
        drawView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                //simTouch();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        drawView.touch_start(x, y);
                        //System.out.println("down " + x + " " + y);
                        drawView.invalidate();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        drawView.touch_move(x, y);
                        //System.out.println("moving " + x + " " + y);
                        drawView.invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        //Bitmap temp;
                        //Bitmap testCurr = bitmapArray.get(0);
                        //if(testCurr != null)
                        //bitmapArray.get(0).recycle();
                        //bitmapArray.remove(0);

                        //System.out.println("up " + x + " " + y);
                        //temp = drawView.getDrawingCache();

                        curr = saveSignature();

                        if(pic) {
                            drawView.touch_up(curr, false);
                            pic = false;
                        }
                        else
                            drawView.touch_up(curr, false);

                        groupMap.put(groupname,drawView.getPath());
                        drawView.invalidate();



                        type = "uploadimage";
                        if(!class_id.equals("") && role.equals("instructor")) {

                            Client updatePath = new Client();
                            updatePath.execute(type, session_id, curr, user_id);
                        }
                        break;
                }
                return true;
            }
        }); //added

        View header=navigationView.getHeaderView(0);

        mBusHandler.sendEmptyMessage(BusHandler.INIT);

        undoBtn = (ImageButton)findViewById(R.id.undo_btn);
        undoBtn.setOnClickListener(onClickListener);
        redoBtn = (ImageButton)findViewById(R.id.redo_btn);
        redoBtn.setOnClickListener(onClickListener);
        brushBtn = (ImageButton)findViewById(R.id.brush_btn);
        brushBtn.setOnClickListener(onClickListener);
        eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(onClickListener);

        collegename_text = (TextView)header.findViewById(R.id.collegename_textview);
        classname_text = (TextView)header.findViewById(R.id.classname_textview);
        username_text = (TextView)header.findViewById(R.id.username_textview);
        classCheckbox = (CheckBox)header.findViewById(R.id.class_checkbox);

        classCheckbox.setOnClickListener(onCheckedListener);

        if(role.equals("student"))
            classCheckbox.setVisibility(View.INVISIBLE);

        collegename_text.setText(college_name);
        username_text.setText(role + ":" + user_name);

        linearLayout1 = (LinearLayout)findViewById(R.id.linearLayout2);
        currPaint = (ImageButton)linearLayout1.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        linearLayout = (LinearLayout)findViewById(R.id.linearLayout_sum);
        linearLayout.setVisibility(View.INVISIBLE);

        previousAnim = AnimationUtils.loadAnimation(NetworkActivity.this, R.anim.previous_colorbar);
        nextAnim = AnimationUtils.loadAnimation(NetworkActivity.this, R.anim.next_colorbar);

//        mNavigationDrawerFragment = (NavigationDrawerFragment)
//                getFragmentManager().findFragmentById(R.id.navigation_drawer);
//        mTitle = getTitle();

        // Set up the drawer.
//        mNavigationDrawerFragment.setUp(
//                R.id.navigation_drawer,
//                (DrawerLayout) findViewById(R.id.drawer_layout));
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(showBar == true) {
                    linearLayout.startAnimation(previousAnim);
                    linearLayout.setVisibility(View.INVISIBLE);
                    showBar = false;
                }
                else {
                    linearLayout.startAnimation(nextAnim);
                    linearLayout.setVisibility(View.VISIBLE);
                    showBar = true;
                }
                //fab.setVisibility(View.INVISIBLE);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        ipAddress = getIPAddress(true);
        getIpAddressAsyn as = new getIpAddressAsyn();
        as.execute();
        try {
            ipAddress = as.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //ipAddress = ipAddress();
        port = generatePort();
        startServerSocket(Integer.parseInt(port));


    }


    private View.OnClickListener onCheckedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = ((CheckBox) view).isChecked();
            if(checked) {
                if(classname_text.getText().equals("No class available")) {
                    Toast.makeText(getApplicationContext(), "Please first select a session to start", Toast.LENGTH_SHORT).show();
                    return;
                }
                type = "opensession";
                BackgroundWorker openSession = new BackgroundWorker(getApplicationContext());
                openSession.execute(type, session_id);
                Intent intent = new Intent(getApplicationContext(), ClassstartActivity.class);
                intent.putExtra("class_name", classname_text.getText());
                startActivityForResult(intent, 5);


            }
            else {
                if(classname_text.getText().equals("No class available")) {
                    Toast.makeText(getApplicationContext(), "Please first select a session to start", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), ClassendActivity.class);
                intent.putExtra("class_name", classname_text.getText());
                startActivityForResult(intent, 6);
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.undo_btn:
                    drawView.onClickUndo();
                    groupMap.put(groupname, drawView.getPath());



                    if(curr == null)
                        temp = drawView.getDrawingCache();
                    else {
                        if( !curr.isRecycled())
                            temp = Bitmap.createBitmap(curr);
                        else
                            temp = drawView.getDrawingCache();
                    }

                    curr = saveSignature();


                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    temp.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] imageBytes = baos.toByteArray();
                    String uploadImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                    type = "uploadimage";
                    if(!class_id.equals("")) {
                        BackgroundWorker updatePath = new BackgroundWorker(getApplicationContext());
                        updatePath.execute(type, class_id, session_id, uploadImage, user_id);
                    }

                    break;
                case R.id.redo_btn:
                    drawView.onClickRedo();
                    groupMap.put(groupname, drawView.getPath());



                    if(curr == null)
                        temp = drawView.getDrawingCache();
                    else {
                        if( !curr.isRecycled())
                            temp = Bitmap.createBitmap(curr);
                        else
                            temp = drawView.getDrawingCache();
                    }

                    curr = saveSignature();


                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                    temp.compress(Bitmap.CompressFormat.PNG, 100, baos1);
                    byte[] imageBytes1 = baos1.toByteArray();
                    String uploadImage1 = Base64.encodeToString(imageBytes1, Base64.DEFAULT);

                    type = "uploadimage";
                    if(!class_id.equals("")) {
                        BackgroundWorker updatePath = new BackgroundWorker(getApplicationContext());
                        updatePath.execute(type, class_id, session_id, uploadImage1, user_id);
                    }

                    break;
                case R.id.brush_btn:
                    final Dialog brushDialog = new Dialog(NetworkActivity.this);
                    brushDialog.setTitle("Brush size:");
                    brushDialog.setContentView(R.layout.brush_chooser);

                    ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
                    smallBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            drawView.setBrushSize(smallBrush);
                            drawView.setLastBrushSize(smallBrush);
                            drawView.setErase(false);
                            brushDialog.dismiss();
                        }
                    });

                    ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
                    mediumBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            drawView.setBrushSize(mediumBrush);
                            drawView.setLastBrushSize(mediumBrush);
                            drawView.setErase(false);
                            brushDialog.dismiss();
                        }
                    });

                    ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
                    largeBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            drawView.setBrushSize(largeBrush);
                            drawView.setLastBrushSize(largeBrush);
                            drawView.setErase(false);
                            brushDialog.dismiss();
                        }
                    });
                    brushDialog.show();
                    break;
                case R.id.erase_btn:
                    final Dialog eraseDialog = new Dialog(NetworkActivity.this);
                    eraseDialog.setTitle("Eraser size:");
                    eraseDialog.setContentView(R.layout.brush_chooser);
                    ImageButton smalleBtn = (ImageButton)eraseDialog.findViewById(R.id.small_brush);
                    smalleBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            drawView.setErase(true);
                            drawView.setBrushSize(smallBrush);
                            eraseDialog.dismiss();
                        }
                    });
                    ImageButton mediumeBtn = (ImageButton)eraseDialog.findViewById(R.id.medium_brush);
                    mediumeBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            drawView.setErase(true);
                            drawView.setBrushSize(mediumBrush);
                            eraseDialog.dismiss();
                        }
                    });
                    ImageButton largeeBtn = (ImageButton)eraseDialog.findViewById(R.id.large_brush);
                    largeeBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            drawView.setErase(true);
                            drawView.setBrushSize(largeBrush);
                            eraseDialog.dismiss();
                        }
                    });
                    eraseDialog.show();
                    break;
            }
        }
    };





    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present. upper right!!!!!!!!!!!!
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            breakOut = true;
            drawView.startNew();
            drawView.setEnabled(true);
            serverThread.closeCLient = true;
            classname_text.setText("No class available");
            return true;
        }
        else if (id == R.id.action_logout) {
            breakOut = true;
            Intent intent = new Intent(NetworkActivity.this, CollegeselectActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        return true;//super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_new) {
            // Handle the camera action
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            //newDialog.setTitle("New Whiteboard");
            newDialog.setMessage("Start new whiteboard?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    curr = null;
                    ArrayList<Object> temp = new ArrayList<Object>();
                    groupMap.put(groupname, temp);
                    if(groupHost.get(groupname) != null) {
                        if (groupHost.get(groupname).equals("client"))
                        {
                            groupNew.put(groupname, "new");
                        }
                    }
                    drawView.resetTempPath();
                    drawView.startNew();
                    reset = true;
                    if(groupname != "No Groups Available") {
//						Toast.makeText(getApplicationContext(), "pre is " + test + "pressed new " + groupMap.get(test).size(), Toast.LENGTH_SHORT).show();

                        groupMap.put(groupname,drawView.getPath());
                        curr = saveSignature();
                    }
                    dialog.dismiss();
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            newDialog.show();
        } else if (id == R.id.nav_save) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},  MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            //saveDialog.setTitle("Save Whiteboard");
            saveDialog.setMessage("Save the whiteboard?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    //save drawing
                    String title = "Whiteboard_" + System.currentTimeMillis() + ".jpg";
                    File filepath = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/AkyumenWhiteboard");
                    Boolean mdir = filepath.exists();
                    if(!mdir) {
                        mdir = filepath.mkdirs();
                    }


                    String file = (filepath.getAbsolutePath()+"/" + title);
                    drawView.setDrawingCacheEnabled(true);
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, title);
                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.DATA, file);

                    Bitmap mBitmap = drawView.getDrawingCache();

                    Uri imgSaved = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    try {
                        OutputStream outStream = getContentResolver().openOutputStream(imgSaved);
                        mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
                        outStream.flush();
                        outStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if((imgSaved!=null) && (mdir!=false)){
                        Toast savedToast = Toast.makeText(getApplicationContext(),
                                "Whiteboard saved", Toast.LENGTH_SHORT);
                        savedToast.show();
                    }
                    else{
                        Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                "Whiteboard could not be saved", Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }
                    drawView.destroyDrawingCache();
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            saveDialog.show();
        } else if (id == R.id.nav_importpic) {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            pic = true;
            Intent customChooserIntent = Intent.createChooser(i, "Pick an image");
            startActivityForResult(customChooserIntent, 10);
        } else if (id == R.id.nav_manage) {
            NetCheck netCheck = new NetCheck();
            netCheck.execute();
            boolean retVal = false;
            try {
                retVal = netCheck.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if(retVal) {

                Intent intent = new Intent(getApplicationContext(), MyclasslistActivity.class);
                intent.putExtra("user_id", user_id);

                String username = user_name + email;
                //System.out.println("username + email is " + username);
                type = "getclasslistofuser";

                GetClassList getClassList = new GetClassList(getApplicationContext());
                getClassList.execute(type, username);

                try {
                    classArray = getClassList.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                classList = new String[classArray.size()];
                for (int i = 0; i < classArray.size(); i++) {
                    classList[i] = classArray.get(i);
                }

                intent.putExtra("role", role);
                intent.putExtra("user_name", user_name);
                intent.putExtra("user_id", user_id);
                intent.putExtra("classArray", classArray);
                intent.putExtra("classlist", classList);
                intent.putExtra("school_id", school_id);
                intent.putExtra("email", email);
                startActivityForResult(intent, 4);
            }
        }

        else if(id == R.id.nav_text) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("Enter text to add");

            // Set an EditText view to get user input
            final EditText input = new EditText(this);
            alert.setView(input);

            alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    value = input.getText().toString();
                    drawView.setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            float x = event.getX();
                            float y = event.getY();
                            drawView.addNewText(value, x, y);
                            curr = saveSignature();
                            drawView.setOnTouchListener(new View.OnTouchListener() {
                                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                                public boolean onTouch(View v, MotionEvent event) {
                                    float x = event.getX();
                                    float y = event.getY();

                                    switch (event.getAction()) {
                                        case MotionEvent.ACTION_DOWN:
                                            drawView.touch_start(x, y);
                                            drawView.invalidate();
                                            break;
                                        case MotionEvent.ACTION_MOVE:
                                            drawView.touch_move(x, y);
                                            drawView.invalidate();
                                            break;
                                        case MotionEvent.ACTION_UP:
                                            Bitmap temp;
                                            if(curr == null)
                                                temp = drawView.getDrawingCache();
                                            else
                                                temp = Bitmap.createBitmap(curr);
                                            curr = saveSignature();
                                            if(pic) {
                                                drawView.touch_up(temp, false);
                                                pic = false;
                                            }
                                            else
                                                drawView.touch_up(curr, false);
                                            drawView.invalidate();

                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            temp.compress(Bitmap.CompressFormat.PNG, 100, baos);
                                            byte[] imageBytes = baos.toByteArray();
                                            String uploadImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                                            type = "uploadimage";
                                            if(!class_id.equals("")) {
                                                BackgroundWorker updatePath = new BackgroundWorker(getApplicationContext());
                                                updatePath.execute(type, class_id, session_id, uploadImage, user_id);
                                            }

                                            if(groupname != "No Groups Available") {
                                                groupMap.put(groupname,drawView.getPath());
                                            }
                                            break;
                                    }
                                    return true;
                                }
                            }); //added
                            return false;
                        }
                    });

                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            });

            alert.show();
        }


        else if (id == R.id.nav_facebook) {
            doWebView("http://www.facebook.com", this, getApplicationContext());
        }
        else if (id == R.id.nav_twitter) {
            doWebView("http://twitter.com", this, getApplicationContext());
        }
        else if (id == R.id.nav_Line) {
            doWebView("http://line.me/en/call", this, getApplication());
        }
        else if (id == R.id.nav_google) {
            doWebView("http://www.google.com", this, getApplicationContext());
        }
        else if (id == R.id.nav_youtube) {
            doWebView("http://www.youtube.com", this, getApplicationContext());
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
		/* Disconnect to prevent resource leaks. */
        mBusHandler.sendEmptyMessage(BusHandler.DISCONNECT);
    }


    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_network, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((NetworkActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public void paintClicked(View view){
        //use chosen color
        linearLayout.startAnimation(previousAnim);
        linearLayout.setVisibility(View.INVISIBLE);
        showBar = false;
        drawView.setErase(false);
        drawView.setBrushSize(drawView.getLastBrushSize());
        //if(view!=currPaint){
            //update color
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();
            drawView.setColor(color);
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint=(ImageButton)view;
        //}
    }

    //added
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    private Bitmap getBitmapFromUri(Uri data){
        Bitmap bitmap = null;

        // Starting fetch image from file
        InputStream is=null;
        try {

            is = getContentResolver().openInputStream(data);

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            // BitmapFactory.decodeFile(path, options);
            BitmapFactory.decodeStream(is, null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, mWidth, mHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;

            is = getContentResolver().openInputStream(data);

            bitmap = BitmapFactory.decodeStream(is,null,options);


            if(bitmap==null){
                Toast.makeText(getBaseContext(), "Image is not Loaded",Toast.LENGTH_SHORT).show();
                return null;
            }

            is.close();
        }catch (IOException e) {
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        return bitmap;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 10 && resultCode == RESULT_OK && null != intent) {
            Uri data = intent.getData();
            Bitmap bitmap = getBitmapFromUri(data);
            if(bitmap!=null){
                drawView.addBitmap(bitmap);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                drawView.addByteArray(byteArray);
            }
        }
        else if(requestCode == 4 && intent != null) {
            if(resultCode == 1) {
                if(role.equals("instructor")) {
                    Toast.makeText(getApplicationContext(), "Failed to start the class", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Failed to join the class", Toast.LENGTH_SHORT).show();
                }
            }
            else {

                ipAddress = getIPAddress(true);
                getIpAddressAsyn as = new getIpAddressAsyn();
                as.execute();
                try {
                    ipAddress = as.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                //ipAddress = ipAddress();
                port = generatePort();
                startServerSocket(Integer.parseInt(port));


                isStarted = true;
                inSession = true;
                class_name = intent.getStringExtra("class_name");
                session_name = intent.getStringExtra("session_name");
                session_id = intent.getStringExtra("session_id");

                type = "updateipandport";

                //System.out.println(ipAddress + " and " + port + " and " + session_id + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                BackgroundWorker updateIPAddress = new BackgroundWorker(getApplicationContext());
                updateIPAddress.execute(type, ipAddress, port, user_id, session_id);
                String[] str = class_name.split(":");
                class_id = str[0];
                classname_text.setText(str[1] + "    " + session_name);
                String groupName = str[1];
                groupname = str[1];

                setGroupParameters();

                if(role.equals("student")) {
                    drawView.setEnabled(false);
                    groupHost.put(groupName, "client");
                    groupNew.put(groupName, "new");
                    ArrayList<Object> temp = new ArrayList<Object>();
                    groupMap.put(groupName, temp);
                    setGroupname(groupName);

                    setTitle();
                    //showDialog(DIALOG_USERNAME_ID);
                    setNickname("", "");
                    permitMap.clear();
                    permitMap.add(new Peers(getUsername(getHostId(groupName)), getHostId(groupName), true, true));
                    //curr = Bitmap.createBitmap(
                    //updateDataFromDatabase();

                }
                    String oldName = groupname;
                    groupMap.put(oldName, drawView.getPath());
                    setGroupname(groupName);
                    if(groupHost.get(groupName) != null) {
                        if (!(groupHost.get(groupName).equals("host"))) {
                            groupNew.put(groupName, "new");
                        }
                    }
//
                    if(groupMap.get(groupName) != null && groupName != "No class Available") {
//
                        drawView.setPaths(groupMap.get(groupName));
                        curr = saveSignature();
                    }
                    else {
                        drawView.startNew();
                        curr = saveSignature();
                        ArrayList<Object> tempL = new ArrayList<Object>();
                        groupMap.put(groupName, tempL);
                    }
                    permitMap.clear();
                    if(groupName!=null && groupName!="No class Available")
                        permitMap.add(new Peers(getUsername(getHostId(groupName)), getHostId(groupName), true, true));
                }
                iCurrentSelection = groupname;
        }
        else if(requestCode == 5) {

            String groupName = class_name;

            type = "isStart";
            BackgroundWorker isStart = new BackgroundWorker(this);
            isStart.execute(type, session_id);

            groupHost.put(groupName, "host");
            groupNew.put(groupName, "old");

            ArrayList<Object> temp = new ArrayList<Object>();
            groupMap.put(groupName, temp);

            setTitle();
            //showDialog(DIALOG_USERNAME_ID);
            setNickname("", "");
            permitMap.clear();
            permitMap.add(new Peers(getUsername(getHostId(groupName)), getHostId(groupName), true, true));

            Toast.makeText(getApplicationContext(), "Session start", Toast.LENGTH_SHORT).show();
        }
        else if(requestCode == 6) {
            Intent intent1 = new Intent(getApplicationContext(), PermissionCode.class);
            Bundle b = new Bundle();
            b.putInt("who", 3);
            b.putString("role", role);
            b.putString("group_name", groupname);
            intent1.putExtras(b);
            startActivityForResult(intent1, 3);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mWidth = drawView.getWidth();
        mHeight = drawView.getHeight();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putInt("width", mWidth);
        outState.putInt("height", mHeight);
        if(drawView.getBitmap()!=null){
            outState.putParcelable("bitmap", drawView.getBitmap());
        }

        super.onSaveInstanceState(outState);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Create the dialogs for each function
     */
    public static final int DIALOG_CREATE_GROUP_ID = 0;
    public static final int DIALOG_DESTROY_GROUP_ID = 1;
    public static final int DIALOG_JOIN_GROUP_ID = 2;
    public static final int DIALOG_LEAVE_GROUP_ID = 3;
    public static final int DIALOG_SELECT_GET_PEER_GROUP_ID = 4;
    public static final int DIALOG_GET_PEERS_ID = 5;
    public static final int DIALOG_OPTION_ID = 6;
    public static final int DIALOG_ALLJOYN_ERROR_ID = 7;
    public static final int DIALOG_USERNAME_ID = 8;

    protected Dialog onCreateDialog(int id, Bundle args) {
        mUIHandler.logInfo("onCreateDialog(" + id + ")");
        Dialog result = null;
        switch(id) {
            case DIALOG_GET_PEERS_ID:
            {
                DialogBuilder builder = new DialogBuilder();
                String groupName = args.getString("groupName");
                result = builder.createParticipantsDialog(this, mBusHandler, groupName, mUIHandler);
            }
            break;
            case DIALOG_SELECT_GET_PEER_GROUP_ID:
            {
                DialogBuilder builder = new DialogBuilder();
                result = builder.createSelectGroupDialog(this, mBusHandler);
            }
            break;
            case DIALOG_OPTION_ID:
            {
                DialogBuilder builder = new DialogBuilder();
                result = builder.createOptionDialog(this, role);
            }
            break;
            case DIALOG_USERNAME_ID:
            {
                DialogBuilder builder = new DialogBuilder();
                result = builder.createUsernameDialog(this, mUIHandler, mBusHandler);
            }
            break;
        }
        return result;
    }

    public void setTitle() {
        name = role + ":" + user_name;
        if (name == null) {
            name = "Not set";
        }
    }

    public String getUserId() {
        return "";//mPeerManager.getMyPeerId();
    }

    public void setNickname(String id, String nick) {
        nickMap.put(user_id, user_name);
    }

    public String getUsername(String id) {
        return nickMap.get(id);
    }

    public void exportPermission(List<Peers> permit) {
        this.permitMap = permit;
    }

    public String getHostId(String gName) {
//        host = mPeerManager.getGroupHostPeerId(gName);
        return "";//mPeerManager.getGroupHostPeerId(gName);
    }

    public void sendPermission() {
        asked = 1;
    }

    public void setGroupname(String group) {
        this.groupname = group;
        class_name = group;
    }

    public Bitmap saveSignature(){
        drawView.setDrawingCacheEnabled(true);
        Bitmap bitmap = drawView.getDrawingCache();
        return bitmap;
    }

    public void setIsNew(boolean isNew) {

        this.isNew = isNew;
        if(isNew)
            groupNew.put(groupname, "new");
        else
            groupNew.put(groupname, "old");
    }

    private class NetCheck extends AsyncTask<String,String,Boolean> {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(NetworkActivity.this);
            nDialog.setTitle("Checking Network");
            nDialog.setMessage("Loading..");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();
        }
        /**
         * Gets current device state and checks for working internet connection by trying Google.
         **/
        @Override
        protected Boolean doInBackground(String... args){



            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(3000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        return true;
                    }
                } catch (MalformedURLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return false;

        }
        @Override
        protected void onPostExecute(Boolean th) {

            if (th == true) {
                nDialog.dismiss();
            } else {
                nDialog.setMessage("not connected to netword");
//				nDialog.show();
                Handler handler = null;
                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        nDialog.cancel();
                        nDialog.dismiss();
                    }
                }, 1000);
            }
        }
    }

    public void doWebView(String url, Activity activity, Context ctx) {
        NetCheck netCheck = new NetCheck();
        netCheck.execute();
        boolean retVal = false;
        try {
            retVal = netCheck.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if(retVal) {


            AlertDialog.Builder builder = new AlertDialog.Builder(NetworkActivity.this);
            LayoutInflater inflater = LayoutInflater.from(activity);
            View v = inflater.inflate(R.layout.web_view, null);

            String frameVideo = "<html><body>Video From YouTube<br><iframe width=\\\"420\\\" height=\\\"315\\\" src=\\\"https://www.youtube.com/embed/47yJ2XCRLZs\\\" frameborder=\\\"0\\\" allowfullscreen></iframe></body></html>";

            web = (WebView) v.findViewById(R.id.web);
            web.setWebChromeClient(new WebChromeClient() {
            });
            WebSettings webSettings = web.getSettings();
            webSettings.setJavaScriptEnabled(true);
            web.loadData(frameVideo, "text/html", "utf-8");
            EditText edit = (EditText) v.findViewById(R.id.edit);
            edit.setFocusable(true);
            edit.requestFocus();
            web.loadUrl(url);
            web.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });
            //this.webView = web;
            builder.setView(v);
            builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            //builder.create();
            builder.show();
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:{
                    web.goBack();
                }break;
            }
        }
    };


    private void setGroupParameters() {
        if (!(iCurrentSelection.equalsIgnoreCase(class_name))) {

            String oldName = groupname;

            setGroupname(class_name);

            groupMap.put(oldName, drawView.getPath());

            if(groupHost.get(groupname) != null) {
                if (!(groupHost.get(groupname).equals("host"))) {
                    groupNew.put(groupname, "new");
                }
            }

            if(groupMap.get(groupname) != null && groupname != "No Groups Available") {

                drawView.setPaths(groupMap.get(groupname));
                curr = saveSignature();
            }
            else {
                drawView.startNew();
                curr = saveSignature();
                ArrayList<Object> temp = new ArrayList<Object>();
                groupMap.put(groupname, temp);
            }
            setTitle();
            permitMap.clear();
            if(groupname!=null && groupname!="No Groups Available")
                permitMap.add(new Peers(getUsername(getHostId(groupname)), getHostId(groupname), true, true));
        }
    }

    private class GetClassList extends AsyncTask<String, Void, ArrayList<String>> {
        Context context;

        ArrayList<String> retVal = new ArrayList<>();

        GetClassList(Context ctx) {
            context = ctx;
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            retVal = new ArrayList<>();
            type = params[0];
            if (type.equals("getclasslist")) {
                try {
                    URL url = new URL(selectclass_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                        String[] parts = line.split(",");

                        for (int i = 0; i < parts.length; i++) {
                            retVal.add(parts[i]);
                        }
                    }
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (type.equals("getclassinfo")) {
                try {
                    String college_name = params[1];
                    URL url = new URL(classinfo_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("school_name", "UTF-8") + "=" + URLEncoder.encode(college_name, "UTF-8");
                    bufferWriter.write(post_data);
                    bufferWriter.flush();
                    bufferWriter.close();
                    outputStream.close();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                    String result = "";
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }

                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();

                    String[] temp = result.split(",");

                    for (int i = 0; i < temp.length; i++) {
                        retVal.add(temp[i]);
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            else if(type.equals("getclasslistofuser")) {
                try {
                    String user = params[1];
                    URL url = new URL(getclasslist_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(user, "UTF-8");
                    bufferWriter.write(post_data);
                    bufferWriter.flush();
                    bufferWriter.close();
                    outputStream.close();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                    String result = "";
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();
                    String[] temp = result.split(";");
                    classArray.clear();
                    for (int i = 0; i < temp.length; i++) {
                        if( i == 1) {
                            String[] tempClasses = temp[i].split(",");
                            for(int j = 0; j < tempClasses.length; j ++) {
                                classArray.add(tempClasses[j]);
                            }
                        }
                        else {
                            retVal.add(temp[i]);
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return retVal;

        }

        @Override
        protected void onPreExecute () {
        }

        @Override
        protected void onPostExecute (ArrayList<String> result){
        }

        @Override
        protected void onProgressUpdate (Void...values){
            super.onProgressUpdate(values);
        }
    }

    private void startServerSocket(int thisPort) {
        try
        {
            // Open a server socket
            serverSocket = new ServerSocket(thisPort);

            // Start a server thread to do socket-accept tasks
            System.out.println("thread testing: start ServerSocket at port " + port + ":"+ thisPort);
            serverThread = new MyServerThread(NetworkActivity.this, mUIHandler);
            serverThread.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }







    class getIpAddressAsyn extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            URL whatismyip = null;
            String ip = "";
            try {
                whatismyip = new URL("http://wtfismyip.com/text");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(
                        whatismyip.openStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                ip = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ip;
        }

        protected void onPostExecute(String feed) {
            // TODO: check this.exception
            // TODO: do something with the feed
            System.out.println("returned ip address is " + feed);
        }
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;
                        System.out.println("thread testing: getting IP Address " + sAddr);
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    public String generatePort() {
        int i = 12000;
        while(true) {
            try {
                ServerSocket socket = new ServerSocket(i);
                socket.close();
                return Integer.toString(i);
            } catch (ConnectException ce) {
                ce.printStackTrace();
                i++;
            } catch (Exception ex) {
                ex.printStackTrace();
                i++;
            }
        }
    }

    public void cleanOut() {
            //drawView.setEnabled(false);
            Bitmap tempB = Bitmap.createBitmap(curr);
            curr = null;
            temp = null;
            curr = Bitmap.createBitmap(tempB);
            ArrayList<Object> temp = new ArrayList<Object>();
            groupMap.put(groupname, temp);
            if(groupHost.get(groupname) != null) {
                if (groupHost.get(groupname).equals("client"))
                {
                    groupNew.put(groupname, "new");
                }
            }
            drawView.resetTempPath();
            drawView.startNewTest();
            reset = true;
            if(groupname != "No Groups Available") {
//						Toast.makeText(getApplicationContext(), "pre is " + test + "pressed new " + groupMap.get(test).size(), Toast.LENGTH_SHORT).show();

                groupMap.put(groupname, drawView.getPath());
                curr = saveSignature();
            }
            System.out.println("executed>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            breakCount = 0;
            //drawView.setEnabled(true);
            simTouch();
            drawView.invalidate();

    }

    public void simTouch() {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = -35.812477f;
        float y = 1614.0f;
        float x1 = -35.0f;
        float y1 = 1614.0f;
        float x2 = -34.5f;
        float y2 = 1614.0f;
        float x3 = -34.0f;
        float y3 = 1614.0f;
        float x4 = -33.5f;
        float y4 = 1614.0f;
        float x5 = -33.0f;
        float y5 = 1614.0f;
        float x6 = -32.5f;
        float y6 = 1614.0f;
        int metaState = 0;
        isProgrammed = true;
        MotionEvent downEvent = MotionEvent.obtain(
                downTime,

                eventTime,
                MotionEvent.ACTION_DOWN,
                x,
                y,
                metaState);
        MotionEvent moveEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_MOVE,
                x1,
                y1,
                metaState);
        MotionEvent moveEvent1 = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_MOVE,
                x2,
                y2,
                metaState);
        MotionEvent moveEvent2 = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_MOVE,
                x3,
                y3,
                metaState);
        MotionEvent moveEvent3 = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_MOVE,
                x4,
                y4,
                metaState);
        MotionEvent moveEvent4 = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_MOVE,
                x5,
                y5,
                metaState);
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                x6,
                y6,
                metaState
        );

// Dispatch touch event to view
        Toast.makeText(getApplicationContext(), "programmed touch", Toast.LENGTH_LONG).show();
        drawView.dispatchTouchEvent(downEvent);
        drawView.dispatchTouchEvent(moveEvent);
        drawView.dispatchTouchEvent(moveEvent1);
        drawView.dispatchTouchEvent(moveEvent2);
        drawView.dispatchTouchEvent(moveEvent3);
        drawView.dispatchTouchEvent(moveEvent4);
        drawView.dispatchTouchEvent(motionEvent);
    }

    public String getRole() {
        return role;
    }

}

