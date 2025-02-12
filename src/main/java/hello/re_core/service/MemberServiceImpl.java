package hello.re_core.service;

import hello.re_core.domain.Member;
import hello.re_core.repository.MemberRepository;
import hello.re_core.repository.MemoryMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberServiceImpl implements MemberService{
    private final MemberRepository memberRepository;

    @Autowired
    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository; // 외부에서 주입
    }

    public MemberRepository getMemberRepository() {
        return new MemoryMemberRepository();
    }

    // 생성자 의존관계 주입

    @Override
    public void join(Member member) {
        memberRepository.save(member);
    }

    @Override
    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId);
    }
}
