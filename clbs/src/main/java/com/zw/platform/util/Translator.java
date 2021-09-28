package com.zw.platform.util;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 翻译器
 *
 * @param <B> 业务值类型
 * @param <P> 产品值类型
 * @author Zhang Yanhui
 * @since 2019/7/3 17:44
 */

public final class Translator<B, P> {

    private final List<Pair<B, P>> dict;

    private Translator(List<Pair<B, P>> dict) {
        this.dict = dict;
    }

    /**
     * 获取翻译器builder
     */
    @NonNull
    public static <B, P> Builder<B, P> builder() {
        return new Builder<>();
    }

    /**
     * 生成指定单词个数的翻译器
     */
    @NonNull
    public static <B, P> Translator<B, P> of(B b1, P p1) {
        return new Translator<>(ImmutableList.of(Pair.of(b1, p1)));
    }

    /**
     * 生成指定单词个数的翻译器
     */
    @NonNull
    public static <B, P> Translator<B, P> of(B b1, P p1, B b2, P p2) {
        return new Translator<>(ImmutableList.of(Pair.of(b1, p1), Pair.of(b2, p2)));
    }

    /**
     * 生成指定单词个数的翻译器
     */
    @NonNull
    public static <B, P> Translator<B, P> of(B b1, P p1, B b2, P p2, B b3, P p3) {
        return new Translator<>(ImmutableList.of(Pair.of(b1, p1), Pair.of(b2, p2), Pair.of(b3, p3)));
    }

    /**
     * 生成指定单词个数的翻译器
     */
    @NonNull
    public static <B, P> Translator<B, P> of(B b1, P p1, B b2, P p2, B b3, P p3, B b4, P p4) {
        return new Translator<>(
            ImmutableList.of(Pair.of(b1, p1), Pair.of(b2, p2), Pair.of(b3, p3), Pair.of(b4, p4)));
    }

    /**
     * 生成指定单词个数的翻译器
     */
    @NonNull
    public static <B, P> Translator<B, P> of(B b1, P p1, B b2, P p2, B b3, P p3, B b4, P p4, B b5, P p5) {
        return new Translator<>(
            ImmutableList.of(Pair.of(b1, p1), Pair.of(b2, p2), Pair.of(b3, p3), Pair.of(b4, p4), Pair.of(b5, p5)));
    }

    /**
     * 生成指定单词个数的翻译器
     */
    @NonNull
    public static <B, P> Translator<B, P> of(B b1, P p1, B b2, P p2, B b3, P p3, B b4, P p4, B b5, P p5, B b6, P p6) {
        return new Translator<>(ImmutableList
            .of(Pair.of(b1, p1), Pair.of(b2, p2), Pair.of(b3, p3), Pair.of(b4, p4), Pair.of(b5, p5), Pair.of(b6, p6)));
    }

    /**
     * 生成指定单词个数的翻译器
     */
    @SafeVarargs
    public static <B, P> Translator<B, P> of(B b1, P p1, B b2, P p2, B b3, P p3, B b4, P p4, B b5, P p5, B b6, P p6,
        Pair<B, P>... others) {
        List<Pair<B, P>> counted = ImmutableList
            .of(Pair.of(b1, p1), Pair.of(b2, p2), Pair.of(b3, p3), Pair.of(b4, p4), Pair.of(b5, p5), Pair.of(b6, p6));
        List<Pair<B, P>> dict = new ArrayList<>(counted.size() + others.length);
        dict.addAll(counted);
        dict.addAll(ImmutableList.copyOf(others));
        return new Translator<>(dict);
    }

    /**
     * 由Iterable生成翻译器
     *
     * @param iterable 可迭代对象
     * @param fb       获取业务值的方法
     */
    public static <T, B> Translator<B, T> ofIterable(Iterable<T> iterable, Function<T, B> fb) {
        return ofIterable(iterable, fb, Function.identity());
    }

    /**
     * 由Iterable生成翻译器
     *
     * @param iterable 可迭代对象
     * @param fb       获取业务值的方法
     * @param fp       获取产品值的方法
     */
    public static <T, B, P> Translator<B, P> ofIterable(Iterable<T> iterable, Function<T, B> fb, Function<T, P> fp) {
        List<Pair<B, P>> dict = new ArrayList<>();
        iterable.forEach(o -> dict.add(Pair.of(fb.apply(o), fp.apply(o))));
        return new Translator<>(dict);
    }

    /**
     * 由数组生成翻译器
     *
     * @param ts 数组
     * @param fb 获取业务值的方法
     */
    public static <T, B> Translator<B, T> ofArray(T[] ts, Function<T, B> fb) {
        return ofArray(ts, fb, Function.identity());
    }

    /**
     * 由数组生成翻译器
     *
     * @param ts 数组
     * @param fb 获取业务值的方法
     * @param fp 获取产品值的方法
     */
    public static <T, B, P> Translator<B, P> ofArray(T[] ts, Function<T, B> fb, Function<T, P> fp) {
        List<Pair<B, P>> dict = new ArrayList<>(ts.length);
        for (T t : ts) {
            dict.add(Pair.of(fb.apply(t), fp.apply(t)));
        }
        return new Translator<>(dict);
    }

    /**
     * 业务值转产品值
     * <p>不存在时返回null</p>
     * <p>存在多个匹配时返回首个</p>
     *
     * @param businessValue 业务值
     * @return 产品值
     */
    public Optional<P> b2pOptional(B businessValue) {
        return Optional.ofNullable(this.b2p(businessValue));
    }

    /**
     * 业务值转产品值
     * <p>不存在时返回null</p>
     * <p>存在多个匹配时返回首个</p>
     *
     * @param businessValue 业务值
     * @return 产品值
     */
    public P b2p(B businessValue) {
        return this.b2p(businessValue, null);
    }

    /**
     * 业务值转产品值
     * <p>存在多个匹配时返回首个</p>
     *
     * @param businessValue       业务值
     * @param defaultProductValue 不存在时返回
     * @return 产品值
     */
    public P b2p(B businessValue, P defaultProductValue) {
        for (Pair<B, P> pair : dict) {
            if (pair.getFirst().equals(businessValue)) {
                return pair.getSecond();
            }
        }
        return defaultProductValue;
    }

    /**
     * 业务值转多个产品值
     * <p>不存在时返回emptyList()</p>
     *
     * @param businessValue 业务值
     * @return 产品值列表
     */
    public List<P> b2ps(B businessValue) {
        List<P> ps = new ArrayList<>();
        for (Pair<B, P> pair : dict) {
            if (pair.getFirst().equals(businessValue)) {
                ps.add(pair.getSecond());
            }
        }
        return ps;
    }

    /**
     * 产品值转业务值
     * <p>存在多个匹配时返回首个</p>
     *
     * @param productValue 产品值
     * @return 业务值
     */
    public Optional<B> p2bOptional(P productValue) {
        return Optional.ofNullable(this.p2b(productValue));
    }

    /**
     * 产品值转业务值
     * <p>不存在时返回null</p>
     * <p>存在多个匹配时返回首个</p>
     *
     * @param productValue 产品值
     * @return 业务值
     */
    public B p2b(P productValue) {
        return this.p2b(productValue, null);
    }

    /**
     * 产品值转业务值
     * <p>不存在时返回null</p>
     * <p>存在多个匹配时返回首个</p>
     *
     * @param productValue         产品值
     * @param defaultBusinessValue 不存在时返回
     * @return 业务值
     */
    public B p2b(P productValue, B defaultBusinessValue) {
        for (Pair<B, P> pair : dict) {
            if (pair.getSecond().equals(productValue)) {
                return pair.getFirst();
            }
        }
        return defaultBusinessValue;
    }

    /**
     * 产品值转多个业务值
     * <p>不存在时返回emptyList()</p>
     *
     * @param productValue 产品值
     * @return 业务值
     */
    public List<B> p2bs(P productValue) {
        List<B> bs = new ArrayList<>();
        for (Pair<B, P> pair : dict) {
            if (pair.getSecond().equals(productValue)) {
                bs.add(pair.getFirst());
            }
        }
        return bs;
    }

    /**
     * builder
     *
     * @param <B> 业务值类型
     * @param <P> 产品值类型
     */
    public static final class Builder<B, P> {
        private List<Pair<B, P>> dict = new ArrayList<>();

        private Builder() {
        }

        /**
         * builder方法
         *
         * @param b 业务值
         * @param p 产品值
         * @return builder
         */
        public Builder<B, P> add(B b, P p) {
            dict.add(Pair.of(b, p));
            return this;
        }

        /**
         * 构建Translator
         *
         * @return Translator
         */
        public Translator<B, P> build() {
            return new Translator<>(dict);
        }
    }

    /**
     * 实现Pair，代码来自org.springframework.data.util.Pair
     *
     * @param <S> 左边类型
     * @param <T> 右边类型
     */
    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Pair<S, T> {

        private final @NonNull S first;
        private final @NonNull T second;

        /**
         * Creates a new {@link Pair} for the given elements.
         *
         * @param first  must not be {@literal null}.
         * @param second must not be {@literal null}.
         */
        public static <S, T> Pair<S, T> of(S first, T second) {
            return new Pair<>(first, second);
        }

        /**
         * Returns the first element of the {@link Pair}.
         */
        public S getFirst() {
            return first;
        }

        /**
         * Returns the second element of the {@link Pair}.
         */
        public T getSecond() {
            return second;
        }
    }
}
