package kyeh.com.bikelights;


import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class SparkLightsFragment extends Fragment implements ColorPicker.OnColorChangedListener {

    private Typeface ROBOTO_LIGHT, ROBOTO_MEDIUM;
    private TextView statusText, sparkText, armText, bearingText;

    private ColorPicker picker;
    private SaturationBar svBar;

    private Context mContext;

    public SparkLightsFragment(Context context) {
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_spark_lights, container, false);
        statusText = (TextView) rootView.findViewById(R.id.text_status);
        sparkText = (TextView) rootView.findViewById(R.id.spark_status);
        armText = (TextView) rootView.findViewById(R.id.arm_status);
        bearingText = (TextView) rootView.findViewById(R.id.bearing_status);

        Button leftButton = (Button) rootView.findViewById(R.id.turn_left_button);
        Button rightButton = (Button) rootView.findViewById(R.id.turn_right_button);
        Button offButton = (Button) rootView.findViewById(R.id.off_button);

        if (ROBOTO_LIGHT == null) {
            ROBOTO_LIGHT = Typeface.createFromAsset(getActivity().getAssets(), "Fonts/Roboto-Light.ttf");
            ROBOTO_MEDIUM = Typeface.createFromAsset(getActivity().getAssets(), "Fonts/Roboto-Medium.ttf");
        }

        statusText.setTypeface(ROBOTO_LIGHT);
        sparkText.setTypeface(ROBOTO_LIGHT);
        armText.setTypeface(ROBOTO_LIGHT);
        bearingText.setTypeface(ROBOTO_LIGHT);

        leftButton.setTypeface(ROBOTO_MEDIUM);
        rightButton.setTypeface(ROBOTO_MEDIUM);
        offButton.setTypeface(ROBOTO_MEDIUM);

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SparkClient.turnLeft(getActivity());
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SparkClient.turnRight(getActivity());
            }
        });

        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SparkClient.turnOff(getActivity());
            }
        });

        picker = (ColorPicker) rootView.findViewById(R.id.picker);
        svBar = (SaturationBar) rootView.findViewById(R.id.svbar);
        picker.addSaturationBar(svBar);
        picker.setShowOldCenterColor(false);
        picker.setColor(16711680);

        picker.setOnColorChangedListener(this);

        return rootView;
    }

    public void setStatusText(String s) {
        if (statusText != null) {
            statusText.setText(s);
        }
    }

    public void setSparkText(String s) {
        if (sparkText != null) {
            sparkText.setText(s);
        }
    }

    public void setArmText(String s) {
        if (armText != null) {
            armText.setText(s);
        }
    }

    public void setBearingText(String s) {
        if (bearingText != null) {
            bearingText.setText(s);
        }
    }

    @Override
    public void onColorChanged(int i) {
        int color = i;
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        SparkClient.setColor(mContext, r, g, b);
    }
}
