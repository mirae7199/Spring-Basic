package hello.re_core.repository;

import hello.re_core.domain.Member;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MemoryMemberRepository implements MemberRepository{

    // HashMap은 동시성 이슈가 있으니 ConcurrentHashMap을 사용하자.(실무에서)
    private final Map<Long, Member> store = new HashMap<>();

    @Override
    public void save(Member member) {
        store.put(member.getId(), member);
    }

    @Override
    public Member findById(Long id) {
        return store.get(id);
    }

    public void clear() {
        store.clear();
    }
}
