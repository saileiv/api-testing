package com.google.android.gms.nearby.messages.samples.nearbydevices;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.MessagesClient;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import com.google.android.gms.nearby.messages.samples.nearbydevices.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int TTL_IN_SECONDS = 20; // Three minutes.

    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(TTL_IN_SECONDS).build();
    private static final String MISSING_API_KEY = "It's possible that you haven't added your" +
            " API-KEY. See  " +
            "https://developers.google.com/nearby/messages/android/get-started#step_4_configure_your_project";


    private Message mMessage;


    private MessageListener mMessageListener;


    private ArrayAdapter<String> mNearbyDevicesArrayAdapter;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // The message being published is simply the Build.MODEL of the device. But since the
        // Messages API is expecting a byte array, you must convert the data to a byte array.
        mMessage = new Message("Hello World".getBytes(Charset.forName("UTF-8")));

        mMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                // Called when a new message is found.
                String msgBody = new String(message.getContent());
                mNearbyDevicesArrayAdapter.add(msgBody);
            }

            @Override
            public void onLost(final Message message) {
                // Called when a message is no longer detectable nearby.
                String msgBody = new String(message.getContent());
                mNearbyDevicesArrayAdapter.remove(msgBody);
            }
        };

        binding.subscribeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                subscribe();
            } else {
                unsubscribe();
            }
        });

        binding.publishSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                publish();
            } else {
                unpublish();
            }
        });

        final List<String> nearbyDevicesArrayList = new ArrayList<>();
        mNearbyDevicesArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                nearbyDevicesArrayList);
        final ListView nearbyDevicesListView = (ListView) findViewById(
                R.id.nearby_devices_list_view);
        if (nearbyDevicesListView != null) {
            nearbyDevicesListView.setAdapter(mNearbyDevicesArrayAdapter);
        }
    }

    private void subscribe() {
        Log.i(TAG, "Subscribing");
        mNearbyDevicesArrayAdapter.clear();
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(new SubscribeCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(TAG, "No longer subscribing");
                        runOnUiThread(() -> binding.subscribeSwitch.setChecked(false));
                    }
                }).build();

        Nearby.getMessagesClient(this).subscribe(mMessageListener, options);
    }


    private void publish() {
        Log.i(TAG, "Publishing");
        PublishOptions options = new PublishOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(new PublishCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(TAG, "No longer publishing");
                        runOnUiThread(() -> binding.publishSwitch.setChecked(false));
                    }
                }).build();

        Nearby.getMessagesClient(this).publish(mMessage, options)
                .addOnFailureListener(e -> {
                    logAndShowSnackbar(MISSING_API_KEY);
                });
    }


    private void unsubscribe() {
        Log.i(TAG, "Unsubscribing.");
        Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
    }


    private void unpublish() {
        Log.i(TAG, "Unpublishing.");
        Nearby.getMessagesClient(this).unpublish(mMessage);
    }

    private void logAndShowSnackbar(final String text) {
        Log.w(TAG, text);
        if (binding.activityMainContainer != null) {
            Snackbar.make(binding.activityMainContainer, text, Snackbar.LENGTH_LONG).show();
        }
    }
}
