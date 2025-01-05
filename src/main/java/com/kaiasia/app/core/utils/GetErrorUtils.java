package com.kaiasia.app.core.utils;

import ms.apiclient.model.ApiError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
public class GetErrorUtils {

    @Autowired
    AppConfigPropertiesUtils appConfig;

    /**
     * Lấy đối tượng ApiError dựa trên mã lỗi.
     *
     * @param code mã lỗi cần lấy mô tả.
     * @return đối tượng ApiError chứa mã lỗi và mô tả tương ứng.
     */
    public ApiError getError(String code) {
        //Lấy mô tả lỗi dựa trên code truyền vào.
        String desc = this.appConfig.getProp("kai.error.code" + code);
        return new ApiError(code, desc);
    }

    /**
     * Lấy đối tượng ApiError dựa trên mã lỗi và các tham số định dạng.
     * Nếu có tham số, chúng sẽ được chèn vào mô tả lỗi.
     *
     * @param code   mã lỗi cần lấy mô tả.
     * @param params các tham số tùy chọn để định dạng mô tả lỗi.
     * @return đối tượng ApiError chứa mã lỗi và mô tả đã được định dạng.
     */
    public ApiError getError(String code, String[] params) {
        String desc = this.appConfig.getProp("kai.error.code" + code);
        if (params != null) {
            MessageFormat formater = new MessageFormat(desc);
            desc = formater.format(params);
        }
        return new ApiError(code, desc);
    }

    /**
     * Lấy lỗi mặc định cho trường hợp "Không xử lý được" (mã lỗi "997").
     *
     * @return đối tượng ApiError cho lỗi "Không xử lý được".
     */
    public ApiError getNotProcessErr() {
        return this.getError("997");
    }

    /**
     * Lấy lỗi mặc định cho trường hợp "Timeout" (mã lỗi "998").
     *
     * @return đối tượng ApiError cho lỗi "Timeout".
     */
    public ApiError getTimeoutErr() {
        return this.getError("998");
    }
}