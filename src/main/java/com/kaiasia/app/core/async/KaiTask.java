package com.kaiasia.app.core.async;

/**
 * Lớp KaiTask là một lớp trừu tượng đại diện cho các tác vụ chạy bất đồng bộ.
 * Lớp này được thiết kế để quản lý vòng đời của các tác vụ trong thread pool.
 *
 * @param <T> Kiểu dữ liệu kết quả của tác vụ.
 */
public abstract class KaiTask<T> implements Runnable {
    public KaiTask() {
    }

    /**
     * Phương thức trừu tượng để thực thi logic chính của tác vụ.
     * Các lớp con cần ghi đè phương thức này để định nghĩa hành vi cụ thể.
     */
    public abstract void execute();

    public abstract void sleep(int threads);

    public void run() {
        // Bắt đầu thực thi tác vụ
        KaiThreadPool.beginTask();
        try {
            // Thực hiện logic của tác vụ
            this.execute();
        } catch (Exception var5) {
            // In ngoại lệ nếu có lỗi xảy ra
            var5.printStackTrace();
        } finally {
            // Kết thúc tác vụ và giải phóng tài nguyên
            KaiThreadPool.endTask();
            // Tạm dừng với giá trị mặc định là 200
            this.sleep(200);
        }
    }

    /**
     * Phương thức trừu tượng để lấy kết quả của tác vụ dưới dạng chuỗi.
     * Các lớp con cần ghi đè phương thức này để trả về kết quả cụ thể.
     *
     * @return Kết quả thực thi tác vụ dưới dạng chuỗi.
     */
    public abstract String executeResult();
}
