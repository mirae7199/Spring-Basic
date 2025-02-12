package hello.re_core.order;

import hello.re_core.domain.Grade;

public interface OrderService {

    Order createOrder(Long memberId, String itemName, int itemPrice);

}
