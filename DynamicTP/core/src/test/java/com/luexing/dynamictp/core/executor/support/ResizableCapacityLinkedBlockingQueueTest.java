/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.executor.support;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ResizableCapacityLinkedBlockingQueue 单元测试
 */
class ResizableCapacityLinkedBlockingQueueTest {

    @Test
    void constructorWithNonPositiveCapacityThrowsIAE() {
        assertThrows(IllegalArgumentException.class, () -> new ResizableCapacityLinkedBlockingQueue<>(0));
        assertThrows(IllegalArgumentException.class, () -> new ResizableCapacityLinkedBlockingQueue<>(-1));
    }

    @Test
    void offerPollPeekFollowsFifoOrder() {
        ResizableCapacityLinkedBlockingQueue<String> queue = new ResizableCapacityLinkedBlockingQueue<>(3);

        assertThat(queue.offer("a")).isTrue();
        assertThat(queue.offer("b")).isTrue();
        assertThat(queue.offer("c")).isTrue();

        assertThat(queue.peek()).isEqualTo("a");
        assertThat(queue.poll()).isEqualTo("a");
        assertThat(queue.peek()).isEqualTo("b");
        assertThat(queue.poll()).isEqualTo("b");
        assertThat(queue.poll()).isEqualTo("c");
        assertThat(queue.poll()).isNull();
    }

    @Test
    void offerReturnsFalseWhenFull() {
        ResizableCapacityLinkedBlockingQueue<Integer> queue = new ResizableCapacityLinkedBlockingQueue<>(2);

        assertThat(queue.offer(1)).isTrue();
        assertThat(queue.offer(2)).isTrue();
        assertThat(queue.offer(3)).isFalse();
        assertThat(queue.size()).isEqualTo(2);
    }

    @Test
    void setCapacityIncreasesCapacityAndAllowsMoreOffers() {
        ResizableCapacityLinkedBlockingQueue<Integer> queue = new ResizableCapacityLinkedBlockingQueue<>(2);

        assertThat(queue.offer(1)).isTrue();
        assertThat(queue.offer(2)).isTrue();
        assertThat(queue.offer(3)).isFalse();

        queue.setCapacity(4);

        assertThat(queue.offer(3)).isTrue();
        assertThat(queue.offer(4)).isTrue();
        assertThat(queue.offer(5)).isFalse();
    }

    @Test
    void remainingCapacityAndSize() {
        ResizableCapacityLinkedBlockingQueue<String> queue = new ResizableCapacityLinkedBlockingQueue<>(5);

        assertThat(queue.remainingCapacity()).isEqualTo(5);
        assertThat(queue.size()).isZero();

        queue.offer("x");
        queue.offer("y");

        assertThat(queue.size()).isEqualTo(2);
        assertThat(queue.remainingCapacity()).isEqualTo(3);
    }

    @Test
    void clearEmptiesQueue() {
        ResizableCapacityLinkedBlockingQueue<String> queue = new ResizableCapacityLinkedBlockingQueue<>(3);
        queue.offer("a");
        queue.offer("b");

        queue.clear();

        assertThat(queue.size()).isZero();
        assertThat(queue.poll()).isNull();
        assertThat(queue.remainingCapacity()).isEqualTo(3);
    }

    @Test
    void removeRemovesMatchingElement() {
        ResizableCapacityLinkedBlockingQueue<String> queue = new ResizableCapacityLinkedBlockingQueue<>(5);
        queue.offer("a");
        queue.offer("b");
        queue.offer("c");

        assertThat(queue.remove("b")).isTrue();
        assertThat(queue.size()).isEqualTo(2);
        assertThat(queue.poll()).isEqualTo("a");
        assertThat(queue.poll()).isEqualTo("c");
        assertThat(queue.remove("missing")).isFalse();
    }

    @Test
    void drainToTransfersElements() {
        ResizableCapacityLinkedBlockingQueue<String> queue = new ResizableCapacityLinkedBlockingQueue<>(5);
        queue.offer("a");
        queue.offer("b");
        queue.offer("c");

        List<String> target = new ArrayList<>();
        int drained = queue.drainTo(target);

        assertThat(drained).isEqualTo(3);
        assertThat(target).containsExactly("a", "b", "c");
        assertThat(queue.size()).isZero();
    }

    @Test
    void drainToWithMaxElementsLimitsTransfer() {
        ResizableCapacityLinkedBlockingQueue<String> queue = new ResizableCapacityLinkedBlockingQueue<>(5);
        queue.offer("a");
        queue.offer("b");
        queue.offer("c");

        List<String> target = new ArrayList<>();
        int drained = queue.drainTo(target, 2);

        assertThat(drained).isEqualTo(2);
        assertThat(target).containsExactly("a", "b");
        assertThat(queue.size()).isEqualTo(1);
    }

    @Test
    void iteratorTraversesElementsInOrder() {
        ResizableCapacityLinkedBlockingQueue<String> queue = new ResizableCapacityLinkedBlockingQueue<>(5);
        queue.offer("a");
        queue.offer("b");
        queue.offer("c");

        List<String> elements = new ArrayList<>();
        Iterator<String> it = queue.iterator();
        while (it.hasNext()) {
            elements.add(it.next());
        }

        assertThat(elements).containsExactly("a", "b", "c");
    }

    @Test
    void nullOfferThrowsNpe() {
        ResizableCapacityLinkedBlockingQueue<String> queue = new ResizableCapacityLinkedBlockingQueue<>(1);

        assertThrows(NullPointerException.class, () -> queue.offer(null));
    }

    @Test
    void collectionConstructorInitializesElements() {
        ResizableCapacityLinkedBlockingQueue<String> queue =
                new ResizableCapacityLinkedBlockingQueue<>(Arrays.asList("x", "y", "z"));

        assertThat(queue.size()).isEqualTo(3);
        assertThat(queue.poll()).isEqualTo("x");
        assertThat(queue.poll()).isEqualTo("y");
        assertThat(queue.poll()).isEqualTo("z");
    }
}
