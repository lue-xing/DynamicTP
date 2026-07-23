/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.executor.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ResizableCapacityLinkedBlockingQueueV1Test2 {

    /**
     * 注意：如果想在 IDEA 里跑这个单元测试，需要在 IDEA 单元测试中设置 VM 参数：
     * --add-opens java.base/java.util.concurrent=ALL-UNNAMED
     */
    public static void main(String[] args) throws Exception {
        ResizableCapacityLinkedBlockingQueueV1<String> queue = new ResizableCapacityLinkedBlockingQueueV1<>(10);
        for (int i = 0; i < 8; i++) {
            queue.put("Element "+ i);
            System.out.println("入队列成功，当前大小：" + queue.size());
        }

        // 通过反射修改容量
        try {
            queue.setCapacity(5);
            System.out.println("通过反射修改容量为：5");
        } catch (Exception e) {
            System.out.println("反射修改容量失败：" + e.getMessage());
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                System.out.println("尝试添加 Element 9，队列已满，线程将被阻塞");
                queue.put("Element 9");
                System.out.println("成功添加 Element 9，队列大小：" + queue.size());
            } catch (InterruptedException e) {
                System.out.println("添加 Element 9 失败");
            }
        });

        // 等待 2 秒，确保线程阻塞
        TimeUnit.SECONDS.sleep(2);

        executor.shutdownNow();

        System.out.println("🔍 最终队列元素数量：" + queue.size());
    }
}