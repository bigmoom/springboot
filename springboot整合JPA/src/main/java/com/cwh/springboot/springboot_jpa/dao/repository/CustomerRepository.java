package com.cwh.springboot.springboot_jpa.dao.repository;

import com.cwh.springboot.springboot_jpa.dao.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.LinkedList;

/**
 * @author cwh
 * @date 2021/6/7 16:28
 */
public interface CustomerRepository extends JpaRepository<Customer,Integer> {

    LinkedList<Customer> findByName(String name);

}
