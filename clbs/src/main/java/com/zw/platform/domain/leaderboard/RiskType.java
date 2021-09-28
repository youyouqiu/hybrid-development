package com.zw.platform.domain.leaderboard;

public enum RiskType {

    TIRED("疑似疲劳"), CRASH("碰撞危险"), EXCEPTION("违规异常"), DISTRACTION("注意力分散"), CLUSTER("组合风险"), INTENSE_DRIVING("激烈驾驶");

    private String type;

    RiskType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

}
