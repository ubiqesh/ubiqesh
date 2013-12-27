package io.ubiqesh.edge.persistence.queries.scripting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.security.*;
import java.security.cert.Certificate;

/**
 * With security manager secured ScriptingEnvironment. For running Javascript on the Server with security constraint.
 *
 * @author Christoph Grotz
 */
public class SandboxedScriptingEnvironment {
    private static final Logger LOGGER = LoggerFactory.getLogger(SandboxedScriptingEnvironment.class);
    private ScriptEngineManager mgr = new ScriptEngineManager();
    private ScriptEngine engine = mgr.getEngineByName("JavaScript");
    private AccessControlContext accessControlContext;

    public SandboxedScriptingEnvironment() {
        Permissions perms = new Permissions();
        perms.add(new RuntimePermission("accessDeclaredMembers"));
        // Cast to Certificate[] required because of ambiguity:
        ProtectionDomain domain = new ProtectionDomain(new CodeSource(null, (Certificate[]) null),
                perms);
        accessControlContext = new AccessControlContext(new ProtectionDomain[]{domain});
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object eval(final String code) {
        return AccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public Object run() {
                try {
                    return engine.eval(code);
                } catch (ScriptException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                return null;
            }
        }, accessControlContext);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object invokeFunction(final String code, final Object... args) {
        return AccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public Object run() {
                try {
                    return ((Invocable) engine).invokeFunction(code, args);
                } catch (ScriptException e) {
                    LOGGER.error(e.getMessage(), e);
                } catch (NoSuchMethodException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                return null;
            }
        }, accessControlContext);
    }

    public void putValue(String key, Object obj) {
        engine.put(key, obj);
    }
}