package kyeh.com.bikelights;


import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;


/**
 * Charts sensory information from the gyroscope and accelerometer.
 *
 */
public class ChartFragment extends Fragment {

    LinearLayout rootView;

    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer  = new XYMultipleSeriesRenderer();

    private XYSeries xSeries, ySeries, zSeries;
    private GraphicalView mChartView;

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

        rootView = (LinearLayout) inflater.inflate(R.layout.fragment_main, container, false);

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
        rootView.addView(mChartView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        return rootView;
    }

    public void addAccelerometerValue(long time, float x, float y, float z) {
        xSeries.add(time, x);
        ySeries.add(time, y);
        zSeries.add(time, z);
        redrawChart();
    }

    public void redrawChart() {
        if (mChartView != null) {
            mChartView.repaint();
        }
    }
}