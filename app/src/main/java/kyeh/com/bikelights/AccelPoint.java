package kyeh.com.bikelights;

import java.io.Serializable;

/**
 * Created by kyeh on 9/29/14.
 */
public class AccelPoint implements Serializable {

    private static final long serialVersionUID = 1L;

    private long timestamp;
    private double x;
    private double y;
    private double z;

    public AccelPoint(long timestamp, double x, double y, double z) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }
    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }
    public double getZ() {
        return z;
    }
    public void setZ(double z) {
        this.z = z;
    }

    public String toString()
    {
        return "t="+timestamp+", x="+x+", y="+y+", z="+z;
    }
}
