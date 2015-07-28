package com.seshtutoring.seshapp.view.components;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import org.json.JSONException;
import org.json.JSONObject;

import fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment;

/**
 * Created by lillioetting on 7/20/15.
 */
public class SeshDialog extends BlurDialogFragment {
    OnSelectionListener mCallback;
    public enum SeshDialogType { ONE_BUTTON, TWO_BUTTON };

    private static final String TAG = SeshDialog.class.getName();
    private Bitmap backgroundOverlayBitmap = null;

    public String firstChoice;
    public String secondChoice;
    public String title;
    public String message;
    public String type;
    public SeshDialogType dialogType = SeshDialogType.TWO_BUTTON;

    // Container Activity must implement this interface
    public interface OnSelectionListener {
        public void onDialogSelection(int selected, String type);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnSelectionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final SeshNetworking seshNetworking = new SeshNetworking(getActivity());
        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Medium.otf");

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.sesh_dialog_layout, null);

        TextView titleText = (TextView) view.findViewById(R.id.dialog_title);
        titleText.setText(title);
        titleText.setTypeface(bold);

        TextView text = (TextView) view.findViewById(R.id.dialog_text);
        text.setText(message);

        Button affirmativeButton = (Button) view.findViewById(R.id.dialog_first_button);
        affirmativeButton.setText(firstChoice);
        affirmativeButton.setTypeface(bold);

        affirmativeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCallback.onDialogSelection(1, type);
                dismiss();
            }
        });

        Button negativeButton = (Button) view.findViewById(R.id.dialog_second_button);

        if (dialogType == SeshDialogType.ONE_BUTTON) {
            negativeButton.setVisibility(View.GONE);
        } else {
            negativeButton.setText(secondChoice);
            negativeButton.setTypeface(bold);

            negativeButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mCallback.onDialogSelection(2, type);
                    dismiss();
                }
            });
        }

//        builder.setTitle("Cash Out?")
//                        //.setView() - set view to new view created above
//                .setPositiveButton(R.string.affirmative, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        //yes, proceed to cashout
//                        Log.e(TAG, "YES BUTTON PRESS");
//                    }
//                })
//                .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        //no, cancel cashout
//                        Log.e(TAG, "NO BUTTON PRESS");
//                    }
//                });
        // Create the AlertDialog object and return it
        builder.setView(view);

        return builder.create();
    }

    public static void showDialog(FragmentManager manager, String title, String message,
                                  String firstChoice, String secondChoice,
                                  Bitmap customBackground, String type) {
        SeshDialog dialog = new SeshDialog();

        if (secondChoice != null) {
            dialog.dialogType = SeshDialog.SeshDialogType.TWO_BUTTON;
        } else  {
            dialog.dialogType = SeshDialog.SeshDialogType.ONE_BUTTON;
        }

        dialog.title = title;
        dialog.message = message;
        dialog.firstChoice = firstChoice;
        dialog.secondChoice = secondChoice;
        dialog.setCustomBackgroundBitmap(customBackground);
        dialog.type = type;

        dialog.show(manager, type);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) {
            return;
        }

        LayoutUtils utils = new LayoutUtils(getActivity());

        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = utils.dpToPixels(330f);
        getDialog().getWindow().setAttributes(params);
    }

    @Override
    protected float getDownScaleFactor() {
        // Allow to customize the down scale factor.
        return 5;
    }

    @Override
    protected int getBlurRadius() {
        // Allow to customize the blur radius factor.
        return 7;
    }

    @Override
    protected boolean isActionBarBlurred() {
        // Enable or disable the blur effect on the action bar.
        // Disabled by default.
        return true;
    }

    @Override
    protected Bitmap getCustomBackgroundBitmap() {
        return backgroundOverlayBitmap;
    }

    /**
     * Overrides blur dialog's default behavior - instead of blurring the background
     * before showing dialog, Bitmap that is passed in will be blurred and placed in the background
     * @param overlay
     */
    public void setCustomBackgroundBitmap(Bitmap overlay) {
        backgroundOverlayBitmap = overlay;
    }
}

