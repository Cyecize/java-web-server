package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.ServiceLoadException;
import com.cyecize.summer.common.annotations.Service;
import com.cyecize.summer.common.enums.ServiceLifeSpan;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class DependencyContainerImpl implements DependencyContainer {

    private Set<Object> allServicesAndBeans;

    private Map<ServiceLifeSpan, List<Class<?>>> cachedClassesByLifeSpan;

    public DependencyContainerImpl() {
        this.allServicesAndBeans = new HashSet<>();
        this.cachedClassesByLifeSpan = new HashMap<>();
    }

    @Override
    public void addServices(Set<Object> services) {
        this.allServicesAndBeans.addAll(services);
    }

    @Override
    public void reloadServices(ServiceLifeSpan lifeSpan) {
        List<Class<?>> classesToReload = this.getOrCacheClassesByLifespan(lifeSpan);
        this.allServicesAndBeans = this.allServicesAndBeans.stream()
                .filter((service) -> !this.isServiceToBeReloaded(service.getClass(), lifeSpan))
                .collect(Collectors.toSet());
        try {
            for (Class<?> clsToReload : classesToReload) {
                this.loadService(clsToReload, lifeSpan);
            }
        } catch (ServiceLoadException ex) {
            //this should not be reached since services have been
            // successfully initialized before in the ServiceLoadingService
            ex.printStackTrace();
        }
        System.gc();
    }

    @Override
    public Object reloadController(Object controller) {
        try {
            Class controllerClass = controller.getClass();
            Constructor<?> constructor = controllerClass.getConstructors()[0];
            if (constructor.getParameterCount() < 1) {
                return constructor.newInstance();
            }
            Object[] paramInstances = new Object[constructor.getParameterCount()];
            Class<?>[] paramTypes = constructor.getExceptionTypes();
            for (int i = 0; i < paramTypes.length; i++) {
                paramInstances[i] = this.findAssignableService(paramTypes[i], null);
            }
            return constructor.newInstance(paramInstances);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | ServiceLoadException e) {
            //should not be reached
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<Object> getServicesAndBeans() {
        return this.allServicesAndBeans;
    }

    private Object loadService(Class<?> serviceClass, ServiceLifeSpan serviceLifeSpan) throws ServiceLoadException {
        Object alreadyLoadedService = this.findAlreadyLoadedService(serviceClass);
        if (alreadyLoadedService != null) {
            return alreadyLoadedService;
        }
        Constructor<?> constructor = serviceClass.getConstructors()[0];
        if (constructor.getParameterCount() < 1) {
            return this.instantiateService(constructor);
        }
        Object[] paramInstances = new Object[constructor.getParameterCount()];
        Class<?>[] paramTypes = constructor.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            paramInstances[i] = this.findAssignableService(paramTypes[i], serviceLifeSpan);
        }
        return this.instantiateService(constructor, paramInstances);
    }

    private Object findAssignableService(Class<?> param, ServiceLifeSpan lifeSpan) throws ServiceLoadException {
        for (Object service : this.allServicesAndBeans) {
            if (param.isAssignableFrom(service.getClass())) {
                return service;
            }
        }
        for (Class<?> cls : this.cachedClassesByLifeSpan.get(lifeSpan)) {
            if (param.isAssignableFrom(cls)) {
                return this.loadService(cls, lifeSpan);
            }
        }
        throw new ServiceLoadException(String.format("Could not find dependency for \"%s\" (this should not be reached!)", param.getName()));
    }

    private Object findAlreadyLoadedService(Class<?> serviceClass) {
        return this.allServicesAndBeans.stream()
                .filter(s -> serviceClass.isAssignableFrom(s.getClass()))
                .findFirst().orElse(null);
    }

    private Object instantiateService(Constructor<?> serviceConstructor, Object... params) throws ServiceLoadException {
        try {
            Object service = serviceConstructor.newInstance(params);
            this.allServicesAndBeans.add(service);
            return service;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new ServiceLoadException(e.getMessage(), e);
        }
    }

    private List<Class<?>> getOrCacheClassesByLifespan(ServiceLifeSpan lifeSpan) {
        if (this.cachedClassesByLifeSpan.containsKey(lifeSpan)) {
            return this.cachedClassesByLifeSpan.get(lifeSpan);
        }
        List<Class<?>> classes = new ArrayList<>();
        for (Object service : this.allServicesAndBeans) {
            if (this.isServiceToBeReloaded(service.getClass(), lifeSpan)) {
                classes.add(service.getClass());
            }
        }
        this.cachedClassesByLifeSpan.put(lifeSpan, classes);
        return classes;
    }

    private boolean isServiceToBeReloaded(Class<?> serviceClass, ServiceLifeSpan lifeSpan) {
        if (!serviceClass.isAnnotationPresent(Service.class)) {
            return false;
        }
        Service serviceAnnotation = serviceClass.getAnnotation(Service.class);
        return serviceAnnotation.lifespan() == lifeSpan;
    }
}