package com.ihomefnt.host;

import org.testng.annotations.Test;

import java.net.*;
import java.util.Objects;

/**
 * Created by chengzhengrong on 2017/6/14.
 */
public class TestD {

    @Test
    public void test1() throws UnknownHostException, SocketException {
        String localIP = "127.0.0.1";
        String localHostName = "local";
        DatagramSocket sock = null;
        InetAddress inetAddress = null;
        try {
            // 首先根据socket来获取本地ip
            InetSocketAddress e = new InetSocketAddress(InetAddress.getByName("1.2.3.4"), 1);
            sock = new DatagramSocket();
            sock.connect(e);
            inetAddress = sock.getLocalAddress();
            System.out.println(inetAddress.isSiteLocalAddress());
            System.out.println(inetAddress.isLoopbackAddress());

            if (inetAddress.isSiteLocalAddress() && !inetAddress.isLoopbackAddress()
                    && !inetAddress.getHostAddress().contains(":") && !Objects.equals(null, "0.0.0.0")) {
                localIP = inetAddress.getHostAddress();
                localHostName = inetAddress.getHostName();
            } else {
                // socket没有获取到，根据NetworkInterface来获取
//                return getLocalIP();
            }
        } catch (Exception e) {
            e.printStackTrace();
//            logger.error("get local ip error",e);
        } finally {
            sock.disconnect();
            sock.close();
        }
        System.out.println(new String[]{localIP,localHostName}.toString());
        System.out.println(localIP+":::"+localHostName);

    }
}
