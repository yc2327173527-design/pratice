package com.yupi.yupicturebackend.model.vo;

import com.yupi.yupicturebackend.model.entity.Note;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * Note view
 */
@Data
public class NoteVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * note content
     */
    private String content;

    /**
     * creator user id
     */
    private Long userId;

    /**
     * create time
     */
    private Date createTime;

    /**
     * last edit time
     */
    private Date editTime;

    /**
     * update time
     */
    private Date updateTime;

    /**
     * creator info
     */
    private UserVO user;

    private static final long serialVersionUID = 1L;

    public static Note voToObj(NoteVO noteVO) {
        if (noteVO == null) {
            return null;
        }
        Note note = new Note();
        BeanUtils.copyProperties(noteVO, note);
        return note;
    }

    public static NoteVO objToVo(Note note) {
        if (note == null) {
            return null;
        }
        NoteVO noteVO = new NoteVO();
        BeanUtils.copyProperties(note, noteVO);
        return noteVO;
    }
}
