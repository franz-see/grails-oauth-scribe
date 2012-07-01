package uk.co.desirableobjects.oauth.scribe

import java.lang.reflect.Method
import org.apache.commons.logging.LogFactory

class SimpleChainSetter {

    private static final log = LogFactory.getLog(this)

    private final Object targetObject

    private def methodMap = [:]

    SimpleChainSetter(Object targetObject) {
        this.targetObject = targetObject
    }

    private def initMethodMap = {
        targetObject.class.getMethods().each { Method m ->
            if (!methodMap.containsKey(m.getName())) {
                methodMap.put(m.getName(), [])
            }
            methodMap.get(m.getName()).add(m)
        }
    }

    def chain = { String chainMethodName, List args ->
        if (!methodMap.containsKey(chainMethodName)) {
            log.warn("Unable to find ${targetObject.class.name}.${chainMethodName}(..) of any args.")
            return null
        }

        return methodMap.get(chainMethodName).find { Method candidateMethod ->
            def returnValue = null
            if (candidateMethod.getParameterTypes().length == args.size()) {
                try {
                    returnValue = candidateMethod.invoke(targetObject, args)
                } catch (Exception e) {
                    log.warn("Tried invoking ${candidateMethod.name}(${candidateMethod.getParameterTypes().join(', ')}) but failed.", e)
                }
            }
            returnValue
        }
    }

}
