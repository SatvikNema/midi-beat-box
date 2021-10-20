package Satvik.main;

import java.io.Serializable;
import java.util.Arrays;

class Message implements Serializable {
    private String message;
    private boolean[] checkBoxState;
    public Message(){}

    public static Message builder(){
        return new Message();
    }

    public Message message(String message){
        this.message = message;
        return this;
    }

    public Message checkBoxState(boolean[] checkBoxState){
        this.checkBoxState = checkBoxState;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean[] getCheckBoxState() {
        return checkBoxState;
    }

    public void setCheckBoxState(boolean[] checkBoxState) {
        this.checkBoxState = checkBoxState;
    }

    @Override
    public String toString() {
        return "message='" + message + '\'' +
                ", checkBoxState=" + Arrays.toString(checkBoxState) +
                '}';
    }
}
