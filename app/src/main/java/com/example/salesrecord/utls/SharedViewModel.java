package com.example.salesrecord.utls;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<Boolean> calcToggle = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> srchToggle = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> margToggle = new MutableLiveData<>(false);


    public LiveData<Boolean> getCalcToggle() {
        return calcToggle;
    }

    public LiveData<Boolean> getSrchToggle() {
        return srchToggle;
    }

    public LiveData<Boolean> getMargToggle() {
        return margToggle;
    }

    /** Alterna el estado de la calculadora */
    public void toggleCalc() {
        Boolean current = calcToggle.getValue();
        calcToggle.setValue(current == null || !current);
    }

    public void setCalcVisible(boolean visible) {
        calcToggle.setValue(visible);
    }

    /** Alterna el estado del buscador */
    public void toggleSrch() {
        Boolean current = srchToggle.getValue();
        srchToggle.setValue(current == null || !current);
    }

    public void setSrchVisible(boolean visible) {
        srchToggle.setValue(visible);
    }

    /** Alterna el estado del input margen */
    public void toggleMarg() {
        Boolean current = margToggle.getValue();
        margToggle.setValue(current == null || !current);
    }

    public void setMargVisible(boolean visible) {
        margToggle.setValue(visible);
    }
}