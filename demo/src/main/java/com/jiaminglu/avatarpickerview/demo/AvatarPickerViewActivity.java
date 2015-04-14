package com.jiaminglu.avatarpickerview.demo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.jiaminglu.avatarpickerview.AvatarPickerView;

public class AvatarPickerViewActivity extends ActionBarActivity {

    AvatarPickerView source;
    ImageView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_picker_view);
        source = (AvatarPickerView) findViewById(R.id.source);
        result = (ImageView) findViewById(R.id.result);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        setResult(RESULT_CANCELED);
    }

    private void openImageIntent() {

        final File filename = new File(Environment.getExternalStorageDirectory(), "avatar_capture");
        Uri outputFileUri = Uri.fromFile(filename);

        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.select_pic));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    final static int PICK_IMAGE = 0;

    @Override
    public void onActivityResult(int req, int res, Intent intent) {
        if (res == RESULT_OK) {
            boolean isCamera = intent == null || intent.getAction() != null && intent.getAction().equals(MediaStore.ACTION_IMAGE_CAPTURE);
            getIntent().setData(isCamera ? Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "avatar_capture")) : intent.getData());
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getData() == null)
            openImageIntent();
        else {
            source.setVisibility(View.VISIBLE);
            source.setImageURI(getIntent().getData());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_ok, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_ok) {
            result.setImageBitmap(source.getClippedBitmap(result.getWidth()));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
