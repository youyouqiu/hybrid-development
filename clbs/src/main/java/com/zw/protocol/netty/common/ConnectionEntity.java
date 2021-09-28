package com.zw.protocol.netty.common;

import java.io.Serializable;

public class ConnectionEntity implements Serializable{

	private static final long serialVersionUID = 1009830886810906744L;
	private ApplicationEntity applicationEntity;
    private String url;
    private Object connectionHandler;



	public ApplicationEntity getApplicationEntity() {
		return applicationEntity;
	}

	public void setApplicationEntity(ApplicationEntity applicationEntity) {
		this.applicationEntity = applicationEntity;
	}

	public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Object getConnectionHandler() {
        return connectionHandler;
    }

    public void setConnectionHandler(Object connectionHandler) {
        this.connectionHandler = connectionHandler;
    }
    
    public boolean isAvailable() {
        return  connectionHandler != null;
    }
    

}
