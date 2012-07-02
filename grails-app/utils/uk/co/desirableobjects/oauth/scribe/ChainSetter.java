package uk.co.desirableobjects.oauth.scribe;

import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

public class ChainSetter {
    private static final Logger log = LoggerFactory.getLogger(ChainSetter.class);

    private Object targetObject;

    private Map<String, List<Method>> methodMap = new LinkedHashMap<String, List<Method>>();

    public ChainSetter(Object targetObject) {
        this.targetObject = targetObject;
        initMethodMap();
    }

    private void initMethodMap() {
        for(Method m : targetObject.getClass().getMethods()) {
            if (!methodMap.containsKey(m.getName())) {
                methodMap.put(m.getName(), new LinkedList<Method>());
            }
            methodMap.get(m.getName()).add(m);
        }
    }

    public Object chain(String chainMethodName, List args) {
        if (!methodMap.containsKey(chainMethodName)) {
            log.warn("Unable to find {}.{}(..) of any args.", targetObject, chainMethodName);
            return null;
        }

        Iterator<Method> i = methodMap.get(chainMethodName).iterator();
        Object returnValue = null;
        while (i.hasNext() && returnValue == null) {
            Method candidateMethod = i.next();
            if (candidateMethod.getParameterTypes().length == args.size()) {
                returnValue = silentInvoke(candidateMethod, args);
            }
        }
        return returnValue;
    }

    private Object silentInvoke(Method candidateMethod, List args) {
        Object returnValue = null;
        try {
            Object[] effectiveArgs = new Object[args.size()];
            for (int i = 0; i < args.size(); i++) {
                Object arg  = args.get(i);
                Class targetType = candidateMethod.getParameterTypes()[i];
                effectiveArgs[i] = ConvertUtils.convert(arg, targetType);
            }
            returnValue = candidateMethod.invoke(targetObject, effectiveArgs);
        } catch (Exception e) {
            log.warn("Tried invoking {}.{}({}) but failed.", new Object[]{
                    targetObject,
                    candidateMethod.getName(),
                    candidateMethod.getParameterTypes()
            });
        }
        return returnValue;
    }

}
