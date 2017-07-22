package com.stefanbildl.test;

import com.stefanbildl.*;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Cancellable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by stefan on 09.07.17.
 */
class ReactiveTableTest {

    private Shell shell;
    private Display display;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        display = new Display();
        shell = new Shell(display);
        shell.setLayout(new FillLayout());
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        shell.dispose();
        display.dispose();
    }

    @Test
    public void testTable() throws Throwable {
        FilterableReactiveTable<Integer> countTable = new FilterableReactiveTable<Integer>(shell, SWT.NONE, new FilterBasedFlowableFactory<Integer>() {
            @Override
            public Flowable<Integer> newFlowable(final FilterInfo filterInfo) {
                return
                Flowable.fromArray(1,2,3,4,5).sorted((i,j)-> filterInfo.isAscending()?i.compareTo(j):j.compareTo(i));
            }
        }) {
            @Override
            public Collection<? extends FilterColumn> getColumns() {
                FilterColumn c = new FilterColumn("Test1","label1", 100);
                FilterColumn c2 = new FilterColumn("Test2","label2", 100);
                return Arrays.asList(c, c2);
            }

            @Override
            protected void fillItem(TableItem item, Integer element) {
                item.setText(new String[] {String.valueOf(element), String.valueOf(element)});
            }
        };




        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }


}