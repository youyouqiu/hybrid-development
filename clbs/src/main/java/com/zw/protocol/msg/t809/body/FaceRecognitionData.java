package com.zw.protocol.msg.t809.body;

import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.util.ConstantUtil;
import com.zw.protocol.msg.t809.T809MsgBody;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author denghuabing on 2019/10/21 9:47
 * 桂标
 */
@Data
public class FaceRecognitionData implements T809MsgBody {

    private String vehicleNo;

    private Integer vehicleColor;

    private Integer dataType;

    private Integer dataLength;

    private Integer driverLength;

    private String driver;

    private Integer driverNoLength;

    private String driverNo;

    private Integer photoLen;

    private Integer type;

    private byte[] photo;



    /**
     * @param photo
     * @param driverName
     * @param vehicleDTO
     * @param drivingLicenseNo
     * @return
     */
    public static FaceRecognitionData getInstance(byte[] photo, String driverName, VehicleDTO vehicleDTO,
        String drivingLicenseNo) {
        FaceRecognitionData data = new FaceRecognitionData();
        // 上报1241
        data.setVehicleNo(vehicleDTO.getName());
        data.setVehicleColor(vehicleDTO.getPlateColor());
        data.setDataType(ConstantUtil.T809_UP_EXG_MSG_FACE_PHOTO_AUTO);
        int dataLength = 7;
        data.setDriverLength(driverName.length());
        data.setDriver(driverName);
        dataLength += driverName.length();
        if (StringUtils.isNotEmpty(drivingLicenseNo)) {
            // 驾驶证号不为空
            data.setDriverNoLength(drivingLicenseNo.length());
            data.setDriverNo(drivingLicenseNo);
            dataLength += drivingLicenseNo.length();
        } else {
            data.setDriverNoLength(0);
        }
        data.setPhotoLen(photo.length);
        dataLength += photo.length;
        data.setPhoto(photo);
        data.setType(1);
        data.setDataLength(dataLength);
        return data;
    }
}
