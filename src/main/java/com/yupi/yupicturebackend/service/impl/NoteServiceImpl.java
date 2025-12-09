package com.yupi.yupicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import com.yupi.yupicturebackend.mapper.NoteMapper;
import com.yupi.yupicturebackend.model.dto.note.NoteAddRequest;
import com.yupi.yupicturebackend.model.dto.note.NoteUpdateRequest;
import com.yupi.yupicturebackend.model.entity.Note;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.NoteVO;
import com.yupi.yupicturebackend.model.vo.UserVO;
import com.yupi.yupicturebackend.service.NoteService;
import com.yupi.yupicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Note service
 */
@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    @Resource
    private UserService userService;

    @Override
    public long addNote(NoteAddRequest noteAddRequest, User loginUser) {
        ThrowUtils.throwIf(noteAddRequest == null, ErrorCode.PARAMS_ERROR);
        Note note = new Note();
        note.setContent(StrUtil.trimToEmpty(noteAddRequest.getContent()));
        note.setUserId(loginUser.getId());
        note.setEditTime(new Date());
        validNote(note, true);
        boolean result = this.save(note);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return note.getId();
    }

    @Override
    public boolean updateNote(NoteUpdateRequest noteUpdateRequest, User loginUser) {
        if (noteUpdateRequest == null || noteUpdateRequest.getId() == null || noteUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Note oldNote = this.getById(noteUpdateRequest.getId());
        ThrowUtils.throwIf(oldNote == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!oldNote.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        Note note = new Note();
        BeanUtils.copyProperties(noteUpdateRequest, note);
        note.setContent(StrUtil.trimToEmpty(noteUpdateRequest.getContent()));
        note.setEditTime(new Date());
        validNote(note, false);
        boolean result = this.updateById(note);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public List<NoteVO> listMyNotes(User loginUser) {
        QueryWrapper<Note> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        queryWrapper.orderByDesc("updateTime");
        List<Note> noteList = this.list(queryWrapper);
        return getNoteVOList(noteList);
    }

    @Override
    public NoteVO getNoteVO(Note note) {
        NoteVO noteVO = NoteVO.objToVo(note);
        if (noteVO == null) {
            return null;
        }
        Long userId = note.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            noteVO.setUser(userVO);
        }
        return noteVO;
    }

    @Override
    public List<NoteVO> getNoteVOList(List<Note> noteList) {
        if (CollUtil.isEmpty(noteList)) {
            return Collections.emptyList();
        }
        List<NoteVO> noteVOList = noteList.stream().map(NoteVO::objToVo).collect(Collectors.toList());
        Set<Long> userIdSet = noteList.stream().map(Note::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        noteVOList.forEach(noteVO -> {
            Long userId = noteVO.getUserId();
            if (userIdUserListMap.containsKey(userId)) {
                User user = userIdUserListMap.get(userId).get(0);
                noteVO.setUser(userService.getUserVO(user));
            }
        });
        return noteVOList;
    }

    @Override
    public void validNote(Note note, boolean add) {
        ThrowUtils.throwIf(note == null, ErrorCode.PARAMS_ERROR);
        String content = note.getContent();
        ThrowUtils.throwIf(StrUtil.isBlank(content), ErrorCode.PARAMS_ERROR, "note content cannot be empty");
        ThrowUtils.throwIf(content.length() > 5000, ErrorCode.PARAMS_ERROR, "note content is too long");
        if (add) {
            ThrowUtils.throwIf(note.getUserId() == null || note.getUserId() <= 0, ErrorCode.PARAMS_ERROR);
        }
    }

    @Override
    public boolean deleteNote(Long noteId, User loginUser) {
        if (noteId == null || noteId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Note oldNote = this.getById(noteId);
        ThrowUtils.throwIf(oldNote == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!oldNote.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        boolean result = this.removeById(noteId);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }
}
