package com.yupi.yupicturebackend.model.dto.note;

import lombok.Data;

import java.io.Serializable;

/**
 * Update note request
 */
@Data
public class NoteUpdateRequest implements Serializable {

    /**
     * note id
     */
    private Long id;

    /**
     * note content
     */
    private String content;

    private static final long serialVersionUID = 1L;
}
