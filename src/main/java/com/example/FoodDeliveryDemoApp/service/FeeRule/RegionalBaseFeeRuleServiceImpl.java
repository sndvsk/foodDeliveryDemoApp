package com.example.FoodDeliveryDemoApp.service.FeeRule;

import com.example.FoodDeliveryDemoApp.model.rules.RegionalBaseFeeRule;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public abstract class RegionalBaseFeeRuleServiceImpl implements FeeRuleService {


    public List<RegionalBaseFeeRule> getAllRegionalBaseFeeRules() {
        return null;
    }

    public RegionalBaseFeeRule addBaseFeeRule() {
        return null;
    }

    public RegionalBaseFeeRule getRegionalBaseFeeRuleById(Long id) {
        return null;
    }

    public RegionalBaseFeeRule patchRegionalBaseFeeRuleById(Long id) {
        return null;
    }

    public String deleteRegionalBaseFeeRuleById(Long id) {
        return null;
    }

}
