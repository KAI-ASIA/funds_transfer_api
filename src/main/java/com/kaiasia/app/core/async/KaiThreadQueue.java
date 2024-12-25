package com.kaiasia.app.core.async;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Lớp KaiThreadQueue là một hàng đợi thread-safe để lưu trữ các nhiệm vụ.
 *
 * @param <T> Kiểu dữ liệu của các nhiệm vụ trong hàng đợi.
 */
public class KaiThreadQueue<T> {

    // Hàng đợi thread-safe để lưu trữ các nhiệm vụ.
    private ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();

    /**
     * Constructor mặc định để khởi tạo hàng đợi.
     */
    public KaiThreadQueue() {
    }

    /**
     * Thêm một nhiệm vụ vào hàng đợi.
     *
     * @param t Nhiệm vụ cần thêm.
     */
    public void add(T t) {
        this.queue.add(t);
    }

    /**
     * Lấy và xóa nhiệm vụ ở đầu hàng đợi.
     *
     * @return Nhiệm vụ ở đầu hàng đợi, hoặc null nếu hàng đợi rỗng.
     */
    public T poll() {
        return this.queue.poll();
    }

    /**
     * Lấy số lượng nhiệm vụ hiện có trong hàng đợi.
     *
     * @return Số lượng nhiệm vụ trong hàng đợi.
     */
    public int size() {
        return this.queue.size();
    }
}
