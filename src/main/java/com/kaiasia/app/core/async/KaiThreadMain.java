package com.kaiasia.app.core.async;

/**
 * Lớp trừu tượng KaiThreadMain chịu trách nhiệm quản lý luồng và hàng đợi nhiệm vụ.
 *
 * @param <T> Kiểu dữ liệu của các nhiệm vụ được quản lý bởi lớp này.
 */
public abstract class KaiThreadMain<T> {

    // Đối tượng KaiThread để quản lý các luồng thực thi nhiệm vụ.
    private KaiThread<T> KaiThread;

    // Hàng đợi chứa các nhiệm vụ chờ xử lý.
    private KaiThreadQueue<T> queue;

    // Biến trạng thái để xác định luồng đang hoạt động hay không.
    private boolean active = true;

    // Kích thước của luồng (số lượng luồng trong pool).
    private int size;

    // Tên của nhóm luồng (thread pool).
    private String threadName;

    /**
     * Phương thức trừu tượng được sử dụng để lấy đối tượng KaiTask từ kiểu dữ liệu đầu vào.
     *
     * @param t Dữ liệu đầu vào.
     * @return Nhiệm vụ KaiTask được khởi tạo từ dữ liệu đầu vào.
     */
    public abstract KaiTask<T> getKaiTask(T t);

    /**
     * Phương thức trừu tượng kiểm tra điều kiện để thực thi nhiệm vụ.
     *
     * @param t Dữ liệu đầu vào.
     * @return True nếu nhiệm vụ có thể chạy, false nếu không.
     */
    public abstract boolean checkToRun(T t);

    /**
     * Lấy tên của nhóm luồng.
     *
     * @return Tên nhóm luồng.
     */
    public String getThreadName() {
        return this.threadName;
    }

    /**
     * Cài đặt tên cho nhóm luồng.
     *
     * @param threadName Tên nhóm luồng.
     */
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    /**
     * Kiểm tra xem nhóm luồng có đang hoạt động hay không.
     *
     * @return True nếu đang hoạt động, false nếu không.
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * Cài đặt trạng thái hoạt động cho nhóm luồng.
     *
     * @param active Trạng thái hoạt động.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Lấy kích thước của nhóm luồng.
     *
     * @return Số lượng luồng trong nhóm.
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Cài đặt kích thước của nhóm luồng.
     *
     * @param size Số lượng luồng trong nhóm.
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Khởi tạo một đối tượng KaiThreadMain với tên và kích thước được chỉ định.
     *
     * @param threadName Tên của nhóm luồng.
     * @param size Kích thước của nhóm luồng.
     */
    public KaiThreadMain(String threadName, int size) {
        this.size = size;
    }

    /**
     * Khởi tạo nhóm luồng và hàng đợi.
     */
    public void init() {
        this.queue = new KaiThreadQueue<>();
        this.KaiThread = new KaiThread<T>(this.threadName, this.size, this.queue) {

            @Override
            public KaiTask<T> getTask(T t) {
                return KaiThreadMain.this.getKaiTask(t);
            }

            @Override
            public boolean check4Run(T t) {
                return KaiThreadMain.this.checkToRun(t);
            }
        };
        Thread thread = new Thread(this.KaiThread);
        thread.start(); // Bắt đầu chạy nhóm luồng.
    }

    /**
     * Thêm nhiệm vụ mới vào hàng đợi.
     *
     * @param t Nhiệm vụ cần thêm.
     */
    public void addQueue(T t) {
        this.queue.add(t);
    }

    /**
     * Lấy số lượng nhiệm vụ đang chờ trong hàng đợi.
     *
     * @return Số lượng nhiệm vụ trong hàng đợi.
     */
    public int getTaskInQueue() {
        return this.queue.size();
    }

    /**
     * Kiểm tra xem tất cả nhiệm vụ có đã hoàn thành hay không.
     *
     * @return True nếu tất cả nhiệm vụ đã hoàn thành, false nếu còn nhiệm vụ đang chạy.
     */
    public boolean isAllTaskFinish() {
        return this.KaiThread.isAllTaskFinish();
    }

    /**
     * Lấy thông tin về trạng thái của nhóm luồng.
     *
     * @return Chuỗi thông tin chi tiết.
     */
    public String getInfo() {
        return this.KaiThread.getInfo();
    }

    /**
     * Dừng toàn bộ nhóm luồng.
     */
    public void shutdown() {
        this.KaiThread.stop();
    }

    /**
     * Lấy số lượng nhiệm vụ đang chạy.
     *
     * @return Số lượng nhiệm vụ đang chạy.
     */
    public int getActive() {
        return this.KaiThread.taskRunning();
    }
}
