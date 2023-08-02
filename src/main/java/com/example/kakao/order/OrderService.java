package com.example.kakao.order;

import com.example.kakao._core.errors.exception.Exception400;
import com.example.kakao.cart.Cart;
import com.example.kakao.cart.CartJPARepository;
import com.example.kakao.order.item.Item;
import com.example.kakao.order.item.ItemJPARepository;
import com.example.kakao.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderJPARepository orderJPARepository;
    private final ItemJPARepository itemJPARepository;
    private final CartJPARepository cartJPARepository;

    @Transactional
    public OrderResponse.SaveDTO saveAll(User sessionuser){

        List<Cart> cartList = cartJPARepository.findByUserIdOrderByOptionIdAsc(sessionuser.getId());
        //1. 카트에 아무것도 없으면 예외 처리
        if( cartList.isEmpty() ){
            throw new Exception400("카트가 비어있습니다.");
        }

        Order order = Order.builder().user(sessionuser).build();
        Order orderPS = orderJPARepository.save(order);
        List<Item> itemList = cartList.stream()
                .map( cart -> Item.builder()
                        .option(cart.getOption())
                        .order(orderPS)
                        .quantity(cart.getQuantity())
                        .price(cart.getPrice())
                        .build())
                .collect(Collectors.toList());

        List<Item> itemListPS = itemJPARepository.saveAll(itemList);

        //2. 장바구니 초기화
        cartJPARepository.deleteAll(cartList);

        return new OrderResponse.SaveDTO(orderPS,itemListPS);
    }

    @Transactional
    public OrderResponse.FindByIdDTO findById(int id,User sessionuser){
        Optional<Order> orderPS = orderJPARepository.findById(id);
        if (orderPS.isPresent()) {
            List<Item> itemListPS = itemJPARepository.findByOrderId(id);
            return new OrderResponse.FindByIdDTO(id, itemListPS);
        }
        else {
            throw new Exception400("존재하지 않는 주문 번호");
        }
//        //주문번호가 없을 경우 예외 처리
//        if ( orderJPARepository.findById(id).isEmpty()){
//            throw new Exception400("해당 주문번호의 주문이 없습니다.");
//        }
//
//
//        List<Item> itemList = itemJPARepository.findByOrderId(id);
//
//        return new OrderResponse.FindByIdDTO(id,itemList);

    }
}
