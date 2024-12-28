package com.kaiasia.app.core.dao;

import com.kaiasia.app.core.model.ApiRequestBean;
import com.kaiasia.app.core.utils.ApiConstant;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.util.HashMap;
import java.util.List;

@Data
public class ApiRequestDAO extends CommonDAO implements IApiRequestDAO{

    @Autowired
    private PosgrestDAOHelper posgrestDAOHelper;

    /**
     * Lấy Request từ db theo ID.
     * @param var1 tham số truyền vào - ID của request.
     * @return Request có ID phù hợp.
     * @throws Exception
     */
    @Override
    public ApiRequestBean getByReqID(String var1) throws Exception {
        ApiRequestBean result = null;
        String sql = "SELECT * from " + this.getTableName() + " where req_id=:REQ_ID";
        HashMap<String, Object> paramMap = new HashMap();
        paramMap.put("REQ_ID", var1);
        result = posgrestDAOHelper.querySingle(sql, paramMap, new BeanPropertyRowMapper(ApiRequestBean.class));
        return result;
    }

    /**
     * Insert request vào db.
     * @param apiReq - RequestBean được convert từ request đầu vào.
     * @return Số bản ghi được thực thi thành công.
     * @throws Exception
     */
    @Override
    public int insert(ApiRequestBean apiReq) throws Exception {
        String sql = "INSERT INTO " + this.getTableName() + "(req_id,  priority, receive_time, request_msg, request_api, request_node, status, timeout, authen_type) VALUES (:REQ_ID, :PRIORITY, :RECEIVE_TIME, :REQUEST_MSG, :REQUEST_API, :REQUEST_NODE, :STATUS, :TIMEOUT, :AUTHEN_TYPE)";
        HashMap<String, Object> param = new HashMap();
        param.put("REQ_ID", apiReq.getReqId());
        param.put("REQUEST_MSG", apiReq.getRequestMsg());
        param.put("RECEIVE_TIME", apiReq.getReceiveTime());
        param.put("STATUS", apiReq.getStatus());
        param.put("REQUEST_API", apiReq.getRequestAPI());
        param.put("REQUEST_NODE", apiReq.getRequestNode());
        param.put("PRIORITY", apiReq.getPriority());
        param.put("TIMEOUT", apiReq.getTimeout());
        param.put("AUTHEN_TYPE", apiReq.getAuthenType());
        int result = posgrestDAOHelper.update(sql, param);
        return result;
    }

    /**
     * Lấy những request chưa dược xử lý (Có status recceive) theo số lượng truyền vào.
     * @param limit - Số luợng giới hạn.
     * @return Danh sách các request phù hợp.
     * @throws Exception
     */
    @Override
    public List<ApiRequestBean> getReqs(int limit) throws Exception {
        String sql = "SELECT  * from " + this.getTableName() + " where  status=:STATUS order by  priority asc, id  limit " + limit + " for update skip locked";
        HashMap<String, Object> param = new HashMap();
        param.put("STATUS", ApiConstant.STATUS.RECEIVE);
        List<ApiRequestBean> result = posgrestDAOHelper.query(sql, param, new BeanPropertyRowMapper(ApiRequestBean.class));
        return result;
    }

    /**
     * Cập nhật trạng thái cho request.
     * @param reqId - ID của request cần cập nhật.
     * @param status - Trạng thái cần cập nhật.
     * @return Số bản ghi được thực thi thành công.
     * @throws Exception
     */
    @Override
    public int updateReq(String reqId, String status) throws Exception {
        String sql = "UPDATE " + this.getTableName() + " set status = :STATUS where req_id = :ID";
        HashMap<String, Object> param = new HashMap();
        param.put("STATUS", status);
        param.put("ID", reqId);
        int result = posgrestDAOHelper.update(sql, param);
        return result;
    }

    /**
     * Cập nhật trạng thái cho nhiều request.
     * @param ids - Danh sách ID các request cần cập nhật.
     * @param status - Trạng thái cần cập nhật.
     * @return Số bản ghi được thực thi thành công.
     * @throws Exception
     */
    @Override
    public int updateReqList(List<String> ids, String status) throws Exception {
        String sql = "UPDATE " + this.getTableName() + " set status = :STATUS where req_id in (:IDS)";
        HashMap<String, Object> param = new HashMap();
        param.put("STATUS", status);
        param.put("IDS", ids);
        int result = posgrestDAOHelper.update(sql, param);
        return result;
    }

    @Override
    public int delete(String requestId) throws Exception {
        String sql = "DELETE FROM " + this.getTableName() + " WHERE req_id = :REQ_ID";
        HashMap<String, Object> paramMap = new HashMap();
        paramMap.put("REQ_ID", requestId);
        return posgrestDAOHelper.update(sql, paramMap);
    }
}
