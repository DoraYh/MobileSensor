package com.qianzuncheng.sensors.Network;

import android.content.Context;

import com.qianzuncheng.sensors.Storage.Cache;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class UploadQueue {
    private volatile static UploadQueue uniqueInstance;
    private Context context;
    private LinkedList<Cache.FilePack> allFilesQueue;
    private PriorityQueue<Cache.FilePack> allFilesPriorityQueue; // small files first
    private AutoSortArrayList<Cache.FilePack> allFilesSortedInSize; // small files first, auto sort
    private AutoSortArrayList<Float> bandwidth; // auto sort
    private int strategyCode = UploadService.STRATEGY.FCFS_CODE;
    private int lastBandwidthIndex = 0;

    private Comparator fileSizeComparator = new Comparator<Cache.FilePack>() {
        @Override
        public int compare(Cache.FilePack filePack, Cache.FilePack t1) {
            File f1 = new File(filePack.filePath);
            File f2 = new File(t1.filePath);
            return (int) (f1.length() - f2.length());
        }
    };

    private class AutoSortArrayList<T> extends ArrayList<T> {
        Comparator<T> comparator;
        public AutoSortArrayList(Comparator<T> c) {
            super();
            comparator = c;
        }
        public int autoSortAdd(T item) {
            // if the data is indeed sorted, the method will return the index of
            // the sought element (if it's found) otherwise (-(insertion point) - 1)
            int index = Collections.binarySearch(this, item, comparator);
            if (index < 0) index = ~index;
            super.add(index, item);
            return index;
        }
    }

    private UploadQueue(Context context) {
        this.context = context;
        restore();
        searchAndAddUploadableFiles();
    }
    // double check lock (DLC)
    public static UploadQueue getInstance(Context context) {
        if(uniqueInstance == null) {
            synchronized (UploadQueue.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new UploadQueue(context);
                }
            }
        }
        return uniqueInstance;
    }

    public synchronized void setStrategy(int strategyCode) {
        this.strategyCode = strategyCode;
    }

    public synchronized void addBandwidthSample(float bw) {
        lastBandwidthIndex = bandwidth.autoSortAdd(bw);
    }

    private synchronized void restore() {
        allFilesQueue = new LinkedList<>();
        allFilesPriorityQueue = new PriorityQueue<Cache.FilePack>(10, fileSizeComparator);
        bandwidth = new AutoSortArrayList<>(new Comparator<Float>() {
            @Override
            public int compare(Float aFloat, Float t1) {
                if (aFloat < t1) return -1;
                if (aFloat > t1) return 1;
                return 0;
            }
        });
        allFilesSortedInSize = new AutoSortArrayList<>(fileSizeComparator);

        String uploadQueueStr = Cache.getInstance(context).read(Cache.CATEGORY.UPLOAD_QUEUE);
        if(uploadQueueStr == null) return;
        String[] filePackStr = uploadQueueStr.split("\n");
        for (String fps : filePackStr) {
            String filePath, fileCategory;
            try {
                filePath = fps.split("\t")[0];
                fileCategory = fps.split("\t")[1];
            } catch (IndexOutOfBoundsException e) {
                continue;
            }
            File file = new File(filePath);
            if(file.length() > 0) {
                add(new Cache.FilePack(filePath, fileCategory));
            } else {
                // for those files whose size is 0, normally this shouldn't happen
                Cache.getInstance(context).clear(file);
            }
        }
        Cache.getInstance(context).clear(Cache.CATEGORY.UPLOAD_QUEUE);
    }

    // find those files last modified >=24 hours ago and not in queue
    // (which maybe caused by app crash) and add them to queue
    private void searchAndAddUploadableFiles() {
        for (String category : Cache.CATEGORY.allUploadCategory()) {
            String[] filePaths = Cache.getInstance(context).getFilePaths(category);
            if(filePaths == null) continue;

            for (String fp : filePaths) {
                File file = new File(fp);

                // time restrictions
                long lastModifiedTime = file.lastModified();
                if(lastModifiedTime == 0 /* does not exist or IO error */) {
                    continue;
                }
                if(lastModifiedTime - System.currentTimeMillis() > 24 * 60 * 60 * 1000) {
                    // find >=24 hours ago
                    continue;
                }

                // if already in queue
                boolean checkFlag = false;
                for(Cache.FilePack inQueueFilePack: allFilesQueue) {
                    if(inQueueFilePack.filePath.equals(fp)) {
                        checkFlag = true;
                        break;
                    }
                }
                if(checkFlag) {
                    continue;
                }

                // if broken fie
                if(file.length() > 0) {
                    add(new Cache.FilePack(fp, category));
                } else {
                    // for those files whose size is 0, normally this shouldn't happen
                    Cache.getInstance(context).clear(file);
                }
            }
        }
    }

    public synchronized void save() {
        Cache cache = Cache.getInstance(context);
        cache.clear(Cache.CATEGORY.UPLOAD_QUEUE);
        //while(!allFilesQueue.isEmpty()) {
        for(Cache.FilePack filePack: allFilesQueue) { // need to maintain this for background service
            //Cache.FilePack filePack = allFilesQueue.poll();
            cache.append(Cache.CATEGORY.UPLOAD_QUEUE, filePack);
        }
    }

    public synchronized void add(Cache.FilePack filePack) {
        allFilesQueue.offer(filePack);
        allFilesPriorityQueue.offer(filePack);
        allFilesSortedInSize.autoSortAdd(filePack);
    }

    public synchronized Cache.FilePack pollByDefault() {
        Cache.FilePack fp = allFilesQueue.poll();
        if(fp == null) return null;
        // remove from priority queue
        for(Iterator<Cache.FilePack> iter = allFilesPriorityQueue.iterator(); iter.hasNext(); ) {
            Cache.FilePack i = iter.next();
            if(fp.filePath.equals(i.filePath)) {
                iter.remove();
            }
        }
        // remove from size array
        for(Iterator<Cache.FilePack> iter = allFilesSortedInSize.iterator(); iter.hasNext(); ) {
            Cache.FilePack i = iter.next();
            if(fp.filePath.equals(i.filePath)) {
                iter.remove();
            }
        }
        return fp;
    }

    public synchronized Cache.FilePack pollByPriority() {
        Cache.FilePack fp = allFilesPriorityQueue.poll();
        if(fp == null) return null;
        // remove from queue
        for(Iterator<Cache.FilePack> iter = allFilesQueue.iterator(); iter.hasNext(); ) {
            Cache.FilePack i = iter.next();
            if(fp.filePath.equals(i.filePath)) {
                iter.remove();
            }
        }
        // remove from size array
        for(Iterator<Cache.FilePack> iter = allFilesSortedInSize.iterator(); iter.hasNext(); ) {
            Cache.FilePack i = iter.next();
            if(fp.filePath.equals(i.filePath)) {
                iter.remove();
            }
        }
        return fp;
    }

    public synchronized Cache.FilePack pollByBandwidth() {
        if(allFilesSortedInSize.size() == 0) return null;
        // approximately algorithm with +1 (avoid divided by 0 error)
        int fileIndex = (int) (lastBandwidthIndex * 1.0 / (bandwidth.size() + 1) * allFilesSortedInSize.size());
        Cache.FilePack fp = allFilesSortedInSize.get(fileIndex);
        allFilesSortedInSize.remove(fileIndex);
        // remove from priority queue
        for(Iterator<Cache.FilePack> iter = allFilesPriorityQueue.iterator(); iter.hasNext(); ) {
            Cache.FilePack i = iter.next();
            if(fp.filePath.equals(i.filePath)) {
                iter.remove();
            }
        }
        // remove from queue
        for(Iterator<Cache.FilePack> iter = allFilesQueue.iterator(); iter.hasNext(); ) {
            Cache.FilePack i = iter.next();
            if(fp.filePath.equals(i.filePath)) {
                iter.remove();
            }
        }
        return fp;
    }

    public synchronized Cache.FilePack getNext() {
        switch (strategyCode) {
            case UploadService.STRATEGY.FCFS_CODE:
                return pollByDefault();
            case UploadService.STRATEGY.SFF_CODE:
                return pollByPriority();
            case UploadService.STRATEGY.FB_CODE:
                return pollByBandwidth();
        }
        return null;
    }
}
