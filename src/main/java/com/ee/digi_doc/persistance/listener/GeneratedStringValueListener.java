package com.ee.digi_doc.persistance.listener;

import com.ee.digi_doc.persistance.annotation.GeneratedStringValue;
import com.ee.digi_doc.persistance.model.DigiDocSequence;
import org.hibernate.StatelessSession;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManagerFactory;
import java.lang.reflect.Field;

@Component
public class GeneratedStringValueListener implements PreInsertEventListener {

    private static final String VALUE_TEMPLATE = "%s_%s.%s";

    private final SessionFactoryImpl sessionFactory;

    public GeneratedStringValueListener(EntityManagerFactory entityManagerFactory) {
        sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
        registry.getEventListenerGroup(EventType.PRE_INSERT).appendListener(this);
    }

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        setGeneratedStringValue(event.getEntity(), event.getState(), event.getPersister().getPropertyNames());
        return false;
    }

    private void setGeneratedStringValue(Object entity, Object[] state, String[] propertyNames) {
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(GeneratedStringValue.class)) {
                GeneratedStringValue fieldAnnotation = field.getAnnotation(GeneratedStringValue.class);
                String prefix = fieldAnnotation.prefix();
                String extension = fieldAnnotation.extension();

                String value = String.format(VALUE_TEMPLATE, prefix, getSequenceNumber(entity.getClass()), extension);

                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field, entity, value);

                setPropertyState(state, propertyNames, field.getName(), value);
            }
        }
    }

    private Integer getSequenceNumber(Class<?> p_className) {
        boolean insert = false;

        StatelessSession session = sessionFactory.openStatelessSession();

        session.beginTransaction();

        DigiDocSequence sequence = (DigiDocSequence) session.get(DigiDocSequence.class, p_className);

        if (sequence == null) {
            sequence = DigiDocSequence.of(p_className);
            insert = true;
        }

        sequence.setNextValue(sequence.getNextValue() + sequence.getIncrementValue());

        if (insert) {
            session.insert(sequence);
        } else {
            session.update(sequence);
        }

        session.getTransaction().commit();

        session.close();

        return sequence.getNextValue();
    }


    private void setPropertyState(Object[] propertyStates, String[] propertyNames, String propertyName,
                                  Object propertyState) {
        for (int i = 0; i < propertyNames.length; i++) {
            if (propertyName.equals(propertyNames[i])) {
                propertyStates[i] = propertyState;
                return;
            }
        }
    }

}
