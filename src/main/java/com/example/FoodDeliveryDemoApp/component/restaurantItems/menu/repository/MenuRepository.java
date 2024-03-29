package com.example.FoodDeliveryDemoApp.component.restaurantItems.menu.repository;

import com.example.FoodDeliveryDemoApp.component.restaurantItems.menu.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
}
