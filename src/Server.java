import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server extends JApplet {

    public void paint(Graphics g) {
        InetAddress local = null;
        try {
            local = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String ip
        if (local != null) {
            ip = local.getHostAddress();
        }
        else
            g.drawString("Hello, world!", 20, 10);
return;

        g.drawString("Hello, world!", 20, 10);

        // Draws a circle on the screen (x=40, y=30).
        g.drawArc(40, 30, 20, 20, 0, 360);

        // Draws a rectangle on the screen (x1=100, y1=100, x2=300,y2=300).
        g.drawRect(100, 100, 300, 300);

        // Draws a square on the screen (x1=100, y1=100, x2=200,y2=200).
        g.drawRect(100, 100, 200, 200);



    }
}
