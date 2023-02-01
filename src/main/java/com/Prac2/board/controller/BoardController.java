package com.Prac2.board.controller;

import com.Prac2.board.dto.BoardDTO;
import com.Prac2.board.dto.CommentDTO;
import com.Prac2.board.service.BoardService;
import com.Prac2.board.service.CommentService;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor //생성자 주입방식으로 의존성 주입
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    //글작성폼 불러오기
    @GetMapping("/save")
    public String saveForm() {

        return "save";
    }

    //글작성 처리하기
    @PostMapping("/save")
    public String save(@ModelAttribute BoardDTO boardDTO) throws IOException {

        Long savedId = boardService.save(boardDTO);

        return "redirect:/board/" + savedId;
    }

    //글목록
    @GetMapping("/")
    public String findAll(Model model) {

        List<BoardDTO> boardDTOList = boardService.findAll();
        model.addAttribute("boardList", boardDTOList);

        return "list";
    }

    //글 상세페이지
    @GetMapping("/{id}")
    public String findById(@PathVariable Long id, Model model, @PageableDefault(page=1) Pageable pageable) {
        /*
         해당 게시글의 조회수 올리기
         게시글 데이터 가져와서 detail.html에 출력
        */
        boardService.updateHits(id);
        BoardDTO boardDTO = boardService.findById(id);

        //댓글목록 가져오기
        List<CommentDTO> commentDTOList = commentService.findAll(id);
        model.addAttribute("commentList", commentDTOList);

        model.addAttribute("board", boardDTO);
        model.addAttribute("page", pageable.getPageNumber());

        return "detail";
    }

    //글 수정폼 불러오기
    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable Long id, Model model) {

        BoardDTO boardDTO = boardService.findById(id);
        model.addAttribute("boardUpdate", boardDTO);

        return "update";
    }

    //글 수정 처리
    @PostMapping("/update")
    public String update(@ModelAttribute BoardDTO boardDTO, Model model, @PageableDefault(page=1) Pageable pageable) {

        BoardDTO board = boardService.update(boardDTO);
        model.addAttribute("board", board);
        model.addAttribute("page", pageable.getPageNumber()); //수정 후, 목록버튼 누르면 1 페이지로 이동

        return "redirect:/board/" + board.getId();
    //  return "redirect:/board/" + boardDTO.getId();  //수정만으로 조회수가 올라갈 우려가 있어서
    }

    //글 삭제
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {

        boardService.delete(id);

        return "redirect:/board/paging";
    }

    //페이징 불러오기
    // /board/paging?page=1
    @GetMapping("/paging")
    public String paging(@PageableDefault(page = 1) Pageable pageable, Model model) {

        //pageable.getPageNumber(): 현재페이지
        Page<BoardDTO> boardList = boardService.paging(pageable);

        int blockLimit = 3; //하단에 보여지는 페이지 갯수
        int startPage = (((int)(Math.ceil((double)pageable.getPageNumber() / blockLimit))) - 1) * blockLimit + 1; // 1 4 7 10 ~~
        int endPage = ((startPage + blockLimit - 1) < boardList.getTotalPages()) ? startPage + blockLimit - 1 : boardList.getTotalPages();

        // page 갯수 20개
        // 하단에 보여지는 페이지 갯수 3개
        // 현재 사용자가 3페이지를 보고있다면,
        // 1 2 3
        // 현재 사용자가 7페이지..
        // 7 8 (9)
        // 총 페이지 갯수 8개

        model.addAttribute("boardList", boardList);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "paging";
    }

}
