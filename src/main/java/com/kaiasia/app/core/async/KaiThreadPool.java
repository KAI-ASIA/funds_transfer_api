package com.kaiasia.app.core.async;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Lớp KaiThreadPool quản lý một ThreadPoolExecutor, cung cấp các phương thức để xử lý và giám sát các tác vụ bất đồng bộ.
 */
public class KaiThreadPool {
    private ThreadPoolExecutor threadPoolExecutor;
    private String threadpoolName;
    private int poolSize;
    private static int active;

    /**
     * Khởi tạo một đối tượng KaiThreadPool với tên và kích thước pool.
     *
     * @param threadpoolName Tên của thread pool.
     * @param poolSize       Số lượng thread trong pool.
     */
    public KaiThreadPool(String threadpoolName, int poolSize) {
        this.threadPoolExecutor = new ThreadPoolExecutor(poolSize, poolSize, 500L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(poolSize * 3));
        this.poolSize = poolSize;
        this.threadpoolName = threadpoolName;
    }

    public void submit(KaiTask runable) throws KaiThreadException {
        try {
            this.threadPoolExecutor.execute(runable);
        } catch (RejectedExecutionException var3) {
            throw new KaiThreadException(this.threadpoolName, var3);
        }
    }

    /**
     * Kiểm tra xem tất cả các tác vụ đã hoàn thành hay chưa.
     *
     * @return true nếu không còn tác vụ nào trong hàng đợi và không có tác vụ đang hoạt động.
     */
    public boolean checkAllTaskFinish() {
        return this.threadPoolExecutor.getQueue().size() == 0 && this.threadPoolExecutor.getActiveCount() == 0 && active == 0;
    }

    /**
     * Kiểm tra trạng thái có cần đưa thread pool vào chế độ "ngủ" không.
     *
     * @return true nếu hàng đợi hoặc số lượng thread đang hoạt động đạt giới hạn.
     */
    public boolean checkSleep() {
        return this.threadPoolExecutor.getQueue().size() >= this.poolSize * 2 || this.threadPoolExecutor.getActiveCount() == this.poolSize;
    }

    /**
     * Kiểm tra trạng thái nếu số lượng thread đang hoạt động đạt tối đa.
     *
     * @return true nếu thread pool đã sử dụng hết tất cả các thread.
     */
    public boolean check2Sleep() {
        return this.threadPoolExecutor.getActiveCount() == this.poolSize;
    }

    /**
     * Lấy thông tin chi tiết về trạng thái của thread pool.
     *
     * @return Chuỗi chứa thông tin về thread pool.
     */
    public String getInfo() {
        StringBuilder builder = (new StringBuilder("\n******************************************")).append("\n*\tName: ").append(this.threadpoolName)
                                                                                                   .append("\n*\tQueueSize: ").append(this.threadPoolExecutor.getQueue().size())
                                                                                                   .append("\n*\tActiveCount: ").append(this.threadPoolExecutor.getActiveCount())
                                                                                                   .append("\n*\tCompletedTaskCount: ").append(this.threadPoolExecutor.getCompletedTaskCount())
                                                                                                   .append("\n*\tTaskCount: ").append(this.threadPoolExecutor.getTaskCount())
                                                                                                   .append("\n*\tLargestPoolSize: ") .append(this.threadPoolExecutor.getLargestPoolSize())
                                                                                                   .append("\n*\tIsShutdown: ") .append(this.threadPoolExecutor.isShutdown())
                                                                                                   .append("\n*\tPoolSize: ").append(this.threadPoolExecutor.getPoolSize())
                                                                                                   .append("\n*\tCorePoolSize: ").append(this.threadPoolExecutor.getCorePoolSize())
                                                                                                   .append("\n*\tactive: ").append(active)
                                                                                                   .append("\n******************************************");
        return builder.toString();
    }

    /**
     * Lấy số lượng tác vụ đang chạy.
     *
     * @return Số lượng tác vụ đang hoạt động.
     */
    public int taskRunning() {
        return active;
    }

    /**
     * Lấy thông tin chi tiết về trạng thái của thread pool dưới dạng HTML.
     *
     * @return Chuỗi HTML chứa thông tin về thread pool.
     */
    public String getHTMLInfo() {
        StringBuilder builder = (new StringBuilder("<br/>******************************************")).append("<br/>*\tName: ").append(this.threadpoolName)
                                                                                                      .append("<br/>*\tQueueSize: ").append(this.threadPoolExecutor.getQueue().size())
                                                                                                      .append("<br/>*\tActiveCount: ").append(this.threadPoolExecutor.getActiveCount())
                                                                                                      .append("<br/>*\tCompletedTaskCount: ").append(this.threadPoolExecutor.getCompletedTaskCount())
                                                                                                      .append("<br/>*\tTaskCount: ").append(this.threadPoolExecutor.getTaskCount())
                                                                                                      .append("<br/>*\tLargestPoolSize: ").append(this.threadPoolExecutor.getLargestPoolSize())
                                                                                                      .append("<br/>*\tIsShutdown: ").append(this.threadPoolExecutor.isShutdown())
                                                                                                      .append("<br/>*\tactive: ").append(active)
                                                                                                      .append("<br/>******************************************");
        return builder.toString();
    }

    /**
     * Đóng thread pool, không cho nhận thêm tác vụ mới.
     */
    public void shutdown() {
        try {
            this.threadPoolExecutor.shutdown();
        } catch (Exception var2) {
            var2.printStackTrace();
        }
    }

    /**
     * Tăng số lượng tác vụ đang hoạt động (synchronized để đảm bảo thread an toàn).
     */
    protected static synchronized void beginTask() {
        ++active;
    }

    /**
     * Giảm số lượng tác vụ đang hoạt động và trả về giá trị hiện tại.
     *
     * @return Số lượng tác vụ còn lại đang hoạt động.
     */
    protected static synchronized int endTask() {
        --active;
        return active;
    }
}
