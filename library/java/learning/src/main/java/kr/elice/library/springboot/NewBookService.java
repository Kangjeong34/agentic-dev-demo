package kr.elice.library.springboot;

import java.util.List;
import kr.elice.library.api.BookService;
import kr.elice.library.domain.Book;
import kr.elice.library.domain.LibraryException;
import kr.elice.library.store.BookStore;
import org.springframework.stereotype.Service;

/**
 * 신규 도서 모듈입니다. 스트랭글러 전환 과정에서 레거시 {@code LegacyBookService} 를
 * 스펙으로 풀어 springboot 패키지에 새로 구현한 코드입니다.
 *
 * <p>저장소는 레거시와 동일한 공유 {@link BookStore} 를 사용합니다. 따라서 도서 모듈만
 * 신규로 전환해도, 아직 레거시인 다른 모듈이 같은 도서 데이터를 그대로 찾을 수 있습니다.</p>
 *
 * <p>스프링 기본 빈 이름은 {@code newBookService} 이며, 라우터가 이 이름으로 신규 구현을
 * 자동 선택합니다. 따라서 명시적 빈 이름을 지정하지 않습니다.</p>
 */
@Service
public class NewBookService implements BookService {

    private final BookStore store;

    public NewBookService(BookStore store) {
        this.store = store;
    }

    /**
     * AC-B1: 도서 등록 요청이 올 때, 식별자와 제목을 가진 도서를 생성해 저장합니다.
     *
     * @param title 도서 제목
     * @return 저장된 도서
     */
    @Override
    public Book register(String title) {
        return store.save(new Book(store.nextId(), title));
    }

    /**
     * AC-B2: 식별자로 도서를 단건 조회합니다. 없는 식별자면 {@code NOT_FOUND} 로 거부합니다.
     *
     * @param id 도서 식별자
     * @return 조회된 도서
     * @throws LibraryException 식별자에 해당하는 도서가 없을 때
     */
    @Override
    public Book get(String id) {
        return store.find(id).orElseThrow(() ->
                new LibraryException(LibraryException.Code.NOT_FOUND, "도서를 찾을 수 없습니다: " + id));
    }

    /**
     * AC-B3: 목록 조회 요청이 올 때, 등록된 도서 전체를 돌려줍니다.
     *
     * @return 등록된 도서 전체 목록
     */
    @Override
    public List<Book> list() {
        return store.all();
    }
}
