package lession03;

import java.util.LinkedList;
import java.util.List;

public class FSEditLog02 {

    private Long txid = 0L;
    private DoubleBuffer doubleBuffer = new DoubleBuffer();

    private volatile Boolean isSyncRunning = false;
    private volatile Boolean isWaitSync = false;
    private volatile Long syncMaxTxid = 0L;
    private ThreadLocal<Long> localTxid = new ThreadLocal<Long>();


    public void logEdit(String context) {
        synchronized (this) {
            txid++;
            localTxid.set(txid);
            EditLog log = new EditLog(txid, context);
            doubleBuffer.writer(log);
        }
        logSync();
    }

    private void logSync() {
        synchronized (this) {
            if (isSyncRunning) {
                long txid = localTxid.get();
                if (txid <= syncMaxTxid) {
                    return;
                }

                if (isWaitSync) {
                    return;
                }
                isWaitSync = true;

                while (isSyncRunning) {
                    try {
                        wait(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                isWaitSync = false;
            }
            doubleBuffer.setReadToSync();
            if (doubleBuffer.syncBuffer.size() > 0) {
                syncMaxTxid = doubleBuffer.getMaxSyncTxId();
            }
            isSyncRunning = true;
        }
        doubleBuffer.flush();

        synchronized (this) {
            isSyncRunning = false;
            notify();
        }

    }


    class EditLog {
        long txid;
        String context;

        public EditLog(Long txid, String context) {
            this.context = context;
            this.txid = txid;
        }

        @Override
        public String toString() {
            return "EditLog{" +
                    "txid=" + txid +
                    ", context='" + context + '\'' +
                    '}';
        }
    }


    class DoubleBuffer {
        LinkedList<EditLog> curBuffer = new LinkedList<EditLog>();
        LinkedList<EditLog> syncBuffer = new LinkedList<EditLog>();

        public void writer(EditLog log) {
            curBuffer.add(log);
        }

        public void setReadToSync() {
            LinkedList<EditLog> temp = curBuffer;
            curBuffer = syncBuffer;
            syncBuffer = temp;
        }

        public Long getMaxSyncTxId() {
            return syncBuffer.getLast().txid;
        }

        public void flush() {
            for (EditLog log : syncBuffer) {
                System.out.println("日志信息: " + log);
            }
            syncBuffer.clear();
        }


    }
}
