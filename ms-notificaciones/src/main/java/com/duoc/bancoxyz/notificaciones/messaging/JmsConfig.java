package com.duoc.bancoxyz.notificaciones.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.Map;

/**
 * Mismo formato JSON que el resto del sistema. Los listeners escuchan un
 * TOPICO porque spring.jms.pub-sub-domain=true (ver config-repo).
 */
@Configuration
public class JmsConfig {

    public static final String TOPICO_RETIROS_EVENTOS = "banco.retiros.eventos";

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_tipo");
        converter.setTypeIdMappings(Map.of("RetiroFinalizado", RetiroFinalizadoEvent.class));
        return converter;
    }
}
