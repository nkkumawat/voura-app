package com.vuora.nkkumawat.Interfaces;

import org.json.JSONObject;

public interface SignalingClientInterface {
    void onStartVideo();
    void onCreatedRoom();
    void onJoinedRoom();
    void onNewPeerJoined();
    void onRemoteHangUp(String msg);
    void onOfferReceived(JSONObject data);
    void onAnswerReceived(JSONObject data);
    void onIceCandidateReceived(JSONObject data);
}
