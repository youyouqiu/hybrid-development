package com.zw.platform.util.imports.lock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.zw.platform.dto.constant.VehicleConstant.ASSIGNMENT_MAX_COUNT;

/**
 * 企业分组下的编号
 * @author create by zhouzongbo on 2020/9/10.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupAssignmentNumberDto {
    /**
     * 下标
     * value = 1, 表示已经被使用
     */
    private int[] assignmentNumbers;

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
        if (assignmentNumbers == null || assignmentNumbers.length == 0) {
            assignmentNumbers = new int[ASSIGNMENT_MAX_COUNT];
            assignmentNumbers[0] = 1;
            return 0;
        }

        for (int i = minNumber; i < assignmentNumbers.length; i++) {
            // 按道理这里每次O(1)就能取到值
            if (assignmentNumbers[i] == 0) {
                assignmentNumbers[i] = 1;
                this.minNumber = i;
                this.maxNumber = Math.max(this.minNumber, this.maxNumber);
                return i;
            }
        }
        // 如果数组遍历完了也没有取到可用的编号, 则返回-1
        return -1;
    }
}
