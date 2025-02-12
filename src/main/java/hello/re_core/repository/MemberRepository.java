package hello.re_core.repository;

import hello.re_core.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    void save(Member member);

    Member findById(Long id);
}
