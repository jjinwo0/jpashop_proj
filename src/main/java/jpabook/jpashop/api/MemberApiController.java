package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

//@ResponseBody: Data 자체를 바로 XML이나 JSON으로 보냄
@RestController //Controller와 ResponseBody를 합침
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @GetMapping("/api/v1/members")
    public List<Member> membersV1(){
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result memberV2(){
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers
                .stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect.size(), collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T>{
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto{
        private String name;
    }

    //V1의 장점: 클래스를 따로 만들지 않고 엔티티를 직접 사용하는 간편함
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        //@RequestBody: JSON으로 넘어온 데이터 중 body를 member에 넣어줌
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    //"API를 만들때에는 엔티티를 파라미터로 받으면 안된다."

    /**
     * V2의 장점
     * 1. 엔티티의 컬럼 등 값의 변화가 발생했을 시, 알아서 메서드 내에 컴파일 에러가 발생함으로써 문제를 예방할 수 있다.
     * 2. API 스펙에 변동사항이 생기지 않음
     * 3. 엔티티에 직접 영향을 미치지 않고 별도의 객체(DTO)를 사용함으로써 유지보수에 도움이 된다.
     */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){
        Member member = new Member();
        member.setName(request.name);

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }


    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2
            (@PathVariable("id") Long id,
             @RequestBody @Valid UpdateMemberRequest request){

        System.out.println(request.getName());
        Member findMember = memberService.findOne(id);

        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    static class CreateMemberRequest{
        @NotEmpty
        private String name;
    }

    @Data
    static class CreateMemberResponse{
        public CreateMemberResponse(Long id) {
            this.id = id;
        }

        private Long id;
    }

    @Data
    static class UpdateMemberRequest{
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse{
        private Long id;
        private String name;
    }
}
