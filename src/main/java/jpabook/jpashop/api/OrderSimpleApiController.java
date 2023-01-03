package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    //Entity반환 -> 사용하면 안됨
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all){
            //order.getMember()까지는 프록시 객체
            //.getName()을 호출하는 순간 LAZY 강제 초기화
            order.getMember().getName(); //LAZY 강제 초기화
            order.getDelivery().getAddress(); //LAZY 강제 초기화
        }
        return all;
    }

    //V1과의 공통 문제점: 지연 로딩으로 인한 많은 쿼리 배출
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){

        //order 2개
        //1 + N -> 1 + Member(N개) + Delivery(N개) -> 총 5개의 쿼리 실행
        return orderRepository.findAllByString(new OrderSearch()).stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3(){

        List<Order> orders = orderRepository.findAllWithMemberDelivery(); //메소드 생성
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @Data
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }
}
