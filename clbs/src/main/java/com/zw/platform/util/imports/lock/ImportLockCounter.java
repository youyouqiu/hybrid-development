package com.zw.platform.util.imports.lock;

import com.zw.platform.util.imports.ZwImportException;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.zw.platform.util.imports.lock.ImportModule.PROFESSIONAL;

/**
 * 信息配置先关导入锁
 * @author create by zhouzongbo on 2020/8/28.
 */
public class ImportLockCounter implements Serializable {

    /**
     * 序列化
     */
    private static final long serialVersionUID = -8705573076241125982L;

    /**
     * 模块 -> 关联模块
     */
    private static final Map<ImportType, List<ImportType>> IMPORT_TYPE_MAP;

    /**
     * 模块or表(key) -> 原子类(value). 这里的原子类值只会是0或则1
     */
    private static final Map<ImportType, ImportTypeCache> ATOMIC_CACHE = new ConcurrentHashMap<>(16);

    /**
     * 平台导入写锁
     * 由于lock() 和 unLock()方法都会涉及到修改相应的AtomicInteger, 所以这两个方法不能同时进行.
     */
    private final Lock writeLock = new ReentrantLock();

    static {
        // 1.初始化模块之间的关联关系
        IMPORT_TYPE_MAP = new ConcurrentHashMap<>(32);
        IMPORT_TYPE_MAP.put(ImportModule.CONFIG, Arrays.asList(ImportModule.values()));
        IMPORT_TYPE_MAP.put(ImportModule.VEHICLE, Arrays.asList(ImportModule.VEHICLE, ImportModule.CONFIG));
        IMPORT_TYPE_MAP.put(ImportModule.PEOPLE, Arrays.asList(ImportModule.PEOPLE, ImportModule.CONFIG));
        IMPORT_TYPE_MAP.put(ImportModule.THING, Arrays.asList(ImportModule.THING, ImportModule.CONFIG));
        IMPORT_TYPE_MAP.put(ImportModule.DEVICE, Arrays.asList(ImportModule.DEVICE, ImportModule.CONFIG));
        IMPORT_TYPE_MAP.put(ImportModule.SIM_CARD, Arrays.asList(ImportModule.SIM_CARD, ImportModule.CONFIG));
        IMPORT_TYPE_MAP.put(ImportModule.ASSIGNMENT, Arrays.asList(ImportModule.ASSIGNMENT, ImportModule.CONFIG));
        IMPORT_TYPE_MAP.put(PROFESSIONAL, Arrays.asList(PROFESSIONAL, ImportModule.CONFIG));

        // 2.初始化表的关联关系, 暂时自己与自己关联
        for (ImportType value : ImportTable.values()) {
            IMPORT_TYPE_MAP.put(value, Collections.singletonList(value));
        }
    }

    /**
     * 因为getInstance()使用的是双重锁, 未了避免并发场景下延迟初始化的优化问题隐患, 因为加上了volatile
     */
    private static volatile ImportLockCounter importLock = null;

    private ImportLockCounter() {

    }

    /**
     * 初始化导入锁类 double check 保证全局唯一性
     * double-checked locking 在并发场景下存在延迟初始化的优化问题隐患(可参考 The "Double-Checked Locking is Broken" Declaration),
     * 1.推荐解决方案中较为简单的一种, 将ImportLockCounter 属性声明为volatile, 或则直接在getInstance方法上加上synchronized关键字
     * 2.使用内部类初始化
     * 3.方法上加锁
     * @return ImportLock
     */
    public static ImportLockCounter getInstance() {
        if (importLock == null) {
            synchronized (ImportLockCounter.class) {
                if (importLock == null) {
                    importLock = new ImportLockCounter();
                }
            }
        }
        return importLock;
    }

    /**
     * 导入加锁:
     * 1.判断当前模块是否正在导入(count > 0)
     * 1.1 count > 0, 有其他用户正在导入, 不允许导入
     * 1.2 count = 0, 可以到导入, 自己的值设置为 1
     * @param types 导入类型
     */
    public void lock(ImportType... types) {
        writeLock.lock();
        try {
            isLock(types);
            incrementModule(types);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 导入完成(成功 or 失败) 释放锁
     * @param types 导入类型
     */
    public void unlock(ImportType... types) {
        writeLock.lock();
        try {
            decrementModule(types);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 主要用于校验模块是否被锁住, 如果模块被锁住, 自己也不允许操作
     * <pre>
     * _______________________________
     * 维护各个模块之前的关联关系.
     * 这里一共有7个关联模块, 其中"×"表示模块之间有依赖关系, 不能同时导入
     *          信息列表   车辆管理  人员信息  物品信息  SIM卡管理 分组管理 从业人员管理
     * 信息列表:   ×          ×        ×         ×       ×       ×        ×
     * 车辆管理:   ×          ×
     * 人员信息:   ×                   ×
     * 物品信息:   ×                             ×
     * SIM卡管理:  ×                                     ×
     * 从业人员:   ×                                             ×
     * 分组管理:   ×                                                      ×
     * ________________________________
     * 模块是否被锁住(逻辑锁)
     * ps: 锁住不允许操作该模块的导入功能
     * </pre>
     * @param types 导入类型
     */
    public void isLock(ImportType... types) {
        for (ImportType type : types) {
            int lockCount = 0;
            final List<ImportType> importTypes = IMPORT_TYPE_MAP.get(type);
            if (CollectionUtils.isEmpty(importTypes)) {
                continue;
            }

            // 检查与自己关联的模块相加值是否大于0, 如果大于0, 说明该模块有人正在执行导入
            for (ImportType module : importTypes) {
                ImportTypeCache cache = ATOMIC_CACHE.get(module);
                if (cache == null) {
                    continue;
                }
                final AtomicInteger atomicVal = cache.getAtomicInteger();
                if (atomicVal != null) {
                    lockCount += atomicVal.get();
                }
            }

            if (lockCount > 0) {
                throw new ZwImportException(String.format("【%s】模块, 其他用户正在操作，为保证数据可靠性，请稍后操作！", type.value()));
            }
        }
    }

    /**
     * 校验表是否被锁住, 表锁住了, 自己可以操作, 其他用户不允许操作
     * @param importTable importTable
     */
    public void checkTableLock(ImportTable importTable) {
        writeLock.lock();
        try {
            Optional.ofNullable(ATOMIC_CACHE.get(importTable)).ifPresent(cache -> {
                final AtomicInteger atomicVal = cache.getAtomicInteger();
                if (atomicVal.get() > 0 && cache.getExclusiveOwnerThread() != Thread.currentThread()) {
                    throw new ZwImportException("其他用户正在操作相关数据表，为保证数据可靠性，请稍后操作！");
                }
            });
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 模块的值 = 1
     * @param types 导入类型
     */
    private void incrementModule(ImportType... types) {
        for (ImportType type : types) {
            ATOMIC_CACHE.computeIfAbsent(type, k -> new ImportTypeCache()).setAtomicInteger(new AtomicInteger(1));
        }
    }

    /**
     * 模块的值 = 0
     * @param types 导入类型
     */
    private void decrementModule(ImportType... types) {
        for (ImportType type : types) {
            ATOMIC_CACHE.remove(type);
        }
    }

    /**
     * 导入类型缓存信息
     */
    private static class ImportTypeCache {

        /**
         * 原子类型值: 0 或则 1
         */
        private AtomicInteger atomicInteger;

        /**
         * 当前线程信息
         * 用途: 如果锁定了表, 只有当前线程可以对这张表进行新增修改的操作
         */
        private transient Thread exclusiveOwnerThread;

        public ImportTypeCache() {
            atomicInteger = new AtomicInteger(1);
            exclusiveOwnerThread = Thread.currentThread();
        }

        public AtomicInteger getAtomicInteger() {
            return atomicInteger;
        }

        public void setAtomicInteger(AtomicInteger atomicInteger) {
            this.atomicInteger = atomicInteger;
        }

        public Thread getExclusiveOwnerThread() {
            return exclusiveOwnerThread;
        }

        public void setExclusiveOwnerThread(Thread exclusiveOwnerThread) {
            this.exclusiveOwnerThread = exclusiveOwnerThread;
        }
    }
}
