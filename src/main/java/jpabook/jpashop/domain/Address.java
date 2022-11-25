package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable //JPA의 내장 타입
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;
}
