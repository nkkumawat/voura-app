package com.vuora.nkkumawat.Utils;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketHelper {
    public static Socket socket = (Socket) init();

    private static Socket init() {
        try {
            socket = IO.socket(Constants.SERVER_URL);
            socket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return socket;
    }

}
