/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.resourcemanager.impl.model;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_7;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.resourcemanager.impl.ApplicationResourceManager;
import org.ogema.resourcemanager.impl.ResourceBase;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jan Lapp, Fraunhofer IWES
 */
enum ResourceFactoryASM {

	INSTANCE;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Map<Class<? extends Resource>, Class<? extends ResourceBase>> implementationTypes = new HashMap<>();
    
    private final String BASECLASS_NAME = Type.getInternalName(ResourceBase.class);
    private final String CONSTRUCTOR_DESCRIPTOR = Type.getConstructorDescriptor(ResourceBase.class.getConstructors()[0]);

	ResourceFactoryASM() {
	}

	private synchronized Class<? extends ResourceBase> getImplementation(Class<? extends Resource> ogemaType) {
		Class<? extends ResourceBase> implementationType = implementationTypes.get(ogemaType);
		if (implementationType != null) {
			return implementationType;
		}
		implementationType = createImplementationType(ogemaType);
		implementationTypes.put(ogemaType, implementationType);
		return implementationType;
	}

	private Class<? extends ResourceBase> createImplementationType(final Class<? extends Resource> ogemaType) {
		final long startTime = System.currentTimeMillis();

		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;
        
		final String classname = "resourceImpl."+ogemaType.getCanonicalName();

		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER,
				classname.replace('.', '/'), null,
				BASECLASS_NAME,
				new String[]{Type.getInternalName(ogemaType)});

		{//constructor
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", CONSTRUCTOR_DESCRIPTOR, null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKESPECIAL, BASECLASS_NAME, "<init>", CONSTRUCTOR_DESCRIPTOR, false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 4);
			mv.visitEnd();
		}

        for (Method m : collectMethodsToImplement(ogemaType)) {
			String signature = null;
			if (ResourceList.class.isAssignableFrom(m.getReturnType())){
				java.lang.reflect.Type type = m.getGenericReturnType();
				Class<?> typeParameter = (Class) ((ParameterizedType)type).getActualTypeArguments()[0];

				//XXX use ASM to generate signatures?
				signature = String.format("()L%s<L%s;>;",
						Type.getInternalName(m.getReturnType()),
						Type.getInternalName(typeParameter));
			}

			mv = cw.visitMethod(ACC_PUBLIC, m.getName(),
					Type.getMethodDescriptor(m),
					signature, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitLdcInsn(m.getName());
			mv.visitMethodInsn(INVOKEVIRTUAL,
					BASECLASS_NAME,
					"getSubResource",
					"(Ljava/lang/String;)Lorg/ogema/core/model/Resource;", false);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		cw.visitEnd();

		final byte[] bytes = cw.toByteArray();

		final ClassLoader cl = ResourceBase.class.getClassLoader();

		return AccessController.doPrivileged(new PrivilegedAction<Class<? extends ResourceBase>>() {
			@Override
			public Class<? extends ResourceBase> run() {
				try {
					Method define = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
					define.setAccessible(true);
					logger.info("created implementation class for type {} ({} bytes, {} ms)",
							ogemaType, bytes.length, System.currentTimeMillis()-startTime);
                    @SuppressWarnings("unchecked")
                    Class<? extends ResourceBase> implementationClass =
                            (Class) define.invoke(cl, classname, bytes, 0, bytes.length, ogemaType.getProtectionDomain());
					return implementationClass;
				} catch (NoSuchMethodException | SecurityException |
						IllegalAccessException | IllegalArgumentException | InvocationTargetException ex){
					throw new InvalidResourceTypeException("Code generation failed", ex);
				}
			}
		});
	}
    
    private Collection<Method> collectMethodsToImplement(Class<?> type){
        Map<String, Method> methodsToImplement = new HashMap<>();
        for (Method m: type.getMethods()){
            if (m.getDeclaringClass().equals(Resource.class) || m.isBridge() || m.isSynthetic()) {
				continue;
			}
            Method previousMethod = methodsToImplement.get(m.getName());
            if (previousMethod != null && methodAoverwritesB(previousMethod, m)){
                continue;
            }
            methodsToImplement.put(m.getName(), m);
        }
        return methodsToImplement.values();
    }
    
    private boolean methodAoverwritesB(Method a, Method b){
        /*
        covariant override can not be detected with Method#isBridge() in Java 7
        (in Java 8, the overridden method is both a bridge and a default method)
        */
        return b.getReturnType().isAssignableFrom(b.getReturnType());
    }

    @SuppressWarnings("unchecked")
	public <T extends ResourceBase> T makeResource(VirtualTreeElement el, String path, ApplicationResourceManager resman) {
		try {
			Class<? extends ResourceBase> impl = getImplementation(el.getType());
			Constructor<?> constr = impl.getConstructor(VirtualTreeElement.class, String.class, ApplicationResourceManager.class);

			return (T) constr.newInstance(new Object[]{el, path, resman});
		} catch (IllegalAccessException | IllegalArgumentException |
				InstantiationException | NoSuchMethodException |
				SecurityException | InvocationTargetException ex) {
			throw new InvalidResourceTypeException("Code generation failed", ex);
		}
	}

}
