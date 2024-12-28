package com.kaiasia.app.core.async;

/**
 * Lớp trừu tượng KaiThread đại diện cho một luồng thực thi nhiệm vụ với cơ chế hàng đợi và thread pool.
 *
 * @param <T> Kiểu dữ liệu của nhiệm vụ được quản lý bởi luồng.
 */
public abstract class KaiThread<T> implements Runnable {

    // Thread pool để quản lý các luồng thực thi nhiệm vụ.
    private KaiThreadPool threadpool;

    // Hàng đợi chứa các nhiệm vụ chờ xử lý.
    private KaiThreadQueue<T> queue;

    // Trạng thái cho biết luồng có đang chạy hay không.
    private boolean start = true;

    // Trạng thái cho biết tất cả các nhiệm vụ đã hoàn thành hay chưa.
    private boolean isFinish = true;

    /**
     * Constructor khởi tạo KaiThread với thông tin về tên nhóm, kích thước pool và hàng đợi nhiệm vụ.
     *
     * @param threadName Tên của nhóm luồng.
     * @param size Số lượng luồng trong thread pool.
     * @param queue Hàng đợi nhiệm vụ.
     */
    public KaiThread(String threadName, int size, KaiThreadQueue<T> queue) {
        this.queue = queue;
        this.threadpool = new KaiThreadPool(threadName, size);
    }

    /**
     * Phương thức run để thực thi logic chính của luồng.
     * Bao gồm việc xử lý nhiệm vụ từ hàng đợi và sử dụng thread pool để thực thi nhiệm vụ.
     */
    public void run() {
        System.out.println("START Kai_THREAD: " + this.threadpool.getInfo());

        while (true) {
            // Kiểm tra trạng thái luồng.
            while (this.start) {
                if (this.queue.size() == 0) {
                    // Nếu hàng đợi rỗng, ngủ trong 500ms.
                    this.sleep(500L);
                } else {
                    // Đặt trạng thái luồng chưa hoàn thành.
                    this.isFinish = false;

                    // Lấy nhiệm vụ từ hàng đợi.
                    T t = this.queue.poll();

                    if (!this.check4Run(t)) {
                        // Nếu nhiệm vụ chưa sẵn sàng, đưa lại vào hàng đợi và ngủ.
                        this.queue.add(t);
                        this.sleep((long) (this.queue.size() * 1000 + 2000));
                    } else {
                        // Nếu nhiệm vụ sẵn sàng, gửi đến thread pool để xử lý.
                        try {
                            this.threadpool.submit(this.getTask(t));
                        } catch (KaiThreadException var3) {
                            var3.printStackTrace();
                        }
                    }

                    // Đặt trạng thái luồng hoàn thành nhiệm vụ hiện tại.
                    this.isFinish = true;

                    // Chờ cho thread pool xử lý các nhiệm vụ hiện có trước khi tiếp tục.
                    while (this.threadpool.check2Sleep()) {
                        this.sleep(500L);
                    }
                }
            }
            return;
        }
    }

    /**
     * Phương thức trừu tượng để lấy nhiệm vụ từ đối tượng đầu vào.
     *
     * @param t Đối tượng đầu vào.
     * @return Nhiệm vụ cần thực thi.
     */
    public abstract KaiTask<T> getTask(T t);

    /**
     * Phương thức trừu tượng để kiểm tra điều kiện cho phép thực thi nhiệm vụ.
     *
     * @param t Đối tượng đầu vào.
     * @return True nếu nhiệm vụ có thể thực thi, False nếu không.
     */
    public abstract boolean check4Run(T t);

    /**
     * Kiểm tra xem tất cả nhiệm vụ đã hoàn thành hay chưa.
     *
     * @return True nếu tất cả nhiệm vụ hoàn thành, False nếu không.
     */
    public boolean isAllTaskFinish() {
        return this.isFinish && this.queue.size() == 0 && this.threadpool.checkAllTaskFinish();
    }

    /**
     * Dừng luồng và tắt thread pool.
     */
    public void stop() {
        System.out.println("STOP Kai_THREAD: " + this.threadpool.getInfo());
        this.start = false;
        this.threadpool.shutdown();
    }

    /**
     * Lấy thông tin chi tiết về thread pool.
     *
     * @return Chuỗi thông tin thread pool.
     */
    public String getInfo() {
        return this.threadpool.getInfo();
    }

    /**
     * Lấy số lượng nhiệm vụ đang chạy trong thread pool.
     *
     * @return Số lượng nhiệm vụ đang chạy.
     */
    public int taskRunning() {
        return this.threadpool.taskRunning();
    }

    /**
     * Phương thức trợ giúp để luồng ngủ trong một khoảng thời gian.
     *
     * @param ms Thời gian ngủ (mili giây).
     */
    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException var4) {
            var4.printStackTrace();
        }
    }
}
