package com.kaiasia.app.register;

import com.kaiasia.app.core.utils.GetErrorUtils;
import ms.apiclient.model.ApiError;
import ms.apiclient.model.ApiRequest;
import ms.apiclient.model.ApiResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class ServiceRegister {
    private final Logger logger = LoggerFactory.getLogger(ServiceRegister.class);

    // Danh sách các method đã đăng ký.
    private static final Map<String, RequestProcessor> processors = new HashMap<>();

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private GetErrorUtils apiErrorUtils;

    /**
     * Phương thức khởi tạo sau khi bean được tạo.
     * Quét toàn bộ packaage 'com.kaiasia.app' và lấy ra các class được ánh dấu bằng annotation KaiService.
     */
    @PostConstruct
    private void init() {
        // Lấy danh sách các class có annotation KaiService
        Set<Class<?>> classesService = new org.reflections.Reflections("com.kaiasia.app").getTypesAnnotatedWith(KaiService.class);
        logger.debug("Found {} classes with annotation SeabService", classesService.size());

        for (Class<?> service : classesService) {
            // Đăng ký các method của class này
            doRegisterMethod(service);
        }
    }

    /**
     * Đăng ký các method có annotation KaiMethod trong class.
     *
     * @param service - Class cần đăng ký các method.
     */
    private void doRegisterMethod(Class service) {
        for (Method m : service.getMethods()) {
            // Kiểm tra xem method có annotation KaiMethod không
            if (m.isAnnotationPresent(KaiMethod.class)) {
                KaiMethod method = m.getAnnotation(KaiMethod.class);
                if (method != null && method.name() != null && method.name().trim().length() > 0) {
                    RequestProcessor processor = new RequestProcessor(ctx, service, m);
                    // Xác định key để lưu vào danh sách processors
                    String key = Register.VALIDATE.equalsIgnoreCase(method.type()) ? Register.VALIDATE_PREFIX + method.name() : method.name(); // VALIDATE_TestService hoặc TestService
                    processors.put(key, processor);
                }
            }
        }
    }

    /**
     * Thực thi method nếu đã đăng ký (Với tình hình hiện tại là nó sẽ tìm mỗi thằng VALIDATE_TestService rồi thực thi)
     *
     * @param authenType - Tên service (UserService,.....).
     * @param request    - Request đầu vào.
     * @return Thông tin lỗi.
     */
    public ApiError processValidate(String authenType, ApiRequest request) {
        String validateAuthenType = Register.VALIDATE_PREFIX + authenType; // VALIDATE_TestService
        ApiError err = new ApiError();
        err.setCode(ApiError.OK_CODE);

        try {
            if (processors.containsKey(validateAuthenType)) {
                // Nếu đã đăng ký thì được phép thực thi
                logger.debug("Execute method validate for AuthenType: {}", validateAuthenType);
                return (ApiError) processors.get(validateAuthenType).execute(request);
            } else {
                // Nếu không có thì không được thực thi
                logger.debug("Dont have method validate for AuthenType: {}", validateAuthenType);
                err.setCode("333");
                err.setDesc("Invalid authenType");
            }
        } catch (Exception ex) {
            logger.error("{}{}", request, ex);
            err = apiErrorUtils.getError("999", new String[]{ExceptionUtils.getRootCauseMessage(ex)});
        }

        return err;
    }

    /**
     * Thực thi method nếu đã đăng ký (Với tình hình hiện tại là nó sẽ tìm mỗi thằng TestService rồi thực thi).
     *
     * @param authenType - Tên service cần thực thi.
     * @param req        - Request đầu vào.
     * @return ApiResponse - Kết quả trả về từ method được thực thi.
     */
    public ApiResponse processAuthenType(String authenType, ApiRequest req) {
        try {
            if (processors.containsKey(authenType)) {
                // Nếu đã đăng ký thì thực thi và trả về kết quả
                logger.debug("Execute method AuthenType: {}", authenType);
                return (ApiResponse) processors.get(authenType).execute(req);
            } else {
                // Nếu không tìm thấy method, trả về lỗi
                logger.error("{}{}-Khong tim thay service voi authenType", req, authenType);
                ApiResponse rs = new ApiResponse();
                ApiError err = new ApiError();
                err.setCode("333");
                err.setDesc("Invalid authenType");
                rs.setError(err);
                return rs;
            }
        } catch (Exception ex) {
            logger.error("{}", ex);
        }

        return null;
    }
}