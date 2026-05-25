package com.example.communication.service;

import com.example.communication.config.MqttProperties;
import com.example.communication.dto.MqttMessageRecord;
import com.example.communication.dto.MqttPublishRequest;
import com.example.communication.dto.MqttStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.mqtt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MqttService {

    private final MqttProperties properties;
    private final MessageChannel mqttOutboundChannel;
    private final MqttMessageStore messageStore;

    public MqttStatusResponse status() {
        return MqttStatusResponse.builder()
                .enabled(true)
                .brokerUrl(properties.getBrokerUrl())
                .clientId(properties.getClientId())
                .subscribeTopics(properties.getSubscribeTopics())
                .connected(messageStore.isBrokerReachable())
                .build();
    }

    public void publish(MqttPublishRequest request) {
        String topic = request.getTopic();
        if (topic == null || topic.isBlank()) {
            topic = properties.getDefaultTopicPrefix() + "/command/" + request.getDeviceId();
        }
        String payload = request.getPayload();
        mqttOutboundChannel.send(MessageBuilder.withPayload(payload)
                .setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, request.getQos())
                .build());
        messageStore.recordOutbound(topic, payload);
        messageStore.markBrokerReachable();
        log.debug("MQTT published topic={} payload={}", topic, payload);
    }

    public List<MqttMessageRecord> recentMessages(int limit) {
        return messageStore.recentInbound(limit);
    }
}
