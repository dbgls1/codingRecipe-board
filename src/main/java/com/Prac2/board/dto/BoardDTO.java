package com.Prac2.board.dto;


import com.Prac2.board.entity.BoardEntity;
import com.Prac2.board.entity.BoardFileEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
@NoArgsConstructor //기본 생성자
@AllArgsConstructor //모든 필드를 매개변수로 하는 생성자
public class BoardDTO {

    private Long id;
    private String boardWriter;
    private String boardPass;
    private String boardTitle;
    private String boardContents;
    private int boardHits;
    private LocalDateTime boardCreatedTime;
    private LocalDateTime boardUpdatedTime;

    private List<MultipartFile> boardFile; //save.html -> controller 파일 담는 용도
    private List<String> originalFileName; //원본 파일 이름
    private List<String> storedFileName; //서버 저장용 파일 이름 (서버에서 파일 이름 중복을 피하기 위함)
    private int fileAttached; //파일 첨부 여부(첨부 1, 미첨부 0), (boolean type은 entity에서 처리가 번거로워짐..?)

    //service.Paging에서 활용
    //목록요소만 담는 생성자 만들기
    // alt + insert 단축키 사용
    public BoardDTO(Long id, String boardWriter, String boardTitle, int boardHits,
        LocalDateTime boardCreatedTime) {
        this.id = id;
        this.boardWriter = boardWriter;
        this.boardTitle = boardTitle;
        this.boardHits = boardHits;
        this.boardCreatedTime = boardCreatedTime;
    }

    public static BoardDTO toBoardDTO(BoardEntity boardEntity) {

        BoardDTO boardDTO = new BoardDTO();

        boardDTO.setId(boardEntity.getId());
        boardDTO.setBoardWriter(boardEntity.getBoardWriter());
        boardDTO.setBoardPass(boardEntity.getBoardPass());
        boardDTO.setBoardTitle(boardEntity.getBoardTitle());
        boardDTO.setBoardContents(boardEntity.getBoardContents());
        boardDTO.setBoardHits(boardEntity.getBoardHits());
        boardDTO.setBoardCreatedTime(boardEntity.getCreatedTime());
        boardDTO.setBoardUpdatedTime(boardEntity.getUpdatedTime());

        if (boardEntity.getFileAttached() == 0) {
            boardDTO.setFileAttached(boardEntity.getFileAttached()); //0
        } else {
            List<String> originalFileNameList = new ArrayList<>();
            List<String> storedFileNameList = new ArrayList<>();

            boardDTO.setFileAttached(boardEntity.getFileAttached()); //1
            //다른 테이블에 있는 파일 이름을 가져가야 함.
            //originalFileName, storedFileName : board_file_table(BoardFileEntity)
            //join
            // select * from board_table b, board_file_table bf
            // where b.id=bf.board_id and where b.id=?
//                boardDTO.setOriginalFileName((boardEntity.getBoardFileEntityList().get(0).getOriginalFileName()));
//                boardDTO.setStoredFileName((boardEntity.getBoardFileEntityList().get(0).getStoredFileName()));
            for (BoardFileEntity boardFileEntity : boardEntity.getBoardFileEntityList()) {

                originalFileNameList.add(boardFileEntity.getOriginalFileName());
                storedFileNameList.add(boardFileEntity.getStoredFileName());
            }
            boardDTO.setOriginalFileName(originalFileNameList);
            boardDTO.setStoredFileName(storedFileNameList);

        }
        return boardDTO;
    }
}
