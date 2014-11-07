import java.applet.Applet;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class WaTorApplet extends Applet {
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