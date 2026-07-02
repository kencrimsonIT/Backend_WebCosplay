package com.example.thuedocosplay.repository.specification;

import com.example.thuedocosplay.entity.OrderItem;
import com.example.thuedocosplay.entity.Product;
import com.example.thuedocosplay.entity.RentalOrder;
import com.example.thuedocosplay.entity.enums.OrderStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class RentalOrderSpecification {

    public static Specification<RentalOrder> sellerId(Long sellerId) {
        return (root, query, cb) -> {

            query.distinct(true);

            Join<RentalOrder, OrderItem> item =
                    root.join("items", JoinType.INNER);

            Join<OrderItem, Product> product =
                    item.join("product", JoinType.INNER);

            return cb.equal(product.get("seller").get("id"), sellerId);
        };
    }

    public static Specification<RentalOrder> status(OrderStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<RentalOrder> fromDate(LocalDate fromDate) {
        return (root, query, cb) -> {
            if (fromDate == null) return null;
            return cb.greaterThanOrEqualTo(root.get("rentFrom"), fromDate);
        };
    }

    public static Specification<RentalOrder> toDate(LocalDate toDate) {
        return (root, query, cb) -> {
            if (toDate == null) return null;
            return cb.lessThanOrEqualTo(root.get("rentTo"), toDate);
        };
    }

    public static Specification<RentalOrder> keyword(String keyword) {

        return (root, query, cb) -> {

            if (keyword == null || keyword.isBlank()) {
                return null;
            }

            String key = "%" + keyword.toLowerCase() + "%";

            Join<RentalOrder, OrderItem> item =
                    root.join("items", JoinType.LEFT);

            return cb.or(

                    cb.like(cb.lower(root.get("orderCode")), key),

                    cb.like(cb.lower(root.get("customerName")), key),

                    cb.like(cb.lower(root.get("customerPhone")), key),

                    cb.like(cb.lower(item.get("productName")), key)

            );
        };
    }

}