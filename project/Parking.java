package com.example.project;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
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

public class Parking extends AppCompatActivity implements OnMapReadyCallback,Overlay.OnClickListener {
    private static final String TAG = "Parking";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    StringBuffer buffer = new StringBuffer();
    String[] name = new String[20000]; //????????? ???
    String[] lati = new String[20000]; //????????? ??????
    String[] lontude = new String[20000]; //????????? ??????
    String[] operD = new String[20000]; //????????????
    String[] lnmadr = new String[20000]; //????????? ??????
    String[] Number = new String[20000]; //????????????
    String[] prknm = new String[20000]; //???????????????
    String[] basicT = new String[20000]; //??????????????????
    String[] basicC = new String[20000]; //??????????????????
    String[] addUT = new String[20000]; // ??????????????????
    String[] addUC = new String[20000]; //??????????????????
    String[] oneday = new String[20000]; //1??? ????????? ??????????????????
    String[] dayC = new String[20000]; //1???????????? ??????
    String[] monthC = new String[20000]; //???????????? ??????
    Marker[] markers = new Marker[20000];
    String[] parking = new String[20000];
    int count = 0;
    private FusedLocationSource mLocationSource;
    private NaverMap mNaverMap;
    private long backKeyPressedTime = 0;
    private Toast toast;
    private Button btn_move10;
    ProgressDialog progressDialog;
    private String SharedPrefFile = "com.example.maptest";
    com.naver.maps.map.overlay.InfoWindow InfoWindow;
    double lat, lon;
    int as = 0; //??????????????? ?????? ??????
    int Mnumber;
    int markNumber = 0;
    int kind;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parking_main);
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
        btn_move10 = findViewById(R.id.btn_move10);
        btn_move10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(com.example.project.Parking.this, camera.class);
                startActivity(intent);
            }
        });
        MyAsyncTask asyncTask = new MyAsyncTask();
        asyncTask.execute();// ?????? Task ??????

        Spinner spinner2 = findViewById(R.id.kind); //??????,?????? ???????????? ????????????????????? ??????
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    kind = 0;
                } else if (position == 1) {
                    kind = 1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button button = findViewById(R.id.check);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateMarkers(kind);
            }
        });

    }


    @UiThread
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
        InfoWindow = new InfoWindow(); //?????? ????????? ???????????? ???????????????
        InfoWindow.setAdapter(new InfoWindow.DefaultViewAdapter(this) {
            @NonNull
            @Override
            protected View getContentView(@NonNull InfoWindow infoWindow) {
                Marker marker = infoWindow.getMarker();
                View view = View.inflate(com.example.project.Parking.this, R.layout.item1, null);
                TextView title = (TextView) view.findViewById(R.id.prkplceNm);
                TextView money = (TextView) view.findViewById(R.id.operday);
                title.setText("???????????????: " + name[as]);
                money.setText("???????????? :" + operD[as]);
                return view;
            }
        });
        Button b4 = (Button) findViewById(R.id.button4); //?????? ?????? ??????
        final EditText et3 = (EditText) findViewById(R.id.editText3);

        final Geocoder geocoder = new Geocoder(this); //
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ??????????????? ???????????? ????????? ?????? ?????????????????? ?????????????????? ??????
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


    private void getXmlData(int q) {  //??????????????? ?????? ????????? ??????
        String queryUrl = "http://api.data.go.kr/openapi/tn_pubr_prkplce_info_api?serviceKey=u3dbLGdaUJ%2BqWl%2BHTN%2FGoEpvtwSEHPztVsTNZPbV7w4KX%2FpVmjjaHJgRzKCEE0EQEtGsL%2B1BxkyveGKxJdNfxw%3D%3D&pageNo=0" + q + "&numOfRows=15000&type=xml";
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
                        tag = xpp.getName();

                        if (tag.equals("item")) ;
                        else if (tag.equals("prkplceNm")) { //????????? ???
                            xpp.next();
                            name[count] = xpp.getText();
                        } else if (tag.equals("basicTime")) { //????????? ?????? ??????
                            xpp.next();
                            basicT[count] = xpp.getText();

                        } else if (tag.equals("parkingchrgeInfo")) {
                            xpp.next();
                            parking[count] = xpp.getText();
                        } else if (tag.equals("basicCharge")) { //?????????????????????
                            xpp.next();
                            basicC[count] = xpp.getText();
                        } else if (tag.equals("addUnitTime")) { //??????????????????
                            xpp.next();
                            addUT[count] = xpp.getText();
                        } else if (tag.equals("addUnitCharge")) { //??????????????????
                            xpp.next();
                            addUC[count] = xpp.getText();

                        } else if (tag.equals("dayCmmtktAdjTime")) { //1??? ????????? ??????????????????
                            xpp.next();
                            oneday[count] = xpp.getText();
                        } else if (tag.equals("dayCmmtkt")) { //1???????????? ??????
                            xpp.next();
                            dayC[count] = xpp.getText();
                        } else if (tag.equals("monthCmmtkt")) { //??????????????????
                            xpp.next();
                            monthC[count] = xpp.getText();
                        } else if (tag.equals("operDay")) { //????????????
                            xpp.next();
                            operD[count] = xpp.getText();
                        } else if (tag.equals("lnmadr")) { //????????? ??????
                            xpp.next();
                            lnmadr[count] = xpp.getText();
                        } else if (tag.equals("phoneNumber")) { //????????????
                            xpp.next();
                            Number[count] = xpp.getText();
                        } else if (tag.equals("prkcmprt")) { //???????????????
                            xpp.next();
                            prknm[count] = xpp.getText();
                        } else if (tag.equals("latitude")) { //???????????????
                            xpp.next();
                            lati[count] = xpp.getText();
                        } else if (tag.equals("longitude")) { //????????? ??????
                            xpp.next();
                            lontude[count] = xpp.getText();
                            count++;
                        }

                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        tag = xpp.getName(); //?????? ?????? ????????????
                        if (tag.equals("item")) buffer.append("\n");// ????????? ?????????????????? ?????????
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
                if ((lati[k] != null) && (lontude[k] != null)) {
                    if ((Double.parseDouble(lati[k]) == lat) && (Double.parseDouble(lontude[k]) == lon)) {
                        as = k;
                        continue;
                    }
                }
            }
            if (marker.getInfoWindow() != null) { //?????? ????????? ?????????????????? ???????????? ???????????? ??????
                InfoWindow.close();
                Toast.makeText(this.getApplicationContext(), "???????????? ????????????.", Toast.LENGTH_LONG).show();
            } else {
                InfoWindow.open(marker);
                AlertDialog.Builder dlg = new AlertDialog.Builder(com.example.project.Parking.this);
                dlg.setTitle("????????????"); //??????
                if (parking[as].equals("??????")) {
                    dlg.setMessage("?????? : " + lnmadr[as] + "\n???????????? : " + Number[as] + "\n???????????? : " + prknm[as] + "???");
                } else {
                    dlg.setMessage("?????? : " + lnmadr[as] + "\n???????????? : " + Number[as] + "\n???????????? : " + prknm[as] + "???\n?????????????????? : " + basicT[as] + "???\n?????????????????? : " + basicC[as] + "???\n?????????????????? : " + addUT[as] + "???\n?????????????????? : " + addUC[as] + "???");
                }
                dlg.setPositiveButton("?????????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int a) { //????????? ?????? ????????? ???????????? intent?????? ?????? ??????????????? ????????? ??????

                        AlertDialog.Builder dlg = new AlertDialog.Builder(com.example.project.Parking.this);
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
                                        intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("kakaomap://route?ep=" + Double.parseDouble(lati[as]) + "," + Double.parseDouble(lontude[as]) + "&by=CAR"));
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
                                Toast.makeText(com.example.project.Parking.this, "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
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


    public void onBackPressed() { //???????????? ??????
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

            getXmlData(0);// ?????? ??????
            return true;
        }

        @Override
        protected void onPreExecute() {// progressDialog??? ??????(????????? ??????????????? ??????)
            progressDialog = ProgressDialog.show(Parking.this, "????????? ??????????????????", "??????????????????.", true);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean s) {// ?????? ?????? ?????? ??????
            super.onPostExecute(s);
            for (int i = 0; i < count; i++) {
                if (name[i] == null || lontude[i] == null || lati[i] == null || operD[i] == null||parking[i].equals("??????")||parking[i].equals("??????")||lnmadr[i] == null|| Number[i] == null|| prknm[i] == null||name[i].equals("?????????????????????")||Number[i].equals("031-481-6316")) {
                    continue;
                }
                markers[markNumber] = new Marker();
                markers[markNumber].setPosition(new LatLng(Double.parseDouble(lati[i]), Double.parseDouble(lontude[i])));
                markers[markNumber].setHideCollidedMarkers(true);
                markers[markNumber].setWidth(50);
                markers[markNumber].setHeight(50);
                markers[markNumber].setIcon(OverlayImage.fromResource(R.drawable.ic_parking));
                markers[markNumber].setMap(mNaverMap);
                markers[markNumber].setOnClickListener(com.example.project.Parking.this);
                markNumber++;
            }
            progressDialog.dismiss();
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

    public void UpdateMarkers(int s) {// ??????,?????? ?????? ??????
        for (int i = 0; i < markNumber; i++) {
            markers[i].setMap(null);
        }// ?????? ?????????
        markNumber = 0;
        if (s == 0) {// ?????? ????????? ??????
            for (int i = 0; i < count; i++) {
                if (name[i] == null || lontude[i] == null || lati[i] == null || operD[i] == null||parking[i].equals("??????")||parking[i].equals("??????")||lnmadr[i] == null|| Number[i] == null|| prknm[i] == null||name[i].equals("?????????????????????")||Number[i].equals("031-481-6316")) {
                    continue;
                }
                markers[markNumber] = new Marker();
                markers[markNumber].setPosition(new LatLng(Double.parseDouble(lati[i]), Double.parseDouble(lontude[i])));
                markers[markNumber].setHideCollidedMarkers(true);
                markers[markNumber].setWidth(50);
                markers[markNumber].setHeight(50);
                markers[markNumber].setIcon(OverlayImage.fromResource(R.drawable.ic_parking));
                markers[markNumber].setMap(mNaverMap);
                markers[markNumber].setOnClickListener(com.example.project.Parking.this);
                markNumber++;
            }
        } else if (s == 1) {// ?????? ????????? ??????
            for (int i = 0; i < count; i++) {
                if (name[i] == null || lontude[i] == null || lati[i] == null || operD[i] == null||parking[i].equals("??????")||lnmadr[i] == null|| Number[i]  == null || prknm[i]== null|| basicT[i]== null|| basicC[i]== null|| addUT[i]== null|| addUC[i]== null) {
                    continue;
                }
                markers[markNumber] = new Marker();
                markers[markNumber].setPosition(new LatLng(Double.parseDouble(lati[i]), Double.parseDouble(lontude[i])));
                markers[markNumber].setHideCollidedMarkers(true);
                markers[markNumber].setWidth(50);
                markers[markNumber].setHeight(50);
                markers[markNumber].setIcon(OverlayImage.fromResource(R.drawable.ic_parking22));
                markers[markNumber].setMap(mNaverMap);
                markers[markNumber].setOnClickListener(com.example.project.Parking.this);
                markNumber++;
            }
        }
    }


}