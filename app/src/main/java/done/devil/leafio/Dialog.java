package done.devil.leafio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class Dialog {
    private Activity activity;
    private AlertDialog dialog;

    Dialog(Activity myActivity) {
        activity = myActivity;
    }

    @SuppressLint("InflateParams")
    void startLoadingDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading, null));
        builder.setCancelable(true);

        dialog = builder.create();
        dialog.show();
    }

    void dismissDialog() {
        dialog.dismiss();
    }

}
