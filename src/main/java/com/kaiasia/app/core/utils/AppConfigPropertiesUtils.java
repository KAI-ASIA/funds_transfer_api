package com.kaiasia.app.core.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;

@Component
@Getter
public class AppConfigPropertiesUtils {

    @Autowired
    Environment env; //Environment của Spring chứa các thông tin cấu hình trong application .properties hoặc yaml
    String ip;

    public AppConfigPropertiesUtils() {
    }

    /**
     * Trả về giá trị của một thuộc tính bất kỳ trong application.properties hoặc application.yml bằng khóa (key) cung cấp.
     * @param key - Tên khóa ử dụng để cung cấp thuộc tính
     * @return Giá trị thuộc tính của khóa đầu vào.
     */
    public String getProp(String key) {
        return this.env.getProperty(key);
    }

    /**
     * Trả về tên của api trong application.properties hoặc application.yml bằng khóa (kai.name).
     * @return Giá trị (tên api) của thuộc tính "kai.name".
     */
    public String getApiName() {
        return this.env.getProperty("kai.name");
    }

    /**
     * Trả về khóa API (API key) được cấu hình trong tệp application.properties hoặc application.yml bằng khóa kai.key.
     * @return Giá trị api key của thuộc tính "kai.key".
     */
    public String getApiKey() {
        return this.env.getProperty("kai.key");
    }

    /**
     * Trả về phiên bản API được cấu hình trong tệp application.properties hoặc application.yml.
     * @return Giá trị của thuộc tính "kai.version".
     */
    public String getApiVersion() {
        return this.env.getProperty("kai.version");
    }

    /**
     * Phương thức khởi tạo, được gọi sau khi bean được tạo và các dependency được inject.
     * Thiết lập địa chỉ IP của máy chủ bằng cách lấy địa chỉ IP từ InetAddress.
     */
    @PostConstruct //Dùng để thực hiện các công việc khởi tạo bổ sung.
    public void init() {
        try {
            //Lấy địa chỉ IP của máy chủ đang chạy.
            String ip = InetAddress.getLocalHost().getHostAddress();
            this.ip = ip;
        } catch (Exception var2) {
        }

    }

    /**
     * Trả về cổng của ứng dụng được cấu hình trong tệp application.properties hoặc application.yml.
     * @return Giá trị của thuộc tính "server.port".
     */
    public String getPort() {
        return this.env.getProperty("server.port");
    }
}