package Satvik.main;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BeatBox1 {
    JPanel panel;
    List<JCheckBox> checkBoxList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame frame;
    JList incomingList;
    ObjectOutputStream oos;
    ObjectInputStream ois;
    List<Message> messages = new ArrayList<>();
    JTextField userMessage;

    String[] instrumentNames = {
            "Base Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare",
            "Crash Cymbal", "hand Clap", "High Tom", "High Bongo", "Maracas",
            "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom",
            "High Agogo", "Open High Conga"
    };

    int[] instruments = {45, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static final String SYS_DIR = "/Users/s0n02qm/practise/MidiBeatBox/src/resources/";

    public static void main(String[] args) {

        BeatBox1 beatBox1 = new BeatBox1();
        beatBox1.setupNetworking();
        beatBox1.buildGui();
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

        JButton sendSequenceButton = new JButton("send sequence to all");
        sendSequenceButton.addActionListener(actionEvent -> sendStateToServer());
        buttonBox.add(sendSequenceButton);

        incomingList = new JList();
        incomingList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()){
                    String selected = (String) incomingList.getSelectedValue();
                    chaneSequence(messages.stream().filter(message -> message.getMessage().equalsIgnoreCase(selected)).collect(Collectors.toList()).get(0).getCheckBoxState());
                }
            }
        });
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane theList = new JScrollPane(incomingList);
        buttonBox.add(theList);
        setJListData();

        userMessage = new JTextField();
        buttonBox.add(userMessage);

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

    private void setJListData() {
        String[] messageList = messages.stream().map(Message::getMessage).collect(Collectors.toList()).toArray(new String[messages.size()]);
        incomingList.setListData(messageList);
    }

    private void chaneSequence(boolean[] checkBoxState) {
        for(int i=0;i<256;i++){
            checkBoxList.get(i).setSelected(checkBoxState[i]);
        }
    }

    private void sendStateToServer() {
        try {
            boolean[] arr = new boolean[256];
            for(int i=0;i<256;i++){
                if(checkBoxList.get(i).isSelected()){
                    arr[i] = true;
                }
            }
            Message message = Message.builder().message(userMessage.getText()).checkBoxState(arr);
            oos.writeObject(message);
        } catch (IOException ex) {
            System.out.println("Could not send the message to the server");
            ex.printStackTrace();
        }
    }

    public void setupNetworking(){
        try {
            Socket socket = new Socket("127.0.0.1", 5000);
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("connected to the server");
            new Thread(new RemoteReader()).start();
        }catch(IOException ex) {
            System.out.println("could not connect to the BeatBoxServer");
            ex.printStackTrace();
        }

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

    public class RemoteReader implements Runnable {
        @Override
        public void run(){
            Message message;
            try{
                while((message = (Message) ois.readObject())!=null){
                    messages.add(message);
                    setJListData();
                }
            }catch(IOException | ClassNotFoundException ex) {
                System.out.println("could not read message from the server");
                ex.printStackTrace();
            }
        }
    }

    public static MidiEvent makeEvent(int command, int channel, int one, int two, int tick) {
        MidiEvent event = null;
        try{
            ShortMessage a = new ShortMessage();
            a.setMessage(command, channel, one, two);
            event = new MidiEvent(a, tick);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return event;
    }
}
