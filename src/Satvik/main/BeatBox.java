package Satvik.main;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static Satvik.MidiSoundGenerator.makeEvent;

public class BeatBox {
    JPanel panel;
    List<JCheckBox> checkBoxList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame frame;

    String[] instrumentNames = {
            "Base Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare",
            "Crash Cymbal", "hand Clap", "High Tom", "High Bongo", "Maracas",
            "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom",
            "High Agogo", "Open High Conga"
    };

    int[] instruments = {45, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static final String SYS_DIR = "/Users/s0n02qm/practise/MidiBeatBox/src/resources/";

    public static void main(String[] args) {
        new BeatBox().buildGui();
    }

    private void buildGui() {
        frame = new JFrame("Cyber BeatBox");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton startButton = new JButton("Start");
        startButton.addActionListener(actionEvent -> buildTrackAndStart());
        buttonBox.add(startButton);

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(actionEvent -> sequencer.stop());
        buttonBox.add(stopButton);

        JButton tempoUpButton = new JButton("Tempo up");
        tempoUpButton.addActionListener(actionEvent -> {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor*1.03));
        });
        buttonBox.add(tempoUpButton);

        JButton tempDownButton = new JButton("Temp down");
        tempDownButton.addActionListener(actionEvent -> {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor*0.97));
        });
        buttonBox.add(tempDownButton);

        JButton saveSequenceButton = new JButton("Save sequence");
        saveSequenceButton.addActionListener(actionEvent -> {
            saveSequenceToFile(checkBoxList, new File(SYS_DIR+"mySequence.ser"));
            System.out.println("Sequence saved!");
        });
        buttonBox.add(saveSequenceButton);

        JButton loadSequenceButton = new JButton("Load sequence");
        loadSequenceButton.addActionListener(actionEvent -> {
            loadSequenceFromFile(checkBoxList, new File(SYS_DIR+"mySequence.ser"));
            System.out.println("Sequence loaded from "+SYS_DIR);
        });
        buttonBox.add(loadSequenceButton);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for(int i=0;i<16;i++){
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        frame.getContentPane().add(background);

        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(1);
        grid.setHgap(2);

        panel = new JPanel(grid);

        background.add(BorderLayout.CENTER, panel);

        checkBoxList = new ArrayList<>();
        for(int i=0;i<256;i++){
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(false);
            checkBoxList.add(checkBox);
            panel.add(checkBox);
        }

        setupMidi();

        frame.setBounds(50, 50, 300, 300);
        frame.pack();
        frame.setVisible(true);

    }

    private void loadSequenceFromFile(List<JCheckBox> checkBoxList, File file) {
        boolean[] arr = null;
        try{
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            arr = (boolean[]) ois.readObject();
        }catch(IOException | ClassNotFoundException e){
            System.out.println("Couldn't load the sequence from "+file.getName()+". "+e.getMessage());
        }

        for(int i=0;i<256;i++){
            if(arr[i]){
                checkBoxList.get(i).setSelected(true);
            } else {
                checkBoxList.get(i).setSelected(false);
            }
        }
        sequencer.stop();
        buildTrackAndStart();
    }

    private static void saveSequenceToFile(List<JCheckBox> checkBoxList, File file) {
        boolean[] arr = new boolean[256];
        for(int i=0;i<256;i++){
            if(checkBoxList.get(i).isSelected()){
                arr[i] = true;
            }
        }
        try{
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream((fos));
            oos.writeObject(arr);
        } catch(IOException e){
            System.out.println("Couldnt save the sequence. "+e.getMessage());
        }
    }

    private void buildTrackAndStart() {
        int[] trackList = null;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for(int i=0;i<16;i++){
            trackList = new int[16];
            int key = instruments[i];
            for(int j=0;j<16;j++){
                JCheckBox checkBox = checkBoxList.get(16*i + j);
                if(checkBox.isSelected()){
                    trackList[j] = key;
                } else {
                    trackList[j] = 0;
                }
            }
            mackTracks(trackList);
            track.add(makeEvent(176, 1, 127, 0, 16));
        }

        track.add(makeEvent(192, 9, 1, 0, 15));
        try{
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    private void mackTracks(int[] trackList) {
        for(int i=0;i<16;i++){
            int key = trackList[i];
            if(key != 0){
                track.add(makeEvent(144, 9, key, 100, i));
            }
        }
    }

    private void setupMidi() {
        try{
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (MidiUnavailableException | InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }
}
