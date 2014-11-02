import scala.Tuple2;
import scala.Tuple3;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

class WaTorCanvas extends Canvas {

    public static final int DEFAULT_WIDTH = 40;
    public static final int DEFAULT_HEIGHT = 40;
    public static final int DEFAULT_TIMEOUT = 100;
    public static final int DEFAULT_FISH_COUNT = 50;
    public static final int DEFAULT_SHARK_COUNT = 5;
    public static final int DEFAULT_SHARK_ENERGY = 20;
    public static final int DEFAULT_FISH_REPR_TIME = 8;
    public static final int DEFAULT_SHARK_REPR_TIME = 15;

    public static final Path pathToPhasePortraitCSV = Paths.get("D:\\phasePortrait.csv");
    public static final Path pathToFishPopulationCSV = Paths.get("D:\\fishPopulation.csv");
    public static final Path pathToSharkPopulationCSV = Paths.get("D:\\sharkPopulation.csv");

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
    private int timeout = DEFAULT_TIMEOUT;

    private java.util.List<Integer> sharks = new ArrayList<>();
    private java.util.List<Integer> fishes = new ArrayList<>();

    public WaTorCanvas(int width, int height, int fishCount, int fishReprTime, int sharkCount, int sharkReprTime, int sharkEnergy) {
        this.width = width;
        this.height = height;
        this.fishCount = fishCount;
        this.fishReprTime = fishReprTime;
        this.sharkCount = sharkCount;
        this.sharkReprTime = sharkReprTime;
        this.sharkEnergy = sharkEnergy;
        world = new World(new Tuple2<>(height, width), new Tuple2<>(fishCount, fishReprTime), new Tuple3<>(sharkCount, sharkReprTime, sharkEnergy));
        sharks.add(world.countSharks());
        fishes.add(world.countFishes());
    }

    public WaTorCanvas(){
        this(
            DEFAULT_WIDTH,
            DEFAULT_HEIGHT,
            DEFAULT_FISH_COUNT,
            DEFAULT_FISH_REPR_TIME,
            DEFAULT_SHARK_COUNT,
            DEFAULT_SHARK_REPR_TIME,
            DEFAULT_SHARK_ENERGY
        );
    }

    /**
     * re-initialize applet with new parameters
     */
    public void reload(int width, int height, int fishCount, int fishReprTime, int sharkCount, int sharkReprTime, int sharkEnergy){
        fishes.clear();
        sharks.clear();
        this.width = width;
        this.height = height;
        world = new World(new Tuple2<>(height, width), new Tuple2<>(fishCount, fishReprTime), new Tuple3<>(sharkCount, sharkReprTime, sharkEnergy));
    }

    /**
     * re-initialize applet with default parameters
     */
    public void reload(){
        sharks.clear();
        fishes.clear();
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
        this.timeout = DEFAULT_TIMEOUT;
        world = new World(
                new Tuple2<>(DEFAULT_WIDTH, DEFAULT_HEIGHT),
                new Tuple2<>(DEFAULT_FISH_COUNT, DEFAULT_FISH_REPR_TIME),
                new Tuple3<>(DEFAULT_SHARK_COUNT, DEFAULT_SHARK_REPR_TIME, DEFAULT_SHARK_ENERGY)
        );
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
            Thread.sleep(timeout);
        } catch (InterruptedException ignored){}
        world.forward(step++);
        sharks.add(world.countSharks());
        fishes.add(world.countFishes());
        repaint();
        paintOcean(g);
    }

    /**
     * show all ocean on each moment of time
     * green square - fish
     * red square - shark
     * blue square - empty
     */
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

    public int getTimeout() {
        return timeout;
    }

    public String setTimeout(String timeout) {
        try{
            this.timeout = Integer.valueOf(timeout);
        } catch (Exception e){
            this.timeout = DEFAULT_TIMEOUT;
        }
        return String.valueOf(this.timeout);
    }

    /**
     * saves data from each moment of time into files
     */
    public void saveData() {
        processData(pathToPhasePortraitCSV, toCSVLine(createPhasePortrait()));
        processData(pathToFishPopulationCSV, toCSVLine(createPopulation(fishes)));
        processData(pathToSharkPopulationCSV, toCSVLine(createPopulation(sharks)));
    }

    private void processData(final Path path, final Collection<String> data){
        try {
            Files.deleteIfExists(path);
            Files.createFile(path);
            Files.write(path, data);
        } catch (IOException ignored) {}
    }

    private Collection<String> toCSVLine(final Collection<Tuple2<Integer, Integer>> lines){
        final String line0 = "" + lines.stream().map(t -> t._1().toString()).collect(Collectors.joining(","));
        final String line1 = lines.stream().map(t -> t._2().toString()).collect(Collectors.joining(","));
        return Arrays.asList(line0, line1);
    }

    private List<Tuple2<Integer, Integer>> createPhasePortrait(){
        final List<Tuple2<Integer, Integer>> list = new ArrayList<>();
        for (int i = 0; i < sharks.size(); i++) {
            list.add(new Tuple2<>(sharks.get(i), fishes.get(i)));
        }
        return list;
    }

    private List<Tuple2<Integer, Integer>> createPopulation(final List<Integer> counts){
        final List<Tuple2<Integer, Integer>> list = new ArrayList<>();
        for (int i = 0; i < sharks.size(); i++) {
            list.add(new Tuple2<>(i, counts.get(i)));
        }
        return list;
    }
}

public class WaTor extends Applet {
    private WaTorCanvas wc;
    private TextField timeoutField;
    private TextField initFishField;
    private TextField initSharkField;
    private TextField fishBreedScrollField;
    private TextField sharkBreedScrollField;
    private TextField sharkStarveScrollField;

    /**
     * initialize applet
     */
    public void init() {
        setLayout(new FlowLayout());
        setSize(650, 475);

        wc = new WaTorCanvas();
        wc.setSize(400, 400);
        add(wc);

        final Panel p = new Panel();
        p.setLayout(new GridLayout(11, 1, 3, 3));
        final Button start = new Button("Start");
        start.addActionListener(e -> {
            wc.setRunning(true);
            timeoutField.setText(wc.setTimeout(timeoutField.getText()));
        });
        p.add(start);
        final Button stop = new Button("Stop");
        stop.addActionListener(e -> wc.setRunning(false));
        p.add(stop);
        final Button reset = new Button("Reset");
        reset.addActionListener(e -> {
            wc.setRunning(false);
            wc.reload();
            initFishField.setText(String.valueOf(wc.getFishCount()));
            fishBreedScrollField.setText(String.valueOf(wc.getFishReprTime()));
            initSharkField.setText(String.valueOf(wc.getSharkCount()));
            sharkBreedScrollField.setText(String.valueOf(wc.getSharkReprTime()));
            sharkStarveScrollField.setText(String.valueOf(wc.getSharkEnergy()));
            timeoutField.setText(String.valueOf(wc.getTimeout()));
        });
        p.add(reset);
        final Button applyNewSettings = new Button("Apply new Settings");
        applyNewSettings.addActionListener(e -> {
            wc.setRunning(false);
            wc.reload(
                    WaTorCanvas.DEFAULT_WIDTH,
                    WaTorCanvas.DEFAULT_HEIGHT,
                    new Integer(initFishField.getText()),
                    new Integer(fishBreedScrollField.getText()),
                    new Integer(initSharkField.getText()),
                    new Integer(sharkBreedScrollField.getText()),
                    new Integer(sharkStarveScrollField.getText())
            );
            timeoutField.setText(wc.setTimeout(timeoutField.getText()));
        });
        p.add(applyNewSettings);

        final Button save = new Button("Save Data");
        save.addActionListener(e -> wc.saveData());
        p.add(save);

        final Panel fish = new Panel();
        fish.add(new Label("Initial Fish"));
        initFishField = new TextField(Integer.toString(wc.getFishCount()), 5);
        fish.add(initFishField);
        p.add(fish);

        final Panel shark = new Panel();
        shark.add(new Label("Initial Sharks"));
        initSharkField = new TextField(Integer.toString(wc.getSharkCount()), 5);
        shark.add(initSharkField);
        p.add(shark);

        final Panel timeout = new Panel();
        timeout.add(new Label("Repaint Timeout"));
        timeoutField = new TextField(Integer.toString(wc.getTimeout()), 5);
        timeout.add(timeoutField);
        timeoutField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    timeoutField.setText(wc.setTimeout(timeoutField.getText()));
                }
            }
        });
        p.add(timeout);

        final Panel fba = new Panel();
        fba.add(new Label("Fish Breed Age"));
        fishBreedScrollField = new TextField(Integer.toString(wc.getFishReprTime()), 5);
        fba.add(fishBreedScrollField);
        p.add(fba);

        final Panel sba = new Panel();
        sba.add(new Label("Shark Breed Age"));
        sharkBreedScrollField = new TextField(Integer.toString(wc.getSharkReprTime()), 5);
        sba.add(sharkBreedScrollField);
        p.add(sba);

        final Panel sst = new Panel();
        sst.add(new Label("Shark Starve Time"));
        sharkStarveScrollField = new TextField(Integer.toString(wc.getSharkEnergy()), 5);
        sst.add(sharkStarveScrollField);
        p.add(sst);
        add(p);
    }
}