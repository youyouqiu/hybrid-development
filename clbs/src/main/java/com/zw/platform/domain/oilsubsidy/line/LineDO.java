package com.zw.platform.domain.oilsubsidy.line;

import java.util.UUID;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author wanxing
 * @Title: 线路实体类-与表一一对应
 * @date 2020/10/911:16
 */
@Data
@Accessors(chain = true)
public class LineDO {

    private String id = UUID.randomUUID().toString();

    /**
     * 对接码组织ID
     */
    @NotNull(message = "对接码组织不能为空")
    private String dockingCodeOrgId;
    /**
     * 线路标识
     */
    @NotNull(message = "线路标识不能为空")
    @Length(max = 30, message = "线路标识最多30个字符，15个汉字")
    private String identify;
    /**
     * 线路名称
     */
    @NotNull(message = "线路名称不能为空")
    @Length(max = 20, message = "线路名称最多20个字符，10个汉字")
    private String name;

    /**
     * 线路类型
     */
    @Min(value = 0, message = "线路类型最小为0")
    @Max(value = 9, message = "线路类型最大为9")
    @NotNull(message = "线路类型不能为空")
    private Integer lineType;

    /**
     * 描述
     */
    @Size(max = 100, message = "描述最多100个字符，50个汉字")
    private String remark;

    private Byte flag;

    /**
     * DTO 转 DO
     * @param
     * @return
     */
    public LineDO copyDto2DO() {
        LineDO lineDO = new LineDO();
        lineDO.setId(this.getId()).setDockingCodeOrgId(this.getDockingCodeOrgId())
            .setIdentify(this.getIdentify()).setLineType(this.getLineType())
            .setName(this.getName()).setRemark(this.getRemark());
        return lineDO;
    }

    /**
     *  DO 转 DTO
     * @param
     * @return
     */
    public LineDTO copyDo2DTO() {
        LineDTO lineDTO = new LineDTO();
        lineDTO.setId(this.getId()).setDockingCodeOrgId(this.getDockingCodeOrgId())
            .setIdentify(this.getIdentify()).setLineType(this.getLineType())
            .setName(this.getName()).setRemark(this.getRemark());
        return lineDTO;
    }

}
