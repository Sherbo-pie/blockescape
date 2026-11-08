package blockescape;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * Draws the board and block in an oblique 3D projection.
 *
 * The grid stays axis-aligned, so controls match the screen:
 * UP = up, DOWN = down, LEFT = left, RIGHT = right.
 * Height (z) pushes points up and slightly left, which exposes the front
 * and right faces of every tile and of the block.
 *
 * Pure rendering: it reads game state and never changes it.
 */
public final class IsoRenderer {

    private IsoRenderer() { }

    private static final Color TILE_A =
            new Color(252, 252, 255);
    private static final Color TILE_B =
            new Color(238, 240, 251);
    private static final Color TILE_LEFT =
            new Color(190, 194, 220);
    private static final Color TILE_RIGHT =
            new Color(214, 218, 240);
    private static final Color EDGE =
            new Color(110, 116, 170, 110);

    private static final Color BLOCK_TOP =
            new Color(242, 64, 114);
    private static final Color BLOCK_LEFT =
            new Color(166, 22, 68);
    private static final Color BLOCK_RIGHT =
            new Color(206, 40, 92);
    private static final Color BLOCK_EDGE =
            new Color(110, 10, 44);

    /** Tile slab thickness, in height units. */
    private static final double THICK = 0.32;

    /** How far height pushes a point left (0 = straight up). */
    private static final double SHEAR_X = 0.55;

    /** How far height pushes a point up. */
    private static final double LIFT_Y = 0.80;

    /**
     * Maps grid coordinates to screen coordinates.
     *
     * Row controls vertical position.
     * Column controls horizontal position.
     * Height shifts the point up and to the left (oblique 3D).
     */
    public static final class Projection {

        public final double tw;
        public final double th;
        public final double unit;
        public final double cx0;
        public final double cy0;

        public Projection(double tw, double cx0, double cy0) {
            this.tw = tw;
            this.th = tw;
            this.unit = tw / 2.0;
            this.cx0 = cx0;
            this.cy0 = cy0;
        }

        public double x(double r, double c, double z) {
            return cx0 + c * tw - z * unit * SHEAR_X;
        }

        public double y(double r, double c, double z) {
            return cy0 + r * th - z * unit * LIFT_Y;
        }
    }

    // --------------------------------------------------
    // Whole-scene rendering
    // --------------------------------------------------

    public static void render(Graphics2D g, GameController ctl, int w, int h,
                              int topMargin, int bottomMargin) {

        Board board = ctl.getBoard();

        int rows = board.getRows();
        int cols = board.getCols();

        double availW = w - 60.0;
        double availH = h - topMargin - bottomMargin;

        double twByWidth = availW / (cols + 0.6);

        // Extra vertical space for the block standing upright.
        double twByHeight =
                availH / (rows + 1.0 + THICK / 2.0 + 0.1);

        double tw = Math.max(
                20,
                Math.min(110, Math.min(twByWidth, twByHeight))
        );

        double contentH =
                tw * (rows + 1.0 + THICK / 2.0);

        // Small right shift balances the left lean of tall blocks.
        double cx0 = (w - cols * tw) / 2.0 + tw * 0.25;

        double cy0 =
                topMargin + (availH - contentH) / 2.0 + tw;

        Projection p = new Projection(tw, cx0, cy0);

        // Back to front: top row first, so lower rows overlap upper ones.
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                drawBoardTile(g, p, ctl, r, c);
            }
        }

        drawPlayerBlock(g, p, ctl);
    }

    // --------------------------------------------------
    // Board tile rendering
    // --------------------------------------------------

    private static void drawBoardTile(Graphics2D g, Projection p,
                                      GameController ctl, int r, int c) {

        Tile t = ctl.getBoard().getTile(r, c);

        if (t.isVoid()) {
            return;
        }

        // Bridge tile.
        if (ctl.isBridge(r, c)) {

            Composite old = g.getComposite();

            g.setComposite(
                    AlphaComposite.SrcOver.derive(0.85f)
            );

            drawSlab(
                    g, p, r, c,
                    new Color(255, 216, 146),
                    new Color(214, 160, 84),
                    new Color(238, 188, 112)
            );

            g.setComposite(old);
            return;
        }

        // Goal tile.
        if (t.isGoal()) {

            drawSlab(
                    g, p, r, c,
                    new Color(70, 62, 110),
                    new Color(46, 40, 80),
                    new Color(58, 50, 94)
            );

            insetDiamond(
                    g, p, r, c, 0.16,
                    new Color(14, 10, 30), null, 0
            );

            insetDiamond(
                    g, p, r, c, 0.16,
                    null, new Color(80, 230, 150, 90), 9f
            );

            insetDiamond(
                    g, p, r, c, 0.16,
                    null, new Color(90, 240, 160), 3.5f
            );

            return;
        }

        // Hole tile.
        if (!t.isSafe()) {

            drawSlab(
                    g, p, r, c,
                    new Color(88, 56, 100),
                    new Color(60, 36, 74),
                    new Color(74, 46, 88)
            );

            insetDiamond(
                    g, p, r, c, 0.10,
                    new Color(22, 8, 30), null, 0
            );

            return;
        }

        // Normal tile.
        drawSlab(
                g, p, r, c,
                ((r + c) % 2 == 0) ? TILE_A : TILE_B,
                TILE_LEFT,
                TILE_RIGHT
        );

        // Switch tile.
        if (t.isSwitch()) {

            insetDiamond(
                    g, p, r, c, 0.2,
                    new Color(60, 110, 240, 40),
                    new Color(40, 90, 230),
                    3.5f
            );
        }
    }

    // --------------------------------------------------
    // Player block rendering
    // --------------------------------------------------

    private static void drawPlayerBlock(Graphics2D g, Projection p,
                                        GameController ctl) {

        Block b = ctl.getBlock();

        int hr = 1;
        int wc = 1;

        double height = 2.0;

        if (b.getOrientation() == Block.Orientation.HORIZONTAL) {
            wc = 2;
            height = 1.0;

        } else if (b.getOrientation() == Block.Orientation.VERTICAL) {
            hr = 2;
            height = 1.0;
        }

        double z0 = 0;

        Composite old = g.getComposite();

        boolean fallen = ctl.getStatus() == GameRules.Status.FALLEN;

        // Soft contact shadow on the tiles (cast down-right).
        if (!fallen) {
            double off = 0.10 * height;
            Path2D.Double shadow = path(
                    p,
                    pt(b.getRow() + 0.10 + off, b.getCol() + 0.10 + off, 0),
                    pt(b.getRow() + 0.10 + off, b.getCol() + wc - 0.10 + off, 0),
                    pt(b.getRow() + hr - 0.10 + off, b.getCol() + wc - 0.10 + off, 0),
                    pt(b.getRow() + hr - 0.10 + off, b.getCol() + 0.10 + off, 0)
            );
            g.setColor(new Color(30, 20, 70, 75));
            g.fill(shadow);
        }

        // Falling block effect.
        if (fallen) {

            z0 = -2.8;

            g.setComposite(
                    AlphaComposite.SrcOver.derive(0.55f)
            );
        }

        double inset = 0.05;

        drawBox(
                g, p,
                b.getRow() + inset,
                b.getCol() + inset,
                hr - 2 * inset,
                wc - 2 * inset,
                height,
                z0,
                BLOCK_TOP,
                BLOCK_LEFT,
                BLOCK_RIGHT,
                BLOCK_EDGE
        );

        g.setComposite(old);
    }

    // --------------------------------------------------
    // Reusable shapes
    // --------------------------------------------------

    public static void drawNormalTile(Graphics2D g, Projection p,
                                      int r, int c) {

        drawSlab(
                g, p, r, c,
                ((r + c) % 2 == 0) ? TILE_A : TILE_B,
                TILE_LEFT,
                TILE_RIGHT
        );
    }

    public static void drawBlockShape(Graphics2D g, Projection p,
                                      int r, int c,
                                      int hr, int wc, double height) {

        double inset = 0.05;

        drawBox(
                g, p,
                r + inset,
                c + inset,
                hr - 2 * inset,
                wc - 2 * inset,
                height,
                0,
                BLOCK_TOP,
                BLOCK_LEFT,
                BLOCK_RIGHT,
                BLOCK_EDGE
        );
    }

    // --------------------------------------------------
    // Tile slab
    // --------------------------------------------------

    private static void drawSlab(Graphics2D g, Projection p,
                                 int r, int c,
                                 Color top, Color left, Color right) {

        // Front face (bottom edge of the tile).
        face(
                g, p, left, EDGE,
                pt(r + 1, c, 0),
                pt(r + 1, c + 1, 0),
                pt(r + 1, c + 1, -THICK),
                pt(r + 1, c, -THICK)
        );

        // Right face.
        face(
                g, p, right, EDGE,
                pt(r + 1, c + 1, 0),
                pt(r, c + 1, 0),
                pt(r, c + 1, -THICK),
                pt(r + 1, c + 1, -THICK)
        );

        // Top surface.
        face(
                g, p, top, EDGE,
                pt(r, c, 0),
                pt(r, c + 1, 0),
                pt(r + 1, c + 1, 0),
                pt(r + 1, c, 0)
        );
    }

    // --------------------------------------------------
    // 3D block
    // --------------------------------------------------

    private static void drawBox(Graphics2D g, Projection p,
                                double r0, double c0,
                                double hr, double wc,
                                double h, double z0,
                                Color top, Color left,
                                Color right, Color edge) {

        double r1 = r0 + hr;
        double c1 = c0 + wc;

        double zt = z0 + h;

        // Front face.
        face(
                g, p, left, edge,
                pt(r1, c0, zt),
                pt(r1, c1, zt),
                pt(r1, c1, z0),
                pt(r1, c0, z0)
        );

        // Right face.
        face(
                g, p, right, edge,
                pt(r1, c1, zt),
                pt(r0, c1, zt),
                pt(r0, c1, z0),
                pt(r1, c1, z0)
        );

        // Top surface.
        face(
                g, p, top, edge,
                pt(r0, c0, zt),
                pt(r0, c1, zt),
                pt(r1, c1, zt),
                pt(r1, c0, zt)
        );
    }

    // --------------------------------------------------
    // Inset tile decorations
    // --------------------------------------------------

    private static void insetDiamond(Graphics2D g, Projection p,
                                     int r, int c,
                                     double inset,
                                     Color fill, Color stroke,
                                     float width) {

        Path2D.Double path = path(
                p,
                pt(r + inset, c + inset, 0),
                pt(r + inset, c + 1 - inset, 0),
                pt(r + 1 - inset, c + 1 - inset, 0),
                pt(r + 1 - inset, c + inset, 0)
        );

        if (fill != null) {
            g.setColor(fill);
            g.fill(path);
        }

        if (stroke != null) {

            g.setColor(stroke);

            g.setStroke(
                    new BasicStroke(
                            width,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            g.draw(path);
        }
    }

    // --------------------------------------------------
    // Coordinate helpers
    // --------------------------------------------------

    private static double[] pt(double r, double c, double z) {
        return new double[] { r, c, z };
    }

    private static Path2D.Double path(Projection p, double[]... pts) {

        Path2D.Double path = new Path2D.Double();

        for (int i = 0; i < pts.length; i++) {

            double x = p.x(pts[i][0], pts[i][1], pts[i][2]);
            double y = p.y(pts[i][0], pts[i][1], pts[i][2]);

            if (i == 0) {
                path.moveTo(x, y);

            } else {
                path.lineTo(x, y);
            }
        }

        path.closePath();

        return path;
    }

    // --------------------------------------------------
    // Draw a face (with a light top-to-bottom gradient for depth)
    // --------------------------------------------------

    private static Color lighten(Color c, float amount) {
        int r = c.getRed() + Math.round((255 - c.getRed()) * amount);
        int gr = c.getGreen() + Math.round((255 - c.getGreen()) * amount);
        int b = c.getBlue() + Math.round((255 - c.getBlue()) * amount);
        return new Color(r, gr, b, c.getAlpha());
    }

    private static void face(Graphics2D g, Projection p,
                             Color fill, Color edge,
                             double[]... pts) {

        Path2D.Double path = path(p, pts);
        Rectangle2D b = path.getBounds2D();

        g.setPaint(new GradientPaint(
                (float) b.getMinX(), (float) b.getMinY(),
                lighten(fill, 0.16f),
                (float) b.getMinX(), (float) b.getMaxY(),
                fill
        ));
        g.fill(path);

        g.setColor(edge);
        g.setStroke(new BasicStroke(1f));
        g.draw(path);
    }
}