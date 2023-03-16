package com.example.FoodDeliveryDemoApp.repository;

import com.example.FoodDeliveryDemoApp.model.OrderData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDataRepository extends JpaRepository<OrderData, Long> {

    List<OrderData> findByWeatherId(Long weatherId);

    OrderData findAllByCity(String city);
}

