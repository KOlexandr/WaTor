import scala.Tuple2;
import scala.Tuple3;

import java.applet.Applet;
import java.awt.*;

class WaTorCanvas extends Canvas {
    private int step = 0;
    private World world;
    private Graphics graphics;
    private Image screenImage;

    private int width;
    private int height;
    private int fishCount;
    private int fishReprTime;
    private int sharkCount;
    private int sharkReprTime;
    private int sharkEnergy;

    private boolean running = false;

    public WaTorCanvas(int width, int height, int fishCount, int fishReprTime, int sharkCount, int sharkReprTime, int sharkEnergy) {
        this.width = width;
        this.height = height;
        this.fishCount = fishCount;
        this.fishReprTime = fishReprTime;
        this.sharkCount = sharkCount;
        this.sharkReprTime = sharkReprTime;
        this.sharkEnergy = sharkEnergy;
        world = new World(new Tuple2<>(height, width), new Tuple2<>(fishCount, fishReprTime), new Tuple3<>(sharkCount, sharkReprTime, sharkEnergy));
    }

    public void reload(int width, int height, int fishCount, int fishReprTime, int sharkCount, int sharkReprTime, int sharkEnergy){
        this.width = width;
        this.height = height;
        world = new World(new Tuple2<>(height, width), new Tuple2<>(fishCount, fishReprTime), new Tuple3<>(sharkCount, sharkReprTime, sharkEnergy));
    }

    public void setRunning(boolean r) {
        running = r;
        repaint();
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
        if (!running) {
            paintOcean(g);
            return;
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored){}
        world.forward(step++);
        repaint();
        paintOcean(g);
    }

    private void paintOcean(Graphics g) {
        // calculate number of pixels in one ocean square
        final Dimension d = getSize();

        final int minDim = Math.min(d.width, d.height);
        final int oceanSquareSize = Math.min(minDim / height, minDim / width);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (world.ocean()[i][j] == null) {
                    g.setColor(Color.cyan);
                } else if (world.ocean()[i][j].isFish()) {
                    g.setColor(Color.green);
                } else if (world.ocean()[i][j].isShark()) {
                    g.setColor(Color.red);
                }
                g.fillRect(j * oceanSquareSize, i * oceanSquareSize, oceanSquareSize - 1, oceanSquareSize - 1);
            }
        }
    }

    public int getFishCount() {
        return fishCount;
    }

    public int getFishReprTime() {
        return fishReprTime;
    }

    public int getSharkCount() {
        return sharkCount;
    }

    public int getSharkReprTime() {
        return sharkReprTime;
    }

    public int getSharkEnergy() {
        return sharkEnergy;
    }
}

class ScrollField extends Panel {
    private Scrollbar s;
    private TextField t;

    ScrollField(String description, int initVal) {
        setLayout(new BorderLayout());

        add("West", new Label(description));
        t = new TextField(Integer.toString(initVal), 5);
        t.addActionListener(e -> s.setValue(Integer.parseInt(t.getText())));
        add("East", t);

        s = new Scrollbar(Scrollbar.HORIZONTAL, initVal, 1, 1, 50);
        s.addAdjustmentListener(e -> t.setText(Integer.toString(s.getValue())));
        add("South", s);
    }

    public int getValue() {
        return s.getValue();
    }

    public void setValue(int val){
        s.setValue(val);
        t.setText(String.valueOf(val));
    }
}

public class WaTor extends Applet {
    private WaTorCanvas wc;
    private TextField initFishField;
    private TextField initSharkField;
    private ScrollField fishBreedScrollField;
    private ScrollField sharkBreedScrollField;
    private ScrollField sharkStarveScrollField;

    public void init() {
        setLayout(new FlowLayout());
        setSize(1000, 600);

        wc = new WaTorCanvas(40, 40, 50, 8, 5, 15, 20);
        wc.setSize(600, 600);
        add(wc);

        final Panel p = new Panel();
        p.setLayout(new GridLayout(8, 1, 3, 3));
        final Button start = new Button("Start");
        start.addActionListener(e -> wc.setRunning(true));
        p.add(start);
        final Button stop = new Button("Stop");
        stop.addActionListener(e -> wc.setRunning(false));
        p.add(stop);
        final Button reset = new Button("Reset");
        reset.addActionListener(e -> {
            wc.setRunning(false);
            initFishField.setText(String.valueOf(50));
            fishBreedScrollField.setValue(8);
            initSharkField.setText(String.valueOf(5));
            sharkBreedScrollField.setValue(15);
            sharkStarveScrollField.setValue(20);
            wc.reload(40, 40, 50, 8, 5, 15, 20);
        });
        p.add(reset);
        final Button applyNewSettings = new Button("Apply new Settings");
        applyNewSettings.addActionListener(e -> {
            wc.setRunning(false);
            wc.reload(
                    40,
                    40,
                    new Integer(initFishField.getText()),
                    fishBreedScrollField.getValue(),
                    new Integer(initSharkField.getText()),
                    sharkBreedScrollField.getValue(),
                    sharkStarveScrollField.getValue()
            );
        });
        p.add(applyNewSettings);

        Panel p1 = new Panel();
        p1.add(new Label("Initial Fish"));
        initFishField = new TextField(Integer.toString(wc.getFishCount()), 5);
        p1.add(initFishField);
        p.add(p1);

        p1 = new Panel();
        p1.add(new Label("Initial Sharks"));
        initSharkField = new TextField(Integer.toString(wc.getSharkCount()), 5);
        p1.add(initSharkField);
        p.add(p1);

        fishBreedScrollField = new ScrollField("Fish Breed Age", wc.getFishReprTime());
        p.add(fishBreedScrollField);

        sharkBreedScrollField = new ScrollField("Shark Breed Age", wc.getSharkReprTime());
        p.add(sharkBreedScrollField);

        sharkStarveScrollField = new ScrollField("Shark Starve Time", wc.getSharkEnergy());
        p.add(sharkStarveScrollField);
        add(p);
    }
}