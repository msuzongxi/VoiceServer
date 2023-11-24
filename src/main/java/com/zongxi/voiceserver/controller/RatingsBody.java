package com.zongxi.voiceserver.controller;

public class RatingsBody {
	
	private String message;
	private String uid;
	
	public String getMesage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	@Override
	public String toString() {
		return "RatingBody [message=" + message + ", uid=" + uid + "]";
	}

}
