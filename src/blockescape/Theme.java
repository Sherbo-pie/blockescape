package blockescape;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;

/** Shared colours, fonts and the animated sky background. */
public final class Theme {

    private Theme() { }

    public static final Color INK = new Color(78, 58, 140);
    public static final Color CRIMSON = new Color(232, 48, 98);
    public static final Color GREEN = new Color(60, 190, 120);
    public static final Color RED = new Color(225, 70, 95);

    private static final Color[] SKY = {
        new Color(134, 126, 204),
        new Color(192, 166, 224),
        new Color(247, 206, 186)
    };

    // x, y (fractions of the screen), size, speed
    private static final double[][] CLOUDS = {
        {0.10, 0.80, 1.4, 0.010}, {0.48, 0.90, 1.9, 0.007},
        {0.82, 0.76, 1.2, 0.012}, {0.25, 0.16, 0.8, 0.014},
        {0.72, 0.24, 0.9, 0.009}, {0.95, 0.93, 1.6, 0.008}
    };

    public static Font font(int style, int size) {
        return new Font(Font.SANS_SERIF, style, size);
    }

    private static double frac(double v) {
        return v - Math.floor(v);
    }

    public static void paintBackground(Graphics2D g, int w, int h, double t) {
        g.setPaint(new LinearGradientPaint(0, 0, 0, h,
                new float[] {0f, 0.55f, 1f}, SKY));
        g.fillRect(0, 0, w, h);

        // twinkling stars in the upper sky
        for (int i = 0; i < 26; i++) {
            double x = frac(i * 0.6180339) * w;
            double y = frac(i * 0.3719) * h * 0.55;
            double tw = 0.5 + 0.5 * Math.sin(t * 1.6 + i * 1.7);
            g.setColor(new Color(255, 255, 255, (int) (35 + 90 * tw)));
            g.fillOval((int) x, (int) y, 3, 3);
        }

        // drifting clouds
        double unit = Math.min(w, h) * 0.16;
        for (double[] c : CLOUDS) {
            double x = ((c[0] + t * c[3]) % 1.4 - 0.2) * w;
            double y = c[1] * h;
            double s = c[2] * unit;
            cloud(g, x, y, s);
        }
    }

    private static void cloud(Graphics2D g, double x, double y, double s) {
        g.setColor(new Color(255, 228, 244, 70));
        g.fillOval((int) (x - s * 1.1), (int) (y), (int) (s * 2.6), (int) (s * 0.6));
        g.setColor(new Color(255, 255, 255, 85));
        g.fillOval((int) (x - s), (int) (y - s * 0.2), (int) (s * 1.3), (int) (s * 0.7));
        g.fillOval((int) (x - s * 0.3), (int) (y - s * 0.5), (int) (s * 1.2), (int) (s * 0.9));
        g.fillOval((int) (x + s * 0.4), (int) (y - s * 0.2), (int) (s * 1.3), (int) (s * 0.7));
        g.fillOval((int) (x - s * 0.8), (int) (y + s * 0.05), (int) (s * 2.4), (int) (s * 0.5));
    }

    /** Text with a soft drop shadow, centred on x. */
    public static void shadowText(Graphics2D g, String text, int centerX,
                                  int baseline, Font font, Color color) {
        g.setFont(font);
        int x = centerX - g.getFontMetrics().stringWidth(text) / 2;
        g.setColor(new Color(40, 20, 90, 90));
        g.drawString(text, x + 2, baseline + 3);
        g.setColor(color);
        g.drawString(text, x, baseline);
    }
}
