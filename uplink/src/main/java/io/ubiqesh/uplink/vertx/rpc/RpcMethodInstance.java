package io.ubiqesh.uplink.vertx.rpc;

import org.vertx.java.core.json.JsonObject;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class RpcMethodInstance {
    private Object instance;
    private java.lang.reflect.Method method;
    private ObjectConverter objectConverter = new ObjectConverter();

    public RpcMethodInstance(Object instance, java.lang.reflect.Method method) {
        this.instance = instance;
        this.method = method;
    }

    public Object invoke(JsonObject passedArgs) {
        try {
            List<Object> args = new ArrayList<Object>();
            for (Annotation[] annotations : method.getParameterAnnotations()) {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof Param) {
                        if (passedArgs != null) {
                            Param param = (Param) annotation;
                            if (passedArgs.getFieldNames().contains(param.value())) {
                                args.add(passedArgs.getField(param.value()));
                            } else {
                                if (param.defaultValue() != null || param.defaultValue().isEmpty()) {
                                    args.add(param.defaultValue());
                                } else {
                                    args.add(null);
                                }
                            }
                        }
                    }
                }
            }
            Object[] argArray = createArray(method, args);
            return method.invoke(instance, argArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object[] createArray(java.lang.reflect.Method method, List<Object> args) {
        Object[] array = args.toArray();
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            array[i] = objectConverter.convert(array[i], parameterTypes[i]);
        }
        return array;
    }
}