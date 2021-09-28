package com.zw.app.domain.activeSecurity;

import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Date;

@Data
public class Risk implements Serializable {

    private static final Logger logger = LogManager.getLogger(Risk.class);
    private static final long serialVersionUID = 7109738529773037715L;

    private String brand;

    private String id;

    private int picFlag;

    private int riskLevel;

    private String riskStatus;

    private String riskType;

    private int videoFlag;

    private Date warningTime;

    private transient byte[] riskId;

}
