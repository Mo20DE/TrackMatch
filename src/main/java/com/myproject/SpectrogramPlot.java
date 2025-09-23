package com.fftplot;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;

import org.jfree.chart.renderer.GrayPaintScale;

import javax.swing.*;
import java.awt.*;

public class SpectrogramPlot extends JFrame {

    public SpectrogramPlot(String title, double[][] spectrogram) {
        super(title);

        int rows = spectrogram.length;       // Zeit (Chunks)
        int cols = spectrogram[0].length;    // Frequenzen

        // Daten vorbereiten
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        double[] x = new double[rows * cols];
        double[] y = new double[rows * cols];
        double[] z = new double[rows * cols];

        int idx = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                x[idx] = i;
                y[idx] = j;
                z[idx] = spectrogram[i][j];
                idx++;
            }
        }

        dataset.addSeries("Spectrogram", new double[][] {x, y, z});

        // Renderer
        XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setBlockWidth(1.0);
        renderer.setBlockHeight(1.0);

        // PaintScale (Schwarz → Weiß)
        double maxVal = findMax(spectrogram);
        LookupPaintScale scale = new LookupPaintScale(0, findMax(spectrogram), Color.BLACK);
        scale.add(findMax(spectrogram), Color.WHITE);
        renderer.setPaintScale(new GrayPaintScale(0, findMax(spectrogram)));

        // Plot
        NumberAxis xAxis = new NumberAxis("Time");
        NumberAxis yAxis = new NumberAxis("Frequency");
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        JFreeChart chart = new JFreeChart("Spectrogram", JFreeChart.DEFAULT_TITLE_FONT, plot, false);

        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(800, 600));
        setContentPane(panel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null); // zentrieren
    }

    private double findMax(double[][] arr) {
        double max = Double.MIN_VALUE;
        for (double[] row : arr)
            for (double val : row)
                if (val > max) max = val;
        return max;
    }

    public static void show(double[][] spectrogram) {
        SwingUtilities.invokeLater(() -> {
            SpectrogramPlot plot = new SpectrogramPlot("Spectrogram", spectrogram);
            plot.setVisible(true);
        });
    }
}