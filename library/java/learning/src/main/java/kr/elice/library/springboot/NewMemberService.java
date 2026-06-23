package kr.elice.library.springboot;

import kr.elice.library.api.MemberService;
import kr.elice.library.domain.LibraryException;
import kr.elice.library.domain.Member;
import kr.elice.library.store.MemberStore;
import org.springframework.stereotype.Service;

/**
 * 스프링부트 회원 모듈입니다. 스트랭글러 전환 대상으로 레거시 {@code LegacyMemberService} 를 대체합니다.
 *
 * <p>회원 등록·단건 조회를 제공하며, 공유 {@link MemberStore} 를 생성자 주입으로 사용해
 * 레거시와 동일한 저장소를 공유합니다.</p>
 */
@Service
public class NewMemberService implements MemberService {

    private final MemberStore store;

    /**
     * 공유 회원 저장소를 주입받습니다.
     *
     * @param store 공유 회원 저장소
     */
    public NewMemberService(MemberStore store) {
        this.store = store;
    }

    /**
     * 회원을 등록합니다. (AC-M1)
     *
     * <p>저장소에서 발급한 식별자와 전달된 이름으로 회원을 생성해 저장합니다.</p>
     *
     * @param name 회원 이름
     * @return 저장된 회원
     */
    @Override
    public Member register(String name) {
        return store.save(new Member(store.nextId(), name));
    }

    /**
     * 식별자로 회원을 단건 조회합니다. (AC-M2)
     *
     * @param id 회원 식별자
     * @return 조회된 회원
     * @throws LibraryException 식별자에 해당하는 회원이 없으면 {@link LibraryException.Code#NOT_FOUND} 로 거부합니다.
     */
    @Override
    public Member get(String id) {
        return store.find(id).orElseThrow(() ->
                new LibraryException(LibraryException.Code.NOT_FOUND, "회원을 찾을 수 없습니다: " + id));
    }
}
