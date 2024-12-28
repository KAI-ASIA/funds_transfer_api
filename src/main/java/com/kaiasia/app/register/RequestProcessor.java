package com.kaiasia.app.register;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

public class RequestProcessor {

    private final ApplicationContext ctx;
    private final Class clazz;
    private final Method method;

    RequestProcessor(ApplicationContext ctx, Class clazz, Method method) {
        this.ctx = ctx;
        this.clazz = clazz;
        this.method = method;
    }

    /**
     * Thực thi phương thức xử lý yêu cầu với dữ liệu đầu vào.
     *
     * @param dataInput Dữ liệu đầu vào cho phương thức.
     * @return Kết quả trả về từ phương thức xử lý.
     * @throws Exception Nếu xảy ra lỗi khi lấy bean, gọi phương thức, hoặc xử lý yêu cầu.
     */
    public Object execute(Object dataInput) throws Exception {
        Object instance = ctx.getBean(clazz); // Lấy bean từ ApplicationContext dựa trên lớp.
        return method.invoke(instance, dataInput); // Gọi phương thức trên bean với tham số đầu vào.
    }
}