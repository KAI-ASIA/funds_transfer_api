package com.kaiasia.app.core.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.util.UUID;

@Component
public class ReqIDAutoCreate {
    @Autowired
    AppConfigPropertiesUtils appConfig;

    // Tiền tố của request ID, được tạo từ tên API và địa chỉ IP máy chủ.
    private String prefix;

    /**
     * Phương thức khởi tạo tiền tố (prefix) cho mã yêu cầu.
     * Tiền tố này kết hợp tên API và địa chỉ IP của máy chủ.
     *
     * @throws Exception nếu xảy ra lỗi khi lấy địa chỉ IP máy chủ.
     */
    @PostConstruct
    public void init() throws Exception {
        String ip = InetAddress.getLocalHost().getHostAddress();
        String api = this.appConfig.getApiName();
        this.prefix = api + "_" + ip;
    }

    public String createIReqId(long sendTime, long timeout) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        // Combine sendTime, timeout, and UUID
        return sendTime + "000" + "_" + timeout + "000" + "_" + uuid;
    }

    /**
     * Tạo request ID với thông tin thời gian gửi và thời gian chờ.
     *
     * @param sendTime thời gian gửi (tính bằng giây hoặc mili giây, tuỳ cách sử dụng).
     * @param timeout thời gian chờ (timeout) trong cùng đơn vị với sendTime.
     * @return Request ID bao gồm thời gian gửi, thời gian chờ và UUID duy nhất.
     */
    public String createReqId(long sendTime, long timeout) {
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // Combine sendTime, timeout, and UUID
        return sendTime + "000" + "_" + timeout + "000" + "_" + uuid;
    }

    /**
     * Trích xuất thời gian chờ (timeout) từ requestID.
     *
     * @param reqId ID của request đầu vào.
     * @return Thời gian chờ nếu mã hợp lệ, hoặc -1 nếu xảy ra lỗi.
     */
    public long getTimeoutFromReqId(String reqId) {
        try {
            // Request ID đang có dạng: sendTime + "000" + "_" + timeout + "000" + "_" + uuid;
            String[] items = reqId.split("_");
            String tail = items[1];
            String timeout = tail.substring(tail.length() - 3);
            return Long.parseLong(timeout);
        } catch (Exception var5) {
            return -1L;
        }
    }

    /**
     * Trích xuất thời gian gửi (sendTime) từ request ID.
     *
     * @param reqId ID của request đầu vào.
     * @return Thời gian chờ nếu mã hợp lệ, hoặc -1 nếu xảy ra lỗi.
     */
    public long getSendTimeFromReqId(String reqId) {
        try {
            String[] items = reqId.split("_");
            String tail = items[0];
            String sendTime = tail.substring(0, tail.length() - 3);
            return Long.parseLong(sendTime);
        } catch (Exception var5) {
            return -1L;
        }
    }
}