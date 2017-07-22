package com.stefanbildl;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by stefan on 09.07.17.
 */
public abstract class FilterableReactiveTable<T> extends ReactiveTable<T> implements ObservableOnSubscribe<FilterInfo> {
    private final FilterBasedFlowableFactory<T> flowableFactory;
    private List<ObservableEmitter<FilterInfo>> emitterList = Collections.synchronizedList(new ArrayList<ObservableEmitter<FilterInfo>>());
    private Subscription subscription;

    public FilterableReactiveTable(Composite parent, int style, FilterBasedFlowableFactory<T> flowableFactory) throws Throwable {
        super(parent, style);
        this.flowableFactory = flowableFactory;
        recreateFlowable(new FilterInfo(true, null));
    }

    public FilterableReactiveTable(Composite parent, int style, Integer reloadThreshold, Integer reloadSize, FilterBasedFlowableFactory<T> flowableFactory) throws Throwable {
        super(parent, style, reloadThreshold, reloadSize);
        this.flowableFactory = flowableFactory;
        recreateFlowable(new FilterInfo(true, null));
    }

    @Override
    protected void createHeader(Table table) throws Throwable {
        Listener sortListener = new Listener() {
            @Override
            public void handleEvent(Event event) {
                TableColumn selectedColumn = (TableColumn) event.widget;
                if(table.getSortColumn() == selectedColumn) {
                    table.setSortDirection(table.getSortDirection() == SWT.UP? SWT.DOWN : SWT.UP);
                }
                else {
                    table.setSortColumn((TableColumn) event.widget);
                    table.setSortDirection(SWT.UP);
                }
                FilterColumn column = ((FilterColumn)table.getSortColumn().getData());
                onFilterChanged(getFilterInfo());
            }
        };

        Collection<? extends FilterColumn> filterColumns = getColumns();
        for(FilterColumn column : filterColumns) {
            TableColumn tableColumn = new TableColumn(table, SWT.LEAD);
            tableColumn.setData(column);
            tableColumn.setText(column.getLabel());
            tableColumn.setWidth(column.getWidth());
            tableColumn.addListener(SWT.Selection, sortListener);
        }
    }

    private void onFilterChanged(FilterInfo filterInfo) {
        clearTableBody();
        for(ObservableEmitter<FilterInfo> e: emitterList)
            e.onNext(filterInfo);

        subscription.cancel();
        recreateFlowable(filterInfo);
    }

    private void recreateFlowable(FilterInfo filterInfo) {
        flowableFactory.newFlowable(filterInfo).observeOn(Schedulers.from(new Executor() {
            @Override
            public void execute(Runnable command) {
                getDisplay().asyncExec(command);
            }
        })).subscribeOn(Schedulers.newThread()).subscribeWith(this);
    }

    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;
        super.onSubscribe(s);
    }

    public FilterInfo getFilterInfo()  {
        return new FilterInfo(isFilterAscending(), ((FilterColumn)table.getSortColumn().getData()).getColumn());
    }
    public boolean isFilterAscending()  {
        return table.getSortDirection() == SWT.UP;
    }

    public abstract Collection<? extends FilterColumn> getColumns();

    @Override
    public void subscribe(ObservableEmitter<FilterInfo> e) throws Exception {
        emitterList.add(e);
        e.setCancellable(new Cancellable() {
            @Override
            public void cancel() throws Exception {
                emitterList.remove(e);
            }
        });

        e.setDisposable(new Disposable() {
            @Override
            public void dispose() {
                emitterList.remove(e);
            }

            @Override
            public boolean isDisposed() {
                return emitterList.contains(e);
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        for(ObservableEmitter<FilterInfo> e: emitterList)
            e.onComplete();
    }
}
