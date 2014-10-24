import java.awt.*;
import java.util.*;

class ChartCanvas extends Canvas {

    private Graphics graphics;
    private Image screenImage;

    private int width;
    private int height;
    private java.util.List<Integer> x;
    private java.util.List<Integer> y;

    public ChartCanvas(int width, int height, java.util.List<Integer> x, java.util.List<Integer> y) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        setSize(width, height);
        setBackground(Color.lightGray);
    }

    public void update(Graphics g) {
        final Dimension d = getSize();
        if (screenImage == null || screenImage.getWidth(null) != d.width || screenImage.getHeight(null) != d.height) {
            screenImage = createImage(d.width, d.height);
            if (graphics != null) {
                graphics.dispose();
            }
            graphics = screenImage.getGraphics();
        }
        graphics.setColor(getBackground());
        graphics.fillRect(0, 0, d.width, d.height);
        paint(graphics);
        g.drawImage(screenImage, 0, 0, this);
    }

    public void paint(Graphics g) {
        repaint();

        g.clearRect(0, 0, width, height);
        g.setColor(Color.red);
        final Optional<Integer> maxX = x.stream().max((o1, o2) -> o1.compareTo(o2));
        final Optional<Integer> maxY = y.stream().max((o1, o2) -> o1.compareTo(o2));
        final int xDim;
        final int yDim;
        if(maxX.isPresent() && 0 != maxX.get()){
            xDim = maxX.get()/width + 1;
        } else {
            xDim = 1;
        }
        if(maxY.isPresent() && 0 != maxY.get()){
            yDim = maxY.get()/height + 1;
        } else {
            yDim = 1;
        }
        for (int i = 0; i < x.size()-1; i++) {
            g.drawLine(x.get(i)/xDim, y.get(i)/yDim*2, x.get(i+1)/xDim, y.get(i+1)/yDim*2);
        }
    }
}