package com.zw.platform.basic.dto.imports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.zw.platform.dto.constant.VehicleConstant.ASSIGNMENT_MAX_COUNT;

/**
 * 企业分组下的编号 ——沿用原来的逻辑
 * @author create by zhangjuan on 2020/11/12.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrgGroupNumberDTO {
    /**
     * 下标
     * value = 1, 表示已经被使用
     */
    private int[] groupNumbers;

    /**
     * 分组最大编号 == 最大下标
     */
    private int maxNumber;

    /**
     * 分组最小编号 = 最小下标, 默认从0
     */
    private int minNumber;

    /**
     * 获取下一个分组编号
     * @return 分组编号
     */
    public int nextAssignmentNumber() {
        if (groupNumbers == null || groupNumbers.length == 0) {
            groupNumbers = new int[ASSIGNMENT_MAX_COUNT];
            groupNumbers[0] = 1;
            return 0;
        }

        for (int i = minNumber; i < groupNumbers.length; i++) {
            // 按道理这里每次O(1)就能取到值
            if (groupNumbers[i] == 0) {
                groupNumbers[i] = 1;
                this.minNumber = i;
                this.maxNumber = Math.max(this.minNumber, this.maxNumber);
                return i;
            }
        }
        // 如果数组遍历完了也没有取到可用的编号, 则返回-1
        return -1;
    }
}
