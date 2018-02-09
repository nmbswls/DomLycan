package com.bean;

import org.springframework.stereotype.Component;

@Component
public class Roles {
    private Integer roomId;
    private String role;
    private Integer playerNumber;
    private String state1;
    private String state2;



    public Roles(){

    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(Integer playerNumber) {
        this.playerNumber = playerNumber;
    }

    public String getState1() {
        return state1;
    }

    public void setState1(String state1) {
        this.state1 = state1;
    }

    public String getState2() {
        return state2;
    }

    public void setState2(String state2) {
        this.state2 = state2;
    }

    public Roles(Integer roomId, String role, Integer playerNumber, String state1, String state2) {
        this.roomId = roomId;
        this.role = role;
        this.playerNumber = playerNumber;
        this.state1 = state1;
        this.state2 = state2;
    }
}
