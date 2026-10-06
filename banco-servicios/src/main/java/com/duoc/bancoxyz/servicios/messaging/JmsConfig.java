package com.duoc.bancoxyz.servicios.messaging;

import com.duoc.bancoxyz.servicios.messaging.event.CompensarRetiroEvent;
import com.duoc.bancoxyz.servicios.messaging.event.ResultadoCuentaEvent;
import com.duoc.bancoxyz.servicios.messaging.event.RetiroFinalizadoEvent;
import com.duoc.bancoxyz.servicios.messaging.event.RetiroSolicitadoEvent;
import jakarta.jms.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.Map;

/**
 * Configuracion JMS de banco-servicios.
 *
 * Conversor JSON y dos JmsTemplate, uno para colas y otro para el tópico.
 */
@Configuration
public class JmsConfig {

    public static final String PROPIEDAD_TIPO = "_tipo";

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName(PROPIEDAD_TIPO);
        converter.setTypeIdMappings(Map.of(
                "RetiroSolicitado", RetiroSolicitadoEvent.class,
                "ResultadoCuenta", ResultadoCuentaEvent.class,
                "CompensarRetiro", CompensarRetiroEvent.class,
                "RetiroFinalizado", RetiroFinalizadoEvent.class));
        return converter;
    }

    /** Para colas (punto a punto). Es el que se inyecta por defecto. */
    @Bean
    @Primary
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setPubSubDomain(false);
        return template;
    }

    /** Para el topico (publicacion-suscripcion). */
    @Bean
    public JmsTemplate jmsTopicTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setPubSubDomain(true);
        return template;
    }
}
