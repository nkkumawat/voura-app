package com.voura.nkkumawat;

import com.voura.nkkumawat.Interfaces.SignalingClientInterface;
import com.voura.nkkumawat.Utils.SocketHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import io.socket.client.Socket;

class SignallingClient {
    private static SignallingClient instance;
    private String roomName = null;
    private Socket socket;
    boolean isChannelReady = false;
    boolean isInitiator = false;
    boolean isStarted = false;
    private SignalingClientInterface callback;

    public static SignallingClient getInstance(String room) {
        if (instance == null) {
            instance = new SignallingClient();
        }
        if (instance.roomName == null) {
            instance.roomName = room;
        }
        return instance;
    }

    public void init(SignalingClientInterface signalingInterface) {
        this.callback = signalingInterface;
        try {
            socket = SocketHelper.socket;
            if (!roomName.isEmpty()) {
                createRoom(roomName);
            }
            socket.on("room-created", args -> {
                isInitiator = true;
                callback.onCreatedRoom();
            });
            socket.on("peer-connect", args -> {
                isChannelReady = true;
                callback.onNewPeerJoined();
            });

            socket.on("peer-connected", args -> {
                isChannelReady = true;
                callback.onJoinedRoom();
            });

            socket.on("call-closed", args -> callback.onRemoteHangUp(""));
            socket.on("connection-events", args -> {
                if (args[0] instanceof String) {
                    String data = (String) args[0];
                    if (data.equalsIgnoreCase("setup-event")) {
                        callback.onStartVideo();
                    }
                    if (data.equalsIgnoreCase("close-call")) {
                        callback.onRemoteHangUp(data);
                    }
                } else if (args[0] instanceof JSONObject) {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        String type = data.getString("type");
                        if (type.equalsIgnoreCase("offer")) {
                            callback.onOfferReceived(data);
                        } else if (type.equalsIgnoreCase("answer") && isStarted) {
                            callback.onAnswerReceived(data);
                        } else if (type.equalsIgnoreCase("candidate") && isStarted) {
                            callback.onIceCandidateReceived(data);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createRoom(String message) {
        socket.emit("create-or-join", message);
    }

    public void emitConnectionEvent(String message) {
        socket.emit("connection-events", message);
    }

    public void emitConnectionEvent(SessionDescription message) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("type", message.type.canonicalForm());
            obj.put("sdp", message.description);
            socket.emit("connection-events", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void emitConnectionEvent(IceCandidate iceCandidate) {
        try {
            JSONObject object = new JSONObject();
            object.put("type", "candidate");
            object.put("label", iceCandidate.sdpMLineIndex);
            object.put("id", iceCandidate.sdpMid);
            object.put("candidate", iceCandidate.sdp);
            socket.emit("connection-events", object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        socket.emit("close-call", roomName);
    }
}
