package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    @Test
    public void 회원가입() throws Exception{
        //given
        Member member = new Member();
        member.setName("Park");

        //when
        Long savedId = memberService.join(member);

        //then
        em.flush();
        Assert.assertEquals(member, memberRepository.findOne(savedId));

        //@Transactional은 default로 Rollback을 실행하므로 insert문이 생성되지 않음.
        //persist를 하고 commit동작을 하지 않으면 insert문을 생성하지 않음.
        //commit동작 전에 rollback 작업
        //EntityManager를 생성하고 flush() 작업을 하면 실제 DB에 insert
        //따라서 insert문을 확인하고 rollback을 하고싶다면 em.flush()사용
    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception{
        //given
        Member member1 = new Member();
        member1.setName("Park");
        Member member2 = new Member();
        member2.setName("Park");

        //when
        memberService.join(member1);
        memberService.join(member2); //어노테이션의 사용으로 try...catch 사용과 동일

        //then
        fail("예외가 발생해야 한다.");
    }
}