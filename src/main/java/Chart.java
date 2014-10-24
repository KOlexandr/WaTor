import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import javax.swing.*;
import java.awt.*;

public class Chart<T extends Number> extends ApplicationFrame {

    private String title;
    private AxisData<T> data;

    public Chart(String title, AxisData<T> data) {
        super(title);
        this.data = data;
        this.title = title;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        final JFreeChart chart = createChart(createDataSet());
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 400));
        setContentPane(chartPanel);
    }

    private XYDataset createDataSet() {
        final XYSeries series = new XYSeries(data.getSeriesName());
        for (int i = 0; i < data.getSize(); i++) {
            series.add(data.getxDataList().get(i), data.getyDataList().get(i));
        }
        return new XYSeriesCollection(series);
    }

    private JFreeChart createChart(final XYDataset dataSet) {
        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
                title,      // chart title
                data.getxAxisName(),                      // x axis label
                data.getyAxisName(),                      // y axis label
                dataSet,                  // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,                     // tooltips
                true                     // urls
        );
        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        //final StandardLegend legend = (StandardLegend) chart.getLegend();
        // legend.setDisplaySeriesShapes(true);

        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(renderer);

        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // OPTIONAL CUSTOMISATION COMPLETED.
        return chart;
    }
}