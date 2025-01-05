package com.kaiasia.app.core.dao;

import com.kaiasia.app.core.model.ApiRequestBean;
import com.kaiasia.app.core.model.ApiResponseBean;

import java.util.List;

public interface IAPIDaoManager {
    /**
     * Insert request và response khi client mới gửi về và được validate thành công.
     * @param var1 - Request cần insert.
     * @param var2 - Response cần insert.
     * @throws Exception
     */
    void insertNewApiRequest(ApiRequestBean var1, ApiResponseBean var2) throws Exception;

    /**
     * Lấy request từ db theo số luợng yêu cầu đồng thời cập nhật trạng thái phù hợp cho các request(PROCESSING, ERROR) và response(PROCESS, REJECT).
     * @param var1 - Số lượng request cần lấy.
     * @return Danh sách request lấy ra.
     * @throws Exception
     */
    List<ApiRequestBean> fetchAPIReqs(int var1) throws Exception;

    /**
     * Cập nhật trạng thái cho request và response sau khi yêu cầu từ client được xử lý.
     * @param var1 - trung chuyển dữ liệu
     * @throws Exception
     */
    void updateResAfterProcessed(ApiResponseBean var1) throws Exception;

    /**
     * Lấy response theo ID.
     * @param reqId - ID của response
     * @return Response tương ứng.
     * @throws Exception
     */
    ApiResponseBean getResponse(String reqId) throws Exception;
}
