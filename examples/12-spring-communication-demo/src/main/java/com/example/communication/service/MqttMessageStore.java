package com.example.communication.service;

import com.example.communication.dto.MqttMessageRecord;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class MqttMessageStore {

    private static final int MAX_SIZE = 100;

    private final Deque<MqttMessageRecord> inbound = new ConcurrentLinkedDeque<>();
    private final Deque<MqttMessageRecord> outbound = new ConcurrentLinkedDeque<>();
    private volatile boolean brokerReachable;

    public void recordInbound(String topic, String payload) {
        brokerReachable = true;
        add(inbound, new MqttMessageRecord(topic, payload, "INBOUND", Instant.now()));
    }

    public void recordOutbound(String topic, String payload) {
        add(outbound, new MqttMessageRecord(topic, payload, "OUTBOUND", Instant.now()));
    }

    public List<MqttMessageRecord> recentInbound(int limit) {
        return tail(inbound, limit);
    }

    public List<MqttMessageRecord> recentOutbound(int limit) {
        return tail(outbound, limit);
    }

    public boolean isBrokerReachable() {
        return brokerReachable;
    }

    public void markBrokerReachable() {
        brokerReachable = true;
    }

    public void markBrokerUnreachable() {
        brokerReachable = false;
    }

    private void add(Deque<MqttMessageRecord> deque, MqttMessageRecord record) {
        deque.addFirst(record);
        while (deque.size() > MAX_SIZE) {
            deque.removeLast();
        }
    }

    private List<MqttMessageRecord> tail(Deque<MqttMessageRecord> deque, int limit) {
        List<MqttMessageRecord> result = new ArrayList<>();
        int count = 0;
        for (MqttMessageRecord record : deque) {
            result.add(record);
            count++;
            if (count >= limit) {
                break;
            }
        }
        return result;
    }
}
