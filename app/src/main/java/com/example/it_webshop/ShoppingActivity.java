package com.example.it_webshop;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ShoppingActivity extends AppCompatActivity {

    private static final String LOG_TAG = ShoppingActivity.class.getName();
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;

    private RecyclerView recyclerView;
    private ArrayList<Item> itemList;
    private ItemAdapter mAdapter;
    private final int gridNumber = 1;
    private Toolbar toolbar;

    private FrameLayout redCircle;
    private TextView contentTextView;
    private int cartItems = 0;
    private int queryLimit = 7;

    private NotificationHandler mNotifHandler;
    private AlarmManager mAlarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();



        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));

        itemList = new ArrayList<>();

        mAdapter = new ItemAdapter(this, itemList);

        recyclerView.setAdapter(mAdapter);


        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("Items");
        queryData();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReceiver, filter);

        mNotifHandler = new NotificationHandler(this);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        setAlarmManager();
    }

    BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();

            if (intentAction == null)
                return;

            switch (intentAction) {
                case Intent.ACTION_POWER_CONNECTED:
                    queryLimit = 7;
                    queryData();
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    queryLimit = 4;
                    queryData();
                    break;
            }
        }
    };

    private void queryData() {
        Log.d(LOG_TAG, "querydata entered");
        itemList.clear();

        //mItems.whereEqualTo();
        mItems.orderBy("cartedCount", Query.Direction.DESCENDING).limit(queryLimit).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc: queryDocumentSnapshots){
                Item item = doc.toObject(Item.class);
                item.setId(doc.getId());
                itemList.add(item);
            }


            if (itemList.isEmpty()){
                initialiseData();
                queryData();
            }

            mAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e(LOG_TAG, "Firestore error: ", e);
        });
    }
    public void deleteItem(Item item){
        if (user == null){
            Toast.makeText(this, "Ehhez a tevékenységhez be kell jelentkeznie", LENGTH_LONG).show();
            return;
        }
        DocumentReference ref = mItems.document(item._getId());

        ref.delete().addOnSuccessListener(s -> {
            Log.d(LOG_TAG, "Item successfully deleted "+ item._getId());
        })
        .addOnFailureListener(f -> {
            Toast.makeText(this, "Sikertelen törlés: " + item._getId(), LENGTH_LONG).show();
        });
        queryData();
        mNotifHandler.cancel();
    }

    private void initialiseData() {
        String[] itemsList = getResources()
                .getStringArray(R.array.shopping_item_names);
        String[] itemsInfo = getResources()
                .getStringArray(R.array.shopping_item_desc);
        String[] itemsPrice = getResources()
                .getStringArray(R.array.shopping_item_price);
        TypedArray itemsImageResources =
                getResources().obtainTypedArray(R.array.shopping_item_images);

        for (int i = 0; i < itemsList.length; i++) {
            mItems.add(new Item(
                    itemsImageResources.getResourceId(i, 0),
                    itemsInfo[i],
                    itemsList[i],
                    itemsPrice[i]
                    ,0));
        }

        itemsImageResources.recycle();

        Log.d(LOG_TAG, "initdata finish");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.shop_list_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(LOG_TAG,s);
                mAdapter.getFilter().filter(s);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.cart){
            Log.d(LOG_TAG, "Cart clicked!");
            return true;
        } else if (item.getItemId() == R.id.log_out_button) {
            Log.d(LOG_TAG, "Logout clicked!");
            if (user == null){
                Toast.makeText(this, "Ehhez a tevékenységhez be kell jelentkeznie!", LENGTH_LONG).show();
            }else{
                FirebaseAuth.getInstance().signOut();
                finish();
            }
            return true;
        } else if (item.getItemId() == R.id.settings_button) {
            Log.d(LOG_TAG, "Settings clicked!");
            return true;
        } else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();

        redCircle = (FrameLayout) rootView.findViewById(R.id.view_alert_red_circle);
        contentTextView = (TextView) rootView.findViewById(R.id.view_alert_count_textview);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(alertMenuItem);
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon(Item item){
        if (user == null){
            Toast.makeText(this, "Ehhez a tevékenységhez be kell jelentkeznie", LENGTH_LONG).show();
            return;
        }
        cartItems = cartItems + 1;
        if (0 < cartItems){
            contentTextView.setText(String.valueOf(cartItems));
        }else{
            contentTextView.setText("");
        }

        redCircle.setVisibility((cartItems > 0) ? VISIBLE : GONE);

        mItems.document(item._getId()).update("cartedCount", item.getCartedCount()+1)
                .addOnFailureListener(f -> {
                    Toast.makeText(this, "Sikertelen frissítés: " + item._getId(), LENGTH_LONG).show();
                });
        mNotifHandler.send(item.getName() + " Hozzáadva a kosárhoz!");
        queryData();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReceiver);
    }

    private void setAlarmManager(){
        long rInterval = 60 * 1000;
        long triggerTime = SystemClock.elapsedRealtime() + rInterval;
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, FLAG_IMMUTABLE);

        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, rInterval, pendingIntent);
    }
}