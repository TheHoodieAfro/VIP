package com.example.airquality;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class Conection extends Activity {

    private int MaxChars = 50000;
    private UUID deviceUUID;

    private Button clearBtn;
    private ScrollView viewData;
    private TextView dataTv;

    private BluetoothDevice device;
    private BluetoothSocket BtSocket;

    private boolean UserDisconnect = false;
    private boolean Connection = false;
    private ProgressDialog progressDialog;
    private Read reader = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conection);

        clearBtn = findViewById(R.id.clearBtn);
        viewData = findViewById(R.id.viewData);
        dataTv = findViewById(R.id.dataTv);
        dataTv.setMovementMethod(new ScrollingMovementMethod());

        Intent intent =getIntent();
        Bundle ex = intent.getExtras();
        device = ex.getParcelable(MainActivity.DEVICE_EXTRA);
        deviceUUID = UUID.fromString(ex.getString(MainActivity.DEVICE_UUID));
        MaxChars = ex.getInt(MainActivity.BUFFER_SIZE);

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataTv.setText("");
            }
        });
    }

    private class Read implements Runnable {

        private boolean stop = false;
        private Thread read;

        public Read() {
            read = new Thread(this, "reader");
            read.start();
        }

        public boolean isRunning() {
            return read.isAlive();
        }

        @Override
        public void run() {
            InputStream inputStream;

            try {
                inputStream = BtSocket.getInputStream();
                while (!stop) {
                    byte[] buffer = new byte[256];
                    if (inputStream.available() > 0) {
                        inputStream.read(buffer);

                        int x=0;
                        for (int i=0; i<buffer.length && buffer[i] != 0; i++) {
                            x++;
                        }
                        final String data = new String(buffer, 0, x);

                        dataTv.post(new Runnable() {
                            @Override
                            public void run() {
                                dataTv.append(data);

                                int txtLength = dataTv.getEditableText().length();
                                if(txtLength > MaxChars){
                                    dataTv.getEditableText().delete(0, txtLength - MaxChars);
                                }

                                viewData.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        viewData.fullScroll(View.FOCUS_DOWN);
                                    }
                                });

                            }
                        });


                    }
                    Thread.sleep(500);
                }
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public void stop() {
            stop = true;
        }

    }

    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (reader != null) {
                reader.stop();
                while (reader.isRunning());
                reader = null;
            }

            try {
                BtSocket.close();
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Connection = false;
            if (UserDisconnect) {
                finish();
            }
        }

    }

    @Override
    protected void onPause() {
        if (BtSocket != null && Connection) {
            new DisConnectBT().execute();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (BtSocket == null || !Connection) {
            new ConnectBT().execute();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean Success = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(Conection.this, "Wait", "Connecting");
        }

        @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (BtSocket == null || !Connection) {
                    BtSocket = device.createInsecureRfcommSocketToServiceRecord(deviceUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    BtSocket.connect();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Success = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!Success) {
                Toast.makeText(getApplicationContext(), "Not able to connect to the device", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Connected to device", Toast.LENGTH_LONG).show();
                Connection = true;
                reader = new Read(); // Kick off input reader
            }

            progressDialog.dismiss();
        }

    }

}
