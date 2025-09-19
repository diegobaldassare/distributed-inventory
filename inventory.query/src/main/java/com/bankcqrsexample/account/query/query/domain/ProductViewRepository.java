package com.bankcqrsexample.account.query.query.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductViewRepository extends JpaRepository<ProductView, String> {
    List<ProductView> findByStoreId(String storeId);
    List<ProductView> findByCategory(String category);
}
