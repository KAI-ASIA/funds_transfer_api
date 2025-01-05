package com.kaiasia.app.core;

import com.kaiasia.app.core.async.IProcess;
import com.kaiasia.app.core.dao.IAPIDaoManager;
import com.kaiasia.app.core.model.ApiRequestBean;
import ms.apiclient.model.*;
import com.kaiasia.app.core.utils.AppConfigPropertiesUtils;
import com.kaiasia.app.core.utils.GetErrorUtils;
import lombok.Data;
import ms.apiclient.model.ApiBody;
import ms.apiclient.model.ApiError;
import ms.apiclient.model.ApiRequest;
import ms.apiclient.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * Lớp ProcessThread thực hiện xử lý các yêu cầu API trong một luồng riêng biệt.
 * Nó sẽ nhận các yêu cầu, thực hiện xử lý, và trả về phản hồi sau khi hoàn thành.
 * Nếu yêu cầu gặp sự cố như timeout hoặc không thể xử lý, nó sẽ trả về lỗi tương ứng.
 */
@Component
@Scope("prototype")  // Đảm bảo mỗi yêu cầu có một thể hiện riêng của ProcessThread
@Data
public class ProcessThread {

    // Các phụ thuộc tiêm vào lớp ProcessThread
    @Autowired
    private IProcess process;  // Xử lý yêu cầu API
    @Autowired
    ProcessService processService;  // Dịch vụ xử lý yêu cầu
    @Autowired
    private GetErrorUtils apiErrUtils;  // Tiện ích xử lý lỗi API
    @Autowired
    AppConfigPropertiesUtils appConfigUtils;  // Tiện ích cấu hình ứng dụng

    private boolean start = true;  // Biến xác định trạng thái của luồng
    private int threadIndex;  // Chỉ số của luồng
    private final Logger logger = LoggerFactory.getLogger(ProcessThread.class);  // Logger để ghi nhận thông tin và lỗi

    /**
     * Phương thức xử lý yêu cầu API và trả về phản hồi.
     *
     * @param apiRequestBean Đối tượng chứa thông tin yêu cầu.
     */
    public void process(ApiRequestBean apiRequestBean) {
        // Lấy đối tượng ApiRequest từ dịch vụ ProcessService
        ApiRequest apiReq = this.processService.getRequest(apiRequestBean);
        if (apiReq == null) {
            try {
                // Nếu yêu cầu không tồn tại, chờ một chút rồi tiếp tục
                Thread.sleep(500);
            } catch (InterruptedException var11) {
                var11.printStackTrace();
            }
        }

        ApiResponse res = null;

        long b = System.currentTimeMillis();
        // Kiểm tra thời gian yêu cầu đã chờ trong hàng đợi
        int checkTimeInQueue = this.processService.checkReqTimeInQueue(apiRequestBean);
        this.logger.info("checkTimeInQueue: " + (System.currentTimeMillis() - b));

        if (1 == checkTimeInQueue) {  // Nếu yêu cầu bị timeout
            res = new ApiResponse();
            res.setHeader(apiReq.getHeader());
            ApiError err = this.apiErrUtils.getNotProcessErr();
            err.setDesc(err.getDesc() + ". " + "Request wait in queue too long");
            ApiBody body = new ApiBody();
            body.put("status", "FAILE");
            res.setBody(body);
            res.setError(err);
        } else {
            // Tiến hành xử lý yêu cầu
            res = this.process.process(apiReq);

            // Kiểm tra thời gian timeout của yêu cầu
            if (apiRequestBean.getTimeout() > 0L && System.currentTimeMillis() - apiRequestBean.getReceiveTime().getTime() >= apiRequestBean.getTimeout() * 1000L) {
                res.setHeader(apiReq.getHeader());
                ApiError err = this.apiErrUtils.getTimeoutErr();
                res.setError(err);
                ApiBody body = new ApiBody();
                body.put("status", "FAILE");
                res.setBody(body);
            }
        }

        // Tính thời gian xử lý yêu cầu
        long duration = System.currentTimeMillis() - apiRequestBean.getReceiveTime().getTime();
        res.getHeader().setDuration(duration);
        res.getHeader().setReqType("RESPONSE");

        try {
            long e = System.currentTimeMillis();
            // Cập nhật kết quả sau khi xử lý yêu cầu
            this.processService.updateResAfterProcessed(apiRequestBean, res);
            this.logger.info(apiReq.get_reqId() + "#saveResponse#Duration: " + (System.currentTimeMillis() - e));
        } catch (Exception var12) {
            // Ghi nhận lỗi khi không thể lưu kết quả
            this.logger.error("saveResponseToRedis:{}", var12);
        }
    }
}