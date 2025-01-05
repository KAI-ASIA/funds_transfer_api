package com.kaiasia.app.core;

import com.kaiasia.app.core.async.IProcess;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.register.ServiceRegister;
import ms.apiclient.model.ApiRequest;
import ms.apiclient.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.kaiasia.app.core.utils.GetErrorUtils;

/**
 * Lớp ProcessImpl thực hiện xử lý yêu cầu API và trả về phản hồi.
 * Nó thực hiện các bước xác thực, xử lý dữ liệu, và trả về kết quả dưới dạng ApiResponse.
 */
@Component
public class ProcessImpl implements IProcess {
    // Logger để ghi nhận các sự kiện và lỗi trong quá trình xử lý
    private final Logger logger = LoggerFactory.getLogger(ProcessImpl.class);

    // Dịch vụ đăng ký được tiêm vào để xử lý các tác vụ liên quan đến xác thực
    @Autowired
    private ServiceRegister serviceRegister;

    // Tiện ích để xử lý lỗi trong quá trình API
    @Autowired
    private GetErrorUtils apiErrUtils;

    // Các giá trị cấu hình được tiêm từ application.yaml
    @Value("${kai.name}")
    private String api;

    @Value("${kai.key}")
    private String apiKey;

    /**
     * Phương thức xử lý yêu cầu API và trả về phản hồi.
     *
     * @param req Đối tượng ApiRequest chứa thông tin yêu cầu.
     * @return ApiResponse đối tượng phản hồi chứa kết quả hoặc lỗi.
     */
    @Override
    public ApiResponse process(ApiRequest req) {
        // Ghi nhận thông điệp nhận được
        logger.debug("received msg: "+req);

        try {
            // Xử lý xác thực và yêu cầu thông qua ServiceRegister
            ApiResponse dataResponse = serviceRegister.processAuthenType(Register.getAuthenType(req), req);

            // Cập nhật các thông tin tiêu đề trong yêu cầu
            req.getHeader().setApi(api);
            req.getHeader().setApiKey(apiKey);

            // Cập nhật thông tin tiêu đề vào phản hồi
            dataResponse.setHeader(req.getHeader());

            return dataResponse;

        } catch (Exception e) {
            // Xử lý lỗi khi có exception xảy ra
            ApiResponse dataResponse = new ApiResponse();
            logger.error("{}", e);  // Ghi nhận lỗi vào log
            dataResponse.setHeader(req.getHeader());
            // Cung cấp thông tin lỗi qua tiện ích GetErrorUtils
            dataResponse.setError(apiErrUtils.getError("999", new String[] {e.toString()}));
            return dataResponse;
        }
    }
}