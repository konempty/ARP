import javax.swing.*;
import java.net.*;
import java.util.Random;

public class Client extends JApplet {
    private int[] address;
    private byte[] macAddress;
    public static final int port = 46810;
    DatagramPacket dp;
    DatagramSocket ds = null;
    String data;
    JLabel jl;
    @Override
    public void init() {
        InetAddress ip_info;
        int i;
        address = new int[4];
        jl = new JLabel("여기");
        add(jl);
        try {
            ip_info = Inet4Address.getLocalHost();
            int prefix = 32;
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(ip_info);
            macAddress = networkInterface.getHardwareAddress();

            for (InterfaceAddress add : networkInterface.getInterfaceAddresses()) {
                if (prefix > add.getNetworkPrefixLength())
                    prefix = add.getNetworkPrefixLength();
            }
            String ip = ip_info.getHostAddress();
            System.out.println(ip);
            String ip2[] = ip.split("\\.");
            for (i = 0; i < 4; i++) {
                address[i] = Integer.parseInt(ip2[i]);
            }
            jl.setText(String.format("%d.%d.%d.%d",address[0],address[1],address[2],address[3]));
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte message[] = new byte[1000];
        dp = new DatagramPacket(message, message.length);
        new Thread(() -> {
            int i1, j, tmp;


            try {
                ds = new DatagramSocket(port);
                int[] server_addr = new int[4];
                while (true) {
                    ds.receive(dp);

                    data = new String(dp.getData(), 0, dp.getLength());
                    if (testCRC32(data)) {
                        if (data.charAt(239) == '1') {
                            String dest_addr = data.substring(368, 400), server_Mac;
                            for (i1 = 0; i1 < 4; i1++) {
                                tmp = 0;
                                for (j = 0; j < 8; j++) {
                                    tmp <<= 1;
                                    tmp += dest_addr.charAt(i1 * 8 + j) - '0';
                                }
                                if (tmp != address[i1])
                                    break;
                            }
                            jl.setText("1234");
                            if (i1 == 4) {
                                jl.setText(server_Mac = data.substring(240, 288));
                                dest_addr = data.substring(288, 320);
                                for (i1 = 0; i1 < 4; i1++) {
                                    server_addr[i1] = 0;
                                    for (j = 0; j < 8; j++) {
                                        server_addr[i1] <<= 1;
                                        server_addr[i1] += dest_addr.charAt(i1 * 8 + j) - '0';
                                    }
                                }
                                sendARP(server_addr, server_Mac);
                            }
                            //jl.setText(dest_addr);
                            System.out.println(data);
                            // serverIp = dp.getAddress().getHostAddress();
                            //new SearchDevice(serverIp, port).start();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            ds.close();
        }).start();
    }

    private void sendARP(int[] ip, String Mac) {
        try {
            DatagramSocket ds = new DatagramSocket();  //굳이 prot를 지정할 필요없음.
            StringBuilder msg = new StringBuilder(), tmp;
            int i, j;
            for (i = 0; i < 7; i++)
                msg.append("10101010"); //Preamble
            msg.append("10101011");//SFD
            msg.append(Mac); //Destination address
            for (byte b : macAddress) {
                tmp = new StringBuilder();
                for (j = 0; j < 8; j++) {
                    tmp.append(((b >> j) & 1));
                }
                msg.append(tmp.reverse()); //Source address
            }
            msg.append("0000100000000110");
            msg.append(MakeARP_Packet(ip, Mac));
            msg.append(MakeCRC32(msg));
            Random random = new Random();
            StringBuilder target_addr = new StringBuilder();
            for (int b : ip)
                target_addr.append(String.valueOf(b)).append(".");
            target_addr.deleteCharAt(target_addr.lastIndexOf("."));
            InetAddress broadAddress = InetAddress.getByName(target_addr.toString());
            DatagramPacket data;
            switch (random.nextInt(10)) {
                case 0:
                    break;
                case 1:
                    int rand_index = random.nextInt(msg.length());
                    if (msg.charAt(rand_index) == '0')
                        msg.replace(rand_index, rand_index + 1, "1");
                    else
                        msg.replace(rand_index, rand_index + 1, "0");
                    data = new DatagramPacket(msg.toString().getBytes(), msg.toString().getBytes().length, broadAddress, 46810);
                    ds.send(data);
                    break;
                default:
                    data = new DatagramPacket(msg.toString().getBytes(), msg.toString().getBytes().length, broadAddress, 46810);
                    ds.send(data);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private StringBuilder MakeARP_Packet(int[] ip, String Mac) {
        StringBuilder data = new StringBuilder("0000000000000001"), tmp; //Hardware Type
        int j;
        data.append("0000100000000000"); //protocol Type
        data.append("00000110"); //Hardware length
        data.append("00000100"); //Protocol length
        data.append("0000000000000010"); //Operation
        for (byte b : macAddress) {
            tmp = new StringBuilder();
            for (j = 0; j < 8; j++) {
                tmp.append(((b >> j) & 1));
            }
            data.append(tmp.reverse()); //Sender Mac address
        }

        for (int i : address) {
            tmp = new StringBuilder();
            for (j = 0; j < 8; j++) {
                tmp.append(((i >> j) & 1));
            }
            data.append(tmp.reverse()); //Sender protocol address
        }
        data.append(Mac);
        for (int b : ip) {
            tmp = new StringBuilder();
            for (j = 0; j < 8; j++) {
                tmp.append(((b >> j) & 1));
            }
            data.append(tmp.reverse()); //Source address
        }
        return data;
    }

    private static StringBuilder MakeCRC32(final StringBuilder data) {
        StringBuilder temp = new StringBuilder(data).append("00000000000000000000000000000000"), CRC32_div = new StringBuilder("100000100110000010001110110110111");
        int i, j, len1 = temp.length() - CRC32_div.length(), len2 = CRC32_div.length();

        for (i = 0; i <= len1; i++) {
            if (temp.charAt(i) == '1') {
                for (j = 0; j < len2; j++) {
                    if (temp.charAt(i + j) == CRC32_div.charAt(j))
                        temp.deleteCharAt(i + j).insert(i + j, '0');
                    else
                        temp.deleteCharAt(i + j).insert(i + j, '1');
                }
            }
        }
        while (temp.charAt(0) == '0')
            temp.deleteCharAt(0);
        for (j = temp.length(); j < 32; j++)
            temp.insert(0, "0");
        return temp;
    }

    private static boolean testCRC32(String data) {
        StringBuilder temp = new StringBuilder(data), CRC32_div = new StringBuilder("100000100110000010001110110110111");
        int i, j, len1 = data.length() - CRC32_div.length(), len2 = CRC32_div.length();
        for (i = 0; i <= len1; i++) {
            if (temp.charAt(i) == '1') {
                for (j = 0; j < len2; j++) {
                    if (temp.charAt(i + j) == CRC32_div.charAt(j))
                        temp.deleteCharAt(i + j).insert(i + j, '0');
                    else
                        temp.deleteCharAt(i + j).insert(i + j, '1');
                }
            }
        }
        len1 = temp.length();
        for (i = 0; i < len1; i++) {
            if (temp.charAt(i) == '1')
                return false;
        }
        return true;
    }
}
