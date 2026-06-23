package kr.elice.library.springboot;

import kr.elice.library.api.LoanService;
import kr.elice.library.domain.LibraryException;
import kr.elice.library.domain.Loan;
import kr.elice.library.platform.CatalogRouter;
import kr.elice.library.store.LoanStore;
import org.springframework.stereotype.Service;

/**
 * 신규(springboot) 대출 모듈입니다. 스트랭글러 전환의 마지막 조각으로, 업무 규칙
 * AC-1·AC-2 를 담은 핵심 자산입니다.
 *
 * <p>레거시와 달리 도서·회원 모듈을 직접 들지 않고 {@link CatalogRouter} 로 활성 구현을
 * 받아 호출합니다. 덕분에 전환 중간 상태에서 도서·회원이 legacy 든 new 든 상관없이
 * 동작합니다. 저장소는 공유 {@link LoanStore} 를 써서 레거시와 같은 데이터를 봅니다.</p>
 *
 * <p>업무 규칙은 두 가지입니다. AC-1 은 한 회원이 동시에 5권을 넘겨 빌릴 수 없다는 것이고,
 * AC-2 는 연체 중인 회원은 새로 빌릴 수 없다는 것입니다.</p>
 */
@Service
public class NewLoanService implements LoanService {

    private static final int LIMIT = 5;

    private final CatalogRouter catalogRouter;
    private final LoanStore store;

    public NewLoanService(CatalogRouter catalogRouter, LoanStore store) {
        this.catalogRouter = catalogRouter;
        this.store = store;
    }

    @Override
    public Loan borrow(String memberId, String bookId, int daysUntilDue) {
        // 전환 중간 상태를 고려해 활성 구현을 매 요청마다 라우터로 해결합니다.
        catalogRouter.members().get(memberId);  // 존재 검증 (없으면 NOT_FOUND)
        catalogRouter.books().get(bookId);       // 존재 검증 (없으면 NOT_FOUND)
        if (activeCount(memberId) >= LIMIT) {                       // AC-1
            throw new LibraryException(LibraryException.Code.LOAN_LIMIT_EXCEEDED,
                    "대출 한도 " + LIMIT + "권을 초과했습니다.");
        }
        if (hasOverdue(memberId)) {                                 // AC-2
            throw new LibraryException(LibraryException.Code.OVERDUE_EXISTS,
                    "연체 중인 대출이 있어 새로 빌릴 수 없습니다.");
        }
        return store.save(new Loan(store.nextId(), memberId, bookId, daysUntilDue));
    }

    @Override
    public void giveBack(String loanId) {
        Loan loan = store.find(loanId).orElseThrow(() ->
                new LibraryException(LibraryException.Code.NOT_FOUND, "대출을 찾을 수 없습니다: " + loanId));
        loan.markReturned();
    }

    @Override
    public int activeCount(String memberId) {
        return store.activeByMember(memberId).size();
    }

    @Override
    public boolean hasOverdue(String memberId) {
        return store.activeByMember(memberId).stream().anyMatch(Loan::isOverdue);
    }
}
