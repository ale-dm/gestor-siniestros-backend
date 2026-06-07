package com.portfolio.siniestros.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Ejecuta una acción en una transacción nueva (REQUIRES_NEW) reintentando
 * cuando se produce una violación de unicidad. Sirve para generar números
 * secuenciales (póliza/siniestro) de forma segura frente a concurrencia:
 * si dos peticiones generan el mismo número, la que pierde la carrera
 * recalcula y vuelve a intentarlo.
 */
@Component
@RequiredArgsConstructor
public class TransaccionRetry {

    private final PlatformTransactionManager txManager;

    public <T> T enNuevaTransaccionConReintento(int maxIntentos, Supplier<T> accion) {
        TransactionTemplate tpl = new TransactionTemplate(txManager);
        tpl.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        DataIntegrityViolationException ultimoError = null;
        for (int intento = 0; intento < maxIntentos; intento++) {
            try {
                return tpl.execute(status -> accion.get());
            } catch (DataIntegrityViolationException e) {
                ultimoError = e;
            }
        }
        throw ultimoError;
    }
}
