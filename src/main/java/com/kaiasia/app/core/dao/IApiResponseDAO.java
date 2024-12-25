package com.kaiasia.app.core.dao;

import com.kaiasia.app.core.model.ApiResponseBean;

import java.util.Date;
import java.util.List;

public interface IApiResponseDAO {
    /**
     * Insert phản hồi từ hệ thống vào db.
     * @param var1 - Phản hồi cần insert.
     * @return Số bản ghi được thực thi thành công.
     * @throws Exception
     */
    int insert(ApiResponseBean var1) throws Exception;

    /**
     * Lấy ra response theo ID.
     * @param var1 - ID của response.
     * @return Response tương ứng.
     * @throws Exception
     */
    ApiResponseBean getRes(String var1) throws Exception;

    /**
     * Lấy danh sách response theo trạng thái và số lượng truyền vào.
     * @param var1 - Trạng thái của response.
     * @param var2 - Số lượng giới hạn.
     * @return Danh sách response tương ứng.
     * @throws Exception
     */
    List<ApiResponseBean> getApiResponseByStatus(String var1, int var2) throws Exception;

    int delete(String var1) throws Exception;

    /**
     * Cập nhật trạng thái của response thành process.
     * @param var1 - Daanh sách các response
     * @param var2 - Thời gian bắt đầu xử lý
     * @throws Exception
     */
    void updateReq2Process(List<String> var1, Date var2) throws Exception;

    /**
     * Dỗi, không muốn xử lý. Cho nó về reject
     * @param var1 - Daanh sách các response
     * @throws Exception
     */
    void updateReq2Reject(List<String> var1) throws Exception;

    /**
     * Cập nhật response sau khi xử lý xong
     * @param var1 - Response xử lý xong
     * @throws Exception
     */
    void updateResAfterProcessed(ApiResponseBean var1) throws Exception;
}
