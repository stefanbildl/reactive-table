package com.stefanbildl;

import io.reactivex.Emitter;
import io.reactivex.FlowableSubscriber;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.reactivestreams.Subscription;

import java.util.*;
import java.util.List;

/**
 */
public abstract class ReactiveTable <T> extends Composite implements FlowableSubscriber<T> {
    private List<Emitter<T>> emitters = Collections.synchronizedList(new ArrayList<>());
    protected Table table;
    private LinkedList<T> cache = new LinkedList<>();
    private int startIndex;
    private int itemCount;

    private Subscription subscription;
    private TreeSet<Integer> alreadyRequested = new TreeSet<>();

    private boolean completed;
    private Throwable loadThrowable;
    private boolean broken;

    public static final int RELOAD_THRESHOLD_DEFAULT = 50;
    public static final int RELOAD_SIZE_DEFAULT = 200;

    private final int reloadThreshold;
    private final int reloadSize;
    private int requestPendingCounter;

    /**
     * Constructs a new instance of this class given its parent.
     */
    public ReactiveTable(Composite parent, int style, Integer reloadThreshold, Integer reloadSize) throws Throwable {
        super(parent, style);

        if(reloadThreshold != null && reloadThreshold != 0)
            this.reloadThreshold = reloadThreshold;
        else
            this.reloadThreshold = RELOAD_THRESHOLD_DEFAULT;
        
        if(reloadSize != null && reloadSize != 0)
            this.reloadSize = reloadSize;
        else
            this.reloadSize = RELOAD_SIZE_DEFAULT;

        setLayout(new FillLayout());

        table = new Table(this, style | SWT.VIRTUAL | SWT.SCROLL_PAGE);

        table.addListener(SWT.SetData, new Listener() {
            @Override
            public void handleEvent(Event event) {
                final TableItem item = (TableItem) event.item;

                T data = null;
                synchronized (cache) {
                    int requested = event.index - startIndex;
                    data = cache.get(requested);
                    if(requested == 0) {
                        cache.poll();
                        startIndex++;
                        while(alreadyRequested.size() > 0 && alreadyRequested.first() == startIndex) {
                            cache.poll();
                            startIndex++;
                            alreadyRequested.remove(startIndex-1);
                        }
                    }
                    else {
                        alreadyRequested.add(event.index);
                    }

                }
                if(data == null) {
                    event.doit = false;
                    return;
                }

                if(requestPendingCounter <= 0) {
                    if(event.index >= (itemCount - ReactiveTable.this.reloadThreshold))
                        request();
                }


                item.setData(data);
                fillItem(item, (T) data);
            }
        });

        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        // table created

        init();
    }

    protected void init() throws Throwable {
        createHeader(table);
    }


    private void request() {
        if(subscription != null) {
            if(requestPendingCounter<=0) {
                requestPendingCounter = reloadSize;
                subscription.request(reloadSize);
            }
        }
        else
            throw new IllegalStateException("Must subscribe to a Flowable first!");
    }


    public ReactiveTable(Composite parent, int style) throws Throwable {
        this(parent, style, null, null);
    }
    
    public ReactiveTable(Composite parent, int style, int reloadThreshold, int reloadSize) throws Throwable {
        this(parent, style, null, null);
    }

    public ReactiveTable<T> clearTableBody() {
        synchronized (cache) {
            table.setItemCount(0);
            table.removeAll();
            requestPendingCounter = 0;
            cache.clear();
            alreadyRequested.clear();
            startIndex = 0;
            broken = false;
            completed = false;
            itemCount = 0;
        }
        return this;
    }

    @Override
    public void onSubscribe(Subscription s) {
        if(this.subscription != null) {
            this.subscription.cancel();
        }
        this.subscription = s;
        clearTableBody();
        request();
    }

    @Override
    public void onNext(T t) {
        synchronized (cache) {
            cache.add(t);
            itemCount++;
            requestPendingCounter--;
            table.setItemCount(itemCount);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        broken = true;
        loadThrowable = throwable;
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        completed = true;
    }

    /**
     * Abstract method for creating the header line in the table
     * @throws Throwable if creating failed
     * @param table
     */
    protected abstract void createHeader(Table table) throws Throwable;
    protected abstract void fillItem(TableItem item, T element);

    public int getItemCount() {
        return itemCount;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isBroken() {
        return broken;
    }

    public int getReloadSize() {
        return reloadSize;
    }

    public Throwable getLoadThrowable() {
        return loadThrowable;
    }

    public Table getTable() {
        return table;
    }
}
