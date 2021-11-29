package  com.pangu.logic.utils;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 线程安全的排行榜
 * 实体使用不建议直接回写数据库
 */
public class RankList {

    //排行榜
    private ArrayList<RankItem> rank;

    public RankList() {
        rank = new ArrayList<>();
    }

    public RankList(int initSize) {
        this.rank = new ArrayList<>(initSize);
    }

    public RankList(ArrayList<RankItem> rank) {
        this.rank = rank;
    }

    private final transient ReadWriteLock lock = new ReentrantReadWriteLock();

    //清空并返回当前排行榜信息
    public List<RankItem> clear() {
        lock.writeLock().lock();
        try {
            ArrayList<RankItem> lasts = new ArrayList<>(rank);
            rank.clear();
            return lasts;
        } finally {
            lock.writeLock().unlock();
        }
    }

    //添加排行榜
    public void add(RankItem item, int limit) {
        lock.writeLock().lock();
        try {
            int index = rank.indexOf(item);
            if (index >= 0) {
                // 添加
                rank.remove(index);
            }
            int addTo = binarySearch(rank, item);
            if (addTo < 0) {
                rank.add(item);
            } else {
                rank.add(addTo, item);
            }

            // 截断
            while (rank.size() > limit) {
                rank.remove(rank.size() - 1);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void merge(List<RankItem> rankItems) {
        lock.writeLock().lock();
        try {
            rank.addAll(rankItems);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void replaceRank(List<RankItem> rankItems) {
        lock.writeLock().lock();
        try {
            this.rank = new ArrayList<>(rankItems);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeUntilLimit(int limit) {
        lock.writeLock().lock();
        try {
            while (rank.size() > limit) {
                rank.remove(rank.size() - 1);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void remove(RankItem item) {
        lock.writeLock().lock();
        try {
            int index = rank.indexOf(item);
            if (index >= 0) {
                rank.remove(index);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    //获取排行位置
    public int indexOf(long owner) {
        RankItem item = createRankItem(owner, 0);
        lock.readLock().lock();
        try {
            return rank.indexOf(item);
        } finally {
            lock.readLock().unlock();
        }
    }

    public RankInfo getRankInfo(long owner) {
        RankItem item = createRankItem(owner, 0);
        lock.readLock().lock();
        try {
            final int index = rank.indexOf(item);
            if (index < 0) {
                return null;
            }
            final RankItem rankItem = rank.get(index);
            return RankInfo.of(index, rankItem.getValue(), rankItem.additions);
        } finally {
            lock.readLock().unlock();
        }
    }

    //获取指定排行位置
    public RankItem get(int index) {
        if (index < 0) {
            return null;
        }
        lock.readLock().lock();
        try {
            if (index >= rank.size()) {
                return null;
            }
            return rank.get(index);
        } finally {
            lock.readLock().unlock();
        }
    }

    //获取子列表
    public List<RankItem> subList(int fromIndex, int toIndex) {
        lock.readLock().lock();
        try {
            int size = rank.size();
            if (fromIndex < 0) {
                // 防止subList前端越界
                fromIndex = 0;
            }
            if (fromIndex > size - 1) {
                return Collections.emptyList();
            }
            int to = toIndex + 1;
            if (to > size) {
                to = size;
            }
            return new ArrayList<>(rank.subList(fromIndex, to));
        } finally {
            lock.readLock().unlock();
        }
    }

    //获取所有排行榜
    public List<RankItem> getRank() {
        lock.readLock().lock();
        try {
            List<RankItem> result = new ArrayList<>(rank);
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    //查找指定区间值的所有RankItem对象
    public List<RankItem> findByInterval(long from, long to) {
        lock.readLock().lock();
        try {
            RankItem min = createRankItem(0, Math.max(from, to));
            RankItem max = createRankItem(0, Math.min(from, to));
            int minIndex = binarySearch(rank, min);
            int maxIndex = binarySearch(rank, max);
            return subList(minIndex, maxIndex);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int size() {
        return rank.size();
    }

    public static RankItem createRankItem(long id, long value) {
        RankItem e = new RankItem();
        e.id = id;
        e.value = value;
        return e;
    }

    public static RankItem createRankItem(long id, long value, long addTime, long[] additions) {
        RankItem e = new RankItem();
        e.id = id;
        e.value = value;
        e.additions = additions;
        e.addTime = addTime;
        return e;
    }

    public static RankItem createRankItem(long id, long value, long addTime) {
        RankItem e = new RankItem();
        e.id = id;
        e.value = value;
        e.addTime = addTime;
        return e;
    }

    private static <T> int binarySearch(List<? extends Comparable<? super T>> list, T key) {
        int low = 0;
        int high = list.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            Comparable<? super T> midVal = list.get(mid);
            int cmp = midVal.compareTo(key);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return low; // key not found
    }

    @Getter
    @Transable
    public static class RankInfo {
        //当前排行下标
        private int index;

        //当前值
        private long value;

        private long[] additions;

        public static RankInfo of(int index, long value, long[] additions) {
            RankInfo rankInfo = new RankInfo();
            rankInfo.index = index;
            rankInfo.value = value;
            rankInfo.additions = additions;
            return rankInfo;
        }
    }

    /**
     * 排行榜对象
     *
     * @author author
     */
    @Getter
    @Setter
    @Transable
    @ToString
    public static class RankItem implements Comparable<RankItem> {

        /**
         * playerId
         */
        private long id;

        /**
         * value 值
         */
        private long value;

        /**
         * addTime 添加时间
         */
        private long addTime;

        /**
         * 附加信息
         */
        private long[] additions;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (id ^ (id >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            RankItem other = (RankItem) obj;
            return id == other.id;
        }

        @Override
        public int compareTo(RankItem o) {
            // 倒序
            CompareToBuilder compare = new CompareToBuilder().append(o.value, value).append(addTime, o.addTime);

            return compare.append(o.id, id).toComparison();
        }
    }

}
