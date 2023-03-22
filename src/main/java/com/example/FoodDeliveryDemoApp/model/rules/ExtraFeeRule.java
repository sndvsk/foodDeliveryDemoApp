package com.example.FoodDeliveryDemoApp.model.rules;

public abstract class ExtraFeeRule implements FeeRule {

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public abstract Double getFee();
    public abstract void setFee(Double fee);

}
