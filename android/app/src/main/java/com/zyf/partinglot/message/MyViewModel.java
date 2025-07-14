package com.zyf.partinglot.message;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyViewModel extends ViewModel {
    private final MutableLiveData<String> rate1 = new MutableLiveData<>();
    private final MutableLiveData<String> rate2 = new MutableLiveData<>();
    private final MutableLiveData<String> totalSpaces = new MutableLiveData<>();
    private final MutableLiveData<String> availableSpaces = new MutableLiveData<>();
    private final MutableLiveData<Integer> reservedSpaceNumber = new MutableLiveData<>();
    private final MutableLiveData<Boolean> startOrEnd = new MutableLiveData<>();
    private final MutableLiveData<Long> startTime = new MutableLiveData<>();
    private final MutableLiveData<Double> costMoney = new MutableLiveData<>();

    public LiveData<String> getRate1() {
        return rate1;
    }

    public LiveData<String> getRate2() {
        return rate2;
    }

    public LiveData<String> getTotalSpaces() {
        return totalSpaces;
    }

    public LiveData<String> getAvailableSpaces() {
        return availableSpaces;
    }

    public LiveData<Integer> getReservedSpaceNumber() {
        return reservedSpaceNumber;
    }

    public LiveData<Double> getCostMoney(){
        return costMoney;
    }
    public LiveData<Boolean> getStartOrEnd() {
        return startOrEnd;
    }

    public LiveData<Long> getStartTime() {
        return startTime;
    }

    // 如果你希望外部代码可以修改这些 LiveData 的值，提供以下方法。
    // 否则，如果不需要外部修改，请删除这些方法以保护内部状态。
    public void setRate1(String value) {
        rate1.setValue(value); // 使用 postValue 保证线程安全
    }

    public void setRate2(String value) {
        rate2.setValue(value);
    }

    public void setTotalSpaces(String value) {
        totalSpaces.setValue(value);
    }

    public void setAvailableSpaces(String value) {
        availableSpaces.setValue(value);
    }

    public void setReservedSpaceNumber(Integer value) {
        reservedSpaceNumber.setValue(value);
    }

    public void setStartOrEnd(Boolean value) {
        startOrEnd.setValue(value);
    }

    public void setStartTime(Long value) {
        startTime.setValue(value);
    }

    public void setCostMoney(Double value){
        costMoney.setValue(value);
    }


}
