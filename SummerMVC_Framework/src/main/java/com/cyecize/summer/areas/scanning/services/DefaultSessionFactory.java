package com.cyecize.summer.areas.scanning.services;

import com.cyecize.http.HttpSession;
import com.cyecize.ioc.models.ServiceDetails;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.summer.common.extensions.SessionScopeFactory;

import java.util.Hashtable;

public class DefaultSessionFactory implements SessionScopeFactory<Object> {

    private final Hashtable<String, Object> instancesBySession;

    public DefaultSessionFactory() {
        this.instancesBySession = new Hashtable<>();
    }

    @Override
    public Object getInstance(ServiceDetails serviceDetails, HttpSoletRequest request,
                              DependencyContainer dependencyContainer) {
        final HttpSession session = request.getSession();

        if (this.instancesBySession.containsKey(session.getId())) {
            return this.instancesBySession.get(session.getId());
        }

        final Object instance = dependencyContainer.getNewInstance(
            serviceDetails.getServiceType(),
            serviceDetails.getInstanceName()
        );

        this.instancesBySession.put(session.getId(), instance);
        return instance;
    }
}
