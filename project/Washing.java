package com.example.project;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.media.VolumeShaper;
import android.media.audiofx.DynamicsProcessing;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.CompassView;
import com.naver.maps.map.widget.LocationButtonView;
import com.naver.maps.map.widget.ScaleBarView;
import com.naver.maps.map.widget.ZoomControlView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;



public class Washing extends AppCompatActivity implements OnMapReadyCallback,Overlay.OnClickListener {
    private static final String TAG = "Washing";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    StringBuffer buffer = new StringBuffer();
    String[] a = new String[20000];
    String[] b = new String[20000];
    String[] c = new String[20000];
    String[] d = new String[20000];
    String[] e = new String[20000];
    String[] f = new String[20000];
    String[] g = new String[20000];
    Marker[] markers = new Marker[20000];
    String[] washing = new String[20000];
    int count = 0;
    int k = 0;
    private FusedLocationSource mLocationSource;
    private NaverMap mNaverMap;
    private long backKeyPressedTime = 0;
    private Toast toast;
    private Button btn_move1;
    private Button btn_move10;
    ProgressDialog progressDialog;
    DrawerLayout drawerLayout;
    View drawerView;
    Button btn_open;
    int n;
    private SharedPreferences mPreferences;
    com.naver.maps.map.overlay.InfoWindow InfoWindow;
    double lat, lon;
    int as = 0;
    int Mnumber;
    int wash1 = 0;
    int se;
    TextView t1;
    TextView t2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.washing_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        // ?????? ?????? ??????
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }


        // getMapAsync??? ???????????? ???????????? onMapReady ?????? ????????? ??????
        // onMapReady?????? NaverMap ????????? ??????
        mapFragment.getMapAsync(this);

        // ????????? ???????????? ???????????? FusedLocationSource ??????
        mLocationSource =
                new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
        MyAsyncTask asyncTask = new MyAsyncTask();
        asyncTask.execute();

    }
    
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Log.d(TAG, "onMapReady");
        // ???????????? ?????? ??????
        mNaverMap = naverMap;
        mNaverMap.setLocationSource(mLocationSource);
        UiSettings uiSettings = mNaverMap.getUiSettings();
        uiSettings.setCompassEnabled(false); // ????????? : true
        uiSettings.setScaleBarEnabled(false); // ????????? : true
        uiSettings.setZoomControlEnabled(false); // ????????? : true
        uiSettings.setLocationButtonEnabled(false); // ????????? : false
        uiSettings.setLogoGravity(Gravity.RIGHT | Gravity.BOTTOM);

        CompassView compassView = findViewById(R.id.compass);
        compassView.setMap(mNaverMap);
        ScaleBarView scaleBarView = findViewById(R.id.scalebar);
        scaleBarView.setMap(mNaverMap);
        ZoomControlView zoomControlView = findViewById(R.id.zoom);
        zoomControlView.setMap(mNaverMap);
        LocationButtonView locationButtonView = findViewById(R.id.location);
        locationButtonView.setMap(mNaverMap);
        LatLng initialPosition = new LatLng(37.506855, 127.066242);
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialPosition);
        naverMap.moveCamera(cameraUpdate);

        // ????????????. ????????? onRequestPermissionsResult ?????? ????????? ??????
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);


        // NaverMap ?????? ????????? NaverMap ????????? ?????? ?????? ??????
        InfoWindow = new InfoWindow();
        InfoWindow.setAdapter(new InfoWindow.DefaultViewAdapter(this) {
            @NonNull
            @Override
            protected View getContentView(@NonNull InfoWindow infoWindow) {
                Marker marker = infoWindow.getMarker();
                View view = View.inflate(com.example.project.Washing.this, R.layout.item2, null);
                TextView title = (TextView) view.findViewById(R.id.prkplceNm);
                TextView money = (TextView) view.findViewById(R.id.operday);
                title.setText("???????????????: " + a[as]);
                money.setText("???????????? :" + d[as]);
                return view;
            }
        });
        Button b4 = (Button) findViewById(R.id.button4);
        final EditText et3 = (EditText) findViewById(R.id.editText3);

        final Geocoder geocoder = new Geocoder(this);
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ??????????????? ??????2?????? ????????? ?????? ?????????????????? ?????????????????? ??????
                List<Address> list = null;

                String str = et3.getText().toString();
                try {
                    list = geocoder.getFromLocationName
                            (str, // ?????? ??????
                                    10); // ?????? ??????
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("test", "????????? ?????? - ???????????? ??????????????? ????????????");
                }
                if (list != null) {
                    if (list.size() != 0) {
                        // ???????????? ????????? ????????? ?????????
                        Address addr = list.get(0);
                        double lat = addr.getLatitude();
                        double lon = addr.getLongitude();
                        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(
                                new LatLng(lat, lon), 15)
                                .animate(CameraAnimation.Fly, 3000);
                        naverMap.moveCamera(cameraUpdate);


                    }
                }


            }
        });

    }


    private void getXmlData(int q) {
        String queryUrl = "http://api.data.go.kr/openapi/tn_pubr_public_carwsh_api?serviceKey=d8w2%2FGzcZJPLy8PLdb7OZOuJk1223dqUzF%2BHWvuT3px1t9dbzJ5cJ95h%2Bg%2B7XsW8hG85guyXA%2BfNbfnLaQtuJA%3D%3D&pageNo=0&numOfRows=15000&type=xml";
        try {
            URL url = new URL(queryUrl);//???????????? ??? ?????? url??? URL ????????? ??????.
            InputStream is = url.openStream(); //url????????? ??????????????? ??????

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();//xml????????? ??????
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8")); //inputstream ???????????? xml ????????????

            String tag;

            xpp.next();
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("?????? ??????...\n\n");
                        break;

                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();//?????? ?????? ????????????

                        if (tag.equals("item")) ;// ????????? ????????????
                        else if (tag.equals("carwshNm")) {
                            xpp.next();
                            a[count] = xpp.getText();
                        } else if (tag.equals("carwshType")) {
                            xpp.next();
                            d[count] = xpp.getText();
                        } else if (tag.equals("rdnmadr")) {
                            xpp.next();
                            e[count] = xpp.getText();
                        } else if (tag.equals("phoneNumber")) {
                            xpp.next();
                            f[count] = xpp.getText();
                        } else if (tag.equals("latitude")) {
                            xpp.next();
                            b[count] = xpp.getText();
                        } else if (tag.equals("longitude")) {
                            xpp.next();
                            c[count] = xpp.getText();
                            count++;
                        }

                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        tag = xpp.getName(); //?????? ?????? ????????????
                        if (tag.equals("item")) buffer.append("\n");// ????????? ??????????????????..?????????
                        break;
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        buffer.append("?????? ???\n");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // request code??? ???????????? ?????? ??????
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }


    @Override
    public boolean onClick(@NonNull Overlay overlay) {
        if (overlay instanceof Marker) {
            LatLng aa = ((Marker) overlay).getPosition();
            lat = aa.latitude;
            lon = aa.longitude;
            Marker marker = (Marker) overlay;
            for (int k = 0; k < count; k++) {
                if ((b[k] != null) && (c[k] != null)) {
                    if ((Double.parseDouble(b[k]) == lat) && (Double.parseDouble(c[k]) == lon)) {
                        as = k;
                        continue;
                    }
                }
            }
            if (marker.getInfoWindow() != null) {
                InfoWindow.close();
                Toast.makeText(this.getApplicationContext(), "???????????? ????????????.", Toast.LENGTH_LONG).show();
            } else {
                InfoWindow.open(marker);
                AlertDialog.Builder dlg = new AlertDialog.Builder(com.example.project.Washing.this);

                dlg.setTitle("????????????"); //??????
                dlg.setMessage("?????? : " + e[as] + "\n???????????? : " + f[as] + "\n???????????? : " + d[as]);
                dlg.setPositiveButton("?????????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int a) {

                        AlertDialog.Builder dlg = new AlertDialog.Builder(com.example.project.Washing.this);
                        dlg.setTitle("?????????");
                        final String[] versionArray = new String[]{"????????????", "????????? ??????"};
                        dlg.setSingleChoiceItems(versionArray, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int a) {
                                Mnumber = a;
                            }
                        });
                        dlg.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int a) {
                                //????????? ?????????
                                Intent intent;
                                if (Mnumber == 0) {
                                    try {
                                        intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("kakaomap://route?ep=" + Double.parseDouble(b[as]) + "," + Double.parseDouble(c[as]) + "&by=CAR"));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=net.daum.android.map&hl=ko"));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                } else if (Mnumber == 1) {
                                    try {
                                        intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("nmap://navigation?dlat=" + lat + "&dlng=" + lon + "&dname=?????????&appname=com.example.maptest"));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=com.skt.tmap.ku&hl=ko"));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                }
                                Toast.makeText(com.example.project.Washing.this, "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dlg.show();
                    }
                });
                AlertDialog alertDialog = dlg.create();
                alertDialog.getWindow().setGravity(Gravity.BOTTOM);
                alertDialog.show();

            }

            return true;
        }
        return false;

    }


    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "?????? ?????? ????????? ??? ??? ??? ???????????? ???????????????.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2500) {
            finish();
            toast.cancel();
            toast = Toast.makeText(this, "????????? ????????? ???????????????.", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public class MyAsyncTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... strings) {

            getXmlData(0);
            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            for (int i = 0; i < count; i++) {

                 if (a[i] == null || c[i] == null || b[i] == null || d[i] == null ||f[i]==null||Double.parseDouble(b[i]) >=38.60491243211909 ||f[i].equals("041-337-7617")||f[i].equals("041-338-6688")||f[i].equals("041-331-0877")|| d[i].equals("????????????") || d[i].equals("??????") || d[i].equals("?????????") || d[i].equals("?????????") || d[i].equals("????????????") || d[i].equals("????????????")  || d[i].equals("?????????") || d[i].equals("???????????????") || d[i].equals("????????????") || d[i].equals("????????????") || d[i].equals("?????????") || d[i].equals("??????") || d[i].equals("??????") || d[i].equals("?????????") || d[i].equals("???????????????") || d[i].equals("???+????????????") || d[i].equals("?????????/????????????") || d[i].equals("????????????") || d[i].equals("???????????????") || d[i].equals("??????/??????") || d[i].equals("???????????????")
                        || d[i].equals("???????????? ?????? ??? ?????? ?????? ????????????") || d[i].equals("??????") || d[i].equals("???????????? ??????")
                        || d[i].equals("???????????????") || d[i].equals("??????") || d[i].equals("?????????????????????") || d[i].equals("?????????")
                        ||f[i].equals("041-335-4904")||f[i].equals("041-333-4874")||f[i].equals("041-332-3530")||f[i].equals("041-338-0705")||f[i].equals("041-333-8827")
                        ||f[i].equals("041-332-2239")||f[i].equals("041-332-4120")||f[i].equals("041-338-6840")||f[i].equals("041-335-2242")||f[i].equals("041-333-2733")||f[i].equals("041-338-5800")
                        ||f[i].equals("041-337-9551")||a[i].equals("???????????????"))

                    continue;

                markers[wash1] = new Marker();
                markers[wash1].setPosition(new LatLng(Double.parseDouble(b[i]), Double.parseDouble(c[i])));
                markers[wash1].setMap(mNaverMap);
                markers[wash1].setWidth(100);
                markers[wash1].setHeight(100);
                markers[wash1].setHideCollidedMarkers(true);
                markers[wash1].setIcon(OverlayImage.fromResource(R.drawable.ic_car_wash_icon22));
                markers[wash1].setOnClickListener(com.example.project.Washing.this);
                wash1++;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(Boolean s) {
            super.onCancelled(s);
        }
    }


}