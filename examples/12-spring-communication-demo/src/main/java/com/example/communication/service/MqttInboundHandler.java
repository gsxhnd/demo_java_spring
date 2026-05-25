package com.example.communication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MqttInboundHandler implements MessageHandler {

    private final MqttMessageStore messageStore;

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
        Object payload = message.getPayload();
        messageStore.recordInbound(topic, payload != null ? payload.toString() : "");
    }
}
