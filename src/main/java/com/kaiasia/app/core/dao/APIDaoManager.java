package com.kaiasia.app.core.dao;

import com.kaiasia.app.core.model.ApiRequestBean;
import com.kaiasia.app.core.model.ApiResponseBean;
import com.kaiasia.app.core.utils.ApiConstant;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Data
public class APIDaoManager implements IAPIDaoManager{

    private final Logger logger = LoggerFactory.getLogger(APIDaoManager.class);

    private IApiRequestDAO apiRequestDao;
    private IApiResponseDAO apiResponseDAO;

    /**
     * Insert request và response khi client mới gửi về và được validate thành công.
     * @param apiReq - Request cần insert.
     * @param apiRes - Response cần insert.
     * @throws Exception
     */
    @Override
    @Transactional
    public void insertNewApiRequest(ApiRequestBean apiReq, ApiResponseBean apiRes) throws Exception {
        String LOCATION = "InsertAPIReqs()" + apiReq.getReqId();
        logger.info(LOCATION + "#BEGIN");
        this.apiRequestDao.insert(apiReq);
        this.apiResponseDAO.insert(apiRes);
        logger.info(LOCATION + "#END");
    }

    /**
     * Lấy request từ db theo số luợng yêu cầu đồng thời cập nhật trạng thái phù hợp cho các request(PROCESSING, ERROR) và response(PROCESS, REJECT).
     * @param limit - Số lượng request cần lấy.
     * @return Danh sách request lấy ra.
     * @throws Exception
     */
    @Override
    @Transactional
    public List<ApiRequestBean> fetchAPIReqs(int limit) throws Exception {
        List<ApiRequestBean> listReqs = this.apiRequestDao.getReqs(limit);
        List<ApiRequestBean> result = new ArrayList();
        if (listReqs != null && listReqs.size() > 0) {
            //Lưu trữ danh sách id các request bình thường.
            List<String> ids = new ArrayList();

            //Lưu trữ danh sách id các request bị lỗi SLA (timeout).
            List<String> ids_sla_error = new ArrayList();

            Date currentDate = new Date();
            long currentMs = System.currentTimeMillis();
            Iterator var10 = listReqs.iterator();

            while(var10.hasNext()) {
                ApiRequestBean apiRequestBean = (ApiRequestBean)var10.next();
                //Check timeout
                if (currentMs - apiRequestBean.getReceiveTime().getTime() >= (long)(3000 * apiRequestBean.getTimeout())) {
                    ids_sla_error.add(apiRequestBean.getReqId());
                } else {
//                    apiRequestBean.setStatus("PROCESSING");
                    apiRequestBean.setStatus(ApiConstant.STATUS.PROCESSING);
                    ids.add(apiRequestBean.getReqId());
                    result.add(apiRequestBean);
                }
            }

            // Nếu request ổn định, chuyển status của request trong db về processing, response trong db về process
            if (ids.size() > 0) {
//                this.apiRequestDao.updateReqList(ids, "PROCESSING");
                this.apiRequestDao.updateReqList(ids, ApiConstant.STATUS.PROCESSING);
                this.apiResponseDAO.updateReq2Process(ids, currentDate);
            }

            // Nếu request bị timeout, chuyển status của request trong db về error, response trong db về reject
            if (ids_sla_error.size() > 0) {
//                this.apiRequestDao.updateReqList(ids_sla_error, "ERROR");
                this.apiRequestDao.updateReqList(ids_sla_error, ApiConstant.STATUS.ERROR);
                this.apiResponseDAO.updateReq2Reject(ids_sla_error);
            }
        }
        return result;
    }

    /**
     * Cập nhật trạng thái cho request và response sau khi yêu cầu từ client được xử lý.
     * @param apiRes - trung chuyển dữ liệu.
     * @throws Exception
     */
    @Override
    public void updateResAfterProcessed(ApiResponseBean apiRes) throws Exception {
        this.apiResponseDAO.updateResAfterProcessed(apiRes);
        this.apiRequestDao.updateReq(apiRes.getReqId(), apiRes.getStatus());
    }

    /**
     * Lấy response theo ID.
     * @param reqId - ID của response
     * @return Response tương ứng.
     * @throws Exception
     */
    @Override
    public ApiResponseBean getResponse(String reqId) throws Exception{
        return this.apiResponseDAO.getRes(reqId);
    }
}
