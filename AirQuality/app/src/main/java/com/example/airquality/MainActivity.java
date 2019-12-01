package com.example.airquality;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ENABLE_BT  = 0;
    public static final String DEVICE_EXTRA = "com.example.anysensormonitoring.SOCKET";
    public static final String DEVICE_UUID = "com.example.anysensormonitoring.uuid";
    private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private int mBufferSize = 50000;
    public static final String BUFFER_SIZE = "com.example.anysensormonitoring.buffersize";

    private LinearLayout layout;
    private TextView statusBT;
    private ImageView BTIcon;
    private Button onBtn, offBtn, pairedBtn, ConnectBtn;
    private ListView listView;

    BluetoothAdapter BT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---------------------------------------------------------
        layout = findViewById(R.id.layout);

        statusBT = findViewById(R.id.statusBT);

        BTIcon = findViewById(R.id.BTIcon);

        onBtn = findViewById(R.id.onBtn);
        offBtn = findViewById(R.id.offBtn);
        pairedBtn = findViewById(R.id.pairedBtn);
        ConnectBtn = findViewById(R.id.ConnectBtn);

        listView = findViewById(R.id.listView);

        BT = BluetoothAdapter.getDefaultAdapter();
        //----------------------------------------------------------

        initList(new ArrayList<BluetoothDevice>());

        if(BT == null){
            statusBT.setText("Bluetooth not available");
        }else{
            statusBT.setText("Bluetooth is available");
        }

        if(BT.isEnabled()){
            BTIcon.setImageResource(R.drawable.ic_action_on);
        }else {
            BTIcon.setImageResource(R.drawable.ic_action_off);
        }

        onBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(!BT.isEnabled()){
                    showToast("Turning bluetooth on");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                }else{
                    showToast("Bluetooth already on");
                }
            }
        });

        offBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(BT.isEnabled()){
                    BT.disable();
                    showToast("Turning bluetooth off");
                    BTIcon.setImageResource(R.drawable.ic_action_off);
                }else{
                    showToast("Bluetooth already off");
                }
            }
        });

        pairedBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(BT.isEnabled()){
                    Set<BluetoothDevice> devices = BT.getBondedDevices();
                    List<BluetoothDevice> listDevices = new ArrayList<BluetoothDevice>();
                    for (BluetoothDevice device : devices) {
                        listDevices.add(device);
                    }
                    MyAdapter adapter = (MyAdapter) listView.getAdapter();
                    adapter.replaceItems(listDevices);
                }else {
                    showToast("Must turn bluetooth on first");
                }
            }
        });

        ConnectBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                BluetoothDevice device = ((MyAdapter) (listView.getAdapter())).getSelectedItem();
                Intent intent = new Intent(getApplicationContext(), Conection.class);
                intent.putExtra(DEVICE_EXTRA, device);
                intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                intent.putExtra(BUFFER_SIZE, mBufferSize);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode ) {
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK){
                    BTIcon.setImageResource(R.drawable.ic_action_on);
                    showToast("Bluetooth on");
                }else {
                    showToast("Not able to turn on");
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initList(List<BluetoothDevice> objects) {
        final MyAdapter adapter = new MyAdapter(getApplicationContext(), R.layout.list_item, R.id.lstContent, objects);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedIndex(position);
                ConnectBtn.setEnabled(true);
            }
        });
    }

    private class MyAdapter extends ArrayAdapter<BluetoothDevice> {
        private int selectedIndex;
        private Context context;
        private int selectedColor = Color.parseColor("#abcdef");
        private List<BluetoothDevice> myList;

        public MyAdapter(Context ctx, int resource, int textViewResourceId, List<BluetoothDevice> objects) {
            super(ctx, resource, textViewResourceId, objects);
            context = ctx;
            myList = objects;
            selectedIndex = -1;
        }

        public void setSelectedIndex(int position) {
            selectedIndex = position;
            notifyDataSetChanged();
        }

        public BluetoothDevice getSelectedItem() {
            return myList.get(selectedIndex);
        }

        @Override
        public int getCount() {
            return myList.size();
        }

        @Override
        public BluetoothDevice getItem(int position) {
            return myList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            TextView tv;
        }

        public void replaceItems(List<BluetoothDevice> list) {
            myList = list;
            notifyDataSetChanged();
        }

        public List<BluetoothDevice> getEntireList() {
            return myList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            ViewHolder holder;
            if (convertView == null) {
                vi = LayoutInflater.from(context).inflate(R.layout.list_item, null);
                holder = new ViewHolder();

                holder.tv = (TextView) vi.findViewById(R.id.lstContent);

                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            if (selectedIndex != -1 && position == selectedIndex) {
                holder.tv.setBackgroundColor(selectedColor);
            } else {
                holder.tv.setBackgroundColor(Color.WHITE);
            }
            BluetoothDevice device = myList.get(position);
            holder.tv.setText(device.getName() + "\n " + device.getAddress());

            return vi;
        }

    }

    private void showToast(String msj){
        Toast.makeText(this, msj, Toast.LENGTH_SHORT).show();
    }

}
