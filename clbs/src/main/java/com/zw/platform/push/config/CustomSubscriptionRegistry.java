package com.zw.platform.push.config;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.broker.AbstractSubscriptionRegistry;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class CustomSubscriptionRegistry extends AbstractSubscriptionRegistry {

    /**
     * Default maximum number of entries for the destination cache: 1024
     */
    public static final int DEFAULT_CACHE_LIMIT = 1024;

    /**
     * Static evaluation context to reuse.
     */
    private static final EvaluationContext messageEvalContext =
        SimpleEvaluationContext.forPropertyAccessors(new SimpMessageHeaderPropertyAccessor()).build();

    /**
     * The maximum number of entries in the cache
     */
    private int cacheLimit = DEFAULT_CACHE_LIMIT;

    private PathMatcher pathMatcher = new AntPathMatcher();

    @Nullable
    private String selectorHeaderName = "selector";

    private volatile boolean selectorHeaderInUse = false;

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    private final DestinationCache destinationCache = new DestinationCache();

    private final SessionRegistry sessionRegistry = new SessionRegistry();

    /**
     * Specify the maximum number of entries for the resolved destination cache.
     * Default is 1024.
     */
    public void setCacheLimit(int cacheLimit) {
        this.cacheLimit = cacheLimit;
        this.destinationCache.ensureCacheLimit();
    }

    /**
     * Return the maximum number of entries for the resolved destination cache.
     */
    public int getCacheLimit() {
        return this.cacheLimit;
    }

    /**
     * Specify the {@link PathMatcher} to use.
     */
    public void setPathMatcher(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    /**
     * Return the configured {@link PathMatcher}.
     */
    public PathMatcher getPathMatcher() {
        return this.pathMatcher;
    }

    /**
     * Configure the name of a selector header that a subscription message can
     * have in order to filter messages based on their headers. The value of the
     * header can use Spring EL expressions against message headers.
     * <p>For example the following expression expects a header called "foo" to
     * have the value "bar":
     * <pre>
     * headers.foo == 'bar'
     * </pre>
     * <p>By default this is set to "selector".
     * @since 4.2
     */
    public void setSelectorHeaderName(@Nullable String selectorHeaderName) {
        this.selectorHeaderName = (StringUtils.hasText(selectorHeaderName) ? selectorHeaderName : null);
    }

    /**
     * Return the name for the selector header.
     */
    @Nullable
    public String getSelectorHeaderName() {
        return this.selectorHeaderName;
    }

    @Override
    protected void addSubscriptionInternal(String sessionId, String subsId, String destination, Message<?> message) {
        boolean isPattern = this.pathMatcher.isPattern(destination);
        Expression expression = getSelectorExpression(message.getHeaders());
        Subscription subscription = new Subscription(subsId, destination, isPattern, expression);

        this.sessionRegistry.addSubscription(sessionId, subscription);
        this.destinationCache.updateAfterNewSubscription(sessionId, subscription);
    }

    @Nullable
    private Expression getSelectorExpression(MessageHeaders headers) {
        if (getSelectorHeaderName() == null) {
            return null;
        }
        String selector = NativeMessageHeaderAccessor.getFirstNativeHeader(getSelectorHeaderName(), headers);
        if (selector == null) {
            return null;
        }
        Expression expression = null;
        try {
            expression = this.expressionParser.parseExpression(selector);
            this.selectorHeaderInUse = true;
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to parse selector: " + selector, ex);
            }
        }
        return expression;
    }

    @Override
    protected void removeSubscriptionInternal(String sessionId, String subscriptionId, Message<?> message) {
        SessionInfo info = this.sessionRegistry.getSession(sessionId);
        if (info != null) {
            Subscription subscription = info.removeSubscription(subscriptionId);
            if (subscription != null) {
                this.destinationCache.updateAfterRemovedSubscription(sessionId, subscription);
            }
        }
    }

    @Override
    public void unregisterAllSubscriptions(String sessionId) {
        SessionInfo info = this.sessionRegistry.removeSubscriptions(sessionId);
        if (info != null) {
            this.destinationCache.updateAfterRemovedSession(sessionId, info);
        }
    }

    @Override
    protected MultiValueMap<String, String> findSubscriptionsInternal(String destination, Message<?> message) {
        MultiValueMap<String, String> allMatches = this.destinationCache.getSubscriptions(destination);
        if (!this.selectorHeaderInUse) {
            return allMatches;
        }
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>(allMatches.size());
        allMatches.forEach((sessionId, subscriptionIds) -> {
            SessionInfo info = this.sessionRegistry.getSession(sessionId);
            if (info != null) {
                for (String subscriptionId : subscriptionIds) {
                    Subscription subscription = info.getSubscription(subscriptionId);
                    if (subscription != null && evaluateExpression(subscription.getSelector(), message)) {
                        result.add(sessionId, subscription.getId());
                    }
                }
            }
        });
        return result;
    }

    private boolean evaluateExpression(@Nullable Expression expression, Message<?> message) {
        if (expression == null) {
            return true;
        }
        try {
            Boolean result = expression.getValue(messageEvalContext, message, Boolean.class);
            if (Boolean.TRUE.equals(result)) {
                return true;
            }
        } catch (SpelEvaluationException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to evaluate selector: " + ex.getMessage());
            }
        } catch (Exception ex) {
            logger.debug("Failed to evaluate selector", ex);
        }
        return false;
    }

    @Override
    public String toString() {
        return "CustomSubscriptionRegistry[" + this.destinationCache + ", " + this.sessionRegistry + "]";
    }

    /**
     * A cache for destinations previously resolved via
     * {@link CustomSubscriptionRegistry#findSubscriptionsInternal(String, Message)}
     */
    private final class DestinationCache {

        // destination -> [sessionId -> subscriptionId's]
        private final Map<String, LinkedMultiValueMap<String, String>> destinationMap =
            new ConcurrentHashMap<>(DEFAULT_CACHE_LIMIT);

        private final AtomicInteger cacheSize = new AtomicInteger();

        private final Queue<String> cacheEvictionPolicy = new ConcurrentLinkedQueue<>();

        public LinkedMultiValueMap<String, String> getSubscriptions(String destination) {
            LinkedMultiValueMap<String, String> sessionIdToSubscriptionIds = this.destinationMap.get(destination);
            if (sessionIdToSubscriptionIds == null) {
                sessionIdToSubscriptionIds = this.destinationMap.computeIfAbsent(destination, destinations -> {
                    LinkedMultiValueMap<String, String> matches = computeMatchingSubscriptions(destination);
                    // Update queue first, so that cacheSize <= queue.size(
                    this.cacheEvictionPolicy.add(destination);
                    this.cacheSize.incrementAndGet();
                    return matches;
                });
                ensureCacheLimit();
            }
            return sessionIdToSubscriptionIds;
        }

        private LinkedMultiValueMap<String, String> computeMatchingSubscriptions(String destination) {
            LinkedMultiValueMap<String, String> sessionIdToSubscriptionIds = new LinkedMultiValueMap<>();
            CustomSubscriptionRegistry.this.sessionRegistry.forEachSubscription((sessionId, subscription) -> {
                if (subscription.isPattern()) {
                    if (pathMatcher.match(subscription.getDestination(), destination)) {
                        addMatchedSubscriptionId(sessionIdToSubscriptionIds, sessionId, subscription.getId());
                    }
                } else if (destination.equals(subscription.getDestination())) {
                    addMatchedSubscriptionId(sessionIdToSubscriptionIds, sessionId, subscription.getId());
                }
            });
            return sessionIdToSubscriptionIds;
        }

        private void addMatchedSubscriptionId(LinkedMultiValueMap<String, String> sessionIdToSubscriptionIds,
            String sessionId, String subscriptionId) {

            sessionIdToSubscriptionIds.compute(sessionId, (key, subscriptionIds) -> {
                if (subscriptionIds == null) {
                    return Collections.singletonList(subscriptionId);
                } else {
                    List<String> result = new ArrayList<>(subscriptionIds.size() + 1);
                    result.addAll(subscriptionIds);
                    result.add(subscriptionId);
                    return result;
                }
            });
        }

        private void ensureCacheLimit() {
            int size = this.cacheSize.get();
            if (size > cacheLimit) {
                do {
                    if (this.cacheSize.compareAndSet(size, size - 1)) {
                        // Remove (vs poll): we expect an element
                        String head = this.cacheEvictionPolicy.remove();
                        this.destinationMap.remove(head);
                    }
                } while ((size = this.cacheSize.get()) > cacheLimit);
            }
        }

        public void updateAfterNewSubscription(String sessionId, Subscription subscription) {
            if (subscription.isPattern()) {
                for (String cachedDestination : this.destinationMap.keySet()) {
                    if (pathMatcher.match(subscription.getDestination(), cachedDestination)) {
                        addToDestination(cachedDestination, sessionId, subscription.getId());
                    }
                }
            } else {
                addToDestination(subscription.getDestination(), sessionId, subscription.getId());
            }
        }

        private void addToDestination(String destination, String sessionId, String subscriptionId) {
            this.destinationMap.computeIfPresent(destination, (key, sessionIdToSubscriptionIds) -> {
                sessionIdToSubscriptionIds = sessionIdToSubscriptionIds.clone();
                addMatchedSubscriptionId(sessionIdToSubscriptionIds, sessionId, subscriptionId);
                return sessionIdToSubscriptionIds;
            });
        }

        public void updateAfterRemovedSubscription(String sessionId, Subscription subscription) {
            if (subscription.isPattern()) {
                String subscriptionId = subscription.getId();
                this.destinationMap.forEach((destination, sessionIdToSubscriptionIds) -> {
                    List<String> subscriptionIds = sessionIdToSubscriptionIds.get(sessionId);
                    if (subscriptionIds != null && subscriptionIds.contains(subscriptionId)) {
                        removeInternal(destination, sessionId, subscriptionId);
                    }
                });
            } else {
                removeInternal(subscription.getDestination(), sessionId, subscription.getId());
            }
        }

        private void removeInternal(String destination, String sessionId, String subscriptionId) {
            this.destinationMap.computeIfPresent(destination, (key, sessionIdToSubscriptionIds) -> {
                sessionIdToSubscriptionIds = sessionIdToSubscriptionIds.clone();
                sessionIdToSubscriptionIds.computeIfPresent(sessionId, (mapKey, subscriptionIds) -> {
                    /* Most likely case: single subscription per destination per session. */
                    if (subscriptionIds.size() == 1 && subscriptionId.equals(subscriptionIds.get(0))) {
                        return null;
                    }
                    subscriptionIds = new ArrayList<>(subscriptionIds);
                    subscriptionIds.remove(subscriptionId);
                    return (subscriptionIds.isEmpty() ? null : subscriptionIds);
                });
                return sessionIdToSubscriptionIds;
            });
        }

        public void updateAfterRemovedSession(String sessionId, SessionInfo info) {
            for (Subscription subscription : info.getSubscriptions()) {
                updateAfterRemovedSubscription(sessionId, subscription);
            }
        }

        @Override
        public String toString() {
            return "cache[" + this.cacheSize.toString() + " destination(s)]";
        }
    }

    /**
     * Registry for all session and their subscriptions.
     */
    private static final class SessionRegistry {

        private final ConcurrentMap<String, SessionInfo> sessions = new ConcurrentHashMap<>();

        @Nullable
        public SessionInfo getSession(String sessionId) {
            return this.sessions.get(sessionId);
        }

        public void forEachSubscription(BiConsumer<String, Subscription> consumer) {
            this.sessions.forEach((sessionId, info) ->
                info.getSubscriptions().forEach(subscription -> consumer.accept(sessionId, subscription)));
        }

        public void addSubscription(String sessionId, Subscription subscription) {
            SessionInfo info = this.sessions.computeIfAbsent(sessionId, key -> new SessionInfo());
            info.addSubscription(subscription);
        }

        @Nullable
        public SessionInfo removeSubscriptions(String sessionId) {
            return this.sessions.remove(sessionId);
        }

        @Override
        public String toString() {
            return "registry[" + this.sessions.size() + " sessions]";
        }
    }

    /**
     * Container for the subscriptions of a session.
     */
    private static final class SessionInfo {

        // subscriptionId -> Subscription
        private final Map<String, Subscription> subscriptionMap = new ConcurrentHashMap<>();

        public Collection<Subscription> getSubscriptions() {
            return this.subscriptionMap.values();
        }

        @Nullable
        public Subscription getSubscription(String subscriptionId) {
            return this.subscriptionMap.get(subscriptionId);
        }

        public void addSubscription(Subscription subscription) {
            this.subscriptionMap.putIfAbsent(subscription.getId(), subscription);
        }

        @Nullable
        public Subscription removeSubscription(String subscriptionId) {
            return this.subscriptionMap.remove(subscriptionId);
        }
    }

    /**
     * Represents a subscription.
     */
    private static final class Subscription {

        private final String id;

        private final String destination;

        private final boolean isPattern;

        @Nullable
        private final Expression selector;

        public Subscription(String id, String destination, boolean isPattern, @Nullable Expression selector) {
            Assert.notNull(id, "Subscription id must not be null");
            Assert.notNull(destination, "Subscription destination must not be null");
            this.id = id;
            this.selector = selector;
            this.destination = destination;
            this.isPattern = isPattern;
        }

        public String getId() {
            return this.id;
        }

        public String getDestination() {
            return this.destination;
        }

        public boolean isPattern() {
            return this.isPattern;
        }

        @Nullable
        public Expression getSelector() {
            return this.selector;
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return (this == other || (other instanceof Subscription && this.id.equals(((Subscription) other).id)));
        }

        @Override
        public int hashCode() {
            return this.id.hashCode();
        }

        @Override
        public String toString() {
            return "subscription(id=" + this.id + ")";
        }
    }

    private static class SimpMessageHeaderPropertyAccessor implements PropertyAccessor {

        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return new Class<?>[] { Message.class, MessageHeaders.class };
        }

        @Override
        public boolean canRead(EvaluationContext context, @Nullable Object target, String name) {
            return true;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public TypedValue read(EvaluationContext context, @Nullable Object target, String name) {
            Object value;
            if (target instanceof Message) {
                value = name.equals("headers") ? ((Message) target).getHeaders() : null;
            } else if (target instanceof MessageHeaders) {
                MessageHeaders headers = (MessageHeaders) target;
                SimpMessageHeaderAccessor accessor =
                    MessageHeaderAccessor.getAccessor(headers, SimpMessageHeaderAccessor.class);
                Assert.state(accessor != null, "No SimpMessageHeaderAccessor");
                if ("destination".equalsIgnoreCase(name)) {
                    value = accessor.getDestination();
                } else {
                    value = accessor.getFirstNativeHeader(name);
                    if (value == null) {
                        value = headers.get(name);
                    }
                }
            } else {
                // Should never happen...
                throw new IllegalStateException("Expected Message or MessageHeaders.");
            }
            return new TypedValue(value);
        }

        @Override
        public boolean canWrite(EvaluationContext context, @Nullable Object target, String name) {
            return false;
        }

        @Override
        public void write(EvaluationContext context, @Nullable Object target, String name, @Nullable Object value) {
            //Not writable
        }
    }
}
