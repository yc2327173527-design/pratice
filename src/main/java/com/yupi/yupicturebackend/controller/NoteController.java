package com.yupi.yupicturebackend.controller;

import com.yupi.yupicturebackend.common.BaseResponse;
import com.yupi.yupicturebackend.common.ResultUtils;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import com.yupi.yupicturebackend.model.dto.note.NoteAddRequest;
import com.yupi.yupicturebackend.model.dto.note.NoteUpdateRequest;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.NoteVO;
import com.yupi.yupicturebackend.service.NoteService;
import com.yupi.yupicturebackend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Note endpoints
 */
@RestController
@RequestMapping("/note")
public class NoteController {

    @Resource
    private NoteService noteService;

    @Resource
    private UserService userService;

    /**
     * create note
     */
    @PostMapping("/add")
    public BaseResponse<Long> addNote(@RequestBody NoteAddRequest noteAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(noteAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long newId = noteService.addNote(noteAddRequest, loginUser);
        return ResultUtils.success(newId);
    }

    /**
     * update note
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateNote(@RequestBody NoteUpdateRequest noteUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(noteUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = noteService.updateNote(noteUpdateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * list current user's notes
     */
    @GetMapping("/my/list")
    public BaseResponse<List<NoteVO>> listMyNotes(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<NoteVO> noteVOList = noteService.listMyNotes(loginUser);
        return ResultUtils.success(noteVOList);
    }
}
