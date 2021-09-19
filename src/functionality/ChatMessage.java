package functionality;

import java.io.*;
import java.util.Date;

public class ChatMessage implements Serializable {

    protected static final long serialVersionUID = 1112122200L;

    private boolean isMine;
    private String message;
    private Date date;

    public ChatMessage(String message, Date date, boolean isMine){
        this.message=message;
        this.date=date;
        this.isMine=isMine;
    }

    public String getMessage(){
        return message;
    }

    public Date getDate(){
        return date;
    }

    public boolean getIsMine(){
        return isMine;
    }
}