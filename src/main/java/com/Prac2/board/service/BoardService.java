package com.Prac2.board.service;

import com.Prac2.board.dto.BoardDTO;
import com.Prac2.board.entity.BoardEntity;
import com.Prac2.board.entity.BoardFileEntity;
import com.Prac2.board.repository.BoardFileRepository;
import com.Prac2.board.repository.BoardRepository;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// DTO(controller) -> Entity(service, repository) : Entity Class에서 구현
// Entity(service, repository) -> DTO (controller) : DTO Class에서 구현
// 수시로 변환필요

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    private final BoardFileRepository boardFileRepository;

    //게시글 저장
    public Long save(BoardDTO boardDTO) throws IOException {
        System.out.println("============"+boardDTO.getBoardFile().isEmpty());
        // 파일 첨부 여부에 따라 로직 분리
        if (boardDTO.getBoardFile().isEmpty()) {

            BoardEntity boardEntity = BoardEntity.toSaveEntity(boardDTO);
            return boardRepository.save(boardEntity).getId();
        } else {
            /*
                1. DTO에 담긴 파일을 꺼냄
                2. 파일의 이름 가져옴
                3. 서버 저장용 이름으로 수정 //System.currentTimeMillis()도 이용 가능
                4. 저장 경로 설정
                5. 해당 경로에 파일 저장
                6. board_table에 해당 데이터 save 처리 //게시글 정보
                7. board_file_table에 해당 데이터 save 처리 //파일 정보
             */
            //BoardFileEntity를 저장하기 위해 부모인 BoardEntity 객체를 다시 전달해야 하는데,
            //boardEntity는 아직 저장하기 전이라 board_id가 생성안되었으므로,
            //저장 후 아이디를 통해 조회한 board 객체를 사용한다
            BoardEntity boardEntity = BoardEntity.toSaveFileEntity(boardDTO);
            Long savedId = boardRepository.save(boardEntity).getId();
            BoardEntity board = boardRepository.findById(savedId).get(); //6번

            for (MultipartFile boardFile : boardDTO.getBoardFile()) {

//                MultipartFile boardFile = boardDTO.getBoardFile(); //1번 , 다중파일첨부에서는 foreach문이 대체함

                String originalFilename = boardFile.getOriginalFilename();

                String storedFileName = UUID.randomUUID() + "_" + originalFilename;

                String savePath = "D:/springboot_img/" + storedFileName;
                //혹은 [System.getProperty("user.dir"): 프로젝트 폴더(board) 경로 가져오기]을 이용할 수도 있다

                boardFile.transferTo(new File(savePath)); //5번, IOException 처리

                BoardFileEntity boardFileEntity
                    = BoardFileEntity.toBoardFileEntity(board, originalFilename, storedFileName);
                boardFileRepository.save(boardFileEntity); //7번
            }
            return savedId;
        }
    }

    //모든 데이터 불러오기(목록에서)
    //toBoardDTO()에서 자식인 boardFileEntity에 접근하므로 @Transactional을 붙여준다
    @Transactional
    public List<BoardDTO> findAll() {

        List<BoardEntity> boardEntityList = boardRepository.findAll();

        List<BoardDTO> boardDTOList = new ArrayList<>();
        for (BoardEntity boardEntity : boardEntityList) {
            boardDTOList.add(BoardDTO.toBoardDTO(boardEntity));
        }

        return boardDTOList;
    }

    //조회수 처리
    //JPA에서 정의된 데이터가 아닌, 사용자 정의 메서드를 사용할 때 붙이는 걸 권함
    //@Transactional이 붙은 메서드가 표함하고 있는 작업 중에 하나라도 실패할 경우 전체 작업을 취소한다
    @Transactional
    public void updateHits(Long id) {

        boardRepository.updateHits(id);
    }

    //아이디로 조회
    //toBoardDTO()에서 자식인 boardFileEntity에 접근하므로 @Transactional을 붙여준다
    @Transactional
    public BoardDTO findById(Long id) {

        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(id);

        if (optionalBoardEntity.isPresent()) {

            BoardEntity boardEntity = optionalBoardEntity.get();
            BoardDTO boardDTO = BoardDTO.toBoardDTO(boardEntity);

            return boardDTO;

        } else {

            return null;
        }
    }

    //게시글 수정
    @Transactional//이걸 붙이니 LazyInitializationExcetion이 해결됨
    public BoardDTO update(BoardDTO boardDTO) {

        //글 업데이트
        BoardEntity boardEntity = BoardEntity.toUpdateEntity(boardDTO);
        Long updatedId = boardRepository.save(boardEntity).getId();

        return findById(updatedId);
    }

    //삭제
    public void delete(Long id) {

        boardRepository.deleteById(id);
    }

    //페이징 처리
    public Page<BoardDTO> paging(Pageable pageable) {

        int page = pageable.getPageNumber() - 1; //보고싶은 페이지, 0부터 시작하므로
        int pageLimit = 3;  //한 페이지에 보여줄 글 갯수

        //한 페이지당 3개씩 글을 보여주고, id 기준으로 내림차순 정렬
        Page<BoardEntity> boardEntities =                                                   // 엔티티 작성 기준
            boardRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Direction.DESC, "id")));

        //여기서 쓰이는 boardEntities 메서드들
        //entity를 Dto로 변환시키면서 List형으로 가져갈 경우 아래 메서드를 사용할 수 없다
        System.out.println(
            "boardEntities.getContent() = " + boardEntities.getContent()); // 요청 페이지에 해당하는 글
        System.out.println(
            "boardEntities.getTotalElements() = " + boardEntities.getTotalElements()); // 전체 글갯수
        System.out.println(
            "boardEntities.getNumber() = " + boardEntities.getNumber()); // DB로 요청한 페이지 번호
        System.out.println(
            "boardEntities.getTotalPages() = " + boardEntities.getTotalPages()); // 전체 페이지 갯수
        System.out.println(
            "boardEntities.getSize() = " + boardEntities.getSize()); // 한 페이지에 보여지는 글 갯수
        System.out.println(
            "boardEntities.hasPrevious() = " + boardEntities.hasPrevious()); // 이전 페이지 존재 여부
        System.out.println("boardEntities.isFirst() = " + boardEntities.isFirst()); // 첫 페이지 여부
        System.out.println("boardEntities.isLast() = " + boardEntities.isLast()); // 마지막 페이지 여부

        //List가 아닌 Page형으로 가져가기 위해 Page.map()을 이용한다
        //목록에서 필요한 요소: id, writer, title, hits, createdTime
        Page<BoardDTO> boardDTOS = boardEntities.map(board -> new BoardDTO(board.getId(),
            board.getBoardWriter(), board.getBoardTitle(), board.getBoardHits(),
            board.getCreatedTime()));

        return boardDTOS;
    }
}























