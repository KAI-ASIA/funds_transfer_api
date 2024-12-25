package com.kaiasia.app.core.dao;

import com.kaiasia.app.core.model.ApiRequestBean;

import java.util.List;

public interface IApiRequestDAO {
    /**
     * Lấy Request từ db theo ID.
     * @param var1 tham số truyền vào - ID của request.
     * @return Request có ID phù hợp.
     * @throws Exception
     */
    ApiRequestBean getByReqID(String var1) throws Exception;

    /**
     * Insert request vào db.
     * @param var1 - RequestBean được convert từ request đầu vào.
     * @return Số bản ghi được thực thi thành công.
     * @throws Exception
     */
    int insert(ApiRequestBean var1) throws Exception;

    /**
     * Lấy những request chưa dược xử lý (Có status recceive).
     * @param var1 - Số luợng giới hạn.
     * @return Danh sách các request phù hợp.
     * @throws Exception
     */
    List<ApiRequestBean> getReqs(int var1) throws Exception;

    /**
     * Cập nhật trạng thái cho request.
     * @param var1 - ID của request cần cập nhật.
     * @param var2 - Trạng thái cần cập nhật.
     * @return Số bản ghi được thực thi thành công.
     * @throws Exception
     */
    int updateReq(String var1, String var2) throws Exception;

    /**
     * Cập nhật trạng thái cho nhiều request.
     * @param var1 - Danh sách ID các request cần cập nhật.
     * @param var2 - Trạng thái cần cập nhật.
     * @return Số bản ghi được thực thi thành công.
     * @throws Exception
     */
    int updateReqList(List<String> var1, String var2) throws Exception;

    int delete(String var1) throws Exception;
}
