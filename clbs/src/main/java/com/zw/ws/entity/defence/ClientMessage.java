package com.zw.ws.entity.defence;

import lombok.Data;

import java.io.Serializable;

@Data
public class ClientMessage implements Serializable{/**
	 * 
	 */
	private static final long serialVersionUID = 476223305472852L;
	
	private int messageType;
	
	private Object data;
}
