package com.zongxi.voiceserver.controller;

public class VoiceBody {
	private String message;
	private String rid;
	
	public String getMesage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getRid() {
		return rid;
	}
	public void setRid(String rid) {
		this.rid = rid;
	}
	@Override
	public String toString() {
		return "VoiceBody [message=" + message + ", rid=" + rid + "]";
	}
}
