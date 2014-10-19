package kyeh.com.bikelights;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class SparkLightsFragment extends Fragment {

    private TextView statusText, sparkText, armText;
    private Button calibrateYawButton;
    private MyoDeviceListener mMyoListener;

    public SparkLightsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_spark_lights, container, false);
        statusText = (TextView) rootView.findViewById(R.id.text_status);
        sparkText = (TextView) rootView.findViewById(R.id.spark_status);
        armText = (TextView) rootView.findViewById(R.id.arm_status);

        // Calibrate Yaw with arm in yaw=1 position (Arm down by your side)
        calibrateYawButton = (Button) rootView.findViewById(R.id.calibrate_yaw_button);

        if (mMyoListener != null) {
            calibrateYawButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mMyoListener.calibrateYaw();
                }
            });
        }
        return rootView;
    }

    public void setMyoDeviceListener(MyoDeviceListener listener) {
        mMyoListener = listener;
        if (calibrateYawButton != null) {
            calibrateYawButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mMyoListener.calibrateYaw();
                }
            });
        }
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
}
