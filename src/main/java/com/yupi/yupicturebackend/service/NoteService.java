package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.model.dto.note.NoteAddRequest;
import com.yupi.yupicturebackend.model.dto.note.NoteUpdateRequest;
import com.yupi.yupicturebackend.model.entity.Note;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.NoteVO;

import java.util.List;

/**
 * Service for note
 */
public interface NoteService extends IService<Note> {

    /**
     * create note
     *
     * @param noteAddRequest request
     * @param loginUser      operator
     * @return note id
     */
    long addNote(NoteAddRequest noteAddRequest, User loginUser);

    /**
     * update note content
     *
     * @param noteUpdateRequest request
     * @param loginUser         operator
     * @return result
     */
    boolean updateNote(NoteUpdateRequest noteUpdateRequest, User loginUser);

    /**
     * list current user's notes
     *
     * @param loginUser operator
     * @return note view list
     */
    List<NoteVO> listMyNotes(User loginUser);

    /**
     * convert entity to view
     *
     * @param note entity
     * @return view
     */
    NoteVO getNoteVO(Note note);

    /**
     * convert list of entity to view list
     *
     * @param noteList entity list
     * @return view list
     */
    List<NoteVO> getNoteVOList(List<Note> noteList);

    /**
     * delete own note (logical delete)
     *
     * @param noteId    note id
     * @param loginUser operator
     * @return result
     */
    boolean deleteNote(Long noteId, User loginUser);

    /**
     * basic validation
     *
     * @param note entity
     * @param add  create or update
     */
    void validNote(Note note, boolean add);
}
