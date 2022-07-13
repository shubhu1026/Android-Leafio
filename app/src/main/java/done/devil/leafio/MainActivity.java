package done.devil.leafio;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    private ImageView imageView;
    private ImageView addIcon;
    private TextView predictionResult;
    private Button btnKnowMore;

    private String selectedImage;
    private String search = null;

    final Dialog loadingdialog = new Dialog(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.img_preview);
        Button btnGallery = findViewById(R.id.btnGallery);
        Button btnCamera = findViewById(R.id.btnCamera);
        Button btnPredict = findViewById(R.id.btnPredict);
        btnKnowMore = findViewById(R.id.btnKnowMore);
        addIcon= findViewById(R.id.addIcon);
        predictionResult= findViewById(R.id.predictionResult);

        btnGallery.setOnClickListener(v -> {
            reset();
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, 1);
            }else{
                requestStoragePermission();
            }
        });

        btnCamera.setOnClickListener(v -> {
            reset();
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePic, 0);
            }else{
                requestCameraPermission();
            }
        });

        btnKnowMore.setOnClickListener(v -> {
            if (search!=null) {
                String url = "http://www.google.com/search?q=" + search;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }else {
                Toast.makeText(getApplicationContext(),"Result is null, please try this later.",Toast.LENGTH_SHORT).show();
            }
        });

        btnPredict.setOnClickListener(v -> {
            if(selectedImage.equals("")) {
                Toast.makeText(getApplicationContext(),"Please select an image",Toast.LENGTH_SHORT).show();
            }
            else {
                uploadFileToServer();
            }
        });
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is required to access your Gallery")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is required to use your Camera")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode !=RESULT_CANCELED){

            switch (requestCode){
                case 0:
                    if(resultCode == RESULT_OK && data !=null){
                        Bitmap image = (Bitmap) data.getExtras().get("data");
                        Bitmap reducedBitmap = ImageResizer.reduceBitmapSize(image,65536);
                        selectedImage = FileUtils.getPath(MainActivity.this, getImageUri(MainActivity.this,reducedBitmap));
                        imageView.setImageBitmap(reducedBitmap);
                        addIcon.setVisibility(View.INVISIBLE);
                    }
                    break;
                case 1:
                    if(resultCode == RESULT_OK && data !=null){

                        Uri image = data.getData();

                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
                            Bitmap reducedBitmap = ImageResizer.reduceBitmapSize(bitmap,65536);
                            selectedImage = FileUtils.getPath(MainActivity.this, getImageUri(MainActivity.this,reducedBitmap));
                            imageView.setImageBitmap(reducedBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        addIcon.setVisibility(View.INVISIBLE);
                    }
            }

        }
    }

    public Uri getImageUri(Context context, Bitmap bitmap){
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "myImage","");
        return Uri.parse(path);
    }


    public void uploadFileToServer(){

        loadingdialog.startLoadingDialog();

        File file = new File(Uri.parse(selectedImage).getPath());

        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"),file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("imagefile",file.getName(),requestBody);

        ApiInterface service = RetrofitBuilder.getClient().create(ApiInterface.class);

        Call<ResultModel> call = service.callUploadApi(filePart);
        call.enqueue(new Callback<ResultModel>() {
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                ResultModel resultModel = response.body();
                if (response.isSuccessful()) {
                    loadingdialog.dismissDialog();
                    search = resultModel.getResult();
                    predictionResult.setText(resultModel.getResult());
                    btnKnowMore.setVisibility(View.VISIBLE);
                }
                else {
                    loadingdialog.dismissDialog();
                    Toast.makeText(getApplicationContext(),"There was an error. Please Try Again.",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                loadingdialog.dismissDialog();
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reset(){
        selectedImage = "";
        predictionResult.setText("");
        imageView.setImageResource(R.drawable.ic_add_small);
        addIcon.setVisibility(View.VISIBLE);
        search = null;
    }

}