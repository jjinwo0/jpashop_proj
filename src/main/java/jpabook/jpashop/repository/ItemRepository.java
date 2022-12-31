package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    //저장 전까지 item값이 없음 -> 새로 생성
    //else -> update의 느낌 (이미 db에 있기 때문)
    public void save(Item item){
        if (item.getId() == null){ //초기 등록
            em.persist(item); //영속 상태로 전환
        } else{ //이미 등록된 id -> 준영속 상태
            //merge(): 준영속 상태의 엔티티를 영속 상태로 변경
            em.merge(item);
        }
    }

    public Item findOne(Long id){
        return em.find(Item.class, id);
    }

    public List<Item> findAll(){
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}
