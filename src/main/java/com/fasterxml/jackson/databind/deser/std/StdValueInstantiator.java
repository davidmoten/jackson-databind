package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Default {@link ValueInstantiator} implementation, which supports
 * Creator methods that can be indicated by standard Jackson
 * annotations.
 */
@JacksonStdImpl
public class StdValueInstantiator
    extends ValueInstantiator
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;
    
    private static final BigInteger BIG_INTEGER_MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigInteger BIG_INTEGER_MIN_INT = BigInteger.valueOf(Integer.MIN_VALUE);
    private static final BigInteger BIG_INTEGER_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger BIG_INTEGER_MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);

    /**
     * Type of values that are instantiated; used
     * for error reporting purposes.
     */
    protected final String _valueTypeDesc;

    /**
     * @since 2.8
     */
    protected final Class<?> _valueClass;

    // // // Default (no-args) construction

    /**
     * Default (no-argument) constructor to use for instantiation
     * (with {@link #createUsingDefault})
     */
    protected AnnotatedWithParams _defaultCreator;

    // // // With-args (property-based) construction

    protected AnnotatedWithParams _withArgsCreator;
    protected SettableBeanProperty[] _constructorArguments;

    // // // Delegate construction

    protected JavaType _delegateType;
    protected AnnotatedWithParams _delegateCreator;
    protected SettableBeanProperty[] _delegateArguments;

    // // // Array delegate construction

    protected JavaType _arrayDelegateType;
    protected AnnotatedWithParams _arrayDelegateCreator;
    protected SettableBeanProperty[] _arrayDelegateArguments;

    // // // Scalar construction

    protected AnnotatedWithParams _fromStringCreator;
    protected AnnotatedWithParams _fromIntCreator;
    protected AnnotatedWithParams _fromLongCreator;
    protected AnnotatedWithParams _fromBigIntegerCreator;
    protected AnnotatedWithParams _fromDoubleCreator;
    protected AnnotatedWithParams _fromFloatCreator;
    protected AnnotatedWithParams _fromBigDecimalCreator;
    protected AnnotatedWithParams _fromBooleanCreator;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    /**
     * @deprecated Since 2.7 use constructor that takes {@link JavaType} instead
     */
    @Deprecated
    public StdValueInstantiator(DeserializationConfig config, Class<?> valueType) {
        _valueTypeDesc = ClassUtil.nameOf(valueType);
        _valueClass = (valueType == null) ? Object.class : valueType;
    }

    public StdValueInstantiator(DeserializationConfig config, JavaType valueType) {
        _valueTypeDesc = (valueType == null) ? "UNKNOWN TYPE" : valueType.toString();
        _valueClass = (valueType == null) ? Object.class : valueType.getRawClass();
    }

    /**
     * Copy-constructor that sub-classes can use when creating new instances
     * by fluent-style construction
     */
    protected StdValueInstantiator(StdValueInstantiator src)
    {
        _valueTypeDesc = src._valueTypeDesc;
        _valueClass = src._valueClass;

        _defaultCreator = src._defaultCreator;

        _constructorArguments = src._constructorArguments;
        _withArgsCreator = src._withArgsCreator;

        _delegateType = src._delegateType;
        _delegateCreator = src._delegateCreator;
        _delegateArguments = src._delegateArguments;

        _arrayDelegateType = src._arrayDelegateType;
        _arrayDelegateCreator = src._arrayDelegateCreator;
        _arrayDelegateArguments = src._arrayDelegateArguments;

        _fromStringCreator = src._fromStringCreator;
        _fromIntCreator = src._fromIntCreator;
        _fromLongCreator = src._fromLongCreator;
        _fromBigIntegerCreator = src._fromBigIntegerCreator;
        _fromDoubleCreator = src._fromDoubleCreator;
        _fromBigDecimalCreator = src._fromBigDecimalCreator;
        _fromBooleanCreator = src._fromBooleanCreator;
    }

    /**
     * Method for setting properties related to instantiating values
     * from JSON Object. We will choose basically only one approach (out of possible
     * three), and clear other properties
     */
    public void configureFromObjectSettings(AnnotatedWithParams defaultCreator,
            AnnotatedWithParams delegateCreator, JavaType delegateType, SettableBeanProperty[] delegateArgs,
            AnnotatedWithParams withArgsCreator, SettableBeanProperty[] constructorArgs)
    {
        _defaultCreator = defaultCreator;
        _delegateCreator = delegateCreator;
        _delegateType = delegateType;
        _delegateArguments = delegateArgs;
        _withArgsCreator = withArgsCreator;
        _constructorArguments = constructorArgs;
    }

    public void configureFromArraySettings(
            AnnotatedWithParams arrayDelegateCreator,
            JavaType arrayDelegateType,
            SettableBeanProperty[] arrayDelegateArgs)
    {
        _arrayDelegateCreator = arrayDelegateCreator;
        _arrayDelegateType = arrayDelegateType;
        _arrayDelegateArguments = arrayDelegateArgs;
    }

    public void configureFromStringCreator(AnnotatedWithParams creator) {
        _fromStringCreator = creator;
    }

    public void configureFromIntCreator(AnnotatedWithParams creator) {
        _fromIntCreator = creator;
    }

    public void configureFromLongCreator(AnnotatedWithParams creator) {
        _fromLongCreator = creator;
    }

    public void configureFromBigIntegerCreator(AnnotatedWithParams creator) { _fromBigIntegerCreator = creator; }

    public void configureFromDoubleCreator(AnnotatedWithParams creator) {
        _fromDoubleCreator = creator;
    }
    
    public void configureFromFloatCreator(AnnotatedWithParams creator) {
        _fromFloatCreator = creator;
    }

    public void configureFromBigDecimalCreator(AnnotatedWithParams creator) { _fromBigDecimalCreator = creator; }

    public void configureFromBooleanCreator(AnnotatedWithParams creator) {
        _fromBooleanCreator = creator;
    }

    /*
    /**********************************************************
    /* Public API implementation; metadata
    /**********************************************************
     */

    @Override
    public String getValueTypeDesc() {
        return _valueTypeDesc;
    }

    @Override
    public Class<?> getValueClass() {
        return _valueClass;
    }

    @Override
    public boolean canCreateFromString() {
        return (_fromStringCreator != null);
    }

    @Override
    public boolean canCreateFromInt() {
        return (_fromIntCreator != null);
    }

    @Override
    public boolean canCreateFromLong() {
        return (_fromLongCreator != null);
    }

    @Override
    public boolean canCreateFromBigInteger() { return _fromBigIntegerCreator != null; }

    @Override
    public boolean canCreateFromDouble() {
        return (_fromDoubleCreator != null);
    }

    @Override
    public boolean canCreateFromBigDecimal() { return _fromBigDecimalCreator != null; }

    @Override
    public boolean canCreateFromBoolean() {
        return (_fromBooleanCreator != null);
    }

    @Override
    public boolean canCreateUsingDefault() {
        return (_defaultCreator != null);
    }

    @Override
    public boolean canCreateUsingDelegate() {
        return (_delegateType != null);
    }

    @Override
    public boolean canCreateUsingArrayDelegate() {
        return (_arrayDelegateType != null);
    }

    @Override
    public boolean canCreateFromObjectWith() {
        return (_withArgsCreator != null);
    }

    @Override
    public boolean canInstantiate() {
        return canCreateUsingDefault()
                || canCreateUsingDelegate() || canCreateUsingArrayDelegate()
                || canCreateFromObjectWith() || canCreateFromString()
                || canCreateFromInt() || canCreateFromLong()
                || canCreateFromDouble() || canCreateFromBoolean();
    }

    @Override
    public JavaType getDelegateType(DeserializationConfig config) {
        return _delegateType;
    }

    @Override
    public JavaType getArrayDelegateType(DeserializationConfig config) {
        return _arrayDelegateType;
    }

    @Override
    public SettableBeanProperty[] getFromObjectArguments(DeserializationConfig config) {
        return _constructorArguments;
    }

    /*
    /**********************************************************
    /* Public API implementation; instantiation from JSON Object
    /**********************************************************
     */

    @Override
    public Object createUsingDefault(DeserializationContext ctxt) throws IOException
    {
        if (_defaultCreator == null) { // sanity-check; caller should check
            return super.createUsingDefault(ctxt);
        }
        try {
            return _defaultCreator.call();
        } catch (Exception e) { // 19-Apr-2017, tatu: Let's not catch Errors, just Exceptions
            return ctxt.handleInstantiationProblem(_valueClass, null, rewrapCtorProblem(ctxt, e));
        }
    }

    @Override
    public Object createFromObjectWith(DeserializationContext ctxt, Object[] args) throws IOException
    {
        if (_withArgsCreator == null) { // sanity-check; caller should check
            return super.createFromObjectWith(ctxt, args);
        }
        try {
            return _withArgsCreator.call(args);
        } catch (Exception e) { // 19-Apr-2017, tatu: Let's not catch Errors, just Exceptions
            return ctxt.handleInstantiationProblem(_valueClass, args, rewrapCtorProblem(ctxt, e));
        }
    }

    @Override
    public Object createUsingDefaultOrWithoutArguments(DeserializationContext ctxt) throws IOException {
        if (_defaultCreator != null) { // sanity-check; caller should check
            return createUsingDefault(ctxt);
        }
        if (_withArgsCreator != null) {
            return createFromObjectWith(ctxt, new Object[_constructorArguments.length]);
        }
        return super.createUsingDefaultOrWithoutArguments(ctxt);
    }

    @Override
    public Object createUsingDelegate(DeserializationContext ctxt, Object delegate) throws IOException
    {
        // 04-Oct-2016, tatu: Need delegation to work around [databind#1392]...
        if (_delegateCreator == null) {
            if (_arrayDelegateCreator != null) {
                return _createUsingDelegate(_arrayDelegateCreator, _arrayDelegateArguments, ctxt, delegate);
            }
        }
        return _createUsingDelegate(_delegateCreator, _delegateArguments, ctxt, delegate);
    }

    @Override
    public Object createUsingArrayDelegate(DeserializationContext ctxt, Object delegate) throws IOException
    {
        if (_arrayDelegateCreator == null) {
            if (_delegateCreator != null) { // sanity-check; caller should check
                // fallback to the classic delegate creator
                return createUsingDelegate(ctxt, delegate);
            }
        }
        return _createUsingDelegate(_arrayDelegateCreator, _arrayDelegateArguments, ctxt, delegate);
    }

    /*
    /**********************************************************
    /* Public API implementation; instantiation from JSON scalars
    /**********************************************************
     */

    @Override
    public Object createFromString(DeserializationContext ctxt, String value) throws IOException
    {
        if (_fromStringCreator != null) {
            return instantiate(ctxt, _fromStringCreator, value);
        }
        return super.createFromString(ctxt, value);
    }

    @Override
    public Object createFromInt(DeserializationContext ctxt, int value) throws IOException
    {
        // First: "native" int methods work best:
        if (_fromIntCreator != null) {
            return instantiate(ctxt, _fromIntCreator, value);
        }
        // but if not, can do widening conversion
        if (_fromLongCreator != null) {
            Long arg = Long.valueOf(value);
            return instantiate(ctxt, _fromLongCreator, arg);
        }

        if (_fromBigIntegerCreator != null) {
            BigInteger arg = BigInteger.valueOf(value);
            return instantiate(ctxt, _fromBigIntegerCreator, arg);
        }
        
        if (_fromBigDecimalCreator != null) {
            BigDecimal arg = BigDecimal.valueOf(value);
            return instantiate(ctxt, _fromBigDecimalCreator, arg);
        }
        
        if (_fromDoubleCreator != null) {
            Double arg = Double.valueOf(value);
            return instantiate(ctxt, _fromDoubleCreator, arg);
        }

        // may lose precision
        
        if (_fromFloatCreator != null) {
            Float arg = Float.valueOf(value);
            return instantiate(ctxt, _fromFloatCreator, arg);
        }
        
        return super.createFromInt(ctxt, value);
    }

    

    @Override
    public Object createFromLong(DeserializationContext ctxt, long value) throws IOException
    {
        if (_fromLongCreator != null) {
            Long arg = Long.valueOf(value);
            return instantiate(ctxt, _fromLongCreator, arg);
        }

        if (_fromBigIntegerCreator != null) {
            BigInteger arg = BigInteger.valueOf(value);
            return instantiate(ctxt, _fromBigIntegerCreator, arg);
        }
        
        // may lose precision
        
        if (_fromDoubleCreator != null) {
            Double arg = Double.valueOf(value);
            return instantiate(ctxt, _fromDoubleCreator, arg);
        }
        
        if (_fromFloatCreator != null) {
            Float arg = Float.valueOf(value);
            return instantiate(ctxt, _fromFloatCreator, arg);
        }

        return super.createFromLong(ctxt, value);
    }

    @Override
    public Object createFromBigInteger(DeserializationContext ctxt, BigInteger value) throws IOException
    {
        if (_fromBigIntegerCreator != null) {
            return instantiate(ctxt, _fromBigIntegerCreator, value);
        }
        
        if (_fromBigDecimalCreator != null) {
            BigDecimal arg = new BigDecimal(value.toString());
            return instantiate(ctxt, _fromBigDecimalCreator, arg);
        }
        
        if (_fromIntCreator != null) {
            if (value.compareTo(BIG_INTEGER_MAX_INT) <= 0 && value.compareTo(BIG_INTEGER_MIN_INT) >= 0) {
                Integer arg = value.intValue();
                return instantiate(ctxt, _fromIntCreator, arg);
            }
        }
        
        if (_fromLongCreator != null) {
            if (value.compareTo(BIG_INTEGER_MAX_LONG) <= 0 && value.compareTo(BIG_INTEGER_MIN_LONG) >= 0) {
                long arg = value.longValue();
                return instantiate(ctxt, _fromLongCreator, arg);
            }
        }
        
        // may lose precision

        if (_fromDoubleCreator != null) {
            double arg = value.doubleValue();
            if (Double.isFinite(arg)) {
                return instantiate(ctxt, _fromDoubleCreator, arg);
            }
        }
        
        if (_fromFloatCreator != null) {
            float arg = value.floatValue();
            if (Float.isFinite(arg)) {
                return instantiate(ctxt, _fromFloatCreator, arg);
            }
        }
        
        return super.createFromBigInteger(ctxt, value);
    }

    @Override
    public Object createFromDouble(DeserializationContext ctxt, double value) throws IOException
    {
        if(_fromDoubleCreator != null) {
            Double arg = Double.valueOf(value);
            return instantiate(ctxt, _fromDoubleCreator, arg);
        }

        if (_fromBigDecimalCreator != null) {
            BigDecimal arg = BigDecimal.valueOf(value);
            return instantiate(ctxt, _fromLongCreator, arg);
        }
        
        // may lose precision
        
        if(_fromFloatCreator != null) {
            float arg = ((Double) value).floatValue();
            if (Float.isFinite(arg)) {
                return instantiate(ctxt, _fromFloatCreator, arg);
            }
        }

        return super.createFromDouble(ctxt, value);
    }

    @Override
    public Object createFromBigDecimal(DeserializationContext ctxt, BigDecimal value) throws IOException
    {
        if (_fromBigDecimalCreator != null) {
            return instantiate(ctxt, _fromBigDecimalCreator, value);
        }

        // may lose precision 
        
        if (_fromDoubleCreator != null) {
            double arg = value.doubleValue();
            if (Double.isFinite(arg)) {
                return instantiate(ctxt, _fromDoubleCreator, arg);
            }
        }
        
        if(_fromFloatCreator != null) {
            Float arg =  value.floatValue();
            if (Float.isFinite(arg)) {
                return instantiate(ctxt, _fromFloatCreator, arg);
            }
        }

        return super.createFromBigDecimal(ctxt, value);
    }

    @Override
    public Object createFromBoolean(DeserializationContext ctxt, boolean value) throws IOException
    {
        if (_fromBooleanCreator == null) {
            return super.createFromBoolean(ctxt, value);
        }
        final Boolean arg = Boolean.valueOf(value);
        return instantiate(ctxt, _fromBooleanCreator, arg);
    }

    /*
    /**********************************************************
    /* Extended API: configuration mutators, accessors
    /**********************************************************
     */

    @Override
    public AnnotatedWithParams getDelegateCreator() {
        return _delegateCreator;
    }

    @Override
    public AnnotatedWithParams getArrayDelegateCreator() {
        return _arrayDelegateCreator;
    }

    @Override
    public AnnotatedWithParams getDefaultCreator() {
        return _defaultCreator;
    }

    @Override
    public AnnotatedWithParams getWithArgsCreator() {
        return _withArgsCreator;
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    /**
     * @deprecated Since 2.7 call either {@link #rewrapCtorProblem} or
     *  {@link #wrapAsJsonMappingException}
     */
    @Deprecated // since 2.7
    protected JsonMappingException wrapException(Throwable t)
    {
        // 05-Nov-2015, tatu: This used to always unwrap the whole exception, but now only
        //   does so if and until `JsonMappingException` is found.
        for (Throwable curr = t; curr != null; curr = curr.getCause()) {
            if (curr instanceof JsonMappingException) {
                return (JsonMappingException) curr;
            }
        }
        return new JsonMappingException(null,
                "Instantiation of "+getValueTypeDesc()+" value failed: "+ClassUtil.exceptionMessage(t), t);
    }

    /**
     * @deprecated Since 2.7 call either {@link #rewrapCtorProblem} or
     *  {@link #wrapAsJsonMappingException}
     */
    @Deprecated // since 2.10
    protected JsonMappingException unwrapAndWrapException(DeserializationContext ctxt, Throwable t)
    {
        // 05-Nov-2015, tatu: This used to always unwrap the whole exception, but now only
        //   does so if and until `JsonMappingException` is found.
        for (Throwable curr = t; curr != null; curr = curr.getCause()) {
            if (curr instanceof JsonMappingException) {
                return (JsonMappingException) curr;
            }
        }
        return ctxt.instantiationException(getValueClass(), t);
    }

    /**
     * Helper method that will return given {@link Throwable} case as
     * a {@link JsonMappingException} (if it is of that type), or call
     * {@link DeserializationContext#instantiationException(Class, Throwable)} to
     * produce and return suitable {@link JsonMappingException}.
     *
     * @since 2.7
     */
    protected JsonMappingException wrapAsJsonMappingException(DeserializationContext ctxt,
            Throwable t)
    {
        // 05-Nov-2015, tatu: Only avoid wrapping if already a JsonMappingException
        if (t instanceof JsonMappingException) {
            return (JsonMappingException) t;
        }
        return ctxt.instantiationException(getValueClass(), t);
    }

    /**
     * Method that subclasses may call for standard handling of an exception thrown when
     * calling constructor or factory method. Will unwrap {@link ExceptionInInitializerError}
     * and {@link InvocationTargetException}s, then call {@link #wrapAsJsonMappingException}.
     *
     * @since 2.7
     */
    protected JsonMappingException rewrapCtorProblem(DeserializationContext ctxt,
            Throwable t)
    {
        // 05-Nov-2015, tatu: Seems like there are really only 2 useless wrapper errors/exceptions,
        //    so just peel those, and nothing else
        if ((t instanceof ExceptionInInitializerError) // from static initialization block
                || (t instanceof InvocationTargetException) // from constructor/method
                ) {
            Throwable cause = t.getCause();
            if (cause != null) {
                t = cause;
            }
        }
        return wrapAsJsonMappingException(ctxt, t);
    }

    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */

    private Object _createUsingDelegate(AnnotatedWithParams delegateCreator,
            SettableBeanProperty[] delegateArguments,
            DeserializationContext ctxt,
            Object delegate)
            throws IOException
    {
        if (delegateCreator == null) { // sanity-check; caller should check
            throw new IllegalStateException("No delegate constructor for "+getValueTypeDesc());
        }
        try {
            // First simple case: just delegate, no injectables
            if (delegateArguments == null) {
                return delegateCreator.call1(delegate);
            }
            // And then the case with at least one injectable...
            final int len = delegateArguments.length;
            Object[] args = new Object[len];
            for (int i = 0; i < len; ++i) {
                SettableBeanProperty prop = delegateArguments[i];
                if (prop == null) { // delegate
                    args[i] = delegate;
                } else { // nope, injectable:
                    args[i] = ctxt.findInjectableValue(prop.getInjectableValueId(), prop, null);
                }
            }
            // and then try calling with full set of arguments
            return delegateCreator.call(args);
        } catch (Exception t) {
            throw rewrapCtorProblem(ctxt, t);
        }
    }
    
    private Object instantiate(DeserializationContext ctxt, AnnotatedWithParams creator, Object arg) throws IOException {
        try {
            return creator.call1(arg);
        } catch (Exception t0) {
            return ctxt.handleInstantiationProblem(creator.getDeclaringClass(),
                    arg, rewrapCtorProblem(ctxt, t0)
            );
        }
    }
    
}
