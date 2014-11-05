package kyeh.com.bikelights;


import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Charts sensory information from the gyroscope and accelerometer.
 *
 */
public class ChartFragment extends Fragment {

    RelativeLayout rootView;
    private Button toggleButton;

    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer  = new XYMultipleSeriesRenderer();

    private XYSeries xSeries, ySeries, zSeries;
    private GraphicalView mChartView;
    private boolean running = true;

    public ChartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("dataset", mDataset);
        outState.putSerializable("renderer", mRenderer);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void onViewStateRestored(Bundle savedState) {
        super.onViewStateRestored(savedState);
        if (savedState != null) {
            mDataset = (XYMultipleSeriesDataset) savedState.getSerializable("dataset");
            mRenderer = (XYMultipleSeriesRenderer) savedState.getSerializable("renderer");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = (RelativeLayout) inflater.inflate(R.layout.fragment_chart, container, false);
        toggleButton = (Button) rootView.findViewById(R.id.toggle_chart_button);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.getLogger("ChartFragment").log(Level.INFO, "toggling Chart");
                running = !running;
            }
        });

        xSeries = new XYSeries("X");
        ySeries = new XYSeries("Y");
        zSeries = new XYSeries("Z");

        mDataset.addSeries(xSeries);
        mDataset.addSeries(ySeries);
        mDataset.addSeries(zSeries);

        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
        mRenderer.setAxisTitleTextSize(16);
        mRenderer.setChartTitleTextSize(20);
        mRenderer.setLabelsTextSize(15);
        mRenderer.setLegendTextSize(15);
        mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
        mRenderer.setZoomButtonsVisible(true);
        mRenderer.setPointSize(5);

        int colors[] = { Color.RED, Color.GREEN, Color.BLUE };
        for (int i = 0; i < 3; i++) {
            XYSeriesRenderer renderer = new XYSeriesRenderer();
            renderer.setPointStyle(PointStyle.CIRCLE);
            renderer.setFillPoints(true);
            renderer.setDisplayChartValues(true);
            renderer.setDisplayChartValuesDistance(10);
            renderer.setColor(colors[i]);
            mRenderer.addSeriesRenderer(renderer);
        }

        mChartView = ChartFactory.getLineChartView(getActivity(), mDataset, mRenderer);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        lp.addRule(RelativeLayout.ABOVE, R.id.toggle_chart_button);
        rootView.addView(mChartView, 0, lp);

        return rootView;
    }

    public void addAccelerometerValue(long time, float x, float y, float z) {
        xSeries.add(time, x);
        ySeries.add(time, y);
        zSeries.add(time, z);
        if (running) {
            redrawChart();
        }
    }

    public void redrawChart() {
        if (mChartView != null) {
            mChartView.repaint();
        }
    }
}