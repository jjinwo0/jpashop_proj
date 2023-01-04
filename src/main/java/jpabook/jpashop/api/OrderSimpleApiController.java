package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

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

    //fetch join을 활용하여 원하는 데이터만 뽑아냄
    //다양한 API에서 적합하게 변환하여 사용하기 쉬움: fetch join 때문 (적합한 변환 DTO)
    //쿼리가 굉장히 복잡해지기 때문에 성능 면에서 비교적 떨어짐
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3(){

        List<Order> orders = orderRepository.findAllWithMemberDelivery(); //메소드 생성
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    //원하는 데이터에 딱 맞게 조회 쿼리가 작성되었기 때문에 로직을 재활용할 수 없음.
    //성능 면에서 V3보다 최적화됨.
    //DTO를 조회하였기 때문에 변경할 수 없음.
    //코드가 정돈되지 않음.
    //API 스펙이 바뀌면 DTO 자체를 뜯어고쳐야됨.
    //repository는 엔티티를 조회하는 데 사용되어야  -> DTO를 조회하게 되면 API스펙 자체가 들어가버림
    //repository내부에 패키지를 새로 파서 사용하는 방식으로 문제점 해결
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return orderSimpleQueryRepository.findOrderDtos();
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
