package com.zw.platform.push.config;

import org.springframework.expression.BeanResolver;
import org.springframework.expression.ConstructorResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.OperatorOverloader;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypeComparator;
import org.springframework.expression.TypeConverter;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.expression.spel.support.StandardOperatorOverloader;
import org.springframework.expression.spel.support.StandardTypeComparator;
import org.springframework.expression.spel.support.StandardTypeConverter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SimpleEvaluationContext implements EvaluationContext {

    private static final TypeLocator typeNotFoundTypeLocator = typeName -> {
        throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, typeName);
    };

    private final TypedValue rootObject;

    private final List<PropertyAccessor> propertyAccessors;

    private final List<MethodResolver> methodResolvers;

    private final TypeConverter typeConverter;

    private final TypeComparator typeComparator = new StandardTypeComparator();

    private final OperatorOverloader operatorOverloader = new StandardOperatorOverloader();

    private final Map<String, Object> variables = new HashMap<>();


    private SimpleEvaluationContext(List<PropertyAccessor> accessors, List<MethodResolver> resolvers,
        @Nullable TypeConverter converter, @Nullable TypedValue rootObject) {

        this.propertyAccessors = accessors;
        this.methodResolvers = resolvers;
        this.typeConverter = (converter != null ? converter : new StandardTypeConverter());
        this.rootObject = (rootObject != null ? rootObject : TypedValue.NULL);
    }


    /**
     * Return the specified root object, if any.
     */
    @Override
    public TypedValue getRootObject() {
        return this.rootObject;
    }

    /**
     * Return the specified {@link PropertyAccessor} delegates, if any.
     * @see #forPropertyAccessors
     */
    @Override
    public List<PropertyAccessor> getPropertyAccessors() {
        return this.propertyAccessors;
    }

    /**
     * Return an empty list, always, since this context does not support the
     * use of type references.
     */
    @Override
    public List<ConstructorResolver> getConstructorResolvers() {
        return Collections.emptyList();
    }

    /**
     * Return the specified {@link MethodResolver} delegates, if any.
     */
    @Override
    public List<MethodResolver> getMethodResolvers() {
        return this.methodResolvers;
    }

    /**
     * {@code SimpleEvaluationContext} does not support the use of bean references.
     * @return always {@code null}
     */
    @Override
    @Nullable
    public BeanResolver getBeanResolver() {
        return null;
    }

    /**
     * {@code SimpleEvaluationContext} does not support use of type references.
     * @return {@code TypeLocator} implementation that raises a
     * {@link SpelEvaluationException} with {@link SpelMessage#TYPE_NOT_FOUND}.
     */
    @Override
    public TypeLocator getTypeLocator() {
        return typeNotFoundTypeLocator;
    }

    /**
     * The configured {@link TypeConverter}.
     * <p>By default this is {@link StandardTypeConverter}.
     * @see Builder#withTypeConverter
     * @see Builder#withConversionService
     */
    @Override
    public TypeConverter getTypeConverter() {
        return this.typeConverter;
    }

    /**
     * Return an instance of {@link StandardTypeComparator}.
     */
    @Override
    public TypeComparator getTypeComparator() {
        return this.typeComparator;
    }

    /**
     * Return an instance of {@link StandardOperatorOverloader}.
     */
    @Override
    public OperatorOverloader getOperatorOverloader() {
        return this.operatorOverloader;
    }

    @Override
    public void setVariable(String name, @Nullable Object value) {
        this.variables.put(name, value);
    }

    @Override
    @Nullable
    public Object lookupVariable(String name) {
        return this.variables.get(name);
    }


    /**
     * Create a {@code SimpleEvaluationContext} for the specified {@link PropertyAccessor}
     * delegates: typically a custom {@code PropertyAccessor} specific to a use case
     * (e.g. attribute resolution in a custom data structure), potentially combined with
     * a {@link PropertyAccessor} if property dereferences are needed as well.
     * @param accessors the accessor delegates to use
     */
    public static Builder forPropertyAccessors(PropertyAccessor... accessors) {
        for (PropertyAccessor accessor : accessors) {
            if (accessor.getClass() == ReflectivePropertyAccessor.class) {
                throw new IllegalArgumentException("SimpleEvaluationContext is not designed for use with a plain "
                    + "ReflectivePropertyAccessor. Consider using DataBindingPropertyAccessor or a custom subclass.");
            }
        }
        return new Builder(accessors);
    }


    /**
     * Builder for {@code SimpleEvaluationContext}.
     */
    public static class Builder {

        private final List<PropertyAccessor> accessors;

        public Builder(PropertyAccessor... accessors) {
            this.accessors = Arrays.asList(accessors);
        }

        public SimpleEvaluationContext build() {
            return new SimpleEvaluationContext(this.accessors, Collections.emptyList(), null, null);
        }
    }

}
