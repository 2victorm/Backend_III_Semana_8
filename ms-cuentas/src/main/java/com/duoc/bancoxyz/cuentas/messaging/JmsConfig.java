package com.duoc.bancoxyz.cuentas.messaging;

import com.duoc.bancoxyz.cuentas.messaging.event.CompensarRetiroEvent;
import com.duoc.bancoxyz.cuentas.messaging.event.ResultadoCuentaEvent;
import com.duoc.bancoxyz.cuentas.messaging.event.RetiroSolicitadoEvent;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.activemq.autoconfigure.ActiveMQConnectionFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.Map;

/**
 * JSON con nombre logico de tipo y politica de reentrega de ActiveMQ.
 */
@Configuration
public class JmsConfig {

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_tipo");
        converter.setTypeIdMappings(Map.of(
                "RetiroSolicitado", RetiroSolicitadoEvent.class,
                "CompensarRetiro", CompensarRetiroEvent.class,
                "ResultadoCuenta", ResultadoCuentaEvent.class));
        return converter;
    }

    @Bean
    public ActiveMQConnectionFactoryCustomizer politicaDeReentrega(
            @Value("${cuentas.reentrega.maximo:8}") int maximoReentregas) {
        return factory -> {
            RedeliveryPolicy politica = factory.getRedeliveryPolicy();
            politica.setMaximumRedeliveries(maximoReentregas);
            politica.setInitialRedeliveryDelay(1000);
            politica.setUseExponentialBackOff(true);
            politica.setBackOffMultiplier(2);
            politica.setMaximumRedeliveryDelay(30000);
        };
    }
}
