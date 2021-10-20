package Satvik.main.teststuff;

import javax.sound.midi.*;
import javax.swing.*;

public class MidiSoundGenerator {

    static JFrame f = new JFrame("My First Music Video");
    static RectangleGenerator ml;

    public void go(){
        setUpGui();
        try{
            Sequencer sequencer = MidiSystem.getSequencer();
            sequencer.open();

            int[] eventsWeWant = {127};
            sequencer.addControllerEventListener(ml, eventsWeWant);

            Sequence seq = new Sequence(Sequence.PPQ, 4);
            Track track = seq.createTrack();
            for(int i=0;i<60;i+=4) {
                int r = (int) ((Math.random() * 50) + 1);
                track.add(makeEvent(144, 1, i, 100, i));
                track.add(makeEvent(176, 1, 127, 0, i));
                track.add(makeEvent(128, 1, i, 100, i+2));
            }
            sequencer.setSequence(seq);
            sequencer.setTempoInBPM(120);
            sequencer.start();
        } catch(Exception e){
            System.out.println("Exception ================");
            System.out.println(e.getMessage());
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

    public void setUpGui() {
        ml = new RectangleGenerator();
        f.setContentPane(ml);
        f.setBounds(30,30, 300,300);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
