package com.stefanbildl;

import io.reactivex.Flowable;

/**
 * Created by stefan on 09.07.17.
 */
public abstract class FilterBasedFlowableFactory<T> {
    public abstract Flowable<T> newFlowable(FilterInfo filterInfo);
}
