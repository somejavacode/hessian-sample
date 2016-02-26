package test.api;

import java.io.Serializable;


public class RequestDTO implements Serializable {

    private int int1;
    private String string1;

    public int getInt1() {
        return int1;
    }

    public void setInt1(int int1) {
        this.int1 = int1;
    }

    public String getString1() {
        return string1;
    }

    public void setString1(String string1) {
        this.string1 = string1;
    }
}
