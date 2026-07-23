/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.executor.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * BlockingQueueTypeEnum 单元测试
 */
class BlockingQueueTypeEnumTest {

    @ParameterizedTest
    @EnumSource(BlockingQueueTypeEnum.class)
    void createBlockingQueueForEachTypeWithCapacity(BlockingQueueTypeEnum type) {
        BlockingQueue<Object> queue = BlockingQueueTypeEnum.createBlockingQueue(type.getName(), 10);

        assertThat(queue).isNotNull();
        switch (type) {
            case ARRAY_BLOCKING_QUEUE -> assertThat(queue).isInstanceOf(ArrayBlockingQueue.class);
            case LINKED_BLOCKING_QUEUE -> assertThat(queue).isInstanceOf(LinkedBlockingQueue.class);
            case LINKED_BLOCKING_DEQUE -> assertThat(queue).isInstanceOf(LinkedBlockingDeque.class);
            case SYNCHRONOUS_QUEUE -> assertThat(queue).isInstanceOf(SynchronousQueue.class);
            case LINKED_TRANSFER_QUEUE -> assertThat(queue).isInstanceOf(LinkedTransferQueue.class);
            case PRIORITY_BLOCKING_QUEUE -> assertThat(queue).isInstanceOf(PriorityBlockingQueue.class);
            case RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE ->
                    assertThat(queue).isInstanceOf(ResizableCapacityLinkedBlockingQueue.class);
        }
    }

    @Test
    void invalidNameThrowsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> BlockingQueueTypeEnum.createBlockingQueue("UnknownQueue", 10));
    }

    @Test
    void nullCapacityUsesOfWithoutCapacityLimit() {
        BlockingQueue<Object> linkedQueue =
                BlockingQueueTypeEnum.createBlockingQueue("LinkedBlockingQueue", null);
        BlockingQueue<Object> resizableQueue =
                BlockingQueueTypeEnum.createBlockingQueue("ResizableCapacityLinkedBlockingQueue", null);

        assertThat(linkedQueue).isInstanceOf(LinkedBlockingQueue.class);
        assertThat(linkedQueue.remainingCapacity()).isEqualTo(Integer.MAX_VALUE);
        assertThat(resizableQueue).isInstanceOf(ResizableCapacityLinkedBlockingQueue.class);
        assertThat(resizableQueue.remainingCapacity()).isEqualTo(Integer.MAX_VALUE);
    }
}
