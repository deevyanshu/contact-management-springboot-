package com.smartcontact.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartcontact.entities.Contact;
import com.smartcontact.entities.User;

public interface ContactRepository extends JpaRepository<Contact,Integer>{

	@Query(value = "select * from Contact where user_id=:id",nativeQuery = true)
	public Page<Contact> findContactsById(@Param("id") int id,Pageable pageable);// using page list for pagination.
	
	public List<Contact> findByNameContainingAndUser(String name,User user);
}
