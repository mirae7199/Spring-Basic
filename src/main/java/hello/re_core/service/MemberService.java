package hello.re_core.service;

import hello.re_core.domain.Member;

public interface MemberService {
    void join(Member member);

    Member findMember(Long memberId);

}
